package model;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class Adopter extends User {
    private String homeEnvironment;
    private boolean hasOtherPets;
    private List<String> favoritePetIds;

    public Adopter() {
        super();
        this.favoritePetIds = new ArrayList<>();
    }

    public Adopter(String userId, String name, String contactInfo, String password,
                   String homeEnvironment, boolean hasOtherPets) {
        super(userId, name, contactInfo, password);
        this.homeEnvironment = homeEnvironment;
        this.hasOtherPets = hasOtherPets;
        this.favoritePetIds = new ArrayList<>();
    }

    @Override
    public boolean hasPermission(String operation) {
        // Adopter allowed operations: browse, submit application, manage favorites, view own applications
        return operation.equals("BROWSE_PETS") ||
               operation.equals("SUBMIT_APPLICATION") ||
               operation.equals("MANAGE_FAVORITES") ||
               operation.equals("VIEW_OWN_APPLICATIONS");
    }

    @Override
    public JPanel getAuthorizedPanel(JFrame parentFrame) {
        // Actual implementation is in the gui package; return null here and MainFrame will construct panels by type
        return null;
    }

    @Override
    public String displayRole() {
        return "Adopter";
    }

    // Favorites operations
    public void addFavorite(String petId) {
        if (!favoritePetIds.contains(petId)) {
            favoritePetIds.add(petId);
        }
    }

    public void removeFavorite(String petId) {
        favoritePetIds.remove(petId);
    }

    public boolean isFavorite(String petId) {
        return favoritePetIds.contains(petId);
    }

    public List<String> getFavoritePetIds() {
        return favoritePetIds;
    }

    // Getters and Setters
    public String getHomeEnvironment() {
        return homeEnvironment;
    }

    public void setHomeEnvironment(String homeEnvironment) {
        this.homeEnvironment = homeEnvironment;
    }

    public boolean isHasOtherPets() {
        return hasOtherPets;
    }

    public void setHasOtherPets(boolean hasOtherPets) {
        this.hasOtherPets = hasOtherPets;
    }
}

