package com.example.application.data.notification;

import com.example.application.data.User;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@Table(name = "notification")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // The user who receives the notification

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type; // The type of notification (e.g., COMMENT, LIKE)

    @Column(nullable = false)
    private Long referenceId; // ID of the related entity (e.g., comment, like, post)

    @Column(nullable = false)
    private String message; // The notification message (pre-built)

    @Column(nullable = false)
    private boolean isRead; // Whether the notification has been read

    @Column(nullable = false)
    private LocalDateTime timestamp; // The time the notification was created

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender; // The user who receives the notification

    // Default constructor for JPA
    public Notification() {}

    public Notification(User user, User sender, NotificationType type, Long referenceId, String message) {
        this.user = user;
        this.sender = sender;
        this.type = type;
        this.referenceId = referenceId;
        this.message = message;
        this.isRead = false; // Default value for a new notification
        this.timestamp = LocalDateTime.now(ZoneId.of("Asia/Manila")); // Set timestamp to current time
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public Long getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(Long referenceId) {
        this.referenceId = referenceId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean isRead) {
        this.isRead = isRead;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public User getSender(){
    	return sender;
    }

    public void setSender(User sender){
    	this.sender = sender;
    }

    @Override
    public String toString() {
        return "Notification{" +
                "id=" + id +
                ", user=" + user +
                ", type=" + type +
                ", referenceId=" + referenceId +
                ", message='" + message + '\'' +
                ", read=" + isRead +
                ", timestamp=" + timestamp +
                '}';
    }
}
