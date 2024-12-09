package com.example.projektlabor;

public class EventNotification {
    private String notificationId;
    private String eventId;
    private String senderId;   // aki küldte a meghívót
    private String senderName; // a küldő neve
    private String receiverId; // aki kapta a meghívót
    private String eventName;  // az esemény neve
    private long timestamp;
    private String status;     // "pending", "accepted", "declined"

    public EventNotification() {
        // Firebase számára szükséges üres konstruktor
    }

    public EventNotification(String notificationId, String eventId, String senderId,
                             String senderName, String receiverId, String eventName) {
        this.notificationId = notificationId;
        this.eventId = eventId;
        this.senderId = senderId;
        this.senderName = senderName;
        this.receiverId = receiverId;
        this.eventName = eventName;
        this.timestamp = System.currentTimeMillis();
        this.status = "pending";
    }

    // Getterek és setterek
    public String getNotificationId() { return notificationId; }
    public void setNotificationId(String notificationId) { this.notificationId = notificationId; }
    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }
    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }
    public String getReceiverId() { return receiverId; }
    public void setReceiverId(String receiverId) { this.receiverId = receiverId; }
    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}