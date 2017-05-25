package com.dailysofts.parser;

import com.dailysofts.MyApplication;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class SingerParser {
	public static int currentSearchPage = 1;
	public static int lastSearchPage = 1;
	
	public static int currentSongListPage = 1;
	public static int lastSongListPage = 1;
	
	public static JSONArray parseSearchPage(String url){
		JSONArray result = new JSONArray();
		try{
			Document doc = Jsoup.connect(url).get();
			
			JSONObject paginationJsonObject = ParserUtils.getPagination(doc);
			currentSearchPage = Integer.parseInt(paginationJsonObject.get(ParserConfig.CURRENT_PAGE).toString());
			lastSearchPage = Integer.parseInt(paginationJsonObject.get(ParserConfig.LAST_PAGE).toString());
			
			Elements singerElements = doc.select("div#artist div.artistList div.artistRow");
			
			for(Element e:singerElements){
				JSONObject singer = createSingerSearchResult(e);
				if(singer!=null){
					result.add(singer);
				}
				
			}
			
		}catch(Exception e){
			MyApplication.getInstance().trackException(e);
		}
		return result;
	}
	
	private static JSONObject createSingerSearchResult(Element e){
		JSONObject singer = null;
		try{
			Elements singerUrlNodes = e.select("a");
			Elements singerNameNodes = e.select("a");
			Elements imgUrlNodes = e.select("a > img.absmiddle");
			
			String singerName = singerNameNodes.size()>0?singerNameNodes.first().ownText():"";
			String singerUrl = singerUrlNodes.size()>0?ParserConfig.DOMAIN + singerUrlNodes.first().attr("href"):"";
			String imgUrl = imgUrlNodes.size()>0?ParserConfig.DOMAIN + imgUrlNodes.first().attr("src"):"";

			if(!StringUtil.isBlank(singerName)){
				singer = new JSONObject();
				singer.put(ParserConfig.SINGER_NAME, ParserUtils.cleanUpString(singerName));
				singer.put(ParserConfig.IMAGE_URL, imgUrl);
				singer.put(ParserConfig.SINGER_URL, singerUrl);
			}
		}catch(Exception exec){
			MyApplication.getInstance().trackException(exec);
			singer = null;
		}
		return singer;
	}

	public static JSONArray parseListPage(String url){
		JSONArray result = new JSONArray();
		try{
			Document doc = Jsoup.connect(url).get();
			
			JSONObject paginationJsonObject = ParserUtils.getPagination(doc);
			currentSongListPage = Integer.parseInt(paginationJsonObject.get(ParserConfig.CURRENT_PAGE).toString());
			lastSongListPage = Integer.parseInt(paginationJsonObject.get(ParserConfig.LAST_PAGE).toString());
			
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
				song.put(ParserConfig.CATEGORY, ParserUtils.cleanUpString(category));
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
	
	public static void main(String args[]) {
		// TODO Auto-generated method stub
		SingerParser singerParser = new SingerParser();
		singerParser.parseListPage("http://mymp3song.info/singer/Abhijeet+Bhattacharya/new2old/2");
		singerParser.parseSearchPage("http://mymp3song.info/singer/Abhijeet+Bhattacharya/new2old/2");
	}
	
}
