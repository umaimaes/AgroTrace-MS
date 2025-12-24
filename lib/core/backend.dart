import 'config.dart';
import 'api_client.dart';

class Backend {
  static void init({String? baseUrl, String? authBaseUrl, String? token}) {
    if (baseUrl != null) {
      BackendConfig.baseUrl.value = baseUrl;
    }
    if (authBaseUrl != null) {
      BackendConfig.authBaseUrl.value = authBaseUrl;
    }
    BackendConfig.authToken.value = token;
    ApiClient.instance;
  }
}

