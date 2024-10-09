package com.example.application.views.profile;

import com.example.application.data.User;
import com.example.application.services.UserServices;
import com.example.application.views.form.UpdateUserInfo;
import com.example.application.config.CloudinaryService;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.Receiver;
import com.vaadin.flow.component.upload.SucceededEvent;
import com.vaadin.flow.component.upload.FailedEvent;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.theme.lumo.LumoIcon;

import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;
import jakarta.annotation.security.PermitAll;

import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.File;

import java.util.Base64;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;

@PermitAll
@Route("cropper")
public class ImageCropView extends AppLayout implements HasUrlParameter<String> {

    private final UserServices userService;
    private final CloudinaryService cloudinaryService;

    private final Icon saveIcon = new Icon("lumo", "checkmark");
    private final Icon finishIcon = new Icon("lumo", "checkmark");
    private final Icon cropIcon = new Icon(VaadinIcon.CROP);
    private final Icon uncroppedIcon = new Icon(VaadinIcon.TRASH);

    private final Image image = new Image();

    private String base64Image;
    private byte[] notCroppedImageBytes;

    public ImageCropView(UserServices userService, CloudinaryService cloudinaryService) {
        this.userService = userService;
        this.cloudinaryService = cloudinaryService;
        addClassName("cover-photo-app-layout");
    }

    @Override
    public void setParameter(BeforeEvent event, String imageType){
    	mainLayout(imageType);
    	createHeader(imageType);
    }

    private void mainLayout(String imageType) {
    	FormLayout formLayout = new FormLayout();

        image.setId("image");
        image.addClassName("cropped-cover-photo");

        handleUploadSucceeded(imageType);

        // Set up cropping
        cropIcon.addClassName("cover-crop-button");
        cropIcon.addClickListener(event -> {
            activateCropImage();
            cropIcon.setVisible(false);
            saveIcon.setVisible(false);
        });

        // Add components to layout
        formLayout.add(image);
        setContent(formLayout);
    }

    private void activateCropImage(){
    	image.removeClassName("cropped-cover-photo");
    	finishIcon.setVisible(true);
    	// Inject JavaScript to initialize Cropper.js
        getElement().executeJs(
           "var image = document.getElementById('image');" +
           "var cropper = new Cropper(image, {" +
           "   aspectRatio: 4 / 3," +
           "   viewMode: 1," +
           "   movable: true, " +   // Allow image dragging
    	   "   zoomable: true, " +  // Allow zooming
    	   "   scalable: true, " +  // Allow scaling
           "   rotatable: true" +
           "});" +
           "window.cropper = cropper;"
        );
    }

    private void handleUploadSucceeded(String imageType) {
    	try {
            // Choose the correct session attribute based on image type
            String sessionAttribute = imageType.equals("ProfilePhoto") || imageType.equals("ProfileEditPhoto") ? "uploadedProfilePhoto" : "uploadedCoverPhoto";

            // Retrieve the uploaded image bytes from session
            ByteArrayOutputStream outputStream = (ByteArrayOutputStream) VaadinSession.getCurrent().getSession().getAttribute(sessionAttribute);

           if (outputStream != null) {
               notCroppedImageBytes = outputStream.toByteArray();
               String imageBase64 = Base64.getEncoder().encodeToString(notCroppedImageBytes);

               // Load the image into Cropper.js
               String imgUrl = "data:image/jpeg;base64," + imageBase64;
               image.setSrc(imgUrl);
           } else {
               Notification.show("No image found for " + imageType, 3000, Notification.Position.TOP_CENTER);
           }
    	} catch (Exception e) {
           Notification.show("Error processing the image", 3000, Notification.Position.TOP_CENTER);
    	}
    }

    private void cropImage() {
        finishIcon.setVisible(false);
    	getElement().executeJs(
    	   "var croppedCanvas = window.cropper.getCroppedCanvas();" + // No dimensions specified
           "var croppedImage = croppedCanvas.toDataURL('image/jpeg');" +
    	   "window.cropper.destroy();" +
    	   "return croppedImage;"
	).then(json -> {
    	   base64Image = json.asString();
    	   processCroppedImage();
	});
    }

    public void saveImage(String imageType) {
        final String USER_FOLDER = "user_images";

        User user = userService.findCurrentUser();

        File tempFile = null;

        try {
            // Create a temporary file to store the image
            tempFile = File.createTempFile("tempImage", ".png"); // Change extension as needed
            tempFile.deleteOnExit(); // Ensure the file is deleted on exit

            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                // Write the appropriate image data
                if (base64Image == null) {
                    // Image not cropped, write not cropped bytes
                    fos.write(notCroppedImageBytes);
                } else {
                    // Image has been cropped, decode base64
                    String base64Data = base64Image.split(",")[1];
                    byte[] imageBytes = Base64.getDecoder().decode(base64Data);
                    fos.write(imageBytes);
                }
            }

            // Upload the temporary file to Cloudinary
            String imageUrl = cloudinaryService.uploadImage(tempFile, USER_FOLDER);

            if(imageType.equalsIgnoreCase("CoverPhoto") || imageType.equalsIgnoreCase("CoverEditPhoto")){
                user.setCoverPhoto(imageUrl);
            }else{
                user.setProfileImage(imageUrl);
            }

            userService.updateUser(user); // Update user in the database

        } catch (IOException e) {
            e.printStackTrace(); // Handle exceptions appropriately
            // Optionally, notify the user about the failure
        } finally {
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete(); // Clean up temporary file
            }
        }
    }

    private void processCroppedImage() {
	// Remove base64 header
	String base64Data = base64Image.split(",")[1];
	byte[] imageBytes = Base64.getDecoder().decode(base64Data);

	StreamResource resources = new StreamResource("cropped-image", () -> new ByteArrayInputStream(imageBytes));
	image.setSrc(resources);
	image.addClassName("cropped-cover-photo");
    }

    private void navigateAndRemoveAttributes(String imageType){
    	// Map image types to session attributes
        Map<String, String> sessionAttributes = new HashMap<>();
	sessionAttributes.put("ProfilePhoto", "uploadedProfilePhoto");
	sessionAttributes.put("CoverPhoto", "uploadedCoverPhoto");
	sessionAttributes.put("ProfileEditPhoto", "uploadedProfilePhoto");
	sessionAttributes.put("CoverEditPhoto", "uploadedCoverPhoto");

	// Remove attribute if imageType matches
	String sessionAttribute = sessionAttributes.get(imageType);
	if (sessionAttribute != null) {
            VaadinSession.getCurrent().getSession().removeAttribute(sessionAttribute);
	}

	if (imageType.equals("ProfilePhoto") || imageType.equals("CoverPhoto")){
            UI.getCurrent().navigate(OwnProfile.class);
	}else{
            UI.getCurrent().navigate(UpdateUserInfo.class);
	}
    }

    private void createHeader(String imageType){
    	Icon backIcon = new Icon("lumo", "arrow-left");
        backIcon.addClassName("cover-back-button");
        backIcon.addClickListener(event -> {
	     navigateAndRemoveAttributes(imageType);
        });

	saveIcon.setVisible(true);
        saveIcon.addClassName("profile-done-button");
        saveIcon.addClickListener(event -> {
             saveImage(imageType);
             navigateAndRemoveAttributes(imageType);
        });

        finishIcon.setVisible(false);
        finishIcon.addClassName("cover-finish-button");
        finishIcon.addClickListener(event -> {
             cropImage();
             uncroppedIcon.setVisible(true);
             saveIcon.setVisible(true);
	});

	uncroppedIcon.setVisible(false);
	uncroppedIcon.addClassName("cover-uncropped-button");
	uncroppedIcon.addClickListener(event -> {
	     image.removeClassName("cropped-cover-photo");
	     handleUploadSucceeded(imageType);
             uncroppedIcon.setVisible(false);
             cropIcon.setVisible(true);
        });

    	addToNavbar(new HorizontalLayout(backIcon, cropIcon, uncroppedIcon, finishIcon, saveIcon));
    }
}
