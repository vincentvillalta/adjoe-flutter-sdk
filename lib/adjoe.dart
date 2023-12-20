import 'dart:async';

import 'package:adjoe/options.dart';
import 'package:adjoe/params.dart';
import 'package:adjoe/user_profile.dart';
import 'package:flutter/services.dart';
import 'package:adjoe/extensions.dart';

class Adjoe {
  static const int _EVENT_TEASER_SHOWN = 14;

  static const MethodChannel _channel = const MethodChannel('adjoe');

  /// Initializes the adjoe SDK.
  ///
  /// You must initialize the adjoe SDK before you can use any of its features.
  /// The initialization will run asynchronously in the background and returns a [Future]. The callback returns no value when the init succeeds
  /// and returns the error when the init fails.
  ///
  /// The [sdkHash] is required. The [options] are used to pass optional values like the `userId`.
  ///
  /// ```dart
  /// AdjoeOptions options = new AdjoeOptions()
  ///   ..userId = 'user_id'
  ///   ..applicationProcessName = 'name'
  ///   ..params = (new AdjoeParams()
  ///     ..uaNetwork = 'network'
  ///     ..uaChannel = 'channel'
  ///     ..uaSubPublisherCleartext = 'cleartext'
  ///     ..haSubPublisherEncrypted = 'encrypted'
  ///     ..placement = 'placement')
  ///   ..userProfile = (new AdjoeUserProfile()
  ///     ..birthday = birthday
  ///     ..gender = gender;
  ///
  /// Adjoe.init(sdkHash, options).then((_) {
  ///   print('Init finished successful');
  /// }, onError: (err) {
  ///   print('Init failed: $err');
  /// });
  /// ```
  static Future<void> init(String sdkHash, [AdjoeOptions? options]) async {
    return _channel.invokeMethod('init', {
      'sdk_hash': sdkHash,
      'options': {
        'user_id': options?.userId,
        'application_process_name': options?.applicationProcessName,
        'user_profile': _profileToMap(options?.userProfile),
        'params': _paramsToMap(options?.params),
        'extension': _extensionsToMap(options?.extensions),
        'w': 'flutter',
      },
    });
  }

  /// Launches the offerwall which contains the UI to show the user the rewarded apps which he can use.
  ///
  /// You can use the optional [AdjoeParams] to pass information about the placement and user acquisition channel.
  ///
  /// ```dart
  /// Adjoe.showOfferwall();
  /// ```
  static void showOfferwall([AdjoeParams? params]) async {
    return _channel
        .invokeMethod('showOfferwall', {'params': _paramsToMap(params)});
  }

  /// Checks whether the offerwall can be shown for the user.
  ///
  /// Returns `true` if it can be shown and `false` otherwise.
  ///
  /// ```dart
  /// Adjoe.canShowOfferwall().then((canShow) {
  ///   if (canShow) {
  ///     Adjoe.showOfferwall();
  ///   }
  /// });
  /// ```
  static Future<bool?> canShowOfferwall() async {
    return _channel.invokeMethod('canShowOfferwall');
  }

  /// Pays out the user's collected rewards.
  ///
  /// When finished, the returned [Future] succeeds when the payout was successful and it will return the amount of paid out rewards.
  /// If the payout fails, the [Future] will fail with an exception.
  ///
  /// You can use the optional [AdjoeParams] to pass information about the placement and user acquisition channel.
  ///
  /// ```dart
  /// Adjoe.doPayout().then((value) {
  ///   print('Paid out $value rewards');
  /// }, onError: (err) {
  ///   print('Error while paying out: $err');
  /// });
  /// ```
  static Future<int?> doPayout([AdjoeParams? params]) async {
    return _channel.invokeMethod('doPayout', {'params': _paramsToMap(params)});
  }

  /// Provides adjoe with information about the user's profile.
  ///
  /// With this information adjoe can suggest better matching apps to the user.
  ///
  /// The [source] describes where the profile information comes from (like `"facebook"`, `"google"`, `"user-input"`, ...).
  /// It is possible to set only one of `gender` and `birthday` in the [AdjoeUserProfile]. The year of birth suffices for the `birthday`.
  ///
  /// You can use the optional [AdjoeParams] to pass information about the placement and user acquisition channel.
  ///
  /// Returns a [Future] which succeeds when the profile information has been sent and fails if sending the profile information fails.
  ///
  /// ```dart
  /// import 'package:adjoe/user_profile.dart';
  /// import 'package:adjoe/gender.dart';
  /// ...
  /// AdjoeUserProfile profile = new AdjoeUserProfile()
  ///   ..birthday = _birthday
  ///   ..gender = _gender;
  /// Adjoe.setProfile(_profileSource, profile).then((_) {
  ///   print('Profile was set');
  /// }, onError: (err) {
  ///   print('Failed to set profile: $err');
  /// });
  /// ```
  static Future<void> setProfile(String? source, AdjoeUserProfile profile,
      [AdjoeParams? params]) async {
    return _channel.invokeMethod('setProfile', {
      'source': source,
      'user_profile': _profileToMap(profile),
      'params': _paramsToMap(params)
    });
  }

  /// Provides adjoe with information about the User Acquisition parameters (UA Params).
  ///
  ///
  /// [AdjoeParams] to pass information about the placement and user acquisition network, channel.... etc
  ///
  /// Returns a [Future] which succeeds when the UA parameter has been sent and fails if sending UA parameter fails.
  ///
  /// ```dart
  /// import 'package:adjoe/params.dart';
  /// ...
  /// AdjoeParams params = new AdjoeParams(
  ///   ..uaNetwork = 'network'
  ///   ..uaChannel = 'channel'
  ///   ..uaSubPublisherCleartext = 'cleartext'
  ///   ..haSubPublisherEncrypted = 'encrypted'
  ///   ..placement = 'placement')
  /// Adjoe.setProfile(params).then((_) {
  ///   print('UA was set');
  /// }, onError: (err) {
  ///   print('Failed to set UA parameters: $err');
  /// });
  /// ```
  static Future<void> setUAParams(AdjoeParams params) async {
    return _channel.invokeMethod("setUAParams", {
      'params': _paramsToMap(params)
    });
  }

  /// Requests the user's current rewards, including how many of them are available for payout and how many have already been paid out.
  ///
  /// You can use the optional [AdjoeParams] to pass information about the placement and user acquisition channel.
  ///
  /// The returned [Future]'s [Map] contains three fields:
  ///  - `reward`: the total amount of rewards (available for payout + already spent).
  ///  - `available_for_payout`: the amount of rewards which are available for payout.
  ///  - `already_spent`: the amount of rewards which have already been paid out.
  ///  The [Future] will fail with an exception if the request fails.
  ///
  /// You can use the optional [AdjoeParams] to pass information about the placement and user acquisition channel.
  ///
  /// ```dart
  /// Adjoe.requestRewards().then((rewards) {
  ///   int amount = rewards['reward'];
  ///   int available = rewards['available_for_payout'];
  ///   int spent = rewards['already_spent'];
  ///   print('Received rewards: $amount (= $available + $spent)');
  /// }, onError: (err) {
  ///    print('Failed to request rewards: $err');
  /// });
  /// ```
  static Future<Map?> requestRewards([AdjoeParams? params]) async {
    return _channel
        .invokeMapMethod('requestRewards', {'params': _paramsToMap(params)});
  }

  /// Sends the TEASER_SHOWN event to adjoe.
  ///
  /// Call this method when the user can see the teaser, e.g. the button via which he can access the adjoe SDK from the SDK App.
  /// Trigger this event when the teaser has been successfully rendered and would successfully redirect the user to the adjoe SDK.
  /// It should be triggered regardless of whether the user has actually clicked the teaser or not.
  /// This event is mostly appropriate for uses, in which the functionality of the SDK App and SDK are kept separate to a relevant degree.
  //
  /// You can use the optional [AdjoeParams] to pass information about the placement and user acquisition channel.
  ///
  /// ```dart
  /// Adjoe.sendTeaserShownEvent().then((_) {
  ///   print('Sent TEASER SHOWN user event.');
  /// }, onError: (err) {
  ///   print('Failed to send TEASER SHOWN user event: $err');
  /// });
  /// ```
  static Future<void> sendTeaserShownEvent([AdjoeParams? params]) async {
    return _channel.invokeMethod('sendEvent', {
      'event': _EVENT_TEASER_SHOWN,
      'extra': null,
      'params': _paramsToMap(params),
    });
  }

  /// Returns the version of the adjoe SDK.
  ///
  /// ```dart
  /// int adjoeVersion = await Adjoe.getVersion();
  /// ```
  static Future<int?> getVersion() async {
    return _channel.invokeMethod('getVersion');
  }

  /// Returns the version name of the adjoe SDK.
  ///
  /// ```dart
  /// String adjoeVersionName = await Adjoe.getVersionName();
  /// ```
  static Future<String?> getVersionName() async {
    return _channel.invokeMethod('getVersionName');
  }

  /// Checks whether the adjoe SDK is initialized.
  ///
  /// Returns `true` when it is initialized and `false` otherwise.
  ///
  /// ```dart
  /// bool adjoeIsInitialized = await Adjoe.isInitialized();
  /// ```
  static Future<bool?> isInitialized() async {
    return _channel.invokeMethod('isInitialized');
  }

  /// Checks whether the user has accepted the adjoe Terms of Service (TOS).
  ///
  /// Returns `true` if the TOS are accepted and `false` otherwise.
  ///
  /// ```dart
  /// bool hasAcceptedAdjoeTOS = await Adjoe.hasAcceptedTOS();
  /// ```
  static Future<bool?> hasAcceptedTOS() async {
    return _channel.invokeMethod('hasAcceptedTOS');
  }

  /// Checks whether the user has given access to the usage statistics.
  ///
  /// Returns `true` if the user has given access to the usage statistics and `false` otherwise.
  ///
  /// ```dart
  /// bool hasAcceptedUsagePermission = await Adjoe.hasAcceptedUsagePermission();
  /// ```
  static Future<bool?> hasAcceptedUsagePermission() async {
    return _channel.invokeMethod('hasAcceptedUsagePermission');
  }

  /// Returns the unique ID of the user by which he is identified within the adjoe services.
  ///
  /// ```dart
  /// String adjoeUserId = await Adjoe.getUserId();
  /// ```
  static Future<String?> getUserId() async {
    return _channel.invokeMethod('getUserId');
  }

  static Map _paramsToMap(AdjoeParams? params) {
    return {
      'ua_network': params?.uaNetwork,
      'ua_channel': params?.uaChannel,
      'ua_sub_publisher_cleartext': params?.uaSubPublisherCleartext,
      'ua_sub_publisher_encrypted': params?.uaSubPublisherEncrypted,
      'placement': params?.placement,
    };
  }

  static Map _extensionsToMap(AdjoeExtensions? extensions) {
    return {
      'subId1': extensions?.subId1,
      'subId2': extensions?.subId2,
      'subId3': extensions?.subId3,
      'subId4': extensions?.subId4,
      'subId5': extensions?.subId5
    };
  }

  static Map? _profileToMap(AdjoeUserProfile? userProfile) {
    if (userProfile == null) {
      return null;
    }
    return {
      'gender': userProfile.gender?.toString().toLowerCase(),
      'birthday': userProfile.birthday?.millisecondsSinceEpoch,
    };
  }
}
