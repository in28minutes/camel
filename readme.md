# Apache Camel - Simplify Enterprise Integration

## Commands and Snippets

```
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
```

## Diagrams

```
digraph architecture {

rankdir = LR;
node[shape="rect"]
node[style=filled,color="#59C8DE"];

{rank=same; Microservice2, Microservice3}

{rank=same; Microservice1, Microservice6, RabbitMQ3}

RabbitMQ1,RabbitMQ3,RabbitMQ4,RabbitMQ5[shape=underline,style=unfilled,color="#000000",label=<Queue>]

Microservice1 -> RabbitMQ1 -> Microservice2
Microservice1 -> Microservice3
Microservice1 -> RabbitMQ3 -> Microservice6
Microservice3 -> RabbitMQ4 -> Microservice4
Microservice2 -> RabbitMQ5 -> Microservice5

}

digraph architecture {

rankdir = LR;
node[shape="rect"]
node[style=filled,color="#59C8DE"];


Microservice1 -> Microservice2
Microservice1 -> Microservice3
Microservice1 -> Microservice4

}

digraph architecture {

rankdir = LR;
node[shape="rect"]
node[style=filled,color="#59C8DE"];


Microservice1 -> Microservice4
Microservice2 -> Microservice4
Microservice3 -> Microservice4
Microservice4 -> Microservice5

}

digraph architecture {

rankdir = LR;
node[shape="rect"]
node[style=filled,color="#59C8DE"];


Microservice1 -> Microservice4
Microservice2 -> Microservice4
Microservice3 -> Microservice4
Microservice4 -> Microservice5
Microservice4 -> Microservice6
Microservice6 -> Microservice7
Microservice6 -> Microservice8
Microservice8 -> Microservice4

}

digraph architecture {

rankdir = LR;
node[shape=component]
node[style=filled,color="#59C8DE"];


EndPoint1  -> Consumer
EndPoint2  -> Consumer
EndPoint3  -> Consumer

}

digraph architecture {

rankdir = LR;
node[shape=component]
node[style=filled,color="#59C8DE"];


Producer -> EndPoint1
Producer  -> EndPoint2
Producer  -> EndPoint3

}

digraph architecture {

rankdir = LR;
node[style=filled,color="#59C8DE",shape="rect",width=1.5];


Exchange -> ExchangeId
Exchange  -> MEP
Exchange  -> Properties
Exchange  -> Input
Exchange  -> Output
}

digraph G {

rankdir = TB;
    
node[style=filled,color="#59C8DE",shape="rect",width=1];
MicroserviceA -> Queue
Queue2 -> MicroserviceC
Queue, Queue2[shape=underline,style=unfilled,color="#000000",label=<Queue>]
Database[shape=cylinder]
	
	subgraph cluster_1 {
	    label = "MicroserviceB";
		EndPoint1 -> Processor -> EndPoint2
		Processor -> EndPoint3 
	}

    EndPoint2 -> Database
	Queue -> EndPoint1
	EndPoint3 -> Queue2
    {rank=same; Database, Queue2}

}

```