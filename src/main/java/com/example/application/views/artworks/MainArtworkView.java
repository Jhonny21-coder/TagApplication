package com.example.application.views.artworks;

import com.example.application.views.MainFeed;
import com.example.application.data.Artwork;
import com.example.application.data.LikeReaction;
import com.example.application.data.HeartReaction;
import com.example.application.data.User;
import com.example.application.services.UserServices;
import com.example.application.services.ArtworkService;
import com.example.application.services.LikeReactionService;
import com.example.application.services.HeartReactionService;
import com.example.application.data.SavedPost;
import com.example.application.services.SavedPostService;
import com.example.application.data.PostReaction;
import com.example.application.services.PostReactionService;
import com.example.application.services.CommentService;
import com.example.application.data.Comment;
import com.example.application.services.CommentReactionService;
import com.example.application.services.ReplyService;
import com.example.application.config.CloudinaryService;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.contextmenu.HasMenuItems;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.data.value.ValueChangeMode;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.File;

import java.util.List;

import java.net.URLEncoder;

import java.nio.charset.StandardCharsets;

@PermitAll
@Route("main-artworks")
public class MainArtworkView extends AppLayout {

    private final ArtworkService artworkService;
    private final UserServices userService;
    private final LikeReactionService likeReactionService;
    private final HeartReactionService heartReactionService;
    private final SavedPostService savedPostService;
    private final PostReactionService postService;
    private final CommentService commentService;
    private final CommentReactionService commentReactionService;
    private final ReplyService replyService;
    private final CloudinaryService cloudinaryService;

    private VerticalLayout artworksLayout = new VerticalLayout();
    private StreamResource resource;
    private final Span artworkUrlText = new Span("New Artwork Image");
    private final Upload upload = new Upload(new MemoryBuffer());
    private final TextField title = new TextField("Enter New Title");
    private final Button save = new Button("Save");
    private final Button close = new Button("Close");
    private final FormLayout formLayout = new FormLayout();
    private final TextField searchField = new TextField();

    private String newFilename;
    private byte[] bytes;
    private boolean isClicked = true;
    private String searchTerm;

    public MainArtworkView(ArtworkService artworkService, UserServices userService,
    	LikeReactionService likeReactionService, HeartReactionService heartReactionService,
    	SavedPostService savedPostService, PostReactionService postService,
    	CommentService commentService, CommentReactionService commentReactionService,
    	ReplyService replyService, CloudinaryService cloudinaryService){

    	this.artworkService = artworkService;
    	this.userService = userService;
    	this.likeReactionService = likeReactionService;
    	this.heartReactionService = heartReactionService;
    	this.savedPostService = savedPostService;
    	this.postService = postService;
    	this.commentService = commentService;
    	this.commentReactionService = commentReactionService;
    	this.replyService = replyService;
    	this.cloudinaryService = cloudinaryService;

    	addClassName("profile-app-layout");
	artworkUrlText.addClassName("add-text");
        upload.addClassName("register-upload");
        title.addClassName("register-field");
	save.addClassName("save-artwork");
	close.addClassName("close-artwork");

    	displayArtworks();
    	createHeader();
    }

    public void displaySearchedArtworks(){
    	User user = userService.findCurrentUser();

    	List<Artwork> searches = artworkService.searchArtworks(searchTerm.trim(), user.getId());

	if(searches == null || searches.isEmpty()){
           Span noSearched = new Span("No results found");
           noSearched.addClassName("main-no-search");
           formLayout.add(noSearched);
	}else{
           for(Artwork artwork : searches){
	       Div seperator = new Div();
               seperator.addClassName("seperator");

               String description = artwork.getDescription();

               if(description.length() > 37) {
                  description = description.replaceAll("(.{37})", "$1\n");
               }

               H1 artworkDescription = new H1(description);
               artworkDescription.addClassName("main-artwork-title");

               Span artworkDateTime = new Span(artwork.getDateTime());
               artworkDateTime.addClassName("artwork-date-time");

               String filename = "src/main/resources/META-INF/resources/artwork_images/" + artwork.getArtworkUrl();

	       String imageUrl = artwork.getArtworkUrl();

               Image artworkImage = new Image(imageUrl, "artwork-image");
               artworkImage.addClassName("main-artwork-image");

               ConfirmDialog dialog = new ConfirmDialog();
               dialog.addClassName("delete-follower-dialog");

               HorizontalLayout buttonsLayout = createButtons(dialog, artwork);

               VerticalLayout titleTimeLayout = new VerticalLayout();
               titleTimeLayout.add(artworkDescription, artworkDateTime);
               titleTimeLayout.addClassName("main-title-time-layout");

	       HorizontalLayout totalReactionsLayout = createTotalReactions(artwork);

               formLayout.add(titleTimeLayout, artworkImage, totalReactionsLayout, buttonsLayout);
               formLayout.setResponsiveSteps(new ResponsiveStep("0", 1), new ResponsiveStep("500px", 3));

               deleteArtwork(dialog, artwork, artworkDescription, artworkImage, formLayout);
            }
    	}

    	setContent(formLayout);
    }

    private void displayArtworks() {
    	User user = userService.findCurrentUser();

	List<Artwork> artworks = artworkService.getArtworksByUserId(user.getId());

	if(artworks == null || artworks.isEmpty()){
           Span noArtworkText = new Span("No artworks yet. Click above button to add a new artwork.");
           noArtworkText.addClassName("main-no-search");
           formLayout.add(noArtworkText);
	}else{
    	   for(Artwork artwork : artworks){
    	       	Div seperator = new Div();
               	seperator.addClassName("seperator");

	       	if(artwork.getArtworkUrl().equals(artworks.get(artworks.size() - 1).getArtworkUrl())) {
		   seperator.setVisible(false);
	       	}

		String description = artwork.getDescription();

                if(description.length() > 37) {
		   description = description.replaceAll("(.{37})", "$1\n");
		}

		H1 artworkDescription = new H1(description);
		artworkDescription.addClassName("main-artwork-title");

		Span artworkDateTime = new Span(artwork.getDateTime());
		artworkDateTime.addClassName("artwork-date-time");

		String filename = "src/main/resources/META-INF/resources/artwork_images/" + artwork.getArtworkUrl();

	        String imageUrl = artwork.getArtworkUrl();

		Image artworkImage = new Image(imageUrl, "artwork-image");
		artworkImage.addClassName("main-artwork-image");

		ConfirmDialog dialog = new ConfirmDialog();
		dialog.addClassName("delete-follower-dialog");

		HorizontalLayout buttonsLayout = createButtons(dialog, artwork);

		VerticalLayout titleTimeLayout = new VerticalLayout();
		titleTimeLayout.add(artworkDescription, artworkDateTime);
		titleTimeLayout.addClassName("main-title-time-layout");

		HorizontalLayout totalReactionsLayout = createTotalReactions(artwork);

		formLayout.add(titleTimeLayout, artworkImage, totalReactionsLayout, buttonsLayout);
		formLayout.setResponsiveSteps(new ResponsiveStep("0", 1),new ResponsiveStep("500px", 3));

		deleteArtwork(dialog, artwork, artworkDescription, artworkImage, formLayout);
    	   }
    	}

    	setContent(formLayout);
    }

    private HorizontalLayout createTotalReactions(Artwork artwork){
    	List<PostReaction> reactions = postService.getPostReactionsByArtworkId(artwork.getId());

    	Icon likeIcon = new Icon(VaadinIcon.THUMBS_UP);
    	likeIcon.addClassName("main-artwork-icons");

    	Icon heartIcon = new Icon(VaadinIcon.HEART);
    	heartIcon.addClassName("main-artwork-icons");

    	Icon smileyIcon = new Icon(VaadinIcon.SMILEY_O);
    	smileyIcon.addClassName("main-artwork-icons");

    	Span totalReactionsText = new Span(String.valueOf(reactions.size()));
    	totalReactionsText.addClassName("main-artwork-reactions");

    	HorizontalLayout totalReactionsLayout = new HorizontalLayout(likeIcon, heartIcon, smileyIcon, totalReactionsText);
    	totalReactionsLayout.addClassName("main-artwork-total-layout");

    	if(reactions.isEmpty()){
    	   totalReactionsLayout.setVisible(false);
    	}

    	return totalReactionsLayout;
    }

    private void closeSearchField(){
    	searchField.setVisible(false);
    	isClicked = true;
    }

    private void createSearchField(){
    	searchField.setVisible(true);
    	searchField.setPlaceholder("Search by title...");
    	searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.addClassName("view-search-field");
        searchField.setClearButtonVisible(true);
        isClicked = false;
    }

    private void createHeader(){
    	searchField.setVisible(false);
    	searchField.setValueChangeMode(ValueChangeMode.EAGER);
    	searchField.addValueChangeListener(event -> {
    	    searchTerm = searchField.getValue();
	    formLayout.removeAll();
    	    displaySearchedArtworks();
    	});

    	Button addButton = new Button("Add new artwork", new Icon(VaadinIcon.PLUS_CIRCLE_O));
        addButton.addClassName("add-artwork-button");
        addButton.addClickListener(event -> {
            getUI().ifPresent(ui -> ui.navigate(AddArtwork.class));
        });

    	Icon searchIcon = new Icon(VaadinIcon.SEARCH);
        searchIcon.addClassName("search-button");
        searchIcon.addClickListener(event -> {
             if(!isClicked){
             	closeSearchField();
             }else{
	    	createSearchField();
	    }
        });

        Icon backIcon = new Icon(VaadinIcon.ARROW_BACKWARD);
        backIcon.addClassName("back-icon");
        backIcon.addClickListener(event -> {
            getUI().ifPresent(ui -> ui.navigate(MainFeed.class));
        });

        HorizontalLayout iconsLayout = new HorizontalLayout();
        iconsLayout.add(backIcon, addButton, searchIcon);

        VerticalLayout parentLayout = new VerticalLayout(iconsLayout, searchField);

        addToNavbar(parentLayout);
    }

    public HorizontalLayout createButtons(ConfirmDialog dialog, Artwork artwork){

	Button deleteButton = new Button("Delete", new Icon(VaadinIcon.TRASH));
	deleteButton.addClassName("artwork-delete");
	deleteButton.addClickListener(event -> {
            dialog.open();
        });

        Button editButton = new Button("Edit", new Icon(VaadinIcon.EDIT));
        editButton.addClassName("artwork-edit");
        editButton.addClickListener(event -> {
           Long artworkId = artwork.getId();
           UI.getCurrent().navigate(EditArtwork.class, artworkId);
        });

	MenuBar menuBar = new MenuBar();
	menuBar.addClassName("menu-bar");

	VaadinIcon moreIcon = VaadinIcon.ELLIPSIS_DOTS_H;
	VaadinIcon shareIcon = VaadinIcon.SHARE;
	VaadinIcon downloadIcon = VaadinIcon.DOWNLOAD;

	MenuItem more = createIconItem(menuBar, moreIcon, "More", null);

	SubMenu moreSubMenu = more.getSubMenu();

	MenuItem share = createIconItem(moreSubMenu, shareIcon, "Share as link", null, true);
        MenuItem download = createIconItem(moreSubMenu, downloadIcon, "Download as PDF", null, true);

        menuIconListener(share, download, artwork);

        HorizontalLayout buttonsLayout = new HorizontalLayout(deleteButton, editButton, menuBar);
        buttonsLayout.addClassName("main-artwork-buttons");

        return buttonsLayout;
    }

    private void menuIconListener(MenuItem share, MenuItem download, Artwork artwork){
        share.addClickListener(event -> {
            String imageUrl = "https://summary-spider-internally.ngrok-free.app/shared-artwork/" + artwork.getId();
            //String imageUrl = "http://localhost:8080/shared-artwork/" + artwork.getId();

            openShareDialog(imageUrl);
        });

        download.addClickListener(event -> {
             String imageUrl = artwork.getArtworkUrl();
	     String title = artwork.getDescription();

             openDownloadDialog(imageUrl, title);
        });
    }

    private void openDownloadDialog(String imageUrl, String title) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.addClassName("delete-follower-dialog");
        dialog.setCancelable(false);
        dialog.setConfirmText("Close");
        dialog.setHeader("Download Artwork");

        String downloadLink = "/download-pdf?imageUrl=" + imageUrl + "&title=" + title;

        Anchor downloadAnchor = new Anchor(downloadLink, "Download PDF");
        downloadAnchor.getElement().setAttribute("download", true);
	downloadAnchor.addClassName("download-anchor");

	Icon downloadIcon = new Icon(VaadinIcon.DOWNLOAD);
	downloadIcon.addClassName("download-icon");

        HorizontalLayout horizontal = new HorizontalLayout(downloadIcon, downloadAnchor);
	horizontal.addClassName("horizontal");

	Span artworkUrl = new Span(title + ".png");
	artworkUrl.addClassName("download-url");

        VerticalLayout vertical = new VerticalLayout(artworkUrl, horizontal);
        dialog.add(vertical);
        dialog.open();
    }

    private MenuItem createIconItem(HasMenuItems menu, VaadinIcon iconName,
            String label, String ariaLabel) {
        return createIconItem(menu, iconName, label, ariaLabel, false);
    }

    private MenuItem createIconItem(HasMenuItems menu, VaadinIcon iconName,
            String label, String ariaLabel, boolean isChild) {

        Icon icon = new Icon(iconName);

        if (isChild) {
            icon.getStyle().set("width", "var(--lumo-icon-size-s)");
            icon.getStyle().set("height", "var(--lumo-icon-size-s)");
            icon.getStyle().set("marginRight", "var(--lumo-space-s)");
        }

        MenuItem item = menu.addItem(icon, e -> {
        });

        if (ariaLabel != null) {
            item.setAriaLabel(ariaLabel);
        }

        if (label != null) {
            item.add(new Text(label));
        }

        return item;
    }

    private void openShareDialog(String imageUrl) {
        ConfirmDialog shareDialog = new ConfirmDialog();
	shareDialog.addClassName("delete-follower-dialog");
	shareDialog.setConfirmText("Close");
	shareDialog.setCancelable(false);
        shareDialog.setHeader("Copy & Share");

        TextField urlField = new TextField();
        urlField.addClassName("url-field");
        urlField.setValue(imageUrl);
        urlField.setWidth("100%");
        urlField.setReadOnly(true);

        Button copyButton = new Button("Copy Link", new Icon(VaadinIcon.CLIPBOARD));
        copyButton.addClassName("copy-button");
        copyButton.addClickListener(event -> {
            copyToClipboard(urlField.getValue());
            Notification.show("Link copied to clipboard", 1000, Notification.Position.MIDDLE)
            	.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });

        VerticalLayout dialogLayout = new VerticalLayout(urlField, copyButton);
        shareDialog.add(dialogLayout);
        shareDialog.open();
    }

    private void copyToClipboard(String text) {
        String jsCode = "navigator.clipboard.writeText('" + text + "').then(function() { "
                + "console.log('Copying to clipboard was successful!'); "
                + "}, function(err) { "
                + "console.error('Could not copy text: ', err); "
                + "});";
        UI.getCurrent().getPage().executeJs(jsCode);
    }

    public void deleteArtwork(ConfirmDialog dialog, Artwork artwork,
    	H1 artworkDescription, Image artworkImage, FormLayout formLayout){

	dialog.setCancelable(true);
	dialog.setConfirmText("Delete");
	dialog.setConfirmButtonTheme("error primary");
        dialog.setText("Are you sure you want to delete this artwork?");
        dialog.addConfirmListener(events -> {
            List<LikeReaction> likeReactions = likeReactionService.getReactionForArtworkId(artwork.getId());
            List<HeartReaction> heartReactions = heartReactionService.getReactionForArtworkId(artwork.getId());

            for(LikeReaction likeReaction : likeReactions){
		if(likeReaction != null){
                   likeReactionService.deleteLikeReactions(likeReaction);
                }
	    }

	    for(HeartReaction heartReaction : heartReactions){
		if(heartReaction != null){
                   heartReactionService.deleteHeartReactions(heartReaction);
                }
            }

            Long artworkId = artwork.getId();
            Long userId = artwork.getUser().getId();

            SavedPost savedPost = savedPostService.getSavedPostByArtworkAndUserId(artworkId, userId);

            if(savedPost != null){
               savedPostService.deleteSavedPost(artworkId, userId);
            }

            List<Comment> comments = commentService.getCommentsByArtworkId(artworkId);

            if(!comments.isEmpty()){
                comments.forEach(comment -> {
                    Long commentId = comment.getId();
                    commentReactionService.deleteCommentReactionsByCommentId(commentId);
                    replyService.deleteRepliesByCommentId(commentId);
                });
            }

	    String filePath = "src/main/resources/META-INF/resources/artwork_images/" + artwork.getArtworkUrl();

            File file = new File(filePath);

            if(file.exists()){
               if(file.delete()){
                  System.out.println("File deleted successfully.");
               }else{
                  System.out.println("Failed to delete the file.");
               }
            }else{
               System.out.println("File does not exist. Cannot delete.");
            }

	    commentService.deleteCommentsByArtworkId(artworkId);
	    postService.deletePostReactionsByArtworkId(artworkId);
            artworkService.deleteArtwork(artwork);

            String imageUrl = artwork.getArtworkUrl();
            cloudinaryService.deleteImage(imageUrl);

            refreshLayout();
	});
    }

    private void refreshLayout(){
    	formLayout.removeAll();

    	displayArtworks();
    }
}
