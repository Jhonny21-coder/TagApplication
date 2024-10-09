package com.example.application.services.story;

import com.example.application.data.story.StoryReaction;
import com.example.application.repository.story.StoryReactionRepository;
import com.example.application.data.story.Story;
import com.example.application.repository.story.StoryRepository;
import com.example.application.data.User;
import com.example.application.repository.UserRepository;
import org.springframework.stereotype.Service;
import com.vaadin.flow.server.VaadinSession;
import java.util.Optional;

import java.util.List;

@Service
public class StoryReactionService {

    private final StoryRepository storyRepository;
    private final StoryReactionRepository storyReactionRepository;
    private final UserRepository userRepository;

    public StoryReactionService(StoryRepository storyRepository, UserRepository userRepository,
    	StoryReactionRepository storyReactionRepository){

        this.storyRepository = storyRepository;
        this.userRepository = userRepository;
        this.storyReactionRepository = storyReactionRepository;
    }

    public void saveStoryReaction(StoryReaction storyReaction) {
	storyReactionRepository.save(storyReaction);
    }

    public List<StoryReaction> getStoryReactionByReactorAndStoryId(Long reactorId, Long storyId){
    	return storyReactionRepository.findByReactorIdAndStoryId(reactorId, storyId);
    }
}
