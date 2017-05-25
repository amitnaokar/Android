package com.dailysofts.mp3downloader.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.dailysofts.ConnectivityReceiver;
import com.dailysofts.MyApplication;
import com.dailysofts.mp3downloader.R;
import com.dailysofts.parser.ParserConfig;
import com.dailysofts.parser.ParserUtils;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.startapp.android.publish.ads.splash.SplashConfig;
import com.startapp.android.publish.adsCommon.AutoInterstitialPreferences;
import com.startapp.android.publish.adsCommon.SDKAdPreferences;
import com.startapp.android.publish.adsCommon.StartAppAd;
import com.startapp.android.publish.adsCommon.StartAppSDK;

public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback, ConnectivityReceiver.ConnectivityReceiverListener{
    private Toolbar toolbar;
    private Intent intent;
    private static final int REQUEST_EXTERNAL_STORAGE = 0;
    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*startApp initialization*/
        StartAppSDK.init(this, this.getApplication().getResources().getString(R.string.startAppId), true);
        StartAppAd.showSplash(this, savedInstanceState,
                new SplashConfig()
                        .setTheme(SplashConfig.Theme.SKY)
                        .setAppName("")
                        .setLogo(R.mipmap.ic_launcher)
                        .setOrientation(SplashConfig.Orientation.PORTRAIT)
        );
        StartAppAd.enableAutoInterstitial();
        StartAppAd.setAutoInterstitialPreferences(new AutoInterstitialPreferences().setSecondsBetweenAds(60));
        StartAppAd.setAutoInterstitialPreferences(new AutoInterstitialPreferences().setActivitiesBetweenAds(3));

        setContentView(R.layout.activity_main);

        /*admob load add*/
        mAdView = (AdView) findViewById(R.id.adMobFooterBanner);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
//        getSupportActionBar().setDisplayShowHomeEnabled(true);
        /*default setting*/
        ((RadioButton)(findViewById(R.id.songRadioButton))).setChecked(true);
        intent = new Intent(this,SongSearchActivity.class);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestStoragePermission();
            }
        }

    }

    @Override
    public void onBackPressed() {
        StartAppAd.onBackPressed(this);
        super.onBackPressed();
    }

    private void requestStoragePermission() {
        // External Storage permission has not been granted yet. Request it directly.
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},REQUEST_EXTERNAL_STORAGE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public void radioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        switch (view.getId()) {
            case R.id.songRadioButton:
                if (checked) {
                    intent = new Intent(this,SongSearchActivity.class);
                }
                break;

            case R.id.singerRadioButton:
                if(checked){
                    intent = new Intent(this,SingerSearchActivity.class);
                }
                break;

            case R.id.albumRadioButton:
                if(checked){
                    intent = new Intent(this,AlbumSearchActivity.class);
                }
                break;
            default:
                intent = new Intent(this,SongSearchActivity.class);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onResume() {
        MyApplication.getInstance().trackScreenView("Main");
        //register connection status listener
        MyApplication.getInstance().setConnectivityListener(this);
        super.onResume();
        if (mAdView != null) {
            mAdView.resume();
        }
    }

    @Override
    protected void onPause() {
        if (mAdView != null) {
            mAdView.pause();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
        super.onDestroy();
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        ParserUtils.showSnack(isConnected,this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_share) {
            ParserUtils.shareApp(this);
        }

        if(id == R.id.action_rating){
            ParserUtils.rateApp(this);
        }

        if(id == R.id.action_email){
            ParserUtils.emailUs(this);
        }

        return super.onOptionsItemSelected(item);
    }

    public void searchClicked(View view) {
        EditText searchEditText = (EditText) findViewById(R.id.queryString);
        String searchText = searchEditText.getText().toString();
        if(!ConnectivityReceiver.isConnected()){
            Toast.makeText(this, "Please check internet connectivity", Toast.LENGTH_SHORT).show();
        }else if (searchText.trim().isEmpty()) {
            searchEditText.setError("Enter keyword to search");
            if(searchEditText.requestFocus())
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }else{
            SongSearchActivity.clearSearchResult();
            AlbumSearchActivity.clearSearchResult();
            AlbumSongListActivity.clearSearchResult();
            SingerSearchActivity.clearSearchResult();
            SingerSongListActivity.clearSearchResult();

            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra(ParserConfig.SEARCH_KEYWORD, searchText);
            intent.putExtra(ParserConfig.PAGE_NUMBER, 1);
            startActivity(intent);
        }
    }
}
