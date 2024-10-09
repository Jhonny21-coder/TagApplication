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
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.component.html.Anchor;

import org.apache.commons.text.RandomStringGenerator;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;
import java.util.Random;
import java.util.stream.Collectors;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;

import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

@AnonymousAllowed
@Route("random")
public class RandomGeneratorView extends AppLayout {
    private final EmailService emailService;

    private final String EMAIL_USERNAME = "shanemarahay@gmail.com";
    private final String EMAIL_PASSWORD = "azfi yhun igli arcc";

    private Button generateButton = new Button("Generate and send", new Icon("vaadin", "paperplane"));
    private TextField emailField = new TextField();
    private TextField nameField = new TextField();

    public RandomGeneratorView(EmailService emailService){
        this.emailService = emailService;

        addClassName("profile-app-layout");

        Map<Integer, String> names = new HashMap<>();
        Set<String> processedStudents = new HashSet<>();


	List<String> students = getStudents();

	for (int i = 0; i < students.size(); i++) {
	    names.put(i, students.get(i));
	}

	Random random = new Random();

	while (processedStudents.size() < 22) {
	       String name = students.get(random.nextInt(students.size()));
	       processedStudents.add(name);
	}

	Map<String, String> nameEmailMap = getNameEmailMap();

        VerticalLayout nameLayout = new VerticalLayout();

        // Button to generate and trigger the PDF download
        Button generateButton = new Button("Generate and Send");
        generateButton.addClassName("generator-button");
        generateButton.addClickListener( e -> {
            String email = emailField.getValue();
	    String name = nameField.getValue();
	    sendEmail(email, name);

	    Notification.show("Sent email: " + name, 5000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });

	int counter = 1;
	for(String student : processedStudents){
            Span nameSpan = new Span(counter + ". " + student + " = " + nameEmailMap.get(student));
            nameLayout.add(nameSpan);
            counter++;
	}

	// Generate the PDF content dynamically
	byte[] pdfContent = generatePdfWithNamesInGrid(processedStudents);

	// Create a new StreamResource with the generated PDF
	StreamResource resource = new StreamResource("names_list.pdf",
		() -> new ByteArrayInputStream(pdfContent));
	resource.setContentType("application/pdf");

	// Create a download link (Anchor)
	Anchor downloadLink = new Anchor(resource, "Download Names PDF");
	downloadLink.getElement().setAttribute("download", true);  // Make sure it's a download link

        VerticalLayout layout = new VerticalLayout();
        layout.addClassName("generator-layout");

        layout.add(emailField, nameField, generateButton, nameLayout, downloadLink);

        setContent(layout);
    }

    private List<String> getStudents(){
    	List<String> students = Arrays.asList(
                "AREDIDON, GIAN REY",
                "ARRAZ, MARK DHANLOU",
                "AYONG, PRINCESS ATHEA",
                "BALBOA, ROCKY",
                "BEBIANO, JESTAR",
                "BOCOBO, LJ HEART",
                "BOLITRES, ANCEL GWYNETH",
                "CAILO, AIME",
                //"CALAGOS, AICIEL",
                "CALDERON, RECHIE",
                //"CARDEÑO, BRENDON LEIGH",
                //"CARIJUTAN, JANBERS",
                "CASIN, FRANCESS ANNE",
                "CONZON, SHANE",
                "COTANAS, RIO MARK",
                "DELA CRUZ, JOVIT",
                //"DIAZ, CHRISTIAN DENNIS",
                "DISBARRO, SALVADOR JR.",
                "DOINOG, JIM NATHANIEL",
                "DULTRA, ALMYR",
                "ELIZALDE, NICOLE",
                "GABING, JANNIEL ROSE",
                //"GABRAL, FERNANDO JOSE",
                "GALVAN, EMMAN",
                "JABOLI, ANGELO",
                "KING, ALLEN ICE",
                "LEPATA, JOHN MICHAEL",
                //"LOCAÑAS, JONAHLD",
                "MAHILUM, LOURD CEDRIC",
                //"MARAHAY, JHON LEE",
                //"MERILLES, RYAN",
                "MORENO, MANDRAKE",
                "NIEDO, SHAILYN",
                "NIETO, PRINCE JELVIN",
                "OLASIMAN, JUNDY",
                "PADILLO, JENNYLYN",
                "PICORRO, DARIUS KYLE",
                "QUITALLAS, TREXIE",
                "SALURIO, JOSIAH JAMES",
                "SANTOS, ASHLEY",
                "SILDO, JHON CARLO",
                "SONGALIA, MA. DIANA",
                "TOMALE, MA.VENNIS",
                "VILLANUEVA, ALDYN",
                "YLAR, JUSTIN"
        );

        return students;
    }

    private Map<String, String> getNameEmailMap(){
      	Map<String, String> nameEmailMap = new HashMap<>();

	nameEmailMap.put("AREDIDON, GIAN REY", "aredidongianrey@gmail.com");
	nameEmailMap.put("ARRAZ, MARK DHANLOU", "markdhanloudabalos@gmail.com");
	nameEmailMap.put("AYONG, PRINCESS ATHEA", "princessathea47@gmail.com");
	nameEmailMap.put("BALBOA, ROCKY", "drocky0817@gmail.com");
	nameEmailMap.put("BEBIANO, JESTAR", "jestarbebiano28@gmail.com");
	nameEmailMap.put("BOCOBO, LJ HEART", "lujheyheart14@gmail.com");
	nameEmailMap.put("BOLITRES, ANCEL GWYNETH", "ancelgwynethbolitres@gmail.com");
	nameEmailMap.put("CAILO, AIME", "cailoaime62@gmail.com");
	//nameEmailMap.put("CALAGOS, AICIEL", "");
	nameEmailMap.put("CALDERON, RECHIE", "bhongzchie45@gmail.com");
	//nameEmailMap.put("CARDEÑO, BRENDON LEIGH", "");
	//nameEmailMap.put("CARIJUTAN, JANBERS", "");
	nameEmailMap.put("CASIN, FRANCESS ANNE", "balanquitanne36@gmail.com");
	nameEmailMap.put("CONZON, SHANE", "conzonshane123@gmail.com");
	nameEmailMap.put("COTANAS, RIO MARK", "riomarkcotanas75@gmail.com");
	nameEmailMap.put("DELA CRUZ, JOVIT", "jovitutot@gmail.com");
	//nameEmailMap.put("DIAZ, CHRISTIAN DENNIS", "");
	nameEmailMap.put("DISBARRO, SALVADOR JR.", "disbarrobrando6@gmail.com");
	nameEmailMap.put("DOINOG, JIM NATHANIEL", "jimnathanieldoinog@gmail.com");
	nameEmailMap.put("DULTRA, ALMYR", "almyrdultra52@gmail.com");
	nameEmailMap.put("ELIZALDE, NICOLE", "elizaldenicole99@gmail.com");
	nameEmailMap.put("GABING, JANNIEL ROSE", "gabing.jannielrose@nwssu.edu.ph");
	//nameEmailMap.put("GABRAL, FERNANDO JOSE", "balatsgabral@gmail.com");
	nameEmailMap.put("GALVAN, EMMAN", "emmangalvan4@gmail.com");
	nameEmailMap.put("JABOLI, ANGELO", "angelojaboli3@gmail.com");
	nameEmailMap.put("KING, ALLEN ICE", "Allenicebking@gmail.com");
	nameEmailMap.put("LEPATA, JOHN MICHAEL", "johnlepats@gmail.com");
	//nameEmailMap.put("LOCAÑAS, JONAHLD", "");
	nameEmailMap.put("MAHILUM, LOURD CEDRIC", "lourdcedricmahilum@gmail.com");
	//nameEmailMap.put("MARAHAY, JHON LEE", "jhonleemarahay@gmail.com");
	//nameEmailMap.put("MERILLES, RYAN", "merillesryan9@gmail.com");
	nameEmailMap.put("MORENO, MANDRAKE", "dzzydrake@gmail.com");
	nameEmailMap.put("NIEDO, SHAILYN", "shailynniedo24@gmail.com");
	nameEmailMap.put("NIETO, PRINCE JELVIN", "nietojelvin@gmail.com");
	nameEmailMap.put("OLASIMAN, JUNDY", "jundyolasiman19@gmail.com");
	nameEmailMap.put("PADILLO, JENNYLYN", "jennylynpadillo1184@gmail.com");
	nameEmailMap.put("PICORRO, DARIUS KYLE", "kingdaris23@gmail.com");
	nameEmailMap.put("QUITALLAS, TREXIE", "trexquitallas@gmail.com");
	nameEmailMap.put("SALURIO, JOSIAH JAMES", "saluriojosiah.js@gmail.com");
	nameEmailMap.put("SANTOS, ASHLEY", "ashleyot05@gmail.com");
	nameEmailMap.put("SILDO, JHON CARLO", "jhoncarlosildo@gmail.com");
	nameEmailMap.put("SONGALIA, MA. DIANA", "mariadianaaa2005@gmail.com");
	nameEmailMap.put("TOMALE, MA.VENNIS", "mavennistomale1@gmail.com");
	nameEmailMap.put("VILLANUEVA, ALDYN", "aldynt.villanueva@gmail.com");
	nameEmailMap.put("YLAR, JUSTIN", "Brojustinylar@gmail.com");

	return nameEmailMap;
    }

    public byte[] generatePdfWithNamesInGrid(Set<String> names) {
    	try {
            // Create a byte array output stream
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            // Create a table with one column for names
            float[] columnWidths = {300F}; // Define column width for the single column
            Table table = new Table(columnWidths);
            //table.setHorizontalAlignment(HorizontalAlignment.CENTER);

            Cell headerCell = new Cell();
            headerCell.add(new Paragraph("Member Names"));//.setTextAlignment(TextAlignment.CENTER));

            table.addHeaderCell(headerCell);

	    int counter = 1;
            // Add each name as a row in the table
            for (String name : names) {
            	String numberedName = counter + ". " + name;
            	Cell nameCell = new Cell();
            	nameCell.add(new Paragraph(numberedName));//.setTextAlignment(TextAlignment.CENTER));

            	table.addCell(nameCell);

            	counter++;
            }

            // Add the table to the document
            document.add(table);
            // Close the document and return the PDF bytes
            document.close();
            return baos.toByteArray();  // Return the PDF as byte array
    	} catch (Exception e) {
            e.printStackTrace();
            return null;
    	}
    }

    public void sendEmail(String userEmail, String name) {
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
	   message.setFrom(new InternetAddress(EMAIL_USERNAME, "Marahay Team"));

	   // Set To: header field of the header
	   message.addRecipient(Message.RecipientType.TO, new InternetAddress(userEmail));

	   // Set Subject: header field
	   message.setSubject("CONGRATULATOINS!");

	   String htmlFilePath = "src/main/resources/META-INF/resources/email/email-templates/5/index.html";

	   String emailBody = loadHtmlContent(htmlFilePath);

	   /*message.setContent(
    	   "<div style='font-family: Arial, sans-serif; color: #333;'>"
        	+ "<h2 style='color: #4CAF50;'>Congratulations, " + name + "!</h2>"
        	+ "<p style='font-size: 16px;'>"
        	+ "You have been selected to join <strong>Marahay's group</strong>."
       	 	+ " We're excited to have you on board!</p>"
        	+ "<p style='font-size: 16px;'>To join the Group Chat, simply click the button below:</p>"
        	+ "<a href='https://m.me/j/AbZEYBl39VWtnAsk/' style='display: inline-block; background-color: #4CAF50; color: white; padding: 10px 20px; text-decoration: none; font-size: 16px; border-radius: 5px;'>Join Group Chat</a>"
        	+ "<p style='margin-top: 20px; font-size: 14px; color: #777;'>"
        	+ "If you have any questions or need assistance, feel free to reach out to us.</p>"
        	+ "<p style='font-size: 14px; color: #777;'>Best regards,<br>The Marahay Team</p>"
    	   + "</div>",
    	   "text/html");*/
    	   message.setContent(emailBody, "text/html");

	   // Send message
	   Transport.send(message);
	} catch (Exception mex) {
	   mex.printStackTrace();
	}
    }

    // Method to load HTML content from a file
    private static String loadHtmlContent(String filePath) {
        StringBuilder content = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                content.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content.toString();
    }

    private void createHeader(){
        Icon backIcon = new Icon("lumo", "arrow-left");
        backIcon.addClassName("header-back-button");
        backIcon.addClickListener(event -> {
            UI.getCurrent().navigate(ForgotPassword.class);
        });

        Span notificationText = new Span("Random Generator");
        notificationText.addClassName("forgot-password-text");

        HorizontalLayout headerLayout = new HorizontalLayout(backIcon, notificationText);
        headerLayout.addClassName("forgot-header-layout");

        addToNavbar(headerLayout);
    }
}
