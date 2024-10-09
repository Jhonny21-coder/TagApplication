package com.example.application.views;

import com.example.application.data.notification.Notification;
import com.example.application.services.notification.NotificationService;
import com.example.application.data.LikeReaction;
import com.example.application.services.LikeReactionService;
import com.example.application.data.HeartReaction;
import com.example.application.services.HeartReactionService;
import com.example.application.data.PostReaction;
import com.example.application.services.PostReactionService;
import com.example.application.data.User;
import com.example.application.services.UserServices;
import com.example.application.data.Artwork;
import com.example.application.services.ArtworkService;
import com.example.application.data.Comment;
import com.example.application.services.CommentService;
import com.example.application.data.story.Story;
import com.example.application.services.story.StoryService;
import com.example.application.views.profile.UserProfile;
import com.example.application.views.comment.CommentSectionView;
import com.example.application.views.story.StoryView;
import com.example.application.views.story.DisplayStoryView;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.IFrame;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;

import jakarta.annotation.security.PermitAll;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import java.io.FileInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import java.time.LocalTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;
import java.time.Duration;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import java.util.Locale;
import java.util.List;
import java.util.Date;
import java.util.Collections;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Route(value = "", layout=MainLayout.class)
@PermitAll
public class MainFeed extends AppLayout {
    private final ArtworkService artworkService;
    private final LikeReactionService likeService;
    private final HeartReactionService heartService;
    private final CommentService commentService;
    private final UserServices userService;
    private final PostReactionService postService;
    private final StoryService storyService;
    private final NotificationService notificationService;

    @Autowired
    private AuthenticationManager authenticationManager;

    public MainFeed(ArtworkService artworkService, LikeReactionService likeService,
    	HeartReactionService heartService, CommentService commentService, UserServices userService,
    	PostReactionService postService, StoryService storyService, NotificationService notificationService){

    	this.artworkService = artworkService;
    	this.likeService = likeService;
    	this.heartService = heartService;
    	this.commentService = commentService;
	this.userService = userService;
	this.postService = postService;
	this.storyService = storyService;
	this.notificationService = notificationService;

    	addClassName("main-feed");
    	scrollIntoView();
        createFeed();
    }

    // Start story

    private Div createOwnStory(User user){
    	List<Story> stories = storyService.getNonExpiredStoriesByUser(user.getId());

    	Div storyDiv = new Div();
	storyDiv.addClassName("story-own-feed");

	if(!stories.isEmpty()){
	   Story story = stories.get(0);

	   storyDiv.addClickListener(event -> {
            	UI.getCurrent().navigate(DisplayStoryView.class, story.getUser().getId());
           });

           String imageUrl = story.getImageUrl();

	   Image userImage = new Image(imageUrl, "story-image");
	   userImage.addClassName("story-own-user-image");

	   Span name = new Span("Your story");
	   name.addClassName("story-own-name");

	   Span numbers = new Span(String.valueOf(stories.size()));
	   numbers.addClassName("story-own-numbers");

	   storyDiv.add(userImage, numbers, name);
	}else{
	   storyDiv.setVisible(false);
	}

	return storyDiv;
    }

    private HorizontalLayout createStoryLayout(User user){
    	HorizontalLayout storyLayout = new HorizontalLayout();

    	//storyService.updateExpiredStories();

	String imageUrl = user.getProfileImage();

    	Image profileImage = new Image(imageUrl, "profile-image");
    	profileImage.addClassName("happening-profile-image");

        Icon addIcon = new Icon(VaadinIcon.PLUS);
        addIcon.addClassName("happening-add-icon");

        Span createText = new Span("Create story");
        createText.addClassName("happening-create-text");

        Div ownStoryLayout = createOwnStory(user);

        VerticalLayout profileLayout = new VerticalLayout(profileImage, addIcon, createText);
        profileLayout.addClassName("happening-profile-layout");
        profileLayout.addClickListener(event -> {
             UI.getCurrent().navigate(StoryView.class);
        });

        storyLayout.add(profileLayout, ownStoryLayout);

        List<User> users = userService.getAllUsers();
        users.forEach(_user -> {
            List<Story> userStories = storyService.getNonExpiredStoriesByUser(_user.getId());

            Div storyDiv = new Div();
            storyDiv.addClassName("happenings-feed");

            if(!userStories.isEmpty() && user.getFullName().equals(userStories.get(0).getUser().getFullName())){
               storyDiv.setVisible(false);
            }

            if(!userStories.isEmpty()){
               Story story = userStories.get(0);

               storyDiv.addClickListener(event -> {
                   UI.getCurrent().navigate(DisplayStoryView.class, story.getUser().getId());
               });

               String _imageUrl = story.getImageUrl();

               Image userImage = new Image(_imageUrl, "user-image");
               userImage.addClassName("happening-user-image");

               String userFullname = story.getUser().getFullName();

               String parts[] = userFullname.split(" ");

               Span name = new Span(parts[0] + " " + parts[parts.length - 1]);
               name.addClassName("happening-name");

               Span numbers = new Span(String.valueOf(userStories.size()));
               numbers.addClassName("happening-numbers");

               storyDiv.add(userImage, numbers, name);
               storyLayout.add(storyDiv);
            }else{
               storyDiv.setVisible(false);
            }
        });

    	return storyLayout;
    }

    // End story
    private Avatar createAvatar(User user){
    	String imageUrl = user.getProfileImage();

    	Avatar avatar = new Avatar();
    	avatar.setImage(imageUrl);
	avatar.addClassName("view-avatar");

	return avatar;
    }

    private Span createNameLayout(PostReaction reaction){
	String fullname = reaction.getReactor().getFullName();

	int length = 31;

	if(fullname.length() > length){
           fullname = fullname.substring(0, length) + "…";
	}

	Span name = new Span(fullname);
	name.addClassName("main-reactor-name");

	return name;
    }

    private VerticalLayout createAllReactedLayout(List<PostReaction> reactions, ConfirmDialog dialog) {
	VerticalLayout allContent = new VerticalLayout();

	for (PostReaction reaction : reactions) {
	     User user = reaction.getReactor();

	     if (reaction.getReactType().equals("Like")) {
		Icon likeIcon = new Icon(VaadinIcon.THUMBS_UP);
		likeIcon.addClassName("main-like-icon");

		Avatar avatar = createAvatar(user);

		Span name = createNameLayout(reaction);

		HorizontalLayout layout = new HorizontalLayout(likeIcon, avatar, name);
		layout.addClassName("main-reactor-main-layout");
		layout.addClickListener(event -> {
		    dialog.close();
		    UI.getCurrent().navigate(UserProfile.class, user.getId());
		});

		allContent.add(layout);

	     } else if (reaction.getReactType().equals("Heart")) {
		Icon heartIcon = new Icon(VaadinIcon.HEART);
		heartIcon.addClassName("main-heart-icon");

		Avatar avatar = createAvatar(user);

		Span name = createNameLayout(reaction);

		HorizontalLayout layout = new HorizontalLayout(heartIcon, avatar, name);
		layout.addClassName("main-reactor-main-layout");
		layout.addClickListener(event -> {
		    dialog.close();
		    UI.getCurrent().navigate(UserProfile.class, user.getId());
		});

		allContent.add(layout);
	     } else if (reaction.getReactType().equals("Happy")) {
		Icon smileIcon = new Icon(VaadinIcon.SMILEY_O);
		smileIcon.addClassName("main-smile-icon");

		Avatar avatar = createAvatar(user);

		Span name = createNameLayout(reaction);

		HorizontalLayout layout = new HorizontalLayout(smileIcon, avatar, name);
		layout.addClassName("main-reactor-main-layout");
		layout.addClickListener(event -> {
		    dialog.close();
		    UI.getCurrent().navigate(UserProfile.class, user.getId());
		});

		allContent.add(layout);
	     }
	}

	return allContent;
    }

    public void createReactedLayout(List<PostReaction> reactions, ConfirmDialog dialog) {
	dialog.open();

	int totalLikes = 0;
	int totalHearts = 0;
	int totalSmiles = 0;
	int totalReactions = 0;

	VerticalLayout allContent = createAllReactedLayout(reactions, dialog);
	VerticalLayout likeContent = new VerticalLayout();
	VerticalLayout heartContent = new VerticalLayout();
	VerticalLayout smileContent = new VerticalLayout();

	for (PostReaction reaction : reactions) {
	     totalReactions++;

	     User user = reaction.getReactor();

	     if (reaction.getReactType().equals("Like")) {
		Icon likeIcon = new Icon(VaadinIcon.THUMBS_UP);
		likeIcon.addClassName("main-like-icon");

		Avatar avatar = createAvatar(user);

		Span name = createNameLayout(reaction);

		HorizontalLayout layout = new HorizontalLayout(likeIcon, avatar, name);
		layout.addClassName("main-reactor-main-layout");
		layout.addClickListener(event -> {
		    dialog.close();
		    UI.getCurrent().navigate(UserProfile.class, user.getId());
		});

		likeContent.add(layout);

		totalLikes++;
	     } else if (reaction.getReactType().equals("Heart")) {
		Icon heartIcon = new Icon(VaadinIcon.HEART);
		heartIcon.addClassName("main-heart-icon");

		Avatar avatar = createAvatar(user);

		Span name = createNameLayout(reaction);

		HorizontalLayout layout = new HorizontalLayout(heartIcon, avatar, name);
		layout.addClassName("main-reactor-main-layout");
		layout.addClickListener(event -> {
		    dialog.close();
		    UI.getCurrent().navigate(UserProfile.class, user.getId());
		});

		heartContent.add(layout);

		totalHearts++;
             } else if (reaction.getReactType().equals("Happy")) {
		Icon smileIcon = new Icon(VaadinIcon.SMILEY_O);
		smileIcon.addClassName("main-smile-icon");

		Avatar avatar = createAvatar(user);

		Span name = createNameLayout(reaction);

		HorizontalLayout layout = new HorizontalLayout(smileIcon, avatar, name);
		layout.addClassName("main-reactor-main-layout");
		layout.addClickListener(event -> {
		    dialog.close();
		    UI.getCurrent().navigate(UserProfile.class, user.getId());
		});

		smileContent.add(layout);

		totalSmiles++;
	    }
	}

	Icon likeIcon = new Icon(VaadinIcon.THUMBS_UP);
	likeIcon.addClassName("main-reactor-like-icon");

	Icon heartIcon = new Icon(VaadinIcon.HEART);
	heartIcon.addClassName("main-reactor-heart-icon");

	Icon smileIcon = new Icon(VaadinIcon.SMILEY_O);
	smileIcon.addClassName("main-reactor-smile-icon");

	Span allText = new Span("All");
	allText.addClassName("main-all-text");

	Tab allTab = new Tab(allText, new Span(formatValue(totalReactions)));
	Tab likeTab = new Tab(likeIcon, new Span(formatValue(totalLikes)));
	Tab heartTab = new Tab(heartIcon, new Span(formatValue(totalHearts)));
	Tab smileTab = new Tab(smileIcon, new Span(formatValue(totalSmiles)));

	if (totalHearts == 0) {
	    heartTab.setVisible(false);
	}

	if (totalLikes == 0) {
	    likeTab.setVisible(false);
	}

	if (totalSmiles == 0) {
	    smileTab.setVisible(false);
	}

	Tabs tabs = new Tabs(allTab, likeTab, heartTab, smileTab);
	tabs.addClassName("main-reactor-tabs");

	Span headerText = new Span("People who reacted");
	headerText.addClassName("main-reactor-header-text");

	VerticalLayout tabsLayout = new VerticalLayout(headerText, tabs);
	tabsLayout.addClassName("main-tabs-Layout");

	dialog.setHeader(tabsLayout);

	Div dialogContent = new Div(allContent, likeContent, heartContent, smileContent);
	dialogContent.addClassName("main-dialog-content");

	dialog.add(dialogContent);

	// Show content based on the selected tab
	tabs.addSelectedChangeListener(event -> {
	    allContent.setVisible(tabs.getSelectedTab().equals(allTab));
	    likeContent.setVisible(tabs.getSelectedTab().equals(likeTab));
	    heartContent.setVisible(tabs.getSelectedTab().equals(heartTab));
	    smileContent.setVisible(tabs.getSelectedTab().equals(smileTab));
	});

	// Initially show only the first tab's content
	allContent.setVisible(true);
	likeContent.setVisible(false);
	heartContent.setVisible(false);
	smileContent.setVisible(false);
    }

    public void createFeed(){
    	User user_ = userService.findCurrentUser();

    	List<Artwork> artworks = artworkService.getAllArtworks();
    	Collections.reverse(artworks);

	FormLayout formLayout = new FormLayout();
	formLayout.addClassName("feed-form-layout");

	HorizontalLayout storyLayout = createStoryLayout(user_);
        storyLayout.addClassName("main-happening-layout");

        // Create an IFrame component
        IFrame iframe = new IFrame("https://www.youtube.com/embed/dQw4w9WgXcQ");
        iframe.setWidth("560px");
        iframe.setHeight("315px");
        iframe.getElement().setAttribute("frameborder", "0");
        iframe.getElement().setAttribute("allowfullscreen", "true");

        formLayout.add(storyLayout, iframe);

    	for(Artwork artwork : artworks){
    	    String imageUrl = artwork.getArtworkUrl();

	    Image image = new Image(imageUrl, "artwork-image");
    	    image.addClassName("main-feed-image");

	    User user = artwork.getUser();
	    User currentUser = userService.findCurrentUser();

            Span commented = new Span();
            commented.addClassName("commented");

	    Span totalReactions = new Span();
	    totalReactions.addClassName("reacted");

	    Span likes = new Span();
            likes.addClassName("specific-reacts");

            Span hearts = new Span();
            hearts.addClassName("specific-reacts");

            Span smiles = new Span();
            smiles.addClassName("specific-reacts");

            Button likeButton = new Button();
            likeButton.addClassName("feed-reaction");
            likeButton.getStyle().set("color", "white");
            likeButton.setIcon(new Icon(VaadinIcon.THUMBS_UP_O));

            Icon likeIcon = new Icon(VaadinIcon.THUMBS_UP);
            likeIcon.addClassName("reactions-like");

            Icon heartIcon = new Icon(VaadinIcon.HEART);
            heartIcon.addClassName("reactions-heart");

            Icon happyIcon = new Icon(VaadinIcon.SMILEY_O);
            happyIcon.addClassName("reactions-happy");

	    showReactions(likeButton, artwork, totalReactions, likes, hearts, smiles, likeIcon, heartIcon, happyIcon);

	    Button commentButton = createCommentButtonListener(user, artwork);
	    Button shareButton = new Button("7262", new Icon(VaadinIcon.ARROW_FORWARD));
	    shareButton.addClassName("feed-heart");

	    HorizontalLayout buttonsLayout = new HorizontalLayout(likeButton, commentButton, shareButton);
            buttonsLayout.addClassName("main-feed-buttons");

	    VerticalLayout profileLayout = createFeedHeader(user, artwork);
	    profileLayout.addClassName("comment-user-header-layout");

	    List<PostReaction> reactions = postService.getPostReactionsByArtworkId(artwork.getId());

	    HorizontalLayout totalReactionsDiv = createTotalReactions(reactions, commented, artwork, totalReactions, likes, hearts, smiles, likeIcon, heartIcon, happyIcon);
	    totalReactionsDiv.addClassName("comment-reactions-div");
	    totalReactionsDiv.addClickListener(event -> {
                List<PostReaction> reactions2 = postService.getPostReactionsByArtworkId(artwork.getId());

                ConfirmDialog dialog = new ConfirmDialog();
                dialog.addClassName("main-reactor-dialog");
                dialog.setConfirmText("Close");

                createReactedLayout(reactions2, dialog);
            });

	    formLayout.add(profileLayout, image, totalReactionsDiv, buttonsLayout);
    	}

	setContent(formLayout);
    }

    /*---------------------*/
    public void showReactions(Button likeButton, Artwork artwork, Span totalReactions, Span likes, Span hearts,
    	Span smiles, Icon likeIcon, Icon heartIcon, Icon happyIcon){

        List<PostReaction> reactions = postService.getPostReactionsByArtworkId(artwork.getId());

        Dialog dialog = new Dialog();
        dialog.addClassName("comment-dialog");

        AtomicLong totalReacts = new AtomicLong(reactions.size());

        if(totalReacts.get() != 0){
           totalReactions.setText(formatValue(totalReacts.get()) + " reactions");
           likeButton.setText(formatValue(totalReacts.get()));
        }else if(totalReacts.get() == 0){
           likeButton.setIcon(new Icon(VaadinIcon.THUMBS_UP_O));
           likeButton.setText("");
           likeButton.getStyle().set("color", "white");
        }

        User currentUser = userService.findCurrentUser();

        PostReaction reactor = postService.getPostReactionByReactorAndArtworkId(currentUser.getId(), artwork.getId());

        AtomicBoolean isReacted = new AtomicBoolean(reactor != null);

        Icon alreadyLiked = new Icon(VaadinIcon.THUMBS_UP);
        alreadyLiked.addClassName("feed-listener-like");

        Icon alreadyHearted = new Icon(VaadinIcon.HEART);
        alreadyHearted.addClassName("feed-listener-heart");

        Icon alreadySmiled = new Icon(VaadinIcon.SMILEY_O);
        alreadySmiled.addClassName("feed-listener-happy");

        if(isReacted.get() && reactor.getReactType().equalsIgnoreCase("Like")){
            likeButton.setIcon(alreadyLiked);
        }else if(isReacted.get() && reactor.getReactType().equalsIgnoreCase("Heart")){
            likeButton.setIcon(alreadyHearted);
        }else if(isReacted.get() && reactor.getReactType().equalsIgnoreCase("Happy")){
            likeButton.setIcon(alreadySmiled);
        }

        Icon likeReactIcon = new Icon(VaadinIcon.THUMBS_UP);
        likeReactIcon.addClassName("like-react-icon");
        likeReactIcon.addClickListener(e -> {
            createButtonsListener(isReacted, "Like", totalReacts, likeButton, artwork, "primary", totalReactions, likes, hearts, smiles, likeIcon, heartIcon, happyIcon);
            dialog.close();
        });

        Icon heartReactIcon = new Icon(VaadinIcon.HEART);
        heartReactIcon.addClassName("heart-react-icon");
        heartReactIcon.addClickListener(e -> {
            createButtonsListener(isReacted, "Heart", totalReacts, likeButton, artwork, "error", totalReactions, likes, hearts, smiles, likeIcon, heartIcon, happyIcon);
            dialog.close();
        });

        Icon happyReactIcon = new Icon(VaadinIcon.SMILEY_O);
        happyReactIcon.addClassName("happy-react-icon");
        happyReactIcon.addClickListener(e -> {
            createButtonsListener(isReacted, "Happy", totalReacts, likeButton, artwork, "warning", totalReactions, likes, hearts, smiles, likeIcon, heartIcon, happyIcon);
            dialog.close();
        });

        dialog.add(
            new VerticalLayout(likeReactIcon, new Span("Like")),
            new VerticalLayout(heartReactIcon, new Span("Heart")),
            new VerticalLayout(happyReactIcon, new Span("Happy"))
        );

        likeButton.addClickListener(event -> {
             dialog.open();
        });
    }

    public void createButtonsListener(AtomicBoolean isReacted, String reactType, AtomicLong totalReacts, Button button,
        Artwork artwork, String colorTheme, Span totalReactions, Span likes, Span hearts, Span smiles, Icon likeIcon, Icon heartIcon, Icon happyIcon){

        User currentUser = userService.findCurrentUser();

        Icon likeIcon2 = new Icon(VaadinIcon.THUMBS_UP);
        likeIcon2.addClassName("feed-listener-like");

        Icon heartIcon2 = new Icon(VaadinIcon.HEART);
        heartIcon2.addClassName("feed-listener-heart");

        Icon happyIcon2 = new Icon(VaadinIcon.SMILEY_O);
        happyIcon2.addClassName("feed-listener-happy");

        if(!isReacted.get()){
           postService.savePostReaction(artwork, currentUser, reactType);

           notificationService.createReactNotification(currentUser, artwork);

           totalReacts.incrementAndGet();

           button.setText(String.valueOf(totalReacts.get()));
           totalReactions.setText(formatValue(totalReacts.get()) + " reactions");

	   if(colorTheme.equals("primary")){
              button.setIcon(likeIcon2);
           }else if(colorTheme.equals("error")){
              button.setIcon(heartIcon2);
           }else if(colorTheme.equals("warning")){
              button.setIcon(happyIcon2);
           }

           isReacted.set(true);
        }else{
           Long reactorId = currentUser.getId();
           Long artworkId = artwork.getId();

           PostReaction reactor = postService.getPostReactionByReactorAndArtworkId(reactorId, artworkId);

           if(reactor.getReactType().equalsIgnoreCase(reactType)){
              postService.removePostReaction(reactorId, artworkId);

              totalReacts.decrementAndGet();

              if(totalReacts.get() == 0){
                 button.setText("");
                 totalReactions.setText("");
              }else{
                 button.setText(String.valueOf(totalReacts.get()));
                 totalReactions.setText(formatValue(totalReacts.get()) + " reactions");
              }

              button.setIcon(new Icon(VaadinIcon.THUMBS_UP_O));
              button.getStyle().set("color", "white");

              isReacted.set(false);
            }else{
              postService.updatePostReaction(reactor, reactType);

              if(colorTheme.equals("primary")){
              	button.setIcon(likeIcon2);
              }else if(colorTheme.equals("error")){
              	button.setIcon(heartIcon2);
              }else if(colorTheme.equals("warning")){
              	button.setIcon(happyIcon2);
              }

              isReacted.set(true);
            }
        }

        List<PostReaction> reactions = postService.getPostReactionsByArtworkId(artwork.getId());

        int totalLikes = 0;
        int totalHearts = 0;
        int totalSmiles = 0;

        for(PostReaction reaction : reactions){
            if(reaction.getReactType().equals("Like")){
               totalLikes++;
            }else if(reaction.getReactType().equals("Heart")){
               totalHearts++;
            }else if(reaction.getReactType().equals("Happy")){
               totalSmiles++;
            }
        }

        if(totalLikes != 0){
           likeIcon.setVisible(true);
           likes.setVisible(true);
           likes.setText(formatValue(totalLikes));
        }else{
           likeIcon.setVisible(false);
           likes.setVisible(false);
        }

        if(totalHearts != 0){
           heartIcon.setVisible(true);
           hearts.setVisible(true);
           hearts.setText(formatValue(totalHearts));
        }else{
           heartIcon.setVisible(false);
           hearts.setVisible(false);
        }

        if(totalSmiles != 0){
           happyIcon.setVisible(true);
           smiles.setVisible(true);
           smiles.setText(formatValue(totalSmiles));
        }else{
           happyIcon.setVisible(false);
           smiles.setVisible(false);
        }
    }

    /*------------------------*/

    public HorizontalLayout createTotalReactions(List<PostReaction> reactions, Span commented, Artwork artwork, Span totalReactions, Span likes, Span hearts, Span smiles, Icon likeIcon, Icon heartIcon, Icon happyIcon){
    	int totalLikes = 0;
	int totalHearts = 0;
	int totalSmiles = 0;

    	for(PostReaction reaction : reactions){
    	    if(reaction.getReactType().equals("Like")){
    	       totalLikes++;
    	    }else if(reaction.getReactType().equals("Heart")){
    	       totalHearts++;
    	    }else if(reaction.getReactType().equals("Happy")){
    	       totalSmiles++;
    	    }
    	}

    	likes.setText(formatValue(totalLikes));
    	hearts.setText(formatValue(totalHearts));
    	smiles.setText(formatValue(totalSmiles));

        if(totalLikes == 0){
           likeIcon.setVisible(false);
           likes.setVisible(false);
        }

	if(totalHearts == 0){
           heartIcon.setVisible(false);
           hearts.setVisible(false);
        }

        if(totalSmiles == 0){
           happyIcon.setVisible(false);
           smiles.setVisible(false);
        }

        Long totals = (long) reactions.size();

        totalReactions.setText(formatValue(totals) + " reactions");

	List<Comment> comments = commentService.getCommentsByArtworkId(artwork.getId());
        commented.setText(formatValue((long) comments.size()) + " comments");

        HorizontalLayout reactionsDiv = new HorizontalLayout(likeIcon,likes, heartIcon,hearts,  happyIcon, smiles); //commented, totalReactions
        reactionsDiv.addClassName("comment-reactions-div");

        return reactionsDiv;
    }

    private VerticalLayout createFeedHeader(User user, Artwork artwork){
        String imageUrl = user.getProfileImage();

        Avatar avatar = new Avatar();
        avatar.setImage(imageUrl);
        avatar.addClassName("profile-user-avatar");

        Div avatarDiv = new Div(avatar);

        Span name = new Span(user.getFullName());
        name.addClassName("profile-user-fullname");

        String description = artwork.getDescription();

        Span title = new Span(description);
        title.addClassName("comment-title");

        Span dateTime = new Span(artwork.getDateTime());
        dateTime.addClassName("comment-date-time");

        HorizontalLayout layout = new HorizontalLayout(avatarDiv, name);
        layout.addClassName("comment-user-layout");
        layout.addClickListener(event -> {
            UI.getCurrent().navigate(UserProfile.class, user.getId());
        });

        return new VerticalLayout(layout, dateTime, title);
    }

    public Button createCommentButtonListener(User user, Artwork artwork){
    	List<Comment> comments = commentService.getCommentsByArtworkId(artwork.getId());

	Long totalComments = (long) comments.size();

    	Button commentButton = new Button();
	commentButton.addClassName("feed-comment");
	commentButton.setIcon(new Icon(VaadinIcon.COMMENT_ELLIPSIS_O));
	commentButton.setText(formatValue(totalComments));
	commentButton.addClickListener(event -> {
            Long artworkId = artwork.getId();
            VaadinSession.getCurrent().getSession().setAttribute("main", artworkId);
            UI.getCurrent().navigate(CommentSectionView.class, artworkId);
	});

	return commentButton;
    }

    public String checkUser() {
        Authentication auth =
            SecurityContextHolder.getContext().getAuthentication();
        return auth == null ? null : auth.getName();
    }

    private String formatValue(long value) {
        if (value >= 1_000_000) {
            return formatMillions(value);
        } else if (value >= 1_000) {
            return formatThousands(value);
        } else {
            return String.valueOf(value);
        }
    }

    private String formatMillions(long value) {
        String wrapped = String.valueOf(value);
        int length = wrapped.length();
        int significantDigits = length - 6; // Determine significant digits for millions

        if (wrapped.length() > significantDigits + 1 && wrapped.charAt(significantDigits) == '0') {
            return wrapped.substring(0, significantDigits) + "M";
        } else {
            return wrapped.substring(0, significantDigits) + "." + wrapped.charAt(significantDigits) + "M";
        }
    }

    private String formatThousands(long value) {
        String wrapped = String.valueOf(value);
        int length = wrapped.length();
        int significantDigits = length - 3; // Determine significant digits for thousands

        if (wrapped.length() > significantDigits + 1 && wrapped.charAt(significantDigits) == '0') {
            return wrapped.substring(0, significantDigits) + "K";
        } else {
            return wrapped.substring(0, significantDigits) + "." + wrapped.charAt(significantDigits) + "K";
        }
    }
}
