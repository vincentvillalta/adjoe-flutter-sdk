import 'package:adjoe/gender.dart';

/// Use this class to pass the user's profile information (birthday and gender) to adjoe ([Adjoe.init] or [Adjoe.setProfile]).
///
/// ```dart
/// AdjoeUserProfile userProfile = new AdjoeUserProfile()
///   ..gender = _gender
///   ..birthday = _birthday;
/// ```
class AdjoeUserProfile {
  /// The user's gender (see [AdjoeGender].
  AdjoeGender? gender;

  /// The user's birthday. It suffices to set the year.
  DateTime? birthday;
}
