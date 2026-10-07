package com.mcp.webcrawler.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "web_pages")
public class WebPage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 1000)
    private String url;

    @Column(nullable = false)
    private String domain;

    @Column(length = 500)
    private String title;

    @Column(length = 2000)
    private String metaDescription;

    @Column(columnDefinition = "TEXT")
    private String content;

    private int statusCode;

    private int depth;

    private int wordCount;

    private LocalDateTime crawledAt;

    public WebPage() {
    }

    public WebPage(String url, String domain, String title, String metaDescription, String content, int statusCode, int depth, int wordCount, LocalDateTime crawledAt) {
        this.url = url;
        this.domain = domain;
        this.title = title;
        this.metaDescription = metaDescription;
        this.content = content;
        this.statusCode = statusCode;
        this.depth = depth;
        this.wordCount = wordCount;
        this.crawledAt = crawledAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMetaDescription() {
        return metaDescription;
    }

    public void setMetaDescription(String metaDescription) {
        this.metaDescription = metaDescription;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public int getWordCount() {
        return wordCount;
    }

    public void setWordCount(int wordCount) {
        this.wordCount = wordCount;
    }

    public LocalDateTime getCrawledAt() {
        return crawledAt;
    }

    public void setCrawledAt(LocalDateTime crawledAt) {
        this.crawledAt = crawledAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WebPage webPage = (WebPage) o;
        return Objects.equals(url, webPage.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url);
    }

    @Override
    public String toString() {
        return "WebPage{" +
                "id=" + id +
                ", url='" + url + '\'' +
                ", domain='" + domain + '\'' +
                ", title='" + title + '\'' +
                ", statusCode=" + statusCode +
                ", depth=" + depth +
                ", wordCount=" + wordCount +
                ", crawledAt=" + crawledAt +
                '}';
    }
}
