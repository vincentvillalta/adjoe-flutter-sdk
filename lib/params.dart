/// Use this class to pass information about the user acquisition and the Catalog placement.
///
/// ```dart
/// PlaytimeParams params = new PlaytimeParams()
///   ..uaNetwork = 'network'
///   ..uaChannel = 'channel'
///   ..uaSubPublisherCleartext = 'cleartext'
///   ..haSubPublisherEncrypted = 'encrypted'
///   ..placement = 'placement')
/// Playtime.showCatalog(params)
/// ```
class PlaytimeParams {
  /// The user acquisition network.
  String? uaNetwork;

  /// The user acquisition channel.
  String? uaChannel;

  /// The user acquisition publisher cleartext.
  String? uaSubPublisherCleartext;

  /// The user acquisition publisher encrypted.
  String? uaSubPublisherEncrypted;

  /// The Catalog placement.
  String? placement;
}
