package rume;

public class Candidate {
    private final int id;
    private final String name;
    private final String city;
    private final int budget;

    public Candidate(int id, String name, String city, int budget) {
        if (id <= 0 || name == null || name.isBlank() || city == null || city.isBlank() || budget <= 0) {
            throw new IllegalArgumentException("Candidate data must be valid.");
        }
        this.id = id;
        this.name = name.trim();
        this.city = city.trim();
        this.budget = budget;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCity() {
        return city;
    }

    public int getBudget() {
        return budget;
    }

    @Override
    public String toString() {
        return id + " | " + name + " | " + city + " | " + budget + " KZT/month";
    }
}
