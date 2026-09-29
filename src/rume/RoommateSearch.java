package rume;

public class RoommateSearch extends HousingSearch {
    @Override
    public String getCode() {
        return "1";
    }

    @Override
    public String getDescription() {
        return "Find a roommate";
    }

    @Override
    public String getBudgetPrompt() {
        return "Enter your planned monthly budget in KZT: ";
    }

    @Override
    protected boolean matchesBudget(int candidateBudget, int requestedBudget) {
        return candidateBudget == requestedBudget;
    }
}
