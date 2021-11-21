package com.javarush.task.task30.task3008;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

public class Server {
    private static Map<String, Connection> connectionMap = new java.util.concurrent.ConcurrentHashMap<String, Connection>();
/*Класс Handler должен реализовывать протокол общения с клиентом.*/

    private static class Handler extends Thread{
        private Socket socket;

        private Handler(Socket socket){
            this.socket = socket;
        }

        private  String serverHandshake(Connection connection) throws IOException, ClassNotFoundException{

            while (true){
                connection.send(new Message(MessageType.NAME_REQUEST));

                Message message = connection.receive();

                if(message.getType() != MessageType.USER_NAME){
                    ConsoleHelper.writeMessage("тип сообщения неверный");
                    continue;
                }
                String userName = message.getData();
                if (userName.isEmpty()){
                    ConsoleHelper.writeMessage("имя пользователя пустое");
                    continue;
                }
                if (connectionMap.containsKey(userName)){
                    ConsoleHelper.writeMessage("полученное имя пользователя уже есть в списке");
                    continue;
                }
                connectionMap.put(userName,connection);
                connection.send(new Message(MessageType.NAME_ACCEPTED));
                return userName;
            }
        };


        private void notifyUsers(Connection connection, String userName) throws IOException{
            /*отправка новому участнику информации об остальных участниках чата.*/

            for (Map.Entry<String, Connection> e:
                connectionMap.entrySet()) {
                String currentUserName = e.getKey();
                if(!userName.equals(currentUserName)){
                    connection.send(new Message(MessageType.USER_ADDED,currentUserName));
                }
            }
        }
}

    public static void sendBroadcastMessage(Message message){
        for (Connection connection : connectionMap.values()) {
            try {
                connection.send(message);
            } catch (IOException e) {
                ConsoleHelper.writeMessage("не удалось отправить сообщение");
            }
        }
    }

    public static void main(String[] args){
        int port = ConsoleHelper.readInt();
        try(ServerSocket serverSocket = new java.net.ServerSocket(port)){

            ConsoleHelper.writeMessage("сервер запущен");

            while (true){
                Socket socket = serverSocket.accept();
                Handler handler = new Handler(socket);
                handler.start();
            }
        }catch (IOException e){
            ConsoleHelper.writeMessage("произошла ошибка");

        }
    }
}
