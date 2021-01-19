### /src/main/java/com/in28minutes/microservices/currencyexchangeservice/route1/FileCopierRouter.java

```java	
   	

//    @Override
//    public void configure() throws Exception {
//    	//Context-Based Router
//    	from("file:data/from?delay=5000&noop=false&delete=false")
//    	.routeId("context-based-route")
//    	.choice() 
//    		.when(simple("${file:ext} ends with 'xml'")) //Simple
//    		.log("${body}")
//    		.log("XML File")
//    		.when(simple("${file:ext} ends with 'json'"))
//    		.log("JSON File")
//    		.when(simple("${body} contains 'USD'"))
//    		.log("Contains USD")
//    		.log("Let's combine Headers with Body - ${headers} ${body}")
//			.log("${date:now:yyyy-MM-dd HH:mm:ss}")
//    		.otherwise()
//    		.log("Unknown file extension -> ${file:ext}")
//    		.log("Let's combine Headers with Body - ${headers} ${body}")
//    	.end()
//    	.log("${messageHistory}")
//    	.log("${routeId} ${camelId} ${body}")
//        .to("log:output");
//    }

//	==
//			contains
//			starts with
//			simple("${file:ext} ends with 'xml`")
//			simple("${file:ext} in ('xml','json')")
//			simple("${file:ext} range '0..49'")

   	.log("${messageHistory}")

   	.log("${file:name} ${file:name.ext} ${file:name.noext} ${file:onlyname}")
   	.log("${file:onlyname.noext} ${file:parent} ${file:path} ${file:absolute}")
   	.log("${file:size} ${file:modified}")
   	.log("${routeId} ${camelId} ${body}")


//	@Override
//	public void configure() throws Exception {
//		from("file:data/json?delay=5000&noop=false&delete=false")
//		.choice()
//		.when(method("bean:deciderBean"))
//		.log("match")
//		.end()
//		.to("bean:someBean?method=printThis");
//	}


@Component 
class SomeBean
{
	public void printThis(
			@ExchangeProperties Map properties,
			@Headers Map headers ,
			@Header("CamelFileAbsolutePath") String specificValue,
			@Body String message) {
		System.out.println(properties);
		System.out.println(headers);
		System.out.println(specificValue);
		System.out.println(message);
	}
}

@Component 
class DeciderBean
{
	public boolean decideSomething(
			@ExchangeProperties Map properties,
			@Headers Map headers ,
			@Header("CamelFileAbsolutePath") String specificValue,
			@Body String message) {
		System.out.println("Decider" + properties);
		System.out.println("Decider" + headers);
		System.out.println("Decider" + specificValue);
		System.out.println("Decider" + message);
		return message.contains("USD");
	}
}
```
---

## Patterns

### DEFAULT PATTERN USED is PIPELINE! ONE AFTER ANOTHER

### MulticastWithWireTapRoute

```java		
from("timer:hello?period=1000")
.transform(simple("Random number ${random(0,100)}"))
.multicast()
.to("spring-rabbitmq:foo?routingKey=mykey", "spring-rabbitmq:bar?routingKey=mykey");
```

### ContentBasedRoute

```
//Exercise => Based on File extention 
//.when(header("CamelFileName").endsWith(".xml"))
//@Component
public class ContentBasedRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        from("timer:hello?period=1000").routeId("hello")
        	.transform(simple("${random(0,100)}"))
//        	.choice()
//        		//IS there are simple way to get body??
//	        	.when(e -> (Integer)e.getMessage().getBody() > 60)
//	        		.transform(simple("${body} > 60"))
//	        		.to("log:sample")
//	            .when(e -> (Integer)e.getMessage().getBody() > 50)
//	        		.transform(simple("${body} > 50"))
//	        		.to("log:sample")
//	        	.otherwise()
//	        		.transform(simple("${body} going to else"))
//	        		.to("log:sample");
        	.choice()
    		//IS there are simple way to get body??
        	.when(e -> (Integer)e.getMessage().getBody() > 60)
        		.transform(simple("${body} > 60"))
            .when(e -> (Integer)e.getMessage().getBody() > 50)
        		.transform(simple("${body} > 50"))
        	.otherwise()
        		.transform(simple("${body} going to else"))
        	.end()
        	.to("log:sample");       
        
    }

}
```

---

### /src/main/java/com/in28minutes/microservices/currencyexchangeservice/route6/PatternsRouter.java

```java
package com.in28minutes.microservices.currencyexchangeservice.route6;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.camel.AggregationStrategy;
import org.apache.camel.Body;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangeProperties;
import org.apache.camel.Headers;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.stereotype.Component;

import com.in28minutes.microservices.currencyexchangeservice.CurrencyExchange;

//@Component
public class PatternsRouter extends RouteBuilder {

	
<dependency>
	<groupId>org.apache.camel.springboot</groupId>
	<artifactId>camel-csv-starter</artifactId>
</dependency>


//	@Override
//	public void configure() throws Exception {
//		from("file:data/csv?delay=5000&noop=false&delete=false")
//		.unmarshal().csv()
//	    .split(body())
//	    .to("bean:someBean?method=printThis");		
//	}
	
	@Override
	public void configure() throws Exception {
		from("file:data/csv?delay=5000&noop=false&delete=false")
		.convertBodyTo(String.class)
	    .to("bean:someBean?method=printThis");
	}	

	
//	@Override
	//	public void configure() throws Exception {
	//		from("file:data/csv?delay=5000&noop=false&delete=false")
	//		.split(bodyAs(String.class),",")
	//	    .to("bean:someBean6?method=printThis");		
	//	}	

	//	@Override
	//	public void configure() throws Exception {
	//		from("file:data/csv?delay=5000&noop=false&delete=false")
	//		.split(simple("${bean:splitBean}"))
	//	    .to("bean:someBean6?method=printThis");		
	//	}	
	
//	@Override
//	public void configure() throws Exception {
//		from("file:data/csv?delay=5000&noop=false&delete=false")
//		.unmarshal().json(JsonLibrary.Jackson, CurrencyExchange.class)
//		.aggregate(simple("${body.to}"), new ArrayListAggregationStrategy())
//		.completionSize(3)
//		//.completionTimeout(10000)
//		//.completionPredicate
//	    .to("direct:print");
//		
//		from("direct:print")
//		.to("bean:someBean6?method=printThis");
//	}	

//	@Override
//	public void configure() throws Exception {
//		from("file:data/csv?delay=5000&noop=false&delete=false")
//		.unmarshal().json(JsonLibrary.Jackson, CurrencyExchange.class)
//		.routingSlip(simple("direct:route3,direct:route2"));
//		
//		from("direct:route1")
//		.to("bean:someBean6?method=printThis");
//		
//		from("direct:route2")
//		.to("bean:someBean6?method=printThis2");
//		
//		from("direct:route3")
//		.to("bean:someBean6?method=printThis3");
//	}

//	@Override
//	public void configure() throws Exception {
//		
//		from("file:data/csv?delay=5000&noop=false&delete=false")
//		.unmarshal().json(JsonLibrary.Jackson, CurrencyExchange.class)
//		.dynamicRouter(method("dynamicRoutingDecider"));
//		
//		from("direct:route1")
//		.to("bean:someBean6?method=printThis");
//		
//		from("direct:route2")
//		.to("bean:someBean6?method=printThis2");
//		
//		from("direct:route3")
//		.to("bean:someBean6?method=printThis3");
//
//		
//	}

	
//	@Override
//	public void configure() throws Exception {
//		from("file:data/csv?delay=5000&noop=false&delete=false")
//		.unmarshal().json(JsonLibrary.Jackson, CurrencyExchange.class)
//		.to("direct:start");
//	
	
//		Does .failover(MyException.class) work?
//		from("direct:start")
//	    .loadBalance().roundRobin()
//	    	.to("direct:route1")
//	    	.to("direct:route2")
//	    	.to("direct:route3")
//	    .end();
	
	
//
//		from("direct:route1")
//		.to("bean:someBean6?method=printThis");
//		
//		from("direct:route2")
//		.to("bean:someBean6?method=printThis2");
//		
//		from("direct:route3")
//		.to("bean:someBean6?method=printThis3");
//
//		
//	}

	
	@Override
	public void configure() throws Exception {
		from("file:data/csv?delay=5000&noop=false&delete=false")
		.unmarshal().json(JsonLibrary.Jackson, CurrencyExchange.class)
		.to("direct:start");

		from("direct:start")
		.multicast().parallelProcessing()
	    	.to("direct:route1")
	    	.to("direct:route2")
	    	.to("direct:route3")
	    .end();

		from("direct:route1")
		.delay(1000)
		.to("bean:someBean6?method=printThis");
		
		from("direct:route2")
		.delay(200)
		.to("bean:someBean6?method=printThis2");
		
		from("direct:route3")
		.delay(500)
		.to("bean:someBean6?method=printThis3");

		
	}

}

@Component 
class SplitBean
{
	public List<String> splitThis(
			@ExchangeProperties Map properties,
			@Headers Map headers ,
			@Body String message) {
		return List.of("Message 1", "Message 2", "Message 3");
	}
	
}

@Component 
class SomeBean6
{
	public void printThis(
			@ExchangeProperties Map properties,
			@Headers Map headers ,
			@Body String message) {
		System.out.println(properties);
		System.out.println(headers);
		System.out.println(message);
	}
	
	public void printThis2(
			@ExchangeProperties Map properties,
			@Headers Map headers ,
			@Body String message) {
		System.out.println("printThis2" + properties);
		System.out.println("printThis2" + headers);
		System.out.println("printThis2" + message);
	}
	
	public void printThis3(
			@ExchangeProperties Map properties,
			@Headers Map headers ,
			@Body String message) {
		System.out.println("printThis3" + properties);
		System.out.println("printThis3" + headers);
		System.out.println("printThis3" + message);
	}
}

class ArrayListAggregationStrategy implements AggregationStrategy {

    public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
        Object newBody = newExchange.getIn().getBody();
        ArrayList<Object> list = null;
        if (oldExchange == null) {
            list = new ArrayList<Object>();
            list.add(newBody);
            newExchange.getIn().setBody(list);
            return newExchange;
        } else {
            list = oldExchange.getIn().getBody(ArrayList.class);
            list.add(newBody);
            return oldExchange;
        }
    }
}

@Component 
class DynamicRoutingDecider
{
	//NOT PERFECT!
	private int invoked=0;

	public String decideRoutingSlip(
			@ExchangeProperties Map properties,
			@Headers Map headers ,
			@Body String message) {
		invoked++;

		System.out.println("decideRoutingSlip " + properties);
		System.out.println("decideRoutingSlip " + headers);
		System.out.println("decideRoutingSlip " + message);

		
		
		if(invoked==1)
			return "direct:route1,direct:route2";
		if(invoked==2)
			return "direct:route3";
		return null;
		
	}
	
}
```
---

### /src/main/java/com/in28minutes/microservices/currencyexchangeservice/route6/RouteWithMessageFilters.java

```java
package com.in28minutes.microservices.currencyexchangeservice.route6;

import org.apache.camel.builder.RouteBuilder;

//@Component
public class RouteWithMessageFilters extends RouteBuilder {

	@Override
	public void configure() throws Exception {
		from("timer:hello?period=1000").routeId("hello")
		.transform(simple("${random(0,100)}"))
		.filter(e -> (Integer) e.getMessage().getBody() > 60)
		.to("log:sample");
	}

}
```
---

### /src/main/resources/application.properties

```properties
spring.application.name=currency-exchange
server.port=8000

spring.jpa.show-sql=true
spring.datasource.url=jdbc:h2:mem:testdb
spring.h2.console.enabled=true

camel.springboot.name = CamelWithSpringBoot
message = Welcome to in28Minutes
interval = 5000
management.info.camel.enabled=true
management.info.camel.verbose=true

spring.rabbitmq.host = localhost
spring.rabbitmq.port = 5672
spring.rabbitmq.username = guest
spring.rabbitmq.password = guest

#queues are automatic created if NOT present
camel.component.spring-rabbitmq.auto-declare = true


management.endpoints.web.exposure.include=mappings,metrics

spring.activemq.broker-url=tcp://localhost:61616

camel.component.kafka.brokers=localhost:9092
```
---


### Best Practices

#### Configurable
```
route.from = your:queue
from("{{route.from}}").to("{{route.to}}");
```

#### Run Stand Alone

```
camel.springboot.main-run-controller = true
camel.springboot.duration-max-seconds = 60
```

#### Explore Metrics

```
management.endpoints.web.exposure.include=mappings,metrics,shutdown
```

#### Security

```
#camel.component.activemq.password
#camel.component.activemq.username

keytool -genseckey -alias myDesKey -keypass someKeyPassword -keystore myDesKey.jceks -storepass someKeystorePassword -v -storetype JCEKS -keyalg DES



<dependency>
  <groupId>org.apache.camel.springboot</groupId>
  <artifactId>camel-crypto-starter</artifactId>
  <version>${camel.version}</version>
</dependency>

@Component
public class ActiveMqReceiverRouter extends RouteBuilder {
	
	@Override
	public void configure() throws Exception {
		CryptoDataFormat sharedKeyCrypto = createEncryptor();
		from("activemq:my-activemq-queue")
		.unmarshal(sharedKeyCrypto)
		.to("log:received-message-from-active-mq");

	}

	private CryptoDataFormat createEncryptor() throws KeyStoreException, IOException, NoSuchAlgorithmException,
			CertificateException, UnrecoverableKeyException {
		KeyStore keyStore = KeyStore.getInstance("JCEKS");
		ClassLoader classLoader = getClass().getClassLoader();
		keyStore.load(classLoader.getResourceAsStream("myDesKey.jceks"), "someKeystorePassword".toCharArray());
		Key sharedKey = keyStore.getKey("myDesKey", "someKeyPassword".toCharArray());

		CryptoDataFormat sharedKeyCrypto = new CryptoDataFormat("DES", sharedKey);
		return sharedKeyCrypto;
	}

}

```

### Error Handling


##### Use Appropriate Logging Levels
```
# to configure logging levels
#logging.level.org.springframework = INFO
#logging.level.org.apache.camel.spring.boot = INFO
#logging.level.org.springframework.amqp.rabbit = DEBUG
#logging.level.org.apache.camel.impl = DEBUG
```

```java
package com.in28minutes.microservices.currencyexchangeservice.route6;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class ErrorHandlingRouter extends RouteBuilder {
	
	@Override
	public void configure() throws Exception {
		
		//Give Routes Good Names
		//.routeId("first-route")
		
		//Use .log to log meanining intermediate info
		//.to("log:myLog?showAll=true&multiline=true")
		//.to("log:throughput?groupSize=10")
		
		//Tracing
		//getContext().setTracing(true);

		// .throttle(3).timePeriodMillis(10000)
		//.wiretap
		//errorHandler(defaultErrorHandler().maximumRedeliveries(3));
		//errorHandler(deadLetterChannel("direct:error"));//useOriginalMessage
	
		onException(Exception.class)
		.process(System.err::println);
		
		from("file:data/csv?delay=5000&noop=false&delete=false")
		.to("direct:route1");

		
//		from("file:data/csv?delay=5000&noop=false&delete=false")
//		.loadBalance().failover()
//			.to("direct:route1")
//			.to("direct:route2")
//			.end();
		
		from("direct:route1")
		.to("bean:throwErrorBean?method=throwErrorAtRandom1");
		
		from("direct:route2")
		.to("bean:throwErrorBean?method=throwErrorAtRandom2");
		
		from("direct:error")
		.to("log:error");
		
		
	}
	
}

@Component
class ThrowErrorBean {
	
	public void throwErrorAtRandom1() {
		if(Math.random()>0.5) {
			throw new RuntimeException("error");
		} else {
			System.out.println("Successfully Processed");
		}
			
	}

	public void throwErrorAtRandom2() {
		System.out.println("throwErrorAtRandom2");
		if(Math.random()>0.9)
			throw new RuntimeException("error");
	}
	
}
```
