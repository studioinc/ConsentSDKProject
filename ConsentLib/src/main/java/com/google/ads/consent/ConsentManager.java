package com.google.ads.consent;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;

import java.net.MalformedURLException;
import java.net.URL;

public class ConsentManager {

    public static abstract class ConsentCallback {
        public abstract void onResult(boolean isRequestLocationInEeaOrUnknown);
        public void onResult(boolean isRequestLocationInEeaOrUnknown, ConsentStatus consentStatus){}
    }

    public static abstract class ConsentStatusCallback {
        abstract public void onResult(boolean isRequestLocationInEeaOrUnknown, int isConsentPersonalized);
    }

    public static abstract class ConsentInformationCallback {
        public abstract void onResult(ConsentInformation consentInformation, ConsentStatus consentStatus);
        public abstract void onFailed(ConsentInformation consentInformation, String reason);
    }

    public static abstract class LocationIsEeaOrUnknownCallback {
        public abstract void onResult(boolean isRequestLocationInEeaOrUnknown);
    }



    private static String ads_preference = "ads_preference";
    private static String preferences_name = "preferences_name";
    private static String user_status = "user_status";

    private static boolean PERSONALIZED = true;
    private static boolean NON_PERSONALIZED = false;
    private static boolean IS_DEBUG_GEOGRAPHY = false;

    public static boolean isAdsPersonalize;
    public static boolean isAdsNonPersonalize;
    public static boolean isUserNotFromEeaPersonalize;


    public final int AdsNonPersonalize = 0;
    public final int AdsPersonalize = 1;
    public final int AdsUserNotFromEeaPersonalize = 2;

    private Context context;
    private ConsentForm form;

    private String privacyUrl;
    private String publisherId;
    public static ConsentManager consentManager;


    private String testDeviceId = "";
    private String logTag = "";

    private int adAppUnitId;
    private int adBannerId;
    private int adIntetstitiaId;
    private int adRewardedVideoId;


    private AdRequest adRequest = null;
    private InterstitialAd interstitialAd;
    private AdView adView;
    private RewardedVideoAd rewardedVideoAd;
    //private AdCallListener admobCallListener;


    private AdInterstitialCallback adInterstitialCallback;
    private AdRewardedVideoCallback adRewardedVideoCallback;
    private AdBannerCallback adBannerCallback;

    //TODO: InterstialAd Ad Callback result
    public interface AdInterstitialCallback {
        void onInterstialAdClosed();
        void onInterstialAdOpened();
        void onInterstialAdClicked();
        void onInterstialAdLoaded();
        void onInterstialAdFailedToLoad(int i);
        void onInterstialAdImpression();
        void onInterstialAdLeftApplication();
    }

    //TODO: Rewarded Video Callback result
    public interface AdRewardedVideoCallback {
         void onRewardedVideoAdLoaded();
         void onRewardedVideoAdOpened();
         void onRewardedVideoAdStarted();
         void onRewardedAd(RewardItem rewardItem);
         void onRewardedVideoAdClosed();
         void onRewardedVideoAdLeftApplication();
         void onRewardedVideoAdFailedToLoad(int i);
         void onRewardedVideoAdCompleted();
    }

    //TODO: Banner Ad Callback result
    public interface AdBannerCallback {
         void onBannerAdClosed();
         void onBannerAdOpened();
         void onBannerAdClicked();
         void onBannerAdLoaded();
         void onBannerAdFailedToLoad(int i);
         void onBannerAdImpression();
         void onBannerAdLeftApplication();
    }


    // Admob banner test id
    private static String DUMMY_BANNER = "ca-app-pub-3940256099942544/6300978111";

    private SharedPreferences settings;

    public void setAdInterstitialCallback(AdInterstitialCallback listener){
        this.adInterstitialCallback = listener;
    }

    public void setAdRewardedVideoCallback(AdRewardedVideoCallback listener){
        this.adRewardedVideoCallback = listener;
    }

    public void setAdBannerCallback(AdBannerCallback listener){
        this.adBannerCallback = listener;
    }

    public static synchronized ConsentManager getInstance(Context context){
        if (consentManager == null){
            return consentManager = new ConsentManager(context);
        }
        return consentManager;
    }

    public ConsentManager(Context context) {
        this.context = context;
        this.settings = initPreferences(context);
    }

    // Initialize debug
    public ConsentManager(Context context, String publisherId, String privacyURL, boolean DEBUG) {
        this.context = context;
        this.settings = initPreferences(context);
        this.publisherId = publisherId;
        this.privacyUrl = privacyURL;
        this.consentManager = this;
    }


    // Initialize production
    public ConsentManager(Context context, String publisherId, String privacyURL) {
        this.context = context;
        this.settings = context.getSharedPreferences(preferences_name, Context.MODE_PRIVATE);
        this.publisherId = publisherId;
        this.privacyUrl = privacyURL;
        this.consentManager = this;
    }

    // Builder class
    public static class Builder {

        private Context context;
        private String LOG_TAG = "ID_LOG";
        private String DEVICE_ID = "";
        private boolean DEBUG = false;
        private String privacyUrl;
        private String publisherId;

        // Initialize Builder
        public Builder context(Context context) {
            this.context = context;
            return this;
        }

        // Add test device id
        public Builder testDeviceId(String device_id) {
            this.DEVICE_ID = device_id;
            return this;
        }

        // Add privacy policy
        public Builder privacyUrl(String privacyURL) {
            this.privacyUrl = privacyURL;
            return this;
        }

        // Add Publisher Id
        public Builder publisherId(String publisherId) {
            this.publisherId = publisherId;
            return this;
        }

        public Builder debugGeography(boolean debug){
            this.DEBUG = debug;
            return this;
        }

        // Add Logcat id
        public Builder log(String LOG_TAG) {
            this.LOG_TAG = LOG_TAG;
            return this;
        }

        // Build
        public ConsentManager build() {
            ConsentManager consentManager = new ConsentManager(context);
            consentManager.setPrivacyUrl(privacyUrl);
            consentManager.setPublisherId(publisherId);
            consentManager.setDebugGeographyEea(DEBUG);
            consentManager.setTestDeviceId(DEVICE_ID);
            consentManager.setLogTag(LOG_TAG);
            return consentManager;
        }
    }

    // Initialize dummy banner
    public static void initDummyBanner(Context context) {
        AdView adView = new AdView(context);
        adView.setAdSize(AdSize.BANNER);
        adView.setAdUnitId(DUMMY_BANNER);
        adView.loadAd(new AdRequest.Builder().build());
    }

    public void initAdAppId(int id){
        this.adAppUnitId = id;
        MobileAds.initialize(context, context.getResources().getString(adAppUnitId));
    }

    public void initAdFullScreen(int id){
        this.adIntetstitiaId = id;
        if (CheckNetwork.getInstance(context).isOnline()){
            this.interstitialAd = new InterstitialAd(context);
            this.interstitialAd.setAdUnitId(context.getResources().getString(adIntetstitiaId)); //context.getResources().getString(R.string.admob_interstitial_unit_id)
        } else {
            LogDebug.d("initInterstitialAd", "onCreate Ad not found internet network");
        }
    }

    public void initAdRewardedVideo(int id){
        this.rewardedVideoAd = MobileAds.getRewardedVideoAdInstance(context);
        this.adRewardedVideoId = id;
    }

    public void loadRewardedVideo(){
        if (CheckNetwork.getInstance(context).isOnline()){
            if (this.rewardedVideoAd != null){
                this.rewardedVideoAd.loadAd(context.getResources().getString(this.adRewardedVideoId), getAdRequest(context));
            }
        } else {
            LogDebug.d("showRewardedVideo", "showInterstitial Ad not found internet network");
        }
    }

    public RewardedVideoAd showRewardedVideo(){
        this.rewardedVideoAd.setRewardedVideoAdListener(new RewardedVideoAdListener() {
            @Override
            public void onRewardedVideoAdLoaded() {
                if (rewardedVideoAd.isLoaded()){
                    rewardedVideoAd.show();
                }
                if (adRewardedVideoCallback != null){
                    adRewardedVideoCallback.onRewardedVideoAdLoaded();
                }
            }

            @Override
            public void onRewardedVideoAdOpened() {
                if (adRewardedVideoCallback != null){
                    adRewardedVideoCallback.onRewardedVideoAdOpened();
                }
            }

            @Override
            public void onRewardedVideoStarted() {
                if (adRewardedVideoCallback != null){
                    adRewardedVideoCallback.onRewardedVideoAdStarted();
                }
            }

            @Override
            public void onRewardedVideoAdClosed() {
                if (adRewardedVideoCallback != null){
                    adRewardedVideoCallback.onRewardedVideoAdClosed();
                }
            }

            @Override
            public void onRewarded(RewardItem rewardItem) {
                if (adRewardedVideoCallback != null){
                    adRewardedVideoCallback.onRewardedAd(rewardItem);
                }
            }

            @Override
            public void onRewardedVideoAdLeftApplication() {
                if (adRewardedVideoCallback != null){
                    adRewardedVideoCallback.onRewardedVideoAdLeftApplication();
                }
            }

            @Override
            public void onRewardedVideoAdFailedToLoad(int i) {
                if (adRewardedVideoCallback != null){
                    adRewardedVideoCallback.onRewardedVideoAdFailedToLoad(i);
                }
            }

            @Override
            public void onRewardedVideoCompleted() {
                if (adRewardedVideoCallback != null){
                    adRewardedVideoCallback.onRewardedVideoAdCompleted();
                }
            }
        });

        return  this.rewardedVideoAd;
    }


    public void loadAdFullScreen(){
        if (CheckNetwork.getInstance(context).isOnline()){
            if (interstitialAd != null) {
                interstitialAd.loadAd(getAdRequest(context));
            }
        } else {
            LogDebug.d("showInterstitialAd", "showInterstitial Ad not found internet network");
        }
    }

    public void showAdFullScreen(){
        interstitialAd.setAdListener(new AdListener() {
            public void onAdClosed() {
                LogDebug.d("interstitialAd", "Ad Closed");
                if (adInterstitialCallback != null){
                    adInterstitialCallback.onInterstialAdClosed();
                }
            }

            @Override
            public void onAdLoaded() {
                LogDebug.d("interstitialAd", "Ad Loaded");
                if (interstitialAd.isLoaded()) {
                    interstitialAd.show();
                }
                if (adInterstitialCallback != null){
                    adInterstitialCallback.onInterstialAdLoaded();
                }
            }

            @Override
            public void onAdFailedToLoad(int i) {
                LogDebug.d("interstitialAd", "Ad Failed To Load: " + i);
                if (adInterstitialCallback != null){
                    adInterstitialCallback.onInterstialAdFailedToLoad(i);
                }
            }

            @Override
            public void onAdClicked() {
                super.onAdClicked();
                if (adInterstitialCallback != null){
                    adInterstitialCallback.onInterstialAdClicked();
                }
            }

            @Override
            public void onAdOpened() {
                super.onAdOpened();
                if (adInterstitialCallback != null){
                    adInterstitialCallback.onInterstialAdOpened();
                }
            }

            @Override
            public void onAdImpression() {
                super.onAdImpression();
                if (adInterstitialCallback != null){
                    adInterstitialCallback.onInterstialAdImpression();
                }
            }

            @Override
            public void onAdLeftApplication() {
                super.onAdLeftApplication();
                if (adInterstitialCallback != null){
                    adInterstitialCallback.onInterstialAdLeftApplication();
                }
            }
        });
    }

    public void loadAdBanner(AdView view){
        this.adView = view;
        if (CheckNetwork.getInstance(context).isOnline()){
            if (adView != null){
                adView.loadAd(getAdRequest(context));
            }
        } else {
            LogDebug.d("showBannerAd", "showBanner Ad not found internet network");
        }
    }

    public void showAdBanner(){
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                LogDebug.d("AdViewBanner", "Ad Loaded");
                adView.setVisibility(View.VISIBLE);
                if (adBannerCallback != null){
                    adBannerCallback.onBannerAdLoaded();
                }
            }

            @Override
            public void onAdOpened() {
                super.onAdOpened();
                if (adBannerCallback != null){
                    adBannerCallback.onBannerAdOpened();
                }
            }

            @Override
            public void onAdClicked() {
                super.onAdClicked();
                if (adBannerCallback != null){
                    adBannerCallback.onBannerAdClicked();
                }
            }

            @Override
            public void onAdClosed() {
                super.onAdClosed();
                if (adBannerCallback != null){
                    adBannerCallback.onBannerAdClosed();
                }
            }

            @Override
            public void onAdLeftApplication() {
                super.onAdLeftApplication();
                if (adBannerCallback != null){
                    adBannerCallback.onBannerAdLeftApplication();
                }
            }

            @Override
            public void onAdImpression() {
                super.onAdImpression();
                if (adBannerCallback != null){
                    adBannerCallback.onBannerAdImpression();
                }
            }

            @Override
            public void onAdFailedToLoad(int i) {
                LogDebug.d("AdViewBanner",  "Ad Failed To Load: " + i);
                adView.setVisibility(View.GONE);
                if (adBannerCallback != null){
                    adBannerCallback.onBannerAdFailedToLoad(i);
                }
            }
        });
    }


    // Initialize SharedPreferences
    private static SharedPreferences initPreferences(Context context) {
        return context.getSharedPreferences(preferences_name, Context.MODE_PRIVATE);
    }

    // ConsentManager status
    public static boolean isConsentPersonalized(Context context) {
        SharedPreferences settings = initPreferences(context);
        return settings.getBoolean(ads_preference, PERSONALIZED);
    }

    // ConsentManager is personalized
    private void consentIsPersonalized() {
        settings.edit().putBoolean(ads_preference, PERSONALIZED).apply();
    }

    // ConsentManager is non personalized
    private void consentIsNonPersonalized() {
        settings.edit().putBoolean(ads_preference, NON_PERSONALIZED).apply();
    }

    // ConsentManager is within
    private void updateUserStatus(boolean status) {
        settings.edit().putBoolean(user_status, status).apply();
    }

    // Get AdRequest
    public static AdRequest getAdRequest(Context context) {
        if(isConsentPersonalized(context)) {
            return new AdRequest.Builder().build();
        } else {
            return new AdRequest.Builder()
                    .addNetworkExtrasBundle(AdMobAdapter.class, getNonPersonalizedAdsBundle())
                    .build();
        }
    }

    public boolean isUserFromEea(){
        if(isConsentPersonalized(context)) {
            return true;
        } else {
            return false;
        }
    }

    // Get Non Personalized Ads Bundle
    private static Bundle getNonPersonalizedAdsBundle() {
        Bundle extras = new Bundle();
        extras.putString("npa", "1");
        return extras;
    }

    public String getPrivacyUrl() {
        return privacyUrl;
    }

    public void setPrivacyUrl(String privacyURL) {
        this.privacyUrl = privacyURL;
    }

    public String getPublisherId() {
        return publisherId;
    }

    public void setPublisherId(String publisherId) {
        this.publisherId = publisherId;
    }

    public boolean isDebugGeographyEea() {
        return IS_DEBUG_GEOGRAPHY;
    }

    public void setDebugGeographyEea(boolean debugGeographyEea) {
        IS_DEBUG_GEOGRAPHY = debugGeographyEea;
    }

    public String getTestDeviceId() {
        return testDeviceId;
    }

    public void setTestDeviceId(String testDeviceId) {
        this.testDeviceId = testDeviceId;
    }

    public String getLogTag() {
        return logTag;
    }

    public void setLogTag(String logTag) {
        this.logTag = logTag;
    }

    public boolean checkLinkExt(String url){
        if (url.startsWith("http://")){
            return true;
        } else if (url.startsWith("https://")){
            return true;
        } else {
            return false;
        }
    }

    public static String stripNonDigits(final CharSequence input){
        final StringBuilder sb = new StringBuilder(input.length());
        for(int i = 0; i < input.length(); i++){
            final char c = input.charAt(i);
            if(c > 47 && c < 58){
                sb.append(c);
            }
        }
        return sb.toString();
    }

    // ConsentManager information
    private void initConsentInformation(final ConsentInformationCallback callback) {
        final ConsentInformation consentInformation = ConsentInformation.getInstance(context);
        if (isDebugGeographyEea()) {
            consentInformation.addTestDevice(getTestDeviceId());
            consentInformation.setDebugGeography(DebugGeography.DEBUG_GEOGRAPHY_EEA);
        }
        String[] publisherIds = {publisherId};
        consentInformation.requestConsentInfoUpdate(publisherIds, new ConsentInfoUpdateListener() {
            @Override
            public void onConsentInfoUpdated(ConsentStatus consentStatus) {
                if(callback != null) {
                    callback.onResult(consentInformation, consentStatus);
                }
            }

            @Override
            public void onFailedToUpdateConsentInfo(String reason) {
                callback.onFailed(consentInformation, reason);
            }
        });
    }

    // Check if the location is EEA
    public void isRequestLocationIsEeaOrUnknown(final LocationIsEeaOrUnknownCallback callback) {
        // Get ConsentManager information
        initConsentInformation(new ConsentInformationCallback() {
            @Override
            public void onResult(ConsentInformation consentInformation, ConsentStatus consentStatus) {
                callback.onResult(consentInformation.isRequestLocationInEeaOrUnknown());
            }

            @Override
            public void onFailed(ConsentInformation consentInformation, String reason) {
                callback.onResult(false);
            }
        });
    }

    // Check the user location
    public static boolean isUserLocationWithinEea(Context context) {
        return initPreferences(context).getBoolean(user_status, false);
    }

    // Initialize ConsentManager SDK
    public void check(final ConsentCallback callback) {
        // Initialize consentManager information
        initConsentInformation(new ConsentInformationCallback() {
            @Override
            public void onResult(ConsentInformation consentInformation, ConsentStatus consentStatus) {
                // Switch consentManager
                switch(consentStatus) {
                    case UNKNOWN:
                        // Debugging
                        if (isDebugGeographyEea()) {
                            Log.d(getLogTag(), "Unknown ConsentManager");
                            Log.d(getLogTag(), "User location within EEA: " + consentInformation.isRequestLocationInEeaOrUnknown());
                        }
                        // Check the user status
                        if(consentInformation.isRequestLocationInEeaOrUnknown()) {
                            Log.d(getLogTag(), "consentInformation.isRequestLocationInEeaOrUnknown()");
                            request(new ConsentStatusCallback() {
                                @Override
                                public void onResult(boolean isRequestLocationInEeaOrUnknown, int isConsentPersonalized) {
                                    callback.onResult(isRequestLocationInEeaOrUnknown);
                                }
                            }, new ConsentCallback() {
                                @Override
                                public void onResult(boolean isRequestLocationInEeaOrUnknown) {

                                }

                                @Override
                                public void onResult(boolean isRequestLocationInEeaOrUnknown, ConsentStatus consentStatus) {
                                    ConsentStatus position;
                                    switch (consentStatus){
                                        case NON_PERSONALIZED:
                                            position = ConsentStatus.NON_PERSONALIZED;
                                            break;
                                        case PERSONALIZED:
                                            position = ConsentStatus.PERSONALIZED;
                                            break;
                                        default:
                                            position = ConsentStatus.UNKNOWN;
                                            break;
                                    }
                                    callback.onResult(isRequestLocationInEeaOrUnknown, position);
                                }
                            });
                        } else {
                            consentIsPersonalized();
                            // Callback
                            callback.onResult(consentInformation.isRequestLocationInEeaOrUnknown(), consentStatus);
                        }
                        break;
                    case NON_PERSONALIZED:
                        consentIsNonPersonalized();
                        // Callback
                        callback.onResult(consentInformation.isRequestLocationInEeaOrUnknown(), consentStatus);
                        break;
                    case PERSONALIZED:
                        consentIsPersonalized();
                        // Callback
                        callback.onResult(consentInformation.isRequestLocationInEeaOrUnknown(), consentStatus);
                        break;
                    default:
                        consentIsPersonalized();
                        // Callback
                        callback.onResult(consentInformation.isRequestLocationInEeaOrUnknown(), consentStatus);
                        break;
                }
                // Update user status
                updateUserStatus(consentInformation.isRequestLocationInEeaOrUnknown());
            }

            @Override
            public void onFailed(ConsentInformation consentInformation, String reason) {
                if(isDebugGeographyEea()) {
                    Log.d(getLogTag(), "Failed to update: $reason");
                }
                // Update user status
                updateUserStatus(consentInformation.isRequestLocationInEeaOrUnknown());
                // Callback
                callback.onResult(consentInformation.isRequestLocationInEeaOrUnknown());
            }
        });
    }

    // Request ConsentManager
    public void request(final ConsentStatusCallback callback, final ConsentCallback consentCallback) {
        URL privacyUrl = null;
        try {
            privacyUrl = new URL(getPrivacyUrl());
        } catch (MalformedURLException e) {
//            e.printStackTrace();
        }
        form = new ConsentForm.Builder(context, privacyUrl)
                .withListener(new ConsentFormListener() {
                    @Override
                    public void onConsentFormLoaded() {
                        if(isDebugGeographyEea()) {
                            Log.d(getLogTag(), "ConsentManager Form is loaded!");
                        }
                        form.show();
                    }

                    @Override
                    public void onConsentFormError(String reason) {
                        if(isDebugGeographyEea()) {
                            Log.d(getLogTag(), "ConsentManager Form ERROR: $reason");
                        }
                        // Callback on Error
                        if (callback != null) {
                            isRequestLocationIsEeaOrUnknown(new LocationIsEeaOrUnknownCallback() {
                                @Override
                                public void onResult(boolean isRequestLocationInEeaOrUnknown) {
                                    callback.onResult(isRequestLocationInEeaOrUnknown, -1);
                                }
                            });
                        }
                    }

                    @Override
                    public void onConsentFormOpened() {
                        if(isDebugGeographyEea()) {
                            Log.d(getLogTag(), "ConsentManager Form is opened!");
                        }
                    }

                    @Override
                    public void onConsentFormClosed(final ConsentStatus consentStatus, Boolean userPrefersAdFree) {
                        if(isDebugGeographyEea()) {
                            Log.d(getLogTag(), "ConsentManager Form Closed!");
                        }
                        final int isConsentPersonalized;
                        // Check the consentManager status and save it
                        switch (consentStatus) {
                            case NON_PERSONALIZED:
                                consentIsNonPersonalized();
                                isConsentPersonalized = 0;
                                break;
                            case PERSONALIZED:
                                consentIsPersonalized();
                                isConsentPersonalized = 1;
                                break;
                            default:
                                consentIsPersonalized();
                                isConsentPersonalized = 2;
                                break;
                        }
                        // Callback
                        if(callback != null) {
                            isRequestLocationIsEeaOrUnknown(new LocationIsEeaOrUnknownCallback() {
                                @Override
                                public void onResult(boolean isRequestLocationInEeaOrUnknown) {
                                    callback.onResult(isRequestLocationInEeaOrUnknown, isConsentPersonalized);
                                    consentCallback.onResult(isRequestLocationInEeaOrUnknown, consentStatus);
                                }
                            });
                        }
                    }
                })
                .withPersonalizedAdsOption()
                .withNonPersonalizedAdsOption()
                .build();
        form.load();
    }

}
