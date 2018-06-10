package com.gadmob.consentsdk;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.gadmob.consentsdk.gdpr.R;
import com.google.ads.consent.ConsentManager;
import com.google.ads.consent.ConsentStatus;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /**
         - Check the location of the user if it's within EEA and with unknown status.
         - If the user is within EEA and with unknown status show the dialog for consentManager with two options to see relevant ads or to show less relevant ads.
         - The function retrieve a callback after the consentManager has been submitted or if it's not necessary not show the dialog.
         - It's save the consentManager of the user and if the user is not within EEA it saves show relevant ads status.
         */
        ConsentManager consentManager = InitialConsent.getInstance(this);

        // Your code after the consentManager is submitted if needed
        consentManager.check(new ConsentManager.ConsentCallback() {

            @Override
            public void onResult(boolean isRequestLocationInEeaOrUnknown) {
                // Your code
            }

            @Override
            public void onResult(boolean isRequestLocationInEeaOrUnknown, ConsentStatus consentStatus) {
                Toast.makeText(getApplicationContext(),  consentStatus.toString(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
