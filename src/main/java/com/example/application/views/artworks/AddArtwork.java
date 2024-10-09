package com.example.application.views.artworks;

import com.example.application.views.UploadsI18N;
import com.example.application.views.MainLayout;
import com.example.application.data.StudentInfo;
import com.example.application.services.StudentInfoService;
import com.example.application.data.User;
import com.example.application.services.UserServices;
import com.example.application.data.Artwork;
import com.example.application.services.ArtworkService;
import com.example.application.config.CloudinaryService;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.*;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.component.icon.*;
import com.vaadin.flow.dom.DomEventListener;
import com.vaadin.flow.dom.DomEvent;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.UI;
import jakarta.annotation.security.PermitAll;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.File;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

@PermitAll
@Route("addArtwork")
public class AddArtwork extends AppLayout {

    private final StudentInfoService studentInfoService;
    private final UserServices userService;
    private final ArtworkService artworkService;
    private final CloudinaryService cloudinaryService;

    private final String ARTWORK_FOLDER = "artwork_images";

    private final Span artworkUrlText = new Span("Artwork Image");
    private final Upload upload = new Upload(new MemoryBuffer());
    private final Image uploadedImage = new Image();
    private final TextArea title = new TextArea();
    private final Button save = new Button("Save");
    private final Button close = new Button("Cancel");
    private String originalFilename;
    private final Span postButton = new Span("Post");
    private final Button mainPostButton = new Button("Post");

    public AddArtwork(StudentInfoService studentInfoService, UserServices userService,
		ArtworkService artworkService, CloudinaryService cloudinaryService) {
        this.studentInfoService = studentInfoService;
	this.userService = userService;
	this.artworkService = artworkService;
	this.cloudinaryService = cloudinaryService;

	createHeader();

	addClassName("add-artwork-app-layout");

	uploadedImage.setVisible(false);

	title.setSuffixComponent(new Icon(VaadinIcon.TEXT_LABEL));
	title.setPlaceholder("What's on your mind?");

	artworkUrlText.addClassName("add-text");
	upload.addClassName("artwork-upload");
	title.addClassName("add-artwork-area");
	uploadedImage.addClassName("uploaded-image");
	mainPostButton.addClassName("add-main-post-button");

	save.addClassName("save-artwork");
	save.setIcon(new Icon(VaadinIcon.CHECK));

	close.addClassName("close-artwork");
        close.setIcon(new Icon(VaadinIcon.CLOSE));

	HorizontalLayout uploadLayout = new HorizontalLayout();
	uploadLayout.setSpacing(false);
        uploadLayout.setAlignItems(FlexComponent.Alignment.CENTER);

	Icon uploadIcon = VaadinIcon.FILE_ADD.create();

        // Create the text
        Span hint = new Span("Accepted images formats: (png, jpeg)");
        Span hint2 = new Span(")");
        hint.addClassName("hint");
        hint2.addClassName("hin2");

        Span uploadLabel = new Span("Upload artwork (");

        // Add both the icon and text to the HorizontalLayout
        uploadLayout.add(uploadIcon, uploadLabel, hint, hint2);

        // Create the button and set the uploadLayout as its component
        Button uploadButton = new Button();
        uploadButton.getElement().appendChild(uploadLayout.getElement());

        // Set the uploadButton as the upload button
        upload.setUploadButton(uploadButton);

        int maxFileSizeInBytes = 10 * 1024 * 1024; // 10MB

        upload.addFileRejectedListener(event -> {
            String errorMessage = event.getErrorMessage();

            Notification notification = Notification.show(errorMessage, 5000,
                    Notification.Position.MIDDLE);
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        });

        UploadsI18N i18n = new UploadsI18N();
        i18n.getError().setFileIsTooBig("The image exceeds the maximum allowed size of 10MB.");
        i18n.getError().setIncorrectFileType("The provided image does not have the correct format (PNG or JPEG).");

        upload.setI18n(i18n);
        upload.setMaxFileSize(maxFileSizeInBytes);
        upload.setAcceptedFileTypes("image/png", "image/jpeg");
        upload.addSucceededListener(event -> {
            MemoryBuffer buffer = (MemoryBuffer) upload.getReceiver();

            try {
                InputStream inputStream = buffer.getInputStream();
                byte[] bytes = inputStream.readAllBytes();
                originalFilename = event.getFileName();
                StreamResource resource = new StreamResource(originalFilename, () -> new ByteArrayInputStream(bytes));
                uploadedImage.setSrc(resource);
                uploadedImage.setVisible(true);
            } catch (IOException e) {
                Notification.show("Error uploading artwork image", 3000, Notification.Position.TOP_CENTER);
            }
        });

	upload.getElement().addEventListener("upload-abort", new DomEventListener() {
	    @Override
            public void handleEvent(DomEvent domEvent) {
                uploadedImage.setSrc("");
                uploadedImage.setVisible(false);

                originalFilename = null;
            }
	});

        postButton.addClickListener(event -> {
            MemoryBuffer buffer = (MemoryBuffer) upload.getReceiver();

            User user = userService.findCurrentUser();

            if (user != null) {
		String emailValue = user.getEmail();

                if (originalFilename != null && !title.getValue().isEmpty()) {
                    try (InputStream inputStream = buffer.getInputStream()) {
                         // Create a temporary file to store the uploaded image
        		File tempFile = File.createTempFile("tempImage", ".png");
        		tempFile.deleteOnExit(); // Ensure the file is deleted on exit

        		// Save the uploaded image to the temporary file
        		FileOutputStream fos = new FileOutputStream(tempFile);
            		byte[] imageBytes = inputStream.readAllBytes();
            		fos.write(imageBytes);

                    	String imageUrl = cloudinaryService.uploadImage(tempFile, ARTWORK_FOLDER);

                    	artworkService.saveArtwork(emailValue, imageUrl, title.getValue());

                    	Notification.show("Artwork saved successfully", 1000, Notification.Position.TOP_CENTER)
				.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
		    	getUI().ifPresent(ui -> ui.navigate("main-artworks"));
		    }catch(Exception e){
		        System.out.println(e.getMessage());
		    }
                }

                if(originalFilename == null && title.getValue().isEmpty()){
                    Notification.show("Both fields cannot be empty", 1000, Notification.Position.MIDDLE)
			.addThemeVariants(NotificationVariant.LUMO_ERROR);
                } else if(title.getValue().isEmpty() && originalFilename != null){
                    Notification.show("Title cannot be empty", 1000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                }else if((originalFilename == null || originalFilename.isEmpty()) && !title.getValue().isEmpty()){
                    Notification.show("Image cannot be empty", 1000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                }
            } else {
                Notification.show("User not found", 1000, Notification.Position.TOP_CENTER)
			.addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        mainPostButton.addClickListener(event -> {
            MemoryBuffer buffer = (MemoryBuffer) upload.getReceiver();

            User user = userService.findCurrentUser();

            if (user != null) {
                String emailValue = user.getEmail();

                if (originalFilename != null && !title.getValue().isEmpty()) {
                    try (InputStream inputStream = buffer.getInputStream()) {
                        // Create a temporary file to store the uploaded image
                        File tempFile = File.createTempFile("tempImage", ".png");
                        tempFile.deleteOnExit(); // Ensure the file is deleted on exit

                        // Save the uploaded image to the temporary file
                        FileOutputStream fos = new FileOutputStream(tempFile);
                        byte[] imageBytes = inputStream.readAllBytes();
                        fos.write(imageBytes);

                    	String imageUrl = cloudinaryService.uploadImage(tempFile, ARTWORK_FOLDER);

                    	artworkService.saveArtwork(emailValue, imageUrl, title.getValue());

                    	Notification.show("Artwork saved successfully", 1000, Notification.Position.TOP_CENTER)
                        	.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    	getUI().ifPresent(ui -> ui.navigate("main-artworks"));
		    }catch(Exception e){
		        System.out.println(e.getMessage());
		    }
                }

                if(originalFilename == null && title.getValue().isEmpty()){
                    Notification.show("Both fields cannot be empty", 1000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                }else if(title.getValue().isEmpty() && originalFilename != null){
                    Notification.show("Title cannot be empty", 1000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                }else if((originalFilename == null || originalFilename.isEmpty()) && !title.getValue().isEmpty()){
                    Notification.show("Image cannot be empty", 1000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                }
            } else {
                Notification.show("User not found", 3000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

	Div imageDiv = new Div(uploadedImage);
	imageDiv.addClassName("uploaded-image");

	FormLayout formLayout = new FormLayout();
        formLayout.add(createProfileHeader(), artworkUrlText, upload, imageDiv, title, mainPostButton);
	formLayout.setColspan(title, 3);
        formLayout.setResponsiveSteps(new ResponsiveStep("0", 1),
                new ResponsiveStep("500px", 3));

	VerticalLayout artworksDiv = new VerticalLayout(formLayout);
	setContent(artworksDiv);
   }

   private HorizontalLayout createProfileHeader(){
   	User user = userService.findCurrentUser();

   	String imageUrl = user.getProfileImage();

   	Avatar avatar = new Avatar();
   	avatar.setImage(imageUrl);
	avatar.addClassName("add-artwork-avatar");

	Span name = new Span(user.getFullName());
	name.addClassName("add-artwork-name");

	HorizontalLayout layout = new HorizontalLayout(avatar, name);
	layout.addClassName("add-artwork-layout");

	return layout;
   }

   private void createHeader(){
        Icon backButton = new Icon(VaadinIcon.ARROW_BACKWARD);
        backButton.addClassName("profile-back-button");
        backButton.addClickListener(event -> {
             UI.getCurrent().navigate(MainArtworkView.class);
        });

        Span text = new Span("Post an artwork");
        text.addClassName("add-artwork-header-text");

        postButton.addClassName("post-button");

        addToNavbar(new HorizontalLayout(backButton, text, postButton));
    }
}
