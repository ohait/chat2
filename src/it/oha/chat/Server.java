package it.oha.chat;

import it.oha.chat.ipc.Disconnect;
import it.oha.chat.ipc.Packet;
import it.oha.chat.ipc.TopicPacket;
import it.oha.util.Addr;
import it.oha.util.Log;
import sun.misc.Signal;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

public class Server implements AutoCloseable {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: Client <host:port> <storage dir>");
            System.exit(-1);
        }
        try {
            var addr = new Addr(args[0]);
            var store = new NoStore();

            if (args.length == 2) {
                // TODO storage
            }
            var ser = new Server(addr.host, addr.port, store);

            Signal.handle(new Signal("INT"), sig -> {
                try {
                    Log.warning("signal " + sig);
                    ser.close();
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            });
            new Thread(() -> {
                ser.acceptLoop();
            }, "accept").start();

            ser.waitDone(); // wait for close() and all client gone

        } catch (MalformedURLException e) {
            System.err.println("can't parse host: " + args[1]);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private int port;
    private ServerSocket sock;

    public Store getStore() {
        return store;
    }

    private Store store;

    public Server(String host, int port, Store store) throws IOException {
        this.store = store;
        this.port = port;
        sock = new ServerSocket(port, 64, InetAddress.getByName(host));
        Log.info("listening to :" + sock.getLocalPort());
    }

    public int getPort() {
        return sock.getLocalPort();
    }

    /**
     * stop accepting connection
     *
     * @throws IOException
     * @throws InterruptedException
     */
    public void close() throws IOException, InterruptedException {
        sock.close();
        Log.debug("sending Disconnect to all");
        for (Connection cli : clients.values()) {
            Log.debug("sending Disconnect to %s", cli);
            cli.emit(new Disconnect()); // graceful
        }
        Thread.sleep(1000);
        for (Connection cli : clients.values()) {
            cli.emit(null);
        }
    }

    /**
     * wait for all the clients to finish
     *
     * @throws InterruptedException
     */
    synchronized public void waitDone() throws InterruptedException {
        while (!sock.isClosed()) {
            wait();
        }
        while (!clients.isEmpty()) {
            wait();
        }
    }

    /**
     * main accept loop, will further spawn more threads for each connection
     */
    public void acceptLoop() {
        try {
            while (!sock.isClosed()) {
                var remote = sock.accept();
                // TODO(oha): this will require protection from DDOS
                var addr = remote.getRemoteSocketAddress();
                Log.info("connection from %s", addr);
                new Thread(() -> {
                    try {
                        var conn = new Connection(remote);
                        clients.put(addr, conn);
                        conn.startSendLoop(addr.toString() + "-send");

                        conn.recvLoop((p) -> {
                            p.onServer(Server.this, conn);
                        });
                    } catch (Throwable e) {
                        e.printStackTrace();
                    } finally {
                        Log.warning("disconnected %s", addr);
                        clients.remove(addr);
                        synchronized (Server.this) {
                            Server.this.notifyAll();
                        }
                    }
                }, addr.toString() + "-read").start();
            }
        } catch (IOException e) {
            if (sock.isClosed()) return; // ignore when closing
            e.printStackTrace();
        }
    }

    private ConcurrentMap<SocketAddress, Connection> clients = new ConcurrentHashMap<>();

    private ConcurrentMap<String, Topic> topics = new ConcurrentHashMap<>();

    public Topic topic(String id) {
        return topics.computeIfAbsent(id, Topic::new);
    }

    public class Topic {
        public String topic;

        public Topic(String topic) {
            this.topic = topic;
        }

        private ConcurrentMap<UUID, Function<Packet, Boolean>> subscriptions = new ConcurrentHashMap<>();

        public void subscribe(Function<Packet, Boolean> emitter) {
            var id = UUID.randomUUID();
            subscriptions.put(id, emitter);
        }

        public void broadcast(Packet p) {
            Log.debug("broadcasting " + p);
            subscriptions.forEach((id, emitter) -> {
                var ok = emitter.apply(p);
                Log.debug("broadcast to %s: %s", id, ok);
                if (!ok) {
                    subscriptions.remove(id);
                }
            });
        }
    }

    public static class NoStore implements Store {

        @Override
        public void store(TopicPacket p) {
        }

        @Override
        public List<TopicPacket> fetch(String topic) {
            return new ArrayList<TopicPacket>();
        }
    }
}
