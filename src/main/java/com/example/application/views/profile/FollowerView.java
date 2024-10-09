package com.example.application.views.profile;

import com.example.application.data.Follower;
import com.example.application.services.FollowerService;
import com.example.application.data.User;
import com.example.application.services.UserServices;
import com.example.application.data.Status;
import com.example.application.services.StatusService;
import com.example.application.views.MainFeed;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.dialog.Dialog;
import jakarta.annotation.security.PermitAll;

import java.io.FileInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import java.util.List;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@PermitAll
@Route("follower-view")
public class FollowerView extends AppLayout {

     private final FollowerService followerService;
     private final UserServices userService;
     private final StatusService statusService;

     private final FormLayout searchLayout = new FormLayout();
     private final TextField searchField = new TextField();
     private final Grid<Follower> userGrid = new Grid<>(Follower.class, false);
     private final Grid<Follower> followingGrid = new Grid<>(Follower.class, false);
     private Span totalFollowers = new Span();
     private Span totalFollowing = new Span();
     private final VerticalLayout followerLayout = new VerticalLayout();

     public FollowerView(FollowerService followerService, UserServices userService, StatusService statusService){
        this.followerService = followerService;
        this.userService = userService;
        this.statusService = statusService;

        addClassName("follower-app-layout");
        followerLayout.addClassName("follower-main-layout");
	totalFollowers.addClassName("view-total-followers");
	totalFollowing.addClassName("view-total-following");

	User user = userService.findCurrentUser();

	displayAllFollowers(user);
        createHeader(user);
    }

    private void displayAllFollowers(User user) {
        List<Follower> followers = followerService.getFollowersByFollowedUserId(user.getId());
	List<Follower> followings = followerService.getFollowedUsersByFollowerId(user.getId());

	if(followers.size() == 0){
	   totalFollowers.setText("No follower");
	}else if(followers.size() == 1){
	   totalFollowers.setText(followers.size() + " follower");
	}else{
	   totalFollowers.setText(followers.size() + " followers");
	}

	if(followings.size() == 0){
	   totalFollowing.setText("No following");
	}else{
           totalFollowing.setText(followings.size() + " following");
        }

        searchField.setPlaceholder("Search followers");
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.addClassName("view-search-field");
        searchField.setClearButtonVisible(true);
        searchField.setValueChangeMode(ValueChangeMode.EAGER);
        searchField.addValueChangeListener(event -> updateList(user));

        Collections.sort(followers, Comparator.comparing(follower -> follower.getFollower().getFirstName()));

        searchLayout.add(createFollowerLayout(followers, user));
        searchLayout.addClassName("view-search-layout");

        setContent(searchLayout);
    }

    private VerticalLayout createFollowerLayout(List<Follower> followers, User user){
    	followers.forEach(follower -> {
    	User userFollower = follower.getFollower();
	User currentUser = userService.findCurrentUser();

	String imageUrl = userFollower.getProfileImage();

        Avatar avatar = new Avatar();
        avatar.setImage(imageUrl);
        avatar.addClassName("view-avatar");

	String followerFullName = userFollower.getFullName();

	if (followerFullName.length() > 21) {
             followerFullName = followerFullName.substring(0, 21) + "…";;
	}

	Span fullName = new Span(followerFullName);
	fullName.addClassName("follower-full-name");

	Div onlineDiv = new Div();

	Status status = statusService.getStatusByUserId(follower.getFollower().getId());

	if(status.getStatus().equalsIgnoreCase("ONLINE")){
           onlineDiv.addClassName("follower-online");
	}else{
           onlineDiv.addClassName("follower-offline");
	}

	Follower currentFollower = followerService.getFollowerByFollowedUserIdAndFollowerId(userFollower.getId(), currentUser.getId());

	AtomicBoolean userAlreadyFollowed = new AtomicBoolean(currentFollower != null);

	Button followButton = new Button();

	if(!currentUser.getId().equals(userFollower.getId())){
           updateFollowButton(followButton, userAlreadyFollowed.get());

	   followButton.addClickListener(event -> {
	      if(!userAlreadyFollowed.get()) {
		 Follower newFollower = new Follower();
                 newFollower.setFollowedUser(userFollower);
                 newFollower.setFollower(currentUser);
                 newFollower.setFollowed(true);
                 followerService.saveFollower(newFollower);
                 updateFollowButton(followButton, true);
                 userAlreadyFollowed.set(true);
              } else {
                 followerService.deleteFollowerByFollowedUserId(userFollower.getId(), currentUser.getId());
                 updateFollowButton(followButton, false);
                 userAlreadyFollowed.set(false);
              }

              List<Follower> following = followerService.getFollowedUsersByFollowerId(user.getId());

              totalFollowing.setText(following.size() + " Following");
	   });
	}else{
           followButton.addClassName("view-exists-button");
	}

	HorizontalLayout childLayout = new HorizontalLayout(onlineDiv, avatar, fullName, followButton);
	childLayout.addClassName("follower-child-layout");

	followerLayout.add(childLayout);
	});

	return followerLayout;
    }

    // For Follower Grid
    private void updateList(User user) {
        followerLayout.removeAll();
        String searchTerm = searchField.getValue();

	List<Follower> followers = null;
        if(!searchTerm.isEmpty() && searchTerm != null){
           followers = followerService.findAllFollowers(user.getId(), searchTerm);
        }else{
           followers = followerService.getFollowersByFollowedUserId(user.getId());
        }
        createFollowerLayout(followers, user);
    }

    private void updateFollowButton(Button followButton, boolean isFollowed) {
        if (isFollowed) {
            followButton.setIcon(new Icon(VaadinIcon.USER_CHECK));
            followButton.setText("Unfollow");
            followButton.removeClassName("view-follow-button");
            followButton.addClassName("view-unfollow-button");
        } else {
            followButton.setIcon(new Icon(VaadinIcon.PLUS));
            followButton.setText("Follow");
            followButton.removeClassName("view-unfollow-button");
            followButton.addClassName("view-follow-button");
        }
    }

    // For the header of the layout
    private void createHeader(User user){
        Icon backButton = new Icon("lumo", "arrow-left");
        backButton.addClassName("follower-back-button");
        backButton.addClickListener(event -> {
             UI.getCurrent().navigate(MainFeed.class);
        });

        Span text = new Span("Your Followers");
	text.addClassName("view-fullname");

	Icon moreIcon = new Icon(VaadinIcon.ELLIPSIS_DOTS_H);
	moreIcon.addClassName("follower-more-icon");
	moreIcon.addClickListener(event -> {
	   Dialog dialog = new Dialog();
           dialog.addClassName("follower-more-dialog");

           Span actionHeader = new Span("Choose an action");
           actionHeader.addClassName("follower-action-header");

	   dialog.add(actionHeader);
           createActions(user, dialog);
        });

	HorizontalLayout headerLayout = new HorizontalLayout(backButton, text, moreIcon);
	headerLayout.addClassName("follower-header-layout");

	HorizontalLayout middleDiv = new HorizontalLayout(totalFollowers, totalFollowing);
        middleDiv.addClassName("view-middle-div");

	VerticalLayout layout = new VerticalLayout(headerLayout, searchField, middleDiv);
        layout.addClassName("follower-middle-layout");

        addToNavbar(layout);
    }

    // For actions that user can choose
    private void createActions(User user, Dialog dialog){
        HorizontalLayout deleteButton = createDeleteLayout();
        deleteButton.addClassName("follower-delete-button");
        deleteButton.addClickListener(event -> {
            ConfirmDialog deleteDialog = new ConfirmDialog();
            deleteDialog.addClassName("delete-follower-dialog");
            deleteDialog.setHeader("Delete a follower");
            deleteDialog.setConfirmText("Close");
            deleteDialog.addConfirmListener(e -> {
            	deleteDialog.close();
            });

            createDeleteFollower(deleteDialog, user);
        });

        HorizontalLayout viewButton = createViewLayout();
	viewButton.addClassName("follower-view-button");
	viewButton.addClickListener(event -> {
	    ConfirmDialog viewDialog = new ConfirmDialog();
            viewDialog.addClassName("delete-follower-dialog");
            viewDialog.setHeader("Following");
            viewDialog.setConfirmText("Close");
            viewDialog.addConfirmListener(e -> {
                viewDialog.close();
            });

	    createViewFollowing(viewDialog, user);
	});

        dialog.add(deleteButton, viewButton);
        dialog.open();
    }

    private HorizontalLayout createDeleteLayout(){
        Icon deleteIcon = new Icon("vaadin", "trash");
        deleteIcon.addClassName("follower-action-icon");

        Span deleteText = new Span("Delete follower");
        deleteText.addClassName("follower-action-text");

        return new HorizontalLayout(deleteIcon, deleteText);
    }

    private HorizontalLayout createViewLayout(){
        Icon viewIcon = new Icon("vaadin", "eye");
        viewIcon.addClassName("follower-action-icon");

        Span viewText = new Span("View following");
        viewText.addClassName("follower-action-text");

        return new HorizontalLayout(viewIcon, viewText);
    }

    // For viewing following
    private void createViewFollowing(ConfirmDialog viewDialog, User user){
        viewDialog.open();

    	List<Follower> followings = followerService.getFollowedUsersByFollowerId(user.getId());

    	followingGrid.setItems(followings);
        followingGrid.removeAllColumns();
        followingGrid.addClassName("all-users-grid");
        followingGrid.addComponentColumn(following -> {
            String imageUrl = following.getFollowedUser().getProfileImage();

            Avatar avatar = new Avatar();
            avatar.setImage(imageUrl);
            avatar.addClassName("view-avatar");

            Span fullName = new Span(following.getFollowedUser().getFullName());
            fullName.addClassName("view-firstname");

            HorizontalLayout  avatarDiv = new HorizontalLayout(avatar, fullName);
            avatarDiv.addClassName("view-avatar-div");

            return avatarDiv;
        }).setAutoWidth(false);

        viewDialog.add(followingGrid);
    }

    // For deleting follower
    private void createDeleteFollower(ConfirmDialog dialog, User user){
        dialog.open();
        List<Follower> followers = followerService.getFollowersByFollowedUserId(user.getId());

        userGrid.setItems(followers);
        userGrid.removeAllColumns();
        userGrid.addClassName("all-users-grid");

        TextField field = new TextField();
        field.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        field.setClearButtonVisible(true);
        field.setValueChangeMode(ValueChangeMode.EAGER);
        field.setPlaceholder("Find a follower to delete...");
        field.addValueChangeListener(event -> updateFollowerList(user.getId(), field.getValue()));
        dialog.add(field);

        userGrid.addComponentColumn(follower -> {
            String imageUrl = follower.getFollower().getProfileImage();

            Avatar avatar = new Avatar();
            avatar.setImage(imageUrl);
            avatar.addClassName("view-avatar");

            Span fullName = new Span(follower.getFollower().getFullName());
            fullName.addClassName("view-firstname");

            HorizontalLayout  avatarDiv = new HorizontalLayout(avatar, fullName);
            avatarDiv.addClassName("view-avatar-div");

            return avatarDiv;
        }).setAutoWidth(false);

        userGrid.addComponentColumn(follower -> {
            Icon deleteIcon = new Icon(VaadinIcon.TRASH);
            deleteIcon.addClassName("follower-delete-icon");

	    Span following = new Span("You are following " + follower.getFollower().getFirstName());
	    following.addClassName("follower-confirm-text");

	    Span notFollowing = new Span("Not following " + follower.getFollower().getFirstName());
	    notFollowing.addClassName("follower-confirm-text");

	    ConfirmDialog dialogg = new ConfirmDialog();
            dialogg.addClassName("follower-dialog");

	    Long currentFollower = follower.getFollowedUser().getId();
            Long currentFollowed = follower.getFollower().getId();

	    Follower singleFollower = followerService.getFollowerByFollowedUserIdAndFollowerId(currentFollowed, currentFollower);

	    if(singleFollower != null){
	    	dialogg.add(following);
	    }else{
		dialogg.add(notFollowing);
	    }

            dialogg.setCancelable(true);
            dialogg.setConfirmText("Delete");
            dialogg.setHeader("Are you sure you want to delete?");
            dialogg.addConfirmListener(event -> {
                Long userId = follower.getFollowedUser().getId();
                Long followerId = follower.getFollower().getId();

                followerService.deleteFollowerByFollowedUserId(userId, followerId);

                List<Follower> currentFollowers = followerService.getFollowersByFollowedUserId(userId);
		totalFollowers.setText(currentFollowers.size() + " followers");

        	userGrid.setItems(currentFollowers);

        	followerLayout.removeAll();
        	createFollowerLayout(currentFollowers, user);

            	dialogg.close();
            });

            deleteIcon.addClickListener(event -> {
            	dialogg.open();
            });

            return deleteIcon;
        });

        dialog.add(userGrid);
    }

    // For User Grid
    private void updateFollowerList(Long userId, String searchTerm) {
        List<Follower> followers = followerService.findAllFollowers(userId, searchTerm);

        userGrid.setItems(followers);
    }
}
