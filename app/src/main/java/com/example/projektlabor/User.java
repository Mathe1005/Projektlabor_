package com.example.projektlabor;

import java.util.HashMap;
import java.util.Map;

public class User {
    private String userId;
    private String username;
    private String email;
    private Map<String, Boolean> friends;      // Elfogadott barátok
    private Map<String, Boolean> blockedUsers; // Tiltott felhasználók
    private Map<String, Boolean> invitedEvents; // Események amikre meg van hívva

    public User() {
    }

    public User(String userId, String username, String email) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.friends = new HashMap<>();
        this.blockedUsers = new HashMap<>();
        this.invitedEvents = new HashMap<>();
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Map<String, Boolean> getFriends() { return friends; }
    public void setFriends(Map<String, Boolean> friends) { this.friends = friends; }

    public Map<String, Boolean> getBlockedUsers() { return blockedUsers; }
    public void setBlockedUsers(Map<String, Boolean> blockedUsers) { this.blockedUsers = blockedUsers; }

    public Map<String, Boolean> getInvitedEvents() { return invitedEvents; }
    public void setInvitedEvents(Map<String, Boolean> invitedEvents) { this.invitedEvents = invitedEvents; }

    public void blockUser(String userId) {
        if (blockedUsers == null) {
            blockedUsers = new HashMap<>();
        }
        blockedUsers.put(userId, true);
        // Ha blokkoljuk, töröljük a barátok közül
        if (friends != null) {
            friends.remove(userId);
        }
    }

    public void unblockUser(String userId) {
        if (blockedUsers != null) {
            blockedUsers.remove(userId);
        }
    }

    public boolean isBlocked(String userId) {
        return blockedUsers != null && blockedUsers.containsKey(userId);
    }

    public void addFriend(String friendId) {
        if (friends == null) {
            friends = new HashMap<>();
        }
        friends.put(friendId, true);
    }

    public void removeFriend(String friendId) {
        if (friends != null) {
            friends.remove(friendId);
        }
    }

    public void addInvitedEvent(String eventId) {
        if (invitedEvents == null) {
            invitedEvents = new HashMap<>();
        }
        invitedEvents.put(eventId, true);
    }

    public void removeInvitedEvent(String eventId) {
        if (invitedEvents != null) {
            invitedEvents.remove(eventId);
        }
    }
}