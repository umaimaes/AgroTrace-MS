class Parcel {
  final String id;
  final String name;
  final String area;
  final String crop;
  final int health;
  final int irrigation;
  final List<String> tags;

  Parcel({
    required this.id,
    required this.name,
    required this.area,
    required this.crop,
    required this.health,
    required this.irrigation,
    required this.tags,
  });

  factory Parcel.fromJson(Map<String, dynamic> json) {
    return Parcel(
      id: json['id']?.toString() ?? '',
      name: json['name'] ?? '',
      area: json['area'] ?? '',
      crop: json['crop'] ?? '',
      health: (json['health'] is num) ? (json['health'] as num).toInt() : 0,
      irrigation: (json['irrigation'] is num) ? (json['irrigation'] as num).toInt() : 0,
      tags: (json['tags'] as List?)?.map((e) => e.toString()).toList() ?? <String>[],
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'name': name,
      'area': area,
      'crop': crop,
      'health': health,
      'irrigation': irrigation,
      'tags': tags,
    };
  }
}

