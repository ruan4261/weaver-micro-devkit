package com.weaver.test.http;

import java.io.IOException;
import java.net.*;

public class SocketDemo {

    public static void main(String[] args) throws IOException, InterruptedException {
        Socket socket = new Socket("www.baidu.com", 80, InetAddress.getLocalHost(), 12345);
        Socket socket1 = new Socket("www.baidu.com", 80, InetAddress.getLocalHost(), 12346);
        Socket socket2 = new Socket("www.baidu.com", 80, InetAddress.getLocalHost(), 12347);
        Socket socket3 = new Socket("www.baidu.com", 80, InetAddress.getLocalHost(), 12348);
        socket.setKeepAlive(false);

        Thread.sleep(10000);

        if (true)
            return;
        new Thread(new Runnable() {
            @Override
            public void run() {
                ServerSocket serverSocket = null;
                try {
                    serverSocket = new ServerSocket();
                    serverSocket.bind(new InetSocketAddress("127.0.0.1", 8888));
                    System.out.println(serverSocket);
                    while (true) {
                        System.out.println("in loop");
                        Socket socket = serverSocket.accept();
                        System.out.println(socket);
                        System.out.println(socket.getRemoteSocketAddress().toString());
                        socket.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (serverSocket != null) {
                        try {
                            serverSocket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }).start();

        System.out.println("server launch");
        Thread.sleep(500);

    }

}
