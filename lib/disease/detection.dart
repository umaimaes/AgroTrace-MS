import 'dart:async';

import 'package:camera/camera.dart';
import 'package:dio/dio.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';

import '../core/api_client.dart';
import '../core/config.dart';
import 'image_converter.dart';

class DiseaseDetection extends StatefulWidget {
  final bool isEmbedded;
  
  const DiseaseDetection({
    super.key, 
    this.isEmbedded = false,
  });

  @override
  State<DiseaseDetection> createState() => _DiseaseDetectionState();
}

class _DiseaseDetectionState extends State<DiseaseDetection> {
  CameraController? _controller;
  bool _isProcessing = false;
  List<dynamic> _detections = [];
  String? _status;
  Timer? _throttleTimer;
  bool _isCameraInitialized = false;
  bool _hasError = false;
  
  // Debug counters
  int _frameCount = 0;
  String _lastDebugMsg = "";
  
  // Last processed image size for bbox scaling
  Size _lastImageSize = Size.zero;

  Timer? _pollingTimer;

  @override
  void initState() {
    super.initState();
    _initializeCamera();
  }

  Future<void> _initializeCamera() async {
    try {
      final cameras = await availableCameras();
      if (cameras.isEmpty) {
        setState(() => _lastDebugMsg = "No cameras found");
        return;
      }

      final controller = CameraController(
        cameras.first,
        ResolutionPreset.medium, 
        enableAudio: false,
        imageFormatGroup: ImageFormatGroup.yuv420,
      );

      await controller.initialize();
      if (!mounted) return;

      setState(() {
        _controller = controller;
        _isCameraInitialized = true;
        _lastDebugMsg = "Camera Init OK";
      });

      // Try streaming first (Android/iOS)
      try {
        await _controller!.startImageStream(_processCameraImage);
      } catch (e) {
        // Fallback for Web or devices where streaming fails
        debugPrint("Image stream failed, falling back to polling: $e");
        setState(() => _lastDebugMsg = "Stream failed, using poll");
        
        _pollingTimer = Timer.periodic(const Duration(seconds: 1), (timer) async {
           if (!mounted || _isProcessing) return;
           await _captureAndAnalyze();
        });
      }

    } catch (e) {
      debugPrint("Camera initialization failed: $e");
      setState(() => _lastDebugMsg = "Init Error: $e");
    }
  }

  Future<void> _captureAndAnalyze() async {
    if (_controller == null || !_controller!.value.isInitialized) return;
    if (_isProcessing) return;

    if (mounted) setState(() => _isProcessing = true);

    try {
      _frameCount++;
      final file = await _controller!.takePicture();
      final bytes = await file.readAsBytes();
      
      // For fallback polling, we already have JPEG bytes!
      // No need to convert.
      await _sendImageToApi(bytes, Size(_controller!.value.previewSize?.width ?? 640, _controller!.value.previewSize?.height ?? 480));
    } catch (e) {
      debugPrint("Polling error: $e");
      if (mounted) setState(() => _lastDebugMsg = "Poll Error: $e");
    } finally {
      if (mounted) setState(() => _isProcessing = false);
    }
  }

  void _processCameraImage(CameraImage image) {
    _frameCount++;
    if (_frameCount % 60 == 0 && mounted) {
       setState(() {}); 
    }

    if (_isProcessing) return;
    
    // Throttle to ~5 FPS (200ms)
    if (_throttleTimer != null && _throttleTimer!.isActive) return;
    _throttleTimer = Timer(const Duration(milliseconds: 200), () {});

    if (mounted) {
      setState(() {
        _isProcessing = true;
      });
    }
    _analyzeImage(image);
  }

  Future<void> _sendImageToApi(Uint8List bytes, Size size) async {
     try {
      // Send to API
      final formData = FormData.fromMap({
        'file': MultipartFile.fromBytes(bytes, filename: 'frame.jpg'),
      });

      final base = BackendConfig.authBaseUrl.value;
      final response = await ApiClient.instance.post('$base/detect', data: formData);
      
      if (response.statusCode == 200 && response.data != null) {
        final data = response.data;
        if (mounted) {
          setState(() {
            _detections = data['detections'] ?? [];
            _status = data['status'];
            _hasError = false;
            _lastImageSize = size;
          });
        }
      }
    } catch (e) {
      String msg = e.toString();
      bool isServiceUnavailable = false;
      if (e is DioException) {
        final r = e.response;
        final data = r?.data;
        if (r?.statusCode == 503) {
          isServiceUnavailable = true;
        }
        if (data is Map && data['message'] is String) {
          msg = data['message'] as String;
        } else if (data is Map && data['error'] is String) {
          msg = data['error'] as String;
        } else if (data is String && data.isNotEmpty) {
          msg = data;
        } else if (e.message != null && e.message!.isNotEmpty) {
          msg = e.message!;
        }
      }
      if (mounted) {
        setState(() {
          _hasError = true;
          _status = isServiceUnavailable ? 'AI Service unavailable: leaf detection inactive or model missing' : "Error: ${msg.length > 60 ? msg.substring(0, 60) + '...' : msg}";
        });
      }
    }
  }

  Future<void> _analyzeImage(CameraImage image) async {
    try {
      // Prepare data for Isolate
      // On Web/Android/iOS, planes might differ.
      // Ensure we have planes.
      if (image.planes.isEmpty) {
        throw Exception("No image planes");
      }

      final packet = CameraImagePacket(
        planes: image.planes.map((p) => p.bytes).toList(),
        width: image.width,
        height: image.height,
        yRowStride: image.planes[0].bytesPerRow,
        uvRowStride: image.planes.length > 1 ? image.planes[1].bytesPerRow : 0,
        uvPixelStride: image.planes.length > 1 ? image.planes[1].bytesPerPixel ?? 1 : 1,
        format: image.format.group,
      );

      // Run conversion in background
      final result = await compute(convertCameraImageToJpeg, packet);
      
      if (result.bytes.isEmpty) {
        // debugPrint("Conversion failed or empty result");
        return;
      }
      
      await _sendImageToApi(result.bytes, Size(result.width.toDouble(), result.height.toDouble()));

    } catch (e) {
      debugPrint("Error detecting disease: $e");
      if (mounted) {
        setState(() {
          _hasError = true;
          // Show actual error for debugging
          _status = "Error: ${e.toString().length > 20 ? e.toString().substring(0, 20) + '...' : e.toString()}";
        });
      }
    } finally {
      if (mounted) {
        setState(() {
          _isProcessing = false;
        });
      }
    }
  }

  @override
  void dispose() {
    _controller?.dispose();
    _throttleTimer?.cancel();
    _pollingTimer?.cancel();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    if (!_isCameraInitialized || _controller == null) {
      if (widget.isEmbedded) {
        return Container(
          color: Colors.black,
          child: const Center(child: CircularProgressIndicator()),
        );
      }
      return const Scaffold(
        backgroundColor: Colors.black,
        body: Center(child: CircularProgressIndicator()),
      );
    }

    final content = Center(
      child: AspectRatio(
        aspectRatio: _controller!.value.aspectRatio,
        child: Stack(
          fit: StackFit.expand,
          children: [
            CameraPreview(_controller!),
            if (_detections.isNotEmpty && _lastImageSize != Size.zero)
              CustomPaint(
                painter: BoundingBoxPainter(
                  detections: _detections,
                  imageSize: _lastImageSize,
                ),
              ),
            if (_status != null)
              Positioned(
                bottom: 20,
                left: 20,
                right: 20,
                child: Container(
                  padding: const EdgeInsets.all(12),
                  decoration: BoxDecoration(
                    color: _hasError ? Colors.red.withOpacity(0.8) : Colors.black54,
                    borderRadius: BorderRadius.circular(8),
                  ),
                  child: Text(
                    _status!,
                    style: const TextStyle(color: Colors.white, fontSize: 18),
                    textAlign: TextAlign.center,
                  ),
                ),
              ),
            Positioned(
              top: 10,
              right: 10,
              child: Container(
                padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                decoration: BoxDecoration(
                  color: _isProcessing ? Colors.green : Colors.grey,
                  borderRadius: BorderRadius.circular(4),
                ),
                child: Column(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    Text(
                      _isProcessing ? 'SCANNING' : 'STANDBY',
                      style: const TextStyle(
                        color: Colors.white,
                        fontSize: 10,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                    if (_lastDebugMsg.isNotEmpty)
                      Text(
                        _lastDebugMsg,
                        style: const TextStyle(color: Colors.white, fontSize: 8),
                      ),
                    Text(
                      "F: $_frameCount",
                      style: const TextStyle(color: Colors.white, fontSize: 8),
                    ),
                  ],
                ),
              ),
            ),
          ],
        ),
      ),
    );

    if (widget.isEmbedded) {
      return content;
    }

    return Scaffold(
      appBar: AppBar(leading: IconButton(onPressed: () { Navigator.pushReplacementNamed(context, '/role'); }, icon: const Icon(Icons.arrow_back)), title: const Text('Live Disease Detection')),
      backgroundColor: Colors.black,
      body: content,
    );
  }
}

class BoundingBoxPainter extends CustomPainter {
  final List<dynamic> detections;
  final Size imageSize;

  BoundingBoxPainter({
    required this.detections,
    required this.imageSize,
  });

  @override
  void paint(Canvas canvas, Size size) {
    final paint = Paint()
      ..style = PaintingStyle.stroke
      ..strokeWidth = 3.0
      ..color = Colors.red;

    final textPaint = TextPainter(
      textDirection: TextDirection.ltr,
    );

    for (var det in detections) {
      final bbox = det['bbox'];
      if (bbox is List && bbox.length >= 4) {
        double x1 = bbox[0].toDouble();
        double y1 = bbox[1].toDouble();
        double xOrW = bbox[2].toDouble();
        double yOrH = bbox[3].toDouble();

        Rect rect;

        // Case A: normalized [x, y, w, h] (0.0-1.0)
        if (x1 <= 1.0 && y1 <= 1.0 && xOrW <= 1.0 && yOrH <= 1.0) {
          final double x = x1 * size.width;
          final double y = y1 * size.height;
          final double w = xOrW * size.width;
          final double h = yOrH * size.height;
          rect = Rect.fromLTWH(x, y, w, h);
        } else {
          // Case B: pixels [x1, y1, x2, y2] (Ultralytics xyxy)
          // or pixels [x, y, w, h]; handle both robustly
          final double scaleX = size.width / imageSize.width;
          final double scaleY = size.height / imageSize.height;

          // Heuristic: if third value is greater than first, treat as x2/y2 (xyxy)
          final bool looksLikeX2Y2 = xOrW >= x1 && yOrH >= y1;
          if (looksLikeX2Y2) {
            final double x2 = xOrW * scaleX;
            final double y2 = yOrH * scaleY;
            final double sx1 = x1 * scaleX;
            final double sy1 = y1 * scaleY;
            rect = Rect.fromLTRB(sx1, sy1, x2, y2);
          } else {
            final double sx = x1 * scaleX;
            final double sy = y1 * scaleY;
            final double sw = xOrW * scaleX;
            final double sh = yOrH * scaleY;
            rect = Rect.fromLTWH(sx, sy, sw, sh);
          }
        }

        canvas.drawRect(rect, paint);

        final label = "${det['class']} ${(det['confidence'] is num ? ((det['confidence'] as num) * 100).toStringAsFixed(0) : det['confidence'].toString())}%";
        textPaint.text = const TextSpan(style: TextStyle(color: Colors.white, backgroundColor: Colors.red, fontSize: 14, fontWeight: FontWeight.bold));
        textPaint.text = TextSpan(text: label, style: const TextStyle(color: Colors.white, backgroundColor: Colors.red, fontSize: 14, fontWeight: FontWeight.bold));
        textPaint.layout();
        final double lx = rect.left;
        final double ly = rect.top > 20 ? rect.top - 20 : rect.top;
        textPaint.paint(canvas, Offset(lx, ly));
      }
    }
  }

  @override
  bool shouldRepaint(covariant CustomPainter oldDelegate) => true;
}
