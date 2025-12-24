import 'package:dio/dio.dart';
import 'config.dart';

class ApiClient {
  ApiClient._internal() {
    _dio = Dio(
      BaseOptions(
        baseUrl: BackendConfig.baseUrl.value,
        connectTimeout: const Duration(seconds: 20),
        receiveTimeout: const Duration(seconds: 20),
      ),
    );
    BackendConfig.baseUrl.addListener(() {
      _dio.options = _dio.options.copyWith(baseUrl: BackendConfig.baseUrl.value);
    });
    BackendConfig.authToken.addListener(() {
      final token = BackendConfig.authToken.value;
      final headers = Map<String, dynamic>.from(_dio.options.headers);
      if (token != null && token.isNotEmpty) {
        headers['Authorization'] = 'Bearer $token';
      } else {
        headers.remove('Authorization');
      }
      _dio.options = _dio.options.copyWith(headers: headers);
    });
    _dio.interceptors.add(
      InterceptorsWrapper(
        onRequest: (options, handler) {
          handler.next(options);
        },
        onError: (error, handler) {
          handler.next(error);
        },
      ),
    );
    _dio.interceptors.add(
      LogInterceptor(
        requestBody: true,
        responseBody: true,
      ),
    );
  }

  late final Dio _dio;

  static final ApiClient instance = ApiClient._internal();

  Future<Response<dynamic>> get(String path, {Map<String, dynamic>? query}) {
    return _dio.get(path, queryParameters: query);
  }

  Future<Response<dynamic>> post(String path, {dynamic data, Map<String, dynamic>? query}) {
    return _dio.post(path, data: data, queryParameters: query);
  }

  Future<Response<dynamic>> put(String path, {dynamic data, Map<String, dynamic>? query}) {
    return _dio.put(path, data: data, queryParameters: query);
  }

  Future<Response<dynamic>> delete(String path, {dynamic data, Map<String, dynamic>? query}) {
    return _dio.delete(path, data: data, queryParameters: query);
  }
}

