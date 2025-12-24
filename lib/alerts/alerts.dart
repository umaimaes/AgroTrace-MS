import 'package:flutter/material.dart';

class Alerts extends StatefulWidget {
  const Alerts({super.key});
  @override
  State<Alerts> createState() => _AlertsState();
}

class _AlertsState extends State<Alerts> {
  int tab = 0;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Alerts'), backgroundColor: Colors.white, foregroundColor: Colors.black),
      backgroundColor: Colors.grey[100],
      body: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
          Container(
            height: 44,
            decoration: BoxDecoration(color: Colors.white, borderRadius: BorderRadius.circular(12)),
            child: Row(children: [
              _alertTab('All', 0),
              _alertTab('Critical', 1),
              _alertTab('Info', 2),
            ]),
          ),
          const SizedBox(height: 12),
          Expanded(
            child: ListView(children: [
              _alertItem(
                color: Colors.red,
                title: 'High Disease Risk',
                subtitle: 'Field A-3 • 5m ago',
                action: 'View Details',
                onTap: () => Navigator.pushNamed(context, '/disease'),
              ),
              const SizedBox(height: 12),
              _alertItem(
                color: Colors.orange,
                title: 'Urgent Irrigation Needed',
                subtitle: 'Sector 7 • 42m ago',
                action: 'Create Plan',
                onTap: () => Navigator.pushNamed(context, '/irrigation'),
              ),
            ]),
          ),
        ]),
      ),
    );
  }

  Widget _alertTab(String label, int index) {
    final selected = tab == index;
    return Expanded(
      child: InkWell(
        onTap: () => setState(() => tab = index),
        child: Container(
          alignment: Alignment.center,
          decoration: BoxDecoration(
            color: selected ? const Color(0xFF00C853) : Colors.transparent,
            borderRadius: BorderRadius.circular(12),
          ),
          child: Text(label, style: TextStyle(color: selected ? Colors.black : Colors.grey)),
        ),
      ),
    );
  }

  Widget _alertItem({required Color color, required String title, required String subtitle, required String action, required VoidCallback onTap}) {
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(color: Colors.white, borderRadius: BorderRadius.circular(12)),
      child: Row(children: [
        Container(width: 6, height: 48, decoration: BoxDecoration(color: color, borderRadius: BorderRadius.circular(3))),
        const SizedBox(width: 12),
        Expanded(child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [Text(title, style: const TextStyle(fontWeight: FontWeight.bold)), const SizedBox(height: 4), Text(subtitle, style: const TextStyle(color: Colors.grey))])),
        const SizedBox(width: 12),
        TextButton(onPressed: onTap, child: Text(action)),
      ]),
    );
  }
}
