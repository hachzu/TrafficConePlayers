package web;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class LocalServer implements AutoCloseable {
    private final HttpServer server;
   
    public LocalServer(int port) throws IOException{
        server = HttpServer.create((new InetSocketAddress("localhost", port)), 0);
        server.createContext("/", new HttpHandler() {
            @Override 
                public void handle(HttpExchange exchange) throws IOException{
                    byte[] page;
                   
                    try (InputStream resource = LocalServer.class.getResourceAsStream("/assets//embed.html")) {
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

          
        }

        });

         server.start();
    }
    


    @Override
    public void close() throws Exception {
        
        System.out.println("Closing server...");
        server.stop(0);
    }
    
    public void getAddress() {
        System.out.println("Server is running at: http://localhost:" + server.getAddress().getPort());
    }
    
    public void getExecutor() {
        System.out.println("Server executor: " + server.getExecutor());
    }
    
    public String getURL() {
        return "http://localhost:" + server.getAddress().getPort() + "/";
    }
}