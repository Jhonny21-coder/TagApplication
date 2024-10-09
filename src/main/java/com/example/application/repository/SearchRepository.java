package com.example.application.repository;

import com.example.application.data.Search;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface SearchRepository extends JpaRepository<Search, Long> {

    List<Search> findBySearcherId(Long searcherId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Search s WHERE s.searcher.id = :searcherId")
    void deleteBySearcherId(@Param("searcherId") Long searcherId);
}
