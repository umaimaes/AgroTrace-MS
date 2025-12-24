import '../api_client.dart';
import '../models/user.dart';
import '../config.dart';

class AuthRepository {
  Future<User> login(String email, String password) async {
    final base = BackendConfig.authBaseUrl.value;
    final res = await ApiClient.instance.post('$base/auth/login', data: {'email': email, 'password': password});
    final data = res.data is Map ? res.data as Map<String, dynamic> : <String, dynamic>{};
    return User.fromJson(data);
  }

  Future<void> logout() async {
    final base = BackendConfig.authBaseUrl.value;
    await ApiClient.instance.post('$base/auth/logout');
  }
}

