package com.mjc.domaindata.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mjc.domaindata.domain.URLMetric;
import com.mjc.domaindata.service.URLMetricService;

@RestController
public class MozDataController 
{
	
	@Autowired
	private URLMetricService urlMetricService;
	
	@GetMapping("/mozdata")
	public URLMetric getURLMetrics(String domain)
	{
		URLMetric urlMetric = new URLMetric();

		try
		{
			urlMetric = urlMetricService.getURLMetricsForDomain(domain);
			
		}
		catch (Exception e) 
		{
			urlMetric.setDomainAuthority(e.getMessage());
		}
		
		return urlMetric;
	
	}

}
