package com.offshore.proxy;


import java.io.*;
import java.net.*;
import java.net.http.*;
import java.nio.charset.StandardCharsets;

public class OffshoreProxy {
    private static final int PORT = 9090;

    public static void main(String[] args) throws Exception {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Offshore Proxy listening on port " + PORT);
            Socket shipSocket = serverSocket.accept();

            DataInputStream in = new DataInputStream(shipSocket.getInputStream());
            DataOutputStream out = new DataOutputStream(shipSocket.getOutputStream());

            while (true) {
                int len = in.readInt();
                byte[] requestBytes = in.readNBytes(len);

                String requestText = new String(requestBytes, StandardCharsets.UTF_8);
                String[] lines = requestText.split("\\r\\n");
                String url = lines[0].split(" ")[1];
                if (!url.startsWith("http")) url = "http://" + url;

                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .GET()
                        .build();

                HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());

                String headers = "HTTP/1.1 " + response.statusCode() + " OK\r\n"
                        + "Content-Type: " + response.headers().firstValue("Content-Type").orElse("text/html") + "\r\n"
                        + "Content-Length: " + response.body().length + "\r\n"
                        + "Connection: close\r\n"
                        + "\r\n";

                byte[] headerBytes = headers.getBytes(StandardCharsets.UTF_8);
                byte[] body = response.body();

                out.writeInt(headerBytes.length + body.length);
                out.write(headerBytes);
                out.write(body);

                out.flush();
            }
        }
    }
}
