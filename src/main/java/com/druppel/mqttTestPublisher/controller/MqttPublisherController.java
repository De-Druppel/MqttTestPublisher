package com.druppel.mqttTestPublisher.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;

import java.util.Date;
import java.util.Properties;

@Controller
public class MqttPublisherController {
    private final boolean DEBUG = true;

    private final ApplicationContext context;
    private MqttTestPublisherConfiguration.MqttMessageProducer mqttMessageProducer;

    @Autowired
    public MqttPublisherController(ApplicationContext context) {
        this.context = context;
        mqttMessageProducer = this.context.getBean(MqttTestPublisherConfiguration.MqttMessageProducer.class);

        if (DEBUG) {
            System.out.println("DEBUG enabled in class: " + this.getClass());
            startMqttTestPublisher();
        }
    }

    public void startMqttTestPublisher() {
        Properties properties = context.getBean("properties", Properties.class);
        String payload = properties.getProperty("MESSAGE_PAYLOAD");
        while(true) {
            try {
                if (DEBUG) {
                    System.out.println("Publishing to MQTT broker: " + payload + " at timestamp: " + new Date());
                }
                mqttMessageProducer.sendToMqtt(properties.getProperty(payload));
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
