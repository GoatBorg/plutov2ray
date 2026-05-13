
        package com.bycomsolutions.bycomvpn.activities;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bycomsolutions.bycomvpn.R;
import com.facebook.shimmer.ShimmerFrameLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

@SuppressWarnings({"SpellCheckingInspection"})
public class SpeedTestActivity2 extends AppCompatActivity{

    TextView pingTextView,tv_speed,tv_download_speed,tv_upload_speed,tv_server;
    ShimmerFrameLayout pingShimmer,downloadShimmer,uploadShimmer,serverShimmer;
    Button startButton;
    ImageView iv_speed_bar;
    DecimalFormat dec;
    int position,lastPosition;



    Float previousDownloadSpeed = 0.00f;

    Float previousUploadSpeed = 0.00f;



    Boolean pingTestRunning = false;
    Boolean downloadTestRunning = false;
    Boolean uploadTestRunning = false;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speed_test);

        pingTextView = findViewById(R.id.tv_ping);
        startButton = findViewById(R.id.startButton);
        pingShimmer = findViewById(R.id.pingShimmer);
        downloadShimmer = findViewById(R.id.downloadShimmer);
        uploadShimmer = findViewById(R.id.uploadShimmer);
        serverShimmer = findViewById(R.id.serverShimmer);
        tv_speed = findViewById(R.id.tv_speed);
        iv_speed_bar = findViewById(R.id.iv_speed_bar);
        tv_download_speed = findViewById(R.id.tv_download_speed);
        tv_upload_speed = findViewById(R.id.tv_upload_speed);
        tv_server = findViewById(R.id.tv_server);

        pingShimmer.hideShimmer();
        downloadShimmer.hideShimmer();
        uploadShimmer.hideShimmer();



        dec = new DecimalFormat("0.00", new DecimalFormatSymbols(Locale.US));




        WebView webView = findViewById(R.id.webview);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
      /*  webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

            }
        });*/


        webView.loadUrl("https://interprefy.speedtestcustom.com/");
        fetchOptimalServer(webView);



        startButton.setEnabled(false);
        startButton.setOnClickListener(v -> {
            pingShimmer.showShimmer(true);
            startButton.setEnabled(false);
            startButton.setText("Testing");
            startSpeedTest(webView);

        });





    }

    private void startSpeedTest(WebView webView) {
        pingTestRunning = true;
        webView.evaluateJavascript(
                "(function() {" +
                        "   var button = document.querySelector('.button.background-primary-hover.text-primary');" +
                        "   button.click();" + // Click the button
                        "})();",
                value -> Log.d("WebView", "GO button clicked")
        );


        FetchValues(webView);




    }


    private void FetchValues(WebView webView) {
        // Run JavaScript at a regular interval of 500ms to fetch Ping, Jitter, Download, and Upload values
        webView.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Check if the "Run your Speedtest Again" button is visible
                webView.evaluateJavascript(
                        "(function() {" +
                                "   var runAgainButton = document.querySelector('.share-button--go');" +
                                "   return runAgainButton && runAgainButton.offsetParent !== null;" +
                                "})()",
                        new ValueCallback<String>() {
                            @Override
                            public void onReceiveValue(String isVisible) {
                                // Parse the result, isVisible will be "true" if the button is visible, else "false"
                                boolean runAgainVisible = isVisible.equals("true");

                                if (!runAgainVisible) {
                                    // Fetch the values if the "Run your Speedtest Again" button is not visible
                                    webView.evaluateJavascript(
                                            "(function() {" +
                                                    "   var pingValue = document.querySelector('.results-latency .result-tile-ping .result-value .number span');" +
                                                    "   var jitterValue = document.querySelector('.results-latency .result-tile-jitter .result-value .number span');" +
                                                    "   var downloadValue = document.querySelector('.results-speed .result-tile-download .result-value .number span');" +
                                                    "   var uploadValue = document.querySelector('.results-speed .result-tile-upload .result-value .number span');" +
                                                    "   var result = {" +
                                                    "       ping: pingValue ? pingValue.innerText.trim() : null," +
                                                    "       jitter: jitterValue ? jitterValue.innerText.trim() : null," +
                                                    "       download: downloadValue ? downloadValue.innerText.trim() : null," +
                                                    "       upload: uploadValue ? uploadValue.innerText.trim() : null" +
                                                    "   };" +
                                                    "   return result;" +
                                                    "})()",
                                            value -> {
                                                // This callback will run when the JavaScript is evaluated
                                                Log.d("WebView", "Fetched values: " + value);

                                                // Store the fetched values in separate string variables
                                                storeFetchedValues(value);

                                                // Continue the loop if the "Run your Speedtest Again" button is not visible
                                                FetchValues(webView);
                                            }
                                    );
                                } else {
                                    onSpeedTestFinished();
                                }
                            }
                        }
                );
            }
        }, 500);  // Delay of 500ms before starting to fetch values after clicking the button
    }


    private void onSpeedTestFinished(){
        updateSpeedMeter(0.00);
        uploadShimmer.hideShimmer();
        startButton.setEnabled(true);
        startButton.setText("Done");
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    // Function to store the fetched values into separate string variables
    private void storeFetchedValues(String jsonValues) {
        try {
            // Parsing the JSON values
            JSONObject jsonObject = new JSONObject(jsonValues);
            String pingValue = jsonObject.getString("ping");
            String jitterValue = jsonObject.getString("jitter");
            String downloadValue = jsonObject.getString("download");
            String uploadValue = jsonObject.getString("upload");



            updateUi(pingValue,jitterValue,downloadValue,uploadValue);



            // Log the values to confirm they're saved correctly
            Log.d("WebView", "Ping: " + pingValue);
            Log.d("WebView", "Jitter: " + jitterValue);
            Log.d("WebView", "Download: " + downloadValue);
            Log.d("WebView", "Upload: " + uploadValue);

        } catch (JSONException e) {
            Log.e("WebView", "Error parsing fetched values", e);
        }
    }


    private void updateUi(String pingValue,String jitterValue,String downloadValue,String uploadValue){
        if(!pingValue.equals("null") && pingTestRunning){
            pingTestRunning = false;
            downloadTestRunning = true;
            pingShimmer.hideShimmer();
            pingTextView.setText("Connection Ping: " + pingValue+" ms");
            downloadShimmer.showShimmer(true);
        }

        if(!downloadValue.equals("null") && downloadTestRunning) {
            updateDownloadText(Float.parseFloat(downloadValue));
        }


        if(!uploadValue.equals("null")) {
            downloadTestRunning = false;
            downloadShimmer.hideShimmer();
            uploadShimmer.showShimmer(true);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    uploadTestRunning = true;

                }
            },1000);
            if(uploadTestRunning) {
                updateUploadText(Float.parseFloat(uploadValue));
            }else updateSpeedMeter(0.00);
        }




    }
    public void updateSpeedMeter(Double downloadSpeed){
        updateSpeedMeterText(String.valueOf(downloadSpeed));
        position = getRotatePosition(downloadSpeed);
        RotateAnimation rotate = new RotateAnimation(lastPosition, position, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotate.setInterpolator(new LinearInterpolator());
        rotate.setFillAfter(true);
        rotate.setDuration(500);
        iv_speed_bar.startAnimation(rotate);
        lastPosition = position;
    }

    public void updateSpeedMeterText(String downloadSpeed){
        float startValue = Float.parseFloat(tv_speed.getText().toString());
        float endValue = Float.parseFloat(downloadSpeed);
        ValueAnimator animator = ValueAnimator.ofFloat(startValue,endValue);
        animator.setInterpolator(new LinearInterpolator());
        animator.setDuration(500);
        animator.addUpdateListener(animation -> tv_speed.setText(dec.format(animation.getAnimatedValue())));
        animator.start();
    }

    public int getRotatePosition(double speed) {
        double position;
        if(speed <= 1) position = speed * 30;
        else if(speed <= 5) position = (speed * 7.5) + 22.5;
        else if(speed <= 10) position = (speed * 6) + 30;
        else if(speed <= 30) position = (speed * 3) + 60;
        else if(speed <= 50) position = (speed * 1.5) + 105;
        else if(speed <= 100) position = (speed * 1.2) + 120;
        else position = 240;
        return (int) position;
    }
    private void updateDownloadText(float speed) {
        updateSpeedMeter((double) speed);
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(previousDownloadSpeed,speed);
        valueAnimator.setDuration(500);
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.addUpdateListener(animation -> tv_download_speed.setText(dec.format(animation.getAnimatedValue())));
        valueAnimator.start();
        previousDownloadSpeed = speed;
    }

    private void updateUploadText(float speed) {
        updateSpeedMeter((double) speed);
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(previousUploadSpeed, speed);
        valueAnimator.setDuration(500);
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.addUpdateListener(animation -> tv_upload_speed.setText(dec.format(animation.getAnimatedValue())));
        valueAnimator.start();
        previousUploadSpeed = speed;
    }



    private void fetchOptimalServer(WebView webView) {
        Log.e("dfdslfjk","Loading");
        // JavaScript code to extract the values
        String javascriptCode = "(function() { " +
                "var serverElement = document.querySelector('.host-list__link span');" +
                "var locationElement = document.querySelector('.host-list__item__location span');" +
                "if (serverElement && locationElement) {" +
                "   return serverElement.textContent.trim() + ' • ' + locationElement.textContent.trim();" +
                "} else {" +
                "   return null;" +
                "}" +
                "})();";

        // Run the JavaScript periodically using postDelayed directly on the WebView
        webView.postDelayed(() -> {
            webView.evaluateJavascript(javascriptCode, value -> {
                if (value != null && !value.equals("null")) {
                    String result = value.replace("\"", "");

                    tv_server.setText(result);
                    tv_server.setAlpha(0.5f);
                    serverShimmer.hideShimmer();
                    startButton.setEnabled(true);



                } else {
                    // Schedule the next check if the value is not found yet
                    fetchOptimalServer(webView);
                }
            });
        }, 500); // Check every 500ms
    }

}
