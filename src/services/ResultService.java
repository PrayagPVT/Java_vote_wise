package services;

import models.Candidate;
import models.Constituency;
import models.Vote;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResultService {
    private FileService fileService;

    public ResultService(FileService fileService) {
        this.fileService = fileService;
    }

    public void generateResults() {
        List<Vote> votes = fileService.loadVotes();
        List<Candidate> candidates = fileService.loadCandidates();

        Map<Constituency, Map<String, Integer>> voteCounts = new HashMap<>();
        Map<String, Candidate> candidateMap = new HashMap<>();

        for (Candidate c : candidates) {
            voteCounts.putIfAbsent(c.getConstituency(), new HashMap<>());
            voteCounts.get(c.getConstituency()).put(c.getCandidateId(), 0);
            candidateMap.put(c.getCandidateId(), c);
        }

        int totalVotes = votes.size();

        for (Vote v : votes) {
            String decryptedCandidateId = VotingService.decryptVote(v.getEncryptedVoteData());
            Constituency consti = v.getConstituency();
            
            if (voteCounts.containsKey(consti) && voteCounts.get(consti).containsKey(decryptedCandidateId)) {
                voteCounts.get(consti).put(decryptedCandidateId, voteCounts.get(consti).get(decryptedCandidateId) + 1);
            }
        }

        System.out.println("=== Election Results ===");
        
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("election_report.txt"))) {
            bw.write("=== Election Report ===\n");
            
            for (Constituency consti : Constituency.values()) {
                if (!voteCounts.containsKey(consti) || voteCounts.get(consti).isEmpty()) continue;

                System.out.println("\nConstituency: " + consti.name());
                bw.write("\nConstituency: " + consti.name() + "\n");
                
                int constiTotal = 0;
                String winnerId = null;
                int maxVotes = -1;

                Map<String, Integer> counts = voteCounts.get(consti);
                for (int count : counts.values()) {
                    constiTotal += count;
                }

                for (Map.Entry<String, Integer> entry : counts.entrySet()) {
                    String candId = entry.getKey();
                    int count = entry.getValue();
                    Candidate cand = candidateMap.get(candId);

                    if (count > maxVotes) {
                        maxVotes = count;
                        winnerId = candId;
                    }

                    double percentage = constiTotal == 0 ? 0 : ((double) count / constiTotal) * 100;
                    String bar = "*".repeat((int) (percentage / 5)); // 1 star per 5%

                    String resultLine = String.format("%s (%s): %d votes (%.2f%%) | %s", cand.getName(), cand.getParty(), count, percentage, bar);
                    System.out.println(resultLine);
                    bw.write(resultLine + "\n");
                }

                if (winnerId != null) {
                    String winLine = ">> Winner for " + consti.name() + ": " + candidateMap.get(winnerId).getName() + " !!\n";
                    System.out.println(winLine);
                    bw.write(winLine + "\n");
                }
            }
            
            System.out.println("\nReport exported to election_report.txt");
        } catch (IOException e) {
            System.err.println("Error generating report: " + e.getMessage());
        }
    }
}
