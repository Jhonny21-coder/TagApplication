package com.example.application.services;

import com.example.application.data.Status;
import com.example.application.data.User;
import com.example.application.repository.StatusRepository;
import org.springframework.stereotype.Service;
import com.vaadin.flow.server.VaadinSession;

@Service
public class StatusService {

    private final StatusRepository statusRepository;

    public StatusService(StatusRepository statusRepository) {
        this.statusRepository = statusRepository;
    }

    public Status getStatusByUserId(Long userId){
    	return statusRepository.findByUserId(userId);
    }

    public void updateStatus(String email){
    	Status status = statusRepository.findByUserEmail(email);

    	if(status.getStatus().equalsIgnoreCase("ONLINE")){
    	   status.setStatus("OFFLINE");
    	   statusRepository.save(status);

    	}else if(status.getStatus().equalsIgnoreCase("OFFLINE")){
    	   status.setStatus("ONLINE");
    	   statusRepository.save(status);
    	}

    	Status updatedStatus = statusRepository.findByUserEmail(email);
    }

    public Status getStatusByUserEmail(String userEmail){
    	return statusRepository.findByUserEmail(userEmail);
    }
}
