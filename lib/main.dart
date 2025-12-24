import 'package:flutter/material.dart';
import 'package:flutter/foundation.dart';
import 'package:camera/camera.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'core/local_store.dart';
import 'package:pdf/widgets.dart' as pw;
import 'dart:convert';
import 'core/exporter.dart';
import 'auth/login.dart';
import 'auth/signup.dart';
import 'auth/forgot_password.dart';
import 'auth/role_selection.dart';
import 'alerts/alerts.dart';
import 'farmer/profile_settings.dart';
import 'irrigation/recommendation.dart';
import 'disease/detection.dart';
import 'core/backend.dart';
import 'core/config.dart';
import 'core/repositories/farms_repository.dart';
import 'core/models/farm.dart';
import 'core/models/farm_parcel.dart';
import 'core/repositories/applications_repository.dart';
import 'core/models/application.dart';

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await LocalStore.init();
  const envBase = String.fromEnvironment('API_BASE_URL', defaultValue: '');
  const envAuth = String.fromEnvironment('AUTH_BASE_URL', defaultValue: '');
  final baseUrl = envBase.isNotEmpty
      ? envBase
      : (kIsWeb
          ? 'http://localhost:8082'
          : (defaultTargetPlatform == TargetPlatform.android
              ? 'http://10.0.2.2:8082'
              : 'http://localhost:8082'));
  final authBaseUrl = envAuth.isNotEmpty
      ? envAuth
      : (kIsWeb
          ? 'http://localhost:8081'
          : (defaultTargetPlatform == TargetPlatform.android
              ? 'http://10.0.2.2:8081'
              : 'http://localhost:8081'));
  Backend.init(baseUrl: baseUrl, authBaseUrl: authBaseUrl);
  runApp(const FarmManagementApp());
}

class FarmManagementApp extends StatefulWidget {
  const FarmManagementApp({super.key});

  @override
  State<FarmManagementApp> createState() => _FarmManagementAppState();
}

class _FarmManagementAppState extends State<FarmManagementApp> {
  ThemeMode _themeMode = ThemeMode.light;

  ThemeData get _lightTheme => ThemeData(
        useMaterial3: true,
        colorScheme: ColorScheme.fromSeed(
          seedColor: const Color(0xFF00C853),
          brightness: Brightness.light,
        ),
        scaffoldBackgroundColor: const Color(0xFFF5F6F8),
        appBarTheme: const AppBarTheme(
          backgroundColor: Colors.white,
          foregroundColor: Colors.black,
          elevation: 0,
        ),
        cardTheme: CardThemeData(
          color: Colors.white,
          surfaceTintColor: Colors.transparent,
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
        ),
        elevatedButtonTheme: ElevatedButtonThemeData(
          style: ElevatedButton.styleFrom(
            shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
          ),
        ),
      );

  ThemeData get _darkTheme => ThemeData(
        useMaterial3: true,
        colorScheme: ColorScheme.fromSeed(
          seedColor: const Color(0xFF00C853),
          brightness: Brightness.dark,
        ),
        scaffoldBackgroundColor: const Color(0xFF0D2818),
        appBarTheme: const AppBarTheme(
          backgroundColor: Colors.black,
          foregroundColor: Colors.white,
          elevation: 0,
        ),
        cardTheme: CardThemeData(
          color: const Color(0xFF1F1F1F),
          surfaceTintColor: Colors.transparent,
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
        ),
        elevatedButtonTheme: ElevatedButtonThemeData(
          style: ElevatedButton.styleFrom(
            shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
          ),
        ),
      );

  @override
  void initState() {
    super.initState();
    SharedPreferences.getInstance().then((p) {
      final isDark = p.getBool('isDark') ?? false;
      if (_themeMode != (isDark ? ThemeMode.dark : ThemeMode.light)) {
        setState(() {
          _themeMode = isDark ? ThemeMode.dark : ThemeMode.light;
        });
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Farm Management',
      theme: _lightTheme,
      darkTheme: _darkTheme,
      themeMode: _themeMode,
      initialRoute: '/login',
      routes: {
        '/login': (_) => const Login(),
        '/signup': (_) => const SignUp(),
        '/forgot': (_) => const ForgotPassword(),
        '/role': (_) => const RoleSelection(),
        '/farmer': (_) => const FarmerDashboard(),
        '/drone': (_) => const DroneDashboard(),
        '/admin': (_) => const AdminDashboard(),
        '/admin/users': (_) => const ManageUsers(),
        '/admin/farms': (_) => const ManageFarmsParcels(),
        '/admin/applications': (_) => const ApplicationsAdmin(),
        '/admin/devices': (_) => const ManageSensorsDrones(),
        '/admin/health': (_) => const SystemHealthMonitor(),
        '/admin/ai': (_) => const ManageAIModels(),
        '/admin/tasks': (_) => const ExportReports(),
        '/alerts': (_) => const Alerts(),
        '/irrigation': (_) => const IrrigationRecommendation(),
        '/disease': (_) => const DiseaseDetection(),
        '/camera': (_) => const DeviceCameraView(),
      '/farmer/profile': (_) => FarmerProfileSettings(
              isDark: _themeMode == ThemeMode.dark,
              onToggleTheme: (v) {
                setState(() {
                  _themeMode = v ? ThemeMode.dark : ThemeMode.light;
                });
                SharedPreferences.getInstance().then((p) => p.setBool('isDark', v));
              },
            ),
      },
      onGenerateRoute: (settings) {
        if (settings.name == '/parcel') {
          final args = settings.arguments as Map<String, dynamic>;
          return MaterialPageRoute(
            builder: (_) => ParcelDetail(
              name: args['name'],
              area: args['area'],
              crop: args['crop'],
              health: args['health'],
              irrigation: args['irrigation'],
              tags: args['tags'],
            ),
          );
        }
        return null;
      },
      debugShowCheckedModeBanner: false,
    );
  }
}





// ============= FARMER DASHBOARD =============
class FarmerDashboard extends StatefulWidget {
  const FarmerDashboard({super.key});

  @override
  State<FarmerDashboard> createState() => _FarmerDashboardState();
}

class _FarmerDashboardState extends State<FarmerDashboard> {
  int _selectedIndex = 0;

  final List<Widget> _s = [
    const Home(),
    const Parcels(),
    const Sensors(),
    const Alerts(),
    const Reports(),
  ];

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: _s[_selectedIndex],
      bottomNavigationBar: BottomNavigationBar(
        currentIndex: _selectedIndex,
        onTap: (index) {
          setState(() {
            _selectedIndex = index;
          });
        },
        type: BottomNavigationBarType.fixed,
        backgroundColor: const Color(0xFF1a3a2a),
        selectedItemColor: const Color(0xFF00C853),
        unselectedItemColor: Colors.grey,
        items: const [
          BottomNavigationBarItem(icon: Icon(Icons.home), label: 'Home'),
          BottomNavigationBarItem(icon: Icon(Icons.grid_view), label: 'Parcels'),
          BottomNavigationBarItem(icon: Icon(Icons.sensors), label: 'Sensors'),
          BottomNavigationBarItem(icon: Icon(Icons.notifications), label: 'Alerts'),
          BottomNavigationBarItem(icon: Icon(Icons.assessment), label: 'Reports'),
        ],
      ),
    );
  }
}

// ============= HOME  =============
class Home extends StatelessWidget {
  const Home({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.grey[100],
      appBar: AppBar(
        backgroundColor: Colors.white,
        elevation: 0,
        leading: InkWell(
          onTap: () {
            Navigator.of(context).pushNamed('/farmer/profile');
          },
          child: const Padding(
            padding: EdgeInsets.all(8.0),
            child: CircleAvatar(
              backgroundColor: Color(0xFF00C853),
              child: Icon(Icons.person, color: Colors.white),
            ),
          ),
        ),
        actions: [
          IconButton(
            icon: const Icon(Icons.arrow_back, color: Colors.black),
            onPressed: () {
              Navigator.pushReplacementNamed(context, '/role');
            },
          ),
          IconButton(
            icon: const Icon(Icons.notifications_none, color: Colors.black),
            onPressed: () {
              Navigator.of(context).pushNamed('/alerts');
            },
          ),
        ],
      ),
      body: SingleChildScrollView(
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Container(
              color: Colors.white,
              padding: const EdgeInsets.all(16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const Text(
                    'My Farm',
                    style: TextStyle(fontSize: 24, fontWeight: FontWeight.bold),
                  ),
                  const SizedBox(height: 16),
                  Container(
                    height: 120,
                    decoration: BoxDecoration(
                      color: Colors.green[100],
                      borderRadius: BorderRadius.circular(12),
                    ),
                    child: const Center(
                      child: Text('Map View', style: TextStyle(color: Colors.grey)),
                    ),
                  ),
                ],
              ),
            ),
            const SizedBox(height: 16),
            Container(
              color: Colors.white,
              padding: const EdgeInsets.all(16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const Text(
                    'Quick Statistics',
                    style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
                  ),
                  const SizedBox(height: 16),
                  Row(
                    children: [
                      Expanded(
                        child: _StatCard(
                          title: 'Total Area',
                          value: '120 ha',
                        ),
                      ),
                      const SizedBox(width: 12),
                      Expanded(
                        child: _StatCard(
                          title: 'Parcels',
                          value: '8',
                        ),
                      ),
                      const SizedBox(width: 12),
                      Expanded(
                        child: _StatCard(
                          title: 'Active',
                          value: '3',
                        ),
                      ),
                    ],
                  ),
                ],
              ),
            ),
            const SizedBox(height: 16),
            Container(
              color: Colors.white,
              padding: const EdgeInsets.all(16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const Text(
                    'Recent Alerts',
                    style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
                  ),
                  const SizedBox(height: 16),
                  _AlertItem(
                    icon: Icons.bug_report,
                    title: 'Pest detected',
                    subtitle: 'Parcel A - 10 min ago',
                    color: Colors.red,
                  ),
                  const SizedBox(height: 12),
                  _AlertItem(
                    icon: Icons.water_drop,
                    title: 'Low moisture levels',
                    subtitle: 'West Field - 45 min ago',
                    color: Colors.orange,
                  ),
                ],
              ),
            ),
            const SizedBox(height: 16),
            Container(
              color: Colors.white,
              padding: const EdgeInsets.all(16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const Text('Today\'s Irrigation Plan', style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold)),
                  const SizedBox(height: 8),
                  Row(children: const [Text('Progress'), Spacer(), Text('3 of 8 parcels')]),
                  const SizedBox(height: 8),
                  LinearProgressIndicator(value: 0.35, minHeight: 6, color: Color(0xFF00C853), backgroundColor: Color(0xFFEAEAEA)),
                  const SizedBox(height: 16),
                  _IrrigationListItem(name: 'Parcel C', minutes: 45),
                  const SizedBox(height: 12),
                  _IrrigationListItem(name: 'North Field', minutes: 60),
                  const SizedBox(height: 16),
                  SizedBox(
                    width: double.infinity,
                    height: 44,
                    child: ElevatedButton(
                      onPressed: () {
                        Navigator.of(context).pushNamed('/irrigation');
                      },
                      style: ElevatedButton.styleFrom(backgroundColor: const Color(0xFF00C853), shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12))),
                      child: const Text('View All Recommendations', style: TextStyle(color: Colors.black, fontWeight: FontWeight.bold)),
                    ),
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _StatCard extends StatelessWidget {
  final String title;
  final String value;

  const _StatCard({required this.title, required this.value});

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.grey[100],
        borderRadius: BorderRadius.circular(12),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            title,
            style: TextStyle(color: Colors.grey[600], fontSize: 12),
          ),
          const SizedBox(height: 8),
          Text(
            value,
            style: const TextStyle(fontSize: 20, fontWeight: FontWeight.bold),
          ),
        ],
      ),
    );
  }
}

class _AlertItem extends StatelessWidget {
  final IconData icon;
  final String title;
  final String subtitle;
  final Color color;

  const _AlertItem({
    required this.icon,
    required this.title,
    required this.subtitle,
    required this.color,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: Colors.grey[50],
        borderRadius: BorderRadius.circular(12),
      ),
      child: Row(
        children: [
          Container(
            padding: const EdgeInsets.all(8),
            decoration: BoxDecoration(
              color: color.withValues(alpha: 0.1),
              shape: BoxShape.circle,
            ),
            child: Icon(icon, color: color, size: 24),
          ),
          const SizedBox(width: 12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  title,
                  style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 16),
                ),
                Text(
                  subtitle,
                  style: TextStyle(color: Colors.grey[600], fontSize: 14),
                ),
              ],
            ),
          ),
          const Icon(Icons.chevron_right, color: Colors.grey),
        ],
      ),
    );
  }
}

class SafeNetworkImage extends StatelessWidget {
  final String url;
  final double? width;
  final double? height;
  final BoxFit? fit;
  final String? assetPath;
  const SafeNetworkImage({super.key, required this.url, this.width, this.height, this.fit, this.assetPath});
  @override
  Widget build(BuildContext context) {
    if (assetPath != null && assetPath!.isNotEmpty) {
      return Image.asset(assetPath!, width: width, height: height, fit: fit);
    }
    return Image.network(url, width: width, height: height, fit: fit, loadingBuilder: (context, child, loadingProgress) {
      if (loadingProgress == null) return child;
      return Container(width: width, height: height, color: Colors.grey[200], child: const Center(child: CircularProgressIndicator()));
    }, errorBuilder: (context, error, stackTrace) {
      return Container(width: width, height: height, color: Colors.grey[200], child: const Center(child: Icon(Icons.broken_image, color: Colors.grey)));
    });
  }
}

class _IrrigationListItem extends StatelessWidget {
  final String name;
  final int minutes;
  const _IrrigationListItem({required this.name, required this.minutes});
  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(color: Colors.grey[50], borderRadius: BorderRadius.circular(12)),
      child: Row(children: [
        Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
          Text(name, style: const TextStyle(fontWeight: FontWeight.bold)),
          Text('Recommended: $minutes min', style: const TextStyle(color: Colors.grey)),
        ]),
        const Spacer(),
        ElevatedButton(onPressed: () {}, style: ElevatedButton.styleFrom(backgroundColor: const Color(0xFFE5F9E8), foregroundColor: const Color(0xFF00C853)), child: const Text('Start')),
      ]),
    );
  }
}

// ============= PARCELS  =============
class Parcels extends StatefulWidget {
  const Parcels({super.key});
  @override
  State<Parcels> createState() => _ParcelsState();
}

class _ParcelsState extends State<Parcels> {
  final _repo = FarmsRepository();
  late Future<List<FarmParcel>> _future;

  @override
  void initState() {
    super.initState();
    _future = _repo.fetchParcels();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.grey[100],
      appBar: AppBar(
        title: const Text('My Parcels'),
        backgroundColor: Colors.white,
        foregroundColor: Colors.black,
        elevation: 0,
      ),
      body: FutureBuilder<List<FarmParcel>>(
        future: _future,
        builder: (context, snapshot) {
          if (snapshot.connectionState == ConnectionState.waiting) {
            return const Center(child: CircularProgressIndicator());
          }
          if (snapshot.hasError) {
            return Center(child: Text('Error: ${snapshot.error}'));
          }
          final items = snapshot.data ?? const <FarmParcel>[];
          if (items.isEmpty) {
            return const Center(child: Text('No parcels'));
          }
          return ListView.separated(
            padding: const EdgeInsets.all(16),
            separatorBuilder: (_, __) => const SizedBox(height: 12),
            itemCount: items.length,
            itemBuilder: (context, index) {
              final p = items[index];
              return ParcelCard(
                name: p.name,
                area: '${p.areaHa} ha',
                crop: p.cropType,
                status: 'Healthy',
                color: Colors.green,
              );
            },
          );
        },
      ),
    );
  }
}

class ParcelCard extends StatelessWidget {
  final String name;
  final String area;
  final String crop;
  final String status;
  final Color color;

  const ParcelCard({
    super.key,
    required this.name,
    required this.area,
    required this.crop,
    required this.status,
    required this.color,
  });

  @override
  Widget build(BuildContext context) {
    return InkWell(
      onTap: () {
        Navigator.of(context).pushNamed(
          '/parcel',
          arguments: {
            'name': name,
            'area': area,
            'crop': crop,
            'health': 95,
            'irrigation': 40,
            'tags': const ['Healthy', 'Needs Water', 'Pest Alert'],
          },
        );
      },
      child: Container(
        padding: const EdgeInsets.all(16),
        decoration: BoxDecoration(
          color: Colors.white,
          borderRadius: BorderRadius.circular(12),
        ),
        child: Row(
          children: [
            Container(
              padding: const EdgeInsets.all(12),
              decoration: BoxDecoration(
                color: color.withValues(alpha: 0.15),
                shape: BoxShape.circle,
              ),
              child: Icon(Icons.landscape, color: color, size: 28),
            ),
            const SizedBox(width: 16),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    name,
                    style: const TextStyle(
                      fontSize: 18,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                  const SizedBox(height: 4),
                  Text(
                    '$area • $crop',
                    style: TextStyle(
                      fontSize: 14,
                      color: Colors.grey[600],
                    ),
                  ),
                  const SizedBox(height: 4),
                  Text(
                    status,
                    style: TextStyle(
                      fontSize: 14,
                      color: color,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                ],
              ),
            ),
            const Icon(Icons.chevron_right, color: Colors.grey),
          ],
        ),
      ),
    );
  }
}


// ============= SCHEDULE  =============
class Schedule extends StatelessWidget {
  const Schedule({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Schedule'),
        backgroundColor: Colors.white,
        foregroundColor: Colors.black,
      ),
      body: const Center(
        child: Text('Schedule '),
      ),
    );
  }
}

// ============= REPORTS  =============
class Reports extends StatelessWidget {
  const Reports({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Reports'),
        backgroundColor: Colors.white,
        foregroundColor: Colors.black,
      ),
      body: const Center(
        child: Text('Reports '),
      ),
    );
  }
}

// moved to farmer/profile_settings.dart

class DroneDashboard extends StatefulWidget {
  const DroneDashboard({super.key});
  @override
  State<DroneDashboard> createState() => _DroneDashboardState();
}

class _DroneDashboardState extends State<DroneDashboard> {
  int index = 0;
  final s = const [
    LiveDroneStreamViewer(),
    FlightControlPanel(),
    MissionSummary(),
    DroneOperatorProfile(),
  ];
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: s[index],
      bottomNavigationBar: BottomNavigationBar(
        currentIndex: index,
        onTap: (i) => setState(() => index = i),
        type: BottomNavigationBarType.fixed,
        backgroundColor: Colors.white,
        selectedItemColor: const Color(0xFF00C853),
        unselectedItemColor: Colors.grey,
        items: const [
          BottomNavigationBarItem(icon: Icon(Icons.live_tv), label: 'Live'),
          BottomNavigationBarItem(icon: Icon(Icons.control_camera), label: 'Control'),
          BottomNavigationBarItem(icon: Icon(Icons.list_alt), label: 'Missions'),
          BottomNavigationBarItem(icon: Icon(Icons.person), label: 'Profile'),
        ],
      ),
    );
  }
}

class LiveDroneStreamViewer extends StatelessWidget {
  const LiveDroneStreamViewer({super.key});
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.grey[100],
      appBar: AppBar(
        leading: IconButton(onPressed: () { Navigator.pushReplacementNamed(context, '/role'); }, icon: const Icon(Icons.arrow_back)),
        title: Row(children: const [Text('Live Stream'), SizedBox(width: 8), Icon(Icons.circle, color: Colors.green, size: 10)]),
        backgroundColor: Colors.white, foregroundColor: Colors.black, elevation: 0),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16),
        child: Column(
          children: [
            ClipRRect(
              borderRadius: BorderRadius.circular(12),
              child: Container(
                height: 480,
                color: Colors.black,
                child: const DiseaseDetection(isEmbedded: true),
              ),
            ),
            const SizedBox(height: 16),
            Row(children: const [
              Expanded(child: _InfoTile(title: 'Altitude', value: '120m')),
              SizedBox(width: 12),
              Expanded(child: _InfoTile(title: 'Speed', value: '15 km/h')),
            ]),
            const SizedBox(height: 12),
            Row(children: const [
              Expanded(child: _InfoTile(title: 'Flight Time', value: '08:32')),
              SizedBox(width: 12),
              Expanded(child: _InfoTile(title: 'Battery', value: '78%')),
            ]),
            const SizedBox(height: 16),
            Row(
              children: [
                Expanded(child: OutlinedButton(onPressed: () { Navigator.of(context).pushNamed('/camera'); }, child: const Text('Capture'))),
                const SizedBox(width: 12),
                Expanded(child: OutlinedButton(onPressed: () {}, child: const Text('Switch View'))),
              ],
            ),
            const SizedBox(height: 12),
            SizedBox(
              width: double.infinity,
              height: 48,
              child: ElevatedButton(onPressed: () { Navigator.of(context).pushNamed('/disease'); }, style: ElevatedButton.styleFrom(backgroundColor: const Color(0xFF00C853)),
                child: const Text('Open Full Screen Detection', style: TextStyle(color: Colors.black))),
            ),
          ],
        ),
      ),
    );
  }
}

class FlightControlPanel extends StatelessWidget {
  const FlightControlPanel({super.key});
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.grey[100],
      appBar: AppBar(
        leading: IconButton(onPressed: () { Navigator.pushReplacementNamed(context, '/role'); }, icon: const Icon(Icons.arrow_back)),
        title: const Text('Flight Control'), backgroundColor: Colors.white, foregroundColor: Colors.black, elevation: 0),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16),
        child: Column(children: [
          Row(children: const [
            Expanded(child: _InfoTile(title: 'Battery', value: '92%')),
            SizedBox(width: 12),
            Expanded(child: _InfoTile(title: 'Signal', value: 'Strong')),
          ]),
          const SizedBox(height: 12),
          Row(children: const [
            Expanded(child: _InfoTile(title: 'Altitude', value: '120m')),
            SizedBox(width: 12),
            Expanded(child: _InfoTile(title: 'Status', value: 'Ready')),
          ]),
          const SizedBox(height: 12),
          ClipRRect(borderRadius: BorderRadius.circular(12), child: Container(height: 140, color: Colors.grey[300], child: const Center(child: Text('Live Map View')))),
          const SizedBox(height: 16),
          Container(
            padding: const EdgeInsets.all(16),
            decoration: BoxDecoration(color: Colors.white, borderRadius: BorderRadius.circular(12)),
            child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
              const Text('Scanning: Field A-3'),
              const SizedBox(height: 8),
              LinearProgressIndicator(value: 0.65, minHeight: 6, color: const Color(0xFF00C853), backgroundColor: Colors.grey[300]),
              const SizedBox(height: 12),
              Row(children: [
                Expanded(child: OutlinedButton(onPressed: () {}, child: const Text('Pause'))),
                const SizedBox(width: 12),
                Expanded(child: ElevatedButton(onPressed: () {}, style: ElevatedButton.styleFrom(backgroundColor: const Color(0xFF00C853)), child: const Text('Resume', style: TextStyle(color: Colors.black)))),
              ]),
              const SizedBox(height: 12),
              OutlinedButton.icon(onPressed: () {}, icon: const Icon(Icons.upload_file), label: const Text('Upload Images')),
            ]),
          ),
        ]),
      ),
    );
  }
}

class MissionSummary extends StatelessWidget {
  const MissionSummary({super.key});
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(leading: IconButton(onPressed: () { Navigator.pushReplacementNamed(context, '/role'); }, icon: const Icon(Icons.arrow_back)), title: const Text('Mission Summary'), elevation: 0),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          ClipRRect(borderRadius: BorderRadius.circular(16), child: SafeNetworkImage(url: 'https://images.pexels.com/photos/259280/pexels-photo-259280.jpeg', height: 180, fit: BoxFit.cover)),
          const SizedBox(height: 12),
          Card(child: ListTile(title: const Text('Mission Alpha-12'), subtitle: const Text('December 12, 2023'), trailing: const _StatusBadge(text: 'Completed', color: Colors.green))),
          const SizedBox(height: 8),
          Row(children: const [
            Expanded(child: StatInfoCard(title: 'Area Scanned', value: '50 Ha')),
            SizedBox(width: 12),
            Expanded(child: StatInfoCard(title: 'Flight Time', value: '45 Mins')),
          ]),
          const SizedBox(height: 8),
          Row(children: const [
            Expanded(child: StatInfoCard(title: 'Frames Collected', value: '12,450')),
            SizedBox(width: 12),
            Expanded(child: StatInfoCard(title: 'Detections', value: '892')),
          ]),
          const SizedBox(height: 12),
          Card(
            child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
              const ListTile(title: Text('Flight Path'), subtitle: Text('Scanned flight path overlaid on the field.')),
              Padding(padding: const EdgeInsets.all(16), child: Container(height: 160, decoration: BoxDecoration(color: Colors.grey[200], borderRadius: BorderRadius.circular(12)), child: const Center(child: Text('300x300')))),
            ]),
          ),
          const SizedBox(height: 12),
          Card(
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
                const Text('Detection Summary', style: TextStyle(fontWeight: FontWeight.bold)),
                const SizedBox(height: 4),
                const Text('Breakdown of anomalies found during the mission.', style: TextStyle(color: Colors.grey)),
                const SizedBox(height: 12),
                Row(children: const [
                  TotalCircle(value: '892', label: 'Total'),
                  SizedBox(width: 12),
                  Expanded(child: Column(children: [
                    LegendItem(color: Colors.amber, label: 'Nutrient Deficiency', count: 535),
                    SizedBox(height: 8),
                    LegendItem(color: Colors.green, label: 'Weed Growth', count: 223),
                    SizedBox(height: 8),
                    LegendItem(color: Colors.red, label: 'Pest Infestation', count: 134),
                  ])),
                ]),
              ]),
            ),
          ),
          const SizedBox(height: 12),
          Row(children: [
            const Expanded(child: Text('Image Gallery', style: TextStyle(fontWeight: FontWeight.bold))),
            TextButton(onPressed: () {}, child: const Text('View All')),
          ]),
          const SizedBox(height: 8),
          SizedBox(height: 92, child: ListView(scrollDirection: Axis.horizontal, children: [
            _galleryItem('https://images.pexels.com/photos/219692/pexels-photo-219692.jpeg'),
            const SizedBox(width: 12),
            _galleryItem('https://images.pexels.com/photos/212324/pexels-photo-212324.jpeg'),
            const SizedBox(width: 12),
            _galleryItem('https://images.pexels.com/photos/225258/pexels-photo-225258.jpeg'),
          ])),
          const SizedBox(height: 16),
          SizedBox(width: double.infinity, height: 52, child: ElevatedButton(onPressed: () {}, style: ElevatedButton.styleFrom(backgroundColor: const Color(0xFF00C853), shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12))), child: const Text('Generate Full Report', style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold))))
        ],
      ),
    );
  }
  Widget _galleryItem(String url) => ClipRRect(borderRadius: BorderRadius.circular(12), child: SafeNetworkImage(url: url, width: 120, height: 80, fit: BoxFit.cover));
}

class StatInfoCard extends StatelessWidget {
  final String title;
  final String value;
  const StatInfoCard({super.key, required this.title, required this.value});
  @override
  Widget build(BuildContext context) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(12),
        child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
          Text(title, style: const TextStyle(color: Colors.grey)),
          const SizedBox(height: 6),
          Text(value, style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 18)),
        ]),
      ),
    );
  }
}

class TotalCircle extends StatelessWidget {
  final String value;
  final String label;
  const TotalCircle({super.key, required this.value, required this.label});
  @override
  Widget build(BuildContext context) {
    return Container(
      width: 72,
      height: 72,
      decoration: BoxDecoration(color: Colors.orange[50], borderRadius: BorderRadius.circular(36)),
      child: Column(mainAxisAlignment: MainAxisAlignment.center, children: [
        Text(value, style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 20)),
        Text(label, style: const TextStyle(color: Colors.grey)),
      ]),
    );
  }
}

class LegendItem extends StatelessWidget {
  final Color color;
  final String label;
  final int count;
  const LegendItem({super.key, required this.color, required this.label, required this.count});
  @override
  Widget build(BuildContext context) {
    return Row(children: [
      Icon(Icons.circle, color: color, size: 10),
      const SizedBox(width: 8),
      Expanded(child: Text(label)),
      Text('$count', style: const TextStyle(fontWeight: FontWeight.bold)),
    ]);
  }
}

class DroneOperatorProfile extends StatelessWidget {
  const DroneOperatorProfile({super.key});
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(leading: IconButton(onPressed: () { Navigator.pushReplacementNamed(context, '/role'); }, icon: const Icon(Icons.arrow_back)), title: const Text('Operator Profile'), backgroundColor: Colors.white, foregroundColor: Colors.black),
      body: ListView(padding: const EdgeInsets.all(16), children: [
        ListTile(leading: const CircleAvatar(child: Text('AL')), title: const Text('Alex Lee'), subtitle: const Text('alex.lee@droneops.com')),
        const Divider(),
        ListTile(leading: const Icon(Icons.settings), title: const Text('Preferences'), onTap: () {}),
        ListTile(leading: const Icon(Icons.logout), title: const Text('Logout'), onTap: () {
          Navigator.of(context).pushNamedAndRemoveUntil('/login', (r) => false);
        }),
      ]),
    );
  }
}

class AdminDashboard extends StatelessWidget {
  const AdminDashboard({super.key});
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(leading: IconButton(onPressed: () { Navigator.pushReplacementNamed(context, '/role'); }, icon: const Icon(Icons.arrow_back)), title: const Text('Admin Dashboard'), backgroundColor: Colors.white, foregroundColor: Colors.black, elevation: 0),
      backgroundColor: Colors.grey[100],
      floatingActionButton: FloatingActionButton(onPressed: () {}, backgroundColor: const Color(0xFF00C853), child: const Icon(Icons.add, color: Colors.black)),
      body: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
          const ListTile(title: Text('Welcome, Admin'), subtitle: Text('You have 3 Active Farms')),
          const SizedBox(height: 8),
          Expanded(
            child: GridView.count(crossAxisCount: 2, crossAxisSpacing: 12, mainAxisSpacing: 12, children: [
              _adminCard(context, Icons.group, 'Manage Users', const ManageUsers()),
              _adminCard(context, Icons.agriculture, 'Manage Farms', const ManageFarmsParcels()),
              _adminCard(context, Icons.air, 'Manage Drones', const ManageSensorsDrones()),
              _adminCard(context, Icons.sensors, 'Sensor Data', const SystemHealthMonitor()),
              _adminCard(context, Icons.smart_toy, 'AI Models', const ManageAIModels()),
              _adminCard(context, Icons.task, 'Task Management', const ExportReports()),
            ]),
          )
        ]),
      ),
    );
  }
 Widget _adminCard(BuildContext ctx, IconData icon, String title, Widget page) {
  return InkWell(
    onTap: () => Navigator.pushNamed(ctx, {
      ManageUsers: '/admin/users',
      ManageFarmsParcels: '/admin/farms',
      ManageSensorsDrones: '/admin/devices',
      SystemHealthMonitor: '/admin/health',
      ManageAIModels: '/admin/ai',
      ExportReports: '/admin/tasks',
    }[page.runtimeType] ?? '/admin'),
    child: Container(
      decoration: BoxDecoration(color: Colors.white, borderRadius: BorderRadius.circular(12)),
      padding: const EdgeInsets.all(16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Icon(icon, color: const Color(0xFF00C853)),
          const SizedBox(height: 12),
          Text(title, style: const TextStyle(fontWeight: FontWeight.bold)),
          const Spacer(),
          const Text('Open', style: TextStyle(color: Colors.grey)),
        ],
      ),
    ),
  );
}
}

class ManageUsers extends StatelessWidget {
  const ManageUsers({super.key});
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Manage Users'), backgroundColor: Colors.white, foregroundColor: Colors.black),
      backgroundColor: Colors.grey[100],
      floatingActionButton: FloatingActionButton(onPressed: () {}, backgroundColor: const Color(0xFF00C853), child: const Icon(Icons.add, color: Colors.black)),
      body: ListView(padding: const EdgeInsets.all(16), children: [
        _userItem('John Doe', 'john.doe@example.com', 'Farmer', Colors.green),
        _userItem('Jane Smith', 'jane.smith@example.com', 'Admin', Colors.blueGrey),
        _userItem('Alex Lee', 'alex.lee@droneops.com', 'Drone Operator', Colors.blue),
        _userItem('Emily Carter', 'emily.c@farmwise.io', 'Agronomist', Colors.orange),
      ]),
    );
  }
  Widget _userItem(String name, String email, String role, Color color) {
    return Card(
      child: ListTile(
        leading: CircleAvatar(
          backgroundColor: color.withValues(alpha: 0.2),
          child: Text(name.split(' ').first[0] + name.split(' ').last[0]),
        ),
        title: Text(name),
        subtitle: Text(email),
        trailing: Chip(
          label: Text(role),
          backgroundColor: color.withValues(alpha: 0.15),
        ),
      ),
    );
  }
}

class ManageFarmsParcels extends StatefulWidget {
  const ManageFarmsParcels({super.key});
  @override
  State<ManageFarmsParcels> createState() => _ManageFarmsParcelsState();
}

class _ManageFarmsParcelsState extends State<ManageFarmsParcels> {
  final _repo = FarmsRepository();
  List<Farm> _farms = const [];
  bool _loading = true;
  String _query = '';

  final _ownerIdController = TextEditingController();
  final _nameController = TextEditingController();
  final _locationController = TextEditingController();

  @override
  void initState() {
    super.initState();
    _loadFarms();
  }

  Future<void> _loadFarms() async {
    setState(() {
      _loading = true;
    });
    try {
      final items = await _repo.fetchFarms();
      setState(() {
        _farms = items;
      });
    } finally {
      setState(() {
        _loading = false;
      });
    }
  }

  Future<void> _createFarmDialog() async {
    _ownerIdController.text = '';
    _nameController.text = '';
    _locationController.text = '';
    final res = await showDialog<bool>(
      context: context,
      builder: (context) {
        return AlertDialog(
          title: const Text('Create Farm'),
          content: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              TextField(controller: _ownerIdController, keyboardType: TextInputType.number, decoration: const InputDecoration(labelText: 'Owner ID')),
              TextField(controller: _nameController, decoration: const InputDecoration(labelText: 'Name')),
              TextField(controller: _locationController, decoration: const InputDecoration(labelText: 'Location')),
            ],
          ),
          actions: [
            TextButton(onPressed: () => Navigator.pop(context, false), child: const Text('Cancel')),
            ElevatedButton(onPressed: () => Navigator.pop(context, true), child: const Text('Create')),
          ],
        );
      },
    );
    if (res == true) {
      final ownerId = int.tryParse(_ownerIdController.text.trim()) ?? 0;
      final name = _nameController.text.trim();
      final location = _locationController.text.trim();
      if (ownerId <= 0 || name.isEmpty || location.isEmpty) {
        ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('Fill Owner ID, Name and Location')));
        return;
      }
      try {
        await _repo.createFarm(ownerId: ownerId, name: name, location: location);
        await _loadFarms();
        ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('Farm created')));
      } catch (e) {
        ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text('Failed to create farm: $e')));
      }
    }
  }

  Future<void> _editFarmDialog(Farm farm) async {
    _ownerIdController.text = farm.ownerId.toString();
    _nameController.text = farm.name;
    _locationController.text = farm.location;
    final res = await showDialog<bool>(
      context: context,
      builder: (context) {
        return AlertDialog(
          title: const Text('Edit Farm'),
          content: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              TextField(controller: _ownerIdController, keyboardType: TextInputType.number, decoration: const InputDecoration(labelText: 'Owner ID')),
              TextField(controller: _nameController, decoration: const InputDecoration(labelText: 'Name')),
              TextField(controller: _locationController, decoration: const InputDecoration(labelText: 'Location')),
            ],
          ),
          actions: [
            TextButton(onPressed: () => Navigator.pop(context, false), child: const Text('Cancel')),
            ElevatedButton(onPressed: () => Navigator.pop(context, true), child: const Text('Save')),
          ],
        );
      },
    );
    if (res == true) {
      final ownerId = int.tryParse(_ownerIdController.text.trim()) ?? farm.ownerId;
      final name = _nameController.text.trim();
      final location = _locationController.text.trim();
      try {
        await _repo.updateFarm(id: farm.id, ownerId: ownerId, name: name, location: location);
        await _loadFarms();
        ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('Farm updated')));
      } catch (e) {
        ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text('Failed to update farm: $e')));
      }
    }
  }

  Future<void> _deleteFarm(Farm farm) async {
    try {
      await _repo.deleteFarm(farm.id);
      await _loadFarms();
      ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('Farm deleted')));
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text('Failed to delete farm: $e')));
    }
  }

  Future<void> _createParcelDialog(Farm farm) async {
    final nameC = TextEditingController();
    final areaC = TextEditingController();
    final cropC = TextEditingController();
    final geomC = TextEditingController();
    final res = await showDialog<bool>(
      context: context,
      builder: (context) {
        return AlertDialog(
          title: const Text('Create Parcel'),
          content: SingleChildScrollView(
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                TextField(controller: nameC, decoration: const InputDecoration(labelText: 'Name')),
                TextField(controller: areaC, keyboardType: const TextInputType.numberWithOptions(decimal: true), decoration: const InputDecoration(labelText: 'Area (ha)')),
                TextField(controller: cropC, decoration: const InputDecoration(labelText: 'Crop Type')),
                TextField(controller: geomC, decoration: const InputDecoration(labelText: 'Geometry WKT')),
              ],
            ),
          ),
          actions: [
            TextButton(onPressed: () => Navigator.pop(context, false), child: const Text('Cancel')),
            ElevatedButton(onPressed: () => Navigator.pop(context, true), child: const Text('Create')),
          ],
        );
      },
    );
    if (res == true) {
      final area = double.tryParse(areaC.text.trim()) ?? 0;
      if (nameC.text.trim().isEmpty || area <= 0 || cropC.text.trim().isEmpty || geomC.text.trim().isEmpty) {
        ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('Fill name, area, crop type and geometry')));
        return;
      }
      try {
        await _repo.createParcel(
          farmId: farm.id,
          name: nameC.text.trim(),
          areaHa: area,
          cropType: cropC.text.trim(),
          geometry: geomC.text.trim(),
        );
        await _loadFarms();
        ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('Parcel created')));
      } catch (e) {
        ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text('Failed to create parcel: $e')));
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    final filtered = _farms.where((f) {
      if (_query.isEmpty) return true;
      final q = _query.toLowerCase();
      return f.name.toLowerCase().contains(q) || f.location.toLowerCase().contains(q);
    }).toList();
    return Scaffold(
      appBar: AppBar(
        leading: IconButton(onPressed: () { Navigator.pushReplacementNamed(context, '/role'); }, icon: const Icon(Icons.arrow_back)),
        title: const Text('Farms & Parcels'),
        backgroundColor: Colors.white,
        foregroundColor: Colors.black,
        actions: [
          IconButton(
            icon: const Icon(Icons.settings),
            onPressed: _changeServer,
          ),
          IconButton(
            icon: const Icon(Icons.bug_report),
            onPressed: _testFarms,
          ),
        ],
      ),
      backgroundColor: Colors.grey[100],
      floatingActionButton: FloatingActionButton(onPressed: _createFarmDialog, backgroundColor: const Color(0xFF00C853), child: const Icon(Icons.add, color: Colors.black)),
      body: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          children: [
            TextField(
              onChanged: (v) => setState(() => _query = v),
              decoration: InputDecoration(prefixIcon: const Icon(Icons.search), hintText: 'Search farms or parcels...', filled: true, fillColor: Colors.white, border: OutlineInputBorder(borderRadius: BorderRadius.circular(12), borderSide: BorderSide.none)),
            ),
            const SizedBox(height: 8),
            Row(
              children: [
                Expanded(
                  child: ValueListenableBuilder<String>(
                    valueListenable: BackendConfig.baseUrl,
                    builder: (_, value, __) => Text('API: $value', style: const TextStyle(fontSize: 12, color: Colors.grey)),
                  ),
                ),
                Expanded(
                  child: ValueListenableBuilder<String>(
                    valueListenable: BackendConfig.authBaseUrl,
                    builder: (_, value, __) => Text('Auth: $value', style: const TextStyle(fontSize: 12, color: Colors.grey)),
                  ),
                ),
              ],
            ),
            const SizedBox(height: 12),
            Expanded(
              child: _loading
                  ? const Center(child: CircularProgressIndicator())
                  : RefreshIndicator(
                      onRefresh: _loadFarms,
                      child: ListView.builder(
                        itemCount: filtered.length,
                        itemBuilder: (context, index) {
                          final farm = filtered[index];
                          return _farmCard(farm);
                        },
                      ),
                    ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _farmCard(Farm farm) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(12),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Expanded(child: Text(farm.name, style: const TextStyle(fontWeight: FontWeight.bold))),
                IconButton(onPressed: () => _editFarmDialog(farm), icon: const Icon(Icons.edit)),
                IconButton(onPressed: () => _deleteFarm(farm), icon: const Icon(Icons.delete)),
              ],
            ),
            const SizedBox(height: 4),
            Text(farm.location, style: const TextStyle(color: Colors.grey)),
            const SizedBox(height: 8),
            FutureBuilder<List<FarmParcel>>(
              future: _repo.fetchParcels(farmId: farm.id),
              builder: (context, snapshot) {
                if (snapshot.connectionState == ConnectionState.waiting) {
                  return const Padding(
                    padding: EdgeInsets.symmetric(vertical: 12),
                    child: LinearProgressIndicator(),
                  );
                }
                final parcels = snapshot.data ?? const <FarmParcel>[];
                return Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      children: [
                        Text('${parcels.length} Parcels'),
                        const Spacer(),
                        TextButton(onPressed: () => _createParcelDialog(farm), child: const Text('Add Parcel')),
                      ],
                    ),
                    for (final p in parcels)
                      ListTile(
                        leading: const Icon(Icons.eco, color: Colors.green),
                        title: Text(p.name),
                        subtitle: Text('${p.areaHa} ha • ${p.cropType}'),
                        trailing: const Icon(Icons.chevron_right),
                        onTap: () {
                          Navigator.of(context).pushNamed('/parcel', arguments: {
                            'name': p.name,
                            'area': '${p.areaHa} ha',
                            'crop': p.cropType,
                            'health': 90,
                            'irrigation': 50,
                            'tags': const ['Healthy'],
                          });
                        },
                      ),
                  ],
                );
              },
            ),
          ],
        ),
      ),
    );
  }

  Future<void> _changeServer() async {
    final apiC = TextEditingController(text: BackendConfig.baseUrl.value);
    final authC = TextEditingController(text: BackendConfig.authBaseUrl.value);
    final ok = await showDialog<bool>(
      context: context,
      builder: (_) => AlertDialog(
        title: const Text('Server URL'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            TextField(controller: apiC, decoration: const InputDecoration(labelText: 'API Base URL')),
            TextField(controller: authC, decoration: const InputDecoration(labelText: 'Auth Base URL')),
          ],
        ),
        actions: [
          TextButton(onPressed: () => Navigator.pop(context, false), child: const Text('Cancel')),
          ElevatedButton(onPressed: () => Navigator.pop(context, true), child: const Text('Save')),
        ],
      ),
    );
    if (ok == true) {
      BackendConfig.baseUrl.value = apiC.text.trim();
      BackendConfig.authBaseUrl.value = authC.text.trim();
      await _loadFarms();
    }
  }

  Future<void> _testFarms() async {
    try {
      final list = await _repo.fetchFarms();
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text('Farms OK: ${list.length}')));
    } catch (e) {
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text('Farms error: $e')));
    }
  }
}

class ApplicationsAdmin extends StatefulWidget {
  const ApplicationsAdmin({super.key});
  @override
  State<ApplicationsAdmin> createState() => _ApplicationsAdminState();
}

class _ApplicationsAdminState extends State<ApplicationsAdmin> {
  final _repo = ApplicationsRepository();
  List<Application> _apps = const [];
  bool _loading = true;
  final _applicantController = TextEditingController();
  ApplicationStatus? _status;

  final _titleC = TextEditingController();
  final _typeC = TextEditingController();
  final _detailsC = TextEditingController();
  final _parcelIdC = TextEditingController();
  final _applicantIdC = TextEditingController();

  @override
  void initState() {
    super.initState();
    _load();
  }

  Future<void> _load() async {
    setState(() => _loading = true);
    final applicantId = int.tryParse(_applicantController.text.trim());
    final apps = await _repo.fetchApplications(applicantId: applicantId, status: _status);
    setState(() {
      _apps = apps;
      _loading = false;
    });
  }

  Future<void> _createDialog() async {
    _titleC.clear();
    _typeC.clear();
    _detailsC.clear();
    _parcelIdC.clear();
    _applicantIdC.clear();
    final ok = await showDialog<bool>(
      context: context,
      builder: (context) {
        return AlertDialog(
          title: const Text('Create Application'),
          content: SingleChildScrollView(
            child: Column(mainAxisSize: MainAxisSize.min, children: [
              TextField(controller: _applicantIdC, keyboardType: TextInputType.number, decoration: const InputDecoration(labelText: 'Applicant ID')),
              TextField(controller: _parcelIdC, keyboardType: TextInputType.number, decoration: const InputDecoration(labelText: 'Parcel ID')),
              TextField(controller: _titleC, decoration: const InputDecoration(labelText: 'Title')),
              TextField(controller: _typeC, decoration: const InputDecoration(labelText: 'Type')),
              TextField(controller: _detailsC, decoration: const InputDecoration(labelText: 'Details')),
            ]),
          ),
          actions: [
            TextButton(onPressed: () => Navigator.pop(context, false), child: const Text('Cancel')),
            ElevatedButton(onPressed: () => Navigator.pop(context, true), child: const Text('Create')),
          ],
        );
      },
    );
    if (ok == true) {
      final applicantId = int.tryParse(_applicantIdC.text.trim());
      final parcelId = int.tryParse(_parcelIdC.text.trim());
      if (applicantId != null && parcelId != null && _titleC.text.isNotEmpty && _typeC.text.isNotEmpty) {
        await _repo.createApplication(
          applicantId: applicantId,
          parcelId: parcelId,
          title: _titleC.text.trim(),
          type: _typeC.text.trim(),
          details: _detailsC.text.trim(),
        );
        await _load();
      }
    }
  }

  Future<void> _changeStatus(Application app) async {
    final statuses = ApplicationStatus.values;
    ApplicationStatus selected = app.status;
    final reasonC = TextEditingController(text: app.statusReason ?? '');
    final ok = await showDialog<bool>(
      context: context,
      builder: (context) {
        return AlertDialog(
          title: const Text('Change Status'),
          content: Column(mainAxisSize: MainAxisSize.min, children: [
            DropdownButton<ApplicationStatus>(
              value: selected,
              items: [
                for (final s in statuses)
                  DropdownMenuItem(value: s, child: Text(applicationStatusToString(s))),
              ],
              onChanged: (v) {
                if (v != null) selected = v;
              },
            ),
            TextField(controller: reasonC, decoration: const InputDecoration(labelText: 'Reason')),
          ]),
          actions: [
            TextButton(onPressed: () => Navigator.pop(context, false), child: const Text('Cancel')),
            ElevatedButton(onPressed: () => Navigator.pop(context, true), child: const Text('Save')),
          ],
        );
      },
    );
    if (ok == true) {
      await _repo.changeStatus(id: app.id, status: selected, statusReason: reasonC.text.trim().isEmpty ? null : reasonC.text.trim());
      await _load();
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Applications'), backgroundColor: Colors.white, foregroundColor: Colors.black),
      backgroundColor: Colors.grey[100],
      floatingActionButton: FloatingActionButton(onPressed: _createDialog, backgroundColor: const Color(0xFF00C853), child: const Icon(Icons.add, color: Colors.black)),
      body: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          children: [
            Row(children: [
              Expanded(
                child: TextField(
                  controller: _applicantController,
                  keyboardType: TextInputType.number,
                  decoration: InputDecoration(prefixIcon: const Icon(Icons.person), hintText: 'Applicant ID', filled: true, fillColor: Colors.white, border: OutlineInputBorder(borderRadius: BorderRadius.circular(12), borderSide: BorderSide.none)),
                  onSubmitted: (_) => _load(),
                ),
              ),
              const SizedBox(width: 12),
              DropdownButton<ApplicationStatus?>(
                value: _status,
                hint: const Text('Status'),
                items: [
                  const DropdownMenuItem(value: null, child: Text('Any')),
                  for (final s in ApplicationStatus.values)
                    DropdownMenuItem(value: s, child: Text(applicationStatusToString(s))),
                ],
                onChanged: (v) => setState(() => _status = v),
              ),
              const SizedBox(width: 12),
              ElevatedButton(onPressed: _load, child: const Text('Filter')),
            ]),
            const SizedBox(height: 12),
            Expanded(
              child: _loading
                  ? const Center(child: CircularProgressIndicator())
                  : RefreshIndicator(
                      onRefresh: _load,
                      child: ListView.separated(
                        itemCount: _apps.length,
                        separatorBuilder: (_, __) => const SizedBox(height: 8),
                        itemBuilder: (context, index) {
                          final a = _apps[index];
                          return Card(
                            child: ListTile(
                              leading: const Icon(Icons.assignment),
                              title: Text(a.title),
                              subtitle: Text('${a.type} • Parcel ${a.parcelId} • ${applicationStatusToString(a.status)}'),
                              trailing: IconButton(onPressed: () => _changeStatus(a), icon: const Icon(Icons.edit)),
                            ),
                          );
                        },
                      ),
                    ),
            ),
          ],
        ),
      ),
    );
  }
}

class ManageSensorsDrones extends StatefulWidget {
  const ManageSensorsDrones({super.key});
  @override
  State<ManageSensorsDrones> createState() => _ManageSensorsDronesState();
}

class _ManageSensorsDronesState extends State<ManageSensorsDrones> {
  int tab = 0; // 0=Drones, 1=Sensors
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.grey[100],
      appBar: AppBar(
        title: const Text('Manage Devices'),
        backgroundColor: Colors.white,
        foregroundColor: Colors.black,
        elevation: 0,
        actions: [IconButton(onPressed: () {}, icon: const Icon(Icons.add))],
      ),
      bottomNavigationBar: BottomNavigationBar(
        type: BottomNavigationBarType.fixed,
        currentIndex: 0,
        onTap: (_) {},
        items: const [
          BottomNavigationBarItem(icon: Icon(Icons.devices), label: 'Devices'),
          BottomNavigationBarItem(icon: Icon(Icons.map), label: 'Map'),
          BottomNavigationBarItem(icon: Icon(Icons.home), label: 'Home'),
          BottomNavigationBarItem(icon: Icon(Icons.notifications), label: 'Alerts'),
          BottomNavigationBarItem(icon: Icon(Icons.settings), label: 'Settings'),
        ],
      ),
      floatingActionButton: FloatingActionButton(onPressed: () {}, backgroundColor: const Color(0xFF00C853), child: const Icon(Icons.add, color: Colors.black)),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          Container(
            height: 44,
            decoration: BoxDecoration(color: Colors.white, borderRadius: BorderRadius.circular(12)),
            child: Row(children: [
              _segButton('Drones', 0),
              _segButton('Sensors', 1),
            ]),
          ),
          const SizedBox(height: 12),
          TextField(
            decoration: InputDecoration(
              prefixIcon: const Icon(Icons.search),
              hintText: 'Search by name or ID',
              filled: true,
              fillColor: Colors.white,
              border: OutlineInputBorder(borderRadius: BorderRadius.circular(12), borderSide: BorderSide.none),
            ),
          ),
          const SizedBox(height: 12),
          if (tab == 0) ...[
            _deviceCard(
              icon: Icons.airplanemode_active,
              title: 'Agri-Drone 1',
              subtitle: 'DJI Agras T20',
              status: const _StatusBadge(text: 'Active', color: Colors.green),
              signal: 'Strong',
              battery: 85,
            ),
            _deviceCard(
              icon: Icons.airplanemode_active,
              title: 'Sky-Sprayer X',
              subtitle: 'DJI Agras T30',
              status: const _StatusBadge(text: 'In-Flight', color: Colors.blue),
              signal: 'Good',
              battery: 60,
            ),
            _deviceCard(
              icon: Icons.agriculture,
              title: 'Harvester-Bot 3',
              subtitle: 'Custom Build v2.1',
              status: const _StatusBadge(text: 'Offline', color: Colors.red),
              signal: 'No Signal',
              battery: 15,
            ),
          ] else ...[
            _deviceCard(
              icon: Icons.sensors,
              title: 'Soil Moisture A1',
              subtitle: 'Field A • v1.0',
              status: const _StatusBadge(text: 'Online', color: Colors.green),
              signal: 'Strong',
              battery: 100,
            ),
            _deviceCard(
              icon: Icons.sensors,
              title: 'Weather Station W7',
              subtitle: 'North • v2.3',
              status: const _StatusBadge(text: 'Maintenance', color: Colors.orange),
              signal: 'Good',
              battery: 70,
            ),
          ],
        ],
      ),
    );
  }

  Widget _segButton(String label, int index) {
    final selected = tab == index;
    return Expanded(
      child: InkWell(
        onTap: () => setState(() => tab = index),
        child: Container(
          decoration: BoxDecoration(
            color: selected ? const Color(0xFF00C853) : Colors.transparent,
            borderRadius: BorderRadius.circular(12),
          ),
          alignment: Alignment.center,
          child: Text(label, style: TextStyle(color: selected ? Colors.black : Colors.grey[700], fontWeight: FontWeight.bold)),
        ),
      ),
    );
  }

  Widget _deviceCard({required IconData icon, required String title, required String subtitle, required Widget status, required String signal, required int battery}) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(12),
        child: Row(children: [
          Container(width: 42, height: 42, decoration: BoxDecoration(color: Colors.grey[200], borderRadius: BorderRadius.circular(8)), child: Icon(icon)),
          const SizedBox(width: 12),
          Expanded(
            child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
              Row(children: [Expanded(child: Text(title, style: const TextStyle(fontWeight: FontWeight.bold))), const Icon(Icons.chevron_right, color: Colors.grey)]),
              Text(subtitle, style: const TextStyle(color: Colors.grey)),
              const SizedBox(height: 8),
              Row(children: [status, const SizedBox(width: 12), const Icon(Icons.wifi, size: 16, color: Colors.grey), Text(' $signal', style: const TextStyle(color: Colors.grey)), const Spacer(), const Icon(Icons.battery_full, size: 16, color: Colors.grey), Text(' $battery%', style: const TextStyle(color: Colors.grey))]),
            ]),
          ),
        ]),
      ),
    );
  }
}

class ManageAIModels extends StatefulWidget {
  const ManageAIModels({super.key});
  @override
  State<ManageAIModels> createState() => _ManageAIModelsState();
}

class _ManageAIModelsState extends State<ManageAIModels> {
  int filter = 0; // 0 All, 1 Deployed, 2 In Training, 3 Archived
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Manage AI Models'), backgroundColor: Colors.white, foregroundColor: Colors.black),
      backgroundColor: Colors.grey[100],
      body: ListView(padding: const EdgeInsets.all(16), children: [
        TextField(
          decoration: InputDecoration(prefixIcon: const Icon(Icons.search), hintText: 'Search models by name...', filled: true, fillColor: Colors.white, border: OutlineInputBorder(borderRadius: BorderRadius.circular(12), borderSide: BorderSide.none)),
        ),
        const SizedBox(height: 12),
        Row(children: [
          _filterChip('All', 0), const SizedBox(width: 8), _filterChip('Deployed', 1), const SizedBox(width: 8), _filterChip('In Training', 2), const SizedBox(width: 8), _filterChip('Archived', 3),
        ]),
        const SizedBox(height: 12),
        _modelCard(
          title: 'Pest Detection Model',
          status: const _StatusBadge(text: 'Active', color: Colors.green),
          version: 'v2.1',
          accuracy: '98.2%',
          trained: '2023-10-25',
          action: ElevatedButton(onPressed: () {}, style: ElevatedButton.styleFrom(backgroundColor: const Color(0xFF00C853)), child: const Text('Deploy', style: TextStyle(color: Colors.black))),
          barColor: Colors.green,
        ),
        _modelCard(
          title: 'Yield Prediction',
          status: const _StatusBadge(text: 'New Version Available', color: Colors.blue),
          version: 'v1.5',
          accuracy: '95.7%',
          trained: '2023-09-15',
          action: OutlinedButton(onPressed: () {}, child: const Text('Update')),
          barColor: Colors.blue,
        ),
        _modelCard(
          title: 'Crop Disease Identifier',
          status: const _StatusBadge(text: 'In Training', color: Colors.orange),
          version: 'v3.0',
          accuracy: 'Progress 75%',
          trained: 'Started: 2023-11-01',
          action: TextButton(onPressed: () {}, child: const Text('View')),
          barColor: Colors.orange,
        ),
        _modelCard(
          title: 'Soil Moisture Prediction',
          status: const _StatusBadge(text: 'Archived', color: Colors.grey),
          version: 'v1.0',
          accuracy: '92.1%',
          trained: 'Archived: 2023-08-20',
          action: OutlinedButton(onPressed: () {}, child: const Text('Restore')),
          barColor: Colors.grey,
        ),
      ]),
    );
  }

  Widget _filterChip(String label, int idx) {
    final selected = filter == idx;
    return ChoiceChip(
      label: Text(label),
      selected: selected,
      onSelected: (_) => setState(() => filter = idx),
      selectedColor: const Color(0xFF00C853),
      labelStyle: TextStyle(color: selected ? Colors.black : Colors.black),
    );
  }

  Widget _modelCard({required String title, required Widget status, required String version, required String accuracy, required String trained, required Widget action, required Color barColor}) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(12),
        child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
          Row(children: [Expanded(child: Text(title, style: const TextStyle(fontWeight: FontWeight.bold))), const Icon(Icons.more_vert)]),
          const SizedBox(height: 6),
          status,
          const SizedBox(height: 8),
          LinearProgressIndicator(value: 0.9, minHeight: 4, color: barColor, backgroundColor: Colors.grey[300]),
          const SizedBox(height: 8),
          Text('$version • Accuracy: $accuracy'),
          Text('Last Trained: $trained', style: const TextStyle(color: Colors.grey)),
          const SizedBox(height: 8),
          Align(alignment: Alignment.centerRight, child: action),
        ]),
      ),
    );
  }
}

class SystemHealthMonitor extends StatefulWidget {
  const SystemHealthMonitor({super.key});
  @override
  State<SystemHealthMonitor> createState() => _SystemHealthMonitorState();
}

class _SystemHealthMonitorState extends State<SystemHealthMonitor> {
  int range = 1; // 0=24H 1=7D 2=30D
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('System Health Monitor'), backgroundColor: Colors.white, foregroundColor: Colors.black, actions: [IconButton(onPressed: () {}, icon: const Icon(Icons.refresh))]),
      backgroundColor: Colors.grey[100],
      body: ListView(padding: const EdgeInsets.all(16), children: [
        Row(children: [
          _rangeChip('24H', 0), const SizedBox(width: 8), _rangeChip('7D', 1), const SizedBox(width: 8), _rangeChip('30D', 2),
        ]),
        const SizedBox(height: 12),
        Row(children: const [
          Expanded(child: _InfoTile(title: 'Uptime', value: '99.9%')),
          SizedBox(width: 12),
          Expanded(child: _InfoTile(title: 'Avg Latency', value: '50ms')),
        ]),
        const SizedBox(height: 12),
        const _InfoTile(title: 'New Errors', value: '2'),
        const SizedBox(height: 16),
        Card(child: Padding(padding: const EdgeInsets.all(16), child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [const Text('Uptime'), const SizedBox(height: 8), Text('All Systems Operational • Last 7 Days +0.1%'), const SizedBox(height: 8), SizedBox(height: 120, child: Sparkline(data: [93, 94, 94, 95, 95, 96, 95]))]))),
        const SizedBox(height: 12),
        Card(child: Padding(padding: const EdgeInsets.all(16), child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [const Text('API Latency'), const SizedBox(height: 8), Text('Normal • Last 7 Days -5ms'), const SizedBox(height: 8), SizedBox(height: 120, child: Sparkline(data: [45, 60, 55, 52, 48, 50, 46]))]))),
        const SizedBox(height: 12),
        Card(child: Padding(padding: const EdgeInsets.all(16), child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [const Text('System Errors'), const SizedBox(height: 8), Text('2 New Errors • Last 7 Days +2%'), const SizedBox(height: 8), SizedBox(height: 120, child: Sparkline(data: [1, 0, 2, 1, 3, 1, 2]))]))),
      ]),
    );
  }

  Widget _rangeChip(String label, int idx) {
    final selected = range == idx;
    return Expanded(
      child: ChoiceChip(
        label: Text(label),
        selected: selected,
        onSelected: (_) => setState(() => range = idx),
        selectedColor: const Color(0xFF00C853),
        labelStyle: TextStyle(color: selected ? Colors.black : Colors.black),
      ),
    );
  }
}

class LiveAnnotations extends StatelessWidget {
  const LiveAnnotations({super.key});
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Row(children: const [BackButton(), Text('Live Annotation'), Spacer(), Icon(Icons.circle, color: Colors.red, size: 10), SizedBox(width: 6), Text('Live')]), backgroundColor: Colors.white, foregroundColor: Colors.black),
      body: Stack(children: [
        Positioned.fill(child: SafeNetworkImage(url: 'https://images.pexels.com/photos/225258/pexels-photo-225258.jpeg', fit: BoxFit.cover)),
        Positioned(left: 40, top: 80, child: _annotBox('Blight')),
        Positioned(left: 200, top: 160, child: _annotBox('Rust')),
        Positioned.fill(child: Align(
          alignment: Alignment.bottomCenter,
          child: Container(
            padding: const EdgeInsets.all(16),
            decoration: const BoxDecoration(color: Color(0xCC0D2818), borderRadius: BorderRadius.only(topLeft: Radius.circular(16), topRight: Radius.circular(16))),
            child: Column(mainAxisSize: MainAxisSize.min, children: [
              Row(children: const [
                Expanded(child: _InfoTile(title: 'Threat Level', value: 'Medium')),
                SizedBox(width: 12),
                Expanded(child: _InfoTile(title: 'Affected Area', value: '15%')),
              ]),
              const SizedBox(height: 8),
              const _InfoTile(title: 'Detections', value: '82'),
              const SizedBox(height: 12),
              Row(children: [
                ElevatedButton(onPressed: () {}, style: ElevatedButton.styleFrom(backgroundColor: const Color(0xFF00C853), shape: const CircleBorder(), padding: const EdgeInsets.all(12)), child: const Icon(Icons.pause, color: Colors.black)),
                const SizedBox(width: 12),
                OutlinedButton(onPressed: () {}, child: const Icon(Icons.camera_alt)),
                const SizedBox(width: 12),
                OutlinedButton(onPressed: () {}, child: const Icon(Icons.save_alt)),
                const Spacer(),
                ClipRRect(borderRadius: BorderRadius.circular(8), child: SafeNetworkImage(url: 'https://images.pexels.com/photos/219692/pexels-photo-219692.jpeg', width: 90, height: 60, fit: BoxFit.cover)),
              ]),
              const SizedBox(height: 8),
              Align(alignment: Alignment.centerLeft, child: Container(padding: const EdgeInsets.all(8), decoration: BoxDecoration(color: Colors.green[900], borderRadius: BorderRadius.circular(8)), child: const Text('Blight Detected', style: TextStyle(color: Colors.white))))
            ]),
          ),
        )),
      ]),
    );
  }

  Widget _annotBox(String label) {
    return Stack(children: [
      Container(width: 140, height: 80, decoration: BoxDecoration(border: Border.all(color: Colors.green, width: 2), borderRadius: BorderRadius.circular(6))),
      Positioned(top: -22, left: 8, child: Container(padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4), decoration: BoxDecoration(color: Colors.green, borderRadius: BorderRadius.circular(12)), child: Text(label, style: const TextStyle(color: Colors.white))))
    ]);
  }
}

class ExportReports extends StatefulWidget {
  const ExportReports({super.key});
  @override
  State<ExportReports> createState() => _ExportReportsState();
}

class _ExportReportsState extends State<ExportReports> {
  int format = 0; // 0 PDF, 1 CSV
  bool includeCharts = true;
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Export Reports'), backgroundColor: Colors.white, foregroundColor: Colors.black),
      backgroundColor: Colors.grey[100],
      body: ListView(padding: const EdgeInsets.all(16), children: [
        Container(
          height: 44,
          decoration: BoxDecoration(color: Colors.white, borderRadius: BorderRadius.circular(12)),
          child: Row(children: [
            _formatButton('PDF', 0),
            _formatButton('CSV', 1),
          ]),
        ),
        const SizedBox(height: 16),
        Card(child: ListTile(leading: const Icon(Icons.date_range), title: const Text('Date Range'), subtitle: const Text('Select Date Range'), trailing: IconButton(onPressed: () {}, icon: const Icon(Icons.edit))))
        ,
        const SizedBox(height: 8),
        Card(child: ListTile(leading: const Icon(Icons.description), title: const Text('Report Type'), subtitle: const Text('Select report type'), trailing: IconButton(onPressed: () {}, icon: const Icon(Icons.edit))))
        ,
        const SizedBox(height: 8),
        Card(child: ListTile(leading: const Icon(Icons.agriculture), title: const Text('Farms / Fields'), subtitle: const Text('All Farms selected'), trailing: IconButton(onPressed: () {}, icon: const Icon(Icons.edit))))
        ,
        const SizedBox(height: 16),
        SwitchListTile(value: includeCharts, onChanged: (v) => setState(() => includeCharts = v), title: const Text('Include charts & graphs')),
        const SizedBox(height: 16),
        SizedBox(width: double.infinity, height: 52, child: ElevatedButton(onPressed: () async {
          final messenger = ScaffoldMessenger.of(context);
          final ts = DateTime.now().millisecondsSinceEpoch;
          if (format == 0) {
            final doc = pw.Document();
            doc.addPage(pw.Page(build: (ctx) => pw.Column(children: [pw.Text('Farm Report'), pw.Text('Include charts: ${includeCharts ? 'Yes' : 'No'}')])));
            final bytes = await doc.save();
            final savedPath = await saveBytes('report_$ts.pdf', bytes, 'application/pdf');
            if (!mounted) return;
            messenger.showSnackBar(SnackBar(content: Text('Saved: $savedPath')));
          } else {
            final csv = 'metric,value\\narea,120 ha\\nparcels,8\\ninclude_charts,${includeCharts ? 'yes' : 'no'}\\n';
            final savedPath = await saveBytes('report_$ts.csv', utf8.encode(csv), 'text/csv');
            if (!mounted) return;
            messenger.showSnackBar(SnackBar(content: Text('Saved: $savedPath')));
          }
        }, style: ElevatedButton.styleFrom(backgroundColor: const Color(0xFF00C853)), child: const Text('Generate & Export Report', style: TextStyle(color: Colors.black))))
      ]),
    );
  }

  Widget _formatButton(String label, int idx) {
    final selected = format == idx;
    return Expanded(
      child: InkWell(
        onTap: () => setState(() => format = idx),
        child: Container(alignment: Alignment.center, decoration: BoxDecoration(color: selected ? const Color(0xFF00C853) : Colors.transparent, borderRadius: BorderRadius.circular(12)), child: Text(label, style: TextStyle(color: selected ? Colors.black : Colors.grey[700], fontWeight: FontWeight.bold))),
      ),
    );
  }
}

class ParcelDetail extends StatelessWidget {
  final String name;
  final String area;
  final String crop;
  final int health;
  final int irrigation;
  final List<String> tags;

  const ParcelDetail({
    super.key,
    required this.name,
    required this.area,
    required this.crop,
    required this.health,
    required this.irrigation,
    required this.tags,
  });

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('$name Details'),
        backgroundColor: Colors.white,
        foregroundColor: Colors.black,
        elevation: 0,
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Container(
              height: 140,
              decoration: BoxDecoration(
                color: Colors.green[100],
                borderRadius: BorderRadius.circular(12),
              ),
              child: const Center(child: Text('Map View')),
            ),
            const SizedBox(height: 16),
            Container(
              padding: const EdgeInsets.all(16),
              decoration: BoxDecoration(
                color: Colors.white,
                borderRadius: BorderRadius.circular(12),
              ),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(name, style: const TextStyle(fontSize: 22, fontWeight: FontWeight.bold)),
                  Text('$crop • $area', style: TextStyle(color: Colors.grey[600])),
                  const SizedBox(height: 12),
                  Wrap(
                    spacing: 8,
                    children: tags
                        .map((t) => Chip(label: Text(t), backgroundColor: _tagColor(t)))
                        .toList(),
                  ),
                ],
              ),
            ),
            const SizedBox(height: 16),
            Row(
              children: [
                Expanded(
                  child: Container(
                    padding: const EdgeInsets.all(16),
                    decoration: BoxDecoration(color: Colors.white, borderRadius: BorderRadius.circular(12)),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        const Text('Health Status'),
                        const SizedBox(height: 12),
                        PercentGauge(percent: health, color: const Color(0xFF00C853)),
                      ],
                    ),
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: Container(
                    padding: const EdgeInsets.all(16),
                    decoration: BoxDecoration(color: Colors.white, borderRadius: BorderRadius.circular(12)),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        const Text('Irrigation'),
                        const SizedBox(height: 12),
                        PercentGauge(percent: irrigation, color: Colors.blue),
                      ],
                    ),
                  ),
                ),
              ],
            ),
            const SizedBox(height: 16),
            Container(
              padding: const EdgeInsets.all(16),
              decoration: BoxDecoration(color: Colors.white, borderRadius: BorderRadius.circular(12)),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const Text('Disease History (Last 30 Days)'),
                  const SizedBox(height: 12),
                  SizedBox(height: 120, child: Sparkline(data: [5, 20, 14, 18, 12, 16, 10, 22])),
                ],
              ),
            ),
            const SizedBox(height: 16),
            Card(child: ListTile(leading: const Icon(Icons.calendar_today), title: const Text('Last Scanned'), subtitle: const Text('September 28, 2023'))),
            Card(child: ListTile(leading: const Icon(Icons.science), title: const Text('Nutrient Levels'), subtitle: const Text('Nitrogen: Optimal, Phosphorus: Low'))),
            Card(child: ListTile(leading: const Icon(Icons.lightbulb_outline), title: const Text('Recommended Actions'), subtitle: const Text('Schedule irrigation. Monitor for pests.'))),
            const SizedBox(height: 16),
            SizedBox(width: double.infinity, height: 52, child: ElevatedButton(onPressed: () {}, style: ElevatedButton.styleFrom(backgroundColor: const Color(0xFF00C853), shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12))), child: const Text('Request Drone Scan', style: TextStyle(color: Colors.black, fontWeight: FontWeight.bold))))
          ],
        ),
      ),
    );
  }

  Color _tagColor(String t) {
    if (t.contains('Healthy')) return Colors.green[100]!;
    if (t.contains('Water')) return Colors.orange[100]!;
    return Colors.red[100]!;
  }
}

class Sensors extends StatefulWidget {
  const Sensors({super.key});

  @override
  State<Sensors> createState() => _SensorsState();
}

class _SensorsState extends State<Sensors> {
  int selectedRange = 0;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFF0D2818),
      appBar: AppBar(
        title: const Text('Sensor & Weather Data'),
        backgroundColor: const Color(0xFF0D2818),
        foregroundColor: Colors.white,
        elevation: 0,
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16),
        child: Column(
          children: [
            Row(
              children: [
                _rangeChip('24H', 0),
                const SizedBox(width: 8),
                _rangeChip('7D', 1),
                const SizedBox(width: 8),
                _rangeChip('30D', 2),
              ],
            ),
            const SizedBox(height: 16),
            Row(
              children: const [
                Expanded(child: _SensorStat(title: 'Temperature', value: '24°C')),
                SizedBox(width: 12),
                Expanded(child: _SensorStat(title: 'Humidity', value: '65%')),
              ],
            ),
            const SizedBox(height: 12),
            const _SensorStat(title: 'Soil Moisture', value: '45%'),
            const SizedBox(height: 16),
            _chartCard('Air Temperature', [24, 25, 23, 26, 24, 27, 25], '+2.1%'),
            const SizedBox(height: 12),
            _chartCard('Relative Humidity', [65, 66, 64, 63, 67, 66, 65], '-1.5%'),
            const SizedBox(height: 12),
            _chartCard('Soil Moisture', [45, 44, 46, 45, 47, 46, 45], '+0.5%'),
          ],
        ),
      ),
    );
  }

  Widget _rangeChip(String label, int index) {
    final selected = selectedRange == index;
    return Expanded(
      child: GestureDetector(
        onTap: () => setState(() => selectedRange = index),
        child: Container(
          height: 40,
          decoration: BoxDecoration(
            color: selected ? const Color(0xFF00C853) : const Color(0xFF1a3a2a),
            borderRadius: BorderRadius.circular(8),
          ),
          alignment: Alignment.center,
          child: Text(label, style: TextStyle(color: selected ? Colors.black : Colors.white)),
        ),
      ),
    );
  }

  Widget _chartCard(String title, List<double> data, String delta) {
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(color: const Color(0xFF12281C), borderRadius: BorderRadius.circular(12)),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(title, style: const TextStyle(color: Colors.white, fontSize: 18)),
          const SizedBox(height: 4),
          Text('Last 24 Hours $delta', style: const TextStyle(color: Colors.greenAccent)),
          const SizedBox(height: 12),
          SizedBox(height: 120, child: Sparkline(data: data)),
        ],
      ),
    );
  }
}

class _SensorStat extends StatelessWidget {
  final String title;
  final String value;
  const _SensorStat({required this.title, required this.value});
  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(color: const Color(0xFF12281C), borderRadius: BorderRadius.circular(12)),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(title, style: const TextStyle(color: Colors.white70)),
          const SizedBox(height: 8),
          Text(value, style: const TextStyle(color: Colors.white, fontSize: 22, fontWeight: FontWeight.bold)),
        ],
      ),
    );
  }
}



// moved to irrigation/recommendation.dart

// moved to disease/detection.dart

class _InfoTile extends StatelessWidget {
  final String title;
  final String value;
  const _InfoTile({required this.title, required this.value});
  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(color: const Color(0xFF12281C), borderRadius: BorderRadius.circular(12)),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(title, style: const TextStyle(color: Colors.white54)),
          const SizedBox(height: 6),
          Text(value, style: const TextStyle(color: Colors.white, fontWeight: FontWeight.bold)),
        ],
      ),
    );
  }
}

class _StatusBadge extends StatelessWidget {
  final String text;
  final Color color;
  const _StatusBadge({required this.text, required this.color});
  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
      decoration: BoxDecoration(color: color.withValues(alpha: 0.12), borderRadius: BorderRadius.circular(12), border: Border.all(color: color)),
      child: Row(mainAxisSize: MainAxisSize.min, children: [
        Icon(Icons.circle, color: color, size: 8),
        const SizedBox(width: 6),
        Text(text, style: TextStyle(color: color)),
      ]),
    );
  }
}

class DeviceCameraView extends StatefulWidget {
  const DeviceCameraView({super.key});
  @override
  State<DeviceCameraView> createState() => _DeviceCameraViewState();
}

class _DeviceCameraViewState extends State<DeviceCameraView> {
  CameraController? _controller;
  bool _initializing = true;

  @override
  void initState() {
    super.initState();
    _initCamera();
  }

  Future<void> _initCamera() async {
    try {
      final cams = await availableCameras();
      final cam = cams.firstWhere(
        (c) => c.lensDirection == CameraLensDirection.back,
        orElse: () => cams.isNotEmpty ? cams.first : throw StateError('No cameras available'),
      );
      final controller = CameraController(cam, ResolutionPreset.medium, enableAudio: false);
      await controller.initialize();
      if (!mounted) return;
      setState(() {
        _controller = controller;
        _initializing = false;
      });
    } catch (e) {
      if (!mounted) return;
      setState(() {
        _initializing = false;
      });
    }
  }

  @override
  void dispose() {
    _controller?.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Camera'), backgroundColor: Colors.white, foregroundColor: Colors.black, actions: [
        IconButton(onPressed: () { Navigator.of(context).push(MaterialPageRoute(builder: (_) => const PhotoGallery())); }, icon: const Icon(Icons.photo_library))
      ]),
      backgroundColor: Colors.black,
      body: Center(
        child: _initializing
            ? const CircularProgressIndicator()
            : (_controller == null || !_controller!.value.isInitialized)
                ? const Text('Camera unavailable', style: TextStyle(color: Colors.white))
                : AspectRatio(aspectRatio: _controller!.value.previewSize != null ? _controller!.value.previewSize!.width / _controller!.value.previewSize!.height : 3/4, child: CameraPreview(_controller!)),
      ),
      floatingActionButton: (_controller != null && _controller!.value.isInitialized)
          ? FloatingActionButton(
              onPressed: () async {
                try {
                  final messenger = ScaffoldMessenger.of(context);
                  final x = await _controller!.takePicture();
                  final bytes = await x.readAsBytes();
                  final id = DateTime.now().millisecondsSinceEpoch.toString();
                  await LocalStore.savePhoto('photo_$id', bytes);
                  if (!mounted) return;
                  messenger.showSnackBar(const SnackBar(content: Text('Photo saved')));
                } catch (_) {}
              },
              backgroundColor: const Color(0xFF00C853),
              child: const Icon(Icons.camera_alt, color: Colors.black),
            )
          : null,
    );
  }
}

class PhotoGallery extends StatelessWidget {
  const PhotoGallery({super.key});
  @override
  Widget build(BuildContext context) {
    final ids = LocalStore.photoIds();
    return Scaffold(
      appBar: AppBar(title: const Text('Gallery'), backgroundColor: Colors.white, foregroundColor: Colors.black),
      body: ids.isEmpty
          ? const Center(child: Text('No photos'))
          : GridView.builder(
              padding: const EdgeInsets.all(8),
              gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(crossAxisCount: 3, crossAxisSpacing: 4, mainAxisSpacing: 4),
              itemCount: ids.length,
              itemBuilder: (context, index) {
                final b = LocalStore.photoBytes(ids[index]);
                if (b == null) return const SizedBox.shrink();
                return Image.memory(b, fit: BoxFit.cover);
              },
            ),
    );
  }
}

class PercentGauge extends StatelessWidget {
  final int percent;
  final Color color;
  const PercentGauge({super.key, required this.percent, required this.color});
  @override
  Widget build(BuildContext context) {
    return SizedBox(
      height: 100,
      width: 100,
      child: Stack(
        alignment: Alignment.center,
        children: [
          CircularProgressIndicator(value: percent / 100, backgroundColor: Colors.grey[200], valueColor: AlwaysStoppedAnimation(color)),
          Text('$percent%'),
        ],
      ),
    );
  }
}

class Sparkline extends StatelessWidget {
  final List<double> data;
  const Sparkline({super.key, required this.data});
  @override
  Widget build(BuildContext context) {
    return CustomPaint(
      painter: _SparkPainter(data),
      child: Container(),
    );
  }
}

class _SparkPainter extends CustomPainter {
  final List<double> data;
  _SparkPainter(this.data);
  @override
  void paint(Canvas canvas, Size size) {
    final path = Path();
    final paint = Paint()
      ..color = const Color(0xFF00C853)
      ..style = PaintingStyle.stroke
      ..strokeWidth = 2;
    final dx = size.width / (data.length - 1);
    final max = data.reduce((a, b) => a > b ? a : b);
    final min = data.reduce((a, b) => a < b ? a : b);
    for (int i = 0; i < data.length; i++) {
      final x = i * dx;
      final norm = (data[i] - min) / ((max - min) == 0 ? 1 : (max - min));
      final y = size.height - norm * size.height;
      if (i == 0) {
        path.moveTo(x, y);
      } else {
        path.lineTo(x, y);
      }
    }
    canvas.drawPath(path, paint);
  }
  @override
  bool shouldRepaint(covariant CustomPainter oldDelegate) => true;
}
