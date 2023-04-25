package cn.edu.sustech.cs209.chatting.common;

public class Message {
    private final String command;
    private final String sender;
    private final String receiver;
    private final String content;

    private final Long timestamp;
    public static final String MaintainUserList = "UPDATE_CLIENT_LIST";
    public static final String GetPrivateMsg = "REQUEST_PRIVATE_CHAT";
    public static final String ReturnPrivateMsg = "RESPONSE_PRIVATE_CHAT";
    public static final String SendPrivateMsg = "SEND_PRIVATE_MESSAGE";
    public static final String DuplicateUsername = "DUPLICATE_USERNAME";
    public static final String Leave = "REQUEST_TO_LEAVE";
    public static final String LeaveResp = "ALLOW_TO_LEAVE";
    public static final String Join = "REQUEST_TO_JOIN";
    public static final String JoinResp = "ALLOW_TO_JOIN";

    public Message(String command, String sender, String receiver, Long timestamp, String content) {
        this.command = command;
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
        this.timestamp = timestamp;
    }

    public String getSender() {
        return sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public String getContent() {
        return content;
    }


}
