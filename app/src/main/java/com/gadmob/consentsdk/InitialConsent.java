package com.gadmob.consentsdk;

import android.content.Context;

import com.google.ads.consent.ConsentManager;


/**
 * Created by NoThing on 6/9/2018.
 */

public class InitialConsent {
    public static ConsentManager getInstance(Context context){
        return new ConsentManager.Builder()
                .context(context)
                .privacyUrl("https://your.com/privacy/") // Add your privacy policy url
                .publisherId("pub-0123456789012345") // Add your admob publisher id
                .testDeviceId("DEVICE_ID_EMULATOR") // Add your test device id "Remove addTestDeviceId on production!"
                .log("CUSTOM_TAG") // Add custom tag default: ID_LOG
                .debugGeography(true) // Geography appears as in EEA for test devices.
                .build();
    }
}
