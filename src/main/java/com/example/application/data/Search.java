package com.example.application.data;

import jakarta.persistence.*;

@Entity
public class Search {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "searcher_id")
    private User searcher;

    @ManyToOne
    @JoinColumn(name = "searched_user_id")
    private User searchedUser;

    public Search(){}

    public Search(User searcher, User searchedUser){
    	this.searcher = searcher;
    	this.searchedUser = searchedUser;
    }

    public Long getId(){
    	return id;
    }

    public User getSearcher(){
    	return searcher;
    }

    public void setSearcher(User searcher){
    	this.searcher = searcher;
    }

    public User getSearchedUser(){
        return searchedUser;
    }

    public void setSearchedUser(User searchedUser){
        this.searchedUser = searchedUser;
    }
}
