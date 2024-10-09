package com.example.application.repository;

import com.example.application.data.SavedPost;
import com.example.application.data.Artwork;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SavedPostRepository extends JpaRepository<SavedPost, Long> {

    List<SavedPost> findByUserId(Long userId);

    SavedPost findByArtworkIdAndUserId(Long artworkId, Long userId);
}
