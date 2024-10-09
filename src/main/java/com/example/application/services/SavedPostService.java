package com.example.application.services;

import com.example.application.data.SavedPost;
import com.example.application.data.Artwork;
import com.example.application.repository.SavedPostRepository;
import com.example.application.data.User;
import com.example.application.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SavedPostService {

    private final SavedPostRepository savedPostRepository;

    public SavedPostService(SavedPostRepository savedPostRepository){
        this.savedPostRepository = savedPostRepository;
    }

    public List<SavedPost> getSavedPostsByUserId(Long userId){
    	return savedPostRepository.findByUserId(userId);
    }

    public void savePost(Artwork artwork, User user, String dateTime){
    	if(artwork != null && user != null){
    	   SavedPost savedPost = new SavedPost();
    	   savedPost.setArtwork(artwork);
    	   savedPost.setUser(user);
    	   savedPost.setDateTime(dateTime);

    	   savedPostRepository.save(savedPost);
    	}
    }

    public SavedPost getSavedPostByArtworkAndUserId(Long artworkId, Long userId){
    	return savedPostRepository.findByArtworkIdAndUserId(artworkId, userId);
    }

    public void deleteSavedPost(Long artworkId, Long userId){
    	SavedPost savedPost = savedPostRepository.findByArtworkIdAndUserId(artworkId, userId);

    	if(savedPost != null){
    	   savedPostRepository.delete(savedPost);
    	}
    }
}
