package com.mcp.webcrawler.repository;

import com.mcp.webcrawler.entity.CrawlJob;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CrawlJobRepository extends JpaRepository<CrawlJob, Long> {

    List<CrawlJob> findTop10ByOrderByIdDesc();
}
