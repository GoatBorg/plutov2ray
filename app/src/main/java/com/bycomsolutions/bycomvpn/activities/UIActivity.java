package com.bycomsolutions.bycomvpn.activities;

import static com.bycomsolutions.bycomvpn.activities.MainActivity.VPN_CONNECTED;
import static com.bycomsolutions.bycomvpn.utils.BillConfig.INAPPSKUUNIT;
import static com.bycomsolutions.bycomvpn.utils.BillConfig.IN_PURCHASE_KEY;
import static com.bycomsolutions.bycomvpn.utils.BillConfig.One_Month_Sub;
import static com.bycomsolutions.bycomvpn.utils.BillConfig.One_Year_Sub;
import static com.bycomsolutions.bycomvpn.utils.BillConfig.PRIMIUM_STATE;
import static com.bycomsolutions.bycomvpn.utils.BillConfig.PURCHASETIME;
import static com.bycomsolutions.bycomvpn.utils.BillConfig.Six_Month_Sub;
import static com.bycomsolutions.bycomvpn.utils.LicenseUtils.verifyLicense;
import static com.bycomsolutions.bycomvpn.utils.VPNTileService.updateTileState;

import android.animation.ValueAnimator;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.limurse.iap.DataWrappers;
import com.limurse.iap.IapConnector;
import com.limurse.iap.SubscriptionServiceListener;
import com.bycomsolutions.bycomvpn.BuildConfig;
import com.bycomsolutions.bycomvpn.Preference;
import com.bycomsolutions.bycomvpn.R;
import com.bycomsolutions.bycomvpn.utils.AdMod;
import com.bycomsolutions.bycomvpn.utils.BillConfig;
import com.bycomsolutions.bycomvpn.utils.Utils;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.AdapterStatus;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.onesignal.Continue;
import com.onesignal.OneSignal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import unified.vpn.sdk.Callback;
import unified.vpn.sdk.ClientInfo;
import unified.vpn.sdk.CompletableCallback;
import unified.vpn.sdk.HydraTransportConfig;
import unified.vpn.sdk.RemainingTraffic;
import unified.vpn.sdk.SdkNotificationConfig;
import unified.vpn.sdk.TransportConfig;
import unified.vpn.sdk.UnifiedSDK;
import unified.vpn.sdk.UnifiedSdk;
import unified.vpn.sdk.UnifiedSdkConfig;
import unified.vpn.sdk.VpnException;
import unified.vpn.sdk.VpnState;


public abstract class UIActivity extends AppCompatActivity implements View.OnClickListener {

    protected static final String TAG = MainActivity.class.getSimpleName();
    public String SKU_DELAROY_MONTHLY;
    public String SKU_DELAROY_SIXMONTH;
    public String SKU_DELAROY_YEARLY;
    public String base64EncodedPublicKey;


    private static InterstitialAd mInterstitialAd;

    RelativeLayout currentServerBtn;
    TextView server_ip,
            selectedServerTextView,
            tv_uploaded_traffic,
            tv_downloaded_traffic,
            connectionStateTextView;
    ImageView country_flag,img_connect, img_settings;
    LinearLayout premium;
    Preference preference;


    boolean mSubscribedToDelaroy = false;
    boolean connected = false;
    String mDelaroySku = "";
    boolean mAutoRenewEnabled = false;


    int[] Onconnect = {R.drawable.svgswitchon};
    int[] Ondisconnect = {R.drawable.svg_switch};
    private final Handler mUIHandler = new Handler(Looper.getMainLooper());

    final Runnable mUIUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            updateUI();
            checkRemainingTraffic();
            mUIHandler.postDelayed(mUIUpdateRunnable, 10000);
        }
    };


    protected abstract void isLoggedIn(Callback<Boolean> callback);

    protected abstract void loginToVpn();

    protected abstract void isConnected(Callback<Boolean> callback);

    protected abstract void connectToVpn();

    protected abstract void disconnectFromVnp();

    protected abstract void chooseServer();

    protected abstract void getCurrentServer(Callback<String> callback);

    protected abstract void checkRemainingTraffic();

    void complain(String message) {
        alert("Error: " + message);
    }

    void alert(String message) {
        android.app.AlertDialog.Builder bld = new android.app.AlertDialog.Builder(this);
        bld.setMessage(message);
        bld.setNeutralButton(getString(R.string.ok), null);
        bld.create().show();
    }

    private void unlockData() {
        verifyLicense(this);
        if (mSubscribedToDelaroy) {
            unlock();
        } else {
            preference.setBooleanpreference(PRIMIUM_STATE, false);
        }
        if (!preference.isBooleenPreference(PRIMIUM_STATE)) {
            premium.setVisibility(View.VISIBLE);

        } else {
            premium.setVisibility(View.GONE);

        }


        MobileAds.initialize(this, initializationStatus -> {
            Map<String, AdapterStatus> statusMap = initializationStatus.getAdapterStatusMap();
            for (String adapterClass : statusMap.keySet()) {
                AdapterStatus status = statusMap.get(adapterClass);
                assert status != null;
                Log.d("BycomVPN", String.format(
                        "Adapter name: %s, Description: %s, Latency: %d",
                        adapterClass, status.getDescription(), status.getLatency()));
            }
            LoadBannerAd();
            LoadInterstitialAd();
        });


    }

    public void unlock() {
        preference.setBooleanpreference(PRIMIUM_STATE, true);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SplashScreen.installSplashScreen(this);
        setContentView(R.layout.activity_main);
        initViews();
        initHydraSdk();
        verifyLicense(this);




        ImageView iv_earth = findViewById(R.id.iv_earth);
        startRotatingAnimation(iv_earth);





        if(BuildConfig.USE_ONESIGNAL) {
            OneSignal.initWithContext(this,BuildConfig.ONESIGNAL_APP_ID);
            OneSignal.getNotifications().requestPermission(true, Continue.none());
        }

        preference = new Preference(this);
        preference.setStringpreference(IN_PURCHASE_KEY, BuildConfig.IN_APPKEY);
        preference.setStringpreference(One_Month_Sub, BuildConfig.MONTHLY);
        preference.setStringpreference(Six_Month_Sub, BuildConfig.SIX_MONTH);
        preference.setStringpreference(One_Year_Sub, BuildConfig.YEARLY);


        if(!BuildConfig.DEBUG) {
            String installerPackageName = getPackageManager().getInstallerPackageName(getPackageName());
            if (installerPackageName == null || !installerPackageName.equals("com.android.vending")) {
                Toast.makeText(this, getString(R.string.play_verfication_failed), Toast.LENGTH_SHORT).show();
                finish();
            }
        }


        loginToVpn();





        if (BuildConfig.USE_IN_APP_PURCHASE) {
            base64EncodedPublicKey = preference.getStringpreference(IN_PURCHASE_KEY, base64EncodedPublicKey);
            SKU_DELAROY_MONTHLY = preference.getStringpreference(One_Month_Sub, SKU_DELAROY_MONTHLY);
            SKU_DELAROY_SIXMONTH = preference.getStringpreference(Six_Month_Sub, SKU_DELAROY_SIXMONTH);
            SKU_DELAROY_YEARLY = preference.getStringpreference(One_Year_Sub, SKU_DELAROY_YEARLY);


            ArrayList<String> nonConsumableKeys = new ArrayList<>();

            ArrayList<String> consumableKeys = new ArrayList<>();

            ArrayList<String> subscriptionKeys = new ArrayList<>();
            subscriptionKeys.add(SKU_DELAROY_MONTHLY);
            subscriptionKeys.add(SKU_DELAROY_SIXMONTH);
            subscriptionKeys.add(SKU_DELAROY_YEARLY);

            IapConnector iapConnector = new IapConnector(
                    this,
                    nonConsumableKeys,
                    consumableKeys,
                    subscriptionKeys,
                    base64EncodedPublicKey,
                    true
            );

            unlockData();


            iapConnector.addSubscriptionListener(new SubscriptionServiceListener() {
                @Override
                public void onPurchaseFailed(@Nullable DataWrappers.PurchaseInfo purchaseInfo, @Nullable Integer integer) {

                }

                @Override
                public void onSubscriptionRestored(@NonNull DataWrappers.PurchaseInfo purchaseInfo) {

                }

                @Override
                public void onSubscriptionPurchased(@NonNull DataWrappers.PurchaseInfo purchaseInfo) {

                }

                @Override
                public void onPricesUpdated(@NonNull Map<String, ? extends List<DataWrappers.ProductDetails>> map) {

                }
            });




            iapConnector.addSubscriptionListener(new SubscriptionServiceListener() {
                @Override
                public void onPurchaseFailed(@Nullable DataWrappers.PurchaseInfo purchaseInfo, @Nullable Integer integer) {

                }

                @Override
                public void onSubscriptionRestored(@NonNull DataWrappers.PurchaseInfo purchaseInfo) {
                    Log.e("Subscribe", "yes" + purchaseInfo.getSku());
                    if (purchaseInfo.getSku().equals(SKU_DELAROY_MONTHLY) && purchaseInfo.isAutoRenewing()) {
                        mDelaroySku = SKU_DELAROY_MONTHLY;
                        mAutoRenewEnabled = true;
                        mSubscribedToDelaroy = true;
                    } else if (purchaseInfo.getSku().equals(SKU_DELAROY_SIXMONTH) && purchaseInfo.isAutoRenewing()) {
                        mDelaroySku = SKU_DELAROY_SIXMONTH;
                        mAutoRenewEnabled = true;
                        mSubscribedToDelaroy = true;
                    } else if (purchaseInfo.getSku().equals(SKU_DELAROY_YEARLY) && purchaseInfo.isAutoRenewing()) {
                        mDelaroySku = SKU_DELAROY_YEARLY;
                        mAutoRenewEnabled = true;
                        mSubscribedToDelaroy = true;
                    } else {
                        mDelaroySku = "";
                        mAutoRenewEnabled = false;
                        mSubscribedToDelaroy = false;
                    }

                    if (!mDelaroySku.equals("")) {
                        preference.setStringpreference(INAPPSKUUNIT, mDelaroySku);
                        preference.setLongpreference(PURCHASETIME, purchaseInfo.getPurchaseTime());
                    }
                    unlockData();

                }

                @Override
                public void onSubscriptionPurchased(@NonNull DataWrappers.PurchaseInfo purchaseInfo) {


                }

                @Override
                public void onPricesUpdated(@NonNull Map<String, ? extends List<DataWrappers.ProductDetails>> map) {

                }
            });

        } else {
            preference.setBooleanpreference(PRIMIUM_STATE, false);
            premium.setVisibility(View.GONE);


            MobileAds.initialize(this, initializationStatus -> {
                Map<String, AdapterStatus> statusMap = initializationStatus.getAdapterStatusMap();
                for (String adapterClass : statusMap.keySet()) {
                    AdapterStatus status = statusMap.get(adapterClass);
                    Log.d("MyApp", String.format(
                            "Adapter name: %s, Description: %s, Latency: %d",
                            adapterClass, status.getDescription(), status.getLatency()));
                }
                LoadInterstitialAd();
                LoadBannerAd();
            });


        }


    }

    private void startRotatingAnimation(ImageView imageView) {
        ValueAnimator animator = ValueAnimator.ofFloat(0f, 360f);
        animator.setDuration(60000);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setInterpolator(new LinearInterpolator());

        animator.addUpdateListener(animation -> {
            float rotation = (float) animation.getAnimatedValue();
            imageView.setRotation(rotation);
        });

        animator.start();
    }


    private void initViews() {
        server_ip = findViewById(R.id.server_ip);
        img_connect = findViewById(R.id.img_connect);
        connectionStateTextView = findViewById(R.id.tv_connection_status);
        currentServerBtn = findViewById(R.id.rl_select_location);
        selectedServerTextView = findViewById(R.id.selected_server);
        country_flag = findViewById(R.id.country_flag);
        tv_uploaded_traffic = findViewById(R.id.tv_uploaded_traffic);
        tv_downloaded_traffic = findViewById(R.id.tv_downloaded_traffic);
        premium = findViewById(R.id.premium);
        img_settings = findViewById(R.id.img_settings);

       // Activity activity = new egcodes.com.speedtest.MainActivity

        img_settings.setOnClickListener(view -> startActivity(new Intent(this, SettingsActivity.class)));

        premium.setOnClickListener(v -> startActivity(new Intent(UIActivity.this, GetPremiumActivity.class)));

        img_connect.setOnClickListener(v -> isConnected(new Callback<Boolean>() {
            @Override
            public void success(@NonNull Boolean aBoolean) {
                if (aBoolean) {
                    disconnectFromVnp();
                } else {
                    connectToVpn();
                }
            }
            @Override
            public void failure(@NonNull VpnException e) {
            }
        }));

        currentServerBtn.setOnClickListener(v -> chooseServer());

    }
    public void initHydraSdk() {
        createNotificationChannel();


        ClientInfo clientInfo = UnifiedSDK.getClientInfo(this);



        List<TransportConfig> transportConfigList = new ArrayList<>();
        transportConfigList.add(HydraTransportConfig.create());

        UnifiedSdk.update(transportConfigList, CompletableCallback.EMPTY);
        UnifiedSdkConfig config = UnifiedSDK.getAccessConfig(this);
        UnifiedSdk.getInstance(clientInfo, config);
        SdkNotificationConfig notificationConfig = SdkNotificationConfig.newBuilder()
                .title(getResources().getString(R.string.app_name))
                .channelId(getPackageName())
                .build();
        UnifiedSdk.update(notificationConfig);


    }

    private void createNotificationChannel() {
        CharSequence name = getResources().getString(R.string.app_name);
        String description = getResources().getString(R.string.app_name)+getString(R.string.notify);
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel(getPackageName(), name, importance);
        channel.setDescription(description);
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    public void setCurrentIP(){
        new Thread(() -> {
            try {
                URL url = new URL("https://checkip.amazonaws.com");
                BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
                String ipAddress = reader.readLine();
                runOnUiThread(() -> ShowIP(ipAddress));
            } catch (IOException e) {
                e.printStackTrace();
            }





         /*   try {
                URL url = new URL("https://checkip.amazonaws.com/");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String ipAddress = reader.readLine();
                    runOnUiThread(() -> ShowIP(ipAddress));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
*/




        }).start();
    }


    @Override
    public void setTitle(CharSequence title) {
        getSupportActionBar().setTitle(title);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }


    @Override
    protected void onResume() {
        super.onResume();
        isConnected(new Callback<Boolean>() {
            @Override
            public void success(@NonNull Boolean aBoolean) {
                if (aBoolean) {
                    startUIUpdateTask();
                }
            }

            @Override
            public void failure(@NonNull VpnException e) {

            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopUIUpdateTask();
    }

    protected void startUIUpdateTask() {
        stopUIUpdateTask();
        mUIHandler.post(mUIUpdateRunnable);
    }

    protected void stopUIUpdateTask() {
        mUIHandler.removeCallbacks(mUIUpdateRunnable);
        updateUI();
    }


    protected void updateUI() {
        unified.vpn.sdk.UnifiedSdk.getVpnState(new Callback<VpnState>() {
            @Override
            public void success(@NonNull VpnState vpnState) {
                switch (vpnState) {
                    case IDLE: {
                        VPN_CONNECTED = false;
                        Log.e(TAG, "success: IDLE");
                        connectionStateTextView.setText(R.string.disconnected);
                        setCurrentIP();
                        if (connected) {
                            connected = false;
                            animate(img_connect, Ondisconnect, 0, false);
                        }



                        updateTileState(false);





                        /*country_flag.setImageDrawable(getResources().getDrawable(R.drawable.ic_earth));
                        selectedServerTextView.setText(R.string.select_country);*/
                        ChangeBlockVisibility();
                        tv_uploaded_traffic.setText("0 B");
                        tv_downloaded_traffic.setText("0 B");

                        hideConnectProgress();
                        break;
                    }
                    case CONNECTED: {
                        VPN_CONNECTED = true;
                        Log.e(TAG, "success: CONNECTED");
                        if (!connected) {
                            connected = true;
                            animate(img_connect, Onconnect, 0, false);
                        }
                        connectionStateTextView.setText(R.string.connected);
                        hideConnectProgress();
                        updateTileState(true);
                        break;
                    }
                    case CONNECTING_VPN:
                    case CONNECTING_CREDENTIALS:
                    case CONNECTING_PERMISSIONS: {
                        connectionStateTextView.setText(R.string.connecting);
                        ChangeBlockVisibility();
                        /*country_flag.setImageDrawable(getResources().getDrawable(R.drawable.ic_earth));
                        selectedServerTextView.setText(R.string.select_country);*/


                       /* img_connect.setImageDrawable(ContextCompat.getDrawable(UIActivity.this, R.drawable.avd_anim));
                        final AnimatedVectorDrawable animatedVectorDrawable = (AnimatedVectorDrawable) img_connect.getDrawable();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            animatedVectorDrawable.registerAnimationCallback(new Animatable2.AnimationCallback() {
                                @Override
                                public void onAnimationEnd(Drawable drawable) {
                                    animatedVectorDrawable.start(); // Start the animation again when it finishes
                                }
                            });
                        }
                        animatedVectorDrawable.start();*/





                        showConnectProgress();
                        break;
                    }
                    case PAUSED: {
                        Log.e(TAG, "success: PAUSED");
                        ChangeBlockVisibility();
                        /*country_flag.setImageDrawable(getResources().getDrawable(R.drawable.ic_earth));
                        selectedServerTextView.setText(R.string.select_country);*/
                        break;
                    }
                }
            }

            @Override
            public void failure(@NonNull VpnException e) {
                country_flag.setImageDrawable(getResources().getDrawable(R.drawable.ic_earth));
                selectedServerTextView.setText(R.string.select_country);
                updateTileState(false);
            }
        });
        getCurrentServer(new Callback<String>() {
            @Override
            public void success(@NonNull final String currentServer) {
                runOnUiThread(() -> {
                   /* country_flag.setImageDrawable(getResources().getDrawable(R.drawable.ic_earth));
                    selectedServerTextView.setText(R.string.select_country);*/
                    if (!currentServer.equals("")) {
                        Locale locale = new Locale("", currentServer);
                        Resources resources = getResources();
                        String sb = "drawable/" + currentServer.toLowerCase();
                        country_flag.setImageResource(resources.getIdentifier(sb, null, getPackageName()));
                        selectedServerTextView.setText(locale.getDisplayCountry());
                        //selectedCountry = currentServer;
                    } else {
                        /*country_flag.setImageDrawable(getResources().getDrawable(R.drawable.ic_earth));
                        selectedServerTextView.setText(R.string.select_country);*/
                    }
                });
            }

            @Override
            public void failure(@NonNull VpnException e) {
                country_flag.setImageDrawable(getResources().getDrawable(R.drawable.ic_earth));
                selectedServerTextView.setText(R.string.select_country);
            }
        });
    }



    private void ChangeBlockVisibility() {
        if (BuildConfig.USE_IN_APP_PURCHASE) {
            if (preference.isBooleenPreference(PRIMIUM_STATE)) {
                premium.setVisibility(View.GONE);
            } else {
                premium.setVisibility(View.VISIBLE);
            }
        } else {
            premium.setVisibility(View.GONE);
        }
    }

    private void animate(final ImageView imageView, final int images[], final int imageIndex, final boolean forever) {


        int fadeInDuration = 500;
        int timeBetween = 3000;
        int fadeOutDuration = 1000;

        imageView.setVisibility(View.VISIBLE);
        imageView.setImageResource(images[imageIndex]);

        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new DecelerateInterpolator());
        fadeIn.setDuration(fadeInDuration);

        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new AccelerateInterpolator());
        fadeOut.setStartOffset(fadeInDuration + timeBetween);
        fadeOut.setDuration(fadeOutDuration);

        AnimationSet animation = new AnimationSet(false);
        animation.addAnimation(fadeIn);

        animation.setRepeatCount(1);
        imageView.setAnimation(animation);

        animation.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationEnd(Animation animation) {
                if (images.length - 1 > imageIndex) {
                    animate(imageView, images, imageIndex + 1, forever); //Calls itself until it gets to the end of the array
                } else {
                    if (forever) {
                        animate(imageView, images, 0, forever);  //Calls itself to start the animation all over again in a loop if forever = true
                    }
                }
            }

            public void onAnimationRepeat(Animation animation) {
            }

            public void onAnimationStart(Animation animation) {
            }
        });
    }


    protected void updateTrafficStats(long outBytes, long inBytes) {
        String outString = Utils.humanReadableByteCountOld(outBytes, false);
        String inString = Utils.humanReadableByteCountOld(inBytes, false);

        tv_uploaded_traffic.setText(outString);
        tv_downloaded_traffic.setText(inString);

        checkRemainingTraffic();

    }

    protected void updateRemainingTraffic(RemainingTraffic remainingTrafficResponse) {
        if (remainingTrafficResponse.isUnlimited()) {
            //Toast.makeText(this, "Unlimited", Toast.LENGTH_SHORT).show();
        } else {



            TextView tv_limit = findViewById(R.id.tv_traffic_limit);

            String trafficUsed = Utils.megabyteCount(remainingTrafficResponse.getTrafficUsed()) + "MB";
            String trafficLimit = Utils.megabyteCount(remainingTrafficResponse.getTrafficLimit()) + "MB";


            tv_limit.setText("Daily Limit: " + trafficUsed + "/" + trafficLimit);



        }
    }

    protected void ShowIP(String ipaddress) {
        server_ip.setText(ipaddress);
    }


    protected void showConnectProgress() {

    }

    protected void hideConnectProgress() {

    }

    protected void showMessage(String msg) {
        Toast.makeText(UIActivity.this, msg, Toast.LENGTH_SHORT).show();
    }


    public void LoadBannerAd() {
        RelativeLayout adContainer = findViewById(R.id.adView);
        if (BuildConfig.GOOGlE_AD) {
            AdMod.buildAdBanner(getApplicationContext(), adContainer, 0, new AdMod.MyAdListener() {
                @Override
                public void onAdClicked() {
                }

                @Override
                public void onAdClosed() {
                }

                @Override
                public void onAdLoaded() {
                }

                @Override
                public void onAdOpened() {
                }

                @Override
                public void onFailedToLoad(int i) {
                }
            });
        }
    }

    private void LoadInterstitialAd() {
        if (BuildConfig.GOOGlE_AD) {
            Preference preference = new Preference(UIActivity.this);
            if (!preference.isBooleenPreference(BillConfig.PRIMIUM_STATE)) {
                AdRequest adRequest = new AdRequest.Builder().build();
                InterstitialAd.load(this, (BuildConfig.GOOGLE_INTERSTITIAL), adRequest, new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        // The mInterstitialAd reference will be null until
                        // an ad is loaded.
                        mInterstitialAd = interstitialAd;
                        Log.e(TAG, "onAdLoaded");
                        mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                            @Override
                            public void onAdDismissedFullScreenContent() {
                                // Called when fullscreen content is dismissed.
                                Log.e("TAG", "The ad was dismissed.");

                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(AdError adError) {
                                // Called when fullscreen content failed to show.
                                Log.e("TAG", "The ad failed to show.");
                            }

                            @Override
                            public void onAdShowedFullScreenContent() {
                                mInterstitialAd = null;
                            }
                        });
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        Log.e(TAG, loadAdError.getMessage());
                        mInterstitialAd = null;
                    }
                });
            }
        }
    }


    public void showInterstial() {
        if (mInterstitialAd != null) {
            mInterstitialAd.show(UIActivity.this);
        }
    }
}