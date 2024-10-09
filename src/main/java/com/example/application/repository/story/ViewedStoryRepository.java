package com.example.application.repository.story;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.application.data.User;
import com.example.application.data.story.ViewedStory;
import com.example.application.data.story.Story;

import java.util.List;

public interface ViewedStoryRepository extends JpaRepository<ViewedStory, Long> {
    List<ViewedStory> findByUser(User user);
    ViewedStory findByUserAndStory(User user, Story story);
}
