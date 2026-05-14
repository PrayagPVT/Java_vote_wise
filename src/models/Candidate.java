package models;

public class Candidate {
    private String candidateId;
    private String name;
    private String party;
    private Constituency constituency;
    private String manifestoSummary;

    public Candidate(String candidateId, String name, String party, Constituency constituency, String manifestoSummary) {
        this.candidateId = candidateId;
        this.name = name;
        this.party = party;
        this.constituency = constituency;
        this.manifestoSummary = manifestoSummary;
    }

    public String getCandidateId() { return candidateId; }
    public void setCandidateId(String candidateId) { this.candidateId = candidateId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getParty() { return party; }
    public void setParty(String party) { this.party = party; }

    public Constituency getConstituency() { return constituency; }
    public void setConstituency(Constituency constituency) { this.constituency = constituency; }

    public String getManifestoSummary() { return manifestoSummary; }
    public void setManifestoSummary(String manifestoSummary) { this.manifestoSummary = manifestoSummary; }

    public String toCsv() {
        return String.join(",", candidateId, name, party, constituency.name(), manifestoSummary);
    }
    
    public static Candidate fromCsv(String csvLine) {
        String[] parts = csvLine.split(",", 5); // Manifesto might have commas, but we assume no commas for simplicity or handle it
        if (parts.length != 5) return null;
        return new Candidate(
            parts[0],
            parts[1],
            parts[2],
            Constituency.fromString(parts[3]),
            parts[4]
        );
    }
}
