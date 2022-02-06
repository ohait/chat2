package it.oha.chat;

import it.oha.chat.ipc.TopicPacket;
import it.oha.util.Log;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;

public class FileStore implements Store {
    private File path;

    public FileStore(File path) {
        this.path = path;
    }

    public static String encode(String in) {
        try {
            return URLEncoder.encode(in, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException("invalid topic: " + in);
        }
    }

    public File file(String topic) {
        return new File(path, encode(topic) + ".p");
    }

    @Override
    public void store(TopicPacket p) throws IOException {
        var file = file(p.topic);
        Log.notice("storing %s to %s", p, file);
        if (!file.exists()) file.createNewFile();
        var out = new ObjectOutputStream(new FileOutputStream(file, true));
        out.writeObject(p);
        out.close();
        Log.notice("stored %s to %s", p, file);
    }

    @Override
    public List<TopicPacket> fetch(String topic) throws IOException {
        var list = new LinkedList<TopicPacket>();
        var file = file(topic);
        try (
                var in = new ObjectInputStream(new FileInputStream(file))
        ) {
            while (true) {
                var obj = in.readObject();
                if (obj instanceof TopicPacket p) {
                    list.offer(p);
                } else {
                    Log.error("skipping invalid object type (%s) in file %s", obj.getClass(), file);
                }
            }
        } catch (EOFException e) {

        } catch (ClassNotFoundException e) {
            // we ignore the error, since there is no way to recover now
            e.printStackTrace();
        }
        return list;
    }
}
