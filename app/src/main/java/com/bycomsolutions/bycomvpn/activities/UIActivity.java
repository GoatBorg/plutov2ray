package com.bycomsolutions.bycomvpn.activities;

import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.bycomsolutions.bycomvpn.utils.Config;

public abstract class UIActivity extends AppCompatActivity implements View.OnClickListener {
    protected Config preference;
    protected final String TAG = "GoatBorg_VIP";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preference = new Config(this);
        // Reklam başlatma kodları tamamen uçuruldu.
    }

    protected void showMessage(String msg) {
        android.widget.Toast.makeText(this, msg, android.widget.Toast.LENGTH_SHORT).show();
    }

    protected abstract void connectToVpn();
    protected abstract void disconnectFromVnp();
    protected abstract void chooseServer();
    protected abstract void isConnected(unified.vpn.sdk.Callback<Boolean> callback);
    protected abstract void isLoggedIn(unified.vpn.sdk.Callback<Boolean> callback);
    protected abstract void loginToVpn();
    protected abstract void checkRemainingTraffic();
    protected abstract void getCurrentServer(unified.vpn.sdk.Callback<String> callback);

    protected void updateUI() {}
    protected void updateTrafficStats(long tx, long rx) {}
    protected void showConnectProgress() {}
    protected void hideConnectProgress() {}
    protected void startUIUpdateTask() {}
    protected void stopUIUpdateTask() {}
    protected void updateRemainingTraffic(unified.vpn.sdk.RemainingTraffic traffic) {}
    protected void ShowIP(String ip) {}
}
