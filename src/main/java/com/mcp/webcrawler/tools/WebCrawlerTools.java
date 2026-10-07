package com.mcp.webcrawler.tools;

import com.mcp.webcrawler.entity.CrawlJob;
import com.mcp.webcrawler.entity.WebPage;
import com.mcp.webcrawler.repository.CrawlJobRepository;
import com.mcp.webcrawler.repository.WebPageRepository;
import com.mcp.webcrawler.service.WebScraperService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class WebCrawlerTools {

    private static final Logger log = LoggerFactory.getLogger(WebCrawlerTools.class);

    private final WebScraperService webScraperService;
    private final WebPageRepository webPageRepository;
    private final CrawlJobRepository crawlJobRepository;

    public WebCrawlerTools(WebScraperService webScraperService,
                           WebPageRepository webPageRepository,
                           CrawlJobRepository crawlJobRepository) {
        this.webScraperService = webScraperService;
        this.webPageRepository = webPageRepository;
        this.crawlJobRepository = crawlJobRepository;
    }

    @Tool(name = "crawl_website", description = "Crawl a website starting from a URL, follow internal links up to maxDepth (default 2) and maxPages limit (default 20), and save all scraped web pages into the database.")
    public CrawlJob crawlWebsite(String url, Integer maxDepth, Integer maxPages) {
        int depth = (maxDepth == null || maxDepth < 0) ? 2 : maxDepth;
        int pages = (maxPages == null || maxPages <= 0) ? 20 : Math.min(maxPages, 100);
        log.info("MCP Tool: Crawling website {} with depth={}, maxPages={}", url, depth, pages);
        return webScraperService.crawlWebsite(url, depth, pages);
    }

    @Tool(name = "scrape_single_page", description = "Scrape a single webpage by URL, extract its title, meta description, readable text content, and store it in the database.")
    public WebPage scrapeSinglePage(String url) {
        log.info("MCP Tool: Scraping single page {}", url);
        try {
            return webScraperService.scrapeSinglePage(url);
        } catch (Exception e) {
            log.error("Error scraping page {}", url, e);
            throw new RuntimeException("Failed to scrape page: " + e.getMessage(), e);
        }
    }

    @Tool(name = "search_scraped_pages", description = "Search all saved webpages in the database by keyword in title or main text content.")
    public List<WebPage> searchScrapedPages(String keyword) {
        log.info("MCP Tool: Searching pages for keyword: {}", keyword);
        return webPageRepository.searchByKeyword(keyword);
    }

    @Tool(name = "get_page_by_url", description = "Retrieve full scraped webpage details and text content from the database for a specific URL.")
    public WebPage getPageByUrl(String url) {
        log.info("MCP Tool: Fetching page by URL: {}", url);
        return webPageRepository.findByUrl(url).orElse(null);
    }

    @Tool(name = "get_all_pages_by_domain", description = "Get all scraped webpages stored in the database for a specific domain (e.g. spring.io, github.com).")
    public List<WebPage> getAllPagesByDomain(String domain) {
        log.info("MCP Tool: Fetching pages for domain: {}", domain);
        String cleanDomain = domain.toLowerCase().replace("https://", "").replace("http://", "").replace("www.", "");
        return webPageRepository.findByDomain(cleanDomain);
    }

    @Tool(name = "get_all_crawled_domains", description = "Get a summary map of all domains currently stored in the database along with total page count for each domain.")
    public Map<String, Long> getAllCrawledDomains() {
        log.info("MCP Tool: Listing all crawled domains summary");
        List<WebPage> allPages = webPageRepository.findAll();
        return allPages.stream()
                .collect(Collectors.groupingBy(WebPage::getDomain, Collectors.counting()));
    }

    @Tool(name = "get_crawl_jobs", description = "List the recent website crawling jobs and their completion status.")
    public List<CrawlJob> getCrawlJobs() {
        log.info("MCP Tool: Listing crawl jobs history");
        return crawlJobRepository.findTop10ByOrderByIdDesc();
    }

    @Tool(name = "delete_pages_by_domain", description = "Delete all stored pages for a specific domain from the database.")
    public String deletePagesByDomain(String domain) {
        log.info("MCP Tool: Deleting pages for domain: {}", domain);
        String cleanDomain = domain.toLowerCase().replace("https://", "").replace("http://", "").replace("www.", "");
        long countBefore = webPageRepository.countByDomain(cleanDomain);
        webPageRepository.deleteByDomain(cleanDomain);
        return "Deleted " + countBefore + " stored page(s) for domain: " + cleanDomain;
    }
}
