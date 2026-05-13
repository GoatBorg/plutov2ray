package com.bycomsolutions.bycomvpn.activities;

import static com.bycomsolutions.bycomvpn.utils.BillConfig.BUNDLE;
import static com.bycomsolutions.bycomvpn.utils.BillConfig.COUNTRY_DATA;
import static com.bycomsolutions.bycomvpn.utils.BillConfig.PRIMIUM_STATE;
import static com.bycomsolutions.bycomvpn.utils.LicenseUtils.verifyLicense;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bycomsolutions.bycomvpn.Preference;
import com.bycomsolutions.bycomvpn.R;
import com.bycomsolutions.bycomvpn.adapters.LocationListAdapter;
import com.bycomsolutions.bycomvpn.dialog.CountryData;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.gson.Gson;
import unified.vpn.sdk.AvailableLocations;
import unified.vpn.sdk.Callback;
import unified.vpn.sdk.ConnectionType;
import unified.vpn.sdk.UnifiedSdk;
import unified.vpn.sdk.VpnException;

public class ServerActivity extends AppCompatActivity {


    RecyclerView regionsRecyclerView;


    LinearLayout shimmerPlaceholder;


    ShimmerFrameLayout shimmer;

    private LocationListAdapter regionAdapter;
    private RegionChooserInterface regionChooserInterface;
    ImageView backToActivity;
    TextView activity_name;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);
        initViews();
        verifyLicense(this);

        Preference preference = new Preference(this);

        activity_name.setText(R.string.servers);
        backToActivity.setOnClickListener(view -> finish());
        regionChooserInterface = item -> {
            if (!item.isPro() || preference.isBooleenPreference(PRIMIUM_STATE)) {
                Intent intent = new Intent();
                Bundle args = new Bundle();
                Gson gson = new Gson();
                String json = gson.toJson(item);

                args.putString(COUNTRY_DATA, json);
                intent.putExtra(BUNDLE, args);
                setResult(RESULT_OK, intent);
                finish();
            } else {
                Intent intent = new Intent(ServerActivity.this, GetPremiumActivity.class);
                startActivity(intent);
            }
        };

        regionsRecyclerView.setHasFixedSize(true);
        regionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        regionAdapter = new LocationListAdapter(item -> regionChooserInterface.onRegionSelected(item), ServerActivity.this);
        regionsRecyclerView.setAdapter(regionAdapter);
        loadServers();
    }
    private void initViews() {
        regionsRecyclerView = findViewById(R.id.regions_recycler_view);
        shimmerPlaceholder = findViewById(R.id.placeholder);
        shimmer = findViewById(R.id.shimmer);
        activity_name = findViewById(R.id.activity_name);
        backToActivity = findViewById(R.id.finish_activity);
    }

    private void loadServers() {
        showProgress();

        UnifiedSdk.getInstance().getBackend().locations(ConnectionType.HYDRA_TCP,new Callback<AvailableLocations>() {


            @Override
            public void success(@NonNull AvailableLocations availableLocations) {
                hideProgress();
                regionAdapter.setRegions(availableLocations.getLocations());
            }

            @Override
            public void failure(@NonNull VpnException e) {
                hideProgress();
            }
        });







    }

    private void showProgress() {
        shimmer.showShimmer(true);
        shimmerPlaceholder.setVisibility(View.VISIBLE);
        regionsRecyclerView.setVisibility(View.INVISIBLE);
    }

    private void hideProgress() {
        shimmer.hideShimmer();
        shimmerPlaceholder.setVisibility(View.GONE);
        regionsRecyclerView.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public interface RegionChooserInterface {
        void onRegionSelected(CountryData item);
    }
}
