package rume;

public class TenantSearch extends HousingSearch {
    @Override
    public String getCode() {
        return "2";
    }

    @Override
    public String getDescription() {
        return "Find a tenant";
    }

    @Override
    public String getBudgetPrompt() {
        return "Enter the monthly room price in KZT: ";
    }

    @Override
    protected boolean matchesBudget(int candidateBudget, int requestedBudget) {
        return candidateBudget >= requestedBudget;
    }
}
