import 'package:flutter/material.dart';
import '../core/api_client.dart';

class SignUp extends StatefulWidget {
  const SignUp({super.key});
  @override
  State<SignUp> createState() => _SignUpState();
}

class _SignUpState extends State<SignUp> {
  final firstCtrl = TextEditingController();
  final lastCtrl = TextEditingController();
  final emailCtrl = TextEditingController();
  final phoneCtrl = TextEditingController();
  final pwCtrl = TextEditingController();
  final confirmCtrl = TextEditingController();
  bool obscure1 = true;
  bool obscure2 = true;
  bool loading = false;
  String? msg;

  Future<void> _register() async {
    final first = firstCtrl.text.trim();
    final last = lastCtrl.text.trim();
    final email = emailCtrl.text.trim();
    final phone = phoneCtrl.text.trim();
    final pw = pwCtrl.text;
    final cpw = confirmCtrl.text;
    if (first.isEmpty || last.isEmpty || email.isEmpty || phone.isEmpty || pw.isEmpty || cpw.isEmpty) { setState(() { msg = 'Fill all fields'; }); return; }
    if (pw != cpw) { setState(() { msg = 'Passwords do not match'; }); return; }
    setState(() { loading = true; msg = null; });
    try {
      final res = await ApiClient.instance.post('/user/register', data: {
        'firstname': first,
        'lastname': last,
        'email': email,
        'phone': phone,
        'password': pw,
      });
      final map = res.data is Map ? res.data as Map : null;
      final msgStr = map == null ? '' : (map['message']?.toString().toLowerCase() ?? '');
      final ok = res.data == true || (map != null && (map['success'] == true || msgStr.contains('created') || msgStr.contains('success')));
      if (ok) {
        if (!mounted) return;
        Navigator.pop(context);
        ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('Account created')));
      } else {
        final err = map == null ? null : (map['error']?.toString());
        setState(() { msg = err ?? 'Registration failed'; });
      }
    } catch (_) {
      setState(() { msg = 'Registration failed'; });
    } finally {
      setState(() { loading = false; });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Sign Up'), backgroundColor: Colors.white, foregroundColor: Colors.black),
      backgroundColor: const Color(0xFF0D2818),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16),
        child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
          const Text('Create your account', style: TextStyle(color: Colors.white, fontSize: 20, fontWeight: FontWeight.bold)),
          const SizedBox(height: 16),
          const Text('First Name', style: TextStyle(color: Colors.white)),
          const SizedBox(height: 8),
          TextField(
            controller: firstCtrl,
            style: const TextStyle(color: Colors.white),
            decoration: InputDecoration(hintText: 'Enter your first name', hintStyle: TextStyle(color: Colors.grey[600]), filled: true, fillColor: const Color(0xFF1a3a2a), prefixIcon: const Icon(Icons.person_outline, color: Colors.grey), border: OutlineInputBorder(borderRadius: BorderRadius.circular(12), borderSide: BorderSide.none)),
          ),
          const SizedBox(height: 12),
          const Text('Last Name', style: TextStyle(color: Colors.white)),
          const SizedBox(height: 8),
          TextField(
            controller: lastCtrl,
            style: const TextStyle(color: Colors.white),
            decoration: InputDecoration(hintText: 'Enter your last name', hintStyle: TextStyle(color: Colors.grey[600]), filled: true, fillColor: const Color(0xFF1a3a2a), prefixIcon: const Icon(Icons.person, color: Colors.grey), border: OutlineInputBorder(borderRadius: BorderRadius.circular(12), borderSide: BorderSide.none)),
          ),
          const SizedBox(height: 12),
          const Text('Email', style: TextStyle(color: Colors.white)),
          const SizedBox(height: 8),
          TextField(
            controller: emailCtrl,
            style: const TextStyle(color: Colors.white),
            decoration: InputDecoration(hintText: 'Enter your email', hintStyle: TextStyle(color: Colors.grey[600]), filled: true, fillColor: const Color(0xFF1a3a2a), prefixIcon: const Icon(Icons.email, color: Colors.grey), border: OutlineInputBorder(borderRadius: BorderRadius.circular(12), borderSide: BorderSide.none)),
          ),
          const SizedBox(height: 12),
          const Text('Phone', style: TextStyle(color: Colors.white)),
          const SizedBox(height: 8),
          TextField(
            controller: phoneCtrl,
            keyboardType: TextInputType.phone,
            style: const TextStyle(color: Colors.white),
            decoration: InputDecoration(hintText: 'Enter your phone number', hintStyle: TextStyle(color: Colors.grey[600]), filled: true, fillColor: const Color(0xFF1a3a2a), prefixIcon: const Icon(Icons.phone, color: Colors.grey), border: OutlineInputBorder(borderRadius: BorderRadius.circular(12), borderSide: BorderSide.none)),
          ),
          const SizedBox(height: 12),
          const Text('Password', style: TextStyle(color: Colors.white)),
          const SizedBox(height: 8),
          TextField(
            controller: pwCtrl,
            obscureText: obscure1,
            style: const TextStyle(color: Colors.white),
            decoration: InputDecoration(hintText: 'Create a password', hintStyle: TextStyle(color: Colors.grey[600]), filled: true, fillColor: const Color(0xFF1a3a2a), prefixIcon: const Icon(Icons.lock, color: Colors.grey), suffixIcon: IconButton(icon: Icon(obscure1 ? Icons.visibility_off : Icons.visibility, color: Colors.grey), onPressed: () => setState(() => obscure1 = !obscure1)), border: OutlineInputBorder(borderRadius: BorderRadius.circular(12), borderSide: BorderSide.none)),
          ),
          const SizedBox(height: 12),
          const Text('Confirm Password', style: TextStyle(color: Colors.white)),
          const SizedBox(height: 8),
          TextField(
            controller: confirmCtrl,
            obscureText: obscure2,
            style: const TextStyle(color: Colors.white),
            decoration: InputDecoration(hintText: 'Re-enter your password', hintStyle: TextStyle(color: Colors.grey[600]), filled: true, fillColor: const Color(0xFF1a3a2a), prefixIcon: const Icon(Icons.lock, color: Colors.grey), suffixIcon: IconButton(icon: Icon(obscure2 ? Icons.visibility_off : Icons.visibility, color: Colors.grey), onPressed: () => setState(() => obscure2 = !obscure2)), border: OutlineInputBorder(borderRadius: BorderRadius.circular(12), borderSide: BorderSide.none)),
          ),
          const SizedBox(height: 16),
          SizedBox(
            width: double.infinity,
            height: 52,
            child: ElevatedButton(onPressed: loading ? null : _register, style: ElevatedButton.styleFrom(backgroundColor: const Color(0xFF00C853), shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12))), child: loading ? const CircularProgressIndicator(color: Colors.black) : const Text('Create Account', style: TextStyle(color: Colors.black, fontWeight: FontWeight.bold))),
          ),
          const SizedBox(height: 12),
          if (msg != null) Text(msg!, style: const TextStyle(color: Colors.redAccent)),
          const SizedBox(height: 12),
          Center(child: TextButton(onPressed: () => Navigator.pop(context), child: const Text('Back to Login', style: TextStyle(color: Color(0xFF00C853))))),
        ]),
      ),
    );
  }
}
