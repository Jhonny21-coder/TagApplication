package com.example.application.views;

// LoginView.java
import com.example.application.services.UserServices;
import com.example.application.data.User;
import com.example.application.data.Status;
import com.example.application.services.StatusService;
import com.example.application.views.password.ForgotPassword;
import com.example.application.views.password.RandomGeneratorView;

import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.component.html.Span;

import org.springframework.core.env.Environment;
import com.vaadin.flow.component.html.Anchor;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.FileWriter;

import java.util.List;

@Route("login")
@PageTitle("Login | TAG")
@AnonymousAllowed
public class LoginView extends AppLayout implements BeforeEnterObserver {

    private final UserServices userService;
    private final StatusService statusService;
    private final LoginForm loginForm = new LoginForm();
    private final String OAUTH_URL = "/oauth2/authorization/google";

    public LoginView(UserServices userService, StatusService statusService,
    	@Autowired Environment env) {
        this.userService = userService;
        this.statusService = statusService;
	addClassName("form");
	createLoginLayout(env);

	User user = userService.findCurrentUser();

	if(user != null){
	   Status status = statusService.getStatusByUserEmail(user.getEmail());

	   if(status.getStatus().equals("ONLINE")){
	      statusService.updateStatus(user.getEmail());
	   }
	}
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
	// Check if query parameters from login form contains error then i>
	if(beforeEnterEvent.getLocation()
           .getQueryParameters()
           .getParameters()
           .containsKey("error")) {
               // Set the in built login error message
               loginForm.setError(true);
	}
    }

    private void createLoginLayout(@Autowired Environment env){
    	LoginI18n i18n = LoginI18n.createDefault();

	LoginI18n.Form i18nForm = i18n.getForm();
	i18nForm.setUsername("Email");
	i18nForm.setForgotPassword("Forgot password?");
	i18n.setForm(i18nForm);

	LoginI18n.ErrorMessage i18nErrorMessage = i18n.getErrorMessage();
	i18nErrorMessage.setTitle("Incorrect email or password");
	i18nErrorMessage.setMessage(
          "Check that you have entered the corrent email and password and try again.");
	i18n.setErrorMessage(i18nErrorMessage);

	loginForm.getElement().setAttribute("no-autofocus", "");
	loginForm.addClassName("login-form");
	loginForm.setI18n(i18n);
    	loginForm.setAction("login");
    	loginForm.addLoginListener(event -> {
            // Get entered username
            String email = event.getUsername();
            // Get entered password
            String password = event.getPassword();

            System.out.println("\nIn Login");
            System.out.println("Email: " + email);
            System.out.println("Password: " + password);

            String filename = "src/main/resources/META-INF/resources/data/login.txt";

            boolean isExisting = isDataExisting(filename, email, password);

            if(isExisting) {
               System.out.println("Data already exists, cannot save.");
            }else{
                try(PrintWriter writer = new PrintWriter(new FileWriter(filename, true))){
                    writer.println(email + ", " + password);
                    System.out.println("Successfully saved to " + filename);
                }catch(Exception e) {
                    System.out.println(e.getMessage());
                }
            }
        });

        loginForm.addForgotPasswordListener(event -> {
            //UI.getCurrent().navigate(ForgotPassword.class);
            UI.getCurrent().navigate(RandomGeneratorView.class);
        });

	/*// Create forgot password link
	Anchor forgotPassword = new Anchor("forgotPassword", "Forgot password?");
        forgotPassword.addClassName("forgot-password");*/

	Button registerButton = new Button("No account yet? Register here.");
	registerButton.addClassName("login-register-button");
	registerButton.addClickListener(event -> {
	     UI.getCurrent().navigate(RegisterView.class);
	});

	String clientkey = env.getProperty("spring.security.oauth2.client.registration.google.client-id");

	StreamResource resource = new StreamResource("google.svg",
                () -> getClass().getResourceAsStream("/META-INF/resources/icons/google.svg"));

        Div firstLine = new Div();
        firstLine.addClassName("first-line");

        Span orText = new Span("Or");
        Div lastLine = new Div();
        lastLine.addClassName("last-line");

        HorizontalLayout orLayout = new HorizontalLayout(firstLine, orText, lastLine);
        orLayout.addClassName("login-or-layout");

        SvgIcon googleIcon = new SvgIcon(resource);

        Button googleButton = new Button("LOGIN WITH GOOGLE", googleIcon);
        googleButton.setEnabled(false);
        googleButton.addClassName("google-button");
        googleButton.addClickListener(event -> {
            UI.getCurrent().getPage().setLocation(OAUTH_URL);
        });

	VerticalLayout formLayout = new VerticalLayout(loginForm, orLayout, googleButton, registerButton);
	formLayout.addClassName("login-form-layout");

        setContent(formLayout);
    }

    public boolean isDataExisting(String filename, String email, String password) {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                // Split the line into label and data parts
                String[] parts = line.split(", ");
                if (parts.length == 2 && parts[0].equals(email) && parts[1].equals(password)) {
                    return true; // Data exists in the file
                }
            }
        } catch (IOException e) {
    	    e.printStackTrace();
        }
        return false; // Data does not exist in the file
    }
}


