package com.tsc.roboticmind.core;

import com.tsc.roboticmind.utils.Commands;
import com.tsc.roboticmind.utils.Prefs;

public class Task {

    private final MqttRoboClient mqttRoboClient;

    public Task(RoboClient client) {
        mqttRoboClient = MqttRoboClient.getInstance(client);
    }

    public void setMqttEventListener(MqttRoboClient.EventListener eventListener) {
        mqttRoboClient.setEventListener(eventListener);
    }

    public void begin() {
        mqttRoboClient.connect();
    }

    public void subscribe(String topic) {
        mqttRoboClient.subcscribe(topic);
    }

    public void sendCommand(String command, int value) {
        if(value == -1)
            mqttRoboClient.publish(Prefs.FROM_ANDROID, command);
        else if(command.equals(Commands.MOVE_LEFT))
            mqttRoboClient.publish(Prefs.FROM_ANDROID, "L"+value);
        else if(command.equals(Commands.MOVE_RIGHT))
            mqttRoboClient.publish(Prefs.FROM_ANDROID, "R"+value);
    }

    public String reqClientId() {
        return mqttRoboClient.getClientId();
    }

    public void stop() {
       mqttRoboClient.disconnect();
    }
}
