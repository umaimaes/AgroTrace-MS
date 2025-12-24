import 'dart:typed_data';
import 'package:hive_flutter/hive_flutter.dart';

class LocalStore {
  static bool _initialized = false;
  static Future<void> init() async {
    if (_initialized) return;
    await Hive.initFlutter();
    await Hive.openBox('settings');
    await Hive.openBox('parcels');
    await Hive.openBox('alerts');
    await Hive.openBox('photos');
    _initialized = true;
  }

  static Box get settings => Hive.box('settings');
  static Box get parcels => Hive.box('parcels');
  static Box get alerts => Hive.box('alerts');
  static Box get photos => Hive.box('photos');

  static Future<void> savePhoto(String id, Uint8List bytes) async {
    await photos.put(id, bytes);
  }

  static List<String> photoIds() {
    return photos.keys.map((e) => e.toString()).toList();
  }

  static Uint8List? photoBytes(String id) {
    final v = photos.get(id);
    if (v is Uint8List) return v;
    return null;
  }
}

