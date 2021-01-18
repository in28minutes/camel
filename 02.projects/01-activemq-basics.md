<!---
Current Directory : /in28Minutes/git/camel/02.projects
-->

## Complete Code Example


### /docker-compose.yaml

```
version: '2'

services:
  zookeeper:
    image: 'docker.io/bitnami/zookeeper:3-debian-10'
    ports:
      - '2181:2181'
    volumes:
      - 'zookeeper_data:/bitnami'
    environment:
      - ALLOW_ANONYMOUS_LOGIN=yes
  kafka:
    image: 'docker.io/bitnami/kafka:2-debian-10'
    ports:
      - '9092:9092'
    volumes:
      - 'kafka_data:/bitnami'
    environment:
      - KAFKA_CFG_ZOOKEEPER_CONNECT=zookeeper:2181
      - ALLOW_PLAINTEXT_LISTENER=yes
    depends_on:
      - zookeeper

volumes:
  zookeeper_data:
    driver: local
  kafka_data:
    driver: local
```
---

### /camel-microservice-b/pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>
	<parent>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-parent</artifactId>
		<version>2.4.2</version>
		<relativePath/> <!-- lookup parent from repository -->
	</parent>
	<groupId>com.in28minutes.microservices</groupId>
	<artifactId>camel-microservice-b</artifactId>
	<version>0.0.1-SNAPSHOT</version>
	<name>camel-microservice-b</name>
	<description>Demo project for Camel</description>

	<properties>
		<java.version>15</java.version>
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
			<version>${camel.version}</version>
		</dependency>

		<dependency>
			<groupId>org.apache.camel.springboot</groupId>
			<artifactId>camel-activemq-starter</artifactId>
			<version>${camel.version}</version>
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

	<build>
		<plugins>
			<plugin>
				<groupId>org.springframework.boot</groupId>
				<artifactId>spring-boot-maven-plugin</artifactId>
			</plugin>
		</plugins>
	</build>

</project>
```
---

### /camel-microservice-b/src/test/java/com/in28minutes/microservices/camelmicroserviceb/CamelMicroserviceBApplicationTests.java

```java
package com.in28minutes.microservices.camelmicroserviceb;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CamelMicroserviceBApplicationTests {

	@Test
	void contextLoads() {
	}

}
```
---

### /camel-microservice-b/src/main/resources/application.properties

```properties
server.port=8000
spring.activemq.broker-url=tcp://localhost:61616
```
---

### /camel-microservice-b/src/main/java/com/in28minutes/microservices/camelmicroserviceb/CamelMicroserviceBApplication.java

```java
package com.in28minutes.microservices.camelmicroserviceb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CamelMicroserviceBApplication {

	public static void main(String[] args) {
		SpringApplication.run(CamelMicroserviceBApplication.class, args);
	}

}
```
---

### /camel-microservice-b/src/main/java/com/in28minutes/microservices/camelmicroserviceb/routes/ActiveMqReceiverRouter.java

```java
package com.in28minutes.microservices.camelmicroserviceb.routes;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class ActiveMqReceiverRouter extends RouteBuilder{

	@Override
	public void configure() throws Exception {
		
		from("activemq:my-activemq-queue")
		.to("log:received-message-from-active-mq");
		
	}

}
```
---

### /camel-microservice-a/pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>
	<parent>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-parent</artifactId>
		<version>2.4.2</version>
		<relativePath/> <!-- lookup parent from repository -->
	</parent>
	<groupId>com.in28minutes.microservices</groupId>
	<artifactId>camel-microservice-a</artifactId>
	<version>0.0.1-SNAPSHOT</version>
	<name>camel-microservice-a</name>
	<description>Demo project for Camel</description>

	<properties>
		<java.version>15</java.version>
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
			<version>${camel.version}</version>
		</dependency>

		<dependency>
			<groupId>org.apache.camel.springboot</groupId>
			<artifactId>camel-activemq-starter</artifactId>
			<version>${camel.version}</version>
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

	<build>
		<plugins>
			<plugin>
				<groupId>org.springframework.boot</groupId>
				<artifactId>spring-boot-maven-plugin</artifactId>
			</plugin>
		</plugins>
	</build>

</project>
```
---

### /camel-microservice-a/files/output/1002.json

```json
{
  "id": 1002,
  "from": "AUD",
  "to": "INR",
  "conversionMultiple": 10
}
```
---

### /camel-microservice-a/files/output/data.csv

```
"id","from","to","conversionMultiple"
"1001","USD","INR","70"
"1002","EUR","INR","80"
"1003","AUD","INR","10"
```
---

### /camel-microservice-a/files/output/1000.json

```json
{
  "id": 1000,
  "from": "USD",
  "to": "INR",
  "conversionMultiple": 70
}
```
---

### /camel-microservice-a/files/output/1001.json

```json
{
  "id": 1001,
  "from": "EUR",
  "to": "INR",
  "conversionMultiple": 80
}
```
---

### /camel-microservice-a/files/output/single-line.csv

```
"id","from","to","conversionMultiple"
```
---

### /camel-microservice-a/files/output/1002.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<root>
   <id>1002</id>
   <from>AUD</from>
   <to>INR</to>
   <conversionMultiple>10</conversionMultiple>
</root>
```
---

### /camel-microservice-a/files/output/1001.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<root>
   <id>1001</id>
   <from>EUR</from>
   <to>INR</to>
   <conversionMultiple>80</conversionMultiple>
</root>
```
---

### /camel-microservice-a/files/output/1000.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<root>
   <id>1000</id>
   <from>USD</from>
   <to>INR</to>
   <conversionMultiple>70</conversionMultiple>
</root>
```
---

### /camel-microservice-a/src/test/java/com/in28minutes/microservices/camelmicroservicea/CamelMicroserviceAApplicationTests.java

```java
package com.in28minutes.microservices.camelmicroservicea;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CamelMicroserviceAApplicationTests {

	@Test
	void contextLoads() {
	}

}
```
---

### /camel-microservice-a/src/main/resources/application.properties

```properties
spring.activemq.broker-url=tcp://localhost:61616
```
---

### /camel-microservice-a/src/main/java/com/in28minutes/microservices/camelmicroservicea/CamelMicroserviceAApplication.java

```java
package com.in28minutes.microservices.camelmicroservicea;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CamelMicroserviceAApplication {

	public static void main(String[] args) {
		SpringApplication.run(CamelMicroserviceAApplication.class, args);
	}

}
```
---

### /camel-microservice-a/src/main/java/com/in28minutes/microservices/camelmicroservicea/routes/a/MyFirstTimerRouter.java

```java
package com.in28minutes.microservices.camelmicroservicea.routes.a;

import java.time.LocalDateTime;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

//@Component
public class MyFirstTimerRouter extends RouteBuilder{
	
	@Autowired
	private GetCurrentTimeBean getCurrentTimeBean; 
	
	@Autowired
	private SimpleLoggingProcessingComponent loggingComponent;

	@Override
	public void configure() throws Exception {
		// timer
		// transformation
		// log
		// Exchange[ExchangePattern: InOnly, BodyType: null, Body: [Body is null]]
		from("timer:first-timer") //null
		.log("${body}")//null
		.transform().constant("My Constant Message")
		.log("${body}")//My Constant Message
		//.transform().constant("Time now is" + LocalDateTime.now())
		//.bean("getCurrentTimeBean")

		//Processing
		//Transformation
		
		.bean(getCurrentTimeBean)
		.log("${body}")//Time now is2021-01-18T18:32:19.660244
		.bean(loggingComponent)
		.log("${body}")
		.process(new SimpleLoggingProcessor())
		.to("log:first-timer"); //database
		

	}

}

@Component
class GetCurrentTimeBean {
	public String getCurrentTime() {
		return "Time now is" + LocalDateTime.now();
	}
}

@Component
class SimpleLoggingProcessingComponent {
	
	private Logger logger = LoggerFactory.getLogger(SimpleLoggingProcessingComponent.class);

	public void process(String message) {
		
		logger.info("SimpleLoggingProcessingComponent {}", message);
		
	}
}


class SimpleLoggingProcessor implements Processor {
	
	private Logger logger = LoggerFactory.getLogger(SimpleLoggingProcessingComponent.class);

	@Override
	public void process(Exchange exchange) throws Exception {
		logger.info("SimpleLoggingProcessor {}", exchange.getMessage().getBody());
	}

}
```
---

### /camel-microservice-a/src/main/java/com/in28minutes/microservices/camelmicroservicea/routes/c/ActiveMqSenderRouter.java

```java
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
```
---

### /camel-microservice-a/src/main/java/com/in28minutes/microservices/camelmicroservicea/routes/b/MyFileRouter.java

```java
package com.in28minutes.microservices.camelmicroservicea.routes.b;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class MyFileRouter extends RouteBuilder{

	@Override
	public void configure() throws Exception {
		from("file:files/input")
		.log("${body}")
		.to("file:files/output");	
	}

}
```
---
