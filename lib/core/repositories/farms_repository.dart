import '../api_client.dart';
import '../models/farm.dart';
import '../models/farm_parcel.dart';

class FarmsRepository {
  Future<Farm> createFarm({
    required int ownerId,
    required String name,
    required String location,
  }) async {
    final res = await ApiClient.instance.post(
      '/api/farms',
      data: {
        'ownerId': ownerId,
        'name': name,
        'location': location,
      },
    );
    final data = res.data is Map ? res.data as Map<String, dynamic> : <String, dynamic>{};
    return Farm.fromJson(data);
  }

  Future<List<Farm>> fetchFarms() async {
    final res = await ApiClient.instance.get('/api/farms');
    final list = res.data is List ? res.data as List : <dynamic>[];
    return list.map((e) => Farm.fromJson(e as Map<String, dynamic>)).toList();
  }

  Future<Farm> fetchFarm(int id) async {
    final res = await ApiClient.instance.get('/api/farms/$id');
    final data = res.data is Map ? res.data as Map<String, dynamic> : <String, dynamic>{};
    return Farm.fromJson(data);
  }

  Future<Farm> updateFarm({
    required int id,
    required int ownerId,
    required String name,
    required String location,
  }) async {
    final res = await ApiClient.instance.put(
      '/api/farms/$id',
      data: {
        'ownerId': ownerId,
        'name': name,
        'location': location,
      },
    );
    final data = res.data is Map ? res.data as Map<String, dynamic> : <String, dynamic>{};
    return Farm.fromJson(data);
  }

  Future<void> deleteFarm(int id) async {
    await ApiClient.instance.delete('/api/farms/$id');
  }

  Future<FarmParcel> createParcel({
    required int farmId,
    required String name,
    required double areaHa,
    required String cropType,
    required String geometry,
  }) async {
    final res = await ApiClient.instance.post(
      '/api/parcels',
      query: {
        'farmId': farmId,
      },
      data: {
        'name': name,
        'areaHa': areaHa,
        'cropType': cropType,
        'geometry': geometry,
      },
    );
    final data = res.data is Map ? res.data as Map<String, dynamic> : <String, dynamic>{};
    return FarmParcel.fromJson(data);
  }

  Future<List<FarmParcel>> fetchParcels({int? farmId}) async {
    final res = await ApiClient.instance.get(
      '/api/parcels',
      query: farmId != null ? {'farmId': farmId} : null,
    );
    final list = res.data is List ? res.data as List : <dynamic>[];
    return list.map((e) => FarmParcel.fromJson(e as Map<String, dynamic>)).toList();
  }

  Future<FarmParcel> fetchParcel(int id) async {
    final res = await ApiClient.instance.get('/api/parcels/$id');
    final data = res.data is Map ? res.data as Map<String, dynamic> : <String, dynamic>{};
    return FarmParcel.fromJson(data);
  }

  Future<FarmParcel> updateParcel({
    required int id,
    required String name,
    required double areaHa,
    required String cropType,
    required String geometry,
  }) async {
    final res = await ApiClient.instance.put(
      '/api/parcels/$id',
      data: {
        'name': name,
        'areaHa': areaHa,
        'cropType': cropType,
        'geometry': geometry,
      },
    );
    final data = res.data is Map ? res.data as Map<String, dynamic> : <String, dynamic>{};
    return FarmParcel.fromJson(data);
  }

  Future<void> deleteParcel(int id) async {
    await ApiClient.instance.delete('/api/parcels/$id');
  }
}

