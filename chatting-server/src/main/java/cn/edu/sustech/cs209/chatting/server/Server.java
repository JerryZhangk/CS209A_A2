package cn.edu.sustech.cs209.chatting.server;

import cn.edu.sustech.cs209.chatting.common.Message;
import com.alibaba.fastjson.JSONObject;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class Server {
  private static final ArrayList<ClientThread> clients = new ArrayList<>();
  private static final HashMap<String, ArrayList<Message>> messageHistory = new HashMap<>();

  public static void main(String[] args) {
    try {
      ServerSocket server = new ServerSocket(8225);
      // noinspection InfiniteLoopStatement
      while (true) {
      Socket socket = server.accept();
      Server.ClientThread client = new ClientThread(socket);
      boolean uniqueNameCheck = true;
      for (ClientThread it : Server.clients) {
          if (client.getUsername().equals(it.getUsername())) {
          uniqueNameCheck = false;
          break;
        }
      }
      if (!uniqueNameCheck) {

          com.alibaba.fastjson.JSONObject object = new com.alibaba.fastjson.JSONObject();
          object.put("command", Message.DuplicateUsername);
          object.put("sender", "Server");
          object.put("receiver", client.username);
          object.put("time", System.currentTimeMillis());
          object.put("content", "");
          client.sendMessage(object.toString());
          continue;
      }
        com.alibaba.fastjson.JSONObject object = new com.alibaba.fastjson.JSONObject();
        object.put("command", Message.JoinResp);
        object.put("sender", "Server");
        object.put("receiver", client.username);
        object.put("time", System.currentTimeMillis());
        object.put("content", Message.JoinResp);
        client.sendMessage(object.toString());
        clients.add(client);
        client.start();
    ArrayList<String> list = new ArrayList<>();
    for (int i = 0; i < clients.size(); i++) {
        list.add(clients.get(i).username);
    }

        // 通知每个用户新增了client, 要求更新client列表
        broadcast(Message.MaintainUserList, list);
        }
    } catch (IOException e) {
        System.out.println("ERROR CATCH");
    } catch (JSONException e) {
        throw new RuntimeException(e);
    }
    }

  public static synchronized void broadcast(String command, ArrayList<String> list) throws JSONException {
    for (ClientThread client : clients) {
      com.alibaba.fastjson.JSONObject object = new com.alibaba.fastjson.JSONObject();
      object.put("command", command);
      object.put("sender", "Server");
      object.put("receiver", client.username);
      object.put("time", System.currentTimeMillis());
      object.put("list", list);
      client.sendMessage(object.toString());
    }
 }

    public static class ClientThread extends Thread {
    private final Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String username;

    public String getUsername() {
            return username;
        }

  public ClientThread(Socket socket) {
    this.socket = socket;
    try {
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
        String msg = in.readLine();
        JSONObject object = JSONObject.parseObject(msg);
        username = object.get("sender").toString();
      } catch (IOException e) {
        System.out.println("ERROR CATCH");
      }
    }

    public void sendMessage(String message) {
        out.println(message);
    }

    private synchronized void removeClient(ClientThread clientThread) {
        clients.remove(clientThread);
    }

    public void run() {
      System.out.println("Connected: " + socket.getInetAddress() + " - " + username);
    String message;
    try {
        JSONObject object;
        while ((message = in.readLine()) != null) {
            object = JSONObject.parseObject(message);
            if (object.get("command").equals(Message.SendPrivateMsg)) {
                String sender = object.get("sender").toString();
                String receiver = object.get("receiver").toString();
                String key = sender.compareTo(receiver) > 0?sender + "@" + receiver:receiver + "@" + sender;
                if (!messageHistory.containsKey(key)) {
                    messageHistory.put(key, new ArrayList<>());
                }
        Message tem = new Message(object.get("command").toString(), object.get("sender").toString(),
                object.get("receiver").toString(), Long.parseLong(object.get("time").toString()),
                object.get("content").toString());
        messageHistory.get(key).add(tem);
        sendPrivateMessageHistory(tem, key);
            }
            else if (object.get("command").equals(Message.GetPrivateMsg)) {
    Message tmp = new Message(object.get("command").toString(), object.get("sender").toString(),
            object.get("receiver").toString(), Long.parseLong(object.get("time").toString()), object.get("content").toString());
                String sender = tmp.getSender();
                String receiver = tmp.getReceiver();
                String key = sender.compareTo(receiver) > 0?sender + "@" + receiver:receiver + "@" + sender;
                if (!messageHistory.containsKey(key)) {
                    messageHistory.put(key, new ArrayList<>());
                }
                sendPrivateMessageHistory(tmp, key);
            } else if (message.startsWith(Message.Leave)) {

            }
            System.out.println(message);
        }
    } catch (IOException e) {
        System.out.println("ERROR CATCH");
    } finally {
        try {
            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {
            System.out.println("ERROR CATCH");
        }
    }
    }

    private void sendPrivateMessageHistory(Message tmp, String key) {
      ArrayList<Message> list = messageHistory.get(key);
      com.alibaba.fastjson.JSONObject responseToMsgSender = new com.alibaba.fastjson.JSONObject();
      responseToMsgSender.put("command",Message.ReturnPrivateMsg);
      responseToMsgSender.put("sender", "Server");
      responseToMsgSender.put("receiver",tmp.getSender());
      responseToMsgSender.put("time", System.currentTimeMillis());
      responseToMsgSender.put("list", list);
      this.sendMessage(responseToMsgSender.toString());
      for (ClientThread ct: clients) {
          if(ct.getUsername().equals(tmp.getReceiver())){
              responseToMsgSender.put("command",Message.ReturnPrivateMsg);
              responseToMsgSender.put("sender", "Server");
              responseToMsgSender.put("receiver",tmp.getReceiver());
              responseToMsgSender.put("time", System.currentTimeMillis());
              responseToMsgSender.put("list", list);
              ct.sendMessage(responseToMsgSender.toString());
              break;
        }
    }
    }
}
}