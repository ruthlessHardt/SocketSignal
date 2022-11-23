package com.ru.socket2;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Client3 {
    //线程池
    private static ExecutorService executorService=Executors.newFixedThreadPool(10);

    private final static String charset="UTF8";

    private static boolean connection_state = false;

    public static void main(String [] args){
        while (!connection_state){
            connect();
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * connect
     */
    public static void connect(){
        try{
            Socket socket=new Socket();
            socket.connect(new InetSocketAddress("127.0.0.1", 10005));
            connection_state = true;
            System.out.println("客户端已启动，您本地的字符格式为"+Charset.defaultCharset().name()+"，请输入信息：");
            executorService.submit(new ReadThread(socket));
            executorService.submit(new WriteThread(socket));
            executorService.submit(new HeartThread(socket));
//            executorService.shutdown();//在子线程运行结束后关闭线程池，否则主进程不会结束，感觉测试不完整

//由于客户端只有两个进程，所有可以不使用线程池
//            Thread t1=new Thread(new ReadThread(socket));
//            Thread t2=new Thread(new WriteThread(socket));
//            t1.start();
//            t2.start();
//            t1.join();
//            t2.join();
//            System.out.println("结束了--");

        }catch(Exception e){
//            e.printStackTrace();
            System.out.println(e.getMessage());
            connection_state = false;
        }finally {

        }
    }

    private static void reconnect(){
        while (!connection_state){
            System.out.println("正在尝试重新连接.......");
            connect();
        }
        try {
            Thread.sleep(3000);
        } catch (Exception e) {
//            e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }

    //发送消息的线程
    public static class WriteThread implements Runnable{
        private Socket clientSocket;
        private OutputStream outputStream;
        private PrintWriter printWriter;
        public WriteThread(Socket clientSocket){
            this.clientSocket=clientSocket;
        }
        @Override
        public void run() {
            Scanner s=new Scanner(System.in);
            while(true){
                try{
                    outputStream =clientSocket.getOutputStream();
                    printWriter=new PrintWriter(new OutputStreamWriter(outputStream,charset),true);
                    printWriter.println(s.nextLine());

                }catch(Exception e){
                    System.out.println("报错了4，可能是服务端由于某种原因断开了连接，不再向服务器发送信息"+e);

                    try{
                        if(printWriter!=null){ printWriter.close();}
                        if(outputStream!=null){ outputStream.close();}
                        if(clientSocket!=null){ clientSocket.close();}
                        break;//结束本次对客户端的循环
                    }catch(Exception e2){
                        System.out.println("报错了5"+e2);
                    }

                }
            }
        }
    }

    //接收消息的线程
    public static class ReadThread implements Runnable{
        private Socket clientSocket;

        private InputStream inputStream;

        private BufferedReader bufferedReader;

        public ReadThread(Socket clientSocket){
            this.clientSocket=clientSocket;
        }
        @Override
        public void run() {
            //如果客户端没有关闭写，则持续读取
            while(true){
                try{
                    inputStream=clientSocket.getInputStream();
                    bufferedReader=new BufferedReader(new InputStreamReader(inputStream,charset));

                    String str;
                    for(;(str=bufferedReader.readLine())!=null;){
                        System.out.println("服务端："+str);
                    }

                }catch(Exception e) {
                    System.out.println("可能是服务端由于某种原因断开了连接," + e.getMessage() + ";" + Thread.currentThread().getName());
                    break;//结束本次对客户端的循环
                }finally{
                    System.out.println("ReadThread finally");
                    try{
                        if(inputStream!=null){ inputStream.close();}
                        if(bufferedReader!=null){ bufferedReader.close();}
                        if(clientSocket!=null){ clientSocket.close();}
                    }catch(Exception e){
                        System.out.println("报错了"+5);
                    }
                }
            }
        }
    }

    static class HeartThread implements Runnable{
        private Socket clientSocket;
        private OutputStream outputStream;
        private PrintWriter printWriter;

        public HeartThread(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try {
                System.out.println("心跳包线程已启动");
                System.out.println("==================================================");
                while(true){
                    Thread.sleep(7000);
                    outputStream =clientSocket.getOutputStream();
                    printWriter=new PrintWriter(new OutputStreamWriter(outputStream,charset),true);
                    printWriter.println("客户端3 的心跳...");
                }
            } catch (InterruptedException | IOException e) {
//                e.printStackTrace();
                System.out.println(e.getMessage());
                try {
                    clientSocket.close();
                    connection_state = false;
                    reconnect();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}

