package com.dailysofts.parser;

public class ParserConfig {
	public static final String DOMAIN = "http://mymp3song.info";
	//usage /files/search/find/<encoded keyword>/page/<pageNo>
	public static final String SONG_SEARCH_PREFIX_URL = "/files/search/find/";
	//usage /findalbum/find/<encoded keywords>/default/<pageNo>
	public static final String ALBUM_SEARCH_PREFIX_URL = "/findalbum/find/";
	//usage /singerlist/find/<encoded keywords>/<pageNo>
	public static final String SINGER_SEARCH_PREFIX_URL = "/singerlist/find/";

	public static final String SEARCH_KEYWORD = "keyword";
	public static final String PAGE_NUMBER = "pageNumber";

	public static final String CURRENT_PAGE = "currentPage";
	public static final String LAST_PAGE = "lastPage";
	
	public static final String SONG_URL = "songUrl";
	public static final String SONG_NAME = "songName";
	public static final String SINGER_NAME = "singerName";
	public static final String SINGER_URL = "singerUrl";
	public static final String ALBUM_NAME = "albumName";
	public static final String CATEGORY = "category";
	public static final String SIZE = "size";
	public static final String ALBUM_URL = "albumUrl";
	public static final String NO_OF_TRACKS = "numberOfTracks";
	public static final String IMAGE_URL = "ImgUrl";
	public static final String DOWNLOAD_SIZE_128 = "downloadSize128";
	public static final String DOWNLOAD_URL_128 = "downloadUrl128";
	public static final String DOWNLOAD_SIZE_320 = "downloadSize320";
	public static final String DOWNLOAD_URL_320 = "downloadUrl320";
	public static final String PAGINATION_REGX = ".*\\((\\d+)/(\\d+)\\).*";
	public static final String DOWNLOAD_ACTION_128= "downloadAction128";
	public static final String DOWNLOAD_ACTION_320= "downloadAction320";

	public static final String APP_TRACKING_ID = "UA-98687985-1";
}
