```

		<dependency>
			<groupId>org.apache.camel.springboot</groupId>
			<artifactId>camel-spring-boot-starter</artifactId>
			<version>3.7.0</version>
		</dependency>

		<!-- docker run -p 61616:61616 -p 8161:8161 rmohr/activemq -->
		<dependency>
			<groupId>org.apache.camel.springboot</groupId>
			<artifactId>camel-activemq-starter</artifactId>
			<version>3.7.0</version>
		</dependency>

		<dependency>
			<groupId>org.apache.camel.springboot</groupId>
			<artifactId>camel-kafka-starter</artifactId>
			<version>3.7.0</version>
		</dependency>

		<dependency>
			<groupId>org.apache.camel.springboot</groupId>
			<artifactId>camel-http-starter</artifactId>
			<version>3.7.0</version>
		</dependency>




		<dependency>
			<groupId>org.apache.camel.springboot</groupId>
			<artifactId>camel-jackson-starter</artifactId>
			<version>3.7.0</version>
		</dependency>


```


### /src/main/java/com/in28minutes/enterprise/integration/camelmicroservicea/routes/AMyFirstTimerRouter.java

```java
package com.in28minutes.enterprise.integration.camelmicroservicea.routes;

import java.time.LocalDateTime;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

//@Component
public class AMyFirstTimerRouter extends RouteBuilder{
	
	@Autowired
	GetCurrentTimeBean getCurrentTimeBean; 

	@Override
	public void configure() throws Exception {
		from("timer:first-timer?period=1000") //{{interval}}
		//.transform().constant("time now is" + LocalDateTime.now())
		//.bean("getCurrentTimeBean")
//		.bean(getCurrentTimeBean,"getCurrentTimeAsString")
//		.transform(simple("The string I got is ${body}"))
		.to("log:first-timer");
	}

}

@Component
class GetCurrentTimeBean {
	public String getCurrentTimeAsString() {
		return "Time now is" + LocalDateTime.now();
	}
}
```
---

### /src/main/java/com/in28minutes/enterprise/integration/camelmicroservicea/routes/BPlayingWithFilesRouter.java

```java
package com.in28minutes.enterprise.integration.camelmicroservicea.routes;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

//@Component
public class BPlayingWithFilesRouter extends RouteBuilder{
	
	@Autowired
	GetCurrentTimeBean getCurrentTimeBean; 

	@Override
	public void configure() throws Exception {
		from("file:files/input")
		.log("${body}")
//		.to("log:first-timer");
		.to("file:files/output");
	}

}
```
---

### /src/main/java/com/in28minutes/enterprise/integration/camelmicroservicea/routes/CActiveMQSenderRoute.java

```java
package com.in28minutes.enterprise.integration.camelmicroservicea.routes;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.kafka.KafkaConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

//@Component
public class CActiveMQSenderRoute extends RouteBuilder {

	@Autowired
	GetCurrentTimeBean getCurrentTimeBean; 
	
    @Override
    public void configure() throws Exception {

//        from("timer:bar")
//            //.setBody(constant("Hello from Camel"))
//            .bean(getCurrentTimeBean)
//            .to("activemq:my-custom-queue");
    	
//    	from("file:files/json")
//    	.to("activemq:my-custom-queue");

    	//sudo vi /private/etc/hosts
        from("file:files/json")
        .setHeader(KafkaConstants.KEY, constant("Camel")) // Key of the message
       .log("Sending message to Kafka : ${body}")
       .to("kafka:myTopic");
    }

}
```
---

### /src/main/java/com/in28minutes/enterprise/integration/camelmicroservicea/routes/DRestAPIConsumerRouter.java

```java
package com.in28minutes.enterprise.integration.camelmicroservicea.routes;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

//@Component
public class DRestAPIConsumerRouter extends RouteBuilder {

    @Override
    public void configure() throws Exception {
    	restConfiguration().host("localhost").port(8000);

        from("timer:hello?period=5000")
            .setHeader("from", () -> "USD")
            .setHeader("to", () -> "INR")
            .to("rest:get:/currency-exchange/from/{from}/to/{to}")
            .log("${body}");

    	
    }

}
```
---

### /src/main/resources/application.properties

```properties
spring.activemq.broker-url=tcp://localhost:61616

camel.component.kafka.brokers=localhost:9092
```
---


### /src/main/java/com/in28minutes/enterprise/integration/camelmicroserviceb/ActiveMQReceiverRoute.java

```java
package com.in28minutes.enterprise.integration.camelmicroserviceb;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ActiveMQReceiverRoute extends RouteBuilder {
	
	@Autowired
	CurrencyExchangeRepository currencyExchangeRepository;

	@Override
	public void configure() throws Exception {
		from("activemq:my-custom-queue")
		.unmarshal().json(JsonLibrary.Jackson, CurrencyExchange.class)
		.bean(currencyExchangeRepository)
		.to("log:received-from-active-mq");		

		from("kafka:myTopic")
		.unmarshal().json(JsonLibrary.Jackson, CurrencyExchange.class)
		//.bean(currencyExchangeRepository)
		.to("log:received-from-kafka");

	}
}

@Component
class CurrencyExchangeRepository{
	public void save(CurrencyExchange currencyExchange) {
		System.out.print("Saved to database " + currencyExchange);
	}
}
```
---


### /src/main/java/com/in28minutes/enterprise/integration/camelmicroserviceb/CurrencyExchange.java

```java
package com.in28minutes.enterprise.integration.camelmicroserviceb;

import java.math.BigDecimal;

public class CurrencyExchange {
	
	private Long id;
	
	private String from;
	
	private String to;

	private BigDecimal conversionMultiple;

	private String environment;

	public CurrencyExchange() {
		
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public BigDecimal getConversionMultiple() {
		return conversionMultiple;
	}

	public void setConversionMultiple(BigDecimal conversionMultiple) {
		this.conversionMultiple = conversionMultiple;
	}

	public String getEnvironment() {
		return environment;
	}

	public void setEnvironment(String environment) {
		this.environment = environment;
	}

	public CurrencyExchange(Long id, String from, String to, BigDecimal conversionMultiple, String environment) {
		super();
		this.id = id;
		this.from = from;
		this.to = to;
		this.conversionMultiple = conversionMultiple;
		this.environment = environment;
	}

	@Override
	public String toString() {
		return "CurrencyExchange [id=" + id + ", from=" + from + ", to=" + to + ", conversionMultiple="
				+ conversionMultiple + ", environment=" + environment + "]";
	}	

}
```
---

### /src/main/java/com/in28minutes/enterprise/integration/camelmicroserviceb/CurrencyExchangeController.java

```java
package com.in28minutes.enterprise.integration.camelmicroserviceb;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CurrencyExchangeController {
	
	private Logger logger = LoggerFactory.getLogger(CurrencyExchangeController.class);
			
	@GetMapping("/currency-exchange/from/{from}/to/{to}")
	public CurrencyExchange retrieveExchangeValue(
			@PathVariable String from,
			@PathVariable String to) {
		
		logger.info("retrieveExchangeValue called with {} to {}", from, to);
				
		return new CurrencyExchange(1000L, from, to, BigDecimal.TEN,"kafka");
		
	}

}
```
---

### /src/main/resources/application.properties

```properties
server.port=8000

spring.activemq.broker-url=tcp://localhost:61616

camel.component.kafka.brokers=localhost:9092
#camel.component.activemq.password
#camel.component.activemq.username
```
---



### /pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>
	<parent>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-parent</artifactId>
		<version>2.4.1</version>
		<relativePath /> <!-- lookup parent from repository -->
	</parent>
	<groupId>com.in28minutes.microservices</groupId>
	<artifactId>currency-exchange-service</artifactId>
	<version>0.0.1-SNAPSHOT</version> <!-- CHANGE-KUBERNETES -->
	<name>currency-exchange-service-kubernetes</name> <!-- CHANGE-KUBERNETES -->
	<description>1 Demo project for Spring Boot</description>

	<properties>
		<java.version>15</java.version>
		<spring-cloud.version>2020.0.0</spring-cloud.version>
		<camel.version>3.7.0</camel.version>
	</properties>

	<dependencies>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-actuator</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-web</artifactId>
		</dependency>

		<dependency>
			<groupId>org.apache.camel.springboot</groupId>
			<artifactId>camel-spring-boot-starter</artifactId>
		</dependency>

		<dependency>
			<groupId>org.apache.camel.springboot</groupId>
			<artifactId>camel-servlet-starter</artifactId>
		</dependency>


		<dependency>
			<groupId>org.apache.camel.springboot</groupId>
			<artifactId>camel-jackson-starter</artifactId>
		</dependency>

		<dependency>
			<groupId>org.apache.camel.springboot</groupId>
			<artifactId>camel-jacksonxml-starter</artifactId>
		</dependency>

		<dependency>
			<groupId>org.apache.camel.springboot</groupId>
			<artifactId>camel-csv-starter</artifactId>
		</dependency>

		<dependency>
			<groupId>org.apache.camel.springboot</groupId>
			<artifactId>camel-stream-starter</artifactId>
		</dependency>

		<dependency>
			<groupId>org.apache.camel.springboot</groupId>
			<artifactId>camel-rest-openapi-starter</artifactId>
		</dependency>

		<dependency>
			<groupId>org.apache.camel.springboot</groupId>
			<artifactId>camel-http-starter</artifactId>
		</dependency>


		<dependency>
			<groupId>org.apache.camel.springboot</groupId>
			<artifactId>camel-spring-rabbitmq-starter</artifactId>
			<version>3.8.0-SNAPSHOT</version>
		</dependency>

		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-amqp</artifactId>
		</dependency>

		<dependency>
			<groupId>org.apache.camel.springboot</groupId>
			<artifactId>camel-activemq-starter</artifactId>
		</dependency>


		<dependency>
			<groupId>org.apache.camel.springboot</groupId>
			<artifactId>camel-avro-starter</artifactId>
		</dependency>
		<dependency>
			<groupId>org.apache.camel.springboot</groupId>
			<artifactId>camel-kafka-starter</artifactId>
		</dependency>

		<dependency>
			<groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-starter-sleuth</artifactId>
		</dependency>

		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-aop</artifactId>
		</dependency>

		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-devtools</artifactId>
			<scope>runtime</scope>
			<optional>true</optional>
		</dependency>

		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-test</artifactId>
			<scope>test</scope>
		</dependency>
	</dependencies>

	<dependencyManagement>
		<dependencies>
			<dependency>
				<groupId>org.springframework.cloud</groupId>
				<artifactId>spring-cloud-dependencies</artifactId>
				<version>${spring-cloud.version}</version>
				<type>pom</type>
				<scope>import</scope>
			</dependency>
			<dependency>
				<groupId>org.apache.camel.springboot</groupId>
				<artifactId>camel-spring-boot-bom</artifactId>
				<version>${camel.version}</version>
				<type>pom</type>
				<scope>import</scope>
			</dependency>
		</dependencies>
	</dependencyManagement>

	<build>
		<plugins>
			<plugin>
				<groupId>org.springframework.boot</groupId>
				<artifactId>spring-boot-maven-plugin</artifactId>
				<configuration>
					<image>
						<name>in28min/mmv2-${project.artifactId}:${project.version}</name>
					</image>
					<pullPolicy>IF_NOT_PRESENT</pullPolicy>
				</configuration>
			</plugin>
		</plugins>
	</build>

	<repositories>
		<repository>
			<id>spring-milestones</id>
			<name>Spring Milestones</name>
			<url>https://repo.spring.io/milestone</url>
		</repository>
		<repository>
			<id>apache.snapshots</id>
			<url>https://repository.apache.org/snapshots/</url>
			<name>Apache Snapshot Repo</name>
			<snapshots>
				<enabled>true</enabled>
			</snapshots>
			<releases>
				<enabled>false</enabled>
			</releases>
		</repository>
	</repositories>

</project>
```
---

### /src/main/java/com/in28minutes/microservices/currencyexchangeservice/CurrencyExchange.java

```java
package com.in28minutes.microservices.currencyexchangeservice;

import java.math.BigDecimal;

public class CurrencyExchange {
	
	private Long id;
	
	private String from;
	
	private String to;

	private BigDecimal conversionMultiple;
	private String environment;

	public CurrencyExchange() {
		
	}
	
	public CurrencyExchange(Long id, String from, String to, BigDecimal conversionMultiple) {
		super();
		this.id = id;
		this.from = from;
		this.to = to;
		this.conversionMultiple = conversionMultiple;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public BigDecimal getConversionMultiple() {
		return conversionMultiple;
	}

	public void setConversionMultiple(BigDecimal conversionMultiple) {
		this.conversionMultiple = conversionMultiple;
	}

	
	public String getEnvironment() {
		return environment;
	}

	public void setEnvironment(String environment) {
		this.environment = environment;
	}

	@Override
	public String toString() {
		return "CurrencyExchange [id=" + id + ", from=" + from + ", to=" + to + ", conversionMultiple="
				+ conversionMultiple + ", environment=" + environment + "]";
	}
	

	
}
```
---

### /src/main/java/com/in28minutes/microservices/currencyexchangeservice/CurrencyExchangeController.java

```java
package com.in28minutes.microservices.currencyexchangeservice;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CurrencyExchangeController {
	
	private Logger logger = LoggerFactory.getLogger(CurrencyExchangeController.class);
			
	@GetMapping("/currency-exchange/from/{from}/to/{to}")
	public CurrencyExchange retrieveExchangeValue(
			@PathVariable String from,
			@PathVariable String to) {
		
		logger.info("retrieveExchangeValue called with {} to {}", from, to);
				
		return new CurrencyExchange(1000L, from, to, BigDecimal.TEN);
		
	}

}
```
---

### /src/main/java/com/in28minutes/microservices/currencyexchangeservice/CurrencyExchangeServiceApplication.java

```java
package com.in28minutes.microservices.currencyexchangeservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CurrencyExchangeServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CurrencyExchangeServiceApplication.class, args);
	}

}
```
---

### /src/main/java/com/in28minutes/microservices/currencyexchangeservice/route1/FileCopierRouter.java

```java
package com.in28minutes.microservices.currencyexchangeservice.route1;

import java.util.Map;

import org.apache.camel.Body;
import org.apache.camel.ExchangeProperties;
import org.apache.camel.Header;
import org.apache.camel.Headers;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

//@Component
public class FileCopierRouter extends RouteBuilder {
	
//	==
//			contains
//			starts with
//			simple("${file:ext} ends with 'xml`")
//			simple("${file:ext} in ('xml','json')")
//			simple("${file:ext} range '0..49'")


//    @Override
//    public void configure() throws Exception {
//    	from("file:data/from?delay=5000&noop=false&delete=false")
//    	.process((exchange) -> {
//    		System.out.println(exchange);
//    		System.out.println(exchange.getContext());
//    		System.out.println(exchange.getExchangeId());
//    		System.out.println(exchange.getMessage().getBody());
//    		//exchange.getMessage().setHeader(null, exchange);    		
//    	})
//    	.log("    ${messageHistory}")
//        .to("log:output");
//    }

//    @Override
//    public void configure() throws Exception {
//    	//Context-Based Router
//    	from("file:data/xml?delay=5000&noop=false&delete=false")
//    	//.unmarshal().jacksonxml(CurrencyExchange.class)
//    	.log("${file:name} ${file:name.ext} ${file:name.noext} ${file:onlyname}")
//    	.log("${file:onlyname.noext} ${file:parent} ${file:path} ${file:absolute}")
//    	.log("${file:size} ${file:modified}")
////    	.log("${messageHistory}")
////    	.log("${routeId} ${camelId} ${body}")
//        .to("log:output");
//    }
	
    
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

//    @Override
//    public void configure() throws Exception {
//    	//Context-Based Router
//    	from("file:data/json?delay=5000&noop=false&delete=false")
//    	.unmarshal().json(JsonLibrary.Jackson, CurrencyExchange.class)
//    	.log("${body}")
//    	.log("${messageHistory}")
//    	.log("${routeId} ${camelId} ${body}")
//        .to("log:output");
//    }


//    @Override
//    public void configure() throws Exception {
//    	//Context-Based Router
//    	from("file:data/xml?delay=5000&noop=false&delete=false")
//    	//.unmarshal().jacksonxml(CurrencyExchange.class)
//    	.log("${body}")
//    	.log("${messageHistory}")
//    	.log("${routeId} ${camelId} ${body}")
//    	.transform().simple("${bodyAs(String)}")
//        .to("log:output");
//    }

//	@Override
//	public void configure() throws Exception {
//		from("file:data/xml?delay=5000&noop=false&delete=false")
//		.unmarshal().jacksonxml(CurrencyExchange.class)
//		.marshal(new JacksonDataFormat())
//		.transform().body()
//		.to("log:output");
//		
//	}

//	@Override
//	public void configure() throws Exception {
//		from("file:data/json?delay=5000&noop=false&delete=false")
//		.unmarshal().json(JsonLibrary.Jackson, CurrencyExchange.class)
//		.marshal(new JacksonXMLDataFormat())
//		.to("log:output");
//	}
	
//	@Override
//	public void configure() throws Exception {
//		from("file:data/json?delay=5000&noop=false&delete=false")
//		.choice()
//		.when(method("bean:deciderBean"))
//		.log("match")
//		.end()
//		.to("bean:someBean?method=printThis");
//	}

//	@Override
//	public void configure() throws Exception {
//		from("file:data/json?delay=5000&noop=false&delete=false")
//		.transform(body().regexReplaceAll("conversionMultiple", "conversion_multiple"))
//		.to("bean:someBean?method=printThis");
//	}

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

}

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

### /src/main/java/com/in28minutes/microservices/currencyexchangeservice/route1/HelloBean.java

```java
package com.in28minutes.microservices.currencyexchangeservice.route1;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component("helloBean")
public class HelloBean {
	
	private int counter;

	@Value("${message}")
	private String message;

	public String saySomething(String body) {
        return String.format("%s I am invoked %d times", message, ++counter);
    }
	
}
```
---

### /src/main/java/com/in28minutes/microservices/currencyexchangeservice/route1/HelloWorldTimerToOutputRoute.java

```java
package com.in28minutes.microservices.currencyexchangeservice.route1;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;

//@Component
public class HelloWorldTimerToOutputRoute extends RouteBuilder {

    @Autowired
    private HelloBean helloBean;

    @Override
    public void configure() throws Exception {
        from("timer:hello?period={{interval}}").routeId("hello")
                .bean(helloBean, "saySomething")
                .to("stream:out");
    }

}
```
---

### /src/main/java/com/in28minutes/microservices/currencyexchangeservice/route1/KafkaRoute.java

```java
package com.in28minutes.microservices.currencyexchangeservice.route1;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.kafka.KafkaConstants;
import org.springframework.stereotype.Component;

//@Component
public class KafkaRoute extends RouteBuilder {

    /**
     * Let's configure the Camel routing rules using Java code...
     */
    @Override
    public void configure() throws Exception {

        // Kafka Producer
        from("file:data/from?noop=true&delay=5000")
                 .setHeader(KafkaConstants.KEY, constant("Camel")) // Key of the message
                .log("Sending message to Kafka : ${body}")
                .to("kafka:myTopic");

        // Kafka Consumer
        from("kafka:myTopic")
                .log("Message received from Kafka : ${body}")
                .log("    ${headers}")
        		.log("    ${exchange}")
        		.log("    ${messageHistory}");
    }

}
```
---

### /src/main/java/com/in28minutes/microservices/currencyexchangeservice/route2/MagicNumber.java

```java
package com.in28minutes.microservices.currencyexchangeservice.route2;
public interface MagicNumber {
    void onMagicNumber(String message);
}
```
---

### /src/main/java/com/in28minutes/microservices/currencyexchangeservice/route2/NumberPojo.java

```java
package com.in28minutes.microservices.currencyexchangeservice.route2;

import org.apache.camel.Consume;
import org.apache.camel.Produce;
import org.springframework.stereotype.Component;

@Component
public class NumberPojo {

    // sends the message to the stream:out endpoint but hidden behind this interface
    // so the client java code below can use the interface method instead of Camel's
    // FluentProducerTemplate or ProducerTemplate APIs
    @Produce("stream:out")
    private MagicNumber magic;

    // only consume when the predicate matches, eg when the message body is lower than 100
    @Consume(value = "direct:numbers", predicate = "${body} < 100")
    public void lowNumber(int number) {
        magic.onMagicNumber("Got a low number " + number);
    }

    // only consume when the predicate matches, eg when the message body is higher or equal to 100
    @Consume(value = "direct:numbers", predicate = "${body} >= 100")
    public void highNumber(int number) {
        magic.onMagicNumber("Got a high number " + number);
    }

}
```
---

### /src/main/java/com/in28minutes/microservices/currencyexchangeservice/route2/TimerRandomNumberRoute.java

```java
package com.in28minutes.microservices.currencyexchangeservice.route2;
import org.apache.camel.builder.RouteBuilder;

//@Component
public class TimerRandomNumberRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        from("timer:number?period=1000")
            .transform().simple("${random(0,200)}")
            .to("direct:numbers");
    }

}
```
---

### /src/main/java/com/in28minutes/microservices/currencyexchangeservice/route3/ConsumeRestRoute.java

```java
package com.in28minutes.microservices.currencyexchangeservice.route3;

import org.apache.camel.builder.RouteBuilder;

//@Component
public class ConsumeRestRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        
    	restConfiguration().host("localhost").port(8000);

        from("timer:hello?period=5000")
            .setHeader("from", () -> "USD")
            .setHeader("to", () -> "INR")
            .to("rest:get:/currency-exchange/from/{from}/to/{to}")
            .log("${body}");
    }

}
```
---

### /src/main/java/com/in28minutes/microservices/currencyexchangeservice/route3/RestRouteWithTransformation.java

```java
package com.in28minutes.microservices.currencyexchangeservice.route3;

import org.apache.camel.builder.RouteBuilder;

//@Component
public class RestRouteWithTransformation extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        
    	restConfiguration().host("localhost").port(8000);

        from("timer:hello?period=5000")
            .setHeader("from", () -> "USD")
            .setHeader("to", () -> "INR")
            .to("rest:get:/currency-exchange/from/{from}/to/{to}")
            .log("${body}");
    }

}
```
---

### /src/main/java/com/in28minutes/microservices/currencyexchangeservice/route4/ActiveMQReceiverRoute.java

```java
package com.in28minutes.microservices.currencyexchangeservice.route4;

import org.apache.camel.builder.RouteBuilder;

//@Component
public class ActiveMQReceiverRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {
    	from("activemq:foo")
    	.process((exchange)->{
    		
    		String newBody = exchange.getMessage().getBody() + " Append";
    		exchange.getMessage().setBody(newBody);
    	})
    	.to("log:sample");

    }

}
```
---

### /src/main/java/com/in28minutes/microservices/currencyexchangeservice/route4/ActiveMQSenderRoute.java

```java
package com.in28minutes.microservices.currencyexchangeservice.route4;

import org.apache.camel.builder.RouteBuilder;

//@Component
public class ActiveMQSenderRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        from("timer:bar")
            .setBody(constant("Hello from Camel"))
            .to("activemq:foo");
    }

}
```
---

### /src/main/java/com/in28minutes/microservices/currencyexchangeservice/route4/RabbitMqReceiverRouter.java

```java
package com.in28minutes.microservices.currencyexchangeservice.route4;

import org.apache.camel.builder.RouteBuilder;

//@Component
public class RabbitMqReceiverRouter extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        from("spring-rabbitmq:foo?queues=myqueue&routingKey=mykey")
            .log("From RabbitMQ: ${body}");
    }

}
```
---

### /src/main/java/com/in28minutes/microservices/currencyexchangeservice/route4/RabbitMqSenderRouter.java

```java
package com.in28minutes.microservices.currencyexchangeservice.route4;

import org.apache.camel.builder.RouteBuilder;

//@Component
public class RabbitMqSenderRouter extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        from("timer:hello?period=1000")
            .transform(simple("Random number ${random(0,100)}"))
            .to("spring-rabbitmq:foo?routingKey=mykey");

        from("timer:hello?period=2000")
            .transform(simple("Bigger random number ${random(100,200)}"))
            .to("spring-rabbitmq:foo?routingKey=mykey");
    }

}
```
---

### /src/main/java/com/in28minutes/microservices/currencyexchangeservice/route5/MulticastWithWireTapRoute.java

```java
package com.in28minutes.microservices.currencyexchangeservice.route5;

import org.apache.camel.builder.RouteBuilder;

//@Component
public class MulticastWithWireTapRoute extends RouteBuilder {

	@Override
	public void configure() throws Exception {        
		
        from("timer:hello?period=1000")
        .transform(simple("Random number ${random(0,100)}"))
        .multicast()
        .to("spring-rabbitmq:foo?routingKey=mykey", "spring-rabbitmq:bar?routingKey=mykey");
    	
        from("spring-rabbitmq:foo?queues=myqueue&routingKey=mykey")
        .wireTap("log:sample")
        .log("From RabbitMQ foo: ${body}");
        
        from("spring-rabbitmq:bar?queues=myqueue&routingKey=mykey")
        .log("From RabbitMQ bar: ${body}");


	}

}
```
---

### /src/main/java/com/in28minutes/microservices/currencyexchangeservice/route6/ContentBasedRoute.java

```java
package com.in28minutes.microservices.currencyexchangeservice.route6;

import org.apache.camel.builder.RouteBuilder;


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

### /src/main/java/com/in28minutes/microservices/currencyexchangeservice/route6/ErrorHandlingRouter.java

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

	
	//DEFAULT PATTERN USED is PIPELINE! ONE AFTER ANOTHER
	
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

# to configure logging levels
#logging.level.org.springframework = INFO
#logging.level.org.apache.camel.spring.boot = INFO
#logging.level.org.springframework.amqp.rabbit = DEBUG
#logging.level.org.apache.camel.impl = DEBUG

management.endpoints.web.exposure.include=mappings,metrics

spring.activemq.broker-url=tcp://localhost:61616

camel.component.kafka.brokers=localhost:9092
```
---

### /src/test/java/com/in28minutes/microservices/currencyexchangeservice/CurrencyExchangeServiceApplicationTests.java

```java
package com.in28minutes.microservices.currencyexchangeservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CurrencyExchangeServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
```
---

### /urls.txt

```
Currency Exchange Service
- http://localhost:8000/currency-exchange/from/USD/to/INR

Currency Conversion Service
- http://localhost:8100/currency-conversion-feign/from/USD/to/INR/quantity/10

http://35.223.40.231:8000/actuator/health/
http://35.223.40.231:8000/actuator/health/liveness
http://35.223.40.231:8000/actuator/health/readiness
```
---
