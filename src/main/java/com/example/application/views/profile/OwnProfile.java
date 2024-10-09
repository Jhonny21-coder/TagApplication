package com.example.application.views.profile;

import com.example.application.views.UploadsI18N;
import com.example.application.data.PostReaction;
import com.example.application.services.PostReactionService;
import com.example.application.data.Follower;
import com.example.application.services.FollowerService;
import com.example.application.data.StudentInfo;
import com.example.application.data.Contact;
import com.example.application.services.ContactService;
import com.example.application.data.User;
import com.example.application.services.UserServices;
import com.example.application.data.Artwork;
import com.example.application.services.ArtworkService;
import com.example.application.views.MainFeed;
import com.example.application.views.story.StoryView;
import com.example.application.views.form.UpdateUserInfo;
import com.example.application.views.form.ViewProfilePicture;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.Receiver;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.server.VaadinSession;
import jakarta.annotation.security.PermitAll;

import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.File;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import java.text.DecimalFormat;

import com.example.application.services.LikeReactionService;
import com.example.application.services.HeartReactionService;
import com.example.application.services.CommentService;

@PermitAll
@Route("profile-section")
public class OwnProfile extends AppLayout {

    private final ArtworkService artworkService;
    private final ContactService contactService;
    private final FollowerService followerService;
    private final UserServices userService;
    private final PostReactionService postService;
    private final HorizontalLayout headerLayout = new HorizontalLayout();

    private final LikeReactionService likeService;
    private final HeartReactionService heartService;
    private final CommentService commentService;

    private final Upload profilePhotoUpload = new Upload();
    private final Upload coverPhotoUpload = new Upload();

    private String newFilename;
    private String coverPhotoFileName;

    public OwnProfile(ArtworkService artworkService, ContactService contactService,
    	FollowerService followerService, UserServices userService,
    	LikeReactionService likeService, HeartReactionService heartService,
    	CommentService commentService, PostReactionService postService){

	this.artworkService = artworkService;
	this.contactService = contactService;
	this.followerService = followerService;
	this.userService = userService;
	this.likeService = likeService;
        this.heartService = heartService;
        this.commentService = commentService;
        this.postService = postService;

	addClassName("profile-app-layout");

	getUser();
    }

    public void getUser(){
    	User user = userService.findCurrentUser();

	createHeader(user);
    	createFollowerLayout(user);
    }

    private Button createButtonListener(User user, HorizontalLayout layout){
    	List<Follower> followers = followerService.getFollowersByFollowedUserId(user.getId());

        User currentUser = userService.findCurrentUser();

        Follower currentFollower = followerService.getFollowerByFollowedUserIdAndFollowerId(user.getId(), currentUser.getId());
        AtomicBoolean userAlreadyFollowed = new AtomicBoolean(currentFollower != null);

        AtomicLong atomicLong = new AtomicLong(followers.size());

        Button followButton = new Button();
        updateFollowButton(followButton, userAlreadyFollowed.get());
        followButton.addClickListener(event -> {
            if (!userAlreadyFollowed.get()) {
                Follower newFollower = new Follower();
                newFollower.setFollowedUser(user);
                newFollower.setFollower(currentUser);
                newFollower.setFollowed(true);
                followerService.saveFollower(newFollower);
                updateFollowButton(followButton, true);

                atomicLong.incrementAndGet();
                // Update layout dynamically
                refreshFollowerLayout(user, layout, atomicLong.get());
                userAlreadyFollowed.set(true);
            } else {
                followerService.deleteFollowerByFollowedUserId(user.getId(), currentUser.getId());
                updateFollowButton(followButton, false);

                atomicLong.decrementAndGet();
                // Update layout dynamically
                refreshFollowerLayout(user, layout, atomicLong.get());
                userAlreadyFollowed.set(false);
            }
        });

        return followButton;
    }

    private HorizontalLayout createStatistics(User user, HorizontalLayout layout, Avatar avatar){
    	List<Artwork> artworks = artworkService.getArtworksByUserId(user.getId());
        List<Follower> followers = followerService.getFollowersByFollowedUserId(user.getId());

        User currentUser = userService.findCurrentUser();

        Long userFollowers = followerService.countFollowers(user.getId());

        Span totalFollowers = new Span();
        totalFollowers.addClassName("profile-total-followers");

        if(userFollowers == 0){
           totalFollowers.setText("");
        }else{
           totalFollowers.setText(formatValue(followers.size()));
        }

        Span totalArtworks = new Span();
        totalArtworks.addClassName("profile-total-artworks");

        if(artworks.size() == 0){
           totalArtworks.setText("");
        }else{
           totalArtworks.setText(formatValue(artworks.size()));
        }

        Span artworkText = new Span();
        artworkText.addClassName("profile-text");

        if(artworks.size() == 0){
           artworkText.setText("no artwork");
        }else if(artworks.size() == 1){
           artworkText.setText("artwork");
        }else{
           artworkText.setText("artworks");
        }

        Span followText = new Span("followers");
        followText.addClassName("profile-text");

        if(userFollowers == 0){
           followText.setText("no follower");
        }else if(userFollowers == 1){
           followText.setText("follower");
        }else{
           followText.setText("followers");
        }

        Div artworksLayout = new Div(totalArtworks, artworkText);
        artworksLayout.addClassName("own-profile-artworks-layout");

        Div followLayout = new Div(totalFollowers, followText);
        followLayout.addClassName("own-profile-follow-layout");

        Button editButton = new Button("Edit profile", new Icon("lumo", "edit"));
        editButton.addClassName("profile-edit-button");
        editButton.addClickListener(event -> {
             UI.getCurrent().navigate(UpdateUserInfo.class);
        });

        Button storyButton = new Button("Add to story", new Icon("lumo", "plus"));
	storyButton.addClassName("profile-happening-button");
	storyButton.addClickListener(event -> {
	     VaadinSession.getCurrent().getSession().setAttribute("own-profile", user.getId());
	     UI.getCurrent().navigate(StoryView.class);
	});

	Button moreButton = new Button();
	moreButton.addClassName("profile-more-button");
	moreButton.setIcon(new Icon(VaadinIcon.ELLIPSIS_H));

        return new HorizontalLayout(storyButton, editButton, moreButton);
    }

    public void handleFileUpload(){
        UploadsI18N i18n = new UploadsI18N();
        i18n.getError().setFileIsTooBig(
                "The image exceeds the maximum allowed size of 10MB.");
        i18n.getError().setIncorrectFileType(
                "The provided image does not have the correct format (PNG or JPEG).");

        profilePhotoUpload.setI18n(i18n);
        coverPhotoUpload.setI18n(i18n);
    }

    private String formatValue(long value) {
        if (value >= 1_000_000) {
            return formatMillions(value);
        } else if (value >= 1_000) {
            return formatThousands(value);
        } else {
            return String.valueOf(value);
        }
    }

    private String formatMillions(long value) {
        String wrapped = String.valueOf(value);
        int length = wrapped.length();
        int significantDigits = length - 6; // Determine significant digits for millions

        if (wrapped.length() > significantDigits + 1 && wrapped.charAt(significantDigits) == '0') {
            return wrapped.substring(0, significantDigits) + "M";
        } else {
            return wrapped.substring(0, significantDigits) + "." + wrapped.charAt(significantDigits) + "M";
        }
    }

    private String formatThousands(long value) {
        String wrapped = String.valueOf(value);
        int length = wrapped.length();
        int significantDigits = length - 3; // Determine significant digits for thousands

        if (wrapped.length() > significantDigits + 1 && wrapped.charAt(significantDigits) == '0') {
            return wrapped.substring(0, significantDigits) + "K";
        } else {
            return wrapped.substring(0, significantDigits) + "." + wrapped.charAt(significantDigits) + "K";
        }
    }

    private VerticalLayout createFollowerHeader(User user){
    	List<Follower> followers = followerService.getFollowersByFollowedUserId(user.getId());

    	Span followerSpan = new Span("Followers");
        followerSpan.addClassName("follower-span");

        Span moreFollowers = new Span("View all");
        moreFollowers.addClassName("view-more");
        moreFollowers.addClickListener(event -> {
             UI.getCurrent().navigate(ViewAllFollowers.class, user.getId());
        });

	DecimalFormat formatter = new DecimalFormat("#,###");

	String formatted = formatter.format(followers.size());

        Span totalFollowers = new Span(formatted);
	totalFollowers.addClassName("header-total-followers");

	HorizontalLayout layout = new HorizontalLayout(followerSpan, moreFollowers);
	layout.addClassName("header-followers-layout");

        return new VerticalLayout(layout, totalFollowers);
    }

    private VerticalLayout createInformationLayout(User user){
    	Icon age = new Icon(VaadinIcon.USER_CLOCK);
    	age.addClassName("profile-information");

	Span ageSpan = new Span(String.valueOf(user.getAge()));
	ageSpan.addClassName("profile-span");

    	Icon gender = new Icon(VaadinIcon.MALE);
    	gender.addClassName("profile-information");

	Span genderSpan = new Span(user.getGender());
	genderSpan.addClassName("profile-span");

    	Icon dateOfBirth = new Icon(VaadinIcon.CALENDAR_CLOCK);
    	dateOfBirth.addClassName("profile-information");

	Span dateSpan = new Span(user.getDateOfBirth().toString());
	dateSpan.addClassName("profile-span");

    	Icon placeOfBirth = new Icon(VaadinIcon.HOME);
    	placeOfBirth.addClassName("profile-information");

	Span placeSpan = new Span(user.getPlaceOfBirth());
	placeSpan.addClassName("profile-span");

	HorizontalLayout followingDiv = createFollowingDiv(user);
	followingDiv.addClassName("profile-following-div");

	HorizontalLayout moreLayout = createMoreInformation(user);
	moreLayout.addClassName("profile-more-layout");

    	return new VerticalLayout(
    	    followingDiv,
   	    new HorizontalLayout(age, new Span("At age of "), ageSpan),
    	    new HorizontalLayout(gender, new Span("Gender "), genderSpan),
    	    new HorizontalLayout(dateOfBirth, new Span("Born in "), dateSpan),
    	    new HorizontalLayout(placeOfBirth, new Span("Lives in "), placeSpan),
    	    moreLayout
    	);
    }

    private HorizontalLayout createFollowingDiv(User user){
    	List<Follower> following = followerService.getFollowedUsersByFollowerId(user.getId());

    	Icon followingIcon = new Icon(VaadinIcon.USER_CHECK);

    	Span followingText = new Span("Following");
    	followingText.addClassName("profile-following-text");

    	Span numbers = new Span(String.valueOf(following.size()));
    	numbers.addClassName("profile-following-numbers");

    	return new HorizontalLayout(followingIcon, followingText, numbers);
    }

    private HorizontalLayout createMoreInformation(User user){
    	Contact contact = contactService.getContactByUserId(user.getId());

	if(contact != null){
    	   Icon more = new Icon(VaadinIcon.ELLIPSIS_CIRCLE_O);
           more.addClassName("profile-information");
           Span moreSpan = new Span("View more about " + user.getFirstName());

           ConfirmDialog dialog = new ConfirmDialog();
           dialog.setCancelable(false);
           dialog.setConfirmText("Close");
           dialog.setHeader("More information");
	   dialog.addClassName("profile-more-dialog");

	   Span contactText = new Span("Contact Information");
	   contactText.addClassName("profile-contact-text");

	   Span moreText = new Span("More information");
	   moreText.addClassName("profile-contact-text");

	   Image instagramIcon = new Image("./icons/instagram.svg", "Instagram Icon");
           Image tiktokIcon = new Image("./icons/tiktok.svg", "Tiktok Icon");
           Image phoneIcon = new Image("./icons/phone.svg", "Phone Icon");
           Image facebookIcon = new Image("./icons/facebook.svg", "Facebook Icon");

	   Span phoneNumber = new Span(contact.getPhoneNumber());
	   phoneNumber.addClassName("profile-contact");

	   Span facebook = new Span(contact.getFacebook());
	   facebook.addClassName("profile-contact");

	   Span instagram = new Span(contact.getInstagram());
	   instagram.addClassName("profile-contact");

	   Span tiktok = new Span(contact.getTiktok());
	   tiktok.addClassName("profile-contact");

	   StudentInfo studentInfo = user.getStudentInfo();

           Span penName = new Span(studentInfo.getPenName());
           penName.addClassName("profile-contact");

           Span hobby = new Span(studentInfo.getHobby());
           hobby.addClassName("profile-contact");

           Icon penIcon = new Icon(VaadinIcon.PENCIL);
           penIcon.getStyle().set("color", "#0ef");

           Icon hobbyIcon = new Icon(VaadinIcon.GAMEPAD);
           hobbyIcon.getStyle().set("color", "#0ef");
	   dialog.add(
	       new VerticalLayout(
		   contactText,
		   new HorizontalLayout(phoneIcon, phoneNumber),
		   new HorizontalLayout(facebookIcon, facebook),
		   new HorizontalLayout(instagramIcon, instagram),
		   new HorizontalLayout(tiktokIcon, tiktok),
		   moreText,
		   new HorizontalLayout(penIcon, penName),
		   new HorizontalLayout(hobbyIcon, hobby)
	       )
	   );

           moreSpan.addClickListener(event -> dialog.open());

           more.addClickListener(event -> dialog.open());

           return new HorizontalLayout(more, moreSpan);
        }

        return new HorizontalLayout();
    }

    private void handlePhotoUpload() {
      	handleFileUpload();

    	// Memory buffer for cover photo
    	MemoryBuffer coverPhotoBuffer = new MemoryBuffer();
    	coverPhotoUpload.setReceiver(coverPhotoBuffer);
    	coverPhotoUpload.addClassName("profile-cover-photo-upload");
    	coverPhotoUpload.setUploadButton(new Button(new Icon(VaadinIcon.CAMERA)));
    	coverPhotoUpload.setAcceptedFileTypes("image/png", "image/jpeg");
    	coverPhotoUpload.setMaxFiles(1);
    	coverPhotoUpload.addSucceededListener(ev -> {
            try {
                InputStream inputStream = coverPhotoBuffer.getInputStream();

                if (inputStream.available() > 0) {
                   // Convert InputStream to ByteArrayOutputStream
                   ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                   inputStream.transferTo(byteArrayOutputStream); // Copy data from InputStream to ByteArrayOutputStream

                   // Store ByteArrayOutputStream in session
                   VaadinSession.getCurrent().getSession().setAttribute("uploadedCoverPhoto", byteArrayOutputStream);
                   UI.getCurrent().navigate(ImageCropView.class, "CoverPhoto");
            	} else {
                   Notification.show("Cover photo is empty.");
                }
            } catch (Exception e) {
            	Notification.show(e.getMessage());
            }
    	});

    	// Memory buffer for profile photo
    	MemoryBuffer profilePhotoBuffer = new MemoryBuffer();
    	profilePhotoUpload.setReceiver(profilePhotoBuffer);
    	profilePhotoUpload.addClassName("profile-edit-icon");
    	profilePhotoUpload.setUploadButton(new Button(new Icon(VaadinIcon.CAMERA)));
    	profilePhotoUpload.setAcceptedFileTypes("image/png", "image/jpeg");
    	profilePhotoUpload.setMaxFiles(1);
    	profilePhotoUpload.addSucceededListener(ev -> {
            try {
                InputStream inputStream = profilePhotoBuffer.getInputStream();

                if (inputStream.available() > 0) {
                   // Convert InputStream to ByteArrayOutputStream
                   ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                   inputStream.transferTo(byteArrayOutputStream); // Copy data from InputStream to ByteArrayOutputStream

                   // Store ByteArrayOutputStream in session
                   VaadinSession.getCurrent().getSession().setAttribute("uploadedProfilePhoto", byteArrayOutputStream);
                   UI.getCurrent().navigate(ImageCropView.class, "ProfilePhoto");
            	} else {
                   Notification.show("Profile photo is empty.");
            	}
            } catch (Exception e) {
            	Notification.show(e.getMessage());
            }
    	});
    }

    private void displayPhoto(Avatar avatar, Image image, String displayType, User user){
	if(displayType.equalsIgnoreCase("Profile")){
    	    String imageUrl = user.getProfileImage();
            avatar.setImage(imageUrl);
    	}

    	if(!displayType.equalsIgnoreCase("Cover")){
    	    String imageUrl = user.getCoverPhoto();

    	    if(imageUrl != null && !imageUrl.isEmpty()){
    	       image.setSrc(imageUrl);
    	    }else{
    	       StreamResource resource = new StreamResource("coverphoto-placeholder.jpeg",
                    () -> getClass().getResourceAsStream("/META-INF/resources/images/coverphoto-placeholder.jpeg"));
               image.setSrc(resource);
    	    }
    	}
    }

    private void createFollowerLayout(User user) {
    	FormLayout formLayout = new FormLayout();

    	List<Follower> followers = followerService.getFollowersByFollowedUserId(user.getId());
    	int maxFollowersToShow = 3;

    	HorizontalLayout layout = new HorizontalLayout();

    	User currentUser = userService.findCurrentUser();

    	for (int i = 0; i < Math.min(followers.size(), maxFollowersToShow); i++) {
            User followerUser = followers.get(i).getFollower();
            Image image = new Image(followerUser.getProfileImage(), "Image" + (i + 1));
            image.addClassName("follower-image" + (i + 1));

            Span span = new Span(followerUser.getFirstName());

            if (followers.size() < 3) {
            	VerticalLayout verticalLayout = new VerticalLayout(image, span);
            	verticalLayout.addClassName("divdiv" + (i + 1));

            	verticalLayout.addClickListener(event -> {
            	    if(currentUser.getId().equals(followerUser.getId())){
                    	UI.getCurrent().navigate(MainFeed.class);
                    }else{
                    	refreshHeader();
                    	UI.getCurrent().navigate(UserProfile.class, followerUser.getId());
                    }
            	});

            	layout.add(verticalLayout);
            } else {
            	Div div = new Div(image, span);
            	div.addClassName("div" + (i + 1));

            	div.addClickListener(event -> {
                    if(currentUser.getId().equals(followerUser.getId())){
                        UI.getCurrent().navigate(MainFeed.class);
                    }else{
                    	refreshHeader();
                        UI.getCurrent().navigate(UserProfile.class, followerUser.getId());
                    }
            	});

            	layout.add(div);
            }
    	}

        Avatar avatar = new Avatar();
        avatar.addClassName("own-profile-avatar");

        Image image = new Image();
        image.addClassName("profile-cover-photo-image");

        displayPhoto(avatar, image, "Profile", user);

        handlePhotoUpload();

        String nickname = user.getNickname();
	Span nicknameSpan = new Span();
        if(nickname != null && !nickname.isEmpty()){
           nicknameSpan.setText(" (" + nickname + ")");
        }else{
           nicknameSpan.setVisible(false);
        }

        Span nameSpan = new Span(user.getFullName());
        nameSpan.addClassName("own-profile-name");
        nameSpan.add(nicknameSpan);

	String retrievedData = user.getBio();

	Span bioText = new Span();
        bioText.addClassName("own-profile-bio-text");

        if(retrievedData != null && !retrievedData.isEmpty()){
           bioText.getElement().setProperty("innerHTML", retrievedData);
        }else{
           bioText.setVisible(false);
        }

        Span totalFollowers = new Span(formatValue(followers.size() + 999965));
        totalFollowers.addClassName("own-profile-total-followers");

        Span additional = new Span(" followers");
        additional.addClassName("own-profile-total-followers");

        Div totalFollowersDiv = new Div(totalFollowers, additional);
        totalFollowersDiv.addClassName("own-profile-total-div");

        Div avatarDiv = new Div(avatar);
	avatarDiv.addClickListener(event -> {
	    VaadinSession.getCurrent().getSession().setAttribute("own-profile", user.getId());
	    UI.getCurrent().navigate(ViewProfilePicture.class, "Profile");
	});

	HorizontalLayout uploadLayout = new HorizontalLayout(profilePhotoUpload, coverPhotoUpload);
	uploadLayout.addClassName("profile-upload-layout2");

        Div uploadDiv = new Div(avatarDiv, uploadLayout);
        uploadDiv.addClassName("profile-upload-div");

        VerticalLayout nameBioFollowerLayout = new VerticalLayout(nameSpan, bioText, totalFollowersDiv);
        nameBioFollowerLayout.addClassName("name-bio-follower-layout");

        VerticalLayout layout2 = new VerticalLayout(uploadDiv, nameBioFollowerLayout);
        layout2.addClassName("profile-layout");

    	HorizontalLayout buttonLayout = createStatistics(user, layout, avatar);
        buttonLayout.addClassName("profile-upload-layout");

        VerticalLayout informationLayout = createInformationLayout(user);
        informationLayout.addClassName("profile-information-layout");

        VerticalLayout followerHeader = createFollowerHeader(user);

	Button imageButton = createImageButton(user);

        VerticalLayout followLayout = new VerticalLayout(followerHeader, layout, imageButton);
	followLayout.addClassName("second-follow-layout");

	ProfileFeed feed = new ProfileFeed(artworkService, likeService, heartService, commentService, userService, postService);

	FormLayout profileImageFeed = feed.createFeed(user);

        formLayout.add(image, layout2, buttonLayout, informationLayout, followLayout, profileImageFeed);

    	setContent(formLayout);
    }

    private Button createImageButton(User user){
    	Button imageButton = new Button("Artworks", new Icon(VaadinIcon.PICTURE));
        imageButton.addClassName("profile-image-button");
        imageButton.addClickListener(event -> {
             UI.getCurrent().navigate(ArtworkImages.class, user.getId());
        });

        return imageButton;
    }

    private void refreshFollowerLayout(User user, HorizontalLayout layout, long followerCount) {
    	layout.removeAll();
    	createFollowerLayout(user);
    }

    private void refreshHeader(){
    	headerLayout.removeAll();
    }

    private void updateFollowButton(Button followButton, boolean isFollowed) {
        if (isFollowed) {
            followButton.setIcon(new Icon(VaadinIcon.USER_CHECK));
            followButton.setText("Unfollow");
            followButton.removeClassName("follow-button");
            followButton.addClassName("unfollow-button");
        } else {
            followButton.setIcon(new Icon(VaadinIcon.PLUS));
            followButton.setText("Follow");
            followButton.removeClassName("unfollow-button");
            followButton.addClassName("follow-button");
        }
    }

    private void createHeader(User user){
    	Icon backButton = new Icon("lumo", "arrow-left");
	backButton.addClassName("header-back-button");
	backButton.addClickListener(event -> {
	     UI.getCurrent().navigate(MainFeed.class);
	});

	Icon searchIcon = new Icon("lumo", "search");
	searchIcon.addClassName("own-profile-search-icon");

	String originalName = user.getFullName();

	if(originalName.length() > 26){
	   originalName = originalName.substring(0, 26) + "…";
	}

	Span fullName = new Span(originalName);
	fullName.addClassName("profile-fullname");
	fullName.addClickListener(event -> {
             UI.getCurrent().navigate(ImageCropView.class);
        });

	headerLayout.add(backButton, fullName, searchIcon);
	headerLayout.addClassName("own-profile-header-layout");

	addToNavbar(headerLayout);
    }
}
