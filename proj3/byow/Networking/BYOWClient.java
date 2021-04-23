package byow.Networking;

import edu.princeton.cs.introcs.StdDraw;

import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.nio.file.Paths;
import java.util.Scanner;

/**
 * Created by Boren Tsai and Arjun Sahai.
 */

public class BYOWClient {
    private int width;
    private int height;

    private Socket clientStringSocket;
    private Socket clientReadSocket;
    private BufferedWriter out;
    private BufferedReader in;
    private DataInputStream dis;

    static public final String CANVAS_FILE = ".client_canvas";

    static final String canvasPath = join(new File(System.getProperty("user.dir")), CANVAS_FILE).getAbsolutePath();
    static int magic = Integer.MIN_VALUE;

    /*
    Open socket, input/output streams, and create buffered writer & data input streams.
     */
    public void startConnection(String ip, int port) throws IOException {
        clientReadSocket = new Socket(ip, port);
        clientStringSocket = new Socket(ip, port);
        dis = new DataInputStream(clientReadSocket.getInputStream());
        out = new BufferedWriter(new OutputStreamWriter(clientStringSocket.getOutputStream()));
        in = new BufferedReader(new InputStreamReader(clientStringSocket.getInputStream()));
    }

    /*
    Check that the server has acknowledged that it is quitting in a nonblocking fasion
     */
    private boolean shouldClose() throws IOException {
        if (in.ready()) {
            char[] buf = new char[4];
            in.read(buf, 0, 4); // block client
            System.out.println(String.copyValueOf(buf));
            return true;
        }
        return false;
    }

    /*
    Close reader and writer then close socket.
     */
    private void stopConnection() throws IOException {
        dis.close();
        out.close();
        clientReadSocket.close();
        clientStringSocket.close();
    }

    /*
    Non Blocking
    Sends a String Command to the server i.e. W, A, S, D, etc.
     */
    private void sendCommand(String msg) throws IOException {
        out.write(msg);
        out.flush();
    }

    /*
    Blocking
    Displays a canvas if the server has sent the client something to read
    i.e. The server has sent a png file to the client
     */
    private void showCanvas() throws IOException {
        if (dis.available() > 0) {
            boolean configure = dis.readBoolean();
            if (configure) {
                System.out.println("CONFIGURING CANVAS");
                width = dis.readInt();
                height = dis.readInt();
                StdDraw.setCanvasSize(width, height);
                StdDraw.setXscale(0, width);
                StdDraw.setYscale(0, height);
                StdDraw.clear(new Color(0, 0, 0));
                StdDraw.enableDoubleBuffering();
                StdDraw.show();
            }

            StdDraw.clear();

            File newFile = new File(canvasPath + magic + ".png");
            File oldFile = new File(canvasPath + (magic - 1) + ".png");

            FileOutputStream fos = new FileOutputStream(newFile);

            // wait for incoming file size
            long size = dis.readLong();

            int bytes;
            byte[] buffer = new byte[4 * 1024];
            while (size > 0 && (bytes = dis.read(buffer, 0, (int) Math.min(buffer.length, size))) != -1) {
                fos.write(buffer, 0, bytes);
                size -= bytes;
            }
            // Write all buffered bytes into the file
            fos.flush();

            StdDraw.picture(width / 2, height / 2, canvasPath + magic + ".png");
            magic = magic + 1;

            oldFile.delete();
            StdDraw.show();
        }
    }

    private static File join(File first, String... others) {
        return Paths.get(first.getPath(), others).toFile();
    }


    public static void main(String[] args) throws IOException {
        System.out.println("BYOW Client. Please Enter the following information to connect to a server...");
        Scanner scanner = new Scanner(System.in);
        System.out.print("IP Address: ");
        String ip = scanner.next();
        System.out.print("Port (this must be a number): ");
        int port = scanner.nextInt();

        BYOWClient client = new BYOWClient();
        client.startConnection(ip, port); //ip port changes depending on link supplied by ngrok
        client.showCanvas();
        char command;

        try {
            while (true) {
                if (StdDraw.hasNextKeyTyped()) {
                    command = StdDraw.nextKeyTyped();
                    client.sendCommand(Character.toString(command));
                }
                client.showCanvas();
                StdDraw.clear();
                if (client.shouldClose()) {
                    client.stopConnection();
                    System.exit(0);
                }
            }
        } catch (java.net.SocketException e) {
            client.stopConnection();
            System.out.println("Disconnected from server");
            System.exit(0);
        }
        client.stopConnection();
        System.out.println("Disconnected from server");
        System.exit(0);
    }
}
