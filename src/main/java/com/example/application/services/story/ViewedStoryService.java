package com.example.application.services.story;

import com.example.application.data.User;
import com.example.application.data.story.Story;
import com.example.application.data.story.ViewedStory;
import com.example.application.repository.story.ViewedStoryRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ViewedStoryService {

    @Autowired
    private ViewedStoryRepository viewedStoryRepository;

    public List<ViewedStory> getViewedStoriesByUser(User user) {
        return viewedStoryRepository.findByUser(user);
    }

    public boolean hasUserViewedStory(User user, Story story) {
        return viewedStoryRepository.findByUserAndStory(user, story) != null;
    }

    public ViewedStory saveViewedStory(ViewedStory viewedStory) {
        return viewedStoryRepository.save(viewedStory);
    }
}
