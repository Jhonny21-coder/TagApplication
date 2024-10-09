package com.example.application.data;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.ZoneId;
import java.time.LocalDateTime;

@Entity
public class Comment {

     @Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     private Long id;

     @ManyToOne
     @JoinColumn(name = "user_id")
     private User user;

     @ManyToOne
     @JoinColumn(name = "artwork_id")
     private Artwork artwork;

     private String fullName;
     private String comments;
     private String userImage;
     private LocalDateTime dateTimePosted;

     private long likeReact;
     private long heartReact;
     private long happyReact;

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

     public Artwork getArtwork(){
     	return artwork;
     }

     public void setArtwork(Artwork artwork){
     	this.artwork = artwork;
     }

     public String getFullName(){
	return fullName;
     }

     public void setFullName(String fullName){
	this.fullName = fullName;
     }

     public String getComments(){
	return comments;
     }

     public void setComments(String comments){
	this.comments = comments;
     }

     public String getUserImage() {
        return userImage;
     }

     public void setUserImage(String userImage) {
        this.userImage = userImage;
     }

     public LocalDateTime getDateTimePosted(){
     	return dateTimePosted;
     }

     public void setDateTimePosted(){
     	this.dateTimePosted = LocalDateTime.now(ZoneId.of("Asia/Manila"));
     }

     public long getLikeReact(){
     	return likeReact;
     }

     public void setLikeReact(long likeReact){
     	this.likeReact = likeReact;
     }

     public long getHeartReact(){
        return heartReact;
     }

     public void setHeartReact(long heartReact){
        this.heartReact = heartReact;
     }

     public long getHappyReact(){
        return happyReact;
     }

     public void setHappyReact(long happyReact){
        this.happyReact = happyReact;
     }
}
