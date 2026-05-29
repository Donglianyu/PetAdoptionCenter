package util;

import model.AdoptionApplication;
import model.AdoptionApplication.ApplicationStatus;
import model.Pet;
import model.Dog;
import model.Cat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AnalyticsService {

    /**
     * Utility class that generates textual analytics reports and provides
     * structured data (maps) useful for building visualizations.
     *
     * The methods here avoid any UI dependencies so they can be used both
     * by text-only reports and by charting components.
     */

    public static String generateAnalyticsReport(List<AdoptionApplication> applications, List<Pet> pets) {
        StringBuilder report = new StringBuilder();
        report.append("===== Adoption Center Analytics Report =====\n\n");

        // Total successful adoptions
        long totalAdoptions = applications.stream()
                .filter(app -> app.getStatus() == ApplicationStatus.APPROVED)
                .count();
        report.append("Total Successful Adoptions: ").append(totalAdoptions).append("\n\n");

        // Today's successful adoptions
        LocalDate today = LocalDate.now();
        long todayAdoptions = applications.stream()
                .filter(app -> app.getStatus() == ApplicationStatus.APPROVED)
                .filter(app -> app.getReviewTime() != null && app.getReviewTime().toLocalDate().equals(today))
                .count();
        report.append("Today's Adoptions: ").append(todayAdoptions).append("\n");

        // This month's successful adoptions
        int currentMonth = today.getMonthValue();
        int currentYear = today.getYear();
        long monthAdoptions = applications.stream()
                .filter(app -> app.getStatus() == ApplicationStatus.APPROVED)
                .filter(app -> app.getReviewTime() != null)
                .filter(app -> app.getReviewTime().getYear() == currentYear &&
                               app.getReviewTime().getMonthValue() == currentMonth)
                .count();
        report.append("This Month's Adoptions: ").append(monthAdoptions).append("\n\n");

        // Count by species (needs to look up pets from applications)
        Map<String, Long> speciesCount = applications.stream()
                .filter(app -> app.getStatus() == ApplicationStatus.APPROVED)
                .collect(Collectors.groupingBy(
                    app -> {
                        Pet p = findPetById(pets, app.getPetId());
                        if (p instanceof Dog) return "Dog";
                        else if (p instanceof Cat) return "Cat";
                        else return "Other";
                    },
                    Collectors.counting()
                ));
        report.append("Adoptions by Species:\n");
        speciesCount.forEach((species, count) ->
            report.append("  - ").append(species).append(": ").append(count).append("\n"));

        // Pending applications count
        long pendingCount = applications.stream()
                .filter(app -> app.getStatus() == ApplicationStatus.PENDING)
                .count();
        report.append("\nPending Applications: ").append(pendingCount).append("\n");

        // Currently available pets count
        long availablePets = pets.stream().filter(Pet::isAvailable).count();
        report.append("Currently Available Pets: ").append(availablePets).append("\n");

        return report.toString();
    }

    /**
     * Return a map of species to number of approved adoptions.
     * This is useful for building charts without parsing text reports.
     */
    public static java.util.Map<String, Long> getSpeciesCountMap(List<AdoptionApplication> applications, List<Pet> pets) {
        return applications.stream()
                .filter(app -> app.getStatus() == ApplicationStatus.APPROVED)
                .collect(Collectors.groupingBy(
                        app -> {
                            Pet p = findPetById(pets, app.getPetId());
                            if (p instanceof Dog) return "Dog";
                            else if (p instanceof Cat) return "Cat";
                            else return "Other";
                        },
                        Collectors.counting()
                ));
    }

    /**
     * Return a map of LocalDate -> adoptions count for the recent N days (including today).
     * The returned map contains entries for days with zero adoptions as well.
     */
    public static java.util.Map<LocalDate, Long> getDailyAdoptions(List<AdoptionApplication> applications, int daysBack) {
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(daysBack - 1);
        java.util.Map<LocalDate, Long> map = new java.util.HashMap<>();
        // initialize with zeroes
        for (int i = 0; i < daysBack; i++) {
            map.put(start.plusDays(i), 0L);
        }

        applications.stream()
                .filter(app -> app.getStatus() == ApplicationStatus.APPROVED)
                .filter(app -> app.getReviewTime() != null)
                .forEach(app -> {
                    LocalDate d = app.getReviewTime().toLocalDate();
                    if (!d.isBefore(start) && !d.isAfter(end)) {
                        map.put(d, map.getOrDefault(d, 0L) + 1);
                    }
                });

        return map;
    }

    private static Pet findPetById(List<Pet> pets, String petId) {
        return pets.stream().filter(p -> p.getPetId().equals(petId)).findFirst().orElse(null);
    }

    //
    public static String generateDateRangeReport(List<AdoptionApplication> applications,
                                                  LocalDate start, LocalDate end) {
        StringBuilder report = new StringBuilder();
        report.append("Adoption Report from ").append(start).append(" to ").append(end).append("\n\n");

        List<AdoptionApplication> filtered = applications.stream()
                .filter(app -> app.getStatus() == ApplicationStatus.APPROVED)
                .filter(app -> app.getReviewTime() != null)
                .filter(app -> {
                    LocalDate reviewDate = app.getReviewTime().toLocalDate();
                    return !reviewDate.isBefore(start) && !reviewDate.isAfter(end);
                })
                .collect(Collectors.toList());

        report.append("Total Adoptions in Period: ").append(filtered.size()).append("\n");
        for (AdoptionApplication app : filtered) {
            report.append("  - App ID: ").append(app.getApplicationId())
                  .append(", Pet: ").append(app.getPetId())
                  .append(", Date: ").append(app.getFormattedReviewTime()).append("\n");
        }

        return report.toString();
    }
}

