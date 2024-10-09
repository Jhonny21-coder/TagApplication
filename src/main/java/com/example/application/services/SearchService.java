package com.example.application.services;

import com.example.application.data.Search;
import com.example.application.repository.SearchRepository;
import com.example.application.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SearchService {

    private final SearchRepository searchRepository;
    private final UserRepository userRepository;

    public SearchService(SearchRepository searchRepository, UserRepository userRepository){
        this.searchRepository = searchRepository;
        this.userRepository = userRepository;
    }

    public void saveSearch(Search search) {
    	searchRepository.save(search);
    }

    public List<Search> getSearchesBySearcherId(Long searcherId){
    	return searchRepository.findBySearcherId(searcherId);
    }

    public void removeSearches(Search search){
    	searchRepository.delete(search);
    }

    public void deleteAllUserSearches(Long searcherId){
    	searchRepository.deleteBySearcherId(searcherId);
    }
}
