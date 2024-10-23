package com.example.projektlabor;

import java.util.HashMap;
import java.util.Map;

public class User {
    private String userId;
    private String username;
    private String email;
    private Map<String, Boolean> friends;

    public User() {
    }

    public User(String userId, String username, String email) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.friends = new HashMap<>();
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Map<String, Boolean> getFriends() { return friends; }
    public void setFriends(Map<String, Boolean> friends) { this.friends = friends; }

    public void addFriend(String friendId) {
        if (friends == null) {
            friends = new HashMap<>();
        }
        friends.put(friendId, true);
    }

    @Override
    public String toString() {
        return "User{" +
                "userId='" + userId + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", friendsCount=" + (friends != null ? friends.size() : 0) +
                '}';
    }
}