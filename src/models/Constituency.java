package models;

public enum Constituency {
    NORTH_DISTRICT,
    SOUTH_DISTRICT,
    EAST_DISTRICT,
    WEST_DISTRICT,
    CENTRAL_DISTRICT;

    public static Constituency fromString(String text) {
        for (Constituency b : Constituency.values()) {
            if (b.name().equalsIgnoreCase(text)) {
                return b;
            }
        }
        return null;
    }
}
