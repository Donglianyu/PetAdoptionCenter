package manager;

import model.Pet;
import model.Dog;
import model.Cat;
import exception.PetNotFoundException;

/**
 * Simple factory to create Pet instances based on a type string.
 */
public class PetFactory {
    public static Pet createPet(String type, String id, String name, int age, String size,
                                String health, String desc, boolean available, String extra) {
        if (type == null) throw new IllegalArgumentException("Pet type cannot be null");
        switch (type.toUpperCase()) {
            case "DOG":
                return new Dog(id, name, age, size, health, desc, available, extra);
            case "CAT":
                boolean indoor = "true".equalsIgnoreCase(extra) || "yes".equalsIgnoreCase(extra);
                return new Cat(id, name, age, size, health, desc, available, indoor);
            default:
                throw new IllegalArgumentException("Unknown pet type: " + type);
        }
    }
}

