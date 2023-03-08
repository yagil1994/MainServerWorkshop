package com.workshop.mainserverworkshop.utils;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UdpSender {
    public static void main(String[] args) throws Exception {
        String host = "239.255.255.250";
        int port = 1982;
        String path = "/index.html";

        // Build the HTTP request
        String httpRequest = "GET " + path + " HTTP/1.1\r\n" +
                "Host: " + host + "\r\n" +
                "Connection: close\r\n\r\n";

        String message = "Hello, world!";
        InetAddress address = InetAddress.getByName(host);

        DatagramSocket socket = new DatagramSocket();
        byte[] buffer = message.getBytes("UTF-8");
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, port);
        socket.send(packet);
        socket.close();
    }
}
