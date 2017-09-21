---
layout: post
title: "java/MQ/install rabbitmq on debian and how to using it"
date: 2017-06-23 10:07:03 +0800
comments: true
categories: 
---

# install rabbitmq-server

1. add sources.list

```shell
echo 'deb http://www.rabbitmq.com/debian/ testing main' | sudo tee /etc/apt/sources.list.d/rabbitmq.list
wget -O- https://www.rabbitmq.com/rabbitmq-release-signing-key.asc | sudo apt-key add -
```

2. update & upgrade System

```shell
apt-get update
apt-get upgrade
```

3. install rabbitmq-server

```shell
apt-get install rabbitmq-server
```

# add a new administrator & delete guest

1. list default user list

```sh
rabbitmqctl list_users
```

2.list default vhosts

```sh
rabbitmqctl list_vhosts
```

3. add a new user: liujun & set password & add to administrator group

```shell
rabbitmqctl add_user liujun **********
rabbitmqctl set_permissions -p / liujun ".*" ".*" ".*"
rabbitmqctl set_user_tags liujun administrator
rabbitmqctl list_users
```


# enable plugin: amqp_client rabbitmq_jms_topic_exchange rabbitmq_management rabbitmq_stomp rabbitmq_web_mqtt rabbitmq_web_stomp sockjs

```shell
cd /usr/lib/rabbitmq/lib/rabbitmq_server-3.6.10
sbin/rabbitmq-plugins enable --offline --online amqp_client rabbitmq_jms_topic_exchange rabbitmq_management rabbitmq_stomp rabbitmq_web_mqtt rabbitmq_web_stomp sockjs
/etc/init.d/rabbitmq-server restart
netstat -ant | grep 672

tcp        0      0 0.0.0.0:15672           0.0.0.0:*               LISTEN
tcp        0      0 0.0.0.0:25672           0.0.0.0:*               LISTEN
tcp6       0      0 :::5672                 :::*                    LISTEN
```

# add a empty rabbitmq.config

```shell
echo []. > /etc/rabbitmq/rabbitmq.config
```

# restart rabbitmq-server

```shell
/etc/init.d/rabbitmq-server restart
```
# config rabbitmq in spring

1. create a websocket message broker

```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig extends AbstractWebSocketMessageBrokerConfigurer {

	@Override
	public void configureMessageBroker(MessageBrokerRegistry config) {
		config.enableStompBrokerRelay("/topic/");
		config.setApplicationDestinationPrefixes("/app");
	}

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry.addEndpoint("/quakesep").withSockJS();//endpoint
	}
}
```

2. create a controller & template

```java
@Controller
public class DashboardController {
	@RequestMapping(value = {"", "/"}, method = RequestMethod.GET)
	public String index(){
		return "dashboard/index";
	}
}
```

```html
<!DOCTYPE html>
<html lang="en" xmlns:th="http://www.thymeleaf.org">
<head>
	<meta charset="UTF-8"/>
	<title></title>

	<link rel="stylesheet" th:href="@{/css/bootstrap.min.css}" href="../../static/css/bootstrap.min.css"/>
	<link rel="stylesheet" th:href="@{/css/bootstrap-theme.css}" href="../../static/css/bootstrap-theme.css"/>
	<link rel="stylesheet" th:href="@{/css/application.css}" href="../../static/css/application.css"/>
</head>
<body>

<script th:src="@{/js/jquery-1.7.2.js}" src="../../js/jquery-1.7.2.js"></script>
<script th:src="@{/js/moment.min.js}" src="../../js/moment.min.js"></script>

</body>
</html>
```

3. create gateway xml & rabbit config

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:int="http://www.springframework.org/schema/integration"
       xmlns:int-http="http://www.springframework.org/schema/integration/http"
       xmlns:int-amqp="http://www.springframework.org/schema/integration/amqp"
       xsi:schemaLocation="http://www.springframework.org/schema/integration http://www.springframework.org/schema/integration/spring-integration.xsd
		http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
		http://www.springframework.org/schema/integration/amqp http://www.springframework.org/schema/integration/amqp/spring-integration-amqp.xsd
		http://www.springframework.org/schema/integration/http http://www.springframework.org/schema/integration/http/spring-integration-http.xsd">

    <import resource="rabbit-context.xml"/>
    <!--<int:inbound-channel-adapter channel="quakeinfotrigger" expression="''">-->
        <!--<int:poller fixed-delay="1000"/>-->
    <!--</int:inbound-channel-adapter>-->

    <!--<int:channel id="quakeinfo"/>-->

    <!--<int:channel id="quakeinfotrigger"/>-->

    <!--<int-http:outbound-gateway id="quakerHttpGateway"-->
                               <!--request-channel="quakeinfotrigger"-->
                               <!--url="http://localhost:8180/stomp/quake/json"-->
                               <!--http-method="GET"-->
                               <!--expected-response-type="java.lang.String"-->
                               <!--charset="UTF-8"-->
                               <!--reply-channel="quakeinfo">-->
    <!--</int-http:outbound-gateway>-->
    <!--<int-amqp:outbound-channel-adapter amqp-template="amqpTemplate" channel="quakeinfo"/>-->
</beans>
```

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"

	   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:rabbit="http://www.springframework.org/schema/rabbit"
	   xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd http://www.springframework.org/schema/rabbit http://www.springframework.org/schema/rabbit/spring-rabbit.xsd">

    <rabbit:connection-factory id="connectionFactory"
                               username="liujun" host="localhost" password="*********" port="5672"/>
    <rabbit:template id="amqpTemplate"
                     connection-factory="connectionFactory"
                     exchange="amq.topic"
                     routing-key="quakes.all"
                     channel-transacted="true"/>
    <rabbit:admin connection-factory="connectionFactory"/>

    <rabbit:topic-exchange name="amq.topic"/>

</beans>
```


4. config application

```java
@ComponentScan
@EnableAutoConfiguration
@ImportResource("classpath:httpgateway.xml")
public class Main {
	public static void main(String[] args) {
		SpringApplication.run(Main.class, args);
	}
}
```


# send message to rabbitmq using topic

because I using node js to send message into broker & i using amqplib, so:

```shell
npm i amqplib --save
```

```javascript
var amqp = require('./node_modules/amqplib/callback_api');

amqp.connect('amqp://liujun:*************@localhost', function(err, conn) {
	conn.createChannel(function(err, ch) {
		if (err){
			setTimeout(function() { conn.close(); process.exit(0) }, 500);
		}

		setInterval(function(){
			pub(ch);
		}, 1000);
	});

	function pub(ch){
		var ex = 'amq.topic';
		var key = 'quakes.all';
		var msg = new Date().getTime().toString();

		ch.assertExchange(ex, 'topic', {durable: true});
		ch.publish(ex, key, new Buffer(msg));

		report(key, msg);
	}

	function report(key, msg){
		console.log(" [x] Sent %s:'%s'", key, msg);
	}


});
```

# subscribe topic with socketjs

inject stomp.js & sockjs into html template:

```html
<script th:src="@{/js/sockjs-0.3.4.js}" src="../../js/sockjs-0.3.4.js"></script>
<script th:src="@{/js/stomp.js}" src="../../js/stomp.js"></script>

```

```javascript
	/*<![CDATA[*/
	$(document).ready(function() {
		connect();
	});
	var stompClient = null;


	function connect() {
		var socket = new SockJS('/quakesep');//连接endpoint
		stompClient = Stomp.over(socket);
		stompClient.connect({}, function(frame) {
			console.log('Connected: ' + frame);
			stompClient.subscribe('/topic/quakes.all', function(message){
				showQuakeInfo(message.body);
			});
		});
	}


	function showQuakeInfo(quakeCollectionJson) {
		var quakeCollection = $.parseJSON(quakeCollectionJson);
		var name = quakeCollection.name
		console.log(name);
	}
	/*]]>*/
```