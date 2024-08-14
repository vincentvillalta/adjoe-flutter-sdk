package io.adjoe.sdk.flutter;

import android.app.Activity;
import android.content.Intent;

import androidx.annotation.NonNull;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import io.adjoe.sdk.Playtime;
import io.adjoe.sdk.custom.PlaytimeCustom;
import io.adjoe.sdk.PlaytimeException;
import io.adjoe.sdk.PlaytimeExtensions;
import io.adjoe.sdk.PlaytimeGender;
import io.adjoe.sdk.PlaytimeInitialisationListener;
import io.adjoe.sdk.PlaytimeNotInitializedException;
import io.adjoe.sdk.PlaytimeParams;
import io.adjoe.sdk.PlaytimeOptions;
import io.adjoe.sdk.custom.PlaytimePayoutError;
import io.adjoe.sdk.custom.PlaytimePayoutListener;
import io.adjoe.sdk.custom.PlaytimeRewardListener;
import io.adjoe.sdk.custom.PlaytimeRewardResponse;
import io.adjoe.sdk.custom.PlaytimeRewardResponseError;
import io.adjoe.sdk.PlaytimeUserProfile;
import io.flutter.Log;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

/** AdjoePlugin */
public class PlaytimePlugin implements FlutterPlugin, MethodCallHandler, ActivityAware {

    private MethodChannel channel;

    private Activity context;

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "playtime");
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

            case "showCatalog":
                showCatalog(call, result);
                break;

            case "doPayout":
                doPayout(call, result);
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
        PlaytimeOptions options = new PlaytimeOptions();
        if (optionsMap != null) {
            String userId = (String) optionsMap.get("user_id");
            String applicationProcessName = (String) optionsMap.get("application_process_name");

            Map<String, Object> userProfileMap = (Map<String, Object>) optionsMap.get("user_profile");
            PlaytimeUserProfile userProfile = null;
            if (userProfileMap != null) {
                String genderString = (String) userProfileMap.get("gender");
                PlaytimeGender gender = PlaytimeGender.UNKNOWN;
                if (genderString.equals("male")) {
                    gender = PlaytimeGender.MALE;
                } else if (genderString.equals("female")) {
                    gender = PlaytimeGender.FEMALE;
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
                userProfile = new PlaytimeUserProfile(gender, birthday);
            }

            Map<String, Object> paramsMap = (Map<String, Object>) optionsMap.get("params");
            PlaytimeParams params = getParamsFromMap(paramsMap);
            Map<String, Object> extensionsMap = (Map<String, Object>) optionsMap.get("extension");
            PlaytimeExtensions extensions = getAdjoeExtensionsFromMap(extensionsMap);

            options
                    .setUserId(userId)
                    .setApplicationProcessName(applicationProcessName)
                    .setUserProfile(userProfile)
                    .setExtensions(extensions)
                    .setParams(params);
        }

        options.w("flutter");

        Playtime.init(context, sdkHash, options, new PlaytimeInitialisationListener() {

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
        result.success(Playtime.isInitialized());
    }

    private void showCatalog(MethodCall call, Result result) {
        try {
            Map<String, Object> paramsMap = (Map<String, Object>) call.argument("params");
            PlaytimeParams params = getParamsFromMap(paramsMap);

            Intent adjoePlaytimeIntent = Playtime.getCatalogIntent(context, params);
            context.startActivity(adjoePlaytimeIntent);
            result.success(null);
        } catch (PlaytimeException exception) {
            result.error("0", exception.getMessage(), null);
        }
    }

    private void doPayout(MethodCall call, final Result result) {
        Map<String, Object> paramsMap = (Map<String, Object>) call.argument("params");
        PlaytimeParams params = getParamsFromMap(paramsMap);
        PlaytimeCustom.doPayout(context, params, new PlaytimePayoutListener() {

            @Override
            public void onPayoutExecuted(int coins) {
                result.success(coins);
            }

            @Override
            public void onPayoutError(PlaytimePayoutError adjoePayoutError) {
                if (adjoePayoutError != null) {
                    Exception e = adjoePayoutError.getException();
                    result.error(String.valueOf(adjoePayoutError.getReason()), e == null ? null : e.getMessage(), null);
                } else { // should never happen
                    result.error("-1", "Unknown error", null);
                }
            }
        });
    }

    private void setUAParams(MethodCall call, Result result) {
        try {
            Map<String, Object> paramsMap = call.argument("params");
            if (paramsMap == null || paramsMap.isEmpty()) {
                result.error("90", "Ua Params is empty", null);
                return;
            }
            PlaytimeParams params = getParamsFromMap(paramsMap);
            Playtime.setUAParams(context, params);
            result.success(null);
        } catch (Exception exception) {
            result.error("91", "error call setUAParams", exception);
        }
    }

    private void requestRewards(MethodCall call, final Result result) {
        Map<String, Object> paramsMap = (Map<String, Object>) call.argument("params");
        PlaytimeParams params = getParamsFromMap(paramsMap);

        PlaytimeCustom.requestRewards(context, params, new PlaytimeRewardListener() {

            @Override
            public void onUserReceivesReward(PlaytimeRewardResponse adjoeRewardResponse) {
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
            public void onUserReceivesRewardError(PlaytimeRewardResponseError adjoeRewardResponseError) {
                if (adjoeRewardResponseError != null && adjoeRewardResponseError.getException() != null) {
                    result.error("0", adjoeRewardResponseError.getException().getMessage(), null);
                } else { // should never happen
                    result.error("-1", "Unknown error", null);
                }
            }
        });
    }

    private void sendEvent(MethodCall call, Result result) {
        try {
            int event = call.argument("event");
            String extra = call.argument("extra");
            Map<String, Object> paramsMap = (Map<String, Object>) call.argument("params");
            PlaytimeParams params = getParamsFromMap(paramsMap);

            Playtime.sendUserEvent(context, event, extra, params);
            result.success(null);
        } catch (PlaytimeNotInitializedException e) {
            result.error("0", e.getMessage(), null);
        }

    }

    private void getVersion(MethodCall call, Result result) {
        result.success(Playtime.getVersion());
    }

    private void getVersionName(MethodCall call, Result result) {
        result.success(Playtime.getVersionName());
    }

    private void hasAcceptedTOS(MethodCall call, Result result) {
        result.success(Playtime.hasAcceptedTOS(context));
    }

    private void hasAcceptedUsagePermission(MethodCall call, Result result) {
        result.success(Playtime.hasAcceptedUsagePermission(context));
    }

    private void getUserId(MethodCall call, Result result) {
        result.success(Playtime.getUserId(context));
    }

    private PlaytimeParams getParamsFromMap(Map<String, Object> paramsMap) {
        String uaNetwork = (String) paramsMap.get("ua_network");
        String uaChannel = (String) paramsMap.get("ua_channel");
        String uaSubPublisherCleartext = (String) paramsMap.get("ua_sub_publisher_cleartext");
        String uaSubPublisherEncrypted = (String) paramsMap.get("ua_sub_publisher_encrypted");
        String placement = (String) paramsMap.get("placement");

        return new PlaytimeParams.Builder()
                .setPlacement(placement)
                .setUaNetwork(uaNetwork)
                .setUaChannel(uaChannel)
                .setUaSubPublisherCleartext(uaSubPublisherCleartext)
                .setUaSubPublisherEncrypted(uaSubPublisherEncrypted)
                .build();
    }

    private PlaytimeExtensions getAdjoeExtensionsFromMap(Map<String, Object> extensionsMap) {
        String subId1 = (String) extensionsMap.get("subId1");
        String subId2 = (String) extensionsMap.get("subId2");
        String subId3 = (String) extensionsMap.get("subId3");
        String subId4 = (String) extensionsMap.get("subId4");
        String subId5 = (String) extensionsMap.get("subId5");
        return new PlaytimeExtensions.Builder()
                .setSubId1(subId1)
                .setSubId2(subId2)
                .setSubId3(subId3)
                .setSubId4(subId4)
                .setSubId5(subId5)
                .build();
    }
}
