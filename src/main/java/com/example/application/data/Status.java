package com.example.application.data;

import jakarta.persistence.*;
import java.time.LocalDate;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@Entity
public class Status {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String status;

    public Long getId(){
    	return id;
    }

    public User getUser(){
    	return user;
    }

    public void setUser(User user){
    	this.user = user;
    }

    public String getStatus(){
    	return status;
    }

    public void setStatus(String status){
    	this.status = status;
    }
}
