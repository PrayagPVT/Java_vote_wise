package services;

import exceptions.InvalidVoterException;
import models.Constituency;
import models.Voter;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class AuthService {
    private FileService fileService;

    public AuthService(FileService fileService) {
        this.fileService = fileService;
    }

    public Voter registerVoter(String voterId, String name, int age, Constituency constituency, String password) throws InvalidVoterException {
        if (age < 18) {
            throw new InvalidVoterException("Voter must be 18 or older.");
        }

        List<Voter> voters = fileService.loadVoters();
        for (Voter v : voters) {
            if (v.getVoterId().equals(voterId)) {
                throw new InvalidVoterException("Voter ID already exists.");
            }
        }

        String hashedPassword = hashPassword(password);
        Voter newVoter = new Voter(voterId, name, age, constituency, hashedPassword, false);
        voters.add(newVoter);
        fileService.saveVoters(voters);

        return newVoter;
    }

    public Voter loginVoter(String voterId, String password) throws InvalidVoterException {
        List<Voter> voters = fileService.loadVoters();
        String hashedPassword = hashPassword(password);

        for (Voter v : voters) {
            if (v.getVoterId().equals(voterId) && v.getHashedPassword().equals(hashedPassword)) {
                return v;
            }
        }
        throw new InvalidVoterException("Invalid Voter ID or Password.");
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }
}
