package com.example.application.views.story;

import com.example.application.data.User;
import com.example.application.services.UserServices;
import com.example.application.data.story.Story;
import com.example.application.services.story.StoryService;
import com.example.application.data.story.StoryReaction;
import com.example.application.services.story.StoryReactionService;
import com.example.application.views.MainFeed;
import com.example.application.views.profile.UserProfile;
import com.example.application.views.profile.OwnProfile;

import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.server.VaadinSession;
import jakarta.annotation.security.PermitAll;

import java.io.FileInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.Duration;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

@PermitAll
@Route("display-story")
public class DisplayStoryView extends AppLayout implements HasUrlParameter<Long> {

    private final UserServices userService;
    private final StoryService storyService;
    private final StoryReactionService storyReactionService;
    private boolean isOpened = true;
    private int[] currentStoryIndex = {0};

    public DisplayStoryView(UserServices userService, StoryService storyService,
    	StoryReactionService storyReactionService){

    	this.userService = userService;
    	this.storyService = storyService;
    	this.storyReactionService = storyReactionService;

    	addClassName("story-display-layout");
    }

    @Override
    public void setParameter(BeforeEvent event, Long userId){
	displayStoryImage(userId);
    }

    private HorizontalLayout createHeader(Long userId, Span hoursAgo){
    	User user = userService.getUserById(userId);
    	User currentUser = userService.findCurrentUser();

    	HorizontalLayout headerLayout = new HorizontalLayout();
    	headerLayout.addClassName("story-view-header-layout");

    	String imageUrl = user.getProfileImage();

    	Avatar avatar = new Avatar();
    	avatar.setImage(imageUrl);
    	avatar.addClassName("story-display-avatar");

    	Div avatarDiv = new Div(avatar);
    	avatarDiv.addClickListener(event -> {
    	    if(currentUser.getId().equals(user.getId())){
    	        UI.getCurrent().navigate(OwnProfile.class);
    	    }else{
            	UI.getCurrent().navigate(UserProfile.class, user.getId());
            }
        });

        Span nameSpan = new Span(user.getFullName());
        nameSpan.addClassName("story-display-name");

        Icon closeIcon = new Icon("lumo", "cross");
        closeIcon.addClassName("story-display-close-icon");
        closeIcon.addClickListener(event -> {
            Set<String> sessionNames = VaadinSession.getCurrent().getSession().getAttributeNames();

            UI.getCurrent().navigate(MainFeed.class);
        });

        VerticalLayout layout = new VerticalLayout(nameSpan, hoursAgo);
        layout.addClassName("story-display-layout2");
        layout.addClickListener(event -> {
            if(currentUser.getId().equals(user.getId())){
                UI.getCurrent().navigate(OwnProfile.class);
            }else{
                UI.getCurrent().navigate(UserProfile.class, user.getId());
            }
        });

        headerLayout.add(avatarDiv, layout, closeIcon);

        return headerLayout;
    }

    private void updateStoryReactions(List<StoryReaction> reactions, HorizontalLayout iconsLayout){
	for(StoryReaction reaction : reactions){
            if(reaction.getReactType().equals("LIKE")){
               Icon icon = new Icon(VaadinIcon.THUMBS_UP);
               icon.addClassName("story-reacted-like");
               iconsLayout.add(icon);
	    }else if(reaction.getReactType().equals("HEART")){
               Icon icon = new Icon(VaadinIcon.HEART);
               icon.addClassName("story-reacted-heart");
               iconsLayout.add(icon);
	    }else if(reaction.getReactType().equals("HAPPY")){
               Icon icon = new Icon(VaadinIcon.SMILEY_O);
               icon.addClassName("story-reacted-happy");
               iconsLayout.add(icon);
            }
        }

        iconsLayout.setVisible(true);
    }

    private HorizontalLayout createFooter(List<Story> stories, HorizontalLayout iconsLayout, List<Integer> storySizes) {
    	Story story = stories.get(currentStoryIndex[0]);
        User reactor = userService.findCurrentUser();

        List<StoryReaction> reactions = storyReactionService.getStoryReactionByReactorAndStoryId(reactor.getId(), story.getId());

        if(!reactions.isEmpty()){
           iconsLayout.removeAll();
	   updateStoryReactions(reactions, iconsLayout);
	}

	Icon likeIcon = new Icon(VaadinIcon.THUMBS_UP);
    	likeIcon.addClassName("story-like-icon");
    	likeIcon.addClickListener(event -> {
    	    Story story2 = stories.get(currentStoryIndex[0]);

    	    List<StoryReaction> reactions3 = storyReactionService.getStoryReactionByReactorAndStoryId(reactor.getId(), story2.getId());

    	    if(reactions3.size() < 9){
               StoryReaction reaction = new StoryReaction(reactor, story2, "LIKE");
               storyReactionService.saveStoryReaction(reaction);

               List<StoryReaction> reactions2 = storyReactionService.getStoryReactionByReactorAndStoryId(reactor.getId(), story2.getId());

               if(!reactions2.isEmpty()){
                  iconsLayout.removeAll();
                  updateStoryReactions(reactions2, iconsLayout);
               }else{
               	  Icon icon = new Icon(VaadinIcon.THUMBS_UP);
                  icon.addClassName("story-reacted-like");
	          iconsLayout.add(icon);
	          iconsLayout.setVisible(true);
	       }
	    }else{
	       System.out.println("Size 10 reached to story id: " + story2.getId());
	    }
	});

	Icon heartIcon = new Icon(VaadinIcon.HEART);
    	heartIcon.addClassName("story-heart-icon");
    	heartIcon.addClickListener(event -> {
    	    Story story2 = stories.get(currentStoryIndex[0]);

    	    List<StoryReaction> reactions3 = storyReactionService.getStoryReactionByReactorAndStoryId(reactor.getId(), story2.getId());

    	    if(reactions3.size() < 9){
               StoryReaction reaction = new StoryReaction(reactor, story2, "HEART");
               storyReactionService.saveStoryReaction(reaction);

               List<StoryReaction> reactions2 = storyReactionService.getStoryReactionByReactorAndStoryId(reactor.getId(), story2.getId());

	       if(!reactions2.isEmpty()){
	          iconsLayout.removeAll();
	          updateStoryReactions(reactions2, iconsLayout);
               }else{
                  Icon icon = new Icon(VaadinIcon.HEART);
                  icon.addClassName("story-reacted-heart");
                  iconsLayout.add(icon);
                  iconsLayout.setVisible(true);
               }
            }else{
               System.out.println("Size 10 reached to story id: " + story2.getId());
            }
	});

	Icon happyIcon = new Icon(VaadinIcon.SMILEY_O);
    	happyIcon.addClassName("story-happy-icon");
    	happyIcon.addClickListener(event -> {
    	    Story story2 = stories.get(currentStoryIndex[0]);

    	    List<StoryReaction> reactions3 = storyReactionService.getStoryReactionByReactorAndStoryId(reactor.getId(), story2.getId());

    	    if(reactions3.size() < 9){
               StoryReaction reaction = new StoryReaction(reactor, story2, "HAPPY");
               storyReactionService.saveStoryReaction(reaction);

               List<StoryReaction> reactions2 = storyReactionService.getStoryReactionByReactorAndStoryId(reactor.getId(), story2.getId());

	       if(!reactions2.isEmpty()){
	          iconsLayout.removeAll();
	          updateStoryReactions(reactions2, iconsLayout);
               }else{
                  Icon icon = new Icon(VaadinIcon.SMILEY_O);
                  icon.addClassName("story-reacted-happy");
                  iconsLayout.add(icon);
                  iconsLayout.setVisible(true);
               }
            }else{
               System.out.println("Size 10 reached to story id: " + story2.getId());
            }
	});

    	HorizontalLayout footerLayout = new HorizontalLayout(likeIcon, heartIcon, happyIcon);
    	footerLayout.addClassName("story-footer-layout");

    	return footerLayout;
    }

    private void displayStoryImage(Long userId){
    	List<Story> userStories = storyService.getNonExpiredStoriesByUser(userId);

    	FormLayout formLayout = new FormLayout();

    	HorizontalLayout iconsLayout = new HorizontalLayout();
    	iconsLayout.addClassName("story-icons-layout");

	Span hoursAgo = new Span();
        hoursAgo.addClassName("story-hours-ago");

        Button eyeIcon = new Button();

    	HorizontalLayout headerLayout = createHeader(userId, hoursAgo);
    	formLayout.add(headerLayout);

    	if(!userStories.isEmpty()) {
           HorizontalLayout progressLayout = new HorizontalLayout();
           progressLayout.addClassName("story-progress-layout");
           progressLayout.setWidthFull();

           // Create a progress bar for each story and apply default "inactive" class
           List<ProgressBar> progressBars = new ArrayList<>();
           List<Integer> storySizes = new ArrayList<>();
           for (int i = 0; i < userStories.size(); i++) {
            	ProgressBar progressBar = new ProgressBar(); // Initially at 0
            	progressBar.setWidth("100%");
            	progressBar.addClassName("story-progress-inactive"); // Default color
            	progressBars.add(progressBar);
            	progressLayout.add(progressBar);

            	storySizes.add(i);
           }

           updateTimeAgo(userStories.get(currentStoryIndex[0]), hoursAgo);

           // Set the first story progress bar to active class since it's the first story being viewed
           ProgressBar bar = progressBars.get(0);
           bar.setValue(1.0);
           bar.addClassName("story-progress-active");

           // Add progress layout to the form
           formLayout.add(progressLayout);

           // Story image
           Image image = new Image();
           image.addClassName("story-display-image");
           loadStoryImage(userStories.get(currentStoryIndex[0]), image, iconsLayout);

           Span storyContent = new Span();
           storyContent.addClassName("story-content");
           storyContent.setText(userStories.get(currentStoryIndex[0]).getContent());

           // Story layout
           HorizontalLayout storyLayout = new HorizontalLayout();
           storyLayout.addClassName("story-image-layout");
           storyLayout.setWidthFull();

           Icon rightIcon = new Icon(VaadinIcon.ANGLE_RIGHT);
           rightIcon.addClassName("story-angle-right-icon");

           if(userStories.size() == 1){
             rightIcon.setVisible(false);
           }

           // Left arrow (Previous story)
           Icon leftIcon = new Icon(VaadinIcon.ANGLE_LEFT);
           leftIcon.addClassName("story-angle-left-icon");
           leftIcon.setVisible(false); // Initially disabled because we start at the first story
           leftIcon.addClickListener(event -> {
            	if (currentStoryIndex[0] > 0) {
                   currentStoryIndex[0]--;
                   loadStoryImage(userStories.get(currentStoryIndex[0]), image, iconsLayout);
                   updateTimeAgo(userStories.get(currentStoryIndex[0]), hoursAgo); // Update hours ago
                   updateStoryContent(currentStoryIndex[0], userStories, storyContent);
                   updateProgressBars(currentStoryIndex[0], progressBars);
                   updateNavigationButtons(currentStoryIndex[0], userStories.size(), leftIcon, rightIcon);
            	}
           });

           rightIcon.addClickListener(event -> {
            	if (currentStoryIndex[0] < userStories.size() - 1) {
		   currentStoryIndex[0]++;
                   loadStoryImage(userStories.get(currentStoryIndex[0]), image, iconsLayout);
                   updateTimeAgo(userStories.get(currentStoryIndex[0]), hoursAgo); // Update hours ago
                   updateStoryContent(currentStoryIndex[0], userStories, storyContent);
                   updateProgressBars(currentStoryIndex[0], progressBars);
                   updateNavigationButtons(currentStoryIndex[0], userStories.size(), leftIcon, rightIcon);
            	}
           });

           eyeIcon.setIcon(new Icon(VaadinIcon.EYE));
           eyeIcon.addClassName("story-display-eye-icon");
           eyeIcon.addClickListener(event -> {
                if(!isOpened){
                   iconsLayout.setVisible(true);
                   storyContent.setVisible(true);

                   isOpened = true;
                   eyeIcon.setIcon(new Icon(VaadinIcon.EYE));
                }else{
                   iconsLayout.setVisible(false);
                   storyContent.setVisible(false);

                   isOpened = false;
                   eyeIcon.setIcon(new Icon(VaadinIcon.EYE_SLASH));
                }
           });

           VerticalLayout textLayout = new VerticalLayout(eyeIcon, storyContent);
           textLayout.addClassName("story-display-text-layout");

           HorizontalLayout footerLayout = createFooter(userStories, iconsLayout, storySizes);
           footerLayout.addClassName("story-footer-layout");

           // Add components to the story layout
           Div imageDiv = new Div(image);

           storyLayout.add(leftIcon, image, rightIcon, textLayout, footerLayout, iconsLayout);
           storyLayout.expand(image); // Let the image take most of the space
           formLayout.add(storyLayout);
    	}

    	setContent(formLayout);
    }

    private void updateStoryContent(int currentIndex, List<Story> stories, Span storyContent) {
    	// Check if the current index is within valid range
    	if (currentIndex >= 0 && currentIndex < stories.size()) {
           // Get the current story
           Story currentStory = stories.get(currentIndex);

           // Display the content of the current story
           storyContent.setVisible(true);
           storyContent.setText(currentStory.getContent());
    	} else {
           // If the index is out of bounds, hide the content
           storyContent.setVisible(false);
    	}
    }

    // Helper method to load a story's image
    private void loadStoryImage(Story story, Image image, HorizontalLayout iconsLayout) {
        iconsLayout.removeAll();

        User reactor = userService.findCurrentUser();

        List<StoryReaction> reactions = storyReactionService.getStoryReactionByReactorAndStoryId(reactor.getId(), story.getId());

        if(!reactions.isEmpty()){
           updateStoryReactions(reactions, iconsLayout);
        }else{
           iconsLayout.setVisible(false);
        }

        String top = String.valueOf(story.getTop());
        String left = String.valueOf(story.getLeft());
        String width = String.valueOf(story.getWidth());
        String height = String.valueOf(story.getHeight());

        String imageUrl = story.getImageUrl();
        image.getStyle().set("position", "absolute");
        image.getStyle().set("top", top + "px");
        image.getStyle().set("left", left + "px");
        image.getStyle().set("width", width + "px");
        image.getStyle().set("height", height + "px");
        image.setSrc(imageUrl);
        image.addClassName("swipe-animation");
        image.getElement().executeJs("setTimeout(() => this.classList.remove('swipe-animation'), 300);");
    }

    // Helper method to update the state of the navigation buttons
    private void updateNavigationButtons(int currentIndex, int totalStories, Icon leftIcon, Icon rightIcon) {
    	leftIcon.setVisible(currentIndex > 0); // Enable if not at the first story
    	rightIcon.setVisible(currentIndex < totalStories - 1); // Enable if not at the last story
    }

    // Helper method to update progress bars based on the current story
    private void updateProgressBars(int currentIndex, List<ProgressBar> progressBars) {
    	for (int i = 0; i < progressBars.size(); i++) {
            ProgressBar progressBar = progressBars.get(i);

            // Remove all classes to reset previous state
            progressBar.removeClassName("story-progress-active");
            progressBar.removeClassName("story-progress-inactive");

            // Set classes based on the story index
            if (i < currentIndex) {
            	// Already viewed stories
            	progressBar.setValue(1.0); // Full
            	progressBar.addClassName("story-progress-active"); // Active color
            } else if (i == currentIndex) {
            	// Currently viewed story
            	progressBar.setValue(1.0); // Full
            	progressBar.addClassName("story-progress-active"); // Active color
            } else {
            	// Upcoming stories
            	progressBar.setValue(0.0); // Empty
            	progressBar.addClassName("story-progress-inactive"); // Inactive color (gray)
            }
    	}
    }

    private void updateTimeAgo(Story story, Span timeAgo) {
    	LocalDateTime creationTime = story.getCreationTime();
    	LocalDateTime currentTime = LocalDateTime.now(ZoneId.of("Asia/Manila"));

    	Duration duration = Duration.between(creationTime, currentTime);

    	long seconds = duration.getSeconds();
    	long minutes = duration.toMinutes();
    	long hours = duration.toHours();
    	long days = duration.toDays();

    	if (seconds < 60) {
           // Less than 1 minute ago, show seconds with 's'
           timeAgo.setText(seconds + "s");
    	} else if (minutes < 60) {
           // Less than 1 hour ago, show minutes with 'm'
           timeAgo.setText(minutes + "m");
    	} else if (hours < 24) {
           // Less than 1 day ago, show hours with 'h'
           timeAgo.setText(hours + "h");
    	} else if (days < 7) {
           // Less than 1 week ago, show days with 'd'
           timeAgo.setText(days + "d");
    	} else {
           // More than 7 days ago, display exact date (e.g., "Sep 7")
           DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d");
           String formattedDate = creationTime.format(formatter);
           timeAgo.setText(formattedDate);
        }
    }
}
