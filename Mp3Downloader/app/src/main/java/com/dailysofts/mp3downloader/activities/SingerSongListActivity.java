package com.dailysofts.mp3downloader.activities;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.dailysofts.ConnectivityReceiver;
import com.dailysofts.MyApplication;
import com.dailysofts.adapter.SingerSongListAdapter;
import com.dailysofts.mp3downloader.R;
import com.dailysofts.parser.ParserConfig;
import com.dailysofts.parser.ParserUtils;
import com.dailysofts.parser.SingerParser;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.jsoup.helper.StringUtil;

public class SingerSongListActivity extends AppCompatActivity implements ConnectivityReceiver.ConnectivityReceiverListener{

    private Toolbar toolbar;
    private RecyclerView searchResultView;
    public static SingerSongListAdapter singerSongListAdapter;
    public static JSONArray singerSongListSearchResult = new JSONArray();
    private String singerSongListUrl;
    private static Boolean requestComplete = false;

    private ProgressBar progressBar;
    private RecyclerView.LayoutManager mLayoutManager;
    private SwipeRefreshLayout swipeRefreshLayout;
    private SingerSongListTask singerSongListTask;
    private Context mActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = this;
        setContentView(R.layout.activity_singer_song_list);

        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.singerSongListTitle);
//        getSupportActionBar().setDisplayShowHomeEnabled(true);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        /*set onRefreshViewListener*/
        swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SearchRefreshViewListener());

        searchResultView = (RecyclerView) findViewById(R.id.recycler_view);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        mLayoutManager = new LinearLayoutManager(this);
        singerSongListAdapter = new SingerSongListAdapter(singerSongListSearchResult,this);

        searchResultView.setLayoutManager(mLayoutManager);
        searchResultView.setItemAnimator(new DefaultItemAnimator());
        /*set adpater*/
        searchResultView.setAdapter(singerSongListAdapter);
        /*set onScrollListener*/
        searchResultView.setOnScrollListener(new SingerSongListScrollListener());
        /*set onTouchListener*/
        searchResultView.addOnItemTouchListener(new SingerSongListItemOnTouchListener(getApplicationContext(),
                searchResultView, new ClickListener() {
            @Override
            public void onClick(View view, int position) {
                if (!ConnectivityReceiver.isConnected()) {
                    Toast.makeText(mActivity, "Please check internet connectivity", Toast.LENGTH_LONG).show();
                } else {
                    Intent intent = new Intent(getApplicationContext(), SongDownloadActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    JSONObject item = (JSONObject) singerSongListSearchResult.get(position);
                    intent.putExtra(ParserConfig.SONG_URL, item.get(ParserConfig.SONG_URL).toString());
                    startActivity(intent);
                }
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));

        if (!ConnectivityReceiver.isConnected()) {
            Toast.makeText(mActivity, "Please check internet connectivity", Toast.LENGTH_LONG).show();
        } else {
            singerSongListUrl = ParserUtils.createSingerSongListPageUrl(getIntent().getStringExtra(ParserConfig.SINGER_URL),
                    getIntent().getIntExtra(ParserConfig.PAGE_NUMBER, 1));
            singerSongListTask = new SingerSongListTask(singerSongListUrl, false);
            singerSongListTask.execute();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        final MenuItem item = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(new SearchFilterQueryListener());

        MenuItemCompat.setOnActionExpandListener(item,
                new MenuItemCompat.OnActionExpandListener() {
                    @Override
                    public boolean onMenuItemActionCollapse(MenuItem item) {
                        singerSongListAdapter.setFiltered(singerSongListSearchResult);
                        return true; // Return true to collapse action view
                    }

                    @Override
                    public boolean onMenuItemActionExpand(MenuItem item) {
                        return true; // Return true to expand action view
                    }
                });
        return true;
    }

    @Override
    protected void onResume() {
        MyApplication.getInstance().trackScreenView("Singer Song List");
        //register connection status listener
        MyApplication.getInstance().setConnectivityListener(this);
        super.onResume();
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        ParserUtils.showSnack(isConnected,this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    /*interface with click events*/
    public interface ClickListener {
        void onClick(View view, int position);

        void onLongClick(View view, int position);
    }
    /*onTouchListener*/
    class SingerSongListItemOnTouchListener implements RecyclerView.OnItemTouchListener{
        private GestureDetector gestureDetector;
        private ClickListener clickListener;

        public SingerSongListItemOnTouchListener(Context context, final RecyclerView recyclerView, final ClickListener clickListener) {
            this.clickListener = clickListener;
            gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                    if (child != null && clickListener != null) {
                        clickListener.onLongClick(child, recyclerView.getChildPosition(child));
                    }
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
            View child = rv.findChildViewUnder(e.getX(), e.getY());
            if (child != null && clickListener != null && gestureDetector.onTouchEvent(e)) {
                clickListener.onClick(child, rv.getChildPosition(child));
            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {

        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    }
    /*onScrollListener*/
    class SingerSongListScrollListener extends RecyclerView.OnScrollListener{
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            if (!ConnectivityReceiver.isConnected()) {
                Toast.makeText(mActivity, "Please check internet connectivity", Toast.LENGTH_LONG).show();
            } else if(SingerParser.currentSongListPage<=SingerParser.lastSongListPage){
                int visibleItemCount = mLayoutManager.getChildCount();
                int totalItemCount = mLayoutManager.getItemCount();
                int pastVisibleItems = ((LinearLayoutManager) searchResultView.getLayoutManager()).findFirstVisibleItemPosition();
                if (pastVisibleItems + visibleItemCount >= totalItemCount && requestComplete) {
                    SingerParser.currentSongListPage = SingerParser.currentSongListPage+1;
                    singerSongListUrl = ParserUtils.createSingerSongListPageUrl(getIntent().getStringExtra(ParserConfig.SINGER_URL),SingerParser.currentSongListPage);
                    singerSongListTask = new SingerSongListTask(singerSongListUrl,false);
                    singerSongListTask.execute();
                }
            }
        }
    }

    /*onRefreshListener*/
    class SearchRefreshViewListener implements SwipeRefreshLayout.OnRefreshListener{
        @Override
        public void onRefresh() {
            swipeRefreshLayout.setRefreshing(false);
            if (!ConnectivityReceiver.isConnected()) {
                Toast.makeText(mActivity, "Please check internet connectivity", Toast.LENGTH_LONG).show();
            } else if (SingerParser.currentSongListPage<=SingerParser.lastSongListPage && requestComplete) {
                SingerParser.currentSongListPage = SingerParser.currentSongListPage+1;
                singerSongListUrl = ParserUtils.createSingerSongListPageUrl(getIntent().getStringExtra(ParserConfig.SINGER_URL),SingerParser.currentSongListPage);
                singerSongListTask = new SingerSongListTask(singerSongListUrl,true);
                singerSongListTask.execute();
            }

        }
    }

    /*onSearchListener*/
    class SearchFilterQueryListener implements SearchView.OnQueryTextListener{
        public boolean onQueryTextChange(String newText) {
            JSONArray filteredArray = new JSONArray();
            if(!StringUtil.isBlank(newText)) {
                newText = newText.toLowerCase();
                for (Object object : singerSongListSearchResult) {
                    JSONObject jsonObject = (JSONObject) object;
                    String albumName = jsonObject.get(ParserConfig.SONG_NAME).toString().toLowerCase();
                    if (albumName.contains(newText)) {
                        filteredArray.add(jsonObject);
                    }
                }
            }else{
                filteredArray = singerSongListSearchResult;
            }
            singerSongListAdapter.setFiltered(filteredArray);
            singerSongListAdapter.notifyDataSetChanged();
            return true;
        }

        public boolean onQueryTextSubmit (String query) {
            return false;
        }
    }

    /*async task to search songs*/
    class SingerSongListTask extends AsyncTask<String, Integer, JSONArray> {
        String searchUrl;
        Boolean isRefreshViewEvent;

        public SingerSongListTask(String url, Boolean isRefreshViewEvent){
            this.searchUrl = url;
            this.isRefreshViewEvent = isRefreshViewEvent;
        }

        @Override
        protected void onPreExecute() {
            if(isRefreshViewEvent) {
                swipeRefreshLayout.setRefreshing(true);
            }else{
                progressBar.setVisibility(View.VISIBLE);
            }
            requestComplete = false;
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(JSONArray result) {
            singerSongListAdapter.notifyDataSetChanged();
            requestComplete = true;
            if(isRefreshViewEvent) {
                swipeRefreshLayout.setRefreshing(false);
            }else{
                progressBar.setVisibility(View.GONE);
            }
            if(singerSongListSearchResult.size()<=0 && ConnectivityReceiver.isConnected()){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mActivity,"No result(s) found",Toast.LENGTH_LONG);
                    }
                });
            }
            super.onPostExecute(result);
        }

        @Override
        protected JSONArray doInBackground(String... params) {
            JSONArray result = SingerParser.parseListPage(searchUrl);
            singerSongListSearchResult.addAll(result);
            return singerSongListSearchResult;
        }
    }

    public static void clearSearchResult(){
        singerSongListSearchResult.clear();
        SingerParser.currentSongListPage = 1;
        SingerParser.lastSongListPage = 1;
    }
}
