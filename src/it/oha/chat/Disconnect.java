package it.oha.chat;

import it.oha.chat.ipc.Packet;

import java.io.IOException;

public class Disconnect extends Packet {

    @Override
    public void onServer(Server ser, Connection c) {
        c.emit(this);
        try {
            c.close();
        } catch (IOException e) {
            //e.printStackTrace();
        }
    }

    @Override
    public void onClient(Client cli, Connection c) {
        try {
            cli.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
