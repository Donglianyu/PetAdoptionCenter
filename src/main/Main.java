package main;

import gui.MainFrame;
import manager.*;
import model.*;
import util.FileHandler;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        // Set Swing look and feel to match system UI where possible
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Initialize data managers and in-memory collections
        // PetDataManager: holds all pets
        // ApplicationManager: holds adoption applications
        // userList: list of users loaded from storage
        // adopterMap: map of adopterId -> Adopter for quick lookup
        PetDataManager petManager = PetDataManager.getInstance();
        ApplicationManager appManager = ApplicationManager.getInstance();
        List<User> userList = new ArrayList<>();
        Map<String, Adopter> adopterMap = new HashMap<>();

        // Load persisted data from the data files into managers/collections
        FileHandler.loadAllData(petManager, appManager, userList, adopterMap);

        // If the data files had no pets, populate with small sample dataset
        if (petManager.getAllPets().isEmpty()) {
            createSampleData(petManager);
        }

        // Create the main frame directly. The frame will show an embedded login panel
        // if no user is currently logged in, keeping the application to a single window.
        SwingUtilities.invokeLater(() -> {
            MainFrame mainFrame = new MainFrame(petManager, appManager, userList, adopterMap);
            mainFrame.setVisible(true);
        });
    }

    private static void createSampleData(PetDataManager petManager) {
        // Insert a few sample pets so the UI has something to display when data files are empty
        petManager.add(new Dog("P001", "Buddy", 3, "Medium", "Healthy",
                "Friendly golden retriever, loves to play fetch.", true, "Golden Retriever"));
        petManager.add(new Dog("P002", "Max", 2, "Large", "Healthy",
                "Energetic German Shepherd, good with kids.", true, "German Shepherd"));
        petManager.add(new Cat("P003", "Luna", 1, "Small", "Healthy",
                "Calm and affectionate cat, litter trained.", true, true));
        petManager.add(new Cat("P004", "Oliver", 4, "Medium", "Needs dental care",
                "Senior cat, loves to nap in sunny spots.", true, true));
        petManager.add(new Dog("P005", "Charlie", 5, "Large", "Healthy",
                "Loyal Labrador Retriever, great with families.", true, "Labrador Retriever"));
        petManager.add(new Cat("P006", "Milo", 2, "Small", "Healthy",
                "Playful kitten, loves toys.", true, false));
        petManager.add(new Dog("P007", "Rocky", 6, "Medium", "Healthy",
                "Strong and protective Rottweiler, needs experienced owner.", true, "Rottweiler"));
        petManager.add(new Cat("P008", "Bella", 3, "Medium", "Healthy",
                "Affectionate cat, good with other pets.", true, true));
        petManager.add(new Dog("P009", "Daisy", 4, "Small", "Healthy",
                "Sweet Beagle, loves to sniff around.", true, "Beagle"));
        petManager.add(new Cat("P010", "Lucy", 5, "Medium", "Healthy",
                "Independent cat, enjoys quiet environments.", true, false));
    }
}

