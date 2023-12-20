/// Use this class to pass information about the user acquisition and the offerwall placement.
///
/// ```dart
/// AdjoeParams params = new AdjoeParams()
///   ..uaNetwork = 'network'
///   ..uaChannel = 'channel'
///   ..uaSubPublisherCleartext = 'cleartext'
///   ..haSubPublisherEncrypted = 'encrypted'
///   ..placement = 'placement')
/// Adjoe.showOfferwall(params)
/// ```
class AdjoeParams {
  /// The user acquisition network.
  String? uaNetwork;

  /// The user acquisition channel.
  String? uaChannel;

  /// The user acquisition publisher cleartext.
  String? uaSubPublisherCleartext;

  /// The user acquisition publisher encrypted.
  String? uaSubPublisherEncrypted;

  /// The offerwall placement.
  String? placement;
}
