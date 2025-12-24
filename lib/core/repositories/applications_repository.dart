import '../api_client.dart';
import '../models/application.dart';

class ApplicationsRepository {
  Future<Application> createApplication({
    required int applicantId,
    required int parcelId,
    required String title,
    required String type,
    required String details,
  }) async {
    final res = await ApiClient.instance.post(
      '/api/applications',
      data: {
        'applicantId': applicantId,
        'parcelId': parcelId,
        'title': title,
        'type': type,
        'details': details,
      },
    );
    final data = res.data is Map ? res.data as Map<String, dynamic> : <String, dynamic>{};
    return Application.fromJson(data);
  }

  Future<List<Application>> fetchApplications({int? applicantId, ApplicationStatus? status}) async {
    final query = <String, dynamic>{};
    if (applicantId != null) query['applicantId'] = applicantId;
    if (status != null) query['status'] = applicationStatusToString(status);
    final res = await ApiClient.instance.get('/api/applications', query: query.isEmpty ? null : query);
    final list = res.data is List ? res.data as List : <dynamic>[];
    return list.map((e) => Application.fromJson(e as Map<String, dynamic>)).toList();
  }

  Future<Application> fetchApplication(int id) async {
    final res = await ApiClient.instance.get('/api/applications/$id');
    final data = res.data is Map ? res.data as Map<String, dynamic> : <String, dynamic>{};
    return Application.fromJson(data);
  }

  Future<Application> updateApplication({
    required int id,
    required int applicantId,
    required int parcelId,
    required String title,
    required String type,
    required String details,
  }) async {
    final res = await ApiClient.instance.put(
      '/api/applications/$id',
      data: {
        'applicantId': applicantId,
        'parcelId': parcelId,
        'title': title,
        'type': type,
        'details': details,
      },
    );
    final data = res.data is Map ? res.data as Map<String, dynamic> : <String, dynamic>{};
    return Application.fromJson(data);
  }

  Future<Application> changeStatus({
    required int id,
    required ApplicationStatus status,
    String? statusReason,
  }) async {
    final res = await ApiClient.instance.post(
      '/api/applications/$id/status',
      data: {
        'status': applicationStatusToString(status),
        'statusReason': statusReason,
      },
    );
    final data = res.data is Map ? res.data as Map<String, dynamic> : <String, dynamic>{};
    return Application.fromJson(data);
  }
}

