package com.mangosteen.mangosteen_test;

public class Url {
	private static final String NEWSURL ="http://203.250.80.96:8080";
	private static final String ORIGINURL="http://203.250.80.96:8080/PriceComparision/";
	private static final String PRICEURL = "http://203.250.80.96:8080";
	private static final String TRACE = "http://203.250.80.96:8080";
	private static final String ENJOY = "http://210.118.69.65/mangosteen_rest/mangosteen_rest/rest/users";
	
	public static String getEnjoy() {
		return ENJOY;
	}


	public static String getPriceurl() {
		return PRICEURL;
	}


	public static String getOriginUrl() {
		return ORIGINURL;
	}


	public static String getNewsUrl() {
		return NEWSURL;
	}


	public static String getTrace() {
		return TRACE;
	}
	
}
