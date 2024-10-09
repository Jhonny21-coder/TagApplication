package com.example.application.services.story;

import com.example.application.data.User;
import com.example.application.data.story.Story;
import com.example.application.repository.story.StoryRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;
import java.time.ZoneId;

import java.util.List;
import java.io.File;

@Service
public class StoryService {

    @Autowired
    private StoryRepository storyRepository;

    public List<Story> getUserStories(User user) {
        return storyRepository.findByUser(user);
    }

    public List<Story> getNonExpiredStoriesByUser(Long userId) {
        LocalDateTime currentTime = LocalDateTime.now(ZoneId.of("Asia/Manila"));
        //return storyRepository.findByUserAndExpirationTimeAfter(user, currentTime);
        // Or use the custom query method
        return storyRepository.findNonExpiredStoriesByUser(currentTime, userId);
    }

    public List<Story> getExpiredStoriesByUser(User user) {
        LocalDateTime currentTime = LocalDateTime.now(ZoneId.of("Asia/Manila"));
        return storyRepository.findByUserAndExpirationTimeBefore(user, currentTime);
        // Or use the custom query method
        // return storyRepository.findExpiredStories(currentTime);
    }

    public void saveStory(Story story) {
        storyRepository.save(story);
    }

    public void setStoryAsExpired(Story story){
    	story.setStatus("EXPIRED");
    	storyRepository.save(story);
    }

    public List<Story> getAllStories(){
    	return storyRepository.findAll();
    }

    // Schedule the task to run every hour (or adjust the cron expression as needed)
    //@Scheduled(cron = "0 0 * * * ?") // Runs every hour
    public void deleteExpiredStories(Story story) {
    	storyRepository.delete(story);

        String filePathToDelete = "src/main/resources/META-INF/resources/story_images/" + story.getImageUrl();

	File file = new File(filePathToDelete);

	if(file.exists()){
	   if(file.delete()){
	      System.out.println("File deleted: " + story.getImageUrl());
	   }
	}else{
	   System.out.println("File does not exists. Cannot delete.");
	}
    }

    public void updateExpiredStories(){
    	storyRepository.updateStoryToExpired();
    }
}
