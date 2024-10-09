package com.example.application.data.story;

import com.example.application.data.User;

import jakarta.persistence.*;

@Entity
public class StoryReaction {

     @Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     private Long id;

     @ManyToOne
     @JoinColumn(name = "user_id")
     private User reactor;

     @ManyToOne
     @JoinColumn(name = "story_id")
     private Story story;

     private String reactType;

     public StoryReaction() {}

     public StoryReaction(User reactor, Story story, String reactType){
     	this.reactor = reactor;
     	this.story = story;
     	this.reactType = reactType;
     }

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

     public Story getStory(){
     	return story;
     }

     public void setStory(Story story){
     	this.story = story;
     }

     public String getReactType(){
     	return reactType;
     }

     public void setReactType(String reactType){
     	this.reactType = reactType;
     }
}
