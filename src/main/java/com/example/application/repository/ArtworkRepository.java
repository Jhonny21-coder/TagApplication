package com.example.application.repository;

import com.example.application.data.Artwork;
import com.example.application.data.StudentInfo;
import com.example.application.data.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ArtworkRepository extends JpaRepository<Artwork, Long> {

   int countByStudentInfoUser(User user);

   List<Artwork> findByUserId(Long userId);

   @Query("SELECT c FROM Artwork c " +
       "WHERE c.user.id = :userId AND (LOWER(c.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<Artwork> search(@Param("searchTerm") String searchTerm,
    			 @Param("userId") Long userId);
}
