package cn.edu.sustech.cs209.chatting.client;

import cn.edu.sustech.cs209.chatting.common.Message;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import java.io.BufferedReader;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class Controller implements Initializable {
  @FXML
   public ListView<Message> chatContentList;
  public ListView<chatPart> chatList;
  public TextArea inputArea;
  public Label currentUsername;
  public Label currentOnlineCnt;
  public Label titleForNow;
  private PrintWriter out;
  private String username;
  private String currentchat;
  private boolean isGroup;

    private ArrayList<String> userlist = new ArrayList<>();

  public List<String> getClientList() {
    return userlist;
  }

  public void setClientList(ArrayList<String> clients) {
    this.userlist = clients;
  }

  private void C2S(String msg) {
    out.println(msg);
  }

    @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {

    Dialog<String> dialog = new TextInputDialog();
    dialog.setTitle("Login");
    dialog.setHeaderText(null);
    dialog.setContentText("Username:");

    Optional<String> input = dialog.showAndWait();
  if (input.isPresent() && !input.get().isEmpty()) {
    this.username = input.get();
    this.currentUsername.setText(String.format("Current User: %s", this.username));
    this.inputArea.setWrapText(true);
    this.currentchat = "";
    try {
        Socket client = new Socket("localhost", 8225);
        // Create input and output streams
        BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        out = new PrintWriter(client.getOutputStream(), true);
        // Send the username to the server
        com.alibaba.fastjson.JSONObject requestJoinMsg = new com.alibaba.fastjson.JSONObject();
        requestJoinMsg.put("command", Message.Join);
        requestJoinMsg.put("sender", this.username);
        requestJoinMsg.put("receiver", "SERVER");
        requestJoinMsg.put("time", System.currentTimeMillis());
        requestJoinMsg.put("content", Message.Join);

        C2S(requestJoinMsg.toString());
        String msg_str;
        JSONObject object;
        boolean al = true;
        while ((msg_str = in.readLine()) != null) {
          object = JSONObject.parseObject(msg_str);
          if (object.get("command").equals(Message.DuplicateUsername)) {
            warningNotice("warning", "duplicate username");
            al = false;
            in.close();
            out.close();
            client.close();
            Platform.exit();
            break;
            } else if (object.get("command").equals(Message.JoinResp)) {
                break;
            }
        }
        if (al) {
            new Thread(new Controller.MsgTrigger(client)).start();
        }
        chatContentList.setCellFactory(new MessageCellFactory());
    } catch (IOException e) {
        System.out.println("ERROR CATCH");
    }
    } else {
        warningNotice("waring", "username cannot be null");
        Platform.exit();
    }
}

    private void requestMessage(String title) {
    com.alibaba.fastjson.JSONObject object = new com.alibaba.fastjson.JSONObject();
    object.put("command", Message.GetPrivateMsg);
    object.put("sender", this.username);
    object.put("receiver", this.currentchat);
    object.put("time", System.currentTimeMillis());
    object.put("content", title);
    C2S(object.toString());
    }

    @FXML
    public void createPrivateChat() {
    AtomicReference<String> user = new AtomicReference<>();
    Stage stage = new Stage();
    stage.setTitle("做出选择");
    ComboBox<String> userSel = new ComboBox<>();
    userSel.setPrefWidth(200.0);
    // FIXME: get the user list from server, the current user's name should be filtered out
    userSel.getItems().addAll(getClientList());
    Button okBtn = new Button("OK");
    okBtn.setOnAction(e -> {
        user.set(userSel.getSelectionModel().getSelectedItem());
        stage.close();
    });
    HBox box = new HBox(10);
    box.setAlignment(Pos.CENTER);
    box.setPadding(new Insets(20, 20, 20, 20));
    box.getChildren().addAll(userSel, okBtn);
    stage.setScene(new Scene(box));
    stage.showAndWait();
    // TODO: otherwise, create a new chat item in the left panel, the title should be the selected user's name
    // Get the selected user name
    String s1 = user.get();
if (s1 != null && !s1.equals("")) {
      this.currentchat = s1;
      this.titleForNow.setText(s1);
      this.isGroup = false;
      boolean checkChat = false;
      for (int i = 0; i <chatList.getItems().size(); i++) {
        if (chatList.getItems().get(i).getTile().equals(s1)){
            checkChat = true;
        }
      }
      if (!checkChat) {
        chatPart newChatPart = new chatPart(s1, false);
        newChatPart.setOnMouseClicked(event -> {
          Controller.this.isGroup = false;
          Controller.this.currentchat = newChatPart.getTile();
          System.out.println(this.currentchat);
          requestMessage(this.currentchat);
          titleForNow.setText(this.currentchat);
        });
        this.chatList.getItems().add(newChatPart);
      }
      requestMessage(this.currentchat);
}

    }

    @FXML
    public void doSendMessage() {
    String content = inputArea.getText();
    if (!content.equals("")) {
        inputArea.setText("");
        if (content.length() > 0) {
        com.alibaba.fastjson.JSONObject msg = new com.alibaba.fastjson.JSONObject();
        msg.put("command", Message.SendPrivateMsg);
        msg.put("sender", this.username);
        msg.put("receiver", this.currentchat);
        msg.put("time", System.currentTimeMillis());
        msg.put("content", content);
        C2S(msg.toString());
            }
        } else {
            warningNotice("提示", "不能发送空消息!");
        }
    }

  public static void warningNotice(String title, String info) {
    Stage stage = new Stage();
    stage.setTitle(title + ": " + info);
    stage.setWidth(500.0);
    stage.setHeight(200.0);
    stage.showAndWait();
  }

    /**
     * You may change the cell factory if you changed the design of {@code Message} model.
     * Hint: you may also define a cell factory for the chats displayed in the left panel, or simply override the toString method.
     */
    private class MessageCellFactory implements Callback<ListView<Message>, ListCell<Message>> {
        @Override
        public ListCell<Message> call(ListView<Message> param) {
            return new ListCell<Message>() {
                @Override
                public void updateItem(Message msg, boolean empty) {
                super.updateItem(msg, empty);
                if (empty || Objects.isNull(msg)) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                HBox wrapper = new HBox();
                Label nameLabel = new Label(msg.getSender());
                Label msgLabel = new Label(msg.getContent());

                nameLabel.setPrefSize(50, 20);
                nameLabel.setWrapText(true);
                nameLabel.setStyle("-fx-border-color: black; -fx-border-width: 1px;");

                if (username.equals(msg.getSender())) {
                    wrapper.setAlignment(Pos.TOP_RIGHT);
                    wrapper.getChildren().addAll(msgLabel, nameLabel);
                    msgLabel.setPadding(new Insets(0, 20, 0, 0));
                } else {
                    wrapper.setAlignment(Pos.TOP_LEFT);
                    wrapper.getChildren().addAll(nameLabel, msgLabel);
                    msgLabel.setPadding(new Insets(0, 0, 0, 20));
                }

                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                setGraphic(wrapper);
                }
            };
        }
    }

  public void powerOff() {
    com.alibaba.fastjson.JSONObject leaveMessage = new com.alibaba.fastjson.JSONObject();
    leaveMessage.put("command", Message.Leave);
    leaveMessage.put("sender", this.username);
    leaveMessage.put("receiver", "SERVER");
    leaveMessage.put("time", System.currentTimeMillis());
    leaveMessage.put("content", "");
    C2S(leaveMessage.toString());
  }

    private class MsgTrigger implements Runnable {
    private final Socket client;

    public MsgTrigger(Socket client) {
        this.client = client;
}

  public void run() {
      try {
  BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
  JSONObject object;
  String message;
    while ((message = in.readLine()) != null) {
        object = JSONObject.parseObject(message);
        if (object.get("command").equals(Message.LeaveResp)) {
            break;
        } else if (object.get("command").equals(Message.MaintainUserList)) {
            List<String> tem = JSON.parseArray(object.getJSONArray("list").toJSONString(), String.class);
            int currentUserCnt = tem.size();
            Platform.runLater(() -> Controller.this.currentOnlineCnt.setText("Online: " + currentUserCnt));
            ArrayList<String> usernameTmpList = new ArrayList<>(tem);
            usernameTmpList.remove(Controller.this.username);
            setClientList(usernameTmpList);
                    } else if (object.get("command").equals(Message.ReturnPrivateMsg)) {
    List<Message> msgList = JSON.parseArray(object.getJSONArray("l" +
            "ist").toJSONString(), Message.class);
    if (!isGroup) {
        String currentChatKey;
        if(Controller.this.username.compareTo(Controller.this.currentchat) > 0){
            currentChatKey = Controller.this.username + "@" + Controller.this.currentchat;
        }
        else {
            currentChatKey = Controller.this.currentchat + "@" + Controller.this.username;
        }
        if (msgList.size() > 0) {
            String sender = msgList.get(0).getSender();
            String receiver = msgList.get(0).getReceiver();
    String key;
                        if(sender.compareTo(receiver) > 0){
                            key = sender + "@" + receiver;
                        }
                        else {
                            key = receiver + "@" + sender;
                        }
                        if(currentChatKey.equals(key)){
                            Platform.runLater(() -> {
                                Controller.this.chatContentList.getItems().clear();
                                Controller.this.chatContentList.getItems().addAll(msgList);
                            });
                        }
                    } else {
                        Platform.runLater(() -> {
                            Controller.this.chatContentList.getItems().clear();
                        });
                    }
                }
            }
            System.out.println(message);
        }
        in.close();
        Platform.exit();
            } catch (IOException e) {
        Platform.runLater(() -> {
            warningNotice("warning", "server closed");
        });
        }
        }
    }
}
