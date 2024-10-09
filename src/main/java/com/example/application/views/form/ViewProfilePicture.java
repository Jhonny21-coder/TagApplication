package com.example.application.views.form;

import com.example.application.data.User;
import com.example.application.services.UserServices;
import com.example.application.views.profile.OwnProfile;

import com.vaadin.flow.router.Route;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.BeforeEvent;
import jakarta.annotation.security.PermitAll;

import java.util.Set;

import java.io.FileInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;

@PermitAll
@Route("view-profile-picture")
public class ViewProfilePicture extends AppLayout implements HasUrlParameter<String> {

    private final UserServices userService;

    public ViewProfilePicture(UserServices userService){
    	this.userService = userService;

    	addClassName("view-profile-app-layout");
    }

    @Override
    public void setParameter(BeforeEvent event, String saveType) {
    	createHeader();

    	User user = userService.findCurrentUser();

    	Image image = new Image();
    	image.addClassName("view-profile-image");

	String currentFilename = "";
	if(saveType.equalsIgnoreCase("Profile")){
    	   String imageUrl = user.getProfileImage();
    	   image.setSrc(imageUrl);
    	}else if(saveType.equalsIgnoreCase("Cover")){
	   String imageUrl = user.getCoverPhoto();
	   image.setSrc(imageUrl);
    	}

        FormLayout formLayout = new FormLayout();
        formLayout.addClassName("view-profile-form-layout");
        formLayout.add(image);

        setContent(formLayout);
    }

    private void createHeader(){
    	Icon backIcon = new Icon("lumo", "arrow-left");
    	backIcon.addClassName("header-back-button");
    	backIcon.addClickListener(event -> {
    	    Set<String> sessionNames = VaadinSession.getCurrent().getSession().getAttributeNames();

            for(String sessionName : sessionNames){
                if(sessionName.equals("own-profile")){
                   VaadinSession.getCurrent().getSession().removeAttribute("own-profile");
                   UI.getCurrent().navigate(OwnProfile.class);
                }else if(sessionName.equals("edit-profile")){
                   VaadinSession.getCurrent().getSession().removeAttribute("edit-profile");
                   UI.getCurrent().navigate(UpdateUserInfo.class);
                }
            }
    	});

    	HorizontalLayout headerLayout = new HorizontalLayout(backIcon);

    	addToNavbar(headerLayout);
    }
}
