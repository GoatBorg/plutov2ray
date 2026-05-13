package com.bycomsolutions.bycomvpn.activities;

import static com.bycomsolutions.bycomvpn.utils.LicenseUtils.verifyLicense;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bycomsolutions.bycomvpn.BuildConfig;
import com.bycomsolutions.bycomvpn.Preference;
import com.bycomsolutions.bycomvpn.R;
import com.bycomsolutions.bycomvpn.adapters.AppListAdapter;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class BypassAppsActivity extends AppCompatActivity {


    RecyclerView recyclerView;

    TextView activityName;

    ImageView backToActivity;

    AppListAdapter appListAdapter;

    Preference preference;

    ShimmerFrameLayout shimmer;

    HashMap<String, String> excludedAppMap;

    LinearLayout placeholder;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_applist);
        initViews();
        verifyLicense(this);

        shimmer.showShimmer(true);

        preference = new Preference(this);

        activityName.setText(getString(R.string.bypass_apps));

        excludedAppMap = new HashMap<>();
        String hashMapJson = preference.getStringpreference(BuildConfig.PREFERENCE_KEY_EXCLUDED_LIST);

        if(!hashMapJson.isEmpty()) {
            Type type = new TypeToken<HashMap<String, String>>() {}.getType();
            excludedAppMap = new Gson().fromJson(hashMapJson, type);
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        new Thread(() -> {
            appListAdapter = new AppListAdapter(getInstalledApps(), excludedAppMap,getPackageManager(),this);
            runOnUiThread(() -> {
                recyclerView.setAdapter(appListAdapter);
                shimmer.hideShimmer();
                placeholder.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            });
        }).start();


    }

    private void initViews() {
        recyclerView = findViewById(R.id.rv_appList);
        activityName = findViewById(R.id.activity_name);
        backToActivity = findViewById(R.id.finish_activity);
        shimmer = findViewById(R.id.shimmer);
        placeholder = findViewById(R.id.placeholder);

        backToActivity.setOnClickListener(v -> finish());
    }


    private List<ResolveInfo> getInstalledApps() {
        PackageManager packageManager = getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> resolveInfoList = packageManager.queryIntentActivities(intent, 0);
        resolveInfoList.sort(Comparator.comparing(resolveInfo -> resolveInfo.loadLabel(packageManager).toString(),String.CASE_INSENSITIVE_ORDER));
        return resolveInfoList;
    }


    @Override
    public void finish() {
        String hashMapJson = new Gson().toJson( appListAdapter.getExcludedAppMap());
        preference.setStringpreference(BuildConfig.PREFERENCE_KEY_EXCLUDED_LIST,hashMapJson);
        super.finish();
    }



}