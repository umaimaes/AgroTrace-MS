import 'dart:typed_data';
import 'package:camera/camera.dart';
import 'package:image/image.dart' as img;

class CameraImagePacket {
  final List<Uint8List> planes;
  final int width;
  final int height;
  final int yRowStride;
  final int uvRowStride;
  final int uvPixelStride;
  final ImageFormatGroup format;

  CameraImagePacket({
    required this.planes,
    required this.width,
    required this.height,
    required this.yRowStride,
    required this.uvRowStride,
    required this.uvPixelStride,
    required this.format,
  });
}

class ConversionResult {
  final Uint8List bytes;
  final double scale;
  final int width;
  final int height;

  ConversionResult(this.bytes, this.scale, this.width, this.height);
}

Future<ConversionResult> convertCameraImageToJpeg(CameraImagePacket packet) async {
  img.Image image;
  double scale = 1.0;

  // Subsample to speed up processing and reduce size
  // Target width approx 640
  int step = 1;
  if (packet.width > 640) {
    step = (packet.width / 640).ceil();
    scale = 1.0 / step;
  }

  if (packet.format == ImageFormatGroup.yuv420) {
    image = _convertYUV420ToImage(packet, step);
  } else if (packet.format == ImageFormatGroup.bgra8888) {
    image = _convertBGRA8888ToImage(packet, step);
  } else {
    // Try to handle as BGRA if we have enough bytes (Web fallback)
    // Often web sends 'unknown' or other formats but the bytes are RGBA/BGRA
    try {
       if (packet.planes.isNotEmpty && packet.planes[0].length >= packet.width * packet.height * 4) {
          image = _convertBGRA8888ToImage(packet, step);
       } else {
          return ConversionResult(Uint8List(0), 1.0, 0, 0);
       }
    } catch (e) {
      return ConversionResult(Uint8List(0), 1.0, 0, 0);
    }
  }

  return ConversionResult(
    Uint8List.fromList(img.encodeJpg(image, quality: 70)),
    scale,
    image.width,
    image.height,
  );
}

img.Image _convertBGRA8888ToImage(CameraImagePacket packet, int step) {
  // Simple skipping
  final width = (packet.width / step).floor();
  final height = (packet.height / step).floor();
  final image = img.Image(width: width, height: height);
  
  final bgra = packet.planes[0].buffer.asUint8List();
  final rowStride = packet.yRowStride;

  for (int y = 0; y < height; y++) {
    for (int x = 0; x < width; x++) {
      final srcX = x * step;
      final srcY = y * step;
      final index = srcY * rowStride + srcX * 4;
      
      final b = bgra[index];
      final g = bgra[index + 1];
      final r = bgra[index + 2];
      // final a = bgra[index + 3];

      image.setPixelRgb(x, y, r, g, b);
    }
  }
  return image;
}

img.Image _convertYUV420ToImage(CameraImagePacket packet, int step) {
  final width = (packet.width / step).floor();
  final height = (packet.height / step).floor();
  final uvRowStride = packet.uvRowStride;
  final uvPixelStride = packet.uvPixelStride;
  final yRowStride = packet.yRowStride;

  final img.Image image = img.Image(width: width, height: height);

  final yPlane = packet.planes[0];
  final uPlane = packet.planes[1];
  final vPlane = packet.planes[2];

  for (int y = 0; y < height; y++) {
    for (int x = 0; x < width; x++) {
      final srcX = x * step;
      final srcY = y * step;

      final int uvIndex = (uvPixelStride * (srcX / 2).floor()) + (uvRowStride * (srcY / 2).floor());
      final int yIndex = srcY * yRowStride + srcX;

      final yValue = yPlane[yIndex];
      final uValue = uPlane[uvIndex];
      final vValue = vPlane[uvIndex];

      // YUV to RGB conversion
      int r = (yValue + 1.370705 * (vValue - 128)).round().clamp(0, 255);
      int g = (yValue - 0.337633 * (uValue - 128) - 0.698001 * (vValue - 128)).round().clamp(0, 255);
      int b = (yValue + 1.732446 * (uValue - 128)).round().clamp(0, 255);

      image.setPixelRgb(x, y, r, g, b);
    }
  }
  return image;
}
