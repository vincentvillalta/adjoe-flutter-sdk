import 'package:adjoe/extensions.dart';
import 'package:adjoe/params.dart';
import 'package:adjoe/user_profile.dart';

/// Use this class to pass additional parameters to [Adjoe.init].
///
/// ```dart
/// AdjoeOptions options = new AdjoeOptions()
///   ..userId = 'userId'
///   ..applicationProcessName = 'name'
///   ..userProfile = _profile
///   ..params = _params;
/// ```
class AdjoeOptions {
  String? userId;
  String? applicationProcessName;
  AdjoeUserProfile? userProfile;
  AdjoeParams? params;
  AdjoeExtensions? extensions;
}
