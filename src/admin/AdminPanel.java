package admin;

import models.Candidate;
import models.Constituency;
import models.Voter;
import services.FileService;

import java.util.List;
import java.util.Scanner;

public class AdminPanel {
    private static AdminPanel instance;
    private FileService fileService;
    private boolean isElectionOpen = false;

    private AdminPanel(FileService fileService) {
        this.fileService = fileService;
    }

    public static AdminPanel getInstance(FileService fileService) {
        if (instance == null) {
            instance = new AdminPanel(fileService);
        }
        return instance;
    }

    public boolean login(String username, String password) {
        return "admin".equals(username) && "admin123".equals(password);
    }

    public void addCandidate(Scanner scanner) {
        System.out.println("--- Add Candidate ---");
        System.out.print("Candidate ID: ");
        String id = scanner.nextLine();
        System.out.print("Name: ");
        String name = scanner.nextLine();
        System.out.print("Party: ");
        String party = scanner.nextLine();
        System.out.println("Select Constituency: ");
        for (Constituency c : Constituency.values()) {
            System.out.println("- " + c.name());
        }
        System.out.print("Enter Constituency: ");
        String cString = scanner.nextLine();
        Constituency constituency = Constituency.fromString(cString);
        if (constituency == null) {
            System.out.println("Invalid constituency.");
            return;
        }
        System.out.print("Manifesto Summary (keywords separated by space): ");
        String manifesto = scanner.nextLine();

        Candidate newCandidate = new Candidate(id, name, party, constituency, manifesto);
        List<Candidate> candidates = fileService.loadCandidates();
        
        // Simple check if candidate ID exists
        if (candidates.stream().anyMatch(c -> c.getCandidateId().equals(id))) {
            System.out.println("Candidate ID already exists.");
            return;
        }
        
        candidates.add(newCandidate);
        fileService.saveCandidates(candidates);
        System.out.println("Candidate added successfully!");
    }

    public void removeCandidate(Scanner scanner) {
        System.out.print("Enter Candidate ID to remove: ");
        String id = scanner.nextLine();
        List<Candidate> candidates = fileService.loadCandidates();
        boolean removed = candidates.removeIf(c -> c.getCandidateId().equals(id));
        if (removed) {
            fileService.saveCandidates(candidates);
            System.out.println("Candidate removed successfully!");
        } else {
            System.out.println("Candidate not found.");
        }
    }

    public void viewRegisteredVoters() {
        System.out.println("--- Registered Voters ---");
        List<Voter> voters = fileService.loadVoters();
        if (voters.isEmpty()) {
            System.out.println("No voters registered.");
        } else {
            for (Voter v : voters) {
                System.out.printf("ID: %s | Name: %s | Age: %d | Constituency: %s | Has Voted: %b\n",
                        v.getVoterId(), v.getName(), v.getAge(), v.getConstituency().name(), v.hasVoted());
            }
        }
    }

    public void resetElectionData() {
        fileService.clearElectionData();
    }

    public void toggleElectionStatus() {
        isElectionOpen = !isElectionOpen;
        System.out.println("Election is now " + (isElectionOpen ? "OPEN" : "CLOSED"));
    }

    public boolean isElectionOpen() {
        return isElectionOpen;
    }
}
