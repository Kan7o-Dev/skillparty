package com.skillparty;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

final class WebServer {
    private final Gson gson = new Gson();
    private final HttpServer server;
    private Supplier<Object> stateSupplier = () -> Map.of("ok", true);
    private Consumer<Map<String, Object>> configConsumer = m -> {};

    WebServer(int port) throws IOException {
        server = HttpServer.create(new InetSocketAddress(InetAddress.getByName("127.0.0.1"), port), 0);

        // GET /state
        server.createContext("/state", ex -> {
            if (!"GET".equals(ex.getRequestMethod())) { send(ex, 405, ""); return; }
            String json = gson.toJson(stateSupplier.get());
            sendJson(ex, 200, json);
        });

        // POST /config
        server.createContext("/config", ex -> {
            if (!"POST".equals(ex.getRequestMethod())) { send(ex, 405, ""); return; }
            String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            @SuppressWarnings("unchecked")
            Map<String, Object> map = gson.fromJson(body, Map.class);
            if (map != null) configConsumer.accept(map);
            sendJson(ex, 200, "{\"ok\":true}");
        });

        // GET /static/*
        server.createContext("/static", ex -> {
            String path = ex.getRequestURI().getPath().replaceFirst("^/static/?", "");
            if (path.isBlank()) { send(ex, 404, ""); return; }
            try (InputStream in = resource("skillparty/" + path)) {
                if (in == null) { send(ex, 404, ""); return; }
                if (path.endsWith(".png")) ex.getResponseHeaders().add("Content-Type", "image/png");
                else ex.getResponseHeaders().add("Content-Type", "application/octet-stream");
                ex.sendResponseHeaders(200, 0);
                in.transferTo(ex.getResponseBody());
            } finally { ex.close(); }
        });

        // GET /
        server.createContext("/", ex -> {
            try (InputStream in = resource("skillparty/skillparty.html")) {
                if (in == null) { send(ex, 404, "overlay missing"); return; }
                ex.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
                ex.sendResponseHeaders(200, 0);
                in.transferTo(ex.getResponseBody());
            } finally { ex.close(); }
        });

        server.setExecutor(null);
    }

    void setStateSupplier(Supplier<Object> s) { stateSupplier = s; }
    void setConfigConsumer(Consumer<Map<String, Object>> c) { configConsumer = c; }

    void start() { server.start(); }
    void stop()  { server.stop(0); }

    private static InputStream resource(String path) {
        return WebServer.class.getClassLoader().getResourceAsStream(path);
    }

    private static void send(HttpExchange ex, int code, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        ex.sendResponseHeaders(code, bytes.length);
        try (var out = ex.getResponseBody()) { out.write(bytes); }
        ex.close();
    }

    private static void sendJson(HttpExchange ex, int code, String json) throws IOException {
        ex.getResponseHeaders().add("Content-Type", "application/json");
        send(ex, code, json);
    }
}
