package com.bycomsolutions.bycomvpn.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.bycomsolutions.bycomvpn.R;

public class GetPremiumActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_premium);

        // GoatBorg VIP: Geri butonu dışında her şeyi etkisiz hale getirdik.
        ImageView backButton = findViewById(R.id.img_back);
        if (backButton != null) {
            backButton.setOnClickListener(view -> finish());
        }

        // Kanki burada normalde satın alma butonları olurdu, 
        // şimdi hepsi sadece birer görsel olarak kalacak ya da tıklansa da işlem yapmayacak.
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
