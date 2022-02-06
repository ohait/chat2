package it.oha.chat.ipc;

import it.oha.chat.Client;
import it.oha.chat.Connection;
import it.oha.chat.Server;

import java.io.IOException;
import java.util.Date;

public class Message extends TopicPacket {
    public String sender;
    public String message;

    public Message(String topic, String message) {
        super(topic);
        this.message = message;
    }

    @Override
    public void onServer(Server ser, Connection c) {
        this.time = new Date(); // set time to server receive time
        this.sender = c.name;
        ser.topic(this.topic).broadcast(this);
        if (this.topic != "all") {
            ser.topic("all").broadcast(this);
        }
        try {
            ser.getStore().store(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClient(Client cli, Connection c) {
        cli.onMessage(this);
    }

    @Override
    public String toString() {
        return "Message{" +
                "time=" + time +
                ", topic='" + topic + '\'' +
                ", sender='" + sender + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
