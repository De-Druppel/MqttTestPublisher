package com.druppel.mqttTestPublisher.controller;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

import java.io.IOException;
import java.util.Properties;

/**
 * This class is responsible for setting up spring IoC container beans regarding MQTT publisher configuration.
 */
@Configuration
public class MqttTestPublisherConfiguration {

    private final ApplicationContext context;


    @Autowired
    public MqttTestPublisherConfiguration(ApplicationContext context) {
        this.context = context;
    }

    @Bean
    @ServiceActivator(inputChannel = "mqttOutboundChannel")
    public MessageHandler mqttOutbound() {
        String clientId = properties().getProperty("CLIENT_ID") + System.currentTimeMillis();
        MqttPahoClientFactory mqttClientFactory = mqttClientFactory();
        MqttPahoMessageHandler messageHandler =
                new MqttPahoMessageHandler(clientId, mqttClientFactory);
        messageHandler.setAsync(true);
        messageHandler.setDefaultTopic(properties().getProperty("MQTT_TOPIC"));
        return messageHandler;
    }

    @Bean
    public MessageChannel mqttOutboundChannel() {
        DirectChannel directChannel = new DirectChannel();
        directChannel.subscribe(mqttOutbound());
        return directChannel;
    }

    @MessagingGateway(defaultRequestChannel = "mqttOutboundChannel")
    public interface MqttMessageProducer {

        void sendToMqtt(String data);
    }

    /**
     * @return MQTT subscriber details properties from properties file.
     */
    @Bean
    public Properties properties() {
        Properties properties = new Properties();
        try {
            properties.load(MqttTestPublisherConfiguration.class.getClassLoader().getResourceAsStream("mqtt.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }

    /**
     * Sets connection options for the Mqtt Client factory.
     *
     * @return mqtt client factory.
     */
    @Bean
    public MqttPahoClientFactory mqttClientFactory() {
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        MqttConnectOptions options = new MqttConnectOptions();
        // Set mqtt connection options
        options.setServerURIs(new String[]{properties().getProperty("SERVERURL")});
        options.setUserName(properties().getProperty("USERNAME"));
        options.setPassword(properties().getProperty("PASSWORD").toCharArray());
        factory.setConnectionOptions(options);
        return factory;
    }
}
