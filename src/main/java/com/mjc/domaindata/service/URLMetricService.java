package com.mjc.domaindata.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.support.BasicAuthorizationInterceptor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import com.mjc.domaindata.domain.URLMetric;
import com.mjc.domaindata.domain.URLResult;
import com.mjc.domaindata.utils.MozUtils;

@Component
public class URLMetricService 
{
	private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";
	private static final String AccessID = "mozscape-ac52dbcf56";
	private static final String SecretKey = "bc0cc0d590fce266ba8f9fc26b996439";
	private Map<String, String> parameters = new HashMap<String, String>();
	private static final String baseURL = "https://lsapi.seomoz.com/linkscape/url-metrics/";

	@Autowired
	private RestTemplate restTemplate;

	public URLMetric getURLMetricsForDomain(String domain) throws InvalidKeyException, SignatureException, NoSuchAlgorithmException, URISyntaxException
	{

		long expiresInterval = 3000; 

		long timeStamp = ((new Date()).getTime())/1000 + expiresInterval;
		
		// Signature : HMAC-SHA1 hash of AccessID and expires parameter signed using secret key.
		// The secure hash needs to be Base64 encoded.
		
		String signature = calculateHMACSHA1Hash(AccessID+timeStamp,SecretKey);
		
		String finalURL = baseURL + domain;
		
		parameters.put("Cols", MozUtils.DomainAuthority_Cols);
		parameters.put("Limit", "10");
		parameters.put("AccessID", AccessID);
		parameters.put("Expires", String.valueOf(timeStamp));
		parameters.put("Signature", signature);
		

		String urlParams = MozUtils.getParams(parameters);
		
		finalURL = finalURL + "?" + urlParams;
		
		// https://lsapi.seomoz.com/linkscape/url-metrics/moz.com?Cols=4&Limit=10&AccessID=mozscape-ac52dbcf56&Expires=1545264000&Signature=bc0cc0d590fce266ba8f9fc26b996439
		UriComponents uri = UriComponentsBuilder.fromUri(new URI(finalURL)).build();
		
		restTemplate.getInterceptors().add(new BasicAuthorizationInterceptor(AccessID, SecretKey));
		
		ResponseEntity<URLResult> response = restTemplate.getForEntity(uri.toUriString(), URLResult.class);
		
		URLResult urlResult = response.getBody();
		
		URLMetric urlMetric = new URLMetric();
		
		urlMetric.setDomainAuthority(urlResult.getPda());
		
		return urlMetric;
	
	}
	
	public static String calculateHMACSHA1Hash(String data, String key)
			throws SignatureException, NoSuchAlgorithmException, InvalidKeyException
	{
		SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(), HMAC_SHA1_ALGORITHM);
		Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
		mac.init(signingKey);
		
		return new String(Base64.getEncoder().encode(mac.doFinal(data.getBytes())));
	}
	
	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) 
	{
	   return builder.build();
	}

}
