package models;

public class Voter {
    private String voterId;
    private String name;
    private int age;
    private Constituency constituency;
    private String hashedPassword;
    private boolean hasVoted;

    public Voter(String voterId, String name, int age, Constituency constituency, String hashedPassword, boolean hasVoted) {
        this.voterId = voterId;
        this.name = name;
        this.age = age;
        this.constituency = constituency;
        this.hashedPassword = hashedPassword;
        this.hasVoted = hasVoted;
    }

    public String getVoterId() { return voterId; }
    public void setVoterId(String voterId) { this.voterId = voterId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public Constituency getConstituency() { return constituency; }
    public void setConstituency(Constituency constituency) { this.constituency = constituency; }

    public String getHashedPassword() { return hashedPassword; }
    public void setHashedPassword(String hashedPassword) { this.hashedPassword = hashedPassword; }

    public boolean hasVoted() { return hasVoted; }
    public void setHasVoted(boolean hasVoted) { this.hasVoted = hasVoted; }
    
    // To CSV string format
    public String toCsv() {
        return String.join(",", voterId, name, String.valueOf(age), constituency.name(), hashedPassword, String.valueOf(hasVoted));
    }
    
    // From CSV string format
    public static Voter fromCsv(String csvLine) {
        String[] parts = csvLine.split(",");
        if (parts.length != 6) return null;
        return new Voter(
            parts[0],
            parts[1],
            Integer.parseInt(parts[2]),
            Constituency.fromString(parts[3]),
            parts[4],
            Boolean.parseBoolean(parts[5])
        );
    }
}
