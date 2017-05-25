package com.dailysofts.parser;

import com.dailysofts.MyApplication;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class SongParser{
	public static int currentSearchPage = 1;
	public static int lastSearchPage = 1;
	
	
	public static JSONObject parseProductPage(String url){
		JSONObject result = new JSONObject();
		try{
			Document doc = Jsoup.connect(url).get();
	
			String downloadUrl1 = "";
			String downloadSize1 = "";
			String downloadUrl2 = "";
			String downloadSize2 = "";
			
			Elements fileInfoElements = doc.select("div#mainDiv div.fshow div.fInfo");
            Elements songNameNodes = fileInfoElements.select("div.fi");
			String songName = songNameNodes.size()>0?songNameNodes.first().ownText():"";
            Elements singerNodes = fileInfoElements.select("div.fhd:contains(Singer)");
			String singer = singerNodes.size()>0?singerNodes.first().nextElementSibling().select("a").first().ownText():"";
            Elements albumNameNodes = fileInfoElements.select("div.fhd:contains(Category)");
			String albumName = albumNameNodes.size()>0?albumNameNodes.first().nextElementSibling().select("div.fi > a").first().ownText():"";
			String albumUrl = albumNameNodes.size()>0?ParserConfig.DOMAIN + albumNameNodes.first().nextElementSibling().select("div.fi > a").first().attr("href"):"";
			if(StringUtil.isBlank(songName)){
				songName = fileInfoElements.select("div.fi").first().select("a.dwnLink").first().ownText();
				downloadUrl1 = ParserConfig.DOMAIN + fileInfoElements.select("div.fi > a.dwnLink").first().attr("href");
				downloadSize1 = fileInfoElements.select("div.fi:contains(mb)").first().ownText();
			}else{
				downloadUrl1 = ParserConfig.DOMAIN + fileInfoElements.select("a.dwnLink2").first().attr("href");
				downloadSize1 = fileInfoElements.select("a.dwnLink2").first().ownText();
				downloadUrl2 = ParserConfig.DOMAIN + fileInfoElements.select("a.dwnLink4").first().attr("href");
				downloadSize2 = fileInfoElements.select("a.dwnLink4").first().ownText();
			}
			result.put(ParserConfig.SONG_NAME, ParserUtils.cleanUpString(songName));
			result.put(ParserConfig.SINGER_NAME, ParserUtils.cleanUpString(singer));
			result.put(ParserConfig.DOWNLOAD_URL_128, downloadUrl1);
			result.put(ParserConfig.DOWNLOAD_SIZE_128, downloadSize1);
			result.put(ParserConfig.DOWNLOAD_URL_320, downloadUrl2);
			result.put(ParserConfig.DOWNLOAD_SIZE_320, downloadSize2);
			result.put(ParserConfig.ALBUM_NAME, ParserUtils.cleanUpString(albumName));
			result.put(ParserConfig.ALBUM_URL, albumUrl);
		}catch(Exception e){
			MyApplication.getInstance().trackException(e);
			result = null;
		}
		return result;
	}

	public static JSONArray parseSearchPage(String url){
		JSONArray result = new JSONArray();
		try{
			Document doc = Jsoup.connect(url).get();
			
			JSONObject paginationJsonObject = ParserUtils.getPagination(doc);
			currentSearchPage = Integer.parseInt(paginationJsonObject.get(ParserConfig.CURRENT_PAGE).toString());
			lastSearchPage = Integer.parseInt(paginationJsonObject.get(ParserConfig.LAST_PAGE).toString());
			
			Elements evenElements = doc.select("div[class='fl odd']:contains(mb)");
			Elements oddElements = doc.select("div[class='fl odd']:contains(mb)");
			
			for(Element e:evenElements){
				JSONObject song = createSongSearchResult(e);
				if(song!=null){
					result.add(song);
				}
			}
			
			for(Element e:oddElements){
				JSONObject song = createSongSearchResult(e);
				if(song!=null){
					result.add(song);
				}
			}
		}catch(Exception e){
			MyApplication.getInstance().trackException(e);
		}
		return result;
	}

	private static JSONObject createSongSearchResult(Element e){
		JSONObject song = null;
		
		try{
			Elements songUrlNodes = e.select("a[class='fileName']");
			Elements songNameNodes = e.select("a[class='fileName'] > div > div");
			Elements albumNameNodes =e.select("a[class='fileName']").select("span[class='alb']");
			Elements categoryNodes = e.select("a[class='fileName']").select("span[class='mc']");
			Elements singerNodes = e.select("a[class='fileName']").select("span[class='ar']");
			Elements sizeNodes = e.select("a[class='fileName'] > div > div");
			Elements songImageUrlNodes = e.select("div > img");

			String songImg = songImageUrlNodes.size()>0?ParserConfig.DOMAIN + songImageUrlNodes.first().attr("src"):"";
			String songName = songNameNodes.size()>0?songNameNodes.first().ownText():"";
			if(StringUtil.isBlank(songName)){
				songNameNodes = songNameNodes.select("div:contains(mb)");
				if(songNameNodes.size()>0){
					songName = songNameNodes.first().ownText();
				}
			}
			String songUrl = songUrlNodes.size()>0?ParserConfig.DOMAIN + songUrlNodes.first().attr("href"):"";
			String albumName = albumNameNodes.size()>0?albumNameNodes.first().html():"";
			String category = categoryNodes.size()>0?categoryNodes.first().html():"";
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
				song.put(ParserConfig.ALBUM_NAME, ParserUtils.cleanUpString(albumName));
				song.put(ParserConfig.CATEGORY, ParserUtils.cleanUpCategoryName(category));
				song.put(ParserConfig.SINGER_NAME, ParserUtils.cleanUpString(singer));
				song.put(ParserConfig.SONG_URL, songUrl);
				song.put(ParserConfig.SIZE, ParserUtils.cleanUpString(size));
				song.put(ParserConfig.IMAGE_URL, songImg);
			}
		}catch(Exception exec){
			MyApplication.getInstance().trackException(exec);
			song = null;
		}
		return song;
	}
	
	public static void main(String args[]) {
		SongParser.parseSearchPage("http://mymp3song.info/files/search/find/jaan/page/2");
	}

}
