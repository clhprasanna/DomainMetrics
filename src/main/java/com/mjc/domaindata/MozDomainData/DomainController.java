package com.mjc.domaindata.MozDomainData;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.Date;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.http.ResponseEntity;
import org.springframework.http.client.support.BasicAuthorizationInterceptor;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;


public class DomainController {

	private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";

	
	public static void main(String[] args) throws InvalidKeyException, SignatureException, NoSuchAlgorithmException, URISyntaxException, IOException 
	{

		long expiresInterval = 3000; 

		long timeStamp = ((new Date()).getTime())/1000 + expiresInterval;
		
		//System.out.println("timestamp : "+timeStamp);

		//String encoding = java.util.Base64.getEncoder().encodeToString(("test1:test1").getBytes("UTF-8"));

		
		getDomainMetrics("techsagar.com");
		
		//callMoz(timeStamp);

	}
	
	// https://lsapi.seomoz.com/linkscape/url-metrics/moz.com?Cols=4&Limit=10&AccessID=mozscape-ac52dbcf56&Expires=1545264000&Signature=bc0cc0d590fce266ba8f9fc26b996439
	
	//  AccessId : mozscape-ac52dbcf56
	// Secret Key: bc0cc0d590fce266ba8f9fc26b996439
	// timestamp : 1545264000 (20/12/2018)
	
	/*
	 * Signature: an HMAC-SHA1 hash of your Access ID, the Expires parameter, 
	 * and your Secret Key. The secure hash must be base64 encoded 
	 * then URL-encoded before Links API accepts the signature as valid.
	 */

	// https://github.com/seomoz/SEOmozAPISamples/tree/master/java/complete/src/com/seomoz/api
	
	// https://www.programmableweb.com/api/mozscape/sample-source-code

	
	public static void getDomainMetrics(String domain) throws InvalidKeyException, SignatureException, NoSuchAlgorithmException, UnsupportedEncodingException, URISyntaxException
	{
		long expiresInterval = 3000; 

		long timeStamp = ((new Date()).getTime())/1000 + expiresInterval;
		
		RestTemplate restTemplate = new RestTemplate();
		
		String accessId = "mozscape-ac52dbcf56";
		String secretKey = "bc0cc0d590fce266ba8f9fc26b996439";
		
		String signature = calculateRFC2104HMAC(accessId+String.valueOf(timeStamp),secretKey);
		
		
		String url = "https://lsapi.seomoz.com/linkscape/url-metrics/";
		
		url = url + domain;
				
		url = url + "?Cols=100&Limit=500&AccessID=mozscape-ac52dbcf56&Expires="+timeStamp+"&Signature="+signature;
		
		UriComponents uri = UriComponentsBuilder.fromUri(new URI(url)).build();
		
		restTemplate.getInterceptors().add(new BasicAuthorizationInterceptor("mozscape-ac52dbcf56", "bc0cc0d590fce266ba8f9fc26b996439"));
		
		ResponseEntity<String> response = restTemplate.getForEntity(uri.toUriString(), String.class);
		
		System.out.println(response.getBody());

		
	}
	
	public static String calculateRFC2104HMAC(String data, String key)
		throws SignatureException, NoSuchAlgorithmException, InvalidKeyException
	{
		SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(), HMAC_SHA1_ALGORITHM);
		Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
		mac.init(signingKey);
		
		//return toHexString(mac.doFinal(data.getBytes()));
		
		//Base64.getEncoder().encodeToString(mac.doFinal(data.getBytes()));
		
		return Base64.encodeBytes(mac.doFinal(data.getBytes()));
	}
	
	public static void callMoz(long timestamp) throws IOException, InvalidKeyException, SignatureException, NoSuchAlgorithmException
	{
		URL url = new URL("https://lsapi.seomoz.com/linkscape/url-metrics/moz.com");
		
		String encoding = java.util.Base64.getEncoder().encodeToString(("mozscape-ac52dbcf56:bc0cc0d590fce266ba8f9fc26b996439").getBytes());
		
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");	
		con.setConnectTimeout(50000);
		con.setReadTimeout(50000);
		
		con.setRequestProperty  ("Authorization", "Basic " + encoding);
		
		String signature = calculateRFC2104HMAC("mozscape-ac52dbcf56"+String.valueOf(timestamp),"bc0cc0d590fce266ba8f9fc26b996439");
		
		Map<String, String> parameters = new HashMap<>();
		parameters.put("Cols", "50");
		parameters.put("Limit", "100");
		parameters.put("AccessID", "mozscape-ac52dbcf56");
		parameters.put("Expires", String.valueOf(timestamp));
		parameters.put("Signature", signature);
		con.setDoOutput(true);
		DataOutputStream out = new DataOutputStream(con.getOutputStream());
		out.writeBytes(getParamsString(parameters));
		System.out.println(getParamsString(parameters));

		out.flush();
		out.close();
	
		
		int status = con.getResponseCode();
		System.out.println(status);
		
        InputStream content = (InputStream)con.getInputStream();
        BufferedReader in   = 
            new BufferedReader (new InputStreamReader (content));
        String line;
        while ((line = in.readLine()) != null) {
            System.out.println(line);
        }

		
	}
	

	public static String getParamsString(Map<String, String> params) throws UnsupportedEncodingException
	{
		StringBuilder result = new StringBuilder();
		for (Map.Entry<String, String> entry : params.entrySet()) 
		{
			result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
			result.append("=");
			result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
			result.append("&");
		}
		
		String resultString = result.toString();
		return resultString.length() > 0
		? resultString.substring(0, resultString.length() - 1)
		: resultString;
	}
	
}
