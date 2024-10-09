package com.example.application.services;

import com.example.application.data.Reply;
import com.example.application.repository.ReplyRepository;
import com.example.application.data.Comment;
import com.example.application.repository.CommentRepository;
import com.example.application.data.User;
import com.example.application.repository.UserRepository;
import org.springframework.stereotype.Service;
import com.vaadin.flow.server.VaadinSession;
import java.util.Optional;

import java.util.List;
import java.util.Date;
import java.time.Instant;
import java.time.ZoneId;
import java.time.LocalDateTime;

@Service
public class ReplyService {

    private final CommentRepository commentRepository;
    private final ReplyRepository replyRepository;
    private final UserRepository userRepository;

    public ReplyService(CommentRepository commentRepository, UserRepository userRepository,
    	ReplyRepository replyRepository){

        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.replyRepository = replyRepository;
    }

    public List<Reply> getAllReplies() {
        return replyRepository.findAll();
    }

    public void saveReply(User replier, String userReply, Comment comment) {
        if(comment != null && replier != null){
            Reply reply = new Reply();
            reply.setReplier(replier);
            reply.setReply(userReply);
            reply.setComment(comment);
            reply.setDateTimePosted(LocalDateTime.now(ZoneId.of("Asia/Manila")));

            replyRepository.save(reply);
        }
    }

    public List<Reply> getRepliesByCommentId(Long commentId){
    	return replyRepository.findByCommentId(commentId);
    }

    public Reply getReplyById(Long replyId){
    	return replyRepository.findById(replyId).orElse(null);
    }

    public void deleteRepliesByCommentId(Long commentId){
    	replyRepository.deleteByCommentId(commentId);
    }
}
