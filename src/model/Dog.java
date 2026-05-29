package model;

public class Dog extends Pet {
    private String breed;

    public Dog() {
        super();
    }

    public Dog(String petId, String name, int age, String size,
               String healthStatus, String description, boolean isAvailable, String breed) {
        super(petId, name, age, size, healthStatus, description, isAvailable);
        this.breed = breed;
    }

    @Override
    public String getAdoptionRequirements() {
        return "Requires proof of a fixed residence with adequate outdoor activity space, " +
               "and a commitment to regular exercise and socialization training.";
    }

    @Override
    public String makeSound() {
        return "Woof! Woof!";
    }

    public String getBreed() {
        return breed;
    }

    public void setBreed(String breed) {
        this.breed = breed;
    }

    @Override
    public String toString() {
        return super.toString() + " [Dog, Breed: " + breed + "]";
    }
}

