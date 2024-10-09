package com.example.application.repository.story;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import com.example.application.data.User;
import com.example.application.data.story.Story;

import java.util.List;
import java.time.LocalDateTime;

public interface StoryRepository extends JpaRepository<Story, Long> {
    List<Story> findByUser(User user);

    List<Story> findByUserAndExpirationTimeBefore(User user, LocalDateTime currentTime);

    // Method to find stories where expirationTime is after the current time
    List<Story> findByUserAndExpirationTimeAfter(User user, LocalDateTime currentTime);

    // Find stories older than 24 hours
    @Query("SELECT s FROM Story s WHERE s.expirationTime > :currentTime AND s.user.id = :userId")
    List<Story> findNonExpiredStoriesByUser(@Param("currentTime") LocalDateTime currentTime, Long userId);

    // Alternatively, using a custom query
    @Query("SELECT s FROM Story s WHERE s.expirationTime < :currentTime")
    List<Story> findExpiredStories(@Param("currentTime") LocalDateTime currentTime);

    @Transactional
    @Modifying
    @Query("UPDATE Story s SET s.status = 'EXPIRED' WHERE s.expirationTime < CURRENT_TIMESTAMP")
    void updateStoryToExpired();
}
