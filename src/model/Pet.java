package model;

import java.io.Serializable;

public abstract class Pet implements Serializable {
    private static final long serialVersionUID = 1L;

    protected String petId;
    protected String name;
    protected int age;
    protected String size;
    protected String healthStatus;
    protected String description;
    protected boolean isAvailable;

    public Pet() {}

    public Pet(String petId, String name, int age, String size,
               String healthStatus, String description, boolean isAvailable) {
        this.petId = petId;
        this.name = name;
        this.age = age;
        this.size = size;
        this.healthStatus = healthStatus;
        this.description = description;
        this.isAvailable = isAvailable;
    }

    // Business-related abstract method: return adoption requirements
    public abstract String getAdoptionRequirements();

    // Abstract method: return the sound (for polymorphism demo)
    public abstract String makeSound();

    // Getter and Setter methods
    public String getPetId() { return petId; }
    public void setPetId(String petId) { this.petId = petId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }

    public String getHealthStatus() { return healthStatus; }
    public void setHealthStatus(String healthStatus) { this.healthStatus = healthStatus; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { isAvailable = available; }

    @Override
    public String toString() {
        return String.format("%s: %s (%s)", petId, name, isAvailable ? "Available" : "Adopted");
    }
}

