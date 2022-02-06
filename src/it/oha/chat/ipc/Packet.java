package it.oha.chat.ipc;

import it.oha.chat.Client;
import it.oha.chat.Connection;
import it.oha.chat.Server;

import java.io.Serializable;

public abstract class Packet implements Serializable {

    public abstract void onServer(Server ser, Connection c);

    public abstract void onClient(Client cli, Connection c);
}