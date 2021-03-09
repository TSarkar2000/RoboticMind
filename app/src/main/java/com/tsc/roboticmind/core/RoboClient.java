package com.tsc.roboticmind.core;

import android.content.Context;

import org.eclipse.paho.client.mqttv3.MqttClient;

public class RoboClient {

    private final int defaultQoS;
    private final String clientId, brokerUri;
    private final Context context;

    public RoboClient(int defaultQoS, String brokerUri, Context context) {
        this.defaultQoS = defaultQoS;
        this.clientId = MqttClient.generateClientId();
        this.brokerUri = brokerUri;
        this.context = context;
    }

    public int getDefaultQoS() {
        return defaultQoS;
    }

    public String getClientId() {
        return clientId;
    }

    public String getBrokerUri() {
        return brokerUri;
    }

    public Context getContext() {
        return context;
    }
}
