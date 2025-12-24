class FarmParcel {
  final int id;
  final int farmId;
  final String name;
  final double areaHa;
  final String cropType;
  final String geometry;
  final DateTime? createdAt;
  final DateTime? updatedAt;

  FarmParcel({
    required this.id,
    required this.farmId,
    required this.name,
    required this.areaHa,
    required this.cropType,
    required this.geometry,
    this.createdAt,
    this.updatedAt,
  });

  factory FarmParcel.fromJson(Map<String, dynamic> json) {
    final farm = json['farm'];
    final farmIdValue = farm is Map<String, dynamic>
        ? farm['id']
        : json['farmId'];
    return FarmParcel(
      id: (json['id'] as num).toInt(),
      farmId: farmIdValue is num ? farmIdValue.toInt() : int.parse(farmIdValue.toString()),
      name: json['name']?.toString() ?? '',
      areaHa: (json['areaHa'] as num).toDouble(),
      cropType: json['cropType']?.toString() ?? '',
      geometry: json['geometry']?.toString() ?? '',
      createdAt: json['createdAt'] != null ? DateTime.parse(json['createdAt'].toString()) : null,
      updatedAt: json['updatedAt'] != null ? DateTime.parse(json['updatedAt'].toString()) : null,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'farmId': farmId,
      'name': name,
      'areaHa': areaHa,
      'cropType': cropType,
      'geometry': geometry,
      'createdAt': createdAt?.toIso8601String(),
      'updatedAt': updatedAt?.toIso8601String(),
    };
  }
}

