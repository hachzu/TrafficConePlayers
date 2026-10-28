package com.tcp.web;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;

/** Serves the embedded player page from localhost so the WebView has a web origin. */
public class LocalPageServer implements AutoCloseable {
	private final HttpServer server;

	public LocalPageServer() throws IOException {
		server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
		server.createContext("/", exchange -> {
			byte[] page;
			try (InputStream resource = LocalPageServer.class.getResourceAsStream("/assets/web/embed.html")) {
				if (resource == null) {
					exchange.sendResponseHeaders(404, -1);
					exchange.close();
					return;
				}
				page = resource.readAllBytes();
			}
			exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
			exchange.getResponseHeaders().set("Cache-Control", "no-store");
			exchange.sendResponseHeaders(200, page.length);
			try (var response = exchange.getResponseBody()) {
				response.write(page);
			}
		});
		server.start();
	}

	public String getUrl() {
		return "http://localhost:" + server.getAddress().getPort() + "/";
	}

	@Override
	public void close() {
		server.stop(0);
	}
}
