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
import com.vaadin.flow.component.icon.SvgIcon;
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
@Route("about-yourself-view")
public class AboutYourselfView extends AppLayout {

    private final UserServices userService;

    public AboutYourselfView(UserServices userService) {
	this.userService = userService;

	createHeader();
	createMainLayout();

	addClassName("edit-app-layout");
    }

    private void createMainLayout() {
	VerticalLayout mainLayout = new VerticalLayout();
	mainLayout.addClassName("about-main-layout");

    	VerticalLayout workLayout = createWorkLayout();
    	workLayout.addClassName("edit-work-layout");

    	VerticalLayout educationLayout = createEducationLayout();
        educationLayout.addClassName("edit-education-layout");

        VerticalLayout placesLivedLayout = createPlacesLivedLayout();
        placesLivedLayout.addClassName("edit-education-layout");

        VerticalLayout contactLayout = createContactLayout();
        contactLayout.addClassName("edit-education-layout");

        VerticalLayout basicInfoLayout = createBasicInfoLayout();
        basicInfoLayout.addClassName("edit-education-layout");

    	mainLayout.add(
    		workLayout, educationLayout, placesLivedLayout,
		contactLayout, basicInfoLayout
	);

	setContent(mainLayout);
   }

   private VerticalLayout createBasicInfoLayout(){
   	Span headerText = new Span("Basic info");
        headerText.addClassName("edit-header-text");

        Icon genderIcon = new Icon(VaadinIcon.USER);
        genderIcon.addClassName("edit-work-icon");

        Span addGender = new Span("Add gender");
        addGender.addClassName("edit-add-work-experience");

        Div genderDiv = new Div(genderIcon, addGender);
        genderDiv.addClassName("edit-work-div");

	StreamResource resource = new StreamResource("cake.svg",
                () -> getClass().getResourceAsStream("/META-INF/resources/icons/cake.svg"));

        SvgIcon birthdayIcon = new SvgIcon(resource);
        birthdayIcon.addClassName("edit-work-icon");

        Span addBirthday = new Span("Add birthday");
        addBirthday.addClassName("edit-add-work-experience");

        Div birthdayDiv = new Div(birthdayIcon, addBirthday);
        birthdayDiv.addClassName("edit-work-div");

        Div lineDiv = new Div();
        lineDiv.addClassName("edit-line-div");

        return new VerticalLayout(headerText, genderDiv, birthdayDiv, lineDiv);
   }

   private VerticalLayout createContactLayout(){
   	Span headerText = new Span("Contact info");
        headerText.addClassName("edit-header-text");

        Icon phoneIcon = new Icon(VaadinIcon.PHONE);
        phoneIcon.addClassName("edit-work-icon");

        Span addPhoneNumber = new Span("Add phone number");
        addPhoneNumber.addClassName("edit-add-work-experience");

        Div phoneNumberDiv = new Div(phoneIcon, addPhoneNumber);
        phoneNumberDiv.addClassName("edit-work-div");

	Icon emailIcon = new Icon(VaadinIcon.ENVELOPE_O);
        emailIcon.addClassName("edit-work-icon");

        Span addEmail = new Span("Add email");
        addEmail.addClassName("edit-add-work-experience");

        Div emailDiv = new Div(emailIcon, addEmail);
        emailDiv.addClassName("edit-work-div");

        Div lineDiv = new Div();
        lineDiv.addClassName("edit-line-div");

        return new VerticalLayout(headerText, phoneNumberDiv, emailDiv, lineDiv);
   }

   private VerticalLayout createPlacesLivedLayout(){
   	Span headerText = new Span("Places lived");
        headerText.addClassName("edit-header-text");

        StreamResource resource = new StreamResource("hometown.svg",
                () -> getClass().getResourceAsStream("/META-INF/resources/icons/hometown.svg"));

        SvgIcon hometownIcon = new SvgIcon(resource);
        hometownIcon.addClassName("edit-work-icon");

        Span addHometown = new Span("Add hometown");
        addHometown.addClassName("edit-add-work-experience");

        Div hometownDiv = new Div(hometownIcon, addHometown);
        hometownDiv.addClassName("edit-work-div");

        Icon cityIcon = new Icon(VaadinIcon.HOME);
        cityIcon.addClassName("edit-work-icon");

        Span addCity = new Span("Add city");
        addCity.addClassName("edit-add-work-experience");

        Div cityDiv = new Div(cityIcon, addCity);
        cityDiv.addClassName("edit-work-div");

        Div lineDiv = new Div();
        lineDiv.addClassName("edit-line-div");

        return new VerticalLayout(headerText, hometownDiv, cityDiv, lineDiv);
   }

   private VerticalLayout createEducationLayout(){
   	Span headerText = new Span("Education");
        headerText.addClassName("edit-header-text");

        Icon collegeIcon = new Icon(VaadinIcon.ACADEMY_CAP);
        collegeIcon.addClassName("edit-work-icon");

        Span addCollege = new Span("Add college");
        addCollege.addClassName("edit-add-work-experience");

        Div collegeDiv = new Div(collegeIcon, addCollege);
        collegeDiv.addClassName("edit-work-div");

	StreamResource resource = new StreamResource("highschool.svg",
                () -> getClass().getResourceAsStream("/META-INF/resources/icons/highschool.svg"));

        SvgIcon highschoolIcon = new SvgIcon(resource);
        highschoolIcon.addClassName("edit-work-icon");

        Span addHighschool = new Span("Add high school");
        addHighschool.addClassName("edit-add-work-experience");

        Div highschoolDiv = new Div(highschoolIcon, addHighschool);
        highschoolDiv.addClassName("edit-work-div");

        Div lineDiv = new Div();
        lineDiv.addClassName("edit-line-div");

        return new VerticalLayout(headerText, collegeDiv, highschoolDiv, lineDiv);
   }

   private VerticalLayout createWorkLayout(){
   	Span headerText = new Span("Work");
   	headerText.addClassName("edit-header-text");

   	Icon workIcon = new Icon(VaadinIcon.WORKPLACE);
	workIcon.addClassName("edit-work-icon");

   	Span addWorkExperience = new Span("Add work experience");
   	addWorkExperience.addClassName("edit-add-work-experience");

   	Div workDiv = new Div(workIcon, addWorkExperience);
   	workDiv.addClassName("edit-work-div");

   	Div lineDiv = new Div();
        lineDiv.addClassName("edit-line-div");

   	return new VerticalLayout(headerText, workDiv, lineDiv);
   }

   private void createHeader(){
        Icon backButton = new Icon("lumo", "arrow-left");
        backButton.addClassName("header-back-button");
        backButton.addClickListener(event -> {
             UI.getCurrent().navigate(UpdateUserInfo.class);
        });

        Span text = new Span("About");
        text.addClassName("edit-bio-header-text");

        HorizontalLayout headerLayout = new HorizontalLayout(backButton, text);
        headerLayout.addClassName("edit-work-header-layout");

        addToNavbar(headerLayout);
    }
}
