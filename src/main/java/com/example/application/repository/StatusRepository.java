package com.example.application.repository;

import com.example.application.data.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface StatusRepository extends JpaRepository<Status, Long> {

    Status findByUserId(Long userId);

    Status findByUserEmail(String userEmail);
}
