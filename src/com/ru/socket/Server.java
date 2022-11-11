package com.ru.socket;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Server {
    private static Integer count = 0;
    public static void main(String[] args) {
        try {
            System.out.println("服务器运行...........");
            ServerSocket serverSocket = new ServerSocket(9999);
            while(true){
                Socket socket = serverSocket.accept();
                new  Thread(new Server_listen(socket)).start();
                new  Thread(new Server_send(socket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

static class Server_listen implements Runnable{
    private Socket socket;

    public Server_listen(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            Map<String,String> map = (Map<String, String>) ois.readObject();
            int count = 0;
            while(true){
                /**
                 *                 //心跳检测不到，判定断线
                 *                 //有心跳则重置计时器
                 *                 if(Objects.nonNull(map) && "heart".equals(map.get("type"))){
                 *                     count = 0;
                 *                 }
                 *                 count++;
                 *                 if(count == 7){
                 *                     System.out.println("超过7s未检测到心跳！！ 判定--> Client已断连");
                 *                 }
                 */
                if(ois.readObject()!=null){
                    System.out.println(ois.readObject());
                }
            }
        } catch (IOException | ClassNotFoundException e) {
//            e.printStackTrace();
            if("Connection reset".equals(e.getMessage())){
                System.out.println("Client已经断线！");
            }
        }finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

static class Server_send implements Runnable{
    private Socket socket;

    public Server_send(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            Scanner scanner = new Scanner(System.in);
            while(true){
                System.out.println("请输入---》");
                String s = scanner.nextLine();
                Map<String,String> map = new HashMap<>();
                map.put("type","chat");
                map.put("msg",s);
                oos.writeObject(map);
                oos.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
}
