package com.in28minutes.microservices.camelmicroservicea.routes.c;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class ActiveMqSenderRouter extends RouteBuilder{

	@Override
	public void configure() throws Exception {
		//timer
		from("timer:active-mq-timer?period=10000")
		.transform().constant("My message for Active MQ")
		.log("${body}")
		.to("activemq:my-activemq-queue");
		//queue
		
	}

}
