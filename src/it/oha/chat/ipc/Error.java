package it.oha.chat.ipc;

import it.oha.chat.Client;
import it.oha.chat.Connection;
import it.oha.chat.Server;

public class Error extends Packet {
    public String message;

    @Override
    public void onServer(Server ser, Connection c) {
        // ignore
    }

    @Override
    public void onClient(Client cli, Connection c) {
        cli.onError(this);
    }
}
