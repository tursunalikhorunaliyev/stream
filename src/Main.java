import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.*;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        DatagramSocket socket = new DatagramSocket();
        InetAddress group = InetAddress.getByName("230.0.0.1");
        int port = 4446;

        File audioFile = new File("music/Jonasu_-_Rhythm_Inside__ft._Rory_Hope_(256k).wav");
        FileInputStream fileInputStream = new FileInputStream(audioFile);

        byte[] buffer = new byte[512];
        System.out.println("Radio is on the air");


        double bytesPerNano = 176400.0 / 1_000_000_000.0;

        long startTime = System.nanoTime();
        long totalBytesSent = 0;

        while (fileInputStream.read(buffer) != -1){
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, port);
            socket.send(packet);
            totalBytesSent += buffer.length;

            long expectedNanoTime = (long) (totalBytesSent / bytesPerNano);
            long actualNanoTime = System.nanoTime() - startTime;

            long waitTime = expectedNanoTime - actualNanoTime;

            if (waitTime > 0) {
                // We are too fast! Sleep exactly the amount of time needed to stay in sync.
                Thread.sleep(waitTime / 1_000_000, (int) (waitTime % 1_000_000));
            }
        }
        socket.close();
    }
}