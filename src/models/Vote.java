package models;

public class Vote {
    private String voterId;
    private String candidateId;
    private Constituency constituency;
    private String encryptedVoteData;
    private long timestamp;

    public Vote(String voterId, String candidateId, Constituency constituency, String encryptedVoteData, long timestamp) {
        this.voterId = voterId;
        this.candidateId = candidateId;
        this.constituency = constituency;
        this.encryptedVoteData = encryptedVoteData;
        this.timestamp = timestamp;
    }

    public String getVoterId() { return voterId; }
    public String getCandidateId() { return candidateId; }
    public Constituency getConstituency() { return constituency; }
    public String getEncryptedVoteData() { return encryptedVoteData; }
    public long getTimestamp() { return timestamp; }

    public String toCsv() {
        return String.join(",", voterId, candidateId, constituency.name(), encryptedVoteData, String.valueOf(timestamp));
    }
    
    public static Vote fromCsv(String csvLine) {
        String[] parts = csvLine.split(",");
        if (parts.length != 5) return null;
        return new Vote(
            parts[0],
            parts[1],
            Constituency.fromString(parts[2]),
            parts[3],
            Long.parseLong(parts[4])
        );
    }
}
