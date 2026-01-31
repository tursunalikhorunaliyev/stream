import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.*;
import java.util.Arrays;
import java.util.Comparator;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        DatagramSocket socket = new DatagramSocket();
        InetAddress group = InetAddress.getByName("230.0.0.1");
        int port = 4446;

        byte[] buffer = new byte[1024];

        // ADJUST THIS: If music is still slightly slow, increase 176400 to 176450.
        // For 44.1kHz 16-bit Stereo, the exact value is 176400.
        double bytesPerNano = 176400.0 / 1_000_000_000.0;

        File musicFolder = new File("music");
        System.out.println("Radio Station Started - High Precision Mode");

        while (true) {
            File[] audioFiles = musicFolder.listFiles((dir, name) -> name.toLowerCase().endsWith(".wav"));

            if (audioFiles == null || audioFiles.length == 0) {
                System.out.println("Waiting for music files...");
                Thread.sleep(5000);
                continue;
            }

            Arrays.sort(audioFiles, Comparator.comparing(File::getName));

            for (File audioFile : audioFiles) {
                System.out.println("Streaming: " + audioFile.getName());

                try (FileInputStream fileInputStream = new FileInputStream(audioFile)) {
                    // Skip WAV header (first 44 bytes) to prevent "pop" sounds
                    fileInputStream.skip(44);

                    long startTime = System.nanoTime();
                    long totalBytesSent = 0;
                    int bytesRead;

                    while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                        DatagramPacket packet = new DatagramPacket(buffer, bytesRead, group, port);
                        socket.send(packet);
                        totalBytesSent += bytesRead;

                        long expectedNanoTime = (long) (totalBytesSent / bytesPerNano);
                        long actualNanoTime = System.nanoTime() - startTime;
                        long waitTime = expectedNanoTime - actualNanoTime;

                        if (waitTime > 2000000) {
                            // Long wait: Sleep to save CPU (2ms threshold)
                            Thread.sleep(waitTime / 1_000_000);
                        } else {
                            // Short wait: Use precise Busy-Wait loop
                            // This prevents the "slowness" caused by lazy thread sleeping
                            while (System.nanoTime() - startTime < expectedNanoTime) {
                                // Spin for precision
                            }
                        }
                    }
                } catch (IOException e) {
                    System.err.println("Error: " + audioFile.getName());
                }
                Thread.sleep(1500); // 1.5s gap between songs
            }
        }
    }
}