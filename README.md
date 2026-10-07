# Web Crawler MCP Server (STDIO)

An advanced Model Context Protocol (MCP) Server built with **Spring Boot 3.5.5**, **Spring AI**, **JSoup**, and **H2 Database**. It enables AI models to crawl websites recursively, scrape single pages, extract clean text & metadata, persist structured web data into a relational database, and perform search/analytics across crawled domains.

---

## 🚀 Features

- 🕷️ **Full Site Crawling**: Recursively crawls target websites (Breadth-First Search) following internal links up to configurable `maxDepth` and `maxPages` limits.
- 📄 **Single-Page Scraping**: Scrapes individual web pages on demand, extracting title, meta descriptions, cleaned text content, and word counts.
- 💾 **Database Persistence**: Stores all scraped web pages and crawl job histories in an H2 database using Spring Data JPA.
- 🔍 **Keyword Search**: Search full-text content and titles across all scraped web pages stored in the database.
- 🧹 **Domain Management**: Retrieve domain statistics, filter saved pages by domain, or purge all pages associated with a specific domain.
- 🤖 **Spring AI MCP Standard**: Exposes native Spring AI `@Tool` functions via standard input/output (STDIO) JSON-RPC transport for seamless AI assistant integration.

---

## 🏗️ Technology Stack

- **Java Version**: 21
- **Framework**: Spring Boot 3.5.5
- **MCP Framework**: Spring AI `1.0.2` (`spring-ai-starter-mcp-server`)
- **HTML Parser**: JSoup `1.18.1`
- **Database**: H2 (In-Memory / JPA)
- **Transport Protocol**: STDIO (Standard I/O)

---

## 🧰 Available MCP Tools

The server exposes 8 tools for AI assistant invocation:

| Tool Name | Parameters | Description |
| :--- | :--- | :--- |
| `crawl_website` | `url` *(String)*, `maxDepth` *(Int, default: 2)*, `maxPages` *(Int, default: 20)* | Crawls a website starting from `url`, follows internal domain links up to `maxDepth` & `maxPages`, and saves all pages to the database. |
| `scrape_single_page` | `url` *(String)* | Scrapes a single web page, extracts title, meta description, and body text, and saves it into the database. |
| `search_scraped_pages` | `keyword` *(String)* | Performs a keyword search across saved titles and body text in the database. |
| `get_page_by_url` | `url` *(String)* | Retrieves complete stored page details and full text content for a given URL. |
| `get_all_pages_by_domain` | `domain` *(String)* | Retrieves all stored pages belonging to a specific domain (e.g., `spring.io`). |
| `get_all_crawled_domains` | *None* | Returns a summary breakdown of all stored domains and their page counts. |
| `get_crawl_jobs` | *None* | Lists the 10 most recent crawling jobs along with execution status and page counts. |
| `delete_pages_by_domain` | `domain` *(String)* | Purges all stored web pages for a specific domain from the database. |

---

## 🛠️ Building the Project

### Prerequisites
- **JDK 21** or higher installed.

### Build Executable JAR
From the project root directory, run:

```bash
./mvnw clean package -DskipTests
```

The output executable JAR will be located at:
```
target/mcp-web-crawler.jar
```

---

## 🧪 Testing with MCP Inspector

You can inspect and manually test all tools using the official Model Context Protocol Inspector UI:

```bash
npx @modelcontextprotocol/inspector java -jar /Users/rajesh/Desktop/Java/mcp-web-crawler/target/mcp-web-crawler.jar
```

Once launched, open the browser interface URL displayed in your terminal (typically `http://localhost:5173` or similar) to view and test all tools.

---

## ⚙️ Integration Configuration (`mcp.json`)

To register this MCP server with **Claude Desktop**, **Antigravity IDE**, or any MCP-compliant client, add the following entry to your `mcp.json`:

```json
{
  "mcpServers": {
    "mcp-web-crawler": {
      "command": "java",
      "args": [
        "-jar",
        "/Users/rajesh/Desktop/Java/mcp-web-crawler/target/mcp-web-crawler.jar"
      ]
    }
  }
}
```

---

## 📁 Project Structure

```
mcp-web-crawler/
├── src/
│   ├── main/
│   │   ├── java/com/mcp/webcrawler/
│   │   │   ├── entity/           # JPA Entities (WebPage, CrawlJob)
│   │   │   ├── repository/       # Spring Data JPA Repositories
│   │   │   ├── service/          # Web Scraper & Crawler Business Logic
│   │   │   └── tools/            # Spring AI @Tool MCP Function Definitions
│   │   └── resources/
│   │       ├── application.properties
│   │       └── mcp.json          # Example MCP configuration
├── pom.xml                       # Maven dependencies & build configuration
└── README.md
```

---

## 💡 Notes & Troubleshooting

- **STDIO Transport Logging**: Since communication occurs over `System.out` (STDIO transport), Spring Boot console logging is disabled (`logging.level.root=OFF`, `spring.main.banner-mode=off`) to prevent log output from corrupting JSON-RPC protocol frames.
- **Database Scope**: Uses an in-memory H2 database (`jdbc:h2:mem:crawlerdb`) by default.
