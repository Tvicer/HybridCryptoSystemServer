package Server;

import java.io.*;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.Objects;

public class ServerSocketAssistant implements Closeable {
    private final Socket socket;
    private final BufferedReader reader;
    private final BufferedWriter writer;
    private int[] key;
    private Modes mode;
    private byte[] IV;
    public SymmetricCrypto symmetricCrypto;;

    public ServerSocketAssistant(ServerSocket server) {
        try {
            this.socket = server.accept();
            this.reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            this.writer = new BufferedWriter(new OutputStreamWriter(this.socket.getOutputStream()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendMessage(String data) {
        try {
            writer.write(data);
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendMessage(byte[] data) {
        sendMessage(Arrays.toString(data));
    }

    public void sendMessage(BigInteger[] data) {
        sendMessage(Arrays.toString(data));
    }

    public byte[] getByteMessage() {
        try {
            String str = reader.readLine();
            String[] arrStr = str.substring(1, str.length() - 1).split(", ");
            byte[] res = new byte[arrStr.length];
            for (int i = 0; i < arrStr.length; i++)
                res[i] = Byte.parseByte(arrStr[i]);
            return res;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public String getStringMessage() {
        try {
            return reader.readLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }



    @Override
    public void close() throws IOException {
        reader.close();
        writer.close();
        socket.close();
    }

    public void setKey(int[] key) {
        this.key = key;
    }
    public void setMode(String mode) {
        switch (mode) {
            case "CBC" -> this.mode = Modes.CBC;
            case "CFB" -> this.mode = Modes.CFB;
            case "CTR" -> this.mode = Modes.CTR;
            case "ECB" -> this.mode = Modes.ECB;
            case "OFB" -> this.mode = Modes.OFB;
            case "RD" -> this.mode = Modes.RD;
            case "RDH" -> this.mode = Modes.RDH;
            default -> this.mode = Modes.CBC;

        }
    }
    public void setIV(byte[] IV) {
        this.IV =IV;
    }

    public int[] getKey() {
        return this.key;
    }
    public Modes getMode() {
        return this.mode;
    }

    public byte[] getIV() {
        return this.IV;
    }



}
