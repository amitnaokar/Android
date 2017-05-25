package com.dailysofts.parser;

import com.dailysofts.MyApplication;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class AlbumParser {
	public static int currentSearchPage = 0;
	public static int lastSearchPage = 1;
	
	public static JSONArray parseSearchPage(String url){
		JSONArray result = new JSONArray();
		try{
			Document doc = Jsoup.connect(url).get();
			
			JSONObject paginationJsonObject = ParserUtils.getPagination(doc);
			currentSearchPage = Integer.parseInt(paginationJsonObject.get(ParserConfig.CURRENT_PAGE).toString());
			lastSearchPage = Integer.parseInt(paginationJsonObject.get(ParserConfig.LAST_PAGE).toString());
			
			Elements albumNodeList = doc.select("div#cateogry > div.catList > div.catRow");
			for(Element element:albumNodeList){
				JSONObject albumJsonObject = new JSONObject();
				Elements albumUrlNodes = element.select("a");
				String albumUrl = albumUrlNodes.size()>0?ParserConfig.DOMAIN + albumUrlNodes.first().attr("href"):"";
				
				Elements albumNameNodes = element.select("a > div:contains(Song)");
				String albumName = albumNameNodes.size()>0?albumNameNodes.first().ownText():"";
				
				Elements numberOfTrackNodes = element.select("a > div > span[class='gn']");
				String numberOfTracks = numberOfTrackNodes.size()>0?numberOfTrackNodes.first().ownText():"0";
				
				Elements imageUrlNodes = element.select("a > div > img.absmiddle");
				String imageUrl = imageUrlNodes.size()>0?ParserConfig.DOMAIN + imageUrlNodes.first().attr("src"):"";
				
				Elements categoryNodes = element.select("a > div > small > i");
				String category = categoryNodes.size()>0?categoryNodes.first().ownText():"";
				
				if(!StringUtil.isBlank(albumName)){
					albumJsonObject.put(ParserConfig.ALBUM_NAME, ParserUtils.cleanUpString(albumName));
					albumJsonObject.put(ParserConfig.ALBUM_URL, albumUrl);
					albumJsonObject.put(ParserConfig.CATEGORY, ParserUtils.cleanUpCategoryName(category));
					albumJsonObject.put(ParserConfig.NO_OF_TRACKS, ParserUtils.cleanUpString(numberOfTracks));
					albumJsonObject.put(ParserConfig.IMAGE_URL, imageUrl);
					result.add(albumJsonObject);
				}
			}
		}catch(Exception e){
			MyApplication.getInstance().trackException(e);
		}
		return result;
	}
	
	public static JSONArray parseListPage(String url){
		JSONArray result = new JSONArray();
		int currentListPage = 1;
		int lastListPage = 1;
		
		try{
			while(currentListPage<=lastListPage){
				Document doc = Jsoup.connect(url).get();
				JSONObject paginationJsonObject = ParserUtils.getPagination(doc);
				lastListPage = Integer.parseInt(paginationJsonObject.get(ParserConfig.LAST_PAGE).toString());
				
				Elements evenElements = doc.select("div[class='fl odd']:contains(mb)");
				Elements oddElements = doc.select("div[class='fl odd']:contains(mb)");
				
				for(Element e:evenElements){
					JSONObject song = createAlbumListResult(e);
					if(song!=null){
						result.add(song);
					}
				}
				
				for(Element e:oddElements){
					JSONObject song = createAlbumListResult(e);
					if(song!=null){
						result.add(song);
					}
				}
				//TODO create link and increase product page
				String[] urlParts = url.split("/");
				urlParts[urlParts.length-1] = Integer.toString(currentListPage+1);
				StringBuffer sb = new StringBuffer();
				for(int i=0;i<urlParts.length;i++){
					sb.append(urlParts[i]);
					if(i!=urlParts.length-1) {
						sb.append("/");
					}
				}
				url = sb.toString();
				currentListPage++;
			}
		}catch(Exception e){
			MyApplication.getInstance().trackException(e);
		}
		return result;
	}
	
	private static JSONObject createAlbumListResult(Element e){
		JSONObject song = null;
		Elements songUrlNodes = e.select("a[class='fileName']");
		Elements songNameNodes = e.select("a[class='fileName'] > div > div").select("div:contains(.mp3)");
		Elements singerNodes = e.select("a[class='fileName']").select("span[class='ar']");
		Elements sizeNodes = e.select("a[class='fileName']").select("span:contains(mb)");
		try{
			String songName = songNameNodes.size()>0?songNameNodes.first().ownText():"";
			if(StringUtil.isBlank(songName)){
				songNameNodes = e.select("a[class='fileName'] > div > div > span:contains(mb)");
				if(songNameNodes.size()>0){
					songName = songNameNodes.first().parent().ownText();
				}
			}
			if(StringUtil.isBlank(songName)){
				songNameNodes = songNameNodes.select("div:contains(mb)");
				if(songNameNodes.size()>0){
					songName = songNameNodes.first().ownText();
				}
			}
			String songUrl = songUrlNodes.size()>0?ParserConfig.DOMAIN + songUrlNodes.first().attr("href"):"";
			String singer = singerNodes.size()>0?singerNodes.first().html().replaceAll("Singer:", "").trim():"";
			String size = sizeNodes.size()>0?sizeNodes.first().select("span:nth-last-child(2)").html():"";
			if(StringUtil.isBlank(size)){
				sizeNodes = sizeNodes.select("span:contains(mb)");
				if(sizeNodes.size()>0){
					size = sizeNodes.first().ownText();
				}
			}
			if(!StringUtil.isBlank(songName)){
				song = new JSONObject();
				song.put(ParserConfig.SONG_NAME, ParserUtils.cleanUpString(songName));
				song.put(ParserConfig.SINGER_NAME, ParserUtils.cleanUpString(singer));
				song.put(ParserConfig.SONG_URL, songUrl);
				song.put(ParserConfig.SIZE, ParserUtils.cleanUpString(size));
			}
		}catch(Exception exec){
			MyApplication.getInstance().trackException(exec);
			song = null;
		}
		return song;
	}
	
	public static void main(String[] args) {
		AlbumParser albumParser = new AlbumParser();
		albumParser.parseSearchPage("http://mymp3song.info/findalbum/find/jaan/default/1");
		albumParser.parseListPage("http://mymp3song.info/findalbum/find/jaan/default/1");
	}

}
