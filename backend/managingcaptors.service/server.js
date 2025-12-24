const http = require('http');

const PORT = Number(process.env.PORT || 8082);

function getTokenInfo() {
  return new Promise((resolve) => {
    const req = http.request({ hostname: 'localhost', port: 8081, path: '/user/get-token-info', method: 'GET' }, (res) => {
      let data = '';
      res.on('data', (c) => data += c);
      res.on('end', () => {
        try { resolve(JSON.parse(data || 'null')); } catch { resolve(null); }
      });
    });
    req.on('error', () => resolve(null));
    req.end();
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

const server = http.createServer(async (req, res) => {
  const u = new URL(req.url, `http://localhost:${PORT}`);
  const path = u.pathname;
  if (req.method === 'GET' && path === '/token-test/from-users-manage') {
    const info = await getTokenInfo();
    const tok = info && info.token ? info.token : null;
    return text(res, 200, `Token from users_manage.server: ${tok}`);
  }
  if (req.method === 'GET' && path === '/token-test/verify') {
    const auth = req.headers['authorization'] || '';
    const m = auth.match(/^Bearer\s+(.+)$/i);
    const clientToken = m ? m[1] : null;
    const clientEmail = u.searchParams.get('email') || '';
    const info = await getTokenInfo();
    const serverToken = info && info.token ? info.token : null;
    const serverEmail = info && info.user && info.user.email ? info.user.email : null;
    const ok = !!clientToken && !!serverToken && clientToken === serverToken && !!clientEmail && clientEmail === serverEmail;
    return json(res, 200, ok);
  }
  res.writeHead(404, { 'Content-Type': 'application/json' });
  res.end(JSON.stringify({ error: 'Not found' }));
});

server.on('error', (err) => {
  if (err && err.code === 'EADDRINUSE') {
    console.error(`Port ${PORT} in use. Set PORT to a free port, e.g.: PORT=8084 node server.js`);
  } else {
    console.error(err);
  }
});
server.listen(PORT);
