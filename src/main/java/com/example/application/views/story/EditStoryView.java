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
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.IFrame;
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

import elemental.json.JsonObject;
import elemental.json.Json;

import java.util.Set;
import java.util.List;
import java.util.Base64;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.File;

@PermitAll
@Route("story-view")
public class EditStoryView extends AppLayout implements HasUrlParameter<String> {

   private Image image = new Image();
   private IFrame videoFrame = new IFrame();

   private Icon textButton = new Icon("vaadin", "text-label");
   private TextField textField = new TextField();

   private Span doneText = new Span("Done");
   private Span storyText = new Span();
   private Span contentText = new Span();

   private Button discardButton = new Button("Discard");
   private Button shareButton = new Button("Share now");
   private Button doneButton = new Button("Done");

   private HorizontalLayout textLayout = new HorizontalLayout();

   private MemoryBuffer buffer = new MemoryBuffer();
   private Upload upload = new Upload(buffer);

   private final String IMAGE_FOLDER = "story_images";
   private final String VIDEO_FOLDER = "story_videos";

   private final UserServices userService;
   private final StoryService storyService;
   private final CloudinaryService cloudinaryService;

   private byte[] bytes = null;

   public EditStoryView(UserServices userService, StoryService storyService,
   	CloudinaryService cloudinaryService){
   	this.userService = userService;
   	this.storyService = storyService;
   	this.cloudinaryService = cloudinaryService;

	addClassName("story-app-layout");

	image.addClassName("story-edit-image");
	videoFrame.addClassName("story-edit-video");
	textButton.addClassName("story-text-button");
	contentText.addClassName("story-edit-content-text");

   	createMainLayout();
   }

   @Override
   public void setParameter(BeforeEvent event, String mimeType){
   	handleUploadSucceeded(mimeType);
   }

   private void handleUploadSucceeded(String mimeType) {
    	try {
            // Choose the correct session attribute based on image type
            String sessionAttribute = "singleUpload";

            // Retrieve the uploaded image bytes from session
            ByteArrayOutputStream outputStream = (ByteArrayOutputStream) VaadinSession.getCurrent().getSession().getAttribute(sessionAttribute);
            //byte[] bytes = null;

            if (outputStream != null) {
            	bytes = outputStream.toByteArray();

            	if ("IMAGE".equalsIgnoreCase(mimeType)) {
                    String imageBase64 = Base64.getEncoder().encodeToString(bytes);
                    String imgUrl = "data:image/jpeg;base64," + imageBase64;

                    // Display the image
                    image.setSrc(imgUrl);
                    image.setVisible(true); // Show the image component
                    image.setId("fabricImage");
                    videoFrame.setVisible(false); // Hide the video frame if it was previously visible
            	} else if ("VIDEO".equalsIgnoreCase(mimeType)) {
                    String videoBase64 = Base64.getEncoder().encodeToString(bytes);
                    String videoUrl = "data:video/mp4;base64," + videoBase64;

                    /*// Create a temporary file to store the uploaded image
        	    File tempFile = File.createTempFile("tempVideo", ".mp4");
        	    tempFile.deleteOnExit(); // Ensure the file is deleted on exit

        	    // Save the uploaded image to the temporary file
        	    try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            	    	fos.write(bytes);
        	    }*/

        	    StreamResource resource = new StreamResource("story-video", () -> new ByteArrayInputStream(bytes));

                    // Display video in the frame
        	    videoFrame.setSrc("https://res.cloudinary.com/datiflrbo/video/upload/v1728202862/Picsart_23-12-18_19-39-54-682_rbzwld.mp4");
        	    videoFrame.getElement().setAttribute("frameborder", "0");
        	    videoFrame.getElement().setAttribute("allowfullscreen", "true");
                    videoFrame.setVisible(true); // Show the video frame
                    image.setVisible(false); // Hide the image component if it was previously visible
            	} else {
                    Notification.show("Unsupported file type", 3000, Notification.Position.TOP_CENTER);
            	}
            } else {
            	Notification.show("No file found", 3000, Notification.Position.TOP_CENTER);
            }
    	} catch (Exception e) {
            Notification.show("Error processing the file", 3000, Notification.Position.TOP_CENTER);
    	}
   }

   private void createMainLayout(){
   	FormLayout formLayout = new FormLayout();

   	Div canvasContainer = new Div();
   	canvasContainer.getElement().setProperty("innerHTML", "<canvas id='fabricCanvas' width='600' height='701'></canvas>");
   	canvasContainer.addClassName("canvas-container");

   	Icon closeIcon = new Icon("lumo", "cross");
   	closeIcon.addClassName("story-edit-close-icon");
   	closeIcon.addClickListener(event -> {
   	    UI.getCurrent().navigate(StoryView.class);
   	});

	shareButton.setIconAfterText(true);
        shareButton.setIcon(new Icon("vaadin", "paperplane"));
        shareButton.addClassName("story-share-button");
        shareButton.addClickListener(event -> {
	    getElement().executeJs("return captureImageProperties();")
    		.then(String.class, jsonResult -> {
        	System.out.println("JSON Result: " + jsonResult);

        	// Parse the JSON if necessary
        	JsonObject jsonObject = Json.parse(jsonResult);
        	double left = jsonObject.getNumber("left");
        	double top = jsonObject.getNumber("top");
        	double width = jsonObject.getNumber("width");
        	double height = jsonObject.getNumber("height");
        	double angle = jsonObject.getNumber("angle");

        	System.out.println("Left: " + left + ", Top: " + top);
        	System.out.println("Width: " + width + ", Height: " + height);
        	System.out.println("Angle: " + angle);

        	// Choose the correct session attribute based on image type
                String sessionAttribute = "singleUpload";

		try {
            	    // Retrieve the uploaded image bytes from session
            	    ByteArrayOutputStream outputStream = (ByteArrayOutputStream) VaadinSession.getCurrent().getSession().getAttribute(sessionAttribute);
            	    byte[] bytes = null;

            	    if (outputStream != null) {
                    	bytes = outputStream.toByteArray();

                    	File tempFile = File.createTempFile("tempImage", ".jpeg");
                    	tempFile.deleteOnExit(); // Ensure the file is deleted on exit

                    	// Save the uploaded image to the temporary file
                    	FileOutputStream fos = new FileOutputStream(tempFile);
                        fos.write(bytes);

			String content = "NO CONTENT";

                        // Upload the image to Cloudinary
                	String imageUrl = cloudinaryService.uploadImage(tempFile, IMAGE_FOLDER);

                	saveImageProperties(content, imageUrl, left, top, width, height, angle);

                	UI.getCurrent().navigate(MainFeed.class);
            	    }
            	}catch(Exception e){
            	   Notification.show(e.getMessage());
            	}
    	    });
        });

        Button settingIcon = new Button("Privacy", new Icon("vaadin", "cog-o"));
        settingIcon.addClassName("story-setting-icon");

        getElement().executeJs(
            "var canvas = new fabric.Canvas('fabricCanvas');" +
            "canvas.setWidth(canvas.wrapperEl.clientWidth);" +
            "canvas.setHeight(canvas.wrapperEl.clientHeight);" +
            "fabric.Image.fromURL($0.src, function(img) {" +
            "   img.scale(0.5);" +
            "   var aspectRatio = img.width / img.height;" +
            "   var newWidth = 400;" +
            "   var newHeight = newWidth / aspectRatio;" +
            "   img.set({scaleX: newWidth / img.width, scaleY: newHeight / img.height});" +
            "   canvas.add(img);" +
            "   canvas.setActiveObject(img);" + // Set the image as the active object
            "});" +
            "window.captureImageProperties = function() {" +
            "   var img = canvas.getObjects()[0];" +
            "   if (img) {" +
            "	    var left = img.left;" +
            "       var top = img.top;" +
            "       var width = img.width * img.scaleX;" + // Actual width considering scale
            "       var height = img.height * img.scaleY;" + // Actual height considering scale
            "       var angle = img.angle;" +
            "       console.log('Capturing image properties:', left, top, width, height, angle);" +
	    "       return JSON.stringify({left: left, top: top, width: width, height: height, angle: angle});" +
            "   }" +
            "   return JSON.stringify({ left: 0, top: 0, width: 0, height: 0, angle: 0 });" +
            "};", image.getElement()
        );

        handleAddText();

        textLayout.add(textField, doneButton);
        textLayout.addClassName("edit-story-add-text-layout");

        VerticalLayout buttonsLayout = new VerticalLayout(textButton, textLayout);
        buttonsLayout.addClassName("edit-story-buttons-layout");

   	formLayout.add(image, canvasContainer, videoFrame, closeIcon, buttonsLayout, settingIcon, shareButton, contentText);

   	setContent(formLayout);
   }

   // Server-side method that receives the image properties
   private void saveImageProperties(String content, String imageUrl, double left, double top, double width, double height, double angle) {
   	User user = userService.findCurrentUser();

	Story story = new Story(content, imageUrl, user, "ACTIVE");
	story.setLeft(left);
	story.setTop(top);
	story.setWidth(width);
	story.setHeight(height);
	storyService.saveStory(story);
   }

   private void handleAddText(){
   	/*doneButton.setVisible(false);
   	textField.setVisible(false);*/
   	contentText.setVisible(false);
   	textLayout.setVisible(false);
        textField.addClassName("edit-story-field");
        textField.setValueChangeMode(ValueChangeMode.EAGER);
        textField.setPlaceholder("Write a text...");

        textButton.addClickListener(event -> {
            /*textField.setVisible(true);
            doneButton.setVisible(true);*/
            textLayout.setVisible(true);
        });

        doneButton.addClickListener(event -> {
            textLayout.setVisible(false);
            contentText.setVisible(true);
            contentText.setText(textField.getValue());
        });
   }

   /*private void createHeader(){
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
   }*/
}
