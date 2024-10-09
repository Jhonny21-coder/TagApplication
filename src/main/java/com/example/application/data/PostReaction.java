package com.example.application.data;

import jakarta.persistence.*;

@Entity
public class PostReaction {

     @Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     private Long id;

     @ManyToOne
     @JoinColumn(name = "user_id")
     private User reactor;

     @ManyToOne
     @JoinColumn(name = "artwork_id")
     private Artwork artwork;

     private String reactType;

     public Long getId(){
     	return id;
     }

     public void setId(Long id){
     	this.id = id;
     }

     public User getReactor(){
     	return reactor;
     }

     public void setReactor(User reactor){
     	this.reactor = reactor;
     }

     public Artwork getArtwork(){
     	return artwork;
     }

     public void setArtwork(Artwork artwork){
     	this.artwork = artwork;
     }

     public String getReactType(){
     	return reactType;
     }

     public void setReactType(String reactType){
     	this.reactType = reactType;
     }
}
