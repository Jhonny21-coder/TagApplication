package com.example.application.views.password;

import com.example.application.data.User;
import com.example.application.services.UserServices;
import com.example.application.services.EmailService;

import com.vaadin.flow.component.textfield.TextField;
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
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import org.apache.commons.text.RandomStringGenerator;

/*import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;*/

import org.springframework.mail.SimpleMailMessage;

import java.util.Properties;
import java.util.Random;
import java.util.stream.Collectors;

@AnonymousAllowed
@Route("verification")
public class VerifyView extends AppLayout implements HasUrlParameter<String> {
    private final UserServices userService;
    private final EmailService emailService;

    private final String EMAIL_USERNAME = "shanemarahay@gmail.com";
    private final String EMAIL_PASSWORD = "azfi yhun igli arcc";

    private String generatedOTP;
    private String userInputOTP = "";
    private HorizontalLayout verificationCodeLayout;

    public VerifyView(UserServices userService, EmailService emailService){
        this.userService = userService;
        this.emailService = emailService;

        addClassName("profile-app-layout");
    }

    @Override
    public void setParameter(BeforeEvent event, String userEmail){
        createHeader();
        createVerifyNumbers(userEmail);
    }

    public void createVerifyNumbers(String userEmail) {
    	User user = userService.findUserByEmail(userEmail);

    	Span invalidText = new Span();
        invalidText.addClassName("forgot-invalid-text");
        invalidText.setVisible(false);

	Span sentText = new Span("We have sent a 6-digit verification code to your email.");
	sentText.addClassName("forgot-sent-text");

    	Span maskedEmailText = new Span();
    	maskedEmailText.addClassName("forgot-masked-email-text");

    	Span enterText = new Span("Enter the verification code.");
    	enterText.addClassName("forgot-enter-text");

	Span sendAgainText = new Span("Didn't recieve the code?");
	sendAgainText.addClassName("forgot-send-again");

    	Span resendText = new Span("Resend code.");
    	resendText.addClassName("forgot-resend");
    	resendText.addClickListener(event -> {
            sendVerificationEmail(userEmail);
            resetVerificationCodeFields(verificationCodeLayout);
            invalidText.setVisible(false);
        });

        // Send verification code to given email
        sendVerificationEmail(userEmail);
        user.setVerification(generatedOTP);
        userService.updateUser(user);

        String[] parts = userEmail.split("@");

	if (parts.length == 2) {
            // Get the local part and domain part
            String localPart = parts[0];
            String domainPart = parts[1];

            int visibleLength = 3;
	    String maskedEmail = "";

	    // Ensure at least the first 3 characters are visible
            if(localPart.length() <= visibleLength){
            	// If local part is too short, just use it as is
            	maskedEmail = localPart + "@" + domainPart;
            }else{
            	// Create masked part
            	String maskedPart = "*".repeat(localPart.length() - visibleLength);

		// Combine visible part with masked part
            	maskedEmail = localPart.substring(0, visibleLength) + maskedPart + "@" + domainPart;
            }

            // Set masked email
            maskedEmailText.setText(maskedEmail);
        } else {
            System.out.println("Invalid email address");
        }

	Button verifyButton = new Button("Verify");
	verifyButton.addClassName("forgot-verify-button");
        verifyButton.addClickListener(event -> {
            boolean isEmpty = false;
            for (Component component : verificationCodeLayout.getChildren().toArray(Component[]::new)) {
            	if (((TextField) component).getValue().isEmpty()){
            	   isEmpty = true;
            	}
            }

            if (isEmpty){
                Notification.show("Please complete the verification code", 2000, Notification.Position.MIDDLE)
		    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }else{
            	invalidText.setVisible(true);

            	String userVerification = user.getVerification();
                if (verifyOTP(userInputOTP, userVerification)){
                    invalidText.getElement().setAttribute("theme", "badge success");
		    UI.getCurrent().navigate(RecoverView.class, userEmail);
		}else{
                    invalidText.getElement().setAttribute("theme", "badge error");
                }
            }
        });

	verificationCodeLayout = generateVerificationCodeFields(6, invalidText, user);

	VerticalLayout layout = new VerticalLayout(invalidText, sentText, maskedEmailText,
		enterText, verificationCodeLayout, verifyButton, sendAgainText,
		resendText
	);
	layout.addClassName("forgot-verify-layout");

	setContent(layout);
    }

    private void resetVerificationCodeFields(HorizontalLayout layout) {
        for (Component component : layout.getChildren().toArray(Component[]::new)) {
            if (component instanceof TextField) {
                ((TextField) component).clear();
            }
        }
    }

    private HorizontalLayout generateVerificationCodeFields(int length, Span invalidText, User user) {
        HorizontalLayout layout = new HorizontalLayout();

        for (int i = 0; i < length; i++) {
            TextField digitField = new TextField();
            digitField.setValueChangeMode(ValueChangeMode.EAGER);
            digitField.setAllowedCharPattern("[0-9]");
            digitField.addClassName("digit-field");
            digitField.setMaxLength(1);
            digitField.addValueChangeListener(event -> {
                String value = event.getValue();
                userInputOTP = getUserInputOTP();

                if (!value.isEmpty()) {
                    int currentIndex = layout.getChildren().collect(Collectors.toList()).indexOf(digitField);
                    if (currentIndex < layout.getChildren().count() - 1) {
                        TextField nextField = (TextField) layout.getChildren().toArray()[currentIndex + 1];
                        nextField.focus();
                    }
                }

		invalidText.setVisible(true);
                if (value.isEmpty()){
                   invalidText.setText("Invalid verification code.");
                   invalidText.getElement().setAttribute("theme", "badge error");
                }else{
                   String userVerification = user.getVerification();
                   if (verifyOTP(userInputOTP, userVerification)){
                       invalidText.setText("Valid verification code.");
                       invalidText.getElement().setAttribute("theme", "badge success");
                   }else{
                       invalidText.setText("Invalid verification code.");
                       invalidText.getElement().setAttribute("theme", "badge error");
                   }
                }
            });
            layout.add(digitField);
        }
        return layout;
    }

    private String getUserInputOTP() {
        StringBuilder numbers = new StringBuilder();

        for (Component component : verificationCodeLayout.getChildren().toArray(Component[]::new)) {
            TextField digitField = (TextField) component;
            numbers.append(digitField.getValue());
        }
        return numbers.toString();
    }

    public String generateOTP(int length) {
        Random random = new Random();
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < length; i++) {
            otp.append(random.nextInt(10));
        }
        return otp.toString();
    }

    public boolean verifyOTP(String userInputOTP, String userVerification) {
        return userInputOTP.equals(userVerification);
    }

    private void sendVerificationEmail(String email){
        generatedOTP = generateOTP(6);

    	SimpleMailMessage registrationEmail = new SimpleMailMessage();
	registrationEmail.setTo(email);
	registrationEmail.setSubject("Email Verification");
	registrationEmail.setText("Here is your 6 digit verification code: " + generatedOTP);
	registrationEmail.setFrom("noreply@example.com");

	emailService.sendEmail(registrationEmail);
    }

    /*public void sendVerificationEmail(String userEmail) {
	// Generate OTP
        generatedOTP = generateOTP(6);

	// Email configuration
	String host = "smtp.gmail.com";
	Properties properties = System.getProperties();
	properties.setProperty("mail.smtp.host", host);
	properties.setProperty("mail.smtp.port", "587");
	properties.setProperty("mail.smtp.auth", "true");
	properties.setProperty("mail.smtp.starttls.enable", "true");

	// Authenticator to log in to your email account
	Authenticator authenticator = new Authenticator() {
		@Override
		protected PasswordAuthentication getPasswordAuthentication() {
			return new PasswordAuthentication(EMAIL_USERNAME, EMAIL_PASSWORD);
		}
	};

	// Create a session with the email server
	Session session = Session.getDefaultInstance(properties, authenticator);

	try {
	   // Create a default MimeMessage object
	   MimeMessage message = new MimeMessage(session);

	   // Set From: header field of the header
	   message.setFrom(new InternetAddress(EMAIL_USERNAME));

	   // Set To: header field of the header
	   message.addRecipient(Message.RecipientType.TO, new InternetAddress(userEmail));

	   // Set Subject: header field
	   message.setSubject("TAG Verification Number");

	   // Set the actual message as HTML content
	   message.setContent("<h1>Your verification number is:</h1><p>" + generatedOTP + "</p>" + "<h2>Enter it to reset your password.<h2>", "text/html");

	   // Send message
	   Transport.send(message);
	   System.out.println("Email sent successfully with veritication number: " + generatedOTP);
	} catch (MessagingException mex) {
	   mex.printStackTrace();
	}
    }*/

    private void createHeader(){
        Icon backIcon = new Icon("lumo", "arrow-left");
        backIcon.addClassName("header-back-button");
        backIcon.addClickListener(event -> {
            UI.getCurrent().navigate(ForgotPassword.class);
        });

        Span notificationText = new Span("Verification");
        notificationText.addClassName("forgot-password-text");

        HorizontalLayout headerLayout = new HorizontalLayout(backIcon, notificationText);
        headerLayout.addClassName("forgot-header-layout");

        addToNavbar(headerLayout);
    }
}
