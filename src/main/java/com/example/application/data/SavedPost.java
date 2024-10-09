package com.example.application.data;

import jakarta.persistence.*;

@Entity
public class SavedPost {

     @Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     private Long id;

     @ManyToOne
     @JoinColumn(name = "artwork_id")
     private Artwork artwork;

     @ManyToOne
     @JoinColumn(name = "user_id")
     private User user;

     private String dateTime;

     public Long getId(){
     	return id;
     }

     public void setId(Long id){
     	this.id = id;
     }

     public Artwork getArtwork(){
     	return artwork;
     }

     public void setArtwork(Artwork artwork){
     	this.artwork = artwork;
     }

     public User getUser(){
     	return user;
     }

     public void setUser(User user){
     	this.user = user;
     }

     public String getDateTime(){
     	return dateTime;
     }

     public void setDateTime(String dateTime){
     	this.dateTime = dateTime;
     }
}
