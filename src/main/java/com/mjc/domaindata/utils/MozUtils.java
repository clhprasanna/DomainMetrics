package com.mjc.domaindata.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

public class MozUtils 
{
	
	public static final String DomainAuthority_Cols = "68719476736";

	public static final String PageAuthority_Cols = "34359738368";
	
	
	
	public static String getParams(Map<String, String> params)
	{
		StringBuilder result = new StringBuilder();
		
		params.forEach((key, value) -> {
			try 
			{
				result.append(URLEncoder.encode(key, "UTF-8"));
				result.append("=");
				result.append(URLEncoder.encode(value, "UTF-8"));
				result.append("&");
			
			} 
			catch (UnsupportedEncodingException e) 
			{
				e.printStackTrace();
			}
		});
		
		String paramStr = result.toString();
		
		return paramStr.substring(0,paramStr.length()-1);
		
		
	}

}
