import 'package:flutter/material.dart';

class FarmerProfileSettings extends StatelessWidget {
  final bool isDark;
  final ValueChanged<bool> onToggleTheme;
  const FarmerProfileSettings({super.key, required this.isDark, required this.onToggleTheme});
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Profile & Settings'), backgroundColor: Colors.white, foregroundColor: Colors.black),
      backgroundColor: const Color(0xFFF5F6F8),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          Center(
            child: Stack(
              children: [
                const CircleAvatar(radius: 48, backgroundColor: Colors.redAccent, child: Icon(Icons.person, color: Colors.white, size: 48)),
                Positioned(
                  right: 4,
                  bottom: 4,
                  child: Container(
                    decoration: BoxDecoration(color: const Color(0xFF00C853), borderRadius: BorderRadius.circular(14), border: Border.all(color: Colors.white, width: 2)),
                    width: 24,
                    height: 24,
                    child: const Icon(Icons.check, color: Colors.white, size: 16),
                  ),
                ),
              ],
            ),
          ),
          const SizedBox(height: 12),
          const Center(child: Text('John Appleseed', style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold))),
          const Center(child: Text('john.appleseed@farm.com', style: TextStyle(color: Colors.grey))),
          const SizedBox(height: 16),

          _sectionHeader('Personal Information'),
          Card(color: Colors.white, child: Column(children: [
            ListTile(leading: const Icon(Icons.person, color: Colors.green), title: const Text('Full Name'), trailing: const Icon(Icons.chevron_right)),
            const Divider(height: 1),
            ListTile(leading: const Icon(Icons.phone, color: Colors.green), title: const Text('Phone Number'), trailing: const Icon(Icons.chevron_right)),
            const Divider(height: 1),
            ListTile(leading: const Icon(Icons.lock, color: Colors.green), title: const Text('Change Password'), trailing: const Icon(Icons.chevron_right)),
          ])),

          const SizedBox(height: 12),
          _sectionHeader('Farm Details'),
          Card(color: Colors.white, child: Column(children: [
            ListTile(leading: const Icon(Icons.agriculture, color: Colors.green), title: const Text('Farm Name'), trailing: const Icon(Icons.chevron_right)),
            const Divider(height: 1),
            ListTile(leading: const Icon(Icons.place, color: Colors.green), title: const Text('Location / Address'), trailing: const Icon(Icons.chevron_right)),
            const Divider(height: 1),
            ListTile(leading: const Icon(Icons.area_chart, color: Colors.green), title: const Text('Farm Size'), trailing: const Text('120 Acres')),
          ])),

          const SizedBox(height: 12),
          _sectionHeader('Application Settings'),
          Card(color: Colors.white, child: Column(children: [
            SwitchListTile(value: true, onChanged: (_) {}, title: const Text('Notifications'), secondary: const Icon(Icons.notifications, color: Colors.green)),
            const Divider(height: 1),
            SwitchListTile(value: isDark, onChanged: onToggleTheme, title: const Text('Dark Mode'), secondary: const Icon(Icons.dark_mode, color: Colors.green)),
            const Divider(height: 1),
            ListTile(leading: const Icon(Icons.straighten, color: Colors.green), title: const Text('Units'), trailing: const Text('Imperial')),
            const Divider(height: 1),
            ListTile(leading: const Icon(Icons.language, color: Colors.green), title: const Text('Language'), trailing: const Text('English')),
          ])),

          const SizedBox(height: 12),
          _sectionHeader('Support & Legal'),
          Card(color: Colors.white, child: Column(children: [
            ListTile(leading: const Icon(Icons.help_outline, color: Colors.green), title: const Text('Help Center'), trailing: const Icon(Icons.chevron_right)),
            const Divider(height: 1),
            ListTile(leading: const Icon(Icons.privacy_tip_outlined, color: Colors.green), title: const Text('Privacy Policy'), trailing: const Icon(Icons.chevron_right)),
            const Divider(height: 1),
            ListTile(leading: const Icon(Icons.article_outlined, color: Colors.green), title: const Text('Terms of Service'), trailing: const Icon(Icons.chevron_right)),
          ])),

          const SizedBox(height: 16),
          SizedBox(
            width: double.infinity,
            height: 50,
            child: ElevatedButton(
              onPressed: () {
                Navigator.of(context).pushNamedAndRemoveUntil('/login', (route) => false);
              },
              style: ElevatedButton.styleFrom(backgroundColor: const Color(0x33FF5A5A), foregroundColor: Colors.red, shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12))),
              child: const Row(mainAxisAlignment: MainAxisAlignment.center, children: [Icon(Icons.logout), SizedBox(width: 8), Text('Log Out')]),
            ),
          ),
        ],
      ),
    );
  }

  Widget _sectionHeader(String title) {
    return Padding(
      padding: const EdgeInsets.only(left: 4, bottom: 6, top: 6),
      child: Text(title, style: const TextStyle(color: Colors.grey, fontWeight: FontWeight.bold)),
    );
  }
}
