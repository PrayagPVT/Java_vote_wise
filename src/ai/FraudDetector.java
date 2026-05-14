package ai;

import java.util.HashMap;
import java.util.Map;

public class FraudDetector {
    private Map<String, Integer> failedLoginAttempts;
    private static final int MAX_ATTEMPTS = 3;

    public FraudDetector() {
        this.failedLoginAttempts = new HashMap<>();
    }

    public void logFailedAttempt(String voterId) {
        failedLoginAttempts.put(voterId, failedLoginAttempts.getOrDefault(voterId, 0) + 1);
    }

    public void resetAttempts(String voterId) {
        failedLoginAttempts.remove(voterId);
    }

    public boolean isFraudulentActivity(String voterId) {
        int attempts = failedLoginAttempts.getOrDefault(voterId, 0);
        if (attempts >= MAX_ATTEMPTS) {
            System.err.println("[AI FRAUD ALERT] Suspicious activity detected for Voter ID: " + voterId + ". Too many failed logins.");
            return true;
        }
        return false;
    }
}
