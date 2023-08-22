package Server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Objects;
import java.util.Random;


public class Server {
    private static Random randomizer = new Random(LocalDateTime.now().getNano());

    public static void main(String[] args) throws IOException {
        try (ServerSocket server = new ServerSocket(1234)) {
            System.out.println("Сервер включен");

            while (true) {
                ServerSocketAssistant socketAssistant = new ServerSocketAssistant(server);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            byte[] byteData;
                            String strData;

                            XTR xtr = new XTR(Entities.TestMode.MILLER_RABIN, 128);

                            socketAssistant.sendMessage(xtr.getPublicKey());

                            byteData = socketAssistant.getByteMessage();
                            socketAssistant.setKey(byteArrayToInt(xtr.decryptKey(byteData)));

                            System.out.println(Arrays.toString(socketAssistant.getKey()));

                            strData = socketAssistant.getStringMessage();
                            socketAssistant.setMode(strData);

                            byteData = socketAssistant.getByteMessage();

                            socketAssistant.setIV(byteData);

                            socketAssistant.symmetricCrypto = new SymmetricCrypto(socketAssistant.getMode(), new Serpent(socketAssistant.getKey()), socketAssistant.getIV());

                            while (true) {

                                strData = socketAssistant.getStringMessage();

                                String path = socketAssistant.getStringMessage();

                                if (Objects.equals(strData, "Send")) {

                                    byteData = socketAssistant.getByteMessage();

                                    File file = new File(path);
                                    FileOutputStream fileOutputStream = new FileOutputStream(file);
                                    fileOutputStream.write(socketAssistant.symmetricCrypto.decryptData(byteData));
                                    fileOutputStream.close();

                                    socketAssistant.sendMessage(socketAssistant.symmetricCrypto.encryptFile(new FileInputStream(file), file.length()));


                                } else if (Objects.equals(strData, "Download")) {

                                    File file = new File(path);

                                    socketAssistant.sendMessage(file.getName());

                                    socketAssistant.sendMessage(socketAssistant.symmetricCrypto.encryptFile(new FileInputStream(file), file.length()));
                                }
                            }

                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }).start();
            }
        }

    }

    public static int[] byteArrayToInt(byte[] array) {
        int[] res = new int[array.length / 4];
        for (int i = 0; i < array.length / 4; i++)
            for (int j = 0; j < 4; j++)
                res[i] = (res[i] << 8) + (array[i * 4 + j] & 0xFF);
        return res;
    }
}
