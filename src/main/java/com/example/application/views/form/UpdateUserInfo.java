package com.example.application.views.form;

import com.example.application.repository.UserRepository;
import com.example.application.views.MainLayout;
import com.example.application.data.StudentInfo;
import com.example.application.services.StudentInfoService;
import com.example.application.data.User;
import com.example.application.services.UserServices;
import com.example.application.config.CloudinaryService;
import com.example.application.views.profile.OwnProfile;
import com.example.application.views.profile.ImageCropView;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import org.springframework.stereotype.Component;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.accordion.AccordionPanel;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.server.VaadinSession;
import jakarta.annotation.security.PermitAll;

import java.io.FileInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.File;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@PermitAll
@Route("updateUser")
public class UpdateUserInfo extends AppLayout {

    private final UserRepository userRepository;
    private final UserServices userService;
    private final CloudinaryService cloudinaryService;

    private String newFileName;
    private String sessionAttribute;
    private String urlAttribute;

    public UpdateUserInfo(UserServices userService, UserRepository userRepository,
    	CloudinaryService cloudinaryService) {
        this.userService = userService;
	this.userRepository = userRepository;
	this.cloudinaryService = cloudinaryService;
	addClassName("edit-app-layout");

	createHeader();
	createMainLayout();
    }

    // Main page
    private void createMainLayout(){
    	FormLayout formLayout = new FormLayout();

    	User user = userService.findCurrentUser();

    	VerticalLayout editProfilePicture = createEditProfilePicture(user);
    	editProfilePicture.addClassName("edit-profile-parent-layout");

    	VerticalLayout editCoverPhoto = createEditCoverPhoto(user);
        editCoverPhoto.addClassName("edit-profile-parent-layout");

        VerticalLayout editBio = createEditBio(user);
        editBio.addClassName("edit-profile-parent-layout");

        VerticalLayout editPersonal = createEditPersonal(user);
        editPersonal.addClassName("edit-profile-parent-layout4");

        Button infoButton = new Button("Edit about your info");
        infoButton.getElement().getThemeList().add("badge");
        infoButton.addClassName("edit-info-button");

        HorizontalLayout infoLayout = new HorizontalLayout(infoButton);
        infoLayout.addClassName("edit-info-layout");
        infoLayout.addClickListener(event -> {
            UI.getCurrent().navigate(AboutYourselfView.class);
        });

    	formLayout.add(editProfilePicture, editCoverPhoto, editBio, editPersonal, infoLayout);

    	setContent(formLayout);
    }

    // Personal information
    private VerticalLayout createEditPersonal(User user){
        Span profilePictureText = new Span("Personal");
        profilePictureText.addClassName("edit-first-text");

        Span editProfileText = new Span("Edit");
        editProfileText.addClassName("edit-text");

        HorizontalLayout headerLayout = new HorizontalLayout(profilePictureText, editProfileText);
        headerLayout.addClassName("edit-profile-layout");

        Icon ageIcon = new Icon(VaadinIcon.USER_CLOCK);
        ageIcon.addClassName("profile-information");

        Span ageSpan = new Span(String.valueOf(user.getAge()));
        ageSpan.addClassName("profile-span");

        Icon genderIcon = new Icon(VaadinIcon.MALE);
        genderIcon.addClassName("profile-information");

        Span genderSpan = new Span(user.getGender());
        genderSpan.addClassName("profile-span");

        Icon dateOfBirthIcon = new Icon(VaadinIcon.CALENDAR_CLOCK);
        dateOfBirthIcon.addClassName("profile-information");

        Span dateSpan = new Span(user.getDateOfBirth().toString());
        dateSpan.addClassName("profile-span");

        Icon placeOfBirthIcon = new Icon(VaadinIcon.HOME);
        placeOfBirthIcon.addClassName("profile-information");

        Span placeSpan = new Span(user.getPlaceOfBirth());
        placeSpan.addClassName("profile-span");

        Div lineDiv = new Div();
        lineDiv.addClassName("edit-last-line-div");

        return new VerticalLayout(
            headerLayout,
            new HorizontalLayout(ageIcon, new Span("At age of "), ageSpan),
            new HorizontalLayout(genderIcon, new Span("Gender "), genderSpan),
            new HorizontalLayout(dateOfBirthIcon, new Span("Born in "), dateSpan),
            new HorizontalLayout(placeOfBirthIcon, new Span("Lives in "), placeSpan),
            lineDiv
        );
    }

    // Method to edit the bio
    private VerticalLayout createEditBio(User user){
        Span profilePictureText = new Span("Bio");
        profilePictureText.addClassName("edit-first-text");

        Span editProfileText = new Span("Edit");
        editProfileText.addClassName("edit-text");
        editProfileText.addClickListener(event -> {
            UI.getCurrent().navigate(EditBioView.class);
        });

        HorizontalLayout headerLayout = new HorizontalLayout(profilePictureText, editProfileText);
        headerLayout.addClassName("edit-profile-layout");

        String bio = "";

        if(user.getBio() != null && !user.getBio().isEmpty()){
           bio = user.getBio();
        }else{
           bio = "Add a bio";
        }

        Span bioText = new Span(bio);
        bioText.addClassName("edit-bio-text");

        Div lineDiv = new Div();
        lineDiv.addClassName("edit-line-div");

        return new VerticalLayout(headerLayout, bioText, lineDiv);
    }

    // Method to upload an image
    private void createUploadImage(Dialog dialog, Avatar avatar, Image image, User user, String saveType){
    	Button uploadButton = new Button("Upload photo", new Icon(VaadinIcon.CAMERA));
	uploadButton.addClassName("edit-upload-button");

        Button eyeButton = new Button(new Icon(VaadinIcon.USER_CARD));
        eyeButton.addClassName("edit-eye-button");
        eyeButton.addClickListener(event -> {
            dialog.close();
            VaadinSession.getCurrent().getSession().setAttribute("edit-profile", user.getId());
            UI.getCurrent().navigate(ViewProfilePicture.class, saveType);
        });

        if(saveType.equalsIgnoreCase("Profile")){
           eyeButton.setText("See profile picture");
           sessionAttribute = "uploadedProfilePhoto";
           urlAttribute = "ProfileEditPhoto";
        }else{
           String imageUrl = user.getCoverPhoto();
           if(imageUrl != null && !imageUrl.isEmpty()){
              eyeButton.setText("See cover photo");
           }else{
              eyeButton.setVisible(false);
           }
           sessionAttribute = "uploadedCoverPhoto";
           urlAttribute = "CoverEditPhoto";
        }

	MemoryBuffer photoBuffer = new MemoryBuffer();

	Upload upload = new Upload();
	upload.setReceiver(photoBuffer);
	upload.addClassName("edit-upload");
	upload.setUploadButton(uploadButton);
	upload.setAcceptedFileTypes("image/png", "image/jpeg", "image/jpg");
	upload.addSucceededListener(event -> {
	     dialog.close();

             try {
                InputStream inputStream = photoBuffer.getInputStream();

                if (inputStream.available() > 0) {
                   // Convert InputStream to ByteArrayOutputStream
                   ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                   inputStream.transferTo(byteArrayOutputStream); // Copy data from InputStream to ByteArrayOutputStream

                   // Store ByteArrayOutputStream in session
                   VaadinSession.getCurrent().getSession().setAttribute(sessionAttribute, byteArrayOutputStream);
                   UI.getCurrent().navigate(ImageCropView.class, urlAttribute);
                } else {
                   Notification.show("Profile photo is empty.");
                }
             } catch (Exception e) {
                Notification.show(e.getMessage());
             }
	});

	dialog.add(upload, eyeButton);
    }

    // Method to save the new profile picture
    private void createSavePicture(User user, Upload upload, Avatar avatar, String saveType){
    	final String USER_FOLDER = "user_images";
	// Saving a file to a directory
	MemoryBuffer buffer = (MemoryBuffer) upload.getReceiver();

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
            	String imageUrl = cloudinaryService.uploadImage(tempFile, USER_FOLDER);

	    	if(saveType.equalsIgnoreCase("Profile")){
                   user.setProfileImage(imageUrl);
                   userService.updateUser(user);
            	}else{
                   user.setCoverPhoto(imageUrl);
                   userService.updateUser(user);
            	}
            }
	}catch(Exception e){
            Notification.show("Error saving artwork image", 3000, Notification.Position.TOP_CENTER);
	}

	System.out.println("New added file: " + newFileName);
    }

    // Method to edit the profile picture
    private VerticalLayout createEditProfilePicture(User user){
    	String imageUrl = user.getProfileImage();
        Avatar avatar = new Avatar();
        avatar.setImage(imageUrl);
        avatar.addClassName("edit-profile-avatar");

    	Span profilePictureText = new Span("Profile picture");
    	profilePictureText.addClassName("edit-first-text");

    	Span editText = new Span("Edit");
    	editText.addClassName("edit-text");
    	editText.addClickListener(event -> {
    	     Dialog dialog = new Dialog();
    	     dialog.addClassName("edit-profile-dialog");
    	     dialog.open();

    	     createUploadImage(dialog, avatar, new Image(), user, "Profile");
    	});

    	HorizontalLayout headerLayout = new HorizontalLayout(profilePictureText, editText);
    	headerLayout.addClassName("edit-profile-layout");

        Div lineDiv = new Div();
        lineDiv.addClassName("edit-line-div");

        return new VerticalLayout(headerLayout, avatar, lineDiv);
    }

    // Method to edit the cover photo
    private VerticalLayout createEditCoverPhoto(User user){
        String imageUrl = user.getCoverPhoto();

        Image image = new Image();
	image.addClassName("edit-cover-image");

        if(imageUrl != null){
           image.setSrc(imageUrl);
        }else{
           StreamResource resource = new StreamResource("coverphoto-placeholder.jpeg",
                () -> getClass().getResourceAsStream("/META-INF/resources/images/coverphoto-placeholder.jpeg"));
           image.setSrc(resource);
        }

        Span coverPhotoText = new Span("Cover photo");
        coverPhotoText.addClassName("edit-first-text");

        Span saveText = new Span("Save");
        saveText.addClassName("edit-profile-save-text");
        saveText.setVisible(false);

        Span discardText = new Span("Discard");
        discardText.addClassName("edit-profile-discard-text");
        discardText.setVisible(false);

        Span editText = new Span("Edit");
        editText.addClassName("edit-text");
        editText.addClickListener(event -> {
             Dialog dialog = new Dialog();
             dialog.addClassName("edit-profile-dialog");
             dialog.open();

             createUploadImage(dialog, new Avatar(), image, user, "Cover");
        });

        HorizontalLayout headerLayout = new HorizontalLayout(coverPhotoText, saveText, discardText, editText);
        headerLayout.addClassName("edit-profile-layout");

        Div lineDiv = new Div();
        lineDiv.addClassName("edit-line-div");

        return new VerticalLayout(headerLayout, image, lineDiv);
    }

    // Header of the layout
    private void createHeader(){
	Span editText = new Span("Edit information");
	editText.addClassName("edit-header-text");

	Icon backIcon = new Icon("lumo", "arrow-left");
        backIcon.addClassName("header-back-button");
        backIcon.addClickListener(event -> {
            UI.getCurrent().navigate(OwnProfile.class);
        });

        HorizontalLayout header = new HorizontalLayout(backIcon, editText);
        header.setWidthFull();

	addToNavbar(header);
    }
}
