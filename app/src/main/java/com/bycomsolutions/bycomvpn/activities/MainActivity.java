package com.bycomsolutions.bycomvpn.activities;

import static com.bycomsolutions.bycomvpn.utils.BillConfig.BUNDLE;
import static com.bycomsolutions.bycomvpn.utils.BillConfig.COUNTRY_DATA;
import static com.bycomsolutions.bycomvpn.utils.BillConfig.PRIMIUM_STATE;
import static com.bycomsolutions.bycomvpn.utils.BillConfig.SELECTED_COUNTRY;
import static com.bycomsolutions.bycomvpn.utils.LicenseUtils.verifyLicense;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import java.util.Map;

import unified.vpn.sdk.AuthMethod;
import unified.vpn.sdk.Backend;
import unified.vpn.sdk.Callback;
import unified.vpn.sdk.CompletableCallback;
import unified.vpn.sdk.Location;
import unified.vpn.sdk.HydraTransport;
import unified.vpn.sdk.HydraVpnTransportException;
import unified.vpn.sdk.NetworkRelatedException;
import unified.vpn.sdk.PartnerApiException;
import unified.vpn.sdk.Purchase;
import unified.vpn.sdk.RemainingTraffic;
import unified.vpn.sdk.SessionConfig;
import unified.vpn.sdk.SessionInfo;
import unified.vpn.sdk.TrackingConstants;
import unified.vpn.sdk.TrafficListener;
import unified.vpn.sdk.TrafficRule;
import unified.vpn.sdk.UnifiedSdk;
import unified.vpn.sdk.User;
import unified.vpn.sdk.VpnException;
import unified.vpn.sdk.VpnPermissionDeniedException;
import unified.vpn.sdk.VpnPermissionRevokedException;
import unified.vpn.sdk.VpnState;
import unified.vpn.sdk.VpnStateListener;
import unified.vpn.sdk.VpnTransportException;

public class MainActivity extends UIActivity implements TrafficListener, VpnStateListener {
    public static String selectedCountry = "";
    private String ServerIPaddress = "00.000.000.00";

    public static boolean VPN_CONNECTED = false;

    @Override
    protected void onStart() {
        super.onStart();
        UnifiedSdk.addTrafficListener(this);
        UnifiedSdk.addVpnStateListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        UnifiedSdk.removeVpnStateListener(this);
        UnifiedSdk.removeTrafficListener(this);
    }

    @Override
    public void onTrafficUpdate(long bytesTx, long bytesRx) {
        updateUI();
        updateTrafficStats(bytesTx, bytesRx);
    }

    @Override
    public void vpnStateChanged(@NonNull VpnState vpnState) {
        Log.e("sdfkljslkfj",vpnState.name());
        updateUI();
    }

    @Override
    public void vpnError(@NonNull VpnException e) {
        updateUI();
        handleError(e);
    }

    @Override
    protected void isLoggedIn(Callback<Boolean> callback) {
        UnifiedSdk.getInstance().getBackend().isLoggedIn(callback);
    }

    @Override
    protected void loginToVpn() {
        verifyLicense(this);
        AuthMethod authMethod = AuthMethod.anonymous();

        UnifiedSdk.getInstance().getBackend().login(authMethod, new Callback<User>() {
            @Override
            public void success(@NonNull User user) {
                updateUI();
            }

            @Override
            public void failure(@NonNull VpnException e) {
                updateUI();
                handleError(e);
            }
        });
    }

    public void logOut(){
        UnifiedSdk.getInstance().getBackend().logout(new CompletableCallback() {
            @Override
            public void complete() {
            }

            @Override
            public void error(VpnException e) {
            }
        });
    }

    @Override
    protected void isConnected(Callback<Boolean> callback) {
        UnifiedSdk.getVpnState(new Callback<VpnState>() {
            @Override
            public void success(@NonNull VpnState vpnState) {
                callback.success(vpnState == VpnState.CONNECTED);
            }

            @Override
            public void failure(@NonNull VpnException e) {
                callback.success(false);
            }
        });
    }

    @Override
    protected void connectToVpn() {
        verifyLicense(this);
        isLoggedIn(new Callback<Boolean>() {
            @Override
            public void success(@NonNull Boolean aBoolean) {
                if (aBoolean) {

                    Locale locale = new Locale("", selectedCountry);
                    if(selectedCountry.isEmpty())
                        showMessage(getString(R.string.connecting_to_fastest_server));
                    else showMessage(getString(R.string.connecting_to)+ locale.getDisplayCountry());

                    List<String> bypassDomains = new LinkedList<>();

                    List<String> excludedApps = new ArrayList<>();
                    String json = preference.getStringpreference(BuildConfig.PREFERENCE_KEY_EXCLUDED_LIST);
                    if(!json.isEmpty()) {
                        Type type = new TypeToken<HashMap<String, String>>() {}.getType();
                        HashMap<String, String> excludedAppMap = new Gson().fromJson(json, type);
                        excludedApps = new ArrayList<>(excludedAppMap.keySet());
                    }

                    UnifiedSdk.getInstance().getVpn().start(new SessionConfig.Builder()
                            .withReason(TrackingConstants.GprReasons.M_UI)
                            .withLocation(selectedCountry)
                            .withTransport(HydraTransport.TRANSPORT_ID)
                            .addDnsRule(TrafficRule.dns().bypass().fromDomains(bypassDomains))
                            .exceptApps(excludedApps)
                            .build(), new CompletableCallback() {
                        @Override
                        public void complete() {
                            hideConnectProgress();
                            startUIUpdateTask();

                            if(selectedCountry.isEmpty())
                                showMessage(getString(R.string.connected_to_fastest_server));
                            else showMessage(getString(R.string.connected_to)+ locale.getDisplayCountry());
                            
                            // GoatBorg VIP: Reklam yükleme ve gösterme kodları tamamen silindi!
                        }

                        @Override
                        public void error(@NonNull VpnException e) {
                            hideConnectProgress();
                            updateUI();
                            handleError(e);
                            Toast.makeText(MainActivity.this, getString(R.string.vpn_server_error), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }

            @Override
            public void failure(@NonNull VpnException e) {
            }
        });
    }

    @Override
    protected void disconnectFromVnp() {
        verifyLicense(this);
        showConnectProgress();
        UnifiedSdk.getInstance().getVpn().stop(TrackingConstants.GprReasons.M_UI, new CompletableCallback() {
            @Override
            public void complete() {
                hideConnectProgress();
                stopUIUpdateTask();
            }

            @Override
            public void error(@NonNull VpnException e) {
                hideConnectProgress();
                updateUI();
                handleError(e);
            }
        });
    }

    @Override
    protected void chooseServer() {
        verifyLicense(this);
        isLoggedIn(new Callback<Boolean>() {
            @Override
            public void success(@NonNull Boolean aBoolean) {
                if (aBoolean) {
                    startActivityForResult(new Intent(MainActivity.this, ServerActivity.class), 3000);
                } else {
                    showMessage(getString(R.string.login_please));
                }
            }

            @Override
            public void failure(@NonNull VpnException e) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 3000) {
            if (resultCode == RESULT_OK) {
                Gson gson = new Gson();
                Bundle args = data.getBundleExtra(BUNDLE);
                CountryData item = gson.fromJson(args.getString(COUNTRY_DATA), CountryData.class);
                onRegionSelected(item);
            }
        }
    }

    @Override
    protected void getCurrentServer(final Callback<String> callback) {
        verifyLicense(this);
        UnifiedSdk.getVpnState(new Callback<VpnState>() {
            @Override
            public void success(@NonNull VpnState state) {
                if (state == VpnState.CONNECTED) {
                    UnifiedSdk.getStatus(new Callback<SessionInfo>() {
                        @Override
                        public void success(@NonNull SessionInfo sessionInfo) {
                            ServerIPaddress = sessionInfo.getCredentials().getServers().get(0).getAddress();
                            ShowIP(ServerIPaddress);
                            callback.success(sessionInfo.getCredentials().getServers().get(0).getCountry());
                        }

                        @Override
                        public void failure(@NonNull VpnException e) {
                            callback.success(selectedCountry);
                        }
                    });
                } else {
                    callback.success(selectedCountry);
                }
            }

            @Override
            public void failure(@NonNull VpnException e) {
                callback.failure(e);
            }
        });
    }

    @Override
    protected void checkRemainingTraffic() {
        UnifiedSdk.getInstance().getBackend().remainingTraffic(new Callback<RemainingTraffic>() {
            @Override
            public void success(@NonNull RemainingTraffic remainingTraffic) {
                updateRemainingTraffic(remainingTraffic);
            }

            @Override
            public void failure(@NonNull VpnException e) {
                updateUI();
                handleError(e);
            }
        });
    }

    public void onRegionSelected(CountryData item) {
         final Location new_countryValue = item.getCountryvalue();
        if (!item.isPro() || preference.isBooleenPreference(PRIMIUM_STATE)) {
            selectedCountry = new_countryValue.getName();
            preference.setStringpreference(SELECTED_COUNTRY, selectedCountry);
            updateUI();
            UnifiedSdk.getVpnState(new Callback<VpnState>() {
                @Override
                public void success(@NonNull VpnState state) {
                    if (state == VpnState.CONNECTED) {
                        UnifiedSdk.getInstance().getVpn().stop(TrackingConstants.GprReasons.M_UI, new CompletableCallback() {
                            @Override
                            public void complete() {
                                connectToVpn();
                            }

                            @Override
                            public void error(@NonNull VpnException e) {
                                selectedCountry = "";
                                preference.setStringpreference(SELECTED_COUNTRY, selectedCountry);
                                connectToVpn();
                            }
                        });
                    }else img_connect.performClick();
                }

                @Override
                public void failure(@NonNull VpnException e) {

                }
            });
        } else {
            Intent intent = new Intent(MainActivity.this, GetPremiumActivity.class);
            startActivity(intent);
        }
    }

    public void handleError(Throwable e) {
        if (e instanceof NetworkRelatedException) {
            showMessage(getString(R.string.check_internet_connection));
        } else if (e instanceof VpnException) {
            if (e instanceof VpnPermissionRevokedException) {
                showMessage(getString(R.string.user_revoked_vpn_permissions));
            } else if (e instanceof VpnPermissionDeniedException) {
                showMessage(getString(R.string.user_canceled_to_grant_vpn_permissions));
            } else if (e instanceof VpnTransportException) {
                HydraVpnTransportException hydraVpnTransportException = (HydraVpnTransportException) e;
                if (hydraVpnTransportException.getCode() == HydraVpnTransportException.HYDRA_ERROR_BROKEN) {
                    showMessage(getString(R.string.connection_with_vpn_server_was_lost));
                } else if (hydraVpnTransportException.getCode() == HydraVpnTransportException.HYDRA_DCN_BLOCKED_BW) {
                    showMessage(getString(R.string.client_traffic_exceeded));
                } else {
                    showMessage(getString(R.string.error_in_vpn_transport));
                }
            } else {
                Log.e(TAG, getString(R.string.error_in_vpn_service));
            }
        } else if (e instanceof PartnerApiException) {
            switch (((PartnerApiException) e).getContent()) {
                case PartnerApiException.CODE_NOT_AUTHORIZED:
                    showMessage(getString(R.string.user_unauthorized));
                    break;
                case PartnerApiException.CODE_TRAFFIC_EXCEED:
                    showMessage(getString(R.string.server_unavailable));
                    break;
                default:
                    showMessage(getString(R.string.other_error_check_partnerapiexception_constants));
                    break;
            }
        }
    }

    @Override
    public void onClick(View view) {
    }

    @Override
    public void onBackPressed() {
        MaterialAlertDialogBuilder alertDialog = new MaterialAlertDialogBuilder(MainActivity.this);
        alertDialog.setTitle(R.string.leave_application);
        alertDialog.setMessage(R.string.are_you_sure_you_want_to_leave_the_application);
        alertDialog.setPositiveButton(R.string.yes, (dialog, which) -> finish());
        alertDialog.setNegativeButton(R.string.no, null);
        alertDialog.show();
    }
}
