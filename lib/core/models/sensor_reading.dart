class SensorReading {
  final String id;
  final String type;
  final double value;
  final DateTime timestamp;

  SensorReading({
    required this.id,
    required this.type,
    required this.value,
    required this.timestamp,
  });

  factory SensorReading.fromJson(Map<String, dynamic> json) {
    return SensorReading(
      id: json['id']?.toString() ?? '',
      type: json['type'] ?? '',
      value: (json['value'] is num) ? (json['value'] as num).toDouble() : 0.0,
      timestamp: DateTime.tryParse(json['timestamp']?.toString() ?? '') ?? DateTime.fromMillisecondsSinceEpoch(0),
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'type': type,
      'value': value,
      'timestamp': timestamp.toIso8601String(),
    };
  }
}

