package com.in28minutes.microservices.camelmicroservicea.routes.c;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

//@Component
public class KafkaSenderRouter extends RouteBuilder{

	@Override
	public void configure() throws Exception {
		from("file:files/json")
		.log("${body}")
		.to("kafka:myKafkaTopic");
		
	}

}
