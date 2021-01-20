Caused by: java.lang.IllegalArgumentException: 
Data format 'json-jackson' could not be created. 
Ensure that the data format is valid and the associated Camel component is present on the classpath

2021-01-19 17:03:09.765  INFO 18780 --- [activemq-queue]] received-message-from-active-mq          : Exchange[ExchangePattern: InOnly, BodyType: com.in28minutes.microservices.camelmicroserviceb.CurrencyExchange, Body: CurrencyExchange [id=1000, from=USD, to=INR, conversionMultiple=70]]


java.net.UnknownHostException: 3f2b68cf485c

{CamelFileAbsolute=false, CamelFileAbsolutePath=/in28Minutes/git/camel/02.projects/camel-microservice-a/files/input/1000.json, CamelFileLastModified=1610976220529, CamelFileLength=76, CamelFileName=1000.json, CamelFileNameConsumed=1000.json, CamelFileNameOnly=1000.json, CamelFileParent=files/input, CamelFilePath=files/input/1000.json, CamelFileRelativePath=1000.json}

{CamelFileAbsolute=false, CamelFileAbsolutePath=/in28Minutes/git/camel/02.projects/camel-microservice-a/files/input/1000.json, CamelFileLastModified=1610976220529, CamelFileLength=76, CamelFileName=1000.json, CamelFileNameConsumed=1000.json, CamelFileNameOnly=1000.json, CamelFileParent=files/input, CamelFilePath=files/input/1000.json, CamelFileRelativePath=1000.json} 

{CamelBatchSize=1, CamelBatchComplete=true, CamelBatchIndex=0, CamelFilterMatched=false, CamelFileExchangeFile=GenericFile[1000.json]}


2021-01-19 20:21:44.883  INFO 21647 --- [/aggregate-json] aggregate-json                           : Exchange[ExchangePattern: InOnly, BodyType: java.util.ArrayList, Body: [CurrencyExchange [id=1002, from=AUD, to=INR, conversionMultiple=10], CurrencyExchange [id=1000, from=USD, to=INR, conversionMultiple=70], CurrencyExchange [id=1001, from=EUR, to=INR, conversionMultiple=80]]]



2021-01-19 20:23:25.202  INFO 21647 --- [/aggregate-json] aggregate-json                           : Exchange[ExchangePattern: InOnly, BodyType: java.util.ArrayList, Body: [CurrencyExchange [id=1002, from=AUD, to=INR, conversionMultiple=10], CurrencyExchange [id=1000, from=USD, to=INR, conversionMultiple=70], CurrencyExchange [id=1001, from=EUR, to=INR, conversionMultiple=80]]]

2021-01-19 20:23:25.205  INFO 21647 --- [/aggregate-json] aggregate-json                           : Exchange[ExchangePattern: InOnly, BodyType: java.util.ArrayList, Body: [CurrencyExchange [id=1000, from=USD, to=USD, conversionMultiple=70], CurrencyExchange [id=1002, from=AUD, to=USD, conversionMultiple=10], CurrencyExchange [id=1001, from=EUR, to=USD, conversionMultiple=80]]]


2021-01-19 20:50:05.538  INFO 22114 --- [r://routingSlip] c.i.m.c.r.patterns.DynamicRouterBean     : {CamelTimerPeriod=10000, CamelTimerCounter=8, CamelTimerFiredTime=Tue Jan 19 20:50:05 IST 2021, CamelSlipEndpoint=direct://endpoint1, CamelTimerName=routingSlip, CamelToEndpoint=log://directendpoint1} {firedTime=Tue Jan 19 20:50:05 IST 2021} My Message is Hardcoded
2021-01-19 20:50:05.538  INFO 22114 --- [r://routingSlip] directendpoint2                          : Exchange[ExchangePattern: InOnly, BodyType: String, Body: My Message is Hardcoded]
2021-01-19 20:50:05.539  INFO 22114 --- [r://routingSlip] directendpoint3                          : Exchange[ExchangePattern: InOnly, BodyType: String, Body: My Message is Hardcoded]


keytool -genseckey -alias myDesKey -keypass someKeyPassword -keystore myDesKey.jceks -storepass someKeystorePassword -v -storetype JCEKS -keyalg DES

private CryptoDataFormat createEncryptor() throws KeyStoreException, IOException, NoSuchAlgorithmException,
		CertificateException, UnrecoverableKeyException {
	KeyStore keyStore = KeyStore.getInstance("JCEKS");
	ClassLoader classLoader = getClass().getClassLoader();
	keyStore.load(classLoader.getResourceAsStream("myDesKey.jceks"), "someKeystorePassword".toCharArray());
	Key sharedKey = keyStore.getKey("myDesKey", "someKeyPassword".toCharArray());

	CryptoDataFormat sharedKeyCrypto = new CryptoDataFormat("DES", sharedKey);
	return sharedKeyCrypto;
}