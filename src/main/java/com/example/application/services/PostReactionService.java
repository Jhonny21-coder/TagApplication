package com.example.application.services;

import com.example.application.data.PostReaction;
import com.example.application.repository.PostReactionRepository;
import com.example.application.data.Artwork;
import com.example.application.repository.ArtworkRepository;
import com.example.application.data.User;
import com.example.application.repository.UserRepository;
import org.springframework.stereotype.Service;
import com.vaadin.flow.server.VaadinSession;
import java.util.Optional;

import java.util.List;
import java.util.Date;
import java.time.Instant;

@Service
public class PostReactionService {

    private final ArtworkRepository artworkRepository;
    private final PostReactionRepository postReactionRepository;
    private final UserRepository userRepository;

    public PostReactionService(ArtworkRepository artworkRepository, UserRepository userRepository,
    	PostReactionRepository postReactionRepository){

        this.artworkRepository = artworkRepository;
        this.userRepository = userRepository;
        this.postReactionRepository = postReactionRepository;
    }

    public List<PostReaction> getAllPostReactions() {
        return postReactionRepository.findAll();
    }

    public void savePostReaction(Artwork artwork, User reactor, String reactType) {
        if(reactor != null && artwork != null){
            PostReaction postReaction = new PostReaction();
            postReaction.setReactor(reactor);
            postReaction.setArtwork(artwork);
            postReaction.setReactType(reactType);

            postReactionRepository.save(postReaction);
        }
    }

    public List<PostReaction> getPostReactionsByArtworkId(Long artworkId) {
        return postReactionRepository.findByArtworkId(artworkId);
    }

    public PostReaction getPostReactionByReactorAndArtworkId(Long reactorId, Long artworkId){
    	return postReactionRepository.findByReactorIdAndArtworkId(reactorId, artworkId);
    }

    public void removePostReaction(Long reactorId, Long artworkId) {
        // Find the like reaction by user email and artwork ID
        PostReaction reaction = postReactionRepository.findByReactorIdAndArtworkId(reactorId, artworkId);

        // If the like reaction exists, delete it
        if (reaction != null) {
            postReactionRepository.delete(reaction);
        }
    }

    public void deletePostReactionsByArtworkId(Long artworkId){
    	postReactionRepository.deleteByArtworkId(artworkId);
    }

    public void updatePostReaction(PostReaction reactor, String reactType){
    	reactor.setReactType(reactType);

    	postReactionRepository.save(reactor);
    }

    public PostReaction getPostReactionByArtwork(Artwork artwork){
    	return postReactionRepository.findByArtwork(artwork);
    }
}
