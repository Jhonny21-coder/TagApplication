package com.example.application.views.password;

import com.example.application.data.User;
import com.example.application.services.UserServices;
import com.example.application.views.LoginView;

import com.vaadin.flow.component.textfield.PasswordField;
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
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

@AnonymousAllowed
@Route("recoverPassword")
public class RecoverView extends AppLayout implements HasUrlParameter<String> {
    private final UserServices userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public RecoverView(UserServices userService){
        this.userService = userService;

        addClassName("profile-app-layout");
    }

    @Override
    public void setParameter(BeforeEvent event, String userEmail){
        createHeader();
        createRecoverPassword(userEmail);
    }

    private void createRecoverPassword(String email){
    	User user = userService.findUserByEmail(email);

    	Span passwordStrength = new Span();
    	passwordStrength.setVisible(false);
        passwordStrength.addClassName("forgot-password-strength");

	String helperText = "For secure password, it must contain uppercase and lowercase letters, numbers, special characters, and must not contain spaces.";

	PasswordField newPasswordField = new PasswordField("Enter your new password.");
	newPasswordField.setHelperText(helperText);
	newPasswordField.addClassName("forgot-new-password-field");
	newPasswordField.setValueChangeMode(ValueChangeMode.EAGER);
	newPasswordField.addValueChangeListener(event -> {
	    passwordStrength.setVisible(true);

	    int length = event.getValue().length();

	    if(length >= 12){
	       passwordStrength.setText("Strong");
	       passwordStrength.getElement().setAttribute("theme", "badge success");
	    }else if(length >= 7){
               passwordStrength.setText("Moderate");
               passwordStrength.getElement().setAttribute("theme", "badge warning");
            }else if(length <= 7){
               passwordStrength.setText("Weak");
               passwordStrength.getElement().setAttribute("theme", "badge error");
            }
	});

	Span resetText = new Span("Reset your password.");
	resetText.addClassName("forgot-rest-text");

	Span cancelButton = new Span("Cancel");
	cancelButton.addClassName("forgot-cancel-button");
	cancelButton.addClickListener(event -> {
	     UI.getCurrent().navigate(LoginView.class);
	});

	ConfirmDialog dialog = new ConfirmDialog();
	dialog.addClassName("forgot-confirm-dialog");
	dialog.setCancelable(true);
	dialog.setConfirmText("Yes");
	dialog.setConfirmButtonTheme("primary");
	dialog.setHeader("Are you sure you want to reset your password?");
	dialog.addConfirmListener(event -> {
	     String newPasswordValue = newPasswordField.getValue();
             String encryptedPassword = passwordEncoder.encode(newPasswordValue);

             user.setPassword(encryptedPassword);
             userService.updateUser(user);

             Notification.show("Password changed successfully", 3000, Notification.Position.TOP_CENTER)
                 .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

             UI.getCurrent().navigate(LoginView.class);
	});

	Button resetButton = new Button("Reset");
        resetButton.addClassName("forgot-reset-button");
	resetButton.addClickListener(event -> {
	     String newPasswordValue = newPasswordField.getValue();

	     if(!newPasswordValue.isEmpty() && !newPasswordValue.matches("\\s*")){
	        dialog.open();
	     }else{
	     	Notification.show("Password cannot be empty", 1000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
	     }
	});

	VerticalLayout layout = new VerticalLayout(passwordStrength, resetText, newPasswordField, resetButton, cancelButton);
	layout.addClassName("forgot-verify-layout");

	setContent(layout);
    }

    private void createHeader(){
        Icon backIcon = new Icon("lumo", "arrow-left");
        backIcon.addClassName("header-back-button");
        backIcon.addClickListener(event -> {
            UI.getCurrent().navigate(ForgotPassword.class);
        });

        Span notificationText = new Span("Recovery");
        notificationText.addClassName("forgot-recovery-text");

        HorizontalLayout headerLayout = new HorizontalLayout(notificationText);
        headerLayout.addClassName("forgot-header-layout");

        addToNavbar(headerLayout);
    }
}
