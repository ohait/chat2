package it.oha.chat;

import it.oha.chat.ipc.Message;
import it.oha.chat.ipc.Packet;
import it.oha.util.Log;

import java.util.regex.Pattern;

public class CmdParser {
    static Pattern reMessage = Pattern.compile("""
                ^\\s* # whitespace
                @(?<recipient>\\w+): # @<who>:
                \\s* # whitespace
                (?<message>\\S+.*?)
                \\s*$ # whitespace
            """, Pattern.COMMENTS);
    static Pattern reExit = Pattern.compile("""
                ^\\s* # whitespace
                (exit)
                \\s*$ # whitespace
            """, Pattern.COMMENTS | Pattern.CASE_INSENSITIVE);
    String lastRecipient;

    public Packet parse(String cmd) {
        var m = reMessage.matcher(cmd);
        if (m.matches()) {
            lastRecipient = m.group("recipient");
            var message = m.group("message");
            return new Message(lastRecipient, message);
        } else if (reExit.matcher(cmd).matches()) {
            return new Disconnect();
        } else if (lastRecipient != "" && !cmd.trim().startsWith("@")) {
            // assume it's a followup on the previous recipient
            return new Message(lastRecipient, cmd.trim());
        } else {
            Log.warning("can't parse: %s", cmd); // using a proper BNF grammar/parser would give a better error
            return null;
        }
    }
}
