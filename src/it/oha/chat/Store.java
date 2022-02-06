package it.oha.chat;

import it.oha.chat.ipc.TopicPacket;

import java.io.IOException;
import java.util.List;

public interface Store {
    public void store(TopicPacket p) throws IOException;

    public List<TopicPacket> fetch(String topic) throws IOException;
}
