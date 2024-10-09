package com.example.application.repository;

import com.example.application.data.Comment;
import com.example.application.data.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    int countByUser(User user);
    int countByArtworkId(Long artworkId);

    List<Comment> findByUserId(Long userId);
    List<Comment> findByArtworkId(Long artworkId);

    Comment findCommentByUserId(Long userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Comment c WHERE c.artwork.id = :artworkId")
    void deleteByArtworkId(@Param("artworkId") Long artworkId);
}
