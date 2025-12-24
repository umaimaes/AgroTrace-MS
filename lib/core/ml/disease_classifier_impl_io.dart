import 'dart:typed_data';
import 'dart:io';
import 'package:tflite_flutter/tflite_flutter.dart';

class DiseaseClassifier {
  Interpreter? _interpreter;

  Future<void> load(String modelPath) async {
    try {
      _interpreter = Interpreter.fromFile(File(modelPath));
    } catch (_) {
      _interpreter = null;
    }
  }

  Future<Map<String, dynamic>> classify(Uint8List imageBytes) async {
    if (_interpreter == null) {
      return {'label': 'unknown', 'score': 0.0};
    }
    return {'label': 'stub', 'score': 0.0};
  }
}

