package com.example.application.views.form;

import com.example.application.views.UploadsI18N;
import com.example.application.views.MainLayout;
import com.example.application.data.StudentInfo;
import com.example.application.services.StudentInfoService;
import com.example.application.data.User;
import com.example.application.services.UserServices;
import com.example.application.data.Artwork;
import com.example.application.services.ArtworkService;

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
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.component.icon.*;
import com.vaadin.flow.dom.DomEventListener;
import com.vaadin.flow.dom.DomEvent;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.data.value.ValueChangeMode;
import jakarta.annotation.security.PermitAll;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

@PermitAll
@Route("edit-bio-view")
public class EditBioView extends AppLayout {

    private final StudentInfoService studentInfoService;
    private final UserServices userService;
    private final ArtworkService artworkService;

    public EditBioView(StudentInfoService studentInfoService, UserServices userService,
			ArtworkService artworkService) {
        this.studentInfoService = studentInfoService;
	this.userService = userService;
	this.artworkService = artworkService;

	createHeader();
	createMainLayout();

	addClassName("edit-app-layout");
    }

    private void createMainLayout() {
    	User currentUser = userService.findCurrentUser();

    	String bio = "";

    	if(currentUser.getBio() != null && !currentUser.getBio().isEmpty()){
    	   bio = currentUser.getBio();
    	}

	int charLimit = 200;

	TextArea textArea = new TextArea();
	textArea.addClassName("edit-bio-text-area");
	textArea.setValueChangeMode(ValueChangeMode.EAGER);
	textArea.setValue(bio);
	textArea.setMaxLength(charLimit);
	textArea.setHelperText(textArea.getValue().length() + "/" + charLimit);
	textArea.addValueChangeListener(e -> {
	    int length = e.getValue().length();
	    e.getSource().setHelperText(length + "/" + charLimit);
        });

	Span bioText = new Span("Bio");
	bioText.addClassName("edit-bio-main-text");

	Button saveButton = new Button("Save");
	saveButton.addClassName("edit-bio-button");
	saveButton.setIcon(new Icon(VaadinIcon.CHECK));
	saveButton.addClickListener(event -> {
	    String bioValue = textArea.getValue();

	    if(bioValue.isEmpty()){
	       Notification.show("Bio cannot be empty", 1000, Notification.Position.MIDDLE)
	       	    .addThemeVariants(NotificationVariant.LUMO_ERROR);
	    }else{
	       currentUser.setBio(bioValue);
	       userService.updateUser(currentUser);
	       UI.getCurrent().navigate(UpdateUserInfo.class);
	    }
	});

	Span helperText1 = new Span("Try adding a short bio to tell people more about yourself.");
	helperText1.addClassName("edit-helper-text");

	Span helperText2 = new Span("Your bio is public and limited to 200 characters.");
	helperText2.addClassName("edit-helper-text");

	FormLayout formLayout = new FormLayout();
        formLayout.add(bioText, textArea, helperText1, helperText2, saveButton);

	VerticalLayout editBioLayout = new VerticalLayout(formLayout);
	editBioLayout.addClassName("edit-bio-layout");

	setContent(editBioLayout);
   }

   public boolean isEmoji(String text) {
    	int[] codePoints = text.codePoints().toArray();
    	for (int codePoint : codePoints) {
            if ((codePoint >= 0x1F600 && codePoint <= 0x1F64F) ||   // Emoticons
            	(codePoint >= 0x1F300 && codePoint <= 0x1F5FF) ||   // Miscellaneous Symbols and Pictographs
            	(codePoint >= 0x1F680 && codePoint <= 0x1F6FF) ||   // Transport and Map Symbols
            	(codePoint >= 0x1F900 && codePoint <= 0x1F9FF) ||   // Supplemental Symbols and Pictographs
            	(codePoint >= 0x2600 && codePoint <= 0x26FF) ||     // Miscellaneous Symbols
            	(codePoint >= 0x2700 && codePoint <= 0x27BF) ||     // Dingbats
            	(codePoint >= 0x1F1E6 && codePoint <= 0x1F1FF) ||   // Flags
            	(codePoint >= 0x1F3FB && codePoint <= 0x1F3FF) ||   // Skin tones
            	(codePoint >= 0x2640 && codePoint <= 0x2642)) {     // Gender symbols
            	return true;
            }
    	}
    	return false;
   }

   private void createHeader(){
        Icon backButton = new Icon(VaadinIcon.ARROW_BACKWARD);
        backButton.addClassName("profile-back-button");
        backButton.addClickListener(event -> {
             UI.getCurrent().navigate(UpdateUserInfo.class);
        });

        Span text = new Span("Edit bio");
        text.addClassName("edit-bio-header-text");

        addToNavbar(new HorizontalLayout(backButton, text));
    }
}
