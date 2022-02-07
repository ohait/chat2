package it.oha.chat;

import it.oha.chat.ipc.Disconnect;
import it.oha.chat.ipc.Packet;
import it.oha.util.Log;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class Connection implements AutoCloseable {
    private ObjectInputStream in;
    private ObjectOutputStream out;
    Socket sock;
    public String name; // assigned by server after optional authentication?

    private BlockingQueue<Packet> outbox = new ArrayBlockingQueue<>(10);

    public Connection(Socket sock) throws IOException {
        this.sock = sock;
        this.out = new ObjectOutputStream(sock.getOutputStream());
        Log.debug("Connection created toward " + sock.getRemoteSocketAddress());
    }

    public void close() throws IOException {
        outbox.offer(new Disconnect());
    }

    /**
     * queue packet to be sent, returns immediately
     *
     * @param p
     * @return false if packet can't be queued
     */
    synchronized public boolean emit(Packet p) {
        if (p == null) {
            new IllegalArgumentException("null packet, use close()");
        }
        if (sock.isOutputShutdown()) return false;
        return outbox.offer(p); // false if queue full
    }

    public Packet read() throws IOException {
        try {
            var p = (Packet) in.readObject();
            Log.debug("got packet " + p);
            return p;
        } catch (ClassNotFoundException e) {
            throw new InvalidObjectException(e.getMessage());
        }
    }

    public void recvLoop(Consumer<Packet> c) {
        try {
            in = new ObjectInputStream(sock.getInputStream());
            Log.debug("ObjectInputStream created from " + sock.getRemoteSocketAddress());
            while (!sock.isInputShutdown()) {
                var p = read();
                c.accept(p);
            }
        } catch (EOFException | SocketException e) {
            Log.info("client going away: " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }
        synchronized (this) {
            notifyAll();
        }

        try {
            sock.close();
        } catch (IOException e) {
            // ignore
        }
    }

    /**
     * send the package to the network without queueing
     *
     * @param p
     * @throws IOException
     */
    public void send(Packet p) throws IOException {
        out.writeObject(p);
    }

    public void sendLoop() {
        try {
            while (true) {
                Packet p = outbox.poll(180, TimeUnit.SECONDS);
                out.writeObject(p);
                if (p.getClass() == Disconnect.class) break;
            }
        } catch (EOFException | SocketException e) {
            Log.info("client going away: " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        synchronized (this) {
            notifyAll();
        }

        try {
            sock.close();
        } catch (IOException e) {
            // ignore
        }
    }

    public void startRecvLoop(String name, Consumer<Packet> c) {
        new Thread(() -> recvLoop(c), name).start();
    }

    public void startSendLoop(String name) {
        new Thread(() -> sendLoop(), name).start();
    }
}
