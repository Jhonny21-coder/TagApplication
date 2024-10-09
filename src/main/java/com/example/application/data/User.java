package com.example.application.data;

import com.example.application.data.story.Story;
import com.example.application.data.story.ViewedStory;

import jakarta.persistence.*;
import java.time.LocalDate;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false)
    private int age;

    @Column(nullable = false)
    private String gender;

    @Column(nullable = false)
    private String dateOfBirth;

    @Column(nullable = false)
    private String placeOfBirth;


    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String profileImage;

    @Column(nullable = false)
    private String bio;

    @Column(nullable = false)
    private String coverPhoto;

    @Column(nullable = false)
    private String nickname;

    @Column(nullable = false)
    private String role;

    @Column(nullable = false)
    private String verification;

    @Column(nullable = false)
    private boolean enabled;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private StudentInfo studentInfo;

    public User() {}

    public User(String firstName, String lastName, String email, String profileImage){
    	this.firstName = firstName;
    	this.lastName = lastName;
    	this.email = email;
    	this.profileImage = profileImage;
    }

    /*@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Story> stories;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ViewedStory> viewedStories;*/

    public Long getId() {
    	return id;
    }

    public void setId(Long id) {
    	this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getPlaceOfBirth() {
        return placeOfBirth;
    }

    public void setPlaceOfBirth(String placeOfBirth) {
        this.placeOfBirth = placeOfBirth;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getBio(){
    	return bio;
    }

    public void setBio(String bio){
    	this.bio = bio;
    }

    public String getCoverPhoto(){
        return coverPhoto;
    }

    public void setCoverPhoto(String coverPhoto){
        this.coverPhoto = coverPhoto;
    }

    public String getNickname(){
    	return nickname;
    }

    public void setNickname(String nickname){
    	this.nickname = nickname;
    }

    public StudentInfo getStudentInfo() {
        return studentInfo;
    }

    public void setStudentInfo(StudentInfo studentInfo) {
        this.studentInfo = studentInfo;
    }

    public String getFullName(){
	return firstName + " " + lastName;
    }

    public String getRole(){
    	return role;
    }

    public void setRole(String role){
    	this.role = role;
    }

    public String getVerification(){
    	return verification;
    }

    public void setVerification(String verification){
    	this.verification = verification;
    }

    public boolean getEnabled(){
    	return enabled;
    }

    public void setEnabled(boolean enabled){
    	this.enabled = enabled;
    }

    // For story logic
    /*public List<Story> getStories() {
        return stories;
    }

    public void setStories(List<Story> stories) {
        this.stories = stories;
    }

    public List<ViewedStory> getViewedStories() {
        return viewedStories;
    }

    public void setViewedStories(List<ViewedStory> viewedStories) {
        this.viewedStories = viewedStories;
    }*/
}
