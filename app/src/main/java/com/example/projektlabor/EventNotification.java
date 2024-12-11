package com.example.projektlabor;

public class EventNotification {
    private String notificationId;
    private String eventId;
    private String senderId;
    private String senderName;
    private String receiverId;
    private String eventName;
    private long timestamp;
    private String status;
    private String notificationType;  // Új mező az értesítés típusának tárolásához

    public EventNotification() {
        // Firebase-hez szükséges üres konstruktor
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
        this.notificationType = "default";  // Alapértelmezett típus
    }

    // Új konstruktor a típus megadásával
    public EventNotification(String notificationId, String eventId, String senderId,
                             String senderName, String receiverId, String eventName,
                             String notificationType) {
        this(notificationId, eventId, senderId, senderName, receiverId, eventName);
        this.notificationType = notificationType;
    }

    // Eredeti getterek és setterek
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

    // Új getter és setter a típushoz
    public String getNotificationType() { return notificationType; }
    public void setNotificationType(String notificationType) { this.notificationType = notificationType; }
}