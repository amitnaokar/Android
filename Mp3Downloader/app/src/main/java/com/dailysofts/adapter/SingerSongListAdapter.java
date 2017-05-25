package com.dailysofts.adapter;

import android.app.Activity;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dailysofts.mp3downloader.R;
import com.dailysofts.parser.ParserConfig;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.jsoup.helper.StringUtil;

/**
 * Created by Amit on 06-05-17.
 */

public class SingerSongListAdapter extends RecyclerView.Adapter<SingerSongListAdapter.singerSongHolder> {

    private JSONArray singerSongList;
    Activity mActivity;

    public SingerSongListAdapter(JSONArray searchResult, Activity activity){
        this.singerSongList = searchResult;
        this.mActivity = activity;
    }

    public class singerSongHolder extends RecyclerView.ViewHolder {
        public TextView songName, songSize, singerName, albumName;

        public singerSongHolder(View view) {
            super(view);
            songName = (TextView) view.findViewById(R.id.songName);
            songSize = (TextView) view.findViewById(R.id.songSizeNormal);
            singerName = (TextView) view.findViewById(R.id.singerName);

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                View divider = view.findViewById(R.id.listDivider);
                divider.setVisibility(View.GONE);
            }albumName = (TextView) view.findViewById(R.id.albumName);
        }
    }

    @Override
    public singerSongHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.song_search_list_item, parent, false);

        return new singerSongHolder(itemView);
    }

    @Override
    public void onBindViewHolder(singerSongHolder holder, int position) {
        JSONObject item = (JSONObject) singerSongList.get(position);

        String songName = (String)item.get(ParserConfig.SONG_NAME);
        holder.songName.setText(Html.fromHtml(mActivity.getResources().getString(R.string.songBoldTxt,songName)));

        String songSize = (String)item.get(ParserConfig.SIZE);
        holder.songSize.setText(Html.fromHtml(mActivity.getResources().getString(R.string.sizeBoldTxt,songSize)));

        String singerName = (String)item.get(ParserConfig.SINGER_NAME);
        if(StringUtil.isBlank(singerName)) {
            holder.singerName.setVisibility(View.GONE);
        }else {
            holder.singerName.setText(Html.fromHtml(mActivity.getResources().getString(R.string.singerBoldTxt,singerName)));
        }

        String albumName = (String)item.get(ParserConfig.ALBUM_NAME);
        if(StringUtil.isBlank(albumName)){
            holder.albumName.setVisibility(View.GONE);
        }else{
            holder.albumName.setText(Html.fromHtml(mActivity.getResources().getString(R.string.albumBoldTxt,albumName)));
        }

    }

    @Override
    public int getItemCount() {
        return singerSongList.size();
    }

    public void setFiltered(JSONArray filtered){
        this.singerSongList = filtered;
    }

}
