package com.game.connection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.EOFException;

public class WebSocketConnection {

    private final Socket socket;
    private final InputStream in;
    private final OutputStream out;
    private final String connectionId;

    public static WebSocketConnection establish(Socket socket) throws IOException {
        try {
            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();
            String generatedId = performHandshake(in, out);
            System.out.println("Handshake successful for client ID: " + generatedId.substring(0, 8));
            return new WebSocketConnection(socket, in, out, generatedId);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-1 algorithm not available, cannot run server.", e);
        }
    }
    
    private WebSocketConnection(Socket socket, InputStream in, OutputStream out, String connectionId) {
        this.socket = socket;
        this.in = in;
        this.out = out;
        this.connectionId = connectionId;
    }

    private static String performHandshake(InputStream in, OutputStream out) throws IOException, NoSuchAlgorithmException {
        try(Scanner s = new Scanner(in, "UTF-8").useDelimiter("\\r\\n\\r\\n")){
            String data = s.hasNext() ? s.next() : "";

            if (data.isEmpty()) throw new IOException("Client did not send a handshake request.");
            
            Matcher get = Pattern.compile("^GET").matcher(data);
            if (!get.find()) throw new IOException("Not a valid WebSocket handshake request.");
            
            Matcher match = Pattern.compile("Sec-WebSocket-Key: (.*)").matcher(data);
            if (!match.find()) throw new IOException("Sec-WebSocket-Key not found.");
            
            String clientKey = match.group(1).trim();
            String magicString = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
            byte[] responseKeyBytes = MessageDigest.getInstance("SHA-1").digest((clientKey + magicString).getBytes("UTF-8"));
            String responseKey = Base64.getEncoder().encodeToString(responseKeyBytes);

            byte[] response = ("HTTP/1.1 101 Switching Protocols\r\n"
                    + "Connection: Upgrade\r\n"
                    + "Upgrade: websocket\r\n"
                    + "Sec-WebSocket-Accept: " + responseKey + "\r\n\r\n").getBytes("UTF-8");

            out.write(response);
            out.flush();
    
            return responseKey;

        }catch(Exception e){
            throw e;
        }

    }

    public byte[] readBinaryMessage() throws IOException {
        int firstByte = in.read();
        if (firstByte == -1) throw new EOFException("Connection closed by client.");

        int opcode = firstByte & 0x0F;
        if (opcode == 0x8) return null;
        if (opcode != 0x2) throw new IOException("Unsupported opcode, expected BINARY (2): " + opcode);

        int secondByte = in.read();
        if ((secondByte & 0x80) == 0) throw new IOException("Received unmasked frame from client.");
        
        long payloadLength = secondByte & 0x7F;
        if (payloadLength == 126) payloadLength = (in.read() << 8) | in.read();
        else if (payloadLength == 127) throw new IOException("Received a message that is too long.");

        byte[] maskingKey = new byte[4];
        in.readNBytes(maskingKey, 0, 4);

        byte[] payload = new byte[(int) payloadLength];
        in.readNBytes(payload, 0, (int) payloadLength);

        for (int i = 0; i < payload.length; i++) {
            payload[i] = (byte) (payload[i] ^ maskingKey[i % 4]);
        }

        return payload;
    }

    public void sendBinaryMessage(byte[] payload) throws IOException {
        int length = payload.length;
        
        out.write(0x82); // FIN bit + BINARY opcode

        if (length <= 125) {
            out.write(length);
        } else if (length <= 65535) {
            out.write(126);
            out.write((length >> 8) & 0xFF);
            out.write(length & 0xFF);
        } else {
            throw new IOException("Cannot send a message that is too long.");
        }

        out.write(payload);
        out.flush();
    }
    
    public String getConnectionId() { return this.connectionId; }
    public void close() throws IOException { if (socket != null && !socket.isClosed()) socket.close(); }
    public boolean isClosed() { return socket.isClosed(); }
}