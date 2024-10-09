package com.example.application.views.search;

import com.example.application.data.Follower;
import com.example.application.services.FollowerService;
import com.example.application.data.Search;
import com.example.application.services.SearchService;
import com.example.application.data.User;
import com.example.application.services.UserServices;
import com.example.application.views.profile.UserProfile;
import com.example.application.views.MainFeed;

import com.vaadin.flow.router.Route;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.server.StreamResource;
import jakarta.annotation.security.PermitAll;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@PermitAll
@Route("search-history-view")
public class SearchHistoryView extends AppLayout {

    private final UserServices userService;
    private final SearchService searchService;
    private final FollowerService followerService;

    private VerticalLayout searchHistoryLayout = new VerticalLayout();

    public SearchHistoryView(UserServices userService, SearchService searchService,
    	FollowerService followerService){
    	this.userService = userService;
    	this.searchService = searchService;
    	this.followerService = followerService;

    	addClassName("profile-app-layout");
    	createHeader();
    	createMainLayout();
    }

    public void createMainLayout(){
    	searchHistoryLayout.addClassName("search-history-main-layout");
    	setContent(createSearchHistoryLayout());
    }

    private VerticalLayout createSearchHistoryLayout(){
    	searchHistoryLayout.removeAll();

    	User user = userService.findCurrentUser();

        List<Search> searches = searchService.getSearchesBySearcherId(user.getId());
        List<Follower> followers = followerService.getFollowedUsersByFollowerId(user.getId());

	for(int i = 0; i < searches.size(); i++){
	    Search search = searches.get(i);

	    HorizontalLayout layout = new HorizontalLayout();
	    layout.addClassName("search-history-layout-1");

	    String imageUrl = search.getSearchedUser().getProfileImage();

    	    Avatar avatar = new Avatar();
    	    avatar.setImage(imageUrl);
    	    avatar.addClassName("search-history-avatar");

    	    Div avatarDiv = new Div(avatar);
    	    avatarDiv.addClickListener(event -> {
                UI.getCurrent().navigate(UserProfile.class, search.getSearchedUser().getId());
            });

            String name = search.getSearchedUser().getFullName();

            if(name.length() > 27){
               name = name.substring(0, 27) + "…";
            }

            Span nameSpan = new Span(name);
            nameSpan.addClassName("search-history-name");
            nameSpan.addClickListener(event -> {
                UI.getCurrent().navigate(UserProfile.class, search.getSearchedUser().getId());
            });

            Icon removeIcon = new Icon("lumo", "cross");
            removeIcon.addClassName("search-history-cross-icon");
            removeIcon.addClickListener(event -> {
                 searchService.removeSearches(search);
                 createSearchHistoryLayout();
            });

            User followedUser = search.getSearchedUser();

            List<Follower> userFollowers = followerService.getFollowersByFollowedUserId(followedUser.getId());

            Span followersSpan = new Span(userFollowers.size() + " followers");
            followersSpan.addClassName("search-history-followers-text");
            followersSpan.addClickListener(event -> {
                UI.getCurrent().navigate(UserProfile.class, search.getSearchedUser().getId());
            });

	    Button followButton = new Button();

            VerticalLayout middleLayout = new VerticalLayout(nameSpan, followersSpan, followButton);
            middleLayout.addClassName("search-history-middle-layout");

            Follower currentFollower = followerService.getFollowerByFollowedUserIdAndFollowerId(search.getSearchedUser().getId(), user.getId());

            AtomicBoolean isFollowed = new AtomicBoolean(currentFollower != null);

	    followButton.addClickListener(event -> {
                handleFollowButtonListener(isFollowed, followButton, followedUser, user);
            });

            if(!isFollowed.get()){
                followButton.setText("Follow");
                followButton.setIcon(new Icon("lumo", "plus"));
                followButton.removeClassName("search-history-unfollow-button");
                followButton.addClassName("search-history-follow-button");
            }else{
                followButton.setText("Unfollow");
                followButton.setIcon(new Icon("lumo", "cross"));
                followButton.removeClassName("search-history-follow-button");
                followButton.addClassName("search-history-unfollow-button");
            }

            layout.add(avatarDiv, middleLayout, removeIcon);
            searchHistoryLayout.add(layout);
        }

        return searchHistoryLayout;
    }

    private void handleFollowButtonListener(AtomicBoolean isFollowed, Button followButton, User followedUser, User follower){
    	if(!isFollowed.get()){
           followButton.setText("Unfollow");
           followButton.setIcon(new Icon("lumo", "cross"));
           followButton.removeClassName("search-history-follow-button");
           followButton.addClassName("search-history-unfollow-button");

	   Follower newFollower = new Follower(followedUser, follower);
           followerService.saveFollower(newFollower);

           isFollowed.set(true);
	}else{
           followButton.setText("Follow");
           followButton.setIcon(new Icon("lumo", "plus"));
           followButton.removeClassName("search-history-unfollow-button");
           followButton.addClassName("search-history-follow-button");

           followerService.deleteFollowerByFollowedUserId(followedUser.getId(), follower.getId());

           isFollowed.set(false);
	}
    }

    private void createHeader(){
    	User searcher = userService.findCurrentUser();

    	Icon backIcon = new Icon("lumo", "arrow-left");
    	backIcon.addClassName("header-back-button");
    	backIcon.addClickListener(event -> {
    	    UI.getCurrent().navigate(SearchView.class);
    	});

	Span historyText = new Span("History");
	historyText.addClassName("search-history-text");

    	Span clearText = new Span("Clear all");
    	clearText.addClassName("search-history-clear-text");
    	clearText.addClickListener(event -> {
    	    searchService.deleteAllUserSearches(searcher.getId());
    	    createSearchHistoryLayout();
    	});

    	HorizontalLayout childHeader = new HorizontalLayout(historyText, clearText);
    	childHeader.addClassName("search-child-header");

    	HorizontalLayout parentHeader = new HorizontalLayout(backIcon, childHeader);
    	parentHeader.addClassName("search-parent-header");

    	addToNavbar(parentHeader);
    }
}
