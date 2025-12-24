import 'package:flutter/material.dart';

class RoleSelection extends StatelessWidget {
  const RoleSelection({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Select Role'), backgroundColor: Colors.white, foregroundColor: Colors.black),
      backgroundColor: Colors.grey[100],
      body: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
          const Text('Choose your role to continue', style: TextStyle(fontWeight: FontWeight.bold)),
          const SizedBox(height: 12),
          Card(
            child: ListTile(
              leading: const Icon(Icons.agriculture, color: Color(0xFF00C853)),
              title: const Text('Farmer'),
              subtitle: const Text('Manage fields, sensors, and alerts'),
              onTap: () => Navigator.pushReplacementNamed(context, '/farmer'),
            ),
          ),
          const SizedBox(height: 8),
          Card(
            child: ListTile(
              leading: const Icon(Icons.live_tv, color: Color(0xFF00C853)),
              title: const Text('Drone Operator'),
              subtitle: const Text('Operate drones and review missions'),
              onTap: () => Navigator.pushReplacementNamed(context, '/drone'),
            ),
          ),
          const SizedBox(height: 8),
          Card(
            child: ListTile(
              leading: const Icon(Icons.admin_panel_settings, color: Color(0xFF00C853)),
              title: const Text('Admin'),
              subtitle: const Text('Manage users, farms, and devices'),
              onTap: () => Navigator.pushReplacementNamed(context, '/admin'),
            ),
          ),
        ]),
      ),
    );
  }
}
