package com.ship.proxy;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;

public class ShipProxy {
    private static final int LISTEN_PORT = 8080;
    private static final String OFFSHORE_HOST = "offshore-proxy";
    private static final int OFFSHORE_PORT = 9090;

    private static Socket offshoreSocket;
    private static DataOutputStream offshoreOut;
    private static DataInputStream offshoreIn;
    private static final BlockingQueue<Socket> requestQueue = new LinkedBlockingQueue<>();

    public static void main(String[] args) throws Exception {
        offshoreSocket = new Socket(OFFSHORE_HOST, OFFSHORE_PORT);
        offshoreOut = new DataOutputStream(offshoreSocket.getOutputStream());
        offshoreIn = new DataInputStream(offshoreSocket.getInputStream());

        ExecutorService executor = Executors.newFixedThreadPool(2);

        executor.submit(() -> {
            try (ServerSocket serverSocket = new ServerSocket(LISTEN_PORT)) {
                System.out.println("Ship Proxy listening on port " + LISTEN_PORT);
                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    requestQueue.offer(clientSocket);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        executor.submit(() -> {
            while (true) {
                try {
                    Socket clientSocket = requestQueue.take();
                    handleRequest(clientSocket);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private static void handleRequest(Socket clientSocket) {
        try (clientSocket;
             InputStream in = clientSocket.getInputStream();
             OutputStream out = clientSocket.getOutputStream()) {

            // Read just the HTTP headers from the client
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder requestBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null && !line.isEmpty()) {
                requestBuilder.append(line).append("\r\n");
            }
            requestBuilder.append("\r\n"); // end of headers

            byte[] requestBytes = requestBuilder.toString().getBytes(StandardCharsets.UTF_8);

            // Send to offshore
            offshoreOut.writeInt(requestBytes.length);
            offshoreOut.write(requestBytes);
            offshoreOut.flush();

            // Read response
            int len = offshoreIn.readInt();
            byte[] responseBytes = offshoreIn.readNBytes(len);

            out.write(responseBytes);
            out.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}