package com.example.application.repository.story;

import com.example.application.data.story.Story;
import com.example.application.data.story.StoryReaction;
import com.example.application.data.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface StoryReactionRepository extends JpaRepository<StoryReaction, Long> {

    List<StoryReaction> findByStoryId(Long storyId);
    List<StoryReaction> findByReactorIdAndStoryId(Long reactorId, Long storyId);
}
