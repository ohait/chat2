package it.oha.chat.ipc;

import java.util.Date;

public abstract class TopicPacket extends Packet {
    public String topic;
    public Date time;


    public TopicPacket(String topic) {
        this.topic = topic;
    }
}
