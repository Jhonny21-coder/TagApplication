package com.example.application.views.profile;

import com.example.application.data.SavedPost;
import com.example.application.data.Artwork;
import com.example.application.data.User;
import com.example.application.services.SavedPostService;
import com.example.application.services.UserServices;
import com.example.application.views.MainFeed;
import com.example.application.views.comment.CommentSectionView;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.button.Button;
import jakarta.annotation.security.PermitAll;

import java.util.List;

import java.io.FileInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;

@PermitAll
@Route("saved-post")
public class SavedPostView extends AppLayout {

   private final SavedPostService savedPostService;
   private final UserServices userService;
   private FormLayout formLayout = new FormLayout();

   public SavedPostView(SavedPostService savedPostService, UserServices userService){
   	this.savedPostService = savedPostService;
   	this.userService = userService;

	addClassName("profile-app-layout");

   	createMainLayout();
   	createHeader();
   }

   private void createMainLayout(){
   	User user = userService.findCurrentUser();

   	List<SavedPost> savedPosts = savedPostService.getSavedPostsByUserId(user.getId());
	savedPosts.forEach(savedPost -> {
	    String imageUrl = savedPost.getArtwork().getArtworkUrl();

	    Image artworkImage = new Image(imageUrl, "artwork-image");
	    artworkImage.addClassName("saved-image");

	    Span name = new Span(savedPost.getArtwork().getUser().getFullName());
	    name.addClassName("saved-name");

	    Span dateTime = new Span(savedPost.getDateTime());
	    dateTime.addClassName("saved-date-time");

	    VerticalLayout childLayout = new VerticalLayout(name, dateTime);
	    childLayout.addClassName("saved-child-layout");
	    childLayout.addClickListener(event -> {
	    	Long artworkId = savedPost.getArtwork().getId();

            	VaadinSession.getCurrent().getSession().setAttribute("savedpost", artworkId);

	    	UI.getCurrent().navigate(CommentSectionView.class, artworkId);
	    });

	    Icon moreIcon = new Icon(VaadinIcon.ELLIPSIS_V);
	    moreIcon.addClickListener(event -> {
	    	Dialog dialog = new Dialog();
		dialog.addClassName("unsave-dialog");

	    	createDialog(dialog, savedPost.getArtwork());
	    });

            HorizontalLayout parentLayout = new HorizontalLayout(artworkImage, childLayout, moreIcon);
            parentLayout.addClassName("saved-parent-layout");

            formLayout.add(parentLayout);
	});

	setContent(formLayout);
   }

   private void createDialog(Dialog dialog, Artwork artwork){
   	dialog.open();

   	HorizontalLayout unsaveButton = new HorizontalLayout(new Icon(VaadinIcon.TRASH), new Span("Unsave"));
   	unsaveButton.addClassName("saved-post-buttons");
   	unsaveButton.addClickListener(event -> {
   	    createDeleteSavedPost(artwork);
            dialog.close();
   	});

   	HorizontalLayout viewButton = new HorizontalLayout(new Icon(VaadinIcon.EYE), new Span("View Post"));
   	viewButton.addClassName("saved-post-buttons");
   	viewButton.addClickListener(event -> {
   	    dialog.close();

   	    Long artworkId = artwork.getId();

            VaadinSession.getCurrent().getSession().setAttribute("savedpost", artworkId);

            UI.getCurrent().navigate(CommentSectionView.class, artworkId);
   	});

   	HorizontalLayout shareButton = new HorizontalLayout(new Icon(VaadinIcon.ARROW_FORWARD), new Span("Share"));
   	shareButton.addClassName("saved-post-buttons");

   	dialog.add(new VerticalLayout(unsaveButton, viewButton, shareButton));
   }

   private void createDeleteSavedPost(Artwork artwork){
   	User user = userService.findCurrentUser();

   	savedPostService.deleteSavedPost(artwork.getId(), user.getId());

   	formLayout.removeAll();

   	createMainLayout();
   }

   private void createHeader(){
        Icon backButton = new Icon(VaadinIcon.ARROW_BACKWARD);
        backButton.addClassName("profile-back-button");
        backButton.addClickListener(event -> {
             UI.getCurrent().navigate(MainFeed.class);
        });

        Span text = new Span("Saved Artworks");
        text.addClassName("view-fullname");

        addToNavbar(new HorizontalLayout(backButton, text));
    }
}
