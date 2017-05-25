package com.dailysofts.mp3downloader.activities;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.dailysofts.ConnectivityReceiver;
import com.dailysofts.MyApplication;
import com.dailysofts.mp3downloader.R;
import com.dailysofts.parser.ParserConfig;
import com.dailysofts.parser.ParserUtils;
import com.dailysofts.parser.SongParser;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import org.json.simple.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.helper.StringUtil;

public class SongDownloadActivity extends ActionBarActivity implements ConnectivityReceiver.ConnectivityReceiverListener,
        ActivityCompat.OnRequestPermissionsResultCallback {
    private static final int EXTERNAL_STORAGE_PERMISSION_CONSTANT = 100;
    private static final int REQUEST_PERMISSION_SETTING = 101;

    private Toolbar toolbar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private boolean requestComplete;
    private String songDetailsUrl;
    private SongDetailsTask songDetailsTask;

    ImageView songImageView;
    TextView songNameView;
    TextView songSizeTxtView;
    TextView songSizeNormalView;
    TextView songSizeHighView;
    TextView singerNameView;
    TextView albumNameView;
    Button downloadNormalQualityButton;
    Button downloadHighQualityButton;
    LinearLayout songDetailsLayout;
    Activity mActivity;

    private JSONObject songObject;
    private String downloadUrlNormalQuality;
    private String downloadSizeNormalQuality;
    private String downloadUrlHighQuality;
    private String downloadSizeHighQuality;
    private String downloadAction;
    private ProgressBar progressBar;
    private AdView mAdView;

    public void setSongObject(JSONObject obj){
        this.songObject = obj;
    }

    public JSONObject getSongObject(){
        return  this.songObject;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_download);

        /*admob load add*/
        mAdView = (AdView) findViewById(R.id.adMobHeaderBanner);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        mActivity = this;
        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.songDownloadTitle);
//        getSupportActionBar().setDisplayShowHomeEnabled(true);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
//        songImageView = (ImageView)findViewById(R.id.songImage);
        songNameView = (TextView)findViewById(R.id.songName);
        songSizeTxtView = (TextView)findViewById(R.id.songSizeText);
        songSizeNormalView = (TextView)findViewById(R.id.songSizeNormal);
        songSizeHighView = (TextView)findViewById(R.id.songSizeHigh);
        singerNameView = (TextView)findViewById(R.id.singerName);
        albumNameView = (TextView)findViewById(R.id.albumName);

        downloadNormalQualityButton = (Button)findViewById(R.id.downloadNormalQuality);
        downloadNormalQualityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadSong(v);
            }
        });
        downloadHighQualityButton = (Button)findViewById(R.id.downloadHighQuality);
        downloadHighQualityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadSong(v);
            }
        });

        songDetailsLayout = (LinearLayout)findViewById(R.id.songDetailsLayout);
        swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new RefreshViewListener());

        if (!ConnectivityReceiver.isConnected()) {
            Toast.makeText(mActivity, "Please check internet connectivity", Toast.LENGTH_LONG).show();
        } else {
            songDetailsUrl = getIntent().getStringExtra(ParserConfig.SONG_URL);
            songDetailsTask = new SongDetailsTask(false);
            songDetailsTask.execute();
        }
    }

    @Override
    protected void onResume() {
        MyApplication.getInstance().trackScreenView("Download Song");
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
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

        if (id == android.R.id.home) {
            super.onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    private void downloadSong(View view){
        if (!ConnectivityReceiver.isConnected()) {
            Toast.makeText(mActivity, "Please check internet connectivity", Toast.LENGTH_LONG).show();
        } else if(view.getId()==R.id.downloadHighQuality) {
            downloadAction = ParserConfig.DOWNLOAD_ACTION_320;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},EXTERNAL_STORAGE_PERMISSION_CONSTANT);
            }else {
                downloadHighQuality();
            }
        } else if(view.getId()==R.id.downloadNormalQuality){
            downloadAction = ParserConfig.DOWNLOAD_ACTION_128;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, EXTERNAL_STORAGE_PERMISSION_CONSTANT);
            }else {
                downloadNormalQuality();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == EXTERNAL_STORAGE_PERMISSION_CONSTANT) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if(downloadAction.equals(ParserConfig.DOWNLOAD_ACTION_320)){
                    downloadHighQuality();
                }else if(downloadAction.equals(ParserConfig.DOWNLOAD_ACTION_128)){
                    downloadNormalQuality();
                }
            }else if (grantResults[0] == PackageManager.PERMISSION_DENIED){
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    //Show an explanation to the user *asynchronously*
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage("This permission is required to download songs.")
                            .setTitle("Need Storage Permission");
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            ActivityCompat.requestPermissions(mActivity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, EXTERNAL_STORAGE_PERMISSION_CONSTANT);
                        }
                    });
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, EXTERNAL_STORAGE_PERMISSION_CONSTANT);
                }else{
                    //Never ask again and handle your app without permission.
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage("This permission is required to download songs.")
                            .setTitle("Need Storage Permission");
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", getPackageName(), null);
                            intent.setData(uri);
                            startActivityForResult(intent, REQUEST_PERMISSION_SETTING);
                            Toast.makeText(getBaseContext(), "Go to Permissions to Grant Storage", Toast.LENGTH_LONG).show();
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    builder.show();
                }
            }
        }
    }

    /*onRefreshListener*/
    class RefreshViewListener implements SwipeRefreshLayout.OnRefreshListener{
        @Override
        public void onRefresh() {
            if (!ConnectivityReceiver.isConnected()) {
                Toast.makeText(mActivity, "Please check internet connectivity", Toast.LENGTH_LONG).show();
                swipeRefreshLayout.setRefreshing(false);
            } else if(requestComplete) {
                swipeRefreshLayout.setRefreshing(false);
                songDetailsTask = new SongDetailsTask(true);
                songDetailsTask.execute();
            }
        }
    }

    /*async task to download songs*/
    class SongDetailsTask extends AsyncTask<String, Integer, JSONObject> {
        Boolean isRefreshViewEvent;

        public SongDetailsTask(Boolean isRefreshViewEvent){
            this.isRefreshViewEvent = isRefreshViewEvent;
        }

        @Override
        protected void onPreExecute() {
            requestComplete = false;
            songDetailsLayout.setVisibility(View.GONE);
            if(isRefreshViewEvent) {
                swipeRefreshLayout.setRefreshing(true);
            }else{
                progressBar.setVisibility(View.VISIBLE);
            }
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            requestComplete = true;
            if(getSongObject()!=null && getSongObject().size()>0) {
                songDetailsLayout.setVisibility(View.VISIBLE);
            }
            if(isRefreshViewEvent) {
                swipeRefreshLayout.setRefreshing(false);
            }else{
                progressBar.setVisibility(View.GONE);
            }
            super.onPostExecute(result);
        }

        @Override
        protected JSONObject doInBackground(String... params) {
            JSONObject songDetails= SongParser.parseProductPage(songDetailsUrl);
            setSongObject(songDetails);
            setSongDetails();
            return songDetails;
        }
    }

    public void setSongDetails(){
        if(getSongObject()!=null){
            final String songName = getSongObject().get(ParserConfig.SONG_NAME).toString();
            final String singerName = getSongObject().get(ParserConfig.SINGER_NAME).toString();
            final String albumName = getSongObject().get(ParserConfig.ALBUM_NAME).toString();
            downloadUrlNormalQuality = getSongObject().get(ParserConfig.DOWNLOAD_URL_128).toString();
            downloadSizeNormalQuality = getSongObject().get(ParserConfig.DOWNLOAD_SIZE_128).toString();
            downloadUrlHighQuality = getSongObject().get(ParserConfig.DOWNLOAD_URL_320).toString();
            downloadSizeHighQuality = getSongObject().get(ParserConfig.DOWNLOAD_SIZE_320).toString();

            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(StringUtil.isBlank(songName)){
                        songNameView.setVisibility(View.GONE);
                    }else {
                        songNameView.setText(Html.fromHtml(mActivity.getResources().getString(R.string.songBoldTxt,songName)));
                    }
                    songSizeTxtView.setText(Html.fromHtml(mActivity.getResources().getString(R.string.sizeBoldTxt,"")));
                    if(StringUtil.isBlank(downloadSizeNormalQuality)){
                        songSizeNormalView.setVisibility(View.GONE);
                    }else {
                        songSizeNormalView.setText(downloadSizeNormalQuality);
                    }
                    if(StringUtil.isBlank(downloadSizeHighQuality)){
                        songSizeHighView.setVisibility(View.GONE);
                    }else {
                        songSizeHighView.setText(downloadSizeHighQuality);
                    }
                    if(StringUtil.isBlank(singerName)){
                        singerNameView.setVisibility(View.GONE);
                    }else {
                        singerNameView.setText(Html.fromHtml(mActivity.getResources().getString(R.string.singerBoldTxt,singerName)));
                    }
                    if(StringUtil.isBlank(albumName)){
                        albumNameView.setVisibility(View.GONE);
                    }else {
                        albumNameView.setText(Html.fromHtml(mActivity.getResources().getString(R.string.albumBoldTxt,albumName)));
                    }
                    if(StringUtil.isBlank(downloadUrlHighQuality)){
                        downloadHighQualityButton.setVisibility(View.GONE);
                    }
                }
            });
        }
    }

    /*async task to search songs*/
    class FileDownloadTask extends AsyncTask<Object, Object, Void> {
        String downloadUrl;

        public FileDownloadTask(String url){
            this.downloadUrl= url;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
        }

        @Override
        protected Void doInBackground(Object... params) {
            Connection.Response response;
            String fileUrl=null;
            boolean flag = true;
            boolean downloading =true;
            try {
                response = Jsoup.connect(downloadUrl).followRedirects(false).execute();
                if(response.hasHeader("location") && !StringUtil.isBlank(response.header("location"))){
                    fileUrl = response.header("location");
                    String fileName = ParserUtils.getFileName(fileUrl);
                    if(fileName.toLowerCase().contains("(mymp3song)")){
                        fileName = fileName.toLowerCase().replace("(mymp3song)","");
                    }else if(fileName.toLowerCase().contains("mymp3song")){
                        fileName = fileName.toLowerCase().replace("mymp3song","");
                    }
                    DownloadManager downloadManager = (DownloadManager) getSystemService(mActivity.DOWNLOAD_SERVICE);
                    DownloadManager.Request request = new DownloadManager.Request(Uri.parse(fileUrl));
                    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,fileName);
                    request.allowScanningByMediaScanner();
                    if(fileUrl.toLowerCase().endsWith(".mp3")){
                        request.setMimeType("audio/MP3");
                    }
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                    long reference = downloadManager.enqueue(request);

                    /*wait for download to complete*/
/*
                    DownloadManager.Query query = null;
                    query = new DownloadManager.Query();
                    Cursor c = null;
                    if(query!=null) {
                        query.setFilterByStatus(DownloadManager.STATUS_FAILED|DownloadManager.STATUS_PAUSED|DownloadManager.STATUS_SUCCESSFUL|DownloadManager.STATUS_RUNNING|DownloadManager.STATUS_PENDING);
                    }

                    while (downloading) {
                        c = downloadManager.query(query);
                        if(c.moveToFirst()) {
                            Log.i ("FLAG","Downloading");
                            int status =c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));

                            if (status==DownloadManager.STATUS_SUCCESSFUL) {
                                Log.i ("FLAG","done");
                                downloading = false;
                                flag=true;
                                break;
                            }
                            if (status==DownloadManager.STATUS_FAILED) {
                                Log.i ("FLAG","Fail");
                                downloading = false;
                                flag=false;
                                break;
                            }
                        }
                    }
*/
            }else{
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl));
                    startActivity(intent);
                }
            }catch(Exception e) {
                MyApplication.getInstance().trackException(e);
                if(fileUrl!=null && !StringUtil.isBlank(fileUrl)){
                    downloadUrl = fileUrl;
                }
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl));
                startActivity(intent);
            }
            return null;
        }
    }

    public void downloadNormalQuality(){
        try {
            AsyncTask downloadSongTask = new FileDownloadTask(downloadUrlNormalQuality);
            downloadSongTask.execute();
        }catch(Exception e) {
            MyApplication.getInstance().trackException(e);
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrlNormalQuality));
            startActivity(intent);
        }
    }

    public void downloadHighQuality() {
        try {
            AsyncTask downloadSongTask = new FileDownloadTask(downloadUrlHighQuality);
            downloadSongTask.execute();
        }catch(Exception e) {
            MyApplication.getInstance().trackException(e);
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrlHighQuality));
            startActivity(intent);
        }
    }

}
