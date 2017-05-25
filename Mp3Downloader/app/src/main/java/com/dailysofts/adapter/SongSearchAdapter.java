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
 * Created by Amit on 30-04-17.
 */

public class SongSearchAdapter extends RecyclerView.Adapter<SongSearchAdapter.SongSearchHolder> {

    private JSONArray songSearchList;
    Activity mActivity;

    public SongSearchAdapter(JSONArray searchResult, Activity activity){
        this.songSearchList  = searchResult;
        this.mActivity = activity;
    }

    public class SongSearchHolder extends RecyclerView.ViewHolder {
        public TextView songName, songSize, singerName, albumName, songCategory;

        public SongSearchHolder(View view) {
            super(view);
            songName = (TextView) view.findViewById(R.id.songName);
            songSize = (TextView) view.findViewById(R.id.songSizeNormal);
            singerName = (TextView) view.findViewById(R.id.singerName);
            albumName = (TextView) view.findViewById(R.id.albumName);
            songCategory = (TextView) view.findViewById(R.id.songCategory);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                View divider = view.findViewById(R.id.listDivider);
                divider.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public SongSearchHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.song_search_list_item, parent, false);

        return new SongSearchHolder(itemView);
    }

    @Override
    public void onBindViewHolder(SongSearchHolder holder, int position) {
        JSONObject item = (JSONObject) songSearchList.get(position);

        String songName = (String)item.get(ParserConfig.SONG_NAME);
        holder.songName.setText(Html.fromHtml(mActivity.getResources().getString(R.string.songBoldTxt,songName)));

        String songSize = (String)item.get(ParserConfig.SIZE);
        holder.songSize.setText(Html.fromHtml(mActivity.getResources().getString(R.string.sizeBoldTxt,songSize)));

        /*String singerName = (String)item.get(ParserConfig.SINGER_NAME);
        if(StringUtil.isBlank(singerName)) {
            holder.singerName.setVisibility(View.GONE);
        }else {
            holder.singerName.setText("Singer : "+singerName);
        }*/

        String albumName = (String)item.get(ParserConfig.ALBUM_NAME);
        if(StringUtil.isBlank(albumName)){
            holder.albumName.setVisibility(View.GONE);
        }else{
            holder.albumName.setText(Html.fromHtml(mActivity.getResources().getString(R.string.albumBoldTxt,albumName)));
        }

/*        String category = (String)item.get(ParserConfig.CATEGORY).toString();
        if(StringUtil.isBlank(category)) {
            holder.songCategory.setVisibility(View.GONE);
        }else {
            holder.songCategory.setText("Category : "+category);
        }*/
    }

    @Override
    public int getItemCount() {
        return songSearchList.size();
    }

    public void setFiltered(JSONArray filtered){
        this.songSearchList = filtered;
    }

}
