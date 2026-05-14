package services;

import models.Candidate;
import models.Vote;
import models.Voter;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileService {
    private static final String VOTERS_FILE = "voters.csv";
    private static final String CANDIDATES_FILE = "candidates.csv";
    private static final String VOTES_FILE = "votes.csv";

    public FileService() {
        // Create files if they don't exist
        try {
            new File(VOTERS_FILE).createNewFile();
            new File(CANDIDATES_FILE).createNewFile();
            new File(VOTES_FILE).createNewFile();
        } catch (IOException e) {
            System.err.println("Error initializing data files: " + e.getMessage());
        }
    }

    public List<Voter> loadVoters() {
        List<Voter> voters = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(VOTERS_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                Voter v = Voter.fromCsv(line);
                if (v != null) voters.add(v);
            }
        } catch (IOException e) {
            System.err.println("Error reading voters: " + e.getMessage());
        }
        return voters;
    }

    public void saveVoters(List<Voter> voters) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(VOTERS_FILE))) {
            for (Voter v : voters) {
                bw.write(v.toCsv());
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving voters: " + e.getMessage());
        }
    }

    public List<Candidate> loadCandidates() {
        List<Candidate> candidates = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(CANDIDATES_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                Candidate c = Candidate.fromCsv(line);
                if (c != null) candidates.add(c);
            }
        } catch (IOException e) {
            System.err.println("Error reading candidates: " + e.getMessage());
        }
        return candidates;
    }

    public void saveCandidates(List<Candidate> candidates) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(CANDIDATES_FILE))) {
            for (Candidate c : candidates) {
                bw.write(c.toCsv());
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving candidates: " + e.getMessage());
        }
    }

    public List<Vote> loadVotes() {
        List<Vote> votes = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(VOTES_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                Vote v = Vote.fromCsv(line);
                if (v != null) votes.add(v);
            }
        } catch (IOException e) {
            System.err.println("Error reading votes: " + e.getMessage());
        }
        return votes;
    }

    public void saveVotes(List<Vote> votes) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(VOTES_FILE))) {
            for (Vote v : votes) {
                bw.write(v.toCsv());
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving votes: " + e.getMessage());
        }
    }

    public void clearElectionData() {
        try {
            new PrintWriter(VOTES_FILE).close();
            // Also update all voters to have not voted
            List<Voter> voters = loadVoters();
            for (Voter v : voters) {
                v.setHasVoted(false);
            }
            saveVoters(voters);
            System.out.println("Election data cleared successfully.");
        } catch (IOException e) {
            System.err.println("Error clearing election data: " + e.getMessage());
        }
    }
}
