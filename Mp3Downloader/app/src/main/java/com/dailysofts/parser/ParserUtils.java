package com.dailysofts.parser;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.dailysofts.MyApplication;
import com.dailysofts.mp3downloader.R;
import com.dailysofts.mp3downloader.activities.MainActivity;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.simple.JSONObject;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class ParserUtils {
	public static String cleanUpString(String name){
		try{
			name = name.replace("|", "");
			name = name.replace("-", "");
			name = name.replace("Album:", "");
			name = name.replace(":", "");
			name = name.replace("Mp3", "");
			name = name.replace("MP3", "");
			name = name.replace("Songs", "");
			name = name.trim();
		}catch(Exception e){
			MyApplication.getInstance().trackException(e);
		}
		return name;
	}

	public static String cleanUpCategoryName(String name){
		try{
			name = name.replace("(", "");
			name = name.replace(")", "");
			name = name.replace(":", "");
			name = name.replace("-","");
			name = name.replace("Mp3 Songs", "");
			name = name.replace("MP3 Songs", "");
			name = name.trim();
		}catch(Exception e){
			MyApplication.getInstance().trackException(e);
		}
		return name;
	}

	/*public static String cleanUpSize(String name){
		try{
			name = name.replace("Album:", "");
			name = name.replace("|", "");
			name = name.replace(":", "");
			name = name.replace("Mp3", "");
			name = name.replace("Songs", "");
			name = name.trim();
		}catch(Exception e){
			e.printStackTrace();
		}
		return name;
	}*/
	
	public static String createSongSearchPageUrl(String keyword, int pageNumber){
		try {
			return ParserConfig.DOMAIN + ParserConfig.SONG_SEARCH_PREFIX_URL 
					+ URLEncoder.encode(keyword,"UTF-8") + "/page/" + pageNumber;
		} catch (UnsupportedEncodingException e) {
			MyApplication.getInstance().trackException(e);
			return ParserConfig.DOMAIN + ParserConfig.SONG_SEARCH_PREFIX_URL
					+ keyword.replaceAll(" ", "%20") + "/page/" + pageNumber;
		}
	}
	
	public static String createAlbumSearchPageUrl(String keyword, int pageNumber){
		try {
			return ParserConfig.DOMAIN + ParserConfig.ALBUM_SEARCH_PREFIX_URL 
					+ URLEncoder.encode(keyword,"UTF-8") + "/default/" + pageNumber;
		} catch (UnsupportedEncodingException e) {
			MyApplication.getInstance().trackException(e);
			return ParserConfig.DOMAIN + ParserConfig.ALBUM_SEARCH_PREFIX_URL
					+ keyword.replaceAll(" ","%20") + "/default/" + pageNumber;
		}
	}
	
	public static String createSingerSearchPageUrl(String keyword, int pageNumber){
		try {
			return ParserConfig.DOMAIN + ParserConfig.SINGER_SEARCH_PREFIX_URL 
					+ URLEncoder.encode(keyword,"UTF-8") + "/" + pageNumber;
		} catch (UnsupportedEncodingException e) {
			MyApplication.getInstance().trackException(e);
			return ParserConfig.DOMAIN + ParserConfig.SINGER_SEARCH_PREFIX_URL
					+ keyword.replaceAll(" ","%20") + "/" + pageNumber;
		}
	}

	public static String createSingerSongListPageUrl(String url, int pageNumber){
		String[] urlParts = url.split("/");
		urlParts[urlParts.length-1] = Integer.toString(pageNumber);
		StringBuffer sb = new StringBuffer();
		for(int i=0;i<urlParts.length;i++){
			sb.append(urlParts[i]);
			if(i!=urlParts.length-1) {
				sb.append("/");
			}
		}
		url = sb.toString();
		return url;
	}

	public static JSONObject getPagination(Document doc){
		JSONObject pagination = new JSONObject();
		pagination.put(ParserConfig.CURRENT_PAGE, "1");
		pagination.put(ParserConfig.LAST_PAGE, "1");
		
		Elements paginationDivs = doc.select("div[class='pgn'] > div");
		if(paginationDivs.size()>0){
			paginationDivs = paginationDivs.select("div:contains(Page)");
			if(paginationDivs.size()>0){
				String paginationDivText = paginationDivs.first().ownText();
				Pattern p = Pattern.compile(ParserConfig.PAGINATION_REGX);
				Matcher matcher = p.matcher(paginationDivText);
				if(matcher.find() && matcher.groupCount()>=2){
					pagination.put(ParserConfig.CURRENT_PAGE, Integer.parseInt(matcher.group(1)));
					pagination.put(ParserConfig.LAST_PAGE, Integer.parseInt(matcher.group(2)));
				}
			}	
		}
		return pagination;
	}
	
	public static Boolean isProductPage(String url){
		if(url.toLowerCase().contains("filedownload")){
			return true;
		}
		return false;
	}
	
	public static Boolean isSongListPage(String url){
		if(url.toLowerCase().contains("filelist")){
			return true;
		}
		return false;
	}
	
	public static Boolean isSingerListPage(String url){
		if(url.toLowerCase().contains("singer")){
			return true;
		}
		return false;
	}

	public static String getFileName(String url){
		url = URLDecoder.decode(url);
		String urlParts[] = url.split("/");
		return urlParts[urlParts.length-1];
	}

	public static void shareApp(Activity activity){
		Intent intent = new Intent(android.content.Intent.ACTION_SEND);
		intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
				Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
				Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
		intent.setType("text/plain");
		String shareBodyText = "http://play.google.com/store/apps/details?id="+activity.getPackageName()+"\n" +
				"With our application you can search for an artist or a song name or album in the best possible quality and download the results for free\n\n" +
				"Have fun and enjoy our application!";
		intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "FindMyMp3");
		intent.putExtra(android.content.Intent.EXTRA_TEXT, shareBodyText);
		activity.startActivity(Intent.createChooser(intent, "Share via"));
	}

	public static void rateApp(Activity activity){
		Context context = activity;
		String packageName = context.getPackageName();
		Uri uri = Uri.parse("http://play.google.com/store/apps/details?id=" + packageName);
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		// on back button press to taken back to our application, we need to add following flags to intent.
		intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
				Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
				Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
		try {
			activity.startActivity(intent);
		} catch (ActivityNotFoundException e) {
			MyApplication.getInstance().trackException(e);
			activity.startActivity(new Intent(Intent.ACTION_VIEW,
					Uri.parse("market://details?id=" + packageName)));
		}
	}

	public static void emailUs(Activity activity){
		if(Build.VERSION.SDK_INT<=Build.VERSION_CODES.JELLY_BEAN_MR2){

			new AlertDialog.Builder(activity)
			.setTitle("Contact Us")
			.setMessage("Send us your feedback or complaints on dailysofts@gmail.com")
			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}

			})
			.setIcon(android.R.drawable.ic_dialog_info)
			.show();
		}else {
			Intent intent = new Intent(Intent.ACTION_SEND);
			intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
					Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
					Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
			intent.setType("text/email");
			intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"dailysofts@gmail.com"});
			intent.putExtra(Intent.EXTRA_SUBJECT, "Feedback");
			intent.putExtra(Intent.EXTRA_TEXT, "Tell us your feedback or complaints");
			activity.startActivity(Intent.createChooser(intent, "Send Feedback"));
		}
	}

    public static void showSnack(boolean isConnected, final Activity activity) {
		final String message;
		if (isConnected) {
			message = "You are Online";
		} else {
			message = "Sorry! You are offline";
		}
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(activity,message,Toast.LENGTH_LONG).show();
			}
		});
		Snackbar snackbar = Snackbar
				.make(activity.findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG);
		View sbView = snackbar.getView();
		TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
		textView.setTextColor(activity.getResources().getColor(R.color.textColor));
		textView.setGravity(View.TEXT_ALIGNMENT_CENTER);
		snackbar.show();
    }
}
