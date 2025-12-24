const http = require('http');
const crypto = require('crypto');
const mysql = require('mysql2/promise');
const nodemailer = require('nodemailer');

const PORT = Number(process.env.PORT || 8081);
const SECRET = 'agrotrace-secret';

const users = [];
const resetCodes = new Map();
const blacklisted = new Set();
let lastUserInfo = null;

const DB_HOST = process.env.DB_HOST || 'localhost';
const DB_PORT = Number(process.env.DB_PORT || 3306);
const DB_USER = process.env.DB_USER || 'root';
const DB_PASSWORD = process.env.DB_PASSWORD || '';
const DB_NAME = process.env.DB_NAME || 'agricol';

let pool;
let mailer;
async function initDb() {
  pool = mysql.createPool({ host: DB_HOST, port: DB_PORT, user: DB_USER, password: DB_PASSWORD, database: DB_NAME, waitForConnections: true, connectionLimit: 10 });
  await pool.query(
    'CREATE TABLE IF NOT EXISTS users (\n      id INT UNSIGNED NOT NULL AUTO_INCREMENT,\n      firstname VARCHAR(100) NOT NULL,\n      lastname VARCHAR(100) NOT NULL,\n      email VARCHAR(190) NOT NULL UNIQUE,\n      tel VARCHAR(50) NOT NULL,\n      localisation_id VARCHAR(100) NULL,\n      captors_id VARCHAR(100) NULL,\n      resetToken VARCHAR(100) NULL,\n      resetTokenExpiry BIGINT NULL,\n      password_hash VARCHAR(255) NOT NULL,\n      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,\n      PRIMARY KEY (id)\n    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4'
  );
  await pool.query(
    'CREATE TABLE IF NOT EXISTS reset_codes (\n      id INT UNSIGNED NOT NULL AUTO_INCREMENT,\n      email VARCHAR(190) NOT NULL,\n      code VARCHAR(20) NOT NULL,\n      expiry BIGINT NOT NULL,\n      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,\n      PRIMARY KEY (id),\n      INDEX (email)\n    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4'
  );
}

async function initMailer() {
  const host = process.env.SMTP_HOST;
  const port = Number(process.env.SMTP_PORT || 0);
  const user = process.env.SMTP_USER;
  const pass = process.env.SMTP_PASS;
  const from = process.env.SMTP_FROM || 'no-reply@agricol.local';
  if (host && port && user && pass) {
    mailer = nodemailer.createTransport({ host, port, secure: port === 465, auth: { user, pass } });
    mailer.fromAddress = from;
    console.log('SMTP mailer enabled:', host, port);
  } else {
    try {
      const account = await nodemailer.createTestAccount();
      mailer = nodemailer.createTransport({ host: account.smtp.host, port: account.smtp.port, secure: account.smtp.secure, auth: { user: account.user, pass: account.pass } });
      mailer.fromAddress = from;
      console.log('SMTP test mailer enabled (Ethereal):', account.smtp.host, account.smtp.port);
    } catch (e) {
      mailer = null;
      console.log('SMTP mailer disabled: set SMTP_* env vars to enable email sending');
    }
  }
}

async function trySendEmail(to, code) {
  const apiKey = process.env.SENDGRID_API_KEY || '';
  const from = process.env.SMTP_FROM || 'no-reply@agricol.local';
  if (apiKey) {
    const https = require('https');
    const payload = JSON.stringify({
      personalizations: [{ to: [{ email: to }] }],
      from: { email: from },
      subject: 'Your password reset code',
      content: [{ type: 'text/plain', value: `Your verification code is ${code}. It expires in 15 minutes.` }],
    });
    const opts = {
      hostname: 'api.sendgrid.com',
      port: 443,
      path: '/v3/mail/send',
      method: 'POST',
      headers: { 'Authorization': `Bearer ${apiKey}`, 'Content-Type': 'application/json', 'Content-Length': Buffer.byteLength(payload) },
    };
    const result = await new Promise((resolve) => {
      const req = https.request(opts, (res) => {
        let data = '';
        res.on('data', (c) => data += c);
        res.on('end', () => resolve({ status: res.statusCode || 0, body: data }));
      });
      req.on('error', (e) => resolve({ status: 0, body: String(e) }));
      req.write(payload);
      req.end();
    });
    if (result.status === 202) {
      console.log('Reset code email sent via SendGrid to', to);
      return { sent: true };
    } else {
      console.log('SendGrid send failed:', result.status, result.body);
    }
  }
  if (mailer) {
    try {
      const info = await mailer.sendMail({ from: mailer.fromAddress, to, subject: 'Your password reset code', text: `Your verification code is ${code}. It expires in 15 minutes.` });
      const url = nodemailer.getTestMessageUrl(info);
      console.log('Reset code email sent to', to, url ? '(preview)' : '');
      return { sent: true, previewUrl: url || null };
    } catch (e) {
      console.log('SMTP send failed:', e && e.message ? e.message : String(e));
    }
  }
  return { sent: false };
}

function parseBody(req) {
  return new Promise((resolve) => {
    let data = '';
    req.on('data', (chunk) => { data += chunk; });
    req.on('end', () => {
      const ct = req.headers['content-type'] || '';
      if (ct.includes('application/json')) {
        try { resolve(JSON.parse(data || '{}')); } catch { resolve({}); }
      } else if (ct.includes('application/x-www-form-urlencoded')) {
        const o = {};
        data.split('&').forEach(p => {
          const [k, v] = p.split('=');
          if (k) o[decodeURIComponent(k)] = decodeURIComponent((v || '').replace(/\+/g, ' '));
        });
        resolve(o);
      } else {
        resolve({});
      }
    });
  });
}

function json(res, code, obj) {
  const body = JSON.stringify(obj);
  res.writeHead(code, { 'Content-Type': 'application/json' });
  res.end(body);
}

function text(res, code, str) {
  res.writeHead(code, { 'Content-Type': 'text/plain' });
  res.end(str);
}

function hashPassword(pw) {
  return crypto.createHash('sha256').update(pw).digest('hex');
}

function base64url(input) {
  return Buffer.from(input).toString('base64').replace(/=/g, '').replace(/\+/g, '-').replace(/\//g, '_');
}

function signJwt(payload) {
  const header = { alg: 'HS256', typ: 'JWT' };
  const h = base64url(JSON.stringify(header));
  const p = base64url(JSON.stringify(payload));
  const data = `${h}.${p}`;
  const sig = crypto.createHmac('sha256', SECRET).update(data).digest('base64').replace(/=/g, '').replace(/\+/g, '-').replace(/\//g, '_');
  return `${data}.${sig}`;
}

function verifyJwt(token) {
  if (!token) return false;
  if (blacklisted.has(token)) return false;
  const parts = token.split('.');
  if (parts.length !== 3) return false;
  const [h, p, s] = parts;
  const data = `${h}.${p}`;
  const exp = crypto.createHmac('sha256', SECRET).update(data).digest('base64').replace(/=/g, '').replace(/\+/g, '-').replace(/\//g, '_');
  return exp === s;
}

function parseUrl(url) {
  const u = new URL(url, `http://localhost:${PORT}`);
  return u;
}

function sendCode(email) {
  const code = String(Math.floor(100000 + Math.random() * 900000));
  const expiry = Date.now() + 15 * 60 * 1000;
  resetCodes.set(email, { code, expiry });
  return code;
}

const server = http.createServer(async (req, res) => {
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET,POST,PUT,PATCH,DELETE,OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type, Authorization');
  if (req.method === 'OPTIONS') { res.writeHead(204); res.end(); return; }
  const u = parseUrl(req.url);
  const path = u.pathname;
  if (req.method === 'POST' && path === '/user/register') {
    const body = await parseBody(req);
    console.log('REGISTER body:', body);
    const { firstname, lastname, email, phone, password } = body;
    if (!firstname || !lastname || !email || !phone || !password) return json(res, 200, false);
    try {
      const [rows] = await pool.query('SELECT id FROM users WHERE email = ? LIMIT 1', [email]);
      if (Array.isArray(rows) && rows.length > 0) return json(res, 200, false);
      const pw = hashPassword(String(password));
      await pool.query(
        'INSERT INTO users (firstname, lastname, email, tel, localisation_id, captors_id, resetToken, resetTokenExpiry, password_hash) VALUES (?, ?, ?, ?, NULL, NULL, NULL, NULL, ?)',
        [firstname, lastname, email, String(phone), pw]
      );
      return json(res, 200, true);
    } catch {
      return json(res, 200, false);
    }
  }
  if (req.method === 'POST' && path === '/user/login') {
    const body = await parseBody(req);
    const email = body.email || u.searchParams.get('email');
    const password = body.password || u.searchParams.get('password');
    let user = null;
    try {
      const [rows] = await pool.query('SELECT * FROM users WHERE email = ? LIMIT 1', [String(email || '')]);
      user = Array.isArray(rows) && rows.length > 0 ? rows[0] : null;
    } catch {}
    if (!user) return json(res, 401, null);
    if (user.password_hash !== hashPassword(String(password || ''))) return json(res, 401, null);
    const token = signJwt({ sub: user.id, email: user.email, iat: Math.floor(Date.now() / 1000) });
    const userPayload = { id: user.id, firstname: user.firstname, lastname: user.lastname, email: user.email, tel: user.tel, localisation_id: user.localisation_id, captors_id: user.captors_id, resetToken: user.resetToken, resetTokenExpiry: user.resetTokenExpiry };
    lastUserInfo = { user: userPayload, token };
    return json(res, 200, lastUserInfo);
  }
  if (req.method === 'POST' && path === '/auth/login') {
    const body = await parseBody(req);
    const email = body.email || u.searchParams.get('email');
    const password = body.password || u.searchParams.get('password');
    let user = null;
    try {
      const [rows] = await pool.query('SELECT * FROM users WHERE email = ? LIMIT 1', [String(email || '')]);
      user = Array.isArray(rows) && rows.length > 0 ? rows[0] : null;
    } catch {}
    if (!user) return json(res, 401, null);
    if (user.password_hash !== hashPassword(String(password || ''))) return json(res, 401, null);
    const token = signJwt({ sub: user.id, email: user.email, iat: Math.floor(Date.now() / 1000) });
    const userPayload = { id: user.id, firstname: user.firstname, lastname: user.lastname, email: user.email, tel: user.tel, localisation_id: user.localisation_id, captors_id: user.captors_id, resetToken: user.resetToken, resetTokenExpiry: user.resetTokenExpiry };
    lastUserInfo = { user: userPayload, token };
    return json(res, 200, lastUserInfo);
  }
  if (req.method === 'POST' && path === '/user/logout') {
    const auth = req.headers['authorization'] || '';
    const m = auth.match(/^Bearer\s+(.+)$/i);
    if (!m) return json(res, 400, false);
    const token = m[1];
    if (!verifyJwt(token)) return json(res, 400, false);
    blacklisted.add(token);
    return json(res, 200, true);
  }
  if (req.method === 'POST' && path === '/auth/logout') {
    const auth = req.headers['authorization'] || '';
    const m = auth.match(/^Bearer\s+(.+)$/i);
    if (!m) return json(res, 400, false);
    const token = m[1];
    if (!verifyJwt(token)) return json(res, 400, false);
    blacklisted.add(token);
    return json(res, 200, true);
  }
  if (req.method === 'POST' && path === '/user/send-code') {
    const body = await parseBody(req);
    const email = body.email || u.searchParams.get('email');
    let exists = false;
    try {
      const [rows] = await pool.query('SELECT id FROM users WHERE email = ? LIMIT 1', [String(email || '')]);
      exists = Array.isArray(rows) && rows.length > 0;
    } catch {}
    if (!exists) return text(res, 400, 'User with this email not found.');
    const code = sendCode(String(email || ''));
    let previewUrl = null;
    let sent = false;
    try {
      await pool.query('INSERT INTO reset_codes (email, code, expiry) VALUES (?, ?, ?)', [String(email || ''), code, Date.now() + 15 * 60 * 1000]);
      const r = await trySendEmail(String(email || ''), code);
      sent = r.sent;
      previewUrl = r.previewUrl || null;
    } catch {}
    return json(res, 200, { success: true, code, sent, emailPreviewUrl: previewUrl, message: 'Code sent' });
  }
  if (req.method === 'GET' && path.startsWith('/user/verification-code/')) {
    const email = decodeURIComponent(path.substring('/user/verification-code/'.length));
    const code = u.searchParams.get('code') || '';
    let ok = false;
    try {
      const [rows] = await pool.query('SELECT id, expiry FROM reset_codes WHERE email = ? AND code = ? ORDER BY id DESC LIMIT 1', [String(email || ''), String(code || '')]);
      if (Array.isArray(rows) && rows.length > 0) {
        const r = rows[0];
        ok = r.expiry > Date.now();
      }
    } catch {}
    return json(res, 200, ok);
  }
  if (req.method === 'POST' && path === '/user/reset-password') {
    const body = await parseBody(req);
    const code = body.code || u.searchParams.get('code');
    const newPw = body.password || u.searchParams.get('password');
    if (!newPw) return json(res, 200, false);
    let emailFound = null;
    try {
      const [rows] = await pool.query('SELECT email, expiry FROM reset_codes WHERE code = ? ORDER BY id DESC LIMIT 1', [String(code || '')]);
      if (Array.isArray(rows) && rows.length > 0 && rows[0].expiry > Date.now()) emailFound = rows[0].email;
    } catch {}
    if (!emailFound) return json(res, 200, false);
    try {
      await pool.query('UPDATE users SET password_hash = ? WHERE email = ?', [hashPassword(String(newPw)), String(emailFound)]);
      await pool.query('DELETE FROM reset_codes WHERE email = ?', [String(emailFound)]);
      return json(res, 200, true);
    } catch {
      return json(res, 200, false);
    }
  }
  if (req.method === 'GET' && path === '/user/get-token-info') {
    return json(res, 200, lastUserInfo);
  }
  if (req.method === 'GET' && path === '/user/debug-users') {
    try {
      const [rows] = await pool.query('SELECT id, firstname, lastname, email, tel FROM users ORDER BY id ASC');
      return json(res, 200, rows);
    } catch {
      return json(res, 200, []);
    }
  }

  if (req.method === 'POST' && path === '/detect') {
    const chunks = [];
    req.on('data', (c) => chunks.push(c));
    req.on('end', () => {
      const body = Buffer.concat(chunks);
      const opts = {
        hostname: '127.0.0.1',
        port: 8000,
        path: '/detect',
        method: 'POST',
        headers: {
          'Content-Type': req.headers['content-type'] || 'application/octet-stream',
          'Content-Length': Buffer.byteLength(body),
        },
      };
      const fwd = http.request(opts, (r) => {
        let data = '';
        r.on('data', (d) => data += d);
        r.on('end', () => {
          try { json(res, r.statusCode || 500, JSON.parse(data || '{}')); }
          catch { json(res, r.statusCode || 500, { error: data || 'Bad response' }); }
        });
      });
      fwd.on('error', (e) => json(res, 502, { error: String(e) }));
      fwd.write(body);
      fwd.end();
    });
    return;
  }

  if (req.method === 'POST' && path === '/recommend') {
    const body = await parseBody(req);
    const payload = JSON.stringify(body || {});
    const opts = {
      hostname: '127.0.0.1',
      port: 8000,
      path: '/recommend',
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Content-Length': Buffer.byteLength(payload),
      },
    };
    await new Promise((resolve) => {
      const fwd = http.request(opts, (r) => {
        let data = '';
        r.on('data', (d) => data += d);
        r.on('end', () => {
          try { json(res, r.statusCode || 500, JSON.parse(data || '{}')); }
          catch { json(res, r.statusCode || 500, { error: data || 'Bad response' }); }
          resolve();
        });
      });
      fwd.on('error', (e) => { json(res, 502, { error: String(e) }); resolve(); });
      fwd.write(payload);
      fwd.end();
    });
    return;
  }

  res.writeHead(404, { 'Content-Type': 'application/json' });
  res.end(JSON.stringify({ error: 'Not found' }));
});

server.on('error', (err) => {
  if (err && err.code === 'EADDRINUSE') {
    console.error(`Port ${PORT} in use. Set PORT to a free port, e.g.: PORT=8083 node server.js`);
  } else {
    console.error(err);
  }
});
Promise.all([initDb(), initMailer()]).then(() => server.listen(PORT)).catch(() => server.listen(PORT));
