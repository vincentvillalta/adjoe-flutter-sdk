import 'package:adjoe/extensions.dart';
import 'package:adjoe/params.dart';
import 'package:adjoe/user_profile.dart';

/// Use this class to pass additional parameters to [Playtime.init].
///
/// ```dart
/// PlaytimeOptions options = new PlaytimeOptions()
///   ..userId = 'userId'
///   ..applicationProcessName = 'name'
///   ..userProfile = _profile
///   ..params = _params;
/// ```
class PlaytimeOptions {
  String? userId;
  String? applicationProcessName;
  PlaytimeUserProfile? userProfile;
  PlaytimeParams? params;
  PlaytimeExtensions? extensions;
}
