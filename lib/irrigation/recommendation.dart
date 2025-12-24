import 'package:flutter/material.dart';
import 'package:dio/dio.dart';
import '../core/api_client.dart';
const recommBaseUrl = 'http://localhost:9000';

class IrrigationRecommendation extends StatefulWidget {
  const IrrigationRecommendation({super.key});
  @override
  State<IrrigationRecommendation> createState() => _IrrigationRecommendationState();
}

class _IrrigationRecommendationState extends State<IrrigationRecommendation> {
  final _formKey = GlobalKey<FormState>();
  final _stage = TextEditingController(text: '1');
  final _temperature = TextEditingController(text: '35.0');
  final _humidity = TextEditingController(text: '40.0');
  final _soilMoisture = TextEditingController(text: '30.0');
  final _nitrogen = TextEditingController(text: '140');
  final _phosphorus = TextEditingController(text: '70');
  final _potassium = TextEditingController(text: '200');
  final _ph = TextEditingController(text: '6.5');
  final _solarRadiation = TextEditingController(text: '600');
  final _windSpeed = TextEditingController(text: '5.0');

  bool _loading = false;
  Map<String, dynamic>? _result;
  String? _error;

  @override
  void dispose() {
    _stage.dispose();
    _temperature.dispose();
    _humidity.dispose();
    _soilMoisture.dispose();
    _nitrogen.dispose();
    _phosphorus.dispose();
    _potassium.dispose();
    _ph.dispose();
    _solarRadiation.dispose();
    _windSpeed.dispose();
    super.dispose();
  }

  Future<void> _submit() async {
    if (!_formKey.currentState!.validate()) return;
    setState(() {
      _loading = true;
      _error = null;
      _result = null;
    });
    try {
      final payload = {
        'stage': int.tryParse(_stage.text) ?? 0,
        'temperature': double.tryParse(_temperature.text) ?? 0.0,
        'humidity': double.tryParse(_humidity.text) ?? 0.0,
        'soil_moisture': double.tryParse(_soilMoisture.text) ?? 0.0,
        'nitrogen': int.tryParse(_nitrogen.text) ?? 0,
        'phosphorus': int.tryParse(_phosphorus.text) ?? 0,
        'potassium': int.tryParse(_potassium.text) ?? 0,
        'ph': double.tryParse(_ph.text) ?? 7.0,
        'solar_radiation': int.tryParse(_solarRadiation.text) ?? 0,
        'wind_speed': double.tryParse(_windSpeed.text) ?? 0.0,
      };
      final res = await ApiClient.instance.post('$recommBaseUrl/recommend', data: payload);
      final data = res.data is Map ? Map<String, dynamic>.from(res.data) : <String, dynamic>{};
      if (!mounted) return;
      await Navigator.of(context).push(
        MaterialPageRoute(builder: (_) => RecommendationPage(data: data)),
      );
    } catch (e) {
      String msg = e.toString();
      String code = '';
      try {
        if (e is DioException) {
          code = (e.response?.statusCode ?? '').toString();
          final data = e.response?.data;
          if (data is Map && data['message'] is String) {
            msg = data['message'] as String;
          } else if (data is Map && data['detail'] is String) {
            msg = data['detail'] as String;
          } else if (data is Map && data['error'] is String) {
            msg = data['error'] as String;
          } else if (data is String && data.isNotEmpty) {
            msg = data;
          } else if (e.message != null && e.message!.isNotEmpty) {
            msg = e.message!;
          }
        }
      } catch (_) {}
      setState(() {
        _error = code.isNotEmpty ? 'HTTP $code: $msg' : msg;
      });
    } finally {
      setState(() {
        _loading = false;
      });
    }
  }

  Widget _field(String label, TextEditingController c, {String? hint}) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(label, style: const TextStyle(fontWeight: FontWeight.bold)),
        const SizedBox(height: 6),
        TextFormField(
          controller: c,
          decoration: InputDecoration(
            hintText: hint,
            filled: true,
            fillColor: Colors.white,
            border: OutlineInputBorder(borderRadius: BorderRadius.circular(12), borderSide: BorderSide.none),
          ),
          validator: (v) => (v == null || v.isEmpty) ? 'Required' : null,
        ),
      ],
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Irrigation Recommendation'), backgroundColor: Colors.white, foregroundColor: Colors.black),
      backgroundColor: Colors.grey[100],
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16),
        child: Form(
          key: _formKey,
          child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
            const Text('Inputs', style: TextStyle(fontWeight: FontWeight.bold)),
            const SizedBox(height: 8),
            Card(
              child: Padding(
                padding: const EdgeInsets.all(12),
                child: Column(children: [
                  _field('Stage', _stage),
                  const SizedBox(height: 8),
                  _field('Temperature (°C)', _temperature),
                  const SizedBox(height: 8),
                  _field('Humidity (%)', _humidity),
                  const SizedBox(height: 8),
                  _field('Soil Moisture (%)', _soilMoisture),
                  const SizedBox(height: 8),
                  _field('Nitrogen (ppm)', _nitrogen),
                  const SizedBox(height: 8),
                  _field('Phosphorus (ppm)', _phosphorus),
                  const SizedBox(height: 8),
                  _field('Potassium (ppm)', _potassium),
                  const SizedBox(height: 8),
                  _field('pH', _ph),
                  const SizedBox(height: 8),
                  _field('Solar Radiation (W/m²)', _solarRadiation),
                  const SizedBox(height: 8),
                  _field('Wind Speed (m/s)', _windSpeed),
                  const SizedBox(height: 12),
                  SizedBox(
                    width: double.infinity,
                    height: 44,
                    child: ElevatedButton(
                      onPressed: _loading ? null : _submit,
                      style: ElevatedButton.styleFrom(backgroundColor: const Color(0xFF00C853), shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12))),
                      child: Text(_loading ? 'Submitting...' : 'Get Recommendation', style: const TextStyle(color: Colors.black, fontWeight: FontWeight.bold)),
                    ),
                  ),
                ]),
              ),
            ),
            const SizedBox(height: 16),
            if (_error != null)
              Card(child: ListTile(leading: const Icon(Icons.error, color: Colors.red), title: const Text('Error'), subtitle: Text(_error!))),
            const SizedBox.shrink(),
          ]),
        ),
      ),
    );
  }
}

class RecommendationPage extends StatelessWidget {
  final Map<String, dynamic> data;
  const RecommendationPage({super.key, required this.data});
  @override
  Widget build(BuildContext context) {
    final prediction = (data['prediction'] ?? '').toString();
    final confidence = ((data['confidence'] ?? 0) as num).toStringAsFixed(1);
    final isSuitable = (data['is_suitable'] ?? false) == true;
    final recs = (data['recommendations'] as List?) ?? <dynamic>[];
    return Scaffold(
      appBar: AppBar(title: const Text('Recommendation Result'), backgroundColor: Colors.white, foregroundColor: Colors.black),
      backgroundColor: Colors.grey[100],
      body: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
          Card(
            child: Padding(
              padding: const EdgeInsets.all(12),
              child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
                Row(children: [
                  const Icon(Icons.analytics, color: Colors.green),
                  const SizedBox(width: 8),
                  Text(prediction, style: const TextStyle(fontWeight: FontWeight.bold)),
                  const Spacer(),
                  Text('Conf: $confidence%'),
                ]),
                const SizedBox(height: 8),
                Row(children: [
                  const Text('Suitable:'),
                  const SizedBox(width: 6),
                  Container(
                    padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                    decoration: BoxDecoration(color: (isSuitable ? Colors.green : Colors.red).withValues(alpha: 0.15), borderRadius: BorderRadius.circular(8)),
                    child: Text(isSuitable ? 'Yes' : 'No', style: TextStyle(color: isSuitable ? Colors.green : Colors.red)),
                  ),
                ]),
                const SizedBox(height: 12),
                const Text('Recommendations'),
                const SizedBox(height: 6),
                ...recs.map((e) => Padding(
                      padding: const EdgeInsets.symmetric(vertical: 4),
                      child: Row(children: [
                        const Icon(Icons.check_circle, color: Colors.blue, size: 18),
                        const SizedBox(width: 6),
                        Expanded(child: Text(e.toString())),
                      ]),
                    )),
              ]),
            ),
          ),
        ]),
      ),
    );
  }
}
