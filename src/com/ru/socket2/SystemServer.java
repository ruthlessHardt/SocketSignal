package com.ru.socket2;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SystemServer {
    //线程池
    private static ExecutorService executorService = Executors.newFixedThreadPool(10);

    private final static String charset = "UTF8";
    private static volatile Map<String, Socket> clientIpSocketMap = new HashMap<>();

    public static void main(String[] args) {
        //服务端
        ServerSocket serverSocket;
        try {
            serverSocket = new ServerSocket(10005);
            System.out.println("服务端已启动，您本地的字符格式为" + Charset.defaultCharset().name() + "，等待客户端访问");
            if (clientIpSocketMap.values().size() == 0) {
                //开启一个广播写线程，用于服务端向所有客户端发送消息
                executorService.submit(new SystemServer.BroadCastWriteThread());
            }
            //等待客户端的访问
            for (; ; ) {
                //客户端请求
                Socket clientSocket;
                try {
                    clientSocket = serverSocket.accept();
                    executorService.submit(new SystemServer.Server(clientSocket));
                } catch (IOException e) {
                    System.out.println("报错了2" + e);
                }
            }
        } catch (IOException e) {
            System.out.println("报错了1" + e);
        }
    }

    /**
     * 获取客户端信息
     *
     * @param clientSocket
     * @return
     * @throws SocketException
     */
    public static String getClientInfo(Socket clientSocket) {
        if (Objects.isNull(clientSocket)) {
            return "";
        }
        return clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort();
    }

    //服务器处理客户端请求的线程
    public static class Server implements Runnable {
        private Socket clientSocket;

        public Server(Socket clientSocket) {
            this.clientSocket = clientSocket;
            String clientInfo = getClientInfo(clientSocket);
            clientIpSocketMap.put(clientInfo, clientSocket);
        }

        @Override
        public void run() {
            //开启一个读线程
            executorService.submit(new SystemServer.ReadThread(clientSocket));
        }
    }

    //接收信息的线程
    public static class ReadThread implements Runnable {
        private OutputStream outputStream;
        private PrintWriter printWriter;
        private Socket clientSocket;

        private InputStream inputStream;

        private BufferedReader bufferedReader;

        public ReadThread(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            //如果客户端没有关闭写，则持续读取
            while (true) {
                try {
                    inputStream = clientSocket.getInputStream();
                    bufferedReader = new BufferedReader(new InputStreamReader(inputStream, charset));

                    String str;
                    for (; (str = bufferedReader.readLine()) != null; ) {
                        str = "客户端【"+getClientInfo(clientSocket) + "】 : " + str;
                        System.out.println(str);
                        //向其它端转发
                        final String finalStr = str;
                        //System.out.println("服务端：" + str);
                        clientIpSocketMap.values().stream()
                                .filter(Objects::nonNull)
                                .filter(item-> !finalStr.contains(getClientInfo(item)))
                                .forEach(clientSocket -> {
                                    try {
                                        printWriter = doWriter(clientSocket, outputStream, printWriter);
                                        printWriter.println(finalStr);
                                    } catch (IOException e) {
                                        System.out.println("报错6 发送出现异常 " + e.getStackTrace());
                                        removeSocket(clientSocket);
                                    }
                                });
                    }

                } catch (Exception e) {
                    System.out.println("报错了3，可能是客户端由于某种原因断开了连接" + e + ";" + Thread.currentThread().getName());
                    removeSocket(clientSocket);
                    break;//结束本次对客户端的循环
                } finally {
                    try {
                        if (inputStream != null) {
                            inputStream.close();
                        }
                        if (bufferedReader != null) {
                            bufferedReader.close();
                        }
                        if (clientSocket != null) {
                            clientSocket.close();
                        }
                    } catch (Exception e) {
                        System.out.println("报错了" + 5);
                    }
                }
            }
        }
    }

    //发送信息的线程
    public static class BroadCastWriteThread implements Runnable {
        private OutputStream outputStream;
        private PrintWriter printWriter;

        public BroadCastWriteThread() {
        }

        @Override
        public void run() {
            Scanner s = new Scanner(System.in);
            try {
                String str = "";
                System.out.println("服务端--->：" + str);
                while (s.hasNext() && ((str = s.nextLine()) != null)) {
                    final String finalStr = str;
                    //System.out.println("服务端：" + str);
                    clientIpSocketMap.values().stream()
                            .filter(Objects::nonNull)
                            .forEach(clientSocket -> {
                                try {
                                    printWriter = doWriter(clientSocket, outputStream, printWriter);
                                    printWriter.println(finalStr);
                                } catch (IOException e) {
                                    System.out.println("报错6 发送出现异常 " + e.getStackTrace());
                                    removeSocket(clientSocket);
                                }
                            });
                }
            } catch (Exception e) {
                System.out.println("报错了4，可能是客户端由于某种原因断开了连接" + e);
            } finally {
                try {
                    if (printWriter != null) {
                        printWriter.close();
                    }
                    if (outputStream != null) {
                        outputStream.close();
                    }
                } catch (Exception e) {
                    System.out.println("报错了" + 5);
                }
            }


        }
    }

    private static PrintWriter doWriter(Socket clientSocket,
                                        OutputStream outputStream,
                                        PrintWriter printWriter) throws IOException {
        outputStream = clientSocket.getOutputStream();
        printWriter = new PrintWriter(new OutputStreamWriter(outputStream, charset), true);
        return printWriter;
    }

    private static void removeSocket(Socket clientSocket){
        String clientInfo = getClientInfo(clientSocket);
        clientIpSocketMap.remove(clientInfo);
    }
}

