package com.mcp.webcrawler.service;

import com.mcp.webcrawler.entity.CrawlJob;
import com.mcp.webcrawler.entity.WebPage;
import com.mcp.webcrawler.repository.CrawlJobRepository;
import com.mcp.webcrawler.repository.WebPageRepository;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class WebScraperService {

    private static final Logger log = LoggerFactory.getLogger(WebScraperService.class);
    private static final String USER_AGENT = "Mozilla/5.0 (compatible; MCPWebCrawler/1.0; +https://modelcontextprotocol.io)";

    private final WebPageRepository webPageRepository;
    private final CrawlJobRepository crawlJobRepository;

    public WebScraperService(WebPageRepository webPageRepository, CrawlJobRepository crawlJobRepository) {
        this.webPageRepository = webPageRepository;
        this.crawlJobRepository = crawlJobRepository;
    }

    public WebPage scrapeSinglePage(String url) throws Exception {
        log.info("Scraping single URL: {}", url);
        String normalizedUrl = normalizeUrl(url);
        String domain = extractDomain(normalizedUrl);

        Connection.Response response = Jsoup.connect(normalizedUrl)
                .userAgent(USER_AGENT)
                .timeout(10000)
                .followRedirects(true)
                .execute();

        Document doc = response.parse();
        doc.select("script, style, noscript, iframe, svg").remove();

        String title = doc.title();
        String metaDescription = extractMetaDescription(doc);
        String bodyText = doc.body() != null ? doc.body().text() : "";
        int wordCount = bodyText.isBlank() ? 0 : bodyText.split("\\s+").length;

        WebPage page = webPageRepository.findByUrl(normalizedUrl).orElse(new WebPage());
        page.setUrl(normalizedUrl);
        page.setDomain(domain);
        page.setTitle(title.isBlank() ? normalizedUrl : title);
        page.setMetaDescription(metaDescription);
        page.setContent(bodyText);
        page.setStatusCode(response.statusCode());
        page.setDepth(0);
        page.setWordCount(wordCount);
        page.setCrawledAt(LocalDateTime.now());

        return webPageRepository.save(page);
    }

    public CrawlJob crawlWebsite(String startUrl, int maxDepth, int maxPages) {
        CrawlJob job = new CrawlJob(startUrl, maxDepth, maxPages);
        job.setStatus("IN_PROGRESS");
        job = crawlJobRepository.save(job);

        final Long jobId = job.getId();
        final String rootDomain = extractDomain(startUrl);

        try {
            Set<String> visited = new HashSet<>();
            Queue<CrawlNode> queue = new LinkedList<>();

            String initialUrl = normalizeUrl(startUrl);
            queue.add(new CrawlNode(initialUrl, 0));
            visited.add(initialUrl);

            int crawledCount = 0;

            while (!queue.isEmpty() && crawledCount < maxPages) {
                CrawlNode node = queue.poll();
                if (node.depth > maxDepth) {
                    continue;
                }

                try {
                    Connection.Response response = Jsoup.connect(node.url)
                            .userAgent(USER_AGENT)
                            .timeout(8000)
                            .followRedirects(true)
                            .ignoreHttpErrors(true)
                            .execute();

                    if (response.statusCode() == 200 && response.contentType() != null && response.contentType().contains("text/html")) {
                        Document doc = response.parse();
                        doc.select("script, style, noscript, iframe, svg").remove();

                        String title = doc.title();
                        String metaDesc = extractMetaDescription(doc);
                        String bodyText = doc.body() != null ? doc.body().text() : "";
                        int wordCount = bodyText.isBlank() ? 0 : bodyText.split("\\s+").length;

                        WebPage page = webPageRepository.findByUrl(node.url).orElse(new WebPage());
                        page.setUrl(node.url);
                        page.setDomain(extractDomain(node.url));
                        page.setTitle(title.isBlank() ? node.url : title);
                        page.setMetaDescription(metaDesc);
                        page.setContent(bodyText);
                        page.setStatusCode(response.statusCode());
                        page.setDepth(node.depth);
                        page.setWordCount(wordCount);
                        page.setCrawledAt(LocalDateTime.now());

                        webPageRepository.save(page);
                        crawledCount++;

                        if (node.depth < maxDepth && crawledCount < maxPages) {
                            Elements links = doc.select("a[href]");
                            for (Element link : links) {
                                String absUrl = link.attr("abs:href");
                                if (isValidCrawlUrl(absUrl, rootDomain) && !visited.contains(absUrl)) {
                                    visited.add(absUrl);
                                    queue.add(new CrawlNode(absUrl, node.depth + 1));
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    log.warn("Failed to scrape page {}: {}", node.url, e.getMessage());
                }
            }

            job.setPagesCrawled(crawledCount);
            job.setStatus("COMPLETED");
            job.setFinishedAt(LocalDateTime.now());

        } catch (Exception e) {
            log.error("Crawl job failed for URL {}", startUrl, e);
            job.setStatus("FAILED");
            job.setErrorMessage(e.getMessage());
            job.setFinishedAt(LocalDateTime.now());
        }

        return crawlJobRepository.save(job);
    }

    private String extractMetaDescription(Document doc) {
        Element meta = doc.selectFirst("meta[name=description], meta[property=og:description]");
        if (meta != null && meta.hasAttr("content")) {
            String content = meta.attr("content");
            return content.length() > 2000 ? content.substring(0, 2000) : content;
        }
        return "";
    }

    public String extractDomain(String urlStr) {
        try {
            URI uri = new URI(urlStr);
            String domain = uri.getHost();
            if (domain != null) {
                return domain.startsWith("www.") ? domain.substring(4) : domain;
            }
        } catch (Exception ignored) {
        }
        return "unknown";
    }

    private String normalizeUrl(String url) {
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://" + url;
        }
        int hashIdx = url.indexOf('#');
        if (hashIdx != -1) {
            url = url.substring(0, hashIdx);
        }
        return url.endsWith("/") && url.length() > 8 ? url.substring(0, url.length() - 1) : url;
    }

    private boolean isValidCrawlUrl(String url, String rootDomain) {
        if (url == null || url.isBlank()) return false;
        if (!url.startsWith("http://") && !url.startsWith("https://")) return false;

        // Skip binary resources
        String lower = url.toLowerCase();
        if (lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg")
                || lower.endsWith(".gif") || lower.endsWith(".pdf") || lower.endsWith(".zip")
                || lower.endsWith(".css") || lower.endsWith(".js") || lower.endsWith(".svg")) {
            return false;
        }

        String domain = extractDomain(url);
        return domain.equalsIgnoreCase(rootDomain);
    }

    private static class CrawlNode {
        String url;
        int depth;

        CrawlNode(String url, int depth) {
            this.url = url;
            this.depth = depth;
        }
    }
}
