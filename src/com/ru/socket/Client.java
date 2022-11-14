package com.ru.socket;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Client {
    private static Socket socket;
    private static boolean connection_state = false;



    public static void main(String[] args) {
        while (!connection_state){
            connect();
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static void connect(){
        try {
            socket = new Socket("localhost",9999);
            connection_state = true;
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            new Thread(new Client_listen(socket,ois)).start();
            new Thread(new Client_send(socket,oos)).start();
            new Thread(new Client_heart(socket,oos)).start();
        } catch (IOException e) {
            e.printStackTrace();
            connection_state = false;
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
            e.printStackTrace();
        }
    }

    static class Client_listen implements Runnable{
        private Socket socket;
        private ObjectInputStream ois;

        public Client_listen(Socket socket, ObjectInputStream ois) {
            this.socket = socket;
            this.ois = ois;
        }

        @Override
        public void run() {
            try {
                while (true){
                    System.out.println(ois.readObject());
                }
            } catch (IOException | ClassNotFoundException e) {
//                e.printStackTrace();
                System.out.println(e.getMessage());
            }
        }
    }

    static class Client_send implements Runnable{
        private Socket socket;
        private ObjectOutputStream oos;

        public Client_send(Socket socket, ObjectOutputStream oos) {
            this.socket = socket;
            this.oos = oos;
        }

        @Override
        public void run() {
            try {
                Scanner scanner = new Scanner(System.in);
                while(true){
                    System.out.println("请输入你要发送的内容：");
                    String s = scanner.nextLine();
                    Map<String,String> map = new HashMap<>();
                    map.put("type","chat");
                    map.put("msg",s);
                    oos.writeObject(map);
                    oos.flush();
                }
            } catch (IOException e) {
//                e.printStackTrace();
                System.out.println(e.getMessage());
            }
        }
    }

    static class Client_heart implements Runnable{
        private Socket socket;
        private ObjectOutputStream oos;

        public Client_heart(Socket socket, ObjectOutputStream oos) {
            this.socket = socket;
            this.oos = oos;
        }


        @Override
        public void run() {
            try {
                System.out.println("心跳包线程启动");
                while(true){
                    Thread.sleep(7000);
                    Map<String,String> map = new HashMap<>();
                    map.put("type","heart");
                    map.put("msg","来自Client的心跳包");
                    oos.writeObject(map);
                    oos.flush();
                }
            } catch (IOException | InterruptedException e) {
//                e.printStackTrace();
                System.out.println(e.getMessage());
                try {
                    socket.close();
                    Client.connection_state = false;
                    Client.reconnect();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}
