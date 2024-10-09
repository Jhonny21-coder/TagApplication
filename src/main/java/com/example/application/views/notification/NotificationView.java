package com.example.application.views.notification;

import com.example.application.data.PostReaction;
import com.example.application.services.PostReactionService;
import com.example.application.data.notification.Notification;
import com.example.application.data.notification.NotificationType;
import com.example.application.services.notification.NotificationService;
import com.example.application.data.User;
import com.example.application.services.UserServices;
import com.example.application.data.Artwork;
import com.example.application.services.ArtworkService;
import com.example.application.views.MainFeed;
import com.example.application.views.profile.UserProfile;
import com.example.application.views.search.SearchView;
import com.example.application.views.comment.CommentSectionView;
import com.example.application.views.comment.ReplyCommentView;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.component.dialog.Dialog;
import jakarta.annotation.security.PermitAll;

import java.io.FileInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.Collections;

import java.time.format.DateTimeFormatter;
import java.time.ZoneId;
import java.time.LocalDateTime;
import java.time.Duration;

@PermitAll
@Route("notification-view")
public class NotificationView extends AppLayout {

    private final NotificationService notificationService;
    private final UserServices userService;
    private final PostReactionService postService;
    private final ArtworkService artworkService;

    public NotificationView(NotificationService notificationService, UserServices userService,
    	PostReactionService postService, ArtworkService artworkService){
    	this.notificationService = notificationService;
   	this.userService = userService;
   	this.postService = postService;
   	this.artworkService = artworkService;

	addClassName("profile-app-layout");
   	createHeader();
   	createMainLayout();
    }

    private void createMainLayout() {
    	User currentUser = userService.findCurrentUser();

    	FormLayout mainLayout = new FormLayout();
    	mainLayout.addClassName("notif-main-layout");

    	List<Notification> notifications = notificationService.getNotificationsForUser(currentUser.getId());
    	Collections.reverse(notifications);

    	// Map to store grouped reactions by referenceId for both REACT and COMMENT
    	Map<Long, List<Notification>> reactionMap = new HashMap<>();
    	Map<Long, List<Notification>> commentMap = new HashMap<>();
    	Map<Long, List<Notification>> pokeMap = new HashMap<>();
    	Map<Long, List<Notification>> followMap = new HashMap<>();
    	Map<Long, List<Notification>> replyMap = new HashMap<>();

    	Set<Long> processedReactionIds = new HashSet<>(); // Keep track of processed reaction referenceIds
    	Set<Long> processedCommentIds = new HashSet<>(); // Keep track of processed comment referenceIds
    	Set<Long> processedPokeIds = new HashSet<>(); // Keep track of processed poke referenceIds
    	Set<Long> processedFollowIds = new HashSet<>(); // Keep track of processed follow referenceIds
    	Set<Long> processedReplyIds = new HashSet<>(); // Keep track of processed reply referenceIds

    	// First pass: group reactions and comments by referenceId
    	for (Notification notification : notifications) {
            if (notification.getType() == NotificationType.REACT) {
                reactionMap.computeIfAbsent(notification.getReferenceId(), k -> new ArrayList<>()).add(notification);
            } else if (notification.getType() == NotificationType.COMMENT) {
            	commentMap.computeIfAbsent(notification.getReferenceId(), k -> new ArrayList<>()).add(notification);
            } else if (notification.getType() == NotificationType.POKE) {
                pokeMap.computeIfAbsent(notification.getReferenceId(), k -> new ArrayList<>()).add(notification);
            } else if (notification.getType() == NotificationType.FOLLOW) {
                followMap.computeIfAbsent(notification.getReferenceId(), k -> new ArrayList<>()).add(notification);
            } else if (notification.getType() == NotificationType.REPLY) {
                replyMap.computeIfAbsent(notification.getReferenceId(), k -> new ArrayList<>()).add(notification);
            }
    	}

    	// Second pass: build the UI components
    	for (Notification notification : notifications) {
            Long referenceId = notification.getReferenceId();

            Avatar avatar = new Avatar();
            avatar.addClassName("notif-avatar");

            Span message = new Span();
            message.addClassName("notif-message");

            if (notification.getType() == NotificationType.REACT) {
                if (processedReactionIds.contains(referenceId)) {
                   continue; // Skip if this REACT notification's referenceId has already been processed
            	}

            	// Get reactions for this notification's referenceId
            	List<Notification> reactionNotifications = reactionMap.get(referenceId);

            	// Display only the first user's avatar and create a grouped message
            	if (reactionNotifications != null && !reactionNotifications.isEmpty()) {
                    User firstUser = reactionNotifications.get(0).getSender();

                    updateAvatar(firstUser, avatar);

                    // Generate the dynamic message for reactions
                    generateDynamicMessage(reactionNotifications, "REACT", message, true);
            	}

		// Mark this referenceId as processed for REACT
                processedReactionIds.add(referenceId);
            } else if (notification.getType() == NotificationType.COMMENT) {
            	if (processedCommentIds.contains(referenceId)) {
                    continue; // Skip if this COMMENT notification's referenceId has already been processed
            	}

            	// Get comments for this notification's referenceId
            	List<Notification> commentNotifications = commentMap.get(referenceId);

            	// Display only the first user's avatar and create a grouped message
            	if (commentNotifications != null && !commentNotifications.isEmpty()) {
                    User firstUser = commentNotifications.get(0).getSender();

                    updateAvatar(firstUser, avatar);

                    // Generate the dynamic message for comments
                    generateDynamicMessage(commentNotifications, "COMMENT", message, true);
            	}

            	// Mark this referenceId as processed for COMMENT
            	processedCommentIds.add(referenceId);
            } else if (notification.getType() == NotificationType.POKE) {
                if (processedCommentIds.contains(referenceId)) {
                    continue; // Skip if this COMMENT notification's referenceId has already been processed
                }

                // Get comments for this notification's referenceId
                List<Notification> pokeNotifications = pokeMap.get(referenceId);

                // Display only the first user's avatar and create a grouped message
                if (pokeNotifications != null && !pokeNotifications.isEmpty()) {
                    User firstUser = pokeNotifications.get(0).getSender();

                    updateAvatar(firstUser, avatar);

                    // Generate the dynamic message for reactions
                    generateDynamicMessage(pokeNotifications, "POKE", message, true);
                }

                // Mark this referenceId as processed for POKE
                processedPokeIds.add(referenceId);
            } else if (notification.getType() == NotificationType.FOLLOW) {
            	User sender = notification.getSender();

                // Get comments for this notification's referenceId
                List<Notification> followNotifications = notifications.stream()
                	.filter(notif -> notif.getSender().equals(sender))
                	.collect(Collectors.toList());

                updateAvatar(sender, avatar);

                // Generate the dynamic message for reactions
                generateDynamicMessage(followNotifications, "FOLLOW", message, true);
            } else if (notification.getType() == NotificationType.REPLY) {
                if (processedReplyIds.contains(referenceId)) {
                   continue; // Skip if this REACT notification's referenceId has already been processed
                }

                // Get reactions for this notification's referenceId
                List<Notification> replyNotifications = replyMap.get(referenceId);

                // Display only the first user's avatar and create a grouped message
                if (replyNotifications != null && !replyNotifications.isEmpty()) {
                    User firstUser = replyNotifications.get(0).getSender();

                    updateAvatar(firstUser, avatar);

                    // Generate the dynamic message for reactions
                    generateDynamicMessage(replyNotifications, "REPLY", message, true);
                }

                // Mark this referenceId as processed for REACT
                processedReplyIds.add(referenceId);
            }

            Icon emojiIcon = new Icon();
            switch(notification.getType()){
                case COMMENT:
                    Artwork artwork = artworkService.getArtworkById(referenceId);

        	    if(artwork == null){
        	    	emojiIcon.getElement().setAttribute("icon", "vaadin:close");
        	    	emojiIcon.addClassName("notif-touch-icon");
        	    }else{
            	    	emojiIcon.getElement().setAttribute("icon", "vaadin:comment");
            	    	emojiIcon.addClassName("notif-comment-icon");
            	    }
            	    break;
                case REACT:
            	    User _reactor = notification.getSender();

            	    PostReaction reactor = postService.getPostReactionByReactorAndArtworkId(_reactor.getId(), referenceId);

            	    if(reactor == null){
            	        emojiIcon.getElement().setAttribute("icon", "vaadin:close");
                        emojiIcon.addClassName("notif-touch-icon");
            	    }else{
			if(reactor.getReactType().equals("Like")){
            	   	   emojiIcon.getElement().setAttribute("icon", "vaadin:thumbs-up");
                   	   emojiIcon.addClassName("notif-like-icon");
            	    	}else if(reactor.getReactType().equals("Heart")){
            	   	   emojiIcon.getElement().setAttribute("icon", "vaadin:heart");
                   	   emojiIcon.addClassName("notif-heart-icon");
            	    	}else if(reactor.getReactType().equals("Happy")){
            	   	   emojiIcon.getElement().setAttribute("icon", "vaadin:smiley-o");
                   	   emojiIcon.addClassName("notif-happy-icon");
                    	}
                    }
                    break;
                case POKE:
		    emojiIcon.getElement().setAttribute("icon", "vaadin:touch");
                    emojiIcon.addClassName("notif-touch-icon");
                    break;
                case FOLLOW:
                    emojiIcon.getElement().setAttribute("icon", "vaadin:user-check");
                    emojiIcon.addClassName("notif-follow-icon");
                    break;
                case REPLY:
                    emojiIcon.getElement().setAttribute("icon", "vaadin:comment");
                    emojiIcon.addClassName("notif-reply-icon");
                    break;
                default:
                    emojiIcon.getElement().setAttribute("icon", "vaadin:user-check");
                    emojiIcon.addClassName("notif-follow-icon");
            }

            Icon moreIcon = new Icon("vaadin", "ellipsis-dots-h");
            moreIcon.addClassName("notif-more-icon");
            moreIcon.addClickListener(event -> {
                Dialog dialog = new Dialog();
                dialog.addClassName("notif-dialog");
                createMoreDialog(
                    dialog, notification, reactionMap, commentMap,
                    pokeMap, notifications, replyMap
                );
            });

            Span timeAgo = new Span();
            timeAgo.addClassName("notif-time-ago");
            updateTimeAgo(notification, timeAgo);

            Div avatarDiv = new Div(avatar);
            avatarDiv.addClickListener(event -> {
                System.out.println("Ref ID: " + referenceId);

		VaadinSession.getCurrent().getSession().setAttribute("notification", referenceId);

                if (notification.getType() == NotificationType.REACT || notification.getType() == NotificationType.COMMENT) {
                    UI.getCurrent().navigate(CommentSectionView.class, referenceId);
                } else if (notification.getType() == NotificationType.POKE || notification.getType() == NotificationType.FOLLOW) {
                    UI.getCurrent().navigate(UserProfile.class, referenceId);
                } else if(notification.getType() == NotificationType.REPLY){
                    UI.getCurrent().navigate(ReplyCommentView.class, referenceId);
                }
            });

            VerticalLayout messageLayout = new VerticalLayout(message, timeAgo);
            messageLayout.addClassName("notif-message-layout");
	    messageLayout.addClickListener(event -> {
                System.out.println("Ref ID: " + referenceId);

                if (notification.getType() == NotificationType.REACT || notification.getType() == NotificationType.COMMENT) {
                    VaadinSession.getCurrent().getSession().setAttribute("notification", referenceId);
                    UI.getCurrent().navigate(CommentSectionView.class, referenceId);
                } else if (notification.getType() == NotificationType.POKE || notification.getType() == NotificationType.FOLLOW) {
                    UI.getCurrent().navigate(UserProfile.class, referenceId);
                } else if(notification.getType() == NotificationType.REPLY){
                    UI.getCurrent().navigate(ReplyCommentView.class, referenceId);
                }
            });

            HorizontalLayout middleLayout = new HorizontalLayout(emojiIcon, avatarDiv, messageLayout, moreIcon);
            middleLayout.addClassName("notif-middle-layout");

            mainLayout.add(middleLayout);
    	}

    	setContent(mainLayout);
    }

    private void createMoreDialog(Dialog dialog, Notification notification,
    	Map<Long, List<Notification>> reactionMap, Map<Long, List<Notification>> commentMap,
    	Map<Long, List<Notification>> pokeMap, List<Notification> notifications,
    	Map<Long, List<Notification>> replyMap){
    	dialog.open();

	Long referenceId = notification.getReferenceId();
	User user = notification.getSender();

    	List<Notification> reactionNotifications = reactionMap.get(referenceId);
    	List<Notification> commentNotifications = commentMap.get(referenceId);
    	List<Notification> pokeNotifications = pokeMap.get(referenceId);
    	List<Notification> followNotifications = notifications.stream()
                        .filter(notif -> notif.getSender().equals(user))
                        .collect(Collectors.toList());
	List<Notification> replyNotifications = replyMap.get(referenceId);

	Span message = new Span();

	switch(notification.getType()){
    	   case COMMENT:
    	   	generateDynamicMessage(commentNotifications, "COMMENT", message, false);
    	   	break;
    	   case REACT:
    	   	generateDynamicMessage(reactionNotifications, "REACT", message, false);
    	   	break;
    	   case POKE:
           	generateDynamicMessage(pokeNotifications, "POKE", message, false);
           	break;
           case FOLLOW:
           	generateDynamicMessage(followNotifications, "FOLLOW", message, false);
           	break;
           case REPLY:
                generateDynamicMessage(replyNotifications, "REPLY", message, false);
                break;
           default:
           	generateDynamicMessage(followNotifications, "FOLLOW", message, false);
        }

        String imageUrl = user.getProfileImage();

	Avatar avatar = new Avatar();
       	avatar.setImage(imageUrl);

       	VerticalLayout headerLayout = new VerticalLayout(avatar, message);
       	headerLayout.addClassName("notif-dialog-header-layout");

       	Div line = new Div();
       	line.addClassName("notif-line-div");

    	HorizontalLayout removeLayout = new HorizontalLayout(
    		new Icon("vaadin", "close-circle"),
    		new Span("Remove this notification")
    	);
    	removeLayout.addClassName("notif-dialog-layout");
    	removeLayout.addClickListener(event -> {
    	    dialog.close();
    	    NotificationType type = notification.getType();
            notificationService.removeNotificationsByReferenceIdAndType(
                        referenceId, type
            );
            createMainLayout();
    	});

    	HorizontalLayout reportLayout = new HorizontalLayout(
                new Icon("vaadin", "exclamation-circle"),
                new Span("Report this notification")
        );
        reportLayout.addClassName("notif-dialog-layout");

    	dialog.add(headerLayout, line, removeLayout, reportLayout);
    }

    private void updateAvatar(User user, Avatar avatar){
	String imageUrl = user.getProfileImage();

        avatar.setImage(imageUrl);
    }

    private void generateDynamicMessage(List<Notification> notifications, String type, Span message, boolean isAddClass) {
    	List<String> users = notifications.stream()
            .map(n -> n.getSender().getFullName()) // getFullName() gives the user's full name
            .distinct() // Avoid duplicates
            .collect(Collectors.toList()); // Return as List

    	//String action = type.equals("REACT") ? "reacted to your post." : "commented on your post.";

    	Map<String, String> actionMap = new HashMap<>();

        String types[] = {
            "reacted to your post.",
            "commented on your post.",
            "poked you.",
            "followed you.",
            "replied to your comment."
        };

        String names[] = {
            "REACT",
            "COMMENT",
            "POKE",
            "FOLLOW",
            "REPLY"
        };

        for(int i = 0; i < types.length; i++){
            actionMap.put(names[i], types[i]);
        }

    	Span actionSpan = new Span(actionMap.get(type));

   	 switch (users.size()) {
            case 1:
            	message.add(createStyledSpan(users.get(0), isAddClass), new Span(" "), actionSpan);
            	break;
            case 2:
            	message.add(createStyledSpan(users.get(0), isAddClass), new Span(" and "), createStyledSpan(users.get(1), isAddClass),
            		new Span(" "), actionSpan);
            	break;
            default:
            	message.add(createStyledSpan(users.get(0), isAddClass), new Span(", "), createStyledSpan(users.get(1), isAddClass),
         		new Span(" and "), new Span((users.size() - 2) + " others "), actionSpan);
            	break;
    	}
    }

    // Helper method to create a styled Span for user names
    private Span createStyledSpan(String text, boolean isAddClass) {
    	Span span = new Span(text);

    	if(isAddClass){
    	   span.addClassName("notif-user-names");
    	}
    	return span;
    }

    private void updateTimeAgo(Notification notification, Span timeAgo) {
    	LocalDateTime creationTime = notification.getTimestamp();
    	LocalDateTime currentTime = LocalDateTime.now(ZoneId.of("Asia/Manila"));

    	Duration duration = Duration.between(creationTime, currentTime);

    	long days = duration.toDays();
    	long hours = duration.toHours() % 24;
    	long minutes = duration.toMinutes() % 60;

    	DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a");
	DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("EEEE 'at' h:mm a");
	DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM d 'at' h:mm a");

    	if (days == 0) {
           if (hours == 0) {
              timeAgo.setText(minutes + " minutes ago");
           } else if (hours == 1) {
              timeAgo.setText(hours + " hour ago");
           }else{
              timeAgo.setText(hours + " hours ago");
           }
    	} else if (days == 1) {
           timeAgo.setText("Yesterday at " + creationTime.format(timeFormatter));
    	} else if (days < 7) {
           timeAgo.setText("On " + creationTime.format(dayFormatter));
    	} else {
           timeAgo.setText(creationTime.format(dateFormatter));
        }
    }

    private void createHeader(){
        Icon backIcon = new Icon("lumo", "arrow-left");
        backIcon.addClassName("header-back-button");
        backIcon.addClickListener(event -> {
            UI.getCurrent().navigate(MainFeed.class);
        });

    	Span notificationText = new Span("Notifications");
    	notificationText.addClassName("notif-text");

    	Icon searchIcon = new Icon("vaadin", "search");
    	searchIcon.addClassName("notif-search-icon");
    	searchIcon.addClickListener(event -> {
            UI.getCurrent().navigate(SearchView.class);
        });

    	HorizontalLayout headerLayout = new HorizontalLayout(backIcon, notificationText, searchIcon);
    	headerLayout.addClassName("notif-header-layout");

    	addToNavbar(headerLayout);
    }
}

