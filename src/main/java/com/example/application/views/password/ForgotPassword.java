package com.example.application.views.password;

import com.example.application.data.User;
import com.example.application.services.UserServices;
import com.example.application.views.LoginView;

import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@AnonymousAllowed
@Route("forgotPassword")
public class ForgotPassword extends AppLayout {

    private final UserServices userService;

    public ForgotPassword(UserServices userService){
        this.userService = userService;
	createHeader();
        createSenderVerificationLayout();
        addClassName("profile-app-layout");
     }

     public void createSenderVerificationLayout(){
        Span loseText = new Span("Lost your password?");
        loseText.addClassName("forgot-lose-text");

        Span recoverText = new Span("Reset here through your email address.");
        recoverText.addClassName("forgot-recover-text");

	EmailField emailField = new EmailField("Enter your email address.");
	emailField.setPlaceholder("company@example.com");
        emailField.setErrorMessage("Please enter a valid email address");
        emailField.setHelperText("You may receive a 6 digit verification code from our security team.");
        emailField.setSuffixComponent(new Icon(VaadinIcon.ENVELOPE));
        emailField.addClassName("forgot-email-field");

	Button sendButton = new Button("Send Verification");
        sendButton.addClassName("forgot-send-button");
        sendButton.addClickListener(e -> {
            String emailValue = emailField.getValue();
            User user = userService.findUserByEmail(emailValue);

            if(user != null){
                UI.getCurrent().navigate(VerifyView.class, emailValue);
            }else if(emailField.getValue().isEmpty()){
            	Notification.show("Email cannot be empty", 2000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }else{
                Notification.show("No user found with the given email", 2000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        VerticalLayout mainLayout = new VerticalLayout(loseText, recoverText, emailField, sendButton);
	mainLayout.addClassName("forgot-main-layout");

        setContent(mainLayout);
    }

    private void createHeader(){
        Icon backIcon = new Icon("lumo", "arrow-left");
        backIcon.addClassName("header-back-button");
        backIcon.addClickListener(event -> {
            UI.getCurrent().navigate(LoginView.class);
        });

        Span notificationText = new Span("Forgot Password");
        notificationText.addClassName("forgot-password-text");

        HorizontalLayout headerLayout = new HorizontalLayout(backIcon, notificationText);
        headerLayout.addClassName("forgot-header-layout");

        addToNavbar(headerLayout);
    }
}
