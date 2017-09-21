package com.jim.im.utf.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

/**
 * Created by jim on 2017/6/23.
 * This class is ...
 */
@Configuration
@EnableWebSocketMessageBroker
@PropertySource("classpath:socket.config.properties")
public class WebSocketConfig extends AbstractWebSocketMessageBrokerConfigurer {
	private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketConfig.class);
	@Value("${socket.endPoint}")
	private String endPoint;
	@Value("${socket.brokerRelay}")
	private String brokerRelay;
	@Value("${socket.applicationDestinationPrefixes}")
	private String applicationDestinationPrefixes;
	@Value("${socket.streamBytesLimit}")
	private Integer streamBytesLimit;
	@Value("${socket.httpMessageCacheSize}")
	private Integer httpMessageCacheSize;
	@Value("${socket.sendTimeLimit}")
	private Integer sendTimeLimit;
	@Value("${socket.sendBufferSizeLimit}")
	private Integer sendBufferSizeLimit;
	@Value("${socket.messageSizeLimit}")
	private Integer messageSizeLimit;

	@Override
	public void registerStompEndpoints(StompEndpointRegistry stompEndpointRegistry) {
		stompEndpointRegistry.addEndpoint(endPoint).withSockJS()
				.setStreamBytesLimit(streamBytesLimit)
				.setHttpMessageCacheSize(httpMessageCacheSize);
	}

	@Override
	public void configureMessageBroker(MessageBrokerRegistry config) {
		config.enableStompBrokerRelay(brokerRelay);
		config.setApplicationDestinationPrefixes(applicationDestinationPrefixes);
	}

	@Override
	public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
		registration.setSendTimeLimit(sendTimeLimit)
				.setSendBufferSizeLimit(sendBufferSizeLimit)
				.setMessageSizeLimit(messageSizeLimit);
	}
}
