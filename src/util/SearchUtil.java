package util;

import model.Pet;
import java.util.ArrayList;
import java.util.List;

public class SearchUtil {

    // Overload 1: keyword search (name or description)
    public static List<Pet> searchPets(List<Pet> list, String keyword) {
        List<Pet> result = new ArrayList<>();
        String lowerKeyword = keyword.toLowerCase();
        for (Pet p : list) {
            if (p.getName().toLowerCase().contains(lowerKeyword) ||
                p.getDescription().toLowerCase().contains(lowerKeyword)) {
                result.add(p);
            }
        }
        return result;
    }

    // Overload 2: search by species and maximum age
    public static List<Pet> searchPets(List<Pet> list, String species, int maxAge) {
        List<Pet> result = new ArrayList<>();
        for (Pet p : list) {
            boolean speciesMatch = "All".equals(species) ||
                (p instanceof model.Dog && "Dog".equals(species)) ||
                (p instanceof model.Cat && "Cat".equals(species));
            if (speciesMatch && p.getAge() <= maxAge) {
                result.add(p);
            }
        }
        return result;
    }

    // Overload 3: advanced multi-criteria search
    public static List<Pet> searchPets(List<Pet> list, String species, int minAge, int maxAge, String healthStatus) {
        List<Pet> result = new ArrayList<>();
        for (Pet p : list) {
            boolean speciesMatch = "All".equals(species) ||
                (p instanceof model.Dog && "Dog".equals(species)) ||
                (p instanceof model.Cat && "Cat".equals(species));
            boolean ageMatch = p.getAge() >= minAge && p.getAge() <= maxAge;
            boolean healthMatch = "All".equals(healthStatus) || p.getHealthStatus().equalsIgnoreCase(healthStatus);

            if (speciesMatch && ageMatch && healthMatch) {
                result.add(p);
            }
        }
        return result;
    }
}

