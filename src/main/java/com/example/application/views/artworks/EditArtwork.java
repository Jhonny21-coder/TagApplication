package com.example.application.views.artworks;

import com.example.application.views.UploadsI18N;
import com.example.application.data.Artwork;
import com.example.application.data.User;
import com.example.application.services.UserServices;
import com.example.application.services.ArtworkService;
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
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.component.UI;
import jakarta.annotation.security.PermitAll;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.File;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.ZoneId;

import java.util.List;
import java.util.Locale;

@PermitAll
@Route("edit-artwork")
public class EditArtwork extends AppLayout implements HasUrlParameter<Long> {

    private final ArtworkService artworkService;
    private final UserServices userService;
    private final CloudinaryService cloudinaryService;

    private final String ARTWORK_FOLDER = "artwork_images";

    private StreamResource resource;
    private final Span artworkUrlText = new Span("Upload new image");
    private final Span newArtworkText = new Span("New artwork image");
    private final Upload upload = new Upload(new MemoryBuffer());
    private final Image newImage = new Image();
    private final TextArea title = new TextArea("Enter new title");
    private final Button save = new Button("Save");
    private String newFilename;
    private byte[] bytes;
    private Span currentText = new Span("Current artwork image");
    private Image currentImage = new Image();

    public EditArtwork(ArtworkService artworkService, UserServices userService,
    	CloudinaryService cloudinaryService){
    	this.artworkService = artworkService;
        this.userService = userService;
        this.cloudinaryService = cloudinaryService;

        addClassName("profile-app-layout");
        artworkUrlText.addClassName("add-text");
        newArtworkText.addClassName("current-text");
        upload.addClassName("artwork-upload");
        title.addClassName("add-artwork-area");
        save.addClassName("save-artwork");
        currentText.addClassName("current-text");

        createHeader();
    }

    @Override
    public void setParameter(BeforeEvent event, Long artworkId) {
	Artwork artwork = artworkService.getArtworkById(artworkId);
        editArtwork(artwork);
    }

    public void editArtwork(Artwork artwork){
    	newFilename = artwork.getArtworkUrl();

        title.setValue(artwork.getDescription());
        title.setSuffixComponent(new Icon(VaadinIcon.TEXT_LABEL));
        title.setPlaceholder("Current title " + artwork.getDescription());
	title.addValueChangeListener(event -> {
	    title.setLabel("New Title");
	});

        currentText.setVisible(true);
	newArtworkText.setVisible(false);

        newImage.addClassName("uploaded-image");
	newImage.setVisible(false);

	String imageUrl = artwork.getArtworkUrl();

	currentImage.setSrc(imageUrl);
	currentImage.addClassName("uploaded-image");

	Div currentImageDiv = new Div(currentImage);
        currentImageDiv.addClassName("uploaded-image");

        Div newImageDiv = new Div(newImage);
        newImageDiv.addClassName("uploaded-image");

        customUpload();

        upload.addSucceededListener(event -> {
            MemoryBuffer buffer = (MemoryBuffer) upload.getReceiver();

            try {
                InputStream inputStream = buffer.getInputStream();
                byte[] bytes = inputStream.readAllBytes();
                newFilename = event.getFileName();
                StreamResource resource = new StreamResource(newFilename, () -> new ByteArrayInputStream(bytes));
                newImage.setSrc(resource);
                newImage.setVisible(true);
                newArtworkText.setVisible(true);
                currentImage.setVisible(false);
                currentText.setVisible(false);
            } catch (IOException e) {
                Notification.show("Error uploading artwork image", 3000, Notification.Position.TOP_CENTER);
            }
        });

        save.addClickListener(event -> {
            MemoryBuffer buffer = (MemoryBuffer) upload.getReceiver();
	    String descriptionValue = title.getValue();

	    try {
	    	InputStream inputStream = buffer.getInputStream();
            	String currentImageUrl = artwork.getArtworkUrl();

	    	// Saving the artwork to database
            	if (inputStream.available() != 0 && !descriptionValue.isEmpty() && !descriptionValue.matches("\\s*")) {
                    String newImageUrl = cloudinaryService.updateImage(currentImageUrl, inputStream, ARTWORK_FOLDER);

                    artwork.setArtworkUrl(newImageUrl);
            	}else if (inputStream.available() == 0 && !descriptionValue.isEmpty() && !descriptionValue.matches("\\s*")) {
		    artwork.setArtworkUrl(currentImageUrl);
            	}

		artwork.setDescription(descriptionValue);
		artwork.setDateTime(createDateTime());
            	artworkService.updateArtwork(artwork);

            	Notification.show("Artwork edited successfully", 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

                getUI().ifPresent(ui -> ui.navigate("main-artworks"));
            }catch(Exception e){
                Notification.show("Error saving artwork image", 3000, Notification.Position.TOP_CENTER);
            }

            if(descriptionValue.isEmpty() && descriptionValue.matches("\\s*")){
               Notification.show("Title cannot be empty", 1000, Notification.Position.MIDDLE)
                  .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        FormLayout formLayout = new FormLayout();
        formLayout.add(artworkUrlText, upload, newArtworkText, currentText, currentImageDiv, newImageDiv, title, save);

        VerticalLayout artworksDiv = new VerticalLayout(formLayout);
        setContent(artworksDiv);
    }

    private String createDateTime(){
    	DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                                        .withLocale(Locale.US);

        LocalDateTime localDateTime = LocalDateTime.now(ZoneId.of("Asia/Manila"));

        String dateTime = formatter.format(localDateTime);

        return dateTime;
    }

    private void customUpload(){
    	HorizontalLayout uploadLayout = new HorizontalLayout();
        uploadLayout.setSpacing(false);
        uploadLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        Icon uploadIcon = VaadinIcon.FILE_ADD.create();

        // Create the text
        Span hint = new Span("Accepted images formats: (png, jpeg)");
        Span hint2 = new Span(")");
        hint.addClassName("hint");
        hint2.addClassName("hin2");

        Span uploadLabel = new Span("Upload new artwork (");

        // Add both the icon and text to the HorizontalLayout
        uploadLayout.add(uploadIcon, uploadLabel, hint, hint2);

        // Create the button and set the uploadLayout as its component
        Button uploadButton = new Button();
        uploadButton.getElement().appendChild(uploadLayout.getElement());

        // Set the uploadButton as the upload button
        upload.setUploadButton(uploadButton);

        int maxFileSizeInBytes = 10 * 1024 * 1024;

        upload.addFileRejectedListener(event -> {
            String errorMessage = event.getErrorMessage();

            Notification notification = Notification.show(errorMessage, 5000,
                    Notification.Position.MIDDLE);
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        });

        UploadsI18N i18n = new UploadsI18N();

        i18n.getError().setFileIsTooBig(
                "The image exceeds the maximum allowed size of 10MB.");
        i18n.getError().setIncorrectFileType(
                "The provided image does not have the correct format (PNG or JPEG).");
        upload.setI18n(i18n);

        upload.setMaxFileSize(maxFileSizeInBytes);
        upload.setAcceptedFileTypes("image/png", "image/jpeg");

        upload.getElement().addEventListener("upload-abort", event -> {
             newImage.setSrc("");
             newImage.setVisible(false);
             newArtworkText.setVisible(false);
             currentImage.setVisible(true);
             currentText.setVisible(true);
        });
    }

    private void createHeader(){
        Icon backButton = new Icon(VaadinIcon.ARROW_BACKWARD);
        backButton.addClassName("profile-back-button");
        backButton.addClickListener(event -> {
             UI.getCurrent().navigate(MainArtworkView.class);
        });

        Span text = new Span("Edit artwork");
        text.addClassName("view-fullname");

        addToNavbar(new HorizontalLayout(backButton, text));
    }
}
