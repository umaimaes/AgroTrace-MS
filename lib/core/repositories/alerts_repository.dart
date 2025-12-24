import '../api_client.dart';
import '../models/alert.dart';

class AlertsRepository {
  Future<List<AlertItem>> fetchAlerts() async {
    final res = await ApiClient.instance.get('/alerts');
    final list = res.data is List ? res.data as List : <dynamic>[];
    return list.map((e) => AlertItem.fromJson(e as Map<String, dynamic>)).toList();
  }
}

