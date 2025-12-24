import 'package:flutter/material.dart';
import '../core/api_client.dart';
import '../core/config.dart';
class Login extends StatefulWidget {
  const Login({super.key});

  @override
  State<Login> createState() => _LoginState();
}

class _LoginState extends State<Login> {
  final _emailController = TextEditingController();
  final _passwordController = TextEditingController();
  bool _obscurePassword = true;
  bool _loading = false;
  String? _error;

  Future<void> _login() async {
    final email = _emailController.text.trim();
    final password = _passwordController.text;
    if (email.isEmpty || password.isEmpty) {
      setState(() { _error = 'Enter email and password'; });
      return;
    }
    setState(() { _loading = true; _error = null; });
    try {
      final base = BackendConfig.authBaseUrl.value;
      final res = await ApiClient.instance.post('$base/auth/login', data: {'email': email, 'password': password});
      final data = res.data is Map ? res.data as Map<String, dynamic> : <String, dynamic>{};
      final token = data['token']?.toString();
      if (token == null || token.isEmpty) {
        setState(() { _error = 'Invalid credentials'; });
      } else {
        BackendConfig.authToken.value = token;
        if (!mounted) return;
        Navigator.pushReplacementNamed(context, '/role');
      }
    } catch (_) {
      setState(() { _error = 'Login failed'; });
    } finally {
      if (mounted) setState(() { _loading = false; });
    }
  }

  void _openForgotPassword() {
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      backgroundColor: Colors.white,
      builder: (_) => Padding(
        padding: EdgeInsets.only(bottom: MediaQuery.of(context).viewInsets.bottom),
        child: const _ForgotPasswordSheet(),
      ),
    );
  }

  void _openSignUp() {
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      backgroundColor: Colors.white,
      builder: (_) => Padding(
        padding: EdgeInsets.only(bottom: MediaQuery.of(context).viewInsets.bottom),
        child: const _SignUpSheet(),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFF0D2818),
      body: SafeArea(
        child: Center(
          child: SingleChildScrollView(
            padding: const EdgeInsets.all(24.0),
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                Container(
                  width: 80,
                  height: 80,
                  decoration: BoxDecoration(
                    color: const Color(0xFF00C853),
                    shape: BoxShape.circle,
                  ),
                  child: const Icon(Icons.eco, size: 40, color: Colors.white),
                ),
                const SizedBox(height: 32),
                const Text(
                  'Welcome Back',
                  style: TextStyle(
                    fontSize: 28,
                    fontWeight: FontWeight.bold,
                    color: Colors.white,
                  ),
                ),
                const SizedBox(height: 8),
                const Text(
                  'Log in to manage your farm',
                  style: TextStyle(color: Colors.grey, fontSize: 16),
                ),
                const SizedBox(height: 40),
                Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    const Text(
                      'Email',
                      style: TextStyle(color: Colors.white, fontSize: 16),
                    ),
                    const SizedBox(height: 8),
                    TextField(
                      controller: _emailController,
                      style: const TextStyle(color: Colors.white),
                      decoration: InputDecoration(
                        hintText: 'Enter your email',
                        hintStyle: TextStyle(color: Colors.grey[600]),
                        filled: true,
                        fillColor: const Color(0xFF1a3a2a),
                        prefixIcon: const Icon(Icons.person, color: Colors.grey),
                        border: OutlineInputBorder(
                          borderRadius: BorderRadius.circular(12),
                          borderSide: BorderSide.none,
                        ),
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 20),
                Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    const Text(
                      'Password',
                      style: TextStyle(color: Colors.white, fontSize: 16),
                    ),
                    const SizedBox(height: 8),
                    TextField(
                      controller: _passwordController,
                      obscureText: _obscurePassword,
                      style: const TextStyle(color: Colors.white),
                      decoration: InputDecoration(
                        hintText: 'Enter your password',
                        hintStyle: TextStyle(color: Colors.grey[600]),
                        filled: true,
                        fillColor: const Color(0xFF1a3a2a),
                        prefixIcon: const Icon(Icons.lock, color: Colors.grey),
                        suffixIcon: IconButton(
                          icon: Icon(
                            _obscurePassword ? Icons.visibility_off : Icons.visibility,
                            color: Colors.grey,
                          ),
                          onPressed: () {
                            setState(() {
                              _obscurePassword = !_obscurePassword;
                            });
                          },
                        ),
                        border: OutlineInputBorder(
                          borderRadius: BorderRadius.circular(12),
                          borderSide: BorderSide.none,
                        ),
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 12),
                Align(
                  alignment: Alignment.centerRight,
                  child: TextButton(
                    onPressed: () => Navigator.pushNamed(context, '/forgot'),
                    child: const Text(
                      'Forgot Password?',
                      style: TextStyle(color: Color(0xFF00C853)),
                    ),
                  ),
                ),
                const SizedBox(height: 24),
                SizedBox(
                  width: double.infinity,
                  height: 56,
                  child: ElevatedButton(
                    onPressed: _loading ? null : _login,
                    style: ElevatedButton.styleFrom(
                      backgroundColor: const Color(0xFF00C853),
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(12),
                      ),
                    ),
                    child: _loading ? const CircularProgressIndicator(color: Colors.black) : const Text('Login', style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold, color: Colors.black)),
                  ),
                ),
                if (_error != null) ...[
                  const SizedBox(height: 12),
                  Text(_error!, style: const TextStyle(color: Colors.redAccent)),
                ],
                const SizedBox(height: 16),
                Row(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    const Text(
                      "Don't have an account? ",
                      style: TextStyle(color: Colors.grey),
                    ),
                    TextButton(
                      onPressed: () => Navigator.pushNamed(context, '/signup'),
                      child: const Text(
                        'Sign Up',
                        style: TextStyle(color: Color(0xFF00C853)),
                      ),
                    ),
                  ],
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}

class _ForgotPasswordSheet extends StatefulWidget {
  const _ForgotPasswordSheet({super.key});
  @override
  State<_ForgotPasswordSheet> createState() => _ForgotPasswordSheetState();
}

class _ForgotPasswordSheetState extends State<_ForgotPasswordSheet> {
  int step = 0;
  final emailCtrl = TextEditingController();
  final codeCtrl = TextEditingController();
  final newPwCtrl = TextEditingController();
  bool loading = false;
  String? msg;

  Future<void> _sendCode() async {
    final email = emailCtrl.text.trim();
    if (email.isEmpty) { setState(() { msg = 'Enter email'; }); return; }
    setState(() { loading = true; msg = null; });
    try {
      final base = BackendConfig.authBaseUrl.value;
      final res = await ApiClient.instance.post('$base/user/send-code', data: {'email': email});
      final code = res.data?.toString() ?? '';
      setState(() { step = 1; msg = code.isNotEmpty ? 'Code: $code' : 'Code sent'; });
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
      final base = BackendConfig.authBaseUrl.value;
      final res = await ApiClient.instance.post('$base/user/reset-password', data: {'code': code, 'password': pw});
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
    return Padding(
      padding: const EdgeInsets.all(16),
      child: Column(mainAxisSize: MainAxisSize.min, crossAxisAlignment: CrossAxisAlignment.start, children: [
        const Text('Forgot Password', style: TextStyle(fontWeight: FontWeight.bold)),
        const SizedBox(height: 12),
        if (step == 0) ...[
          TextField(controller: emailCtrl, decoration: InputDecoration(hintText: 'Email', border: OutlineInputBorder(borderRadius: BorderRadius.circular(12)))) ,
          const SizedBox(height: 12),
          SizedBox(width: double.infinity, height: 44, child: ElevatedButton(onPressed: loading ? null : _sendCode, child: loading ? const CircularProgressIndicator() : const Text('Send Code'))),
        ] else ...[
          TextField(controller: codeCtrl, decoration: InputDecoration(hintText: 'Verification Code', border: OutlineInputBorder(borderRadius: BorderRadius.circular(12)))) ,
          const SizedBox(height: 8),
          TextField(controller: newPwCtrl, obscureText: true, decoration: InputDecoration(hintText: 'New Password', border: OutlineInputBorder(borderRadius: BorderRadius.circular(12)))) ,
          const SizedBox(height: 12),
          SizedBox(width: double.infinity, height: 44, child: ElevatedButton(onPressed: loading ? null : _reset, child: loading ? const CircularProgressIndicator() : const Text('Reset Password'))),
        ],
        if (msg != null) ...[const SizedBox(height: 8), Text(msg!, style: const TextStyle(color: Colors.red))],
        const SizedBox(height: 12),
      ]),
    );
  }
}

class _SignUpSheet extends StatefulWidget {
  const _SignUpSheet({super.key});
  @override
  State<_SignUpSheet> createState() => _SignUpSheetState();
}

class _SignUpSheetState extends State<_SignUpSheet> {
  final firstCtrl = TextEditingController();
  final lastCtrl = TextEditingController();
  final emailCtrl = TextEditingController();
  final phoneCtrl = TextEditingController();
  final pwCtrl = TextEditingController();
  bool loading = false;
  String? msg;

  Future<void> _register() async {
    final body = {
      'firstname': firstCtrl.text.trim(),
      'lastname': lastCtrl.text.trim(),
      'email': emailCtrl.text.trim(),
      'phone': phoneCtrl.text.trim(),
      'password': pwCtrl.text,
    };
    if ((body['firstname'] as String).isEmpty || (body['lastname'] as String).isEmpty || (body['email'] as String).isEmpty || (body['phone'] as String).isEmpty || (body['password'] as String).isEmpty) {
      setState(() { msg = 'Fill all fields'; });
      return;
    }
    setState(() { loading = true; msg = null; });
    try {
      final base = BackendConfig.authBaseUrl.value;
      final res = await ApiClient.instance.post('$base/user/register', data: body);
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
    return Padding(
      padding: const EdgeInsets.all(16),
      child: SingleChildScrollView(child: Column(crossAxisAlignment: CrossAxisAlignment.start, mainAxisSize: MainAxisSize.min, children: [
        const Text('Sign Up', style: TextStyle(fontWeight: FontWeight.bold)),
        const SizedBox(height: 12),
        TextField(controller: firstCtrl, decoration: InputDecoration(hintText: 'First name', border: OutlineInputBorder(borderRadius: BorderRadius.circular(12)))) ,
        const SizedBox(height: 8),
        TextField(controller: lastCtrl, decoration: InputDecoration(hintText: 'Last name', border: OutlineInputBorder(borderRadius: BorderRadius.circular(12)))) ,
        const SizedBox(height: 8),
        TextField(controller: emailCtrl, decoration: InputDecoration(hintText: 'Email', border: OutlineInputBorder(borderRadius: BorderRadius.circular(12)))) ,
        const SizedBox(height: 8),
        TextField(controller: phoneCtrl, decoration: InputDecoration(hintText: 'Phone', border: OutlineInputBorder(borderRadius: BorderRadius.circular(12)))) ,
        const SizedBox(height: 8),
        TextField(controller: pwCtrl, obscureText: true, decoration: InputDecoration(hintText: 'Password', border: OutlineInputBorder(borderRadius: BorderRadius.circular(12)))) ,
        const SizedBox(height: 12),
        SizedBox(width: double.infinity, height: 44, child: ElevatedButton(onPressed: loading ? null : _register, child: loading ? const CircularProgressIndicator() : const Text('Create Account'))),
        if (msg != null) ...[const SizedBox(height: 8), Text(msg!, style: const TextStyle(color: Colors.red))],
      ])),
    );
  }
}
