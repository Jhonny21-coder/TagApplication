package com.example.application.views.story;

import com.example.application.views.MainFeed;
import com.example.application.views.profile.OwnProfile;
import com.example.application.data.User;
import com.example.application.services.UserServices;
import com.example.application.data.story.Story;
import com.example.application.services.story.StoryService;
import com.example.application.config.CloudinaryService;

import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.server.VaadinSession;
import jakarta.annotation.security.PermitAll;

import java.util.Set;
import java.util.List;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.File;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@PermitAll
@Route("story-view")
public class StoryView extends AppLayout {

   private Button discardButton = new Button("Discard");
   private Image image = new Image();
   private Button textButton = new Button("Text");
   private TextField field = new TextField();
   private Button shareButton = new Button("Share now");
   private Span doneText = new Span("Done");
   private Span storyText = new Span();
   private Button eyeIcon = new Button();
   private boolean isOpened = true;
   private MemoryBuffer buffer = new MemoryBuffer();
   private Upload upload = new Upload(buffer);

   private final String STORY_FOLDER = "story_images";

   private final UserServices userService;
   private final StoryService storyService;
   private final CloudinaryService cloudinaryService;

   public StoryView(UserServices userService, StoryService storyService,
   	CloudinaryService cloudinaryService){
   	this.userService = userService;
   	this.storyService = storyService;
   	this.cloudinaryService = cloudinaryService;

	addClassName("story-app-layout");

   	createFooter();
   	createHeader();
   	createMainLayout();
   }

   private VerticalLayout createPreviousStoryLayout(){
	User user = userService.findCurrentUser();

	List<Story> stories = storyService.getUserStories(user);

	VerticalLayout mainLayout = new VerticalLayout();
	mainLayout.addClassName("previous-main-layout");

	HorizontalLayout rowLayout = new HorizontalLayout();
	rowLayout.addClassName("previous-row-layout");
	mainLayout.add(rowLayout);

	for(int i = 0; i < stories.size(); i++){
	   Story story = stories.get(i);

	   String imageUrl = story.getImageUrl();

	   Image image = new Image(imageUrl, "story-image");
	   image.addClassName("previous-images");

	   Div imageDiv = new Div(image);
	   imageDiv.addClassName("story-previous-images");

	   rowLayout.add(imageDiv);

	   if((i + 1) % 2 == 0 || i == stories.size() - 1){
	      rowLayout = new HorizontalLayout();
	      mainLayout.add(rowLayout);
	   }

	   if(i > 3){
	      image.setVisible(false);
	   }
	}

	Span moreSpan = new Span("View previous stories");

	mainLayout.add(moreSpan);
   	return mainLayout;
   }

   private void createMainLayout(){
   	FormLayout formLayout = new FormLayout();

   	VerticalLayout textLayout = new VerticalLayout(eyeIcon, storyText);
        textLayout.addClassName("story-text-layout");
	textLayout.setVisible(false);

	image.setVisible(false);
	image.addClassName("story-image");

	textButton.setVisible(false);
	textButton.setIcon(new Icon(VaadinIcon.TEXT_LABEL));
	textButton.addClassName("story-text-button");

	field.setVisible(false);
	field.addClassName("story-field");
	field.setValueChangeMode(ValueChangeMode.EAGER);
	field.setPlaceholder("Write a text...");

	storyText.addClassName("story-text");

	eyeIcon.setIcon(new Icon(VaadinIcon.EYE));
	eyeIcon.setVisible(false);
	eyeIcon.addClassName("story-eye-icon");
	eyeIcon.addClickListener(event -> {
	    if(!isOpened){
	       storyText.setVisible(true);

	       isOpened = true;
	       eyeIcon.setIcon(new Icon(VaadinIcon.EYE));
	    }else{
	       storyText.setVisible(false);

	       isOpened = false;
	       eyeIcon.setIcon(new Icon(VaadinIcon.EYE_SLASH));
	    }
	});

   	upload.addClassName("story-upload");
   	upload.setAcceptedFileTypes("image/png", "image/jpeg", "video/mp4", "video/webm", "video/ogg");
   	upload.addSucceededListener(event -> {
   	    /*upload.setVisible(false);
   	    discardButton.setVisible(true);
   	    image.setVisible(true);
   	    textButton.setVisible(true);

   	    MemoryBuffer _buffer = (MemoryBuffer) upload.getReceiver();

   	    String fileName = event.getFileName();

            try {
                InputStream inputStream = _buffer.getInputStream();
                byte[] bytes = inputStream.readAllBytes();

                StreamResource resource = new StreamResource(fileName, () -> new ByteArrayInputStream(bytes));

                image.setSrc(resource);
            } catch (IOException e) {
                Notification.show("Error uploading artwork image", 3000, Notification.Position.TOP_CENTER);
            }*/

            try {
                InputStream inputStream = buffer.getInputStream();

                if (inputStream.available() > 0) {
                   // Convert InputStream to ByteArrayOutputStream
                   ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                   inputStream.transferTo(byteArrayOutputStream); // Copy data from InputStream to ByteArrayOutputStream

                   String mimeType = "";

                   if(event.getMIMEType().startsWith("image/")){
                      mimeType = "IMAGE";
                   }else if(event.getMIMEType().startsWith("video/")){
                      mimeType = "VIDEO";
                   }

		   // Store ByteArrayOutputStream in session
                   VaadinSession.getCurrent().getSession().setAttribute("singleUpload", byteArrayOutputStream);
                   UI.getCurrent().navigate(EditStoryView.class, mimeType);
                } else {
                   Notification.show("Profile photo is empty.");
                }
            } catch (Exception e) {
                Notification.show(e.getMessage());
            }
   	});

	discardButton.setVisible(false);
	discardButton.addClassName("story-discard-button");
   	discardButton.addClickListener(event -> {
            upload.setVisible(true);
            upload.clearFileList();
            discardButton.setVisible(false);
            image.setVisible(false);
            textButton.setVisible(false);
            field.setVisible(false);
            doneText.setVisible(false);
            textLayout.setVisible(false);
            eyeIcon.setVisible(false);

            field.setValue("");
        });

        field.addValueChangeListener(event -> {
            if(!event.getValue().isEmpty() && !event.getValue().matches("\\s*")){
               textButton.setIcon(new Icon(VaadinIcon.CHECK));
            }else{
               textButton.setIcon(new Icon(VaadinIcon.TEXT_LABEL));
            }
        });

        textButton.addClickListener(event -> {
            textButton.setVisible(true);
            image.removeClassName("story-image");
            image.addClassName("story-image2");
            field.setVisible(true);
            doneText.setVisible(true);
        });

	Button uploadButton = new Button("Single", new Icon("lumo", "photo"));
	uploadButton.addClassName("story-upload-button");

   	upload.setUploadButton(uploadButton);

   	Div div = new Div(image, field);
   	div.addClassName("story-div");

	doneText.setVisible(false);
   	doneText.addClassName("story-done-text");
   	doneText.addClickListener(event -> {
            image.removeClassName("story-image2");
            image.addClassName("story-image");
            field.setVisible(false);
            textButton.setIcon(new Icon(VaadinIcon.TEXT_LABEL));
            doneText.setVisible(false);
            eyeIcon.setVisible(true);

            if(!field.getValue().isEmpty() && !field.getValue().matches("\\s*")){
               storyText.setText(field.getValue());
               textLayout.setVisible(true);
            }else{
               textLayout.setVisible(false);
            }
        });

	MultiFileMemoryBuffer multiBuffer = new MultiFileMemoryBuffer();
        Upload multiUpload = new Upload(multiBuffer);
        multiUpload.addClassName("story-multi-upload");
        multiUpload.setAcceptedFileTypes("image/png", "image/jpeg", "video/mp4", "video/webm", "video/ogg");

        Button multiButton = new Button("Multiple", new Icon("vaadin", "picture"));
        multiButton.addClassName("upload-multi-button");
        multiUpload.setUploadButton(multiButton);

        Button _textButton = new Button("Text", new Icon("vaadin", "text-label"));
        _textButton.addClassName("story-main-text-button");

        HorizontalLayout uploadLayout = new HorizontalLayout(upload, multiUpload, _textButton);
        uploadLayout.addClassName("story-upload-image-layout");

        HorizontalLayout previousLayout = new HorizontalLayout(new Span("Recent stories"));
	previousLayout.addClassName("story-previous-layout");

   	formLayout.add(uploadLayout, previousLayout, createPreviousStoryLayout()/*, image, new Div(field, doneText), textLayout*/);

   	setContent(formLayout);
   }

   private void createFooter(){
   	shareButton.setIcon(new Icon(VaadinIcon.SHARE));
   	shareButton.addClassName("story-share-button");
   	shareButton.addClickListener(event -> {
   	    String content = field.getValue();
   	    User user = userService.findCurrentUser();

            try {
            	try (InputStream inputStream = buffer.getInputStream()) {
                     // Create a temporary file to store the uploaded image
                     File tempFile = File.createTempFile("tempImage", ".png");
                     tempFile.deleteOnExit(); // Ensure the file is deleted on exit

                     // Save the uploaded image to the temporary file
                     FileOutputStream fos = new FileOutputStream(tempFile);
                     byte[] imageBytes = inputStream.readAllBytes();
                     fos.write(imageBytes);

            	     // Upload the image to Cloudinary
            	     String imageUrl = cloudinaryService.uploadImage(tempFile, STORY_FOLDER);

            	     Story story = new Story(content, imageUrl, user, "ACTIVE");

            	     storyService.saveStory(story);
            	     UI.getCurrent().navigate(MainFeed.class);
            	}
            }catch(Exception e){
                Notification.show("Error saving artwork image", 3000, Notification.Position.TOP_CENTER);
            }
   	});

   	HorizontalLayout layout = new HorizontalLayout(shareButton, textButton);
   	layout.addClassName("story-footer-layout");

   	//addToNavbar(true, layout);
   }

   private void createHeader(){
   	Icon closeIcon = new Icon("lumo", "cross");
   	closeIcon.addClassName("header-back-button");
   	closeIcon.addClickListener(event -> {
   	    Set<String> sessionNames = VaadinSession.getCurrent().getSession().getAttributeNames();

            for(String sessionName : sessionNames){
                if(sessionName.equals("own-profile")){
                   VaadinSession.getCurrent().getSession().removeAttribute("own-profile");
                   UI.getCurrent().navigate(OwnProfile.class);
                }else{
                   UI.getCurrent().navigate(MainFeed.class);
                }
            }
   	});

   	Span text = new Span("Add story");
   	text.addClassName("story-header-text");

        HorizontalLayout layout = new HorizontalLayout(closeIcon, text, discardButton);
        layout.addClassName("story-header-layout");

   	addToNavbar(layout);
   }
}
