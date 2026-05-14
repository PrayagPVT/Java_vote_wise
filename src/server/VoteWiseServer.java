package server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import admin.AdminPanel;
import models.Candidate;
import models.Constituency;
import models.Voter;
import services.AuthService;
import services.FileService;
import services.ResultService;
import services.VotingService;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class VoteWiseServer {
    private HttpServer server;

    public void startServer() throws IOException {
        FileService fileService = new FileService();
        AuthService authService = new AuthService(fileService);
        VotingService votingService = new VotingService(fileService);
        ResultService resultService = new ResultService(fileService);
        AdminPanel adminPanel = AdminPanel.getInstance(fileService);

        server = HttpServer.create(new InetSocketAddress(8080), 0);

        // Serve Static Files
        server.createContext("/", new StaticFileHandler());

        // Auth
        server.createContext("/api/login", exchange -> {
            if ("POST".equals(exchange.getRequestMethod())) {
                Map<String, String> params = parseFormData(exchange);
                try {
                    Voter voter = authService.loginVoter(params.get("id"), params.get("password"));
                    String json = String.format("{\"error\":false, \"voter\": {\"id\":\"%s\", \"name\":\"%s\", \"constituency\":\"%s\", \"hasVoted\":%b}}",
                            voter.getVoterId(), voter.getName(), voter.getConstituency().name(), voter.hasVoted());
                    sendResponse(exchange, 200, json);
                } catch (Exception e) {
                    sendResponse(exchange, 400, "{\"error\":true, \"message\":\"" + e.getMessage() + "\"}");
                }
            }
        });

        server.createContext("/api/register", exchange -> {
            if ("POST".equals(exchange.getRequestMethod())) {
                Map<String, String> params = parseFormData(exchange);
                try {
                    authService.registerVoter(params.get("id"), params.get("name"), Integer.parseInt(params.get("age")), Constituency.fromString(params.get("constituency")), params.get("password"));
                    sendResponse(exchange, 200, "{\"error\":false, \"message\":\"Registration Successful!\"}");
                } catch (Exception e) {
                    sendResponse(exchange, 400, "{\"error\":true, \"message\":\"Registration Failed.\"}");
                }
            }
        });

        // Voting
        server.createContext("/api/candidates", exchange -> {
            String query = exchange.getRequestURI().getQuery();
            if (query != null && query.contains("constituency=")) {
                Constituency constituency = Constituency.fromString(query.split("=")[1]);
                List<Candidate> candidates = votingService.getCandidatesForConstituency(constituency);
                String candidatesJson = candidates.stream().map(cand -> String.format("{\"id\":\"%s\", \"name\":\"%s\", \"party\":\"%s\"}", cand.getCandidateId(), cand.getName(), cand.getParty())).collect(Collectors.joining(","));
                sendResponse(exchange, 200, "{\"candidates\": [" + candidatesJson + "]}");
            }
        });

        server.createContext("/api/vote", exchange -> {
            if ("POST".equals(exchange.getRequestMethod())) {
                Map<String, String> params = parseFormData(exchange);
                try {
                    Voter voter = fileService.loadVoters().stream().filter(v -> v.getVoterId().equals(params.get("voterId"))).findFirst().get();
                    Candidate candidate = fileService.loadCandidates().stream().filter(c -> c.getCandidateId().equals(params.get("candidateId"))).findFirst().get();
                    votingService.castVote(voter, candidate);
                    sendResponse(exchange, 200, "{\"error\":false, \"message\":\"Vote cast successfully!\"}");
                } catch (Exception e) {
                    sendResponse(exchange, 400, "{\"error\":true}");
                }
            }
        });

        // Admin
        server.createContext("/api/admin/login", exchange -> {
            if ("POST".equals(exchange.getRequestMethod())) {
                Map<String, String> params = parseFormData(exchange);
                if (adminPanel.login(params.get("username"), params.get("password"))) sendResponse(exchange, 200, "{\"error\":false}");
                else sendResponse(exchange, 401, "{\"error\":true, \"message\":\"Invalid credentials\"}");
            }
        });

        server.createContext("/api/admin/status", exchange -> {
            sendResponse(exchange, 200, "{\"status\":\"" + (adminPanel.isElectionOpen() ? "OPEN" : "CLOSED") + "\"}");
        });

        server.createContext("/api/admin/toggle-status", exchange -> {
            adminPanel.toggleElectionStatus();
            sendResponse(exchange, 200, "{\"error\":false}");
        });
        
        server.createContext("/api/admin/add-candidate", exchange -> {
            if ("POST".equals(exchange.getRequestMethod())) {
                Map<String, String> params = parseFormData(exchange);
                try {
                    Candidate newCand = new Candidate(params.get("id"), params.get("name"), params.get("party"), Constituency.fromString(params.get("constituency")), params.get("manifesto"));
                    List<Candidate> candidates = fileService.loadCandidates();
                    candidates.add(newCand);
                    fileService.saveCandidates(candidates);
                    sendResponse(exchange, 200, "{\"error\":false, \"message\":\"Candidate added successfully!\"}");
                } catch(Exception e) {
                    sendResponse(exchange, 400, "{\"error\":true, \"message\":\"Failed to add candidate.\"}");
                }
            }
        });
        
        server.createContext("/api/admin/results", exchange -> {
            try {
                // Since resultService writes to file, we can trigger it and read the file
                resultService.generateResults();
                String report = new String(Files.readAllBytes(new File("election_report.txt").toPath()));
                report = report.replace("\n", "\\n").replace("\r", "");
                sendResponse(exchange, 200, "{\"report\":\"" + report + "\"}");
            } catch (Exception e) {
                sendResponse(exchange, 500, "{\"message\":\"Error generating results.\"}");
            }
        });

        // AI

        server.createContext("/api/ai/sentiment", exchange -> {
            if ("POST".equals(exchange.getRequestMethod())) {
                Map<String, String> params = parseFormData(exchange);
                String text = params.get("text").toLowerCase();
                int score = 0;
                if (text.contains("good") || text.contains("great") || text.contains("easy") || text.contains("fast")) score++;
                if (text.contains("bad") || text.contains("hard") || text.contains("slow") || text.contains("terrible")) score--;
                sendResponse(exchange, 200, "{\"sentiment\":\"" + (score > 0 ? "SATISFIED" : (score < 0 ? "DISSATISFIED" : "NEUTRAL")) + "\"}");
            }
        });
        

        server.setExecutor(null);
        server.start();
        System.out.println("VoteWise Web Server started on http://localhost:8080");
    }

    private Map<String, String> parseFormData(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String, String> params = new HashMap<>();
        for (String param : body.split("&")) {
            String[] pair = param.split("=");
            if (pair.length > 1) {
                params.put(URLDecoder.decode(pair[0], StandardCharsets.UTF_8), URLDecoder.decode(pair[1], StandardCharsets.UTF_8));
            }
        }
        return params;
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, response.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/")) path = "/index.html";
            File file = new File("web" + path);
            if (file.exists() && !file.isDirectory()) {
                if (path.endsWith(".css")) exchange.getResponseHeaders().set("Content-Type", "text/css");
                else if (path.endsWith(".js")) exchange.getResponseHeaders().set("Content-Type", "application/javascript");
                else exchange.getResponseHeaders().set("Content-Type", "text/html");
                byte[] bytes = Files.readAllBytes(file.toPath());
                exchange.sendResponseHeaders(200, bytes.length);
                OutputStream os = exchange.getResponseBody();
                os.write(bytes);
                os.close();
            } else {
                exchange.sendResponseHeaders(404, -1);
            }
        }
    }
}
