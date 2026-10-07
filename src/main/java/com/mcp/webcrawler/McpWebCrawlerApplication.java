package com.mcp.webcrawler;

import com.mcp.webcrawler.tools.WebCrawlerTools;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;

@SpringBootApplication
public class McpWebCrawlerApplication {

	public static void main(String[] args) {
		SpringApplication.run(McpWebCrawlerApplication.class, args);
	}

	@Bean
	public List<ToolCallback> toolCallbacks(WebCrawlerTools webCrawlerTools) {
		return List.of(ToolCallbacks.from(webCrawlerTools));
	}

}
