package com.dailysofts.adapter;

import android.app.Activity;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
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

public class SingerSearchAdapter extends RecyclerView.Adapter<SingerSearchAdapter.SingerSearchHolder> {

    private JSONArray singerSearchList;
    Activity mActivity;

    public SingerSearchAdapter(JSONArray searchResult, Activity activity){
        this.singerSearchList = searchResult;
        this.mActivity = activity;
    }

    public class SingerSearchHolder extends RecyclerView.ViewHolder {
        public TextView singerName;

        public SingerSearchHolder(View view) {
            super(view);
            singerName = (TextView) view.findViewById(R.id.singerName);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                View divider = view.findViewById(R.id.listDivider);
                divider.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public SingerSearchHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.singer_search_list_item, parent, false);

        return new SingerSearchHolder(itemView);
    }

    @Override
    public void onBindViewHolder(SingerSearchHolder holder, int position) {
        JSONObject item = (JSONObject) singerSearchList.get(position);

        String singerName = (String)item.get(ParserConfig.SINGER_NAME);
        if(StringUtil.isBlank(singerName)){
            holder.singerName.setVisibility(View.GONE);
        }else{
            holder.singerName.setText(singerName);
        }
    }

    @Override
    public int getItemCount() {
        return singerSearchList.size();
    }

    public void setFiltered(JSONArray filtered){
        this.singerSearchList = filtered;
    }

}
