import 'package:flutter/foundation.dart';

class BackendConfig {
  static final ValueNotifier<String> baseUrl = ValueNotifier(const String.fromEnvironment('API_BASE_URL', defaultValue: 'http://localhost:8081'));
  static final ValueNotifier<String?> authToken = ValueNotifier(null);
  static final ValueNotifier<String> authBaseUrl = ValueNotifier(const String.fromEnvironment('AUTH_BASE_URL', defaultValue: 'http://localhost:8081'));
}

