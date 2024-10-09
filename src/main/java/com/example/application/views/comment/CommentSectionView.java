package com.example.application.views.comment;

import com.example.application.services.notification.NotificationService;
import com.example.application.data.PostReaction;
import com.example.application.services.PostReactionService;
import com.example.application.data.LikeReaction;
import com.example.application.services.LikeReactionService;
import com.example.application.data.HeartReaction;
import com.example.application.services.HeartReactionService;
import com.example.application.data.User;
import com.example.application.services.UserServices;
import com.example.application.data.Artwork;
import com.example.application.services.ArtworkService;
import com.example.application.data.Comment;
import com.example.application.services.CommentService;
import com.example.application.data.CommentReaction;
import com.example.application.services.CommentReactionService;
import com.example.application.data.Reply;
import com.example.application.services.ReplyService;
import com.example.application.data.SavedPost;
import com.example.application.services.SavedPostService;
import com.example.application.services.story.StoryService;
import com.example.application.views.profile.UserProfile;
import com.example.application.views.profile.ViewSpecificArtwork;
import com.example.application.views.profile.ProfileFeed;
import com.example.application.views.profile.SavedPostView;
import com.example.application.views.notification.NotificationView;
import com.example.application.views.MainFeed;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.virtuallist.VirtualList;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import jakarta.annotation.security.PermitAll;

import java.util.Set;
import java.util.List;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.Date;
import java.util.TimeZone;
import java.util.Locale;

import java.io.FileInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.InputStream;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.Duration;

@Route("comments")
@PermitAll
public class CommentSectionView extends AppLayout implements HasUrlParameter<Long> {

    private final CommentService commentService;
    private final UserServices userService;
    private final ArtworkService artworkService;
    private final LikeReactionService likeService;
    private final HeartReactionService heartService;
    private final CommentReactionService commentReactionService;
    private final ReplyService replyService;
    private final SavedPostService savedPostService;
    private final PostReactionService postService;
    private final StoryService storyService;
    private final NotificationService notificationService;

    private final TextArea inputField = new TextArea();
    private Span noComments = new Span("No comments yet");
    private final Grid<User> userGrid = new Grid<>(User.class, false);

    public CommentSectionView(CommentService commentService, UserServices userService, ArtworkService artworkService,
    	LikeReactionService likeService, HeartReactionService heartService, CommentReactionService commentReactionService,
    	ReplyService replyService, SavedPostService savedPostService, PostReactionService postService,
    	StoryService storyService, NotificationService notificationService) {

    	this.commentService = commentService;
        this.userService = userService;
        this.artworkService = artworkService;
	this.likeService = likeService;
        this.heartService = heartService;
        this.commentReactionService = commentReactionService;
	this.replyService = replyService;
	this.savedPostService = savedPostService;
	this.postService = postService;
	this.storyService = storyService;
	this.notificationService = notificationService;

        addClassName("profile-app-layout");
    }

    @Override
    public void setParameter(BeforeEvent event, Long artworkId) {
        List<Comment> comments = commentService.getCommentsByArtworkId(artworkId);

        Artwork artwork = artworkService.getArtworkById(artworkId);

        User user = artwork.getUser();

        displayComments(artwork, user);
        createHeader(user, artwork);
    }

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
            Icon icon;

            // Select icon based on reaction type
            switch (reaction.getReactType()) {
            	case "Like":
                    icon = new Icon(VaadinIcon.THUMBS_UP);
                    icon.addClassName("main-like-icon");
                    break;
            	case "Heart":
                    icon = new Icon(VaadinIcon.HEART);
                    icon.addClassName("main-heart-icon");
                    break;
            	case "Happy":
                    icon = new Icon(VaadinIcon.SMILEY_O);
                    icon.addClassName("main-smile-icon");
                    break;
            	default:
                    continue; // Skip unknown reaction types
            }

            Avatar avatar = createAvatar(user);
            Span name = createNameLayout(reaction);

       	    HorizontalLayout layout = createAllReactionLayout(icon, avatar, name, dialog, user);
            allContent.add(layout);
    	}

    	return allContent;
    }

    private HorizontalLayout createAllReactionLayout(Icon icon, Avatar avatar, Span name, ConfirmDialog dialog, User user) {
    	HorizontalLayout layout = new HorizontalLayout(icon, avatar, name);
    	layout.addClassName("main-reactor-main-layout");
    	layout.addClickListener(event -> {
            dialog.close();
            UI.getCurrent().navigate(UserProfile.class, user.getId());
    	});
    	return layout;
    }

    private void createReactedLayout(List<PostReaction> reactions, ConfirmDialog dialog) {
    	dialog.open();

    	// Variables to track reaction counts
    	int totalLikes = 0;
    	int totalHearts = 0;
    	int totalSmiles = 0;

    	// Create content layouts
    	VerticalLayout allContent = createAllReactedLayout(reactions, dialog);
    	VerticalLayout likeContent = new VerticalLayout();
    	VerticalLayout heartContent = new VerticalLayout();
    	VerticalLayout smileContent = new VerticalLayout();

    	// Process each reaction and distribute into respective layouts
    	for (PostReaction reaction : reactions) {
            User user = reaction.getReactor();
            String reactType = reaction.getReactType();
            HorizontalLayout layout = createReactionLayout(reaction, dialog, user);

            switch (reactType) {
            	case "Like":
                    likeContent.add(layout);
                    totalLikes++;
                    break;
            	case "Heart":
                    heartContent.add(layout);
                    totalHearts++;
                    break;
            	case "Happy":
                    smileContent.add(layout);
                    totalSmiles++;
                    break;
        	}
    	    }

    	    // Create icons and tab components for each reaction type
    	    Tab allTab = createTab("All", totalLikes + totalHearts + totalSmiles);
    	    Tab likeTab = createTabWithIcon(VaadinIcon.THUMBS_UP, totalLikes, "main-reactor-like-icon");
    	    Tab heartTab = createTabWithIcon(VaadinIcon.HEART, totalHearts, "main-reactor-heart-icon");
    	    Tab smileTab = createTabWithIcon(VaadinIcon.SMILEY_O, totalSmiles, "main-reactor-smile-icon");

    	    // Hide tabs if there are no reactions
    	    likeTab.setVisible(totalLikes > 0);
    	    heartTab.setVisible(totalHearts > 0);
    	    smileTab.setVisible(totalSmiles > 0);

    	    // Tabs layout
    	    Tabs tabs = new Tabs(allTab, likeTab, heartTab, smileTab);
       	    tabs.addClassName("main-reactor-tabs");

    	    Span headerText = new Span("People who reacted");
    	    headerText.addClassName("main-reactor-header-text");

    	    VerticalLayout tabsLayout = new VerticalLayout(headerText, tabs);
    	    tabsLayout.addClassName("main-tabs-Layout");

    	    dialog.setHeader(tabsLayout);

    	    // Content container
    	    Div dialogContent = new Div(allContent, likeContent, heartContent, smileContent);
    	    dialogContent.addClassName("main-dialog-content");

    	    dialog.add(dialogContent);

    	    // Show content based on selected tab
    	    tabs.addSelectedChangeListener(event -> {
       	 	allContent.setVisible(tabs.getSelectedTab().equals(allTab));
        	likeContent.setVisible(tabs.getSelectedTab().equals(likeTab));
        	heartContent.setVisible(tabs.getSelectedTab().equals(heartTab));
        	smileContent.setVisible(tabs.getSelectedTab().equals(smileTab));
    	    });

    	    // Show only the first tab's content initially
    	    allContent.setVisible(true);
    	    likeContent.setVisible(false);
    	    heartContent.setVisible(false);
    	    smileContent.setVisible(false);
    }

    // Helper method to create reaction layout
    private HorizontalLayout createReactionLayout(PostReaction reaction, ConfirmDialog dialog, User user) {
    	Icon icon;
    	switch (reaction.getReactType()) {
            case "Like":
            	icon = new Icon(VaadinIcon.THUMBS_UP);
            	icon.addClassName("main-like-icon");
            	break;
            case "Heart":
            	icon = new Icon(VaadinIcon.HEART);
            	icon.addClassName("main-heart-icon");
            	break;
            case "Happy":
            	icon = new Icon(VaadinIcon.SMILEY_O);
            	icon.addClassName("main-smile-icon");
            	break;
            default:
            	return null; // Skip unknown reaction types
    	}

    	Avatar avatar = createAvatar(user);
    	Span name = createNameLayout(reaction);

    	HorizontalLayout layout = new HorizontalLayout(icon, avatar, name);
    	layout.addClassName("main-reactor-main-layout");
    	layout.addClickListener(event -> {
            dialog.close();
            UI.getCurrent().navigate(UserProfile.class, user.getId());
    	});

    	return layout;
    }

    // Helper method to create a tab with an icon and count
    private Tab createTabWithIcon(VaadinIcon iconType, int count, String className) {
    	Icon icon = new Icon(iconType);
    	icon.addClassName(className);
    	return new Tab(icon, new Span(formatValue(count)));
    }

    // Helper method to create a tab for "All" reactions
    private Tab createTab(String label, int count) {
    	Span labelText = new Span(label);
    	labelText.addClassName("main-all-text");
    	return new Tab(labelText, new Span(formatValue(count)));
    }

    private void updateCommentTime(Comment comment, Span timeAgo) {
    	LocalDateTime creationTime = comment.getDateTimePosted();
    	LocalDateTime currentTime = LocalDateTime.now(ZoneId.of("Asia/Manila"));

    	Duration duration = Duration.between(creationTime, currentTime);

    	long seconds = duration.getSeconds();
    	long minutes = duration.toMinutes();
    	long hours = duration.toHours();
    	long days = duration.toDays();

    	if (seconds < 60) {
           // Less than 1 minute ago, use "sec" or "secs"
           timeAgo.setText(seconds == 1 ? seconds + " sec" : seconds + " secs");
    	} else if (minutes < 60) {
           // Less than 1 hour ago, use "min" or "mins"
           timeAgo.setText(minutes == 1 ? minutes + " min" : minutes + " mins");
    	} else if (hours < 24) {
           // Less than 1 day ago, use "hr" or "hrs"
           timeAgo.setText(hours == 1 ? hours + " hr" : hours + " hrs");
    	} else if (days < 7) {
           // Between 1 and 7 days ago, display day of the week (e.g., "Mon")
           DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("EEE"); // Abbreviated weekday
           String formattedDay = creationTime.format(dayFormatter);
           timeAgo.setText("On " + formattedDay); // e.g., "On Mon"
    	} else if (days < 30) {
           // More than 7 days but less than 1 month, display "1 wk" or "2 wks"
           long weeks = days / 7;
           timeAgo.setText(weeks == 1 ? weeks + " wk" : weeks + " wks");
    	} else {
           // More than 1 month, display "1 mo" or "2 mos"
           long months = days / 30; // Approximate to 30 days in a month
           timeAgo.setText(months == 1 ? months + " mo" : months + " mos");
        }
    }

    private void displayComments(Artwork artwork, User user){
    	List<LikeReaction> totalLikeReactions = likeService.getReactionForArtworkId(artwork.getId());
        List<HeartReaction> totalHeartReactions = heartService.getReactionForArtworkId(artwork.getId());

    	FormLayout formLayout = new FormLayout();
        formLayout.addClassName("specific-form-layout");

	String imageUrl = artwork.getArtworkUrl();

        Image image = new Image(imageUrl, "artwork-image");
        image.addClassName("specific-image");

	Span totalReactions = new Span();
	totalReactions.addClassName("reacted");

	Span commented = new Span();
	commented.addClassName("commented");

	Span likes = new Span();
	likes.addClassName("specific-reacts");

	Span hearts = new Span();
	hearts.addClassName("specific-reacts");

	Span smiles = new Span();
        smiles.addClassName("specific-reacts");

        Icon likeIcon = new Icon(VaadinIcon.THUMBS_UP);
        likeIcon.addClassName("reactions-like");

        Icon heartIcon = new Icon(VaadinIcon.HEART);
	heartIcon.addClassName("reactions-heart");

	Icon happyIcon = new Icon(VaadinIcon.SMILEY_O);
	happyIcon.addClassName("reactions-happy");

	Button likeButton = new Button();
	likeButton.addClassName("feed-reaction");
	showReactions(likeButton, artwork, likes, hearts, smiles, likeIcon, heartIcon, happyIcon);

	Button commentButton = createCommentButtonListener(user, artwork);

	Button shareButton = new Button("7262", new Icon(VaadinIcon.ARROW_FORWARD));
	shareButton.addClassName("feed-heart");

	List<PostReaction> postReactions = postService.getPostReactionsByArtworkId(artwork.getId());

	HorizontalLayout totalReactionsDiv = createTotalReactions(postReactions, commented, artwork, totalReactions, likes, hearts, smiles, likeIcon, heartIcon, happyIcon);
	totalReactionsDiv.addClickListener(event -> {
	    List<PostReaction> reactions = postService.getPostReactionsByArtworkId(artwork.getId());

            ConfirmDialog dialog = new ConfirmDialog();
            dialog.addClassName("main-reactor-dialog");
            dialog.setConfirmText("Close");

            createReactedLayout(reactions, dialog);
	});

	HorizontalLayout buttonLayout = new HorizontalLayout(likeButton, commentButton, shareButton);
	buttonLayout.addClassName("comment-button-layout");

	VirtualList<Comment> commentLayout = createComment(artwork.getId());
	commentLayout.addClassName("comment-virtual-list");
	createFooter(artwork, formLayout, commentButton, commented);

	VerticalLayout userHeader = createUserHeader(user, artwork);
	userHeader.addClassName("comment-user-header-layout");

        formLayout.add(userHeader, image, totalReactionsDiv, buttonLayout, commentLayout);

        setContent(formLayout);
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

    public HorizontalLayout createTotalReactions(List<PostReaction> reactions, Span commented, Artwork artwork,
        Span totalReactions, Span likes, Span hearts, Span smiles, Icon likeIcon, Icon heartIcon, Icon happyIcon){
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

        HorizontalLayout reactionsDiv = new HorizontalLayout(likeIcon,likes, heartIcon,hearts,  happyIcon, smiles);
        reactionsDiv.addClassName("comment-reactions-div");
        return reactionsDiv;
    }

    private VerticalLayout createUserHeader(User user, Artwork artwork){
    	String imageUrl = user.getProfileImage();

    	Avatar avatar = new Avatar();
    	avatar.setImage(imageUrl);
    	avatar.addClassName("comment-user-avatar");

    	Div avatarDiv = new Div(avatar);
	avatarDiv.addClickListener(event -> {
            UI.getCurrent().navigate(UserProfile.class, user.getId());
        });

	Span name = new Span(user.getFullName());
	name.addClassName("comment-user-fullname");
	name.addClickListener(event -> {
            UI.getCurrent().navigate(UserProfile.class, user.getId());
        });

	Icon moreIcon = new Icon(VaadinIcon.ELLIPSIS_DOTS_H);
	moreIcon.addClassName("comment-more-icon");
	moreIcon.addClickListener(event -> {
	    Dialog dialog = new Dialog();
	    dialog.addClassName("post-more-dialog");

	    Button button1 = new Button("Save Post", new Icon(VaadinIcon.PLUS_CIRCLE));
	    Button button2 = new Button("Report Post", new Icon(VaadinIcon.EXCLAMATION_CIRCLE));
	    Button button3 = new Button("Hide Post", new Icon(VaadinIcon.CLOSE_CIRCLE));
	    Button button4 = new Button("Add To Favorites", new Icon(VaadinIcon.PIN_POST));
	    Button copyButton = new Button("Copy Link", new Icon(VaadinIcon.LINK));

	    String _imageUrl = "http://localhost:8080/shared-artwork/" + artwork.getId();
	    createCopyListener(_imageUrl, copyButton);
	    createSavePost(button1, artwork);

	    button1.addClassName("post-more-button");
	    button2.addClassName("post-more-button");
	    button3.addClassName("post-more-button");
	    button4.addClassName("post-more-button");
	    copyButton.addClassName("post-more-button");

	    VerticalLayout layout = new VerticalLayout();
	    layout.add(button1, button2, button3, button4, copyButton);

	    dialog.add(layout);

	    dialog.open();
	});

	String description = artwork.getDescription();

	Span title = new Span(description);
	title.addClassName("comment-title");

	Span dateTime = new Span(artwork.getDateTime());
	dateTime.addClassName("comment-date-time");

	HorizontalLayout layout = new HorizontalLayout(moreIcon, avatarDiv, name);
	layout.addClassName("comment-user-layout");

	return new VerticalLayout(layout, dateTime, title);
    }

    private void createSavePost(Button saveButton, Artwork artwork){
    	User currentUser = userService.findCurrentUser();

        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
					.withLocale(Locale.US);

	LocalDateTime localDateTime = LocalDateTime.now(ZoneId.of("Asia/Manila"));

	// Format the date and time to a string
	String dateTime = formatter.format(localDateTime);

    	saveButton.addClickListener(event -> {
    	    SavedPost savedPost = savedPostService.getSavedPostByArtworkAndUserId(artwork.getId(), currentUser.getId());

    	    if(savedPost == null){
    	       savedPostService.savePost(artwork, currentUser, dateTime);

    	       Notification.show("Artwork saved", 1000, Notification.Position.MIDDLE)
    	    	   .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    	    }else{
    	        Notification.show("Artwork already saved. Cannot save again", 1000, Notification.Position.MIDDLE)
                   .addThemeVariants(NotificationVariant.LUMO_ERROR);
    	    }
    	});
    }

    private void createCopyListener(String imageUrl, Button copyButton){
        copyButton.addClickListener(event -> {
            copyToClipboard(imageUrl);
            Notification.show("Link copied to clipboard", 1000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });
    }

    private void copyToClipboard(String text) {
        String jsCode = "navigator.clipboard.writeText('" + text + "').then(function() { "
                + "console.log('Copying to clipboard was successful!'); "
                + "}, function(err) { "
                + "console.error('Could not copy text: ', err); "
                + "});";
        UI.getCurrent().getPage().executeJs(jsCode);
    }

    private ComponentRenderer<Component, Comment> commentRenderer = new ComponentRenderer<>(
	comment -> {
	    VerticalLayout commentLayout = new VerticalLayout();
            commentLayout.addClassName("comment-layout-2");

	    User user = comment.getUser();

	    String imageUrl = user.getProfileImage();

            Avatar avatar = new Avatar();
            avatar.setImage(imageUrl);
            avatar.addClassName("comment-avatar");

            Div avatarDiv = new Div(avatar);
            avatarDiv.addClassName("comment-avatar-div");
            avatarDiv.addClickListener(event -> {
                UI.getCurrent().navigate(UserProfile.class, user.getId());
            });

            Span name = new Span(user.getFullName());
            name.addClassName("comment-fullname");
            name.addClickListener(event -> {
                UI.getCurrent().navigate(UserProfile.class, user.getId());
            });

            String userComments = comment.getComments();

            Span userComment = new Span(userComments);
            userComment.addClassName("comment-comments");

            VerticalLayout layout1 = new VerticalLayout(name, userComment);
            layout1.addClassName("comment-layout1");

            HorizontalLayout layout2 = new HorizontalLayout(avatarDiv, layout1);
            layout2.addClassName("comment-layout2");

            Span timeAgo = new Span();
            timeAgo.addClassName("comment-time-ago");

            VerticalLayout div = createCommentFooter(comment, timeAgo);
            div.addClassName("comment-div");

            updateCommentTime(comment, timeAgo);

	    commentLayout.add(layout2, div);
            return commentLayout;
    	}
    );

    private VirtualList<Comment> createComment(Long artworkId){
    	List<Comment> comments = commentService.getCommentsByArtworkId(artworkId);
	Collections.reverse(comments);

	/*VerticalLayout commentLayout = new VerticalLayout();
	commentLayout.addClassName("comment-layout-2");

	if(comments.isEmpty()){
	    noComments.addClassName("no-comments");

	    commentLayout.add(noComments);
	}
        }*/

        VirtualList<Comment> list = new VirtualList<>();
        list.setItems(comments);
        list.setRenderer(commentRenderer);

        return list;
    }

    public void showReactions(Button likeButton, Artwork artwork, Span likes, Span hearts,
        Span smiles, Icon likeIcon, Icon heartIcon, Icon happyIcon){

        List<PostReaction> reactions = postService.getPostReactionsByArtworkId(artwork.getId());

        Dialog dialog = new Dialog();
        dialog.addClassName("comment-dialog");

        AtomicLong totalReacts = new AtomicLong(reactions.size());

        if(totalReacts.get() != 0){
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
            createButtonsListener(isReacted, "Like", totalReacts, likeButton, artwork, "primary", likes, hearts, smiles, likeIcon, heartIcon, happyIcon);
            dialog.close();
        });

        Icon heartReactIcon = new Icon(VaadinIcon.HEART);
        heartReactIcon.addClassName("heart-react-icon");
        heartReactIcon.addClickListener(e -> {
            createButtonsListener(isReacted, "Heart", totalReacts, likeButton, artwork, "error", likes, hearts, smiles, likeIcon, heartIcon, happyIcon);
            dialog.close();
        });

        Icon happyReactIcon = new Icon(VaadinIcon.SMILEY_O);
        happyReactIcon.addClassName("happy-react-icon");
        happyReactIcon.addClickListener(e -> {
            createButtonsListener(isReacted, "Happy", totalReacts, likeButton, artwork, "warning", likes, hearts, smiles, likeIcon, heartIcon, happyIcon);
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
        Artwork artwork, String colorTheme, Span likes, Span hearts, Span smiles, Icon likeIcon, Icon heartIcon, Icon happyIcon){

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
              }else{
                 button.setText(String.valueOf(totalReacts.get()));
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

    private VerticalLayout createCommentFooter(Comment comment, Span timeAgo){
    	Span likeButton = new Span("React");
	likeButton.addClassName("comment-buttons");

	Span reacts = new Span();
        reacts.addClassName("reacts");

	showReplyReactions(likeButton, reacts, comment);

	Span replyButton = new Span("Reply");
	replyButton.addClassName("comment-buttons");
	replyButton.addClickListener(event -> {
	    UI.getCurrent().navigate(ReplyCommentView.class, comment.getId());
	});

	Span moreButton = new Span("More");
	moreButton.addClassName("comment-buttons");

	Icon like = new Icon(VaadinIcon.THUMBS_UP);
	like.addClassName("comment-like");

	Icon heart = new Icon(VaadinIcon.HEART);
        heart.addClassName("comment-heart");

	Icon happy = new Icon(VaadinIcon.SMILEY_O);
        happy.addClassName("comment-happy");

        Div reactsDiv = new Div(like, heart, happy, reacts);
        reactsDiv.addClassName("comment-reacts-div");

	List<Reply> replies = replyService.getRepliesByCommentId(comment.getId());

	Span viewReply = new Span();

	if(!replies.isEmpty()){
	    viewReply.setVisible(true);

	    if(replies.size() == 1){
	       viewReply.setText("View " + formatValue(replies.size()) + " reply");
	       viewReply.addClassName("comment-view-reply");
	    }else{
	       viewReply.setText("View " + formatValue(replies.size()) + " replies");
	       viewReply.addClassName("comment-view-reply");
	    }
	}else{
	    viewReply.setVisible(false);
	}

	viewReply.addClickListener(event -> {
            UI.getCurrent().navigate(ReplyCommentView.class, comment.getId());
        });

        HorizontalLayout footerLayout = new HorizontalLayout(likeButton, replyButton, timeAgo, reactsDiv);
        footerLayout.addClassName("comment-footer-layout-1");

	return new VerticalLayout(footerLayout, viewReply);
    }

    private void showReplyReactions(Span likeButton, Span reacts, Comment comment){
    	List<CommentReaction> reactions = commentReactionService.getCommentReactionsByCommentId(comment.getId());

     	Dialog dialog = new Dialog();
        dialog.addClassName("comment-dialog");

	AtomicLong totalReacts = new AtomicLong(reactions.size());

	if(totalReacts.get() != 0){
           reacts.setText(formatValue(totalReacts.get() + 999990));
        }

	User currentUser = userService.findCurrentUser();

	CommentReaction reactor = commentReactionService.getCommentReactionByReactorAndCommentId(currentUser.getId(), comment.getId());

	AtomicBoolean isReacted = new AtomicBoolean(reactor != null);

	if(isReacted.get() && reactor.getReactType().equalsIgnoreCase("Like")){
	    likeButton.setText("Reacted");
            likeButton.getStyle().set("color", "var(--lumo-primary-color)");
	}else if(isReacted.get() && reactor.getReactType().equalsIgnoreCase("Heart")){
	    likeButton.setText("Reacted");
            likeButton.getStyle().set("color", "var(--lumo-error-color)");
	}else if(isReacted.get() && reactor.getReactType().equalsIgnoreCase("Happy")){
	    likeButton.setText("Reacted");
            likeButton.getStyle().set("color", "var(--lumo-warning-color)");
	}

	Icon likeIcon = new Icon(VaadinIcon.THUMBS_UP);
        likeIcon.addClassName("like-react-icon");
        likeIcon.addClickListener(e -> {
            createReplyButtonsListener(isReacted, "Like", totalReacts, likeButton, reacts, comment, "primary");
            dialog.close();
        });

	Icon heartIcon = new Icon(VaadinIcon.HEART);
	heartIcon.addClassName("heart-react-icon");
	heartIcon.addClickListener(e -> {
            createReplyButtonsListener(isReacted, "Heart", totalReacts, likeButton, reacts, comment, "error");
            dialog.close();
        });

	Icon happyIcon = new Icon(VaadinIcon.SMILEY_O);
	happyIcon.addClassName("happy-react-icon");
	happyIcon.addClickListener(e -> {
            createReplyButtonsListener(isReacted, "Happy", totalReacts, likeButton, reacts, comment, "warning");
            dialog.close();
        });

	dialog.add(
	    new VerticalLayout(likeIcon, new Span("Like")),
	    new VerticalLayout(heartIcon, new Span("Heart")),
	    new VerticalLayout(happyIcon, new Span("Happy"))
	);

    	likeButton.addClickListener(event -> {
             dialog.open();
        });
    }

    public void createReplyButtonsListener(AtomicBoolean isReacted, String reactType, AtomicLong totalReacts, Span button,
        Span reacts, Comment comment, String colorTheme){

	User currentUser = userService.findCurrentUser();

        if(!isReacted.get()){
           commentReactionService.saveCommentReaction(comment, currentUser, reactType);

	   totalReacts.incrementAndGet();

           reacts.setText(String.valueOf(totalReacts.get()));

           button.setText("Reacted");
           button.getStyle().set("color", "var(--lumo-" + colorTheme + "-color)");

           isReacted.set(true);
        }else{
           Long reactorId = currentUser.getId();
           Long commentId = comment.getId();

           CommentReaction reactor = commentReactionService.getCommentReactionByReactorAndCommentId(reactorId, commentId);

           if(reactor.getReactType().equalsIgnoreCase(reactType)){
              commentReactionService.removeCommentReaction(reactorId, commentId);

	      totalReacts.decrementAndGet();

              if(totalReacts.get() == 0){
                 reacts.setText("");
              }else{
                 reacts.setText(String.valueOf(totalReacts.get()));
              }

              button.setText("React");
              button.getStyle().set("color", "var(--lumo-contrast-70pct)");

              isReacted.set(false);
            }else{
              commentReactionService.updateCommentReaction(reactor, reactType);

              button.setText("Reacted");
              button.getStyle().set("color", "var(--lumo-" + colorTheme + "-color)");

              isReacted.set(true);
            }
        }
    }

    private void createFooter(Artwork artwork, FormLayout formLayout, Button commentButton, Span commented){
	Button sendIcon = new Button();
    	sendIcon.setIcon(new Icon(VaadinIcon.PAPERPLANE));
	sendIcon.addClassName("comment-send-icon");
	sendIcon.setEnabled(false);
	sendIcon.addClickListener(event -> {
	     noComments.setVisible(false);

	     User user = userService.findCurrentUser();

	     notificationService.createCommentNotification(user, artwork);

	     VerticalLayout singleComment = createSingleComment(artwork, commentButton, commented);

	     inputField.clear();

	     formLayout.addComponentAtIndex(4, singleComment);
	});

	inputField.addClassName("comment-input-field");
	inputField.setPlaceholder("Write a comment...");
	inputField.setValueChangeMode(ValueChangeMode.EAGER);
	inputField.addValueChangeListener(event -> {
	     String value = event.getValue();

	     if(!value.isEmpty() && !value.matches("\\s*")){
	     	sendIcon.setEnabled(true);
	     	sendIcon.getStyle().set("color", "var(--lumo-primary-color)");

		Dialog dialog = new Dialog();
		dialog.addClassName("all-users-dialog");
		dialog.setHeaderTitle("Mention A Follower");

	     	if(value.equals("@")){
	     	   createUsersDiv(dialog);
	     	}
	     }else{
	    	sendIcon.setEnabled(false);
	    	sendIcon.getStyle().set("color", "var(--lumo-contrast-50pct)");
	     }
	});

	Upload upload = createUploadImage();
    	addToNavbar(true, upload, inputField, sendIcon);
    }

    private void createUsersDiv(Dialog dialog){
    	dialog.open();
    	List<User> users = userService.getAllUsers();

    	userGrid.setItems(users);
    	userGrid.removeAllColumns();
    	userGrid.addClassName("all-users-grid");

	TextField field = new TextField();
        field.setValueChangeMode(ValueChangeMode.EAGER);
        field.setPlaceholder("Find more friends...");
        field.addValueChangeListener(event -> updateList(dialog, field.getValue()));
        dialog.add(field);

	userGrid.addComponentColumn(user -> {
	    String imageUrl = user.getProfileImage();

            Avatar avatar = new Avatar();
            avatar.setImage(imageUrl);
            avatar.addClassName("view-avatar");

            Span fullName = new Span(user.getFullName());
            fullName.addClassName("view-firstname");

            HorizontalLayout  avatarDiv = new HorizontalLayout(avatar, fullName);
            avatarDiv.addClassName("view-avatar-div");
            avatarDiv.addClickListener(event -> {
                inputField.setValue("@" + user.getFullName());
                dialog.close();
            });

            return avatarDiv;
        }).setAutoWidth(false);

        dialog.add(userGrid);
    }

    private void updateList(Dialog dialog, String searchTerm) {
	List<User> users = userService.findAllUsers(searchTerm.replace("@", ""));

	userGrid.setItems(users);
    }

    private VerticalLayout createSingleComment(Artwork artwork, Button commentButton, Span commented){
        VerticalLayout commentLayout = new VerticalLayout();

	User user = userService.findCurrentUser();

	String imageUrl = user.getProfileImage();

        Avatar avatar = new Avatar();
        avatar.setImage(imageUrl);
        avatar.addClassName("comment-avatar");

	Div avatarDiv = new Div(avatar);
	avatarDiv.addClassName("comment-avatar-div");
	avatarDiv.addClickListener(event -> {
	    UI.getCurrent().navigate(UserProfile.class, user.getId());
	});

	Span name = new Span(user.getFullName());
	name.addClassName("comment-fullname");
	name.addClickListener(event -> {
            UI.getCurrent().navigate(UserProfile.class, user.getId());
	});

	String commentValue = inputField.getValue();

	if (commentValue.length() > 28) {
            commentValue = commentValue.replaceAll("(.{28})", "$1\n");
	}

	Span userComment = new Span(commentValue);

	userComment.addClassName("comment-comments");

	VerticalLayout layout1 = new VerticalLayout(name, userComment);
	layout1.addClassName("comment-layout1");

	HorizontalLayout layout2 = new HorizontalLayout(avatarDiv, layout1);

	// Save comment to database
	saveCommentToDatabase(user, artwork, commented);

	List<Comment> totalComments = commentService.getCommentsByArtworkId(artwork.getId());

	commentButton.setText(String.valueOf(totalComments.size()));

	Long commentId = totalComments.get(totalComments.size() - 1).getId();

	Comment comment = commentService.getCommentById(commentId);

	Span timeAgo = new Span();
        timeAgo.addClassName("comment-time-ago");

	VerticalLayout div = createCommentFooter(comment, timeAgo);
	div.addClassName("comment-div");

	updateCommentTime(comment, timeAgo);

	commentLayout.add(layout2, div);

        return commentLayout;
    }

    private void saveCommentToDatabase(User user, Artwork artwork, Span commented){
    	String email = user.getEmail();
        String fullName = user.getFullName();
        String comments = inputField.getValue();
        String userImage = user.getProfileImage();
        Long artworkId = artwork.getId();

        commentService.saveComment(email, fullName, comments, userImage, artworkId);

        List<Comment> commentss = commentService.getCommentsByArtworkId(artwork.getId());
        commented.setText(formatValue((long) commentss.size()) + " comments");
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

    private Upload createUploadImage(){
        Icon cameraIcon = new Icon(VaadinIcon.CAMERA);
        cameraIcon.addClassName("comment-camera-icon");

        Upload upload = new Upload(new MemoryBuffer());
        upload.setUploadButton(cameraIcon);
        upload.addClassName("comment-upload");
        upload.setAcceptedFileTypes("image/png");
        upload.addSucceededListener(event -> {
            MemoryBuffer buffer = (MemoryBuffer) upload.getReceiver();

            try {
                InputStream inputStream = buffer.getInputStream();
                byte[] bytes = inputStream.readAllBytes();
                String originalFilename = event.getFileName();

                StreamResource resource = new StreamResource(originalFilename, () -> new ByteArrayInputStream(bytes));

                String filePath = "/src/main/resources/META-INF/resources/comments_images/" + originalFilename;
                FileOutputStream outputStream = new FileOutputStream(filePath);
                //outputStream.write(bytes);

            } catch (IOException e) {
                Notification.show("Error uploading artwork image", 3000, Notification.Position.TOP_CENTER);
            }
        });

        return upload;
     }

    private void createHeader(User user, Artwork artwork){
        Span fullName = new Span(user.getFullName());
        fullName.addClassName("comment-first-name");

        Icon backIcon = new Icon(VaadinIcon.ARROW_BACKWARD);
        backIcon.addClassName("comment-back-icon");
        backIcon.addClickListener(event -> {
            Set<String> sessionNames = VaadinSession.getCurrent().getSession().getAttributeNames();

            for(String sessionName : sessionNames){
                if(sessionName.equals("main")){
                   VaadinSession.getCurrent().getSession().removeAttribute("main");
                   UI.getCurrent().navigate(MainFeed.class);

                }else if(sessionName.equals("profile")){
                   VaadinSession.getCurrent().getSession().removeAttribute("profile");
                   UI.getCurrent().navigate(UserProfile.class, user.getId());

                }else if(sessionName.equals("specific")){
                   VaadinSession.getCurrent().getSession().removeAttribute("specific");
                   UI.getCurrent().navigate(ViewSpecificArtwork.class, artwork.getId());
                }else if(sessionName.equals("savedpost")){
                   VaadinSession.getCurrent().getSession().removeAttribute("savedpost");
                   UI.getCurrent().navigate(SavedPostView.class);
                }else if(sessionName.equals("notification")){
                   VaadinSession.getCurrent().getSession().removeAttribute("notification");
                   UI.getCurrent().navigate(NotificationView.class);
                }
            }
        });

        addToNavbar(backIcon, fullName);
     }
}
