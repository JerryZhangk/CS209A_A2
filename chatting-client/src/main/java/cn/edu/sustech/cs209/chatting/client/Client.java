package cn.edu.sustech.cs209.chatting.client;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;

public class Client extends Application {
    public static void main(String[] args) {
        launch();
    }

  @Override
  public void start(Stage stage) throws IOException {
    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("main.fxml"));
    stage.setScene(new Scene(fxmlLoader.load()));
    stage.setTitle("JerryZHang_chatting");
    stage.show();
    Controller controller = fxmlLoader.getController();
    stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
        @Override
        public void handle(WindowEvent event) {
            controller.powerOff();
        }
    });
  }
}
