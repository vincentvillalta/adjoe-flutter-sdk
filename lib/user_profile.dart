import 'package:adjoe/gender.dart';

/// Use this class to pass the user's profile information (birthday and gender) to Playtime ([Playtime.init] or [Playtime.setProfile]).
///
/// ```dart
/// PlaytimeUserProfile userProfile = new PlaytimeUserProfile()
///   ..gender = _gender
///   ..birthday = _birthday;
/// ```
class PlaytimeUserProfile {
  /// The user's gender (see [PlaytimeGender].
  PlaytimeGender? gender;

  /// The user's birthday. It suffices to set the year.
  DateTime? birthday;
}
