package model;

public class Cat extends Pet {
    private boolean isIndoorOnly;

    public Cat() {
        super();
    }

    public Cat(String petId, String name, int age, String size,
               String healthStatus, String description, boolean isAvailable, boolean isIndoorOnly) {
        super(petId, name, age, size, healthStatus, description, isAvailable);
        this.isIndoorOnly = isIndoorOnly;
    }

    @Override
    public String getAdoptionRequirements() {
        return "Requires a fully enclosed indoor living environment, " +
               "with no plans for free-range outdoor access, and a stable household routine.";
    }

    @Override
    public String makeSound() {
        return "Meow~";
    }

    public boolean isIndoorOnly() {
        return isIndoorOnly;
    }

    public void setIndoorOnly(boolean indoorOnly) {
        isIndoorOnly = indoorOnly;
    }

    @Override
    public String toString() {
        return super.toString() + " [Cat, Indoor Only: " + (isIndoorOnly ? "Yes" : "No") + "]";
    }
}

