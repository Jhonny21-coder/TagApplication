package com.example.application.data.story;

import com.example.application.data.User;

import jakarta.persistence.*;

@Entity
@Table(name = "viewed_stories")
public class ViewedStory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "story_id", nullable = false)
    private Story story;

    // Constructors, getters, and setters

    public ViewedStory() {}

    public ViewedStory(User user, Story story) {
        this.user = user;
        this.story = story;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Story getStory() {
        return story;
    }

    public void setStory(Story story) {
        this.story = story;
    }
}
