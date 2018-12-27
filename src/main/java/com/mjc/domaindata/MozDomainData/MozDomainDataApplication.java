package com.mjc.domaindata.MozDomainData;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = { "com.mjc.domaindata"} )
public class MozDomainDataApplication {

	
	public static void main(String[] args) 
	{
		SpringApplication.run(MozDomainDataApplication.class, args);
	}

}

