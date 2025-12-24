class AlertItem {
  final String id;
  final String title;
  final String subtitle;
  final String level;

  AlertItem({required this.id, required this.title, required this.subtitle, required this.level});

  factory AlertItem.fromJson(Map<String, dynamic> json) {
    return AlertItem(
      id: json['id']?.toString() ?? '',
      title: json['title'] ?? '',
      subtitle: json['subtitle'] ?? '',
      level: json['level'] ?? '',
    );
  }

  Map<String, dynamic> toJson() {
    return {'id': id, 'title': title, 'subtitle': subtitle, 'level': level};
  }
}

