package com.example.projektlabor;

public class FriendRequest {
    private String requestId;
    private String senderId;
    private String receiverId;
    private String senderEmail;
    private String senderUsername;
    private long timestamp;
    private String status; // "pending", "accepted", "rejected"

    public FriendRequest() {
        // Firebase-hez szükséges üres konstruktor
    }

    public FriendRequest(String senderId, String receiverId, String senderEmail, String senderUsername) {
        this.requestId = senderId + "_" + receiverId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.senderEmail = senderEmail;
        this.senderUsername = senderUsername;
        this.timestamp = System.currentTimeMillis();
        this.status = "pending";
    }

    // Getterek és setterek
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getReceiverId() { return receiverId; }
    public void setReceiverId(String receiverId) { this.receiverId = receiverId; }

    public String getSenderEmail() { return senderEmail; }
    public void setSenderEmail(String senderEmail) { this.senderEmail = senderEmail; }

    public String getSenderUsername() { return senderUsername; }
    public void setSenderUsername(String senderUsername) { this.senderUsername = senderUsername; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
