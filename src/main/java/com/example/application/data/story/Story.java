package com.example.application.data.story;

import com.example.application.data.User;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@Table(name = "stories")
public class Story {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private LocalDateTime creationTime;

    @Column(nullable = false)
    private LocalDateTime expirationTime;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String imageUrl;

    private String status;
    private double _left;
    private double top;
    private double width;
    private double height;

    public Story() {}

    public Story(String content, String imageUrl, User user, String status) {
        this.content = content;
        this.user = user;
        this.creationTime = LocalDateTime.now(ZoneId.of("Asia/Manila"));
        this.expirationTime = creationTime.plusHours(24);
        this.imageUrl = imageUrl;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getImageUrl(){
    	return imageUrl;
    }

    public String getStatus(){
    	return status;
    }

    public void setStatus(String status){
    	this.status = status;
    }

    public LocalDateTime getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(LocalDateTime creationTime) {
        this.creationTime = creationTime;
    }

    public LocalDateTime getExpirationTime(){
    	return expirationTime;
    }

    public void setExpirationTime(LocalDateTime expirationTime) {
        this.expirationTime = expirationTime;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public double getLeft() {
    	return _left;
    }

    public void setLeft(double _left) {
    	this._left = _left;
    }

    public double getTop() {
    	return top;
    }

    public void setTop(double top) {
    	this.top = top;
    }

    public double getWidth() {
    	return width;
    }

    public void setWidth(double width) {
    	this.width = width;
    }

    public double getHeight() {
    	return height;
    }

    public void setHeight(double height) {
    	this.height = height;
    }
}
