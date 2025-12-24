import '../api_client.dart';
import '../models/sensor_reading.dart';

class SensorsRepository {
  Future<List<SensorReading>> fetchReadings({String range = '24h'}) async {
    final res = await ApiClient.instance.get('/sensors/readings', query: {'range': range});
    final list = res.data is List ? res.data as List : <dynamic>[];
    return list.map((e) => SensorReading.fromJson(e as Map<String, dynamic>)).toList();
  }
}

