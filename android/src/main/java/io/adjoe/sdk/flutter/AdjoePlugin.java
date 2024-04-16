package io.adjoe.sdk.flutter;

import android.app.Activity;
import android.content.Intent;

import androidx.annotation.NonNull;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import io.adjoe.sdk.Adjoe;
import io.adjoe.sdk.AdjoeException;
import io.adjoe.sdk.AdjoeExtensions;
import io.adjoe.sdk.AdjoeGender;
import io.adjoe.sdk.AdjoeInitialisationListener;
import io.adjoe.sdk.AdjoeNotInitializedException;
import io.adjoe.sdk.AdjoeParams;
import io.adjoe.sdk.AdjoePayoutError;
import io.adjoe.sdk.AdjoePayoutListener;
import io.adjoe.sdk.AdjoeRewardListener;
import io.adjoe.sdk.AdjoeRewardResponse;
import io.adjoe.sdk.AdjoeRewardResponseError;
import io.adjoe.sdk.AdjoeUserProfile;
import io.flutter.Log;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

/** AdjoePlugin */
public class AdjoePlugin implements FlutterPlugin, MethodCallHandler, ActivityAware {

    private MethodChannel channel;

    private Activity context;

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "adjoe");
        channel.setMethodCallHandler(this);
    }

    @Override
    public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
        context = binding.getActivity();
    }

    @Override
    public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {
        context = binding.getActivity();
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        String method = call.method;
        if (method == null) {
            result.notImplemented();
            return;
        }

        switch (method) {
            case "init":
                init(call, result);
                break;

            case "isInitialized":
                isInitialized(call, result);
                break;

            case "showOfferwall":
                showOfferwall(call, result);
                break;

            case "canShowOfferwall":
                canShowOfferwall(call, result);
                break;

            case "doPayout":
                doPayout(call, result);
                break;

            case "setProfile":
                setProfile(call, result);
                break;

            case "requestRewards":
                requestRewards(call, result);
                break;

            case "sendEvent":
                sendEvent(call, result);
                break;

            case "getVersion":
                getVersion(call, result);
                break;

            case "getVersionName":
                getVersionName(call, result);
                break;

            case "hasAcceptedTOS":
                hasAcceptedTOS(call, result);
                break;

            case "hasAcceptedUsagePermission":
                hasAcceptedUsagePermission(call, result);
                break;

            case "getUserId":
                getUserId(call, result);
                break;

            case "setUAParams":
                setUAParams(call, result);
                break;

            default:
                result.notImplemented();
                break;
        }
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {
        context = null;
    }

    @Override
    public void onDetachedFromActivity() {
        context = null;
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        channel.setMethodCallHandler(null);
    }

    private void init(final MethodCall call, final Result result) {
        String sdkHash = call.argument("sdk_hash");
        Map<String, Object> optionsMap = call.argument("options");
        Adjoe.Options options = new Adjoe.Options();
        if (optionsMap != null) {
            String userId = (String) optionsMap.get("user_id");
            String applicationProcessName = (String) optionsMap.get("application_process_name");

            Map<String, Object> userProfileMap = (Map<String, Object>) optionsMap.get("user_profile");
            AdjoeUserProfile userProfile = null;
            if (userProfileMap != null) {
                String genderString = (String) userProfileMap.get("gender");
                AdjoeGender gender = AdjoeGender.UNKNOWN;
                if (genderString.equals("male")) {
                    gender = AdjoeGender.MALE;
                } else if (genderString.equals("female")) {
                    gender = AdjoeGender.FEMALE;
                }

                Number birthdayMillis = (Number) userProfileMap.get("birthday");
                Date birthday = null;
                if (birthdayMillis != null) {
                    birthday = new Date(birthdayMillis.longValue());
                } else {
                    // backward compatibility, if user profile is set, we assume the birthday is set
                    // otherwise we will set it to current date
                    Log.i("Adjoe", "user profile is set but birthday is set to null. Please make sure you pass a proper birthday date");
                    birthday = new Date();
                }
                userProfile = new AdjoeUserProfile(gender, birthday);
            }

            Map<String, Object> paramsMap = (Map<String, Object>) optionsMap.get("params");
            AdjoeParams params = getParamsFromMap(paramsMap);
            Map<String, Object> extensionsMap = (Map<String, Object>) optionsMap.get("extension");
            AdjoeExtensions extensions = getAdjoeExtensionsFromMap(extensionsMap);

            options
                    .setUserId(userId)
                    .setApplicationProcessName(applicationProcessName)
                    .setUserProfile(userProfile)
                    .setExtensions(extensions)
                    .setParams(params);
        }

        options.w("flutter");

        Adjoe.init(context, sdkHash, options, new AdjoeInitialisationListener() {

            @Override
            public void onInitialisationFinished() {
                result.success(null);
            }

            @Override
            public void onInitialisationError(Exception e) {
                result.error("0", e.getMessage(), null);
            }
        });
    }

    private void isInitialized(MethodCall call, Result result) {
        result.success(Adjoe.isInitialized());
    }

    private void showOfferwall(MethodCall call, Result result) {
        try {
            Map<String, Object> paramsMap = (Map<String, Object>) call.argument("params");
            AdjoeParams params = getParamsFromMap(paramsMap);

            Intent adjoeOfferwallIntent = Adjoe.getOfferwallIntent(context, params);
            context.startActivity(adjoeOfferwallIntent);
            result.success(null);
        } catch (AdjoeException exception) {
            result.error("0", exception.getMessage(), null);
        }
    }

    private void canShowOfferwall(MethodCall call, Result result) {
        result.success(Adjoe.canShowOfferwall(context));
    }

    private void doPayout(MethodCall call, final Result result) {
        Map<String, Object> paramsMap = (Map<String, Object>) call.argument("params");
        AdjoeParams params = getParamsFromMap(paramsMap);

        try {
            Adjoe.doPayout(context, params, new AdjoePayoutListener() {

                @Override
                public void onPayoutExecuted(int coins) {
                    result.success(coins);
                }

                @Override
                public void onPayoutError(AdjoePayoutError adjoePayoutError) {
                    if (adjoePayoutError != null) {
                        Exception e = adjoePayoutError.getException();
                        result.error(String.valueOf(adjoePayoutError.getReason()), e == null ? null : e.getMessage(), null);
                    } else { // should never happen
                        result.error("-1", "Unknown error", null);
                    }
                }
            });
        } catch (AdjoeNotInitializedException e) {
            result.error("0", e.getMessage(), null);
        }
    }

    private void setProfile(MethodCall call, Result result) {
        String source = call.argument("source");

        Map<String, Object> userProfileMap = (Map<String, Object>) call.argument("user_profile");
        String genderString = (String) userProfileMap.get("gender");
        AdjoeGender gender = AdjoeGender.UNKNOWN;
        if (genderString.equals("male")) {
            gender = AdjoeGender.MALE;
        } else if (genderString.equals("female")) {
            gender = AdjoeGender.FEMALE;
        }

        Number birthdayMillis = (Number) userProfileMap.get("birthday");
        Date birthday = null;
        if (birthdayMillis != null) {
            birthday = new Date(birthdayMillis.longValue());
        }

        Map<String, Object> paramsMap = (Map<String, Object>) call.argument("params");
        AdjoeParams params = getParamsFromMap(paramsMap);

        try {
            Adjoe.setProfile(context, source, new AdjoeUserProfile(gender, birthday), params);
            result.success(null);
        } catch (AdjoeNotInitializedException e) {
            result.error("0", e.getMessage(), null);
        }
    }

    private void setUAParams(MethodCall call, Result result) {
        try {
            Map<String, Object> paramsMap = call.argument("params");
            if (paramsMap == null || paramsMap.isEmpty()) {
                result.error("90", "Ua Params is empty", null);
                return;
            }
            AdjoeParams params = getParamsFromMap(paramsMap);
            Adjoe.setUAParams(context, params);
            result.success(null);
        } catch (Exception exception) {
            result.error("91", "error call setUAParams", exception);
        }
    }

    private void requestRewards(MethodCall call, final Result result) {
        try {
            Map<String, Object> paramsMap = (Map<String, Object>) call.argument("params");
            AdjoeParams params = getParamsFromMap(paramsMap);

            Adjoe.requestRewards(context, params, new AdjoeRewardListener() {

                @Override
                public void onUserReceivesReward(AdjoeRewardResponse adjoeRewardResponse) {
                    if (adjoeRewardResponse != null) {
                        Map<String, Integer> resultMap = new HashMap<>();
                        resultMap.put("reward", adjoeRewardResponse.getReward());
                        resultMap.put("already_spent", adjoeRewardResponse.getAlreadySpentCoins());
                        resultMap.put("available_for_payout", adjoeRewardResponse.getAvailablePayoutCoins());
                        result.success(resultMap);
                    } else { // should never happen
                        result.success(null);
                    }
                }

                @Override
                public void onUserReceivesRewardError(AdjoeRewardResponseError adjoeRewardResponseError) {
                    if (adjoeRewardResponseError != null && adjoeRewardResponseError.getException() != null) {
                        result.error("0", adjoeRewardResponseError.getException().getMessage(), null);
                    } else { // should never happen
                        result.error("-1", "Unknown error", null);
                    }
                }
            });
        } catch (AdjoeNotInitializedException e) {
            result.error("0", e.getMessage(), null);
        }
    }

    private void sendEvent(MethodCall call, Result result) {
        try {
            int event = call.argument("event");
            String extra = call.argument("extra");
            Map<String, Object> paramsMap = (Map<String, Object>) call.argument("params");
            AdjoeParams params = getParamsFromMap(paramsMap);

            Adjoe.sendUserEvent(context, event, extra, params);
            result.success(null);
        } catch (AdjoeNotInitializedException e) {
            result.error("0", e.getMessage(), null);
        }

    }

    private void getVersion(MethodCall call, Result result) {
        result.success(Adjoe.getVersion());
    }

    private void getVersionName(MethodCall call, Result result) {
        result.success(Adjoe.getVersionName());
    }

    private void hasAcceptedTOS(MethodCall call, Result result) {
        result.success(Adjoe.hasAcceptedTOS(context));
    }

    private void hasAcceptedUsagePermission(MethodCall call, Result result) {
        result.success(Adjoe.hasAcceptedUsagePermission(context));
    }

    private void getUserId(MethodCall call, Result result) {
        result.success(Adjoe.getUserId(context));
    }

    private AdjoeParams getParamsFromMap(Map<String, Object> paramsMap) {
        String uaNetwork = (String) paramsMap.get("ua_network");
        String uaChannel = (String) paramsMap.get("ua_channel");
        String uaSubPublisherCleartext = (String) paramsMap.get("ua_sub_publisher_cleartext");
        String uaSubPublisherEncrypted = (String) paramsMap.get("ua_sub_publisher_encrypted");
        String placement = (String) paramsMap.get("placement");

        return new AdjoeParams.Builder()
                .setPlacement(placement)
                .setUaNetwork(uaNetwork)
                .setUaChannel(uaChannel)
                .setUaSubPublisherCleartext(uaSubPublisherCleartext)
                .setUaSubPublisherEncrypted(uaSubPublisherEncrypted)
                .build();
    }

    private AdjoeExtensions getAdjoeExtensionsFromMap(Map<String, Object> extensionsMap) {
        String subId1 = (String) extensionsMap.get("subId1");
        String subId2 = (String) extensionsMap.get("subId2");
        String subId3 = (String) extensionsMap.get("subId3");
        String subId4 = (String) extensionsMap.get("subId4");
        String subId5 = (String) extensionsMap.get("subId5");
        return new AdjoeExtensions.Builder()
                .setSubId1(subId1)
                .setSubId2(subId2)
                .setSubId3(subId3)
                .setSubId4(subId4)
                .setSubId5(subId5)
                .build();
    }
}
