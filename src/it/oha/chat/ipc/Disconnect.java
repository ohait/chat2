package it.oha.chat.ipc;

import it.oha.chat.Client;
import it.oha.chat.Connection;
import it.oha.chat.Server;

import java.io.IOException;

public class Disconnect extends Packet {

    @Override
    public Error onServer(Server ser, Connection c) {
        c.emit(this);
        try {
            c.close();
        } catch (IOException e) {
            //e.printStackTrace();
        }
        return null;
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
