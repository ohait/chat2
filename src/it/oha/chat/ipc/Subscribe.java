package it.oha.chat.ipc;

import it.oha.chat.Client;
import it.oha.chat.Connection;
import it.oha.chat.Server;

public class Subscribe extends TopicPacket {

    public Subscribe(String topic) {
        super(topic);
    }

    @Override
    public void onServer(Server ser, Connection c) {
        ser.topic(topic).subscribe(c::emit);
    }

    @Override
    public String toString() {
        return "Subscribe{" +
                "topic='" + topic + '\'' +
                '}';
    }

    @Override
    public void onClient(Client cli, Connection c) {

    }
}
