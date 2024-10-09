package com.example.application.repository;

import com.example.application.data.Comment;
import com.example.application.data.Reply;
import com.example.application.data.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ReplyRepository extends JpaRepository<Reply, Long> {

    List<Reply> findByCommentId(Long commentId);

    //Reply findByReplyId(Long replyId);
    @Modifying
    @Transactional
    @Query("DELETE FROM Reply r WHERE r.comment.id = :commentId")
    void deleteByCommentId(@Param("commentId") Long commentId);
}
