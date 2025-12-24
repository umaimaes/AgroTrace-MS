enum ApplicationStatus {
  pending,
  inReview,
  approved,
  rejected,
  inProgress,
  completed,
  cancelled,
}

ApplicationStatus applicationStatusFromString(String value) {
  switch (value) {
    case 'PENDING':
      return ApplicationStatus.pending;
    case 'IN_REVIEW':
      return ApplicationStatus.inReview;
    case 'APPROVED':
      return ApplicationStatus.approved;
    case 'REJECTED':
      return ApplicationStatus.rejected;
    case 'IN_PROGRESS':
      return ApplicationStatus.inProgress;
    case 'COMPLETED':
      return ApplicationStatus.completed;
    case 'CANCELLED':
      return ApplicationStatus.cancelled;
    default:
      return ApplicationStatus.pending;
  }
}

String applicationStatusToString(ApplicationStatus status) {
  switch (status) {
    case ApplicationStatus.pending:
      return 'PENDING';
    case ApplicationStatus.inReview:
      return 'IN_REVIEW';
    case ApplicationStatus.approved:
      return 'APPROVED';
    case ApplicationStatus.rejected:
      return 'REJECTED';
    case ApplicationStatus.inProgress:
      return 'IN_PROGRESS';
    case ApplicationStatus.completed:
      return 'COMPLETED';
    case ApplicationStatus.cancelled:
      return 'CANCELLED';
  }
}

class Application {
  final int id;
  final int applicantId;
  final int parcelId;
  final String title;
  final String type;
  final String details;
  final ApplicationStatus status;
  final String? statusReason;
  final DateTime? createdAt;
  final DateTime? updatedAt;

  Application({
    required this.id,
    required this.applicantId,
    required this.parcelId,
    required this.title,
    required this.type,
    required this.details,
    required this.status,
    this.statusReason,
    this.createdAt,
    this.updatedAt,
  });

  factory Application.fromJson(Map<String, dynamic> json) {
    final parcel = json['parcel'];
    final parcelIdValue = parcel is Map<String, dynamic>
        ? parcel['id']
        : json['parcelId'];
    return Application(
      id: (json['id'] as num).toInt(),
      applicantId: (json['applicantId'] as num).toInt(),
      parcelId: parcelIdValue is num ? parcelIdValue.toInt() : int.parse(parcelIdValue.toString()),
      title: json['title']?.toString() ?? '',
      type: json['type']?.toString() ?? '',
      details: json['details']?.toString() ?? '',
      status: applicationStatusFromString(json['status']?.toString() ?? ''),
      statusReason: json['statusReason']?.toString(),
      createdAt: json['createdAt'] != null ? DateTime.parse(json['createdAt'].toString()) : null,
      updatedAt: json['updatedAt'] != null ? DateTime.parse(json['updatedAt'].toString()) : null,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'applicantId': applicantId,
      'parcelId': parcelId,
      'title': title,
      'type': type,
      'details': details,
      'status': applicationStatusToString(status),
      'statusReason': statusReason,
      'createdAt': createdAt?.toIso8601String(),
      'updatedAt': updatedAt?.toIso8601String(),
    };
  }
}

