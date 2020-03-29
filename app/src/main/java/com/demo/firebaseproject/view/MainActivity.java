package com.demo.firebaseproject.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.demo.firebaseproject.R;
import com.demo.firebaseproject.model.MockInventory;
import com.demo.firebaseproject.model.Product;
import com.demo.firebaseproject.model.Section;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static android.widget.LinearLayout.HORIZONTAL;
import static com.demo.firebaseproject.model.Section.NEW_ARRIVAL;
import static com.demo.firebaseproject.model.Section.RECOMMENDATION;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    // Remote Config Parameter Key
    private Context context;
    List<String> PREDICTIONS_KEYS = Arrays.asList(
            "prediction1", "prediction2", "prediction3" ,"prediction4", "prediction5");
    private FirebaseAnalytics mFirebaseAnalytics;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;

    public static void navigate(Activity activity) {
        Intent intent = new Intent(activity, MainActivity.class);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView newArrivalView = findViewById(R.id.new_arrivals_view);
        renderProducts(NEW_ARRIVAL, newArrivalView);
        RecyclerView recommendView = findViewById(R.id.recommendation_view);
        renderProducts(RECOMMENDATION, recommendView);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        mFirebaseRemoteConfig.setDefaultsAsync(R.xml.remote_config_defaults);
        mFirebaseRemoteConfig.fetchAndActivate()
                .addOnCompleteListener((Activity) context, task -> {
                    if (task.isSuccessful()) {
                        Boolean updated = task.getResult();
                        Log.d(TAG, "Config params updated: " + updated);
                        Toast.makeText(MainActivity.this, "Fetch and activate succeeded",
                                Toast.LENGTH_SHORT).show();

                        for(String rcKey : PREDICTIONS_KEYS) {
                            String pEvent = mFirebaseRemoteConfig.getString(rcKey);
                            if(!pEvent.isEmpty()) {
                                Log.d(TAG, "Remote Config key: " + rcKey + "; pEvent name: " + pEvent);
                                mFirebaseAnalytics.logEvent(pEvent, new Bundle());
                            }
                        }

                    } else {
                        Toast.makeText(MainActivity.this, "Fetch failed",
                                Toast.LENGTH_SHORT).show();
                    }

                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // settings is the only option for now
        new SizeSelectionDialog(this).show();
        return true;
    }

    private void renderProducts(Section section, RecyclerView view) {
        view.setLayoutManager(new LinearLayoutManager(this, HORIZONTAL, false));
        Collection<Product> products = new MockInventory().productsForSection(section);
        ProductListAdapter adapter = new ProductListAdapter(products, R.layout.product_as_icons);
        view.setAdapter(adapter);
    }

    public void cartBtnClick(View v) {
        CartActivity.navigate(this);
    }

    public void recommendClick(View v) {
        SectionActivity.navigateToSectionPage(this, RECOMMENDATION);
    }

    public void newArrClick(View v) {
        SectionActivity.navigateToSectionPage(this, NEW_ARRIVAL);
    }
}