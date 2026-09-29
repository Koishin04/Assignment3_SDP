package rume;

import java.util.List;
import java.util.Scanner;
import java.util.ServiceConfigurationError;

public class Main {
    public static void main(String[] args) {
        AppRegistry registry = new AppRegistry();
        try {
            registry.load();
        } catch (IllegalArgumentException | IllegalStateException | ServiceConfigurationError error) {
            System.out.println("Cannot load application: " + error.getMessage());
            return;
        }
        Scanner scanner = new Scanner(System.in);
        System.out.println("RuMe Housing Search");
        System.out.println("All profiles are fictional. Amounts are monthly budgets in KZT.");
        while (true) {
            System.out.println();
            for (HousingSearch option : registry.getSearches()) {
                System.out.println(option.getCode() + ". " + option.getDescription());
            }
            System.out.print("Choose a search or type exit: ");
            if (!scanner.hasNextLine()) {
                return;
            }
            String choice = scanner.nextLine().trim();
            if (choice.equalsIgnoreCase("exit")) {
                return;
            }
            try {
                HousingSearch search = registry.getSearch(choice);
                System.out.println("Catalogs: " + String.join(" / ", registry.getSourceCodes()));
                System.out.print("Enter catalog: ");
                if (!scanner.hasNextLine()) {
                    return;
                }
                search.setSource(registry.getSource(scanner.nextLine()));
                System.out.print("Enter city (demo data: Astana, Almaty, Aktobe): ");
                if (!scanner.hasNextLine()) {
                    return;
                }
                String city = scanner.nextLine();
                System.out.print(search.getBudgetPrompt());
                if (!scanner.hasNextLine()) {
                    return;
                }
                int budget = Integer.parseInt(scanner.nextLine().trim());
                List<Candidate> result = search.search(city, budget);
                if (result.isEmpty()) {
                    System.out.println("No matching candidates.");
                } else {
                    System.out.println("ID | Name | City | Monthly budget");
                    for (Candidate candidate : result) {
                        System.out.println(candidate);
                    }
                }
            } catch (NumberFormatException error) {
                System.out.println("Enter a positive whole number within the Java int range.");
            } catch (IllegalArgumentException | CandidateSourceException error) {
                System.out.println("Search failed: " + error.getMessage());
            }
        }
    }
}
