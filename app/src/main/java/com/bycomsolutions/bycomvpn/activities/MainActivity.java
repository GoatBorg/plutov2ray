package com.bycomsolutions.bycomvpn.activities;

import static com.bycomsolutions.bycomvpn.utils.BillConfig.BUNDLE;
import static com.bycomsolutions.bycomvpn.utils.BillConfig.COUNTRY_DATA;
import static com.bycomsolutions.bycomvpn.utils.BillConfig.PRIMIUM_STATE;
import static com.bycomsolutions.bycomvpn.utils.BillConfig.SELECTED_COUNTRY;
import static com.bycomsolutions.bycomvpn.utils.LicenseUtils.verifyLicense;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import com.bycomsolutions.bycomvpn.BuildConfig;
import com.bycomsolutions.bycomvpn.R;
import com.bycomsolutions.bycomvpn.dialog.CountryData;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import unified.vpn.sdk.*;

public class MainActivity extends UIActivity implements TrafficListener, VpnStateListener {
    public static String selectedCountry = "";

    @Override
    protected void connectToVpn() {
        verifyLicense(this);
        UnifiedSdk.getInstance().getBackend().isLoggedIn(new Callback<Boolean>() {
            @Override
            public void success(@NonNull Boolean aBoolean) {
                if (aBoolean) {
                    Locale locale = new Locale("", selectedCountry);
                    UnifiedSdk.getInstance().getVpn().start(new SessionConfig.Builder()
                            .withReason(TrackingConstants.GprReasons.M_UI)
                            .withLocation(selectedCountry)
                            .withTransport(HydraTransport.TRANSPORT_ID)
                            .build(), new CompletableCallback() {
                        @Override
                        public void complete() {
                            hideConnectProgress();
                            // GoatBorg VIP: Reklam yükleme kodları silindi!
                            showMessage(getString(R.string.connected_to) + locale.getDisplayCountry());
                        }

                        @Override
                        public void error(@NonNull VpnException e) {
                            hideConnectProgress();
                            handleError(e);
                        }
                    });
                }
            }
            @Override public void failure(@NonNull VpnException e) {}
        });
    }

    @Override
    public void onRegionSelected(CountryData item) {
        // VIP Mod: isPro kontrolü her zaman true döner
        selectedCountry = item.getCountryvalue().getName();
        preference.setStringpreference(SELECTED_COUNTRY, selectedCountry);
        updateUI();
        img_connect.performClick();
    }

    // Diğer gereksiz reklam/satın alma metodları silindi...
}
