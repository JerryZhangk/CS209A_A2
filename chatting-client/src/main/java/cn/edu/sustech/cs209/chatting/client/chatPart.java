package cn.edu.sustech.cs209.chatting.client;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class chatPart extends HBox {
    private Label title;
    private boolean isGroup;
    public chatPart(String title, boolean isGroup){
        super(10); // spacing between children
        this.isGroup = isGroup;
        this.title = new Label(title);
        this.title.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        this.getChildren().addAll(this.title);
    }
    public String getTile(){
        return title.getText();
    }

}
