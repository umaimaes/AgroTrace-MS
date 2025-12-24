import 'package:flutter/material.dart';
import '../core/api_client.dart';

class ForgotPassword extends StatefulWidget {
  const ForgotPassword({super.key});
  @override
  State<ForgotPassword> createState() => _ForgotPasswordState();
}

class _ForgotPasswordState extends State<ForgotPassword> {
  int step = 0;
  final emailCtrl = TextEditingController();
  final codeCtrl = TextEditingController();
  final newPwCtrl = TextEditingController();
  bool loading = false;
  String? msg;

  Future<void> _send() async {
    final email = emailCtrl.text.trim();
    if (email.isEmpty) { setState(() { msg = 'Enter email'; }); return; }
    setState(() { loading = true; msg = null; });
    try {
      final res = await ApiClient.instance.post('/user/send-code', data: {'email': email});
      String nextMsg = 'Code sent';
      if (res.data is Map) {
        final m = res.data as Map;
        final code = m['code']?.toString() ?? '';
        final url = m['emailPreviewUrl']?.toString();
        final sent = m['sent'] == true;
        if (url != null && url.isNotEmpty) {
          nextMsg = 'Open email preview: ' + url;
        } else if (!sent && code.isNotEmpty) {
          nextMsg = 'Code: ' + code;
        }
      } else {
        final code = res.data?.toString() ?? '';
        if (code.isNotEmpty) nextMsg = 'Code: ' + code;
      }
      setState(() { step = 1; msg = nextMsg; });
    } catch (_) {
      setState(() { msg = 'Failed to send code'; });
    } finally {
      setState(() { loading = false; });
    }
  }

  Future<void> _reset() async {
    final code = codeCtrl.text.trim();
    final pw = newPwCtrl.text;
    if (code.isEmpty || pw.isEmpty) { setState(() { msg = 'Enter code and new password'; }); return; }
    setState(() { loading = true; msg = null; });
    try {
      final res = await ApiClient.instance.post('/user/reset-password', data: {'code': code, 'password': pw});
      final ok = res.data == true;
      if (ok) {
        if (!mounted) return;
        Navigator.pop(context);
        ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('Password updated')));
      } else {
        setState(() { msg = 'Invalid code'; });
      }
    } catch (_) {
      setState(() { msg = 'Reset failed'; });
    } finally {
      setState(() { loading = false; });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Forgot Password'), backgroundColor: Colors.white, foregroundColor: Colors.black),
      backgroundColor: const Color(0xFF0D2818),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16),
        child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
          const Text('Reset your password', style: TextStyle(color: Colors.white, fontSize: 20, fontWeight: FontWeight.bold)),
          const SizedBox(height: 8),
          const Text('Enter the email associated with your account.', style: TextStyle(color: Colors.white70)),
          const SizedBox(height: 16),
          const Text('Email', style: TextStyle(color: Colors.white)),
          const SizedBox(height: 8),
          TextField(
            controller: emailCtrl,
            style: const TextStyle(color: Colors.white),
            decoration: InputDecoration(hintText: 'Enter your email', hintStyle: TextStyle(color: Colors.grey[600]), filled: true, fillColor: const Color(0xFF1a3a2a), prefixIcon: const Icon(Icons.email, color: Colors.grey), border: OutlineInputBorder(borderRadius: BorderRadius.circular(12), borderSide: BorderSide.none)),
          ),
          const SizedBox(height: 16),
          SizedBox(
            width: double.infinity,
            height: 52,
            child: ElevatedButton(onPressed: loading ? null : _send, style: ElevatedButton.styleFrom(backgroundColor: const Color(0xFF00C853), shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12))), child: loading ? const CircularProgressIndicator(color: Colors.black) : const Text('Send Code', style: TextStyle(color: Colors.black, fontWeight: FontWeight.bold))),
          ),
          const SizedBox(height: 16),
          if (step == 1) ...[
            const Text('Verification Code', style: TextStyle(color: Colors.white)),
            const SizedBox(height: 8),
            TextField(
              controller: codeCtrl,
              style: const TextStyle(color: Colors.white),
              decoration: InputDecoration(hintText: 'Enter code', hintStyle: TextStyle(color: Colors.grey[600]), filled: true, fillColor: const Color(0xFF1a3a2a), prefixIcon: const Icon(Icons.verified, color: Colors.grey), border: OutlineInputBorder(borderRadius: BorderRadius.circular(12), borderSide: BorderSide.none)),
            ),
            const SizedBox(height: 12),
            const Text('New Password', style: TextStyle(color: Colors.white)),
            const SizedBox(height: 8),
            TextField(
              controller: newPwCtrl,
              obscureText: true,
              style: const TextStyle(color: Colors.white),
              decoration: InputDecoration(hintText: 'Enter new password', hintStyle: TextStyle(color: Colors.grey[600]), filled: true, fillColor: const Color(0xFF1a3a2a), prefixIcon: const Icon(Icons.lock, color: Colors.grey), border: OutlineInputBorder(borderRadius: BorderRadius.circular(12), borderSide: BorderSide.none)),
            ),
            const SizedBox(height: 16),
            SizedBox(
              width: double.infinity,
              height: 52,
              child: ElevatedButton(onPressed: loading ? null : _reset, style: ElevatedButton.styleFrom(backgroundColor: const Color(0xFF00C853), shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12))), child: loading ? const CircularProgressIndicator(color: Colors.black) : const Text('Reset Password', style: TextStyle(color: Colors.black, fontWeight: FontWeight.bold))),
            ),
          ],
          const SizedBox(height: 12),
          if (msg != null) Text(msg!, style: const TextStyle(color: Colors.redAccent)),
          const SizedBox(height: 12),
          Center(child: TextButton(onPressed: () => Navigator.pop(context), child: const Text('Back to Login', style: TextStyle(color: Color(0xFF00C853))))),
        ]),
      ),
    );
  }
}
