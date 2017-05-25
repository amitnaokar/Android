package com.dailysofts.adapter;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dailysofts.mp3downloader.R;
import com.dailysofts.parser.ParserConfig;
import com.dailysofts.parser.ParserUtils;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.jsoup.helper.StringUtil;

/**
 * Created by Amit on 06-05-17.
 */

public class AlbumSearchAdapter extends RecyclerView.Adapter<AlbumSearchAdapter.SongSearchHolder> {

    private JSONArray albumSearchList;
    Activity mActivity;

    public AlbumSearchAdapter(JSONArray searchResult, Activity activity){
        this.albumSearchList = searchResult;
        this.mActivity = activity;
    }

    public class SongSearchHolder extends RecyclerView.ViewHolder {
        public TextView albumName, songCount, songCategory;

        public SongSearchHolder(View view) {
            super(view);
            albumName = (TextView) view.findViewById(R.id.albumName);
            songCount = (TextView) view.findViewById(R.id.songCount);
            songCategory = (TextView) view.findViewById(R.id.category);

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                View divider = view.findViewById(R.id.listDivider);
                divider.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public SongSearchHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.album_search_list_item, parent, false);

        return new SongSearchHolder(itemView);
    }

    @Override
    public void onBindViewHolder(SongSearchHolder holder, int position) {
        JSONObject item = (JSONObject) albumSearchList.get(position);

        String songCount = (String)item.get(ParserConfig.NO_OF_TRACKS);
        holder.songCount.setText(Html.fromHtml(mActivity.getResources().getString(R.string.totalSongsBoldTxt,songCount)));

        String albumName = (String)item.get(ParserConfig.ALBUM_NAME);
        if(StringUtil.isBlank(albumName)){
            holder.albumName.setVisibility(View.GONE);
        }else{
            holder.albumName.setText(Html.fromHtml(mActivity.getResources().getString(R.string.albumBoldTxt,albumName)));
        }

        String category = (String)item.get(ParserConfig.CATEGORY).toString();
        if(StringUtil.isBlank(category)) {
            holder.songCategory.setVisibility(View.GONE);
        }else {
            holder.songCategory.setText(Html.fromHtml(mActivity.getResources().getString(R.string.categoryBoldTxt,ParserUtils.cleanUpCategoryName(category))));
        }

    }

    @Override
    public int getItemCount() {
        return albumSearchList.size();
    }

    public void setFiltered(JSONArray filtered){
        this.albumSearchList = filtered;
    }

}
