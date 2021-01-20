package com.in28minutes.microservices.camelmicroservicea.routes.patterns;

import java.util.List;
import java.util.Map;

import org.apache.camel.Body;
import org.apache.camel.ExchangeProperties;
import org.apache.camel.Headers;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.in28minutes.microservices.camelmicroservicea.CurrencyExchange;

//@Component
public class EipPatternsRouter extends RouteBuilder{
	

	@Autowired
	SplitterComponent splitter;
	
	
	@Autowired
	DynamicRouterBean dynamicRouterBean;

	@Override
	public void configure() throws Exception {

		getContext().setTracing(true);
		
		errorHandler(deadLetterChannel("activemq:dead-letter-queue"));

		//Pipeline
		//Content Based Routing - choice()
		//Multicast
		
		
//		from("timer:multicast?period=10000")
//		.multicast()
//		.to("log:something1", "log:something2", "log:something3");
		
//		from("file:files/csv")
//		.unmarshal().csv()
//		.split(body())
//		.to("activemq:split-queue");
		
		//Message,Message2,Message3
//		from("file:files/csv")
//		.convertBodyTo(String.class)
//		//.split(body(),",")
//		.split(method(splitter))
//		.to("activemq:split-queue");
		
		//Aggregate
		//Messages => Aggregate => Endpoint
		//to , 3
		from("file:files/aggregate-json")
		.unmarshal().json(JsonLibrary.Jackson, CurrencyExchange.class)
		.aggregate(simple("${body.to}"), new ArrayListAggregationStrategy())
		.completionSize(3)
		//.completionTimeout(HIGHEST)
		.to("log:aggregate-json");
		
		String routingSlip = "direct:endpoint1,direct:endpoint3";
		//String routingSlip = "direct:endpoint1,direct:endpoint2,direct:endpoint3";
		
//		from("timer:routingSlip?period=10000")
//		.transform().constant("My Message is Hardcoded")
//		.routingSlip(simple(routingSlip));
		
		//Dynamic Routing
		
		//Step 1, Step 2, Step 3

		from("timer:dynamicRouting?period={{timePeriod}}")
		.transform().constant("My Message is Hardcoded")
		.dynamicRouter(method(dynamicRouterBean));

		
		//Endpoint1
		//Endpoint2
		//Endpoint3

		
		from("direct:endpoint1")
		.wireTap("log:wire-tap") //add
		.to("{{endpoint-for-logging}}");

		from("direct:endpoint2")
		.to("log:directendpoint2");

		from("direct:endpoint3")
		.to("log:directendpoint3");


	}

}

@Component
class SplitterComponent{
	public List<String> splitInput(String body){
		return List.of("ABC", "DEF", "GHI");
	}
}

@Component
class DynamicRouterBean{
	
	Logger logger = LoggerFactory.getLogger(DynamicRouterBean.class);
	
	int invocations ;
	
	public String decideTheNextEndpoint(
				@ExchangeProperties Map<String, String> properties,
				@Headers Map<String, String> headers,
				@Body String body
			) {
		logger.info("{} {} {}", properties, headers, body);
		
		invocations++;
		
		if(invocations%3==0)
			return "direct:endpoint1";
		
		if(invocations%3==1)
			return "direct:endpoint2,direct:endpoint3";
		
		return null;
			
		
	}
}