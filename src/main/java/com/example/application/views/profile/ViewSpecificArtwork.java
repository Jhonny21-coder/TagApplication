package com.example.application.views.profile;

import com.example.application.data.User;
import com.example.application.services.UserServices;
import com.example.application.data.Artwork;
import com.example.application.services.ArtworkService;
import com.example.application.data.LikeReaction;
import com.example.application.services.LikeReactionService;
import com.example.application.data.HeartReaction;
import com.example.application.services.HeartReactionService;
import com.example.application.data.Comment;
import com.example.application.services.CommentService;
import com.example.application.views.comment.CommentSectionView;
import com.example.application.data.PostReaction;
import com.example.application.services.PostReactionService;

import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.dialog.Dialog;
import jakarta.annotation.security.PermitAll;

import java.io.FileInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicBoolean;

import com.vaadin.flow.server.VaadinSession;

@PermitAll
@Route("view-specific-artwork")
public class ViewSpecificArtwork extends AppLayout implements HasUrlParameter<Long> {

    private final ArtworkService artworkService;
    private final LikeReactionService likeService;
    private final HeartReactionService heartService;
    private final CommentService commentService;
    private final UserServices userService;
    private final PostReactionService postService;

    public ViewSpecificArtwork(ArtworkService artworkService,LikeReactionService likeService,
        HeartReactionService heartService, CommentService commentService, UserServices userService,
        PostReactionService postService){

    	this.artworkService = artworkService;
	this.likeService = likeService;
        this.heartService = heartService;
        this.commentService = commentService;
        this.userService = userService;
        this.postService = postService;

    	addClassName("specific-app-layout");
    }

    @Override
    public void setParameter(BeforeEvent event, Long artworkId){
        Artwork artwork = artworkService.getArtworkById(artworkId);
	User user = artwork.getUser();

        displayArtwork(artwork);
        createHeader(user);
        createFooter(user, artwork);
    }

    public void displayArtwork(Artwork artwork){
    	FormLayout formLayout = new FormLayout();
	formLayout.addClassName("specific-form-layout");

	String imageUrl = artwork.getArtworkUrl();

    	Image image = new Image(imageUrl, "artwork-image");
	image.addClassName("specific-image");

	formLayout.add(image);

	setContent(formLayout);
    }

    private void createHeader(User user){
        Icon backButton = new Icon(VaadinIcon.ARROW_BACKWARD);
        backButton.addClassName("profile-back-button");
        backButton.addClickListener(event -> {
             UI.getCurrent().navigate(ArtworkImages.class, user.getId());
        });

        Span fullName = new Span(user.getFullName());
        fullName.addClassName("view-fullname");

        addToNavbar(new HorizontalLayout(backButton, fullName));
    }

    private void createFooter(User user, Artwork artwork){
        Span totalReactions = new Span();
	totalReactions.addClassName("reacted");

	Span likes = new Span();
	likes.addClassName("specific-reacts");

	Span hearts = new Span();
	hearts.addClassName("specific-reacts");

	Span smiles = new Span();
	smiles.addClassName("specific-reacts");

	Button likeButton = new Button();
	likeButton.addClassName("specific-reaction-button");
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
	shareButton.addClassName("specific-share-button");

	HorizontalLayout buttonsLayout = new HorizontalLayout(likeButton, commentButton, shareButton);
	buttonsLayout.addClassName("specific-buttons-layout");

        addToNavbar(true, buttonsLayout);
    }

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
    	Artwork artwork, String colorTheme, Span totalReactions, Span likes, Span hearts, Span smiles,
        Icon likeIcon, Icon heartIcon, Icon happyIcon){

        User currentUser = userService.findCurrentUser();

        Icon likeIcon2 = new Icon(VaadinIcon.THUMBS_UP);
        likeIcon2.addClassName("feed-listener-like");

        Icon heartIcon2 = new Icon(VaadinIcon.HEART);
        heartIcon2.addClassName("feed-listener-heart");

        Icon happyIcon2 = new Icon(VaadinIcon.SMILEY_O);
        happyIcon2.addClassName("feed-listener-happy");

        if(!isReacted.get()){
           postService.savePostReaction(artwork, currentUser, reactType);
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

    private Button createCommentButtonListener(User user, Artwork artwork){
        List<Comment> comments = commentService.getCommentsByArtworkId(artwork.getId());

	Long totalComments = (long) comments.size();

        Button commentButton = new Button();
        commentButton.addClassName("specific-comment-button");
        commentButton.setIcon(new Icon(VaadinIcon.COMMENT_ELLIPSIS_O));
        commentButton.setText(formatValue(totalComments));
        commentButton.addClickListener(event -> {
            Long artworkId = artwork.getId();
            VaadinSession.getCurrent().getSession().setAttribute("specific", artworkId);
            UI.getCurrent().navigate(CommentSectionView.class, artwork.getId());
        });
        return commentButton;
    }

    /* Start format number */
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
    /* End format number */
}
