package com.mcp.webcrawler.repository;

import com.mcp.webcrawler.entity.WebPage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface WebPageRepository extends JpaRepository<WebPage, Long> {

    Optional<WebPage> findByUrl(String url);

    List<WebPage> findByDomain(String domain);

    @Query("SELECT w FROM WebPage w WHERE LOWER(w.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(w.content) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<WebPage> searchByKeyword(@Param("keyword") String keyword);

    long countByDomain(String domain);

    @Transactional
    void deleteByDomain(String domain);
}
