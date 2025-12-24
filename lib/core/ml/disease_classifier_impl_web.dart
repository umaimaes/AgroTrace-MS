import 'dart:typed_data';

class DiseaseClassifier {
  Future<void> load(String modelPath) async {}

  Future<Map<String, dynamic>> classify(Uint8List imageBytes) async {
    return {'label': 'unsupported', 'score': 0.0};
  }
}
