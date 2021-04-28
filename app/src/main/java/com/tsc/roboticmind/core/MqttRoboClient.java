package com.tsc.roboticmind.core;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MqttRoboClient {

    private int defQoS = 0;
    private final MqttAndroidClient androidClient;
    private EventListener listener;

    private MqttRoboClient(RoboClient client) {
        defQoS = client.getDefaultQoS();
        androidClient = new MqttAndroidClient(client.getContext(), client.getBrokerUri(), client.getClientId());
    }

    private static MqttRoboClient mqttRoboClient;

    public static MqttRoboClient getInstance(RoboClient client) {
        if (mqttRoboClient == null)
            mqttRoboClient = new MqttRoboClient(client);
        return mqttRoboClient;
    }

    public void setEventListener(EventListener eventListener) {
        listener = eventListener;
        androidClient.setCallback(listener);
    }

    public void connect() {
        try {
            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setMaxReconnectDelay(60000);
            if (listener != null)
                listener.onMessageEvent("Waiting for response...");
            androidClient.connect(options);
        } catch (MqttException e) {
            if (listener != null)
                listener.onMessageEvent(e.getMessage());
        }
    }

    public String getClientId() {
        return androidClient.getClientId();
    }

    public void subcscribe(String topic) {
        try {
            androidClient.subscribe(topic, defQoS, null, subscribeAction);
        } catch (MqttException e) {
            if (listener != null)
                listener.onMessageEvent(e.getMessage());
        }
    }

    private final IMqttActionListener subscribeAction = new IMqttActionListener() {
        @Override
        public void onSuccess(IMqttToken asyncActionToken) {
            String[] topics = asyncActionToken.getTopics();
            if (listener != null)
                listener.onMessageEvent("Subscribed to " + topics[topics.length - 1]);
        }

        @Override
        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
            if (listener != null)
                listener.onMessageEvent("Subscription failed!");
        }
    };

    private String lastTopic;

    public void publish(String topic, String payload) {
        MqttMessage message = new MqttMessage(payload.getBytes());
        message.setQos(defQoS);
        try {
            lastTopic = topic;
            androidClient.publish(topic, message, null, publishAction);
        } catch (MqttException e) {
            if (listener != null)
                listener.onMessageEvent(e.getMessage());
        }
    }

    private final IMqttActionListener publishAction = new IMqttActionListener() {
        @Override
        public void onSuccess(IMqttToken asyncActionToken) {
            if (listener != null)
                listener.onMessageEvent("Published:\"" + lastTopic + "\"");
        }

        @Override
        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
            if (listener != null)
                listener.onMessageEvent("Failed to publish");
        }
    };

    public void disconnect() {
        try {
            androidClient.disconnect();
            androidClient.close();
        } catch (MqttException e) {
            if (listener != null)
                listener.onMessageEvent(e.getMessage());
        }
    }

    public interface EventListener extends MqttCallbackExtended {
        void onMessageEvent(String message);
    }
}