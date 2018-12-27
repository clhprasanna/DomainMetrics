package com.mjc.domaindata.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.hibernate.validator.internal.util.DomainNameUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.support.BasicAuthorizationInterceptor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import com.mjc.domaindata.MozDomainData.Base64;
import com.mjc.domaindata.domain.URLMetric;
import com.mjc.domaindata.domain.URLResult;
import com.mjc.domaindata.utils.MozUtils;

@Component
public class URLMetricService 
{
	private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";
	private Map<String, String> parameters = new HashMap<>();
	private static final String baseURL = "https://lsapi.seomoz.com/linkscape/url-metrics/";

	@Autowired
	private RestTemplate restTemplate;
	
	public URLMetric getURLMetricsForDomain(String domain) throws InvalidKeyException, SignatureException, NoSuchAlgorithmException, URISyntaxException
	{

		long expiresInterval = 3000; 

		long timeStamp = ((new Date()).getTime())/1000 + expiresInterval;
		
		String accessId = "mozscape-ac52dbcf56";
		String secretKey = "bc0cc0d590fce266ba8f9fc26b996439";
		
		String signature = calculateHMACSHA1Hash(accessId+String.valueOf(timeStamp),secretKey);
		
		
		String finalURL = baseURL + domain;
		
		
		parameters.put("Cols", MozUtils.DomainAuthority_Cols);
		parameters.put("Limit", "500");
		parameters.put("AccessID", "mozscape-ac52dbcf56");
		parameters.put("Expires", String.valueOf(timeStamp));
		parameters.put("Signature", signature);
		
		/*
		
		String urlParams = MozUtils.getParams(parameters);
		
		String url = baseURL + "?" + urlParams;
		
		System.out.println(url);
		
		UriComponents uri = UriComponentsBuilder.fromUri(new URI(url)).build();
		
		restTemplate.getInterceptors().add(new BasicAuthorizationInterceptor("mozscape-ac52dbcf56", "bc0cc0d590fce266ba8f9fc26b996439"));
		
		ResponseEntity<URLResult> response = restTemplate.getForEntity(uri.toUriString(), URLResult.class);
		
		URLResult urlResult = response.getBody();
		
		*/
		
		URLResult urlResult = getResult(finalURL);
		
		URLMetric urlMetric = new URLMetric();
		
		urlMetric.setDomainAuthority(urlResult.getPda());
		
		System.out.println("Domain Authority "+urlResult.getPda());
		
		// page authority
		
		try {
			Thread.currentThread().sleep(1000*25);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		parameters.put("Cols", MozUtils.PageAuthority_Cols);
		urlResult = getResult(finalURL);
		urlMetric.setPageAuthority(urlResult.getUpa());
		
		System.out.println("Page Authority "+urlResult.getUpa());
		
		return urlMetric;
	
	}
	
	private URLResult getResult(String finalURL) throws URISyntaxException
	{
		String urlParams = MozUtils.getParams(parameters);
		String url = finalURL + "?" + urlParams;
		System.out.println("ffff - "+url);
		
		UriComponents uri = UriComponentsBuilder.fromUri(new URI(url)).build();
		restTemplate.getInterceptors().add(new BasicAuthorizationInterceptor("mozscape-ac52dbcf56", "bc0cc0d590fce266ba8f9fc26b996439"));
		ResponseEntity<URLResult> response = restTemplate.getForEntity(uri.toUriString(), URLResult.class);
		
		URLResult urlResult = response.getBody();
		return urlResult;
		
	}
	
	public static String calculateHMACSHA1Hash(String data, String key)
			throws SignatureException, NoSuchAlgorithmException, InvalidKeyException
	{
		SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(), HMAC_SHA1_ALGORITHM);
		Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
		mac.init(signingKey);
		return Base64.encodeBytes(mac.doFinal(data.getBytes()));
	}
	
	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) 
	{
	   return builder.build();
	}

}
