package util;

import manager.PetDataManager;
import manager.ApplicationManager;
import model.*;
import model.AdoptionApplication.ApplicationStatus;

import java.io.*;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;
import java.nio.file.StandardOpenOption;
import java.nio.charset.StandardCharsets;

public class FileHandler {
    private static final String DATA_DIR = "data";
    private static final String PETS_FILE = DATA_DIR + "/pets.txt";
    private static final String USERS_FILE = DATA_DIR + "/users.txt";
    private static final String APPLICATIONS_FILE = DATA_DIR + "/applications.txt";
    private static final String FAVORITES_FILE = DATA_DIR + "/favorites.txt";

    // Helper: escape commas and backslashes when writing fields
    private static String escapeField(String s) {
        if (s == null) return "null";
        return s.replace("\\", "\\\\").replace(",", "\\,");
    }

    // Helper: split a line on unescaped commas and unescape escaped characters
    private static List<String> splitEscaped(String line) {
        List<String> parts = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '\\') {
                if (i + 1 < line.length()) {
                    sb.append(line.charAt(i + 1));
                    i++; // skip next
                }
            } else if (c == ',') {
                parts.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        parts.add(sb.toString());
        return parts;
    }

    // Ensure data directory exists
    private static void ensureDataDir() {
        File dir = new File(DATA_DIR);
        if (!dir.exists()) {
            dir.mkdir();
        }
    }

    private static void logError(String msg) {
        // print to stderr and append to data/error.log for easier debugging
        System.err.println(msg);
        try {
            ensureDataDir();
            Path p = new File(DATA_DIR, "error.log").toPath();
            String entry = LocalDateTime.now().toString() + " - " + msg + System.lineSeparator();
            Files.write(p, entry.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("Failed to write error log: " + e.getMessage());
        }
    }

    // Replace temp file to target with retries and fallbacks to handle transient file locks on Windows.
    // This method will attempt an atomic move first, fall back to a non-atomic move, and as a last resort
    // copy the temp file over the target and delete the temp. It will retry several times for transient locks.
    private static synchronized void replaceFileAtomically(File tempFile, File targetFile) throws IOException {
        // Robust replace strategy for Windows: try deleting target first, then rename temp -> target
        // with retries. Fall back to Files.move or copy if needed.
        if (targetFile.exists()) {
            boolean deleted = false;
            for (int i = 0; i < 5; i++) {
                if (targetFile.delete()) {
                    deleted = true;
                    break;
                }
                try { Thread.sleep(100); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }
            if (!deleted) {
                System.err.println("Warning: Could not delete existing file: " + targetFile.getAbsolutePath());
            }
        }

        // Try rename (File.renameTo is sometimes more reliable on Windows)
        for (int attempt = 0; attempt < 5; attempt++) {
            if (tempFile.renameTo(targetFile)) {
                return; // success
            }
            try { Thread.sleep(100); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }

        // Fallback to Files.move
        try {
            Files.move(tempFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return;
        } catch (IOException e) {
            // Last resort: copy contents and delete temp
            Files.copy(tempFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            try { tempFile.delete(); } catch (Exception ex) { /* ignore */ }
        }
    }

    // Load all data
    public static void loadAllData(PetDataManager petManager, ApplicationManager appManager,
                                   List<User> userList, Map<String, Adopter> adopterMap) {
        ensureDataDir();
        loadUsers(userList, adopterMap);
        loadPets(petManager);
        loadApplications(appManager);
        loadFavorites(adopterMap);
    }

    // Save all data (called on exit)
    public static void saveAllData(List<Pet> pets, List<AdoptionApplication> applications,
                                   List<User> users, Map<String, Adopter> adopterMap) {
        savePets(pets);
        saveApplications(applications);
        saveUsers(users);
        saveFavorites(adopterMap);
    }

    // ---------- Pets file operations ----------
    public static void loadPets(PetDataManager petManager) {
        List<Pet> pets = new ArrayList<>();
        File file = new File(PETS_FILE);
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                List<String> parts = splitEscaped(line);
                if (parts.size() < 2) continue;

                String type = parts.get(0);
                if ("DOG".equals(type) && parts.size() >= 9) {
                    String petId = "null".equals(parts.get(1)) ? null : parts.get(1);
                    String name = "null".equals(parts.get(2)) ? null : parts.get(2);
                    int age = Integer.parseInt(parts.get(3));
                    String size = "null".equals(parts.get(4)) ? null : parts.get(4);
                    String health = "null".equals(parts.get(5)) ? null : parts.get(5);
                    String desc = "null".equals(parts.get(6)) ? null : parts.get(6);
                    boolean avail = Boolean.parseBoolean(parts.get(7));
                    String breed = "null".equals(parts.get(8)) ? null : parts.get(8);
                    Dog dog = new Dog(petId, name, age, size, health, desc, avail, breed);
                    pets.add(dog);
                } else if ("CAT".equals(type) && parts.size() >= 9) {
                    String petId = "null".equals(parts.get(1)) ? null : parts.get(1);
                    String name = "null".equals(parts.get(2)) ? null : parts.get(2);
                    int age = Integer.parseInt(parts.get(3));
                    String size = "null".equals(parts.get(4)) ? null : parts.get(4);
                    String health = "null".equals(parts.get(5)) ? null : parts.get(5);
                    String desc = "null".equals(parts.get(6)) ? null : parts.get(6);
                    boolean avail = Boolean.parseBoolean(parts.get(7));
                    boolean indoor = Boolean.parseBoolean(parts.get(8));
                    Cat cat = new Cat(petId, name, age, size, health, desc, avail, indoor);
                    pets.add(cat);
                }
            }
        } catch (IOException e) {
            logError("Error loading pets: " + e.getMessage());
        }
        petManager.setPetList(pets);
    }

    public static void savePets(List<Pet> pets) {
        ensureDataDir();
        File file = new File(PETS_FILE);
        File tempFile = new File(PETS_FILE + ".tmp");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
            for (Pet p : pets) {
                StringBuilder sb = new StringBuilder();
                if (p instanceof Dog) {
                    Dog d = (Dog) p;
                    sb.append("DOG,")
                      .append(escapeField(d.getPetId())).append(",")
                      .append(escapeField(d.getName())).append(",")
                      .append(d.getAge()).append(",")
                      .append(escapeField(d.getSize())).append(",")
                      .append(escapeField(d.getHealthStatus())).append(",")
                      .append(escapeField(d.getDescription())).append(",")
                      .append(d.isAvailable()).append(",")
                      .append(escapeField(d.getBreed()));
                } else if (p instanceof Cat) {
                    Cat c = (Cat) p;
                    sb.append("CAT,")
                      .append(escapeField(c.getPetId())).append(",")
                      .append(escapeField(c.getName())).append(",")
                      .append(c.getAge()).append(",")
                      .append(escapeField(c.getSize())).append(",")
                      .append(escapeField(c.getHealthStatus())).append(",")
                      .append(escapeField(c.getDescription())).append(",")
                      .append(c.isAvailable()).append(",")
                      .append(c.isIndoorOnly());
                }
                writer.write(sb.toString());
                writer.newLine();
            }
            writer.flush();
            // Atomic replace — use replaceFileAtomically for reliable cross-platform replacement
            try {
                replaceFileAtomically(tempFile, file);
            } catch (IOException e) {
                logError("Error saving pets: " + e.getMessage());
            }
        } catch (IOException e) {
            logError("Error saving pets: " + e.getMessage());
        }
    }

    // ---------- Users file operations ----------
    public static void loadUsers(List<User> userList, Map<String, Adopter> adopterMap) {
        File file = new File(USERS_FILE);
        if (!file.exists()) {
            // Create default accounts
            createDefaultUsers(userList, adopterMap);
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                List<String> parts = splitEscaped(line);
                if (parts.size() < 5) continue;

                String role = parts.get(0);
                if ("ADOPTER".equals(role) && parts.size() >= 7) {
                    String homeEnv = "null".equals(parts.get(5)) ? null : parts.get(5);
                    Adopter adopter = new Adopter(
                        parts.get(1), parts.get(2), parts.get(3), parts.get(4), homeEnv,
                        Boolean.parseBoolean(parts.get(6))
                    );
                    userList.add(adopter);
                    adopterMap.put(adopter.getUserId(), adopter);
                } else if ("STAFF".equals(role) && parts.size() >= 7) {
                    Staff staff = new Staff(
                        parts.get(1), parts.get(2), parts.get(3), parts.get(4), parts.get(5), parts.get(6)
                    );
                    userList.add(staff);
                }
            }
        } catch (IOException e) {
            logError("Error loading users: " + e.getMessage());
        }

        if (userList.isEmpty()) {
            createDefaultUsers(userList, adopterMap);
        }
    }

    private static void createDefaultUsers(List<User> userList, Map<String, Adopter> adopterMap) {
        // Create default staff
        Staff staff = new Staff("staff1", "Admin Staff", "555-0000", "123", "Administration", "Senior");
        userList.add(staff);
        // Create default adopter
        Adopter adopter = new Adopter("adopter1", "William Qin", "555-1234", "123", "Apartment with balcony", false);
        userList.add(adopter);
        adopterMap.put(adopter.getUserId(), adopter);

        saveUsers(userList);
    }

    public static void saveUsers(List<User> users) {
        ensureDataDir();
        File file = new File(USERS_FILE);
        File tempFile = new File(USERS_FILE + ".tmp");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
            for (User u : users) {
                StringBuilder sb = new StringBuilder();
                if (u instanceof Adopter) {
                    Adopter a = (Adopter) u;
                    sb.append("ADOPTER,")
                      .append(escapeField(a.getUserId())).append(",")
                      .append(escapeField(a.getName())).append(",")
                      .append(escapeField(a.getContactInfo())).append(",")
                      .append(escapeField(a.getPassword())).append(",")
                      .append(escapeField(a.getHomeEnvironment())).append(",")
                      .append(a.isHasOtherPets());
                } else if (u instanceof Staff) {
                    Staff s = (Staff) u;
                    sb.append("STAFF,")
                      .append(escapeField(s.getUserId())).append(",")
                      .append(escapeField(s.getName())).append(",")
                      .append(escapeField(s.getContactInfo())).append(",")
                      .append(escapeField(s.getPassword())).append(",")
                      .append(escapeField(s.getDepartment())).append(",")
                      .append(escapeField(s.getStaffLevel()));
                }
                writer.write(sb.toString());
                writer.newLine();
            }
            writer.flush();
            try {
                replaceFileAtomically(tempFile, file);
            } catch (IOException e) {
                System.err.println("Error saving users: " + e.getMessage());
            }
        } catch (IOException e) {
            logError("Error saving users: " + e.getMessage());
        }
    }

    // ---------- Applications file operations ----------
    public static void loadApplications(ApplicationManager appManager) {
        List<AdoptionApplication> apps = new ArrayList<>();
        File file = new File(APPLICATIONS_FILE);
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                List<String> parts = splitEscaped(line);
                if (parts.size() < 8) continue;

                AdoptionApplication app = new AdoptionApplication();
                app.setApplicationId("null".equals(parts.get(0)) ? null : parts.get(0));
                app.setPetId("null".equals(parts.get(1)) ? null : parts.get(1));
                app.setAdopterId("null".equals(parts.get(2)) ? null : parts.get(2));
                app.setApplicantName("null".equals(parts.get(3)) ? null : parts.get(3));
                app.setContactInfo("null".equals(parts.get(4)) ? null : parts.get(4));
                app.setHomeEnvironmentDesc("null".equals(parts.get(5)) ? null : parts.get(5));
                app.setStatus(ApplicationStatus.valueOf(parts.get(6)));
                app.setSubmitTime(LocalDateTime.parse(parts.get(7)));
                if (parts.size() >= 10 && !"null".equals(parts.get(8))) {
                    app.setReviewerStaffId(parts.get(8));
                    if (parts.size() >= 10 && !"null".equals(parts.get(9))) {
                        app.setReviewTime(LocalDateTime.parse(parts.get(9)));
                    }
                }
                if (parts.size() >= 11) {
                    app.setRejectReason("null".equals(parts.get(10)) ? null : parts.get(10));
                }
                apps.add(app);
            }
        } catch (IOException e) {
            logError("Error loading applications: " + e.getMessage());
        }
        appManager.setApplicationList(apps);
    }

    public static void saveApplications(List<AdoptionApplication> apps) {
        // debug: report when saveApplications is invoked
        System.out.println("Saving applications, count: " + apps.size());
        ensureDataDir();
        File file = new File(APPLICATIONS_FILE);
        File tempFile = new File(APPLICATIONS_FILE + ".tmp");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
            for (AdoptionApplication app : apps) {
                StringBuilder sb = new StringBuilder();
                sb.append(escapeField(app.getApplicationId())).append(",")
                  .append(escapeField(app.getPetId())).append(",")
                  .append(escapeField(app.getAdopterId())).append(",")
                  .append(escapeField(app.getApplicantName())).append(",")
                  .append(escapeField(app.getContactInfo())).append(",")
                  .append(escapeField(app.getHomeEnvironmentDesc())).append(",")
                  .append(app.getStatus().name()).append(",")
                  .append(app.getSubmitTime().toString()).append(",")
                  .append(escapeField(app.getReviewerStaffId())).append(",")
                  .append(app.getReviewTime() != null ? app.getReviewTime().toString() : "null").append(",")
                  .append(escapeField(app.getRejectReason()));
                writer.write(sb.toString());
                writer.newLine();
            }
            writer.flush();
            try {
                replaceFileAtomically(tempFile, file);
            } catch (IOException e) {
                logError("Error saving applications: " + e.getMessage());
            }
        } catch (IOException e) {
            logError("Error saving applications: " + e.getMessage());
        }
    }

    // ---------- Favorites file operations ----------
    public static void loadFavorites(Map<String, Adopter> adopterMap) {
        File file = new File(FAVORITES_FILE);
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                List<String> parts = splitEscaped(line);
                if (parts.size() >= 2) {
                    String adopterId = parts.get(0);
                    String petId = parts.get(1);
                    Adopter adopter = adopterMap.get(adopterId);
                    if (adopter != null) {
                        adopter.addFavorite(petId);
                    }
                }
            }
        } catch (IOException e) {
            logError("Error loading favorites: " + e.getMessage());
        }
    }

    public static void saveFavorites(Map<String, Adopter> adopterMap) {
        ensureDataDir();
        File file = new File(FAVORITES_FILE);
        File tempFile = new File(FAVORITES_FILE + ".tmp");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
            for (Adopter adopter : adopterMap.values()) {
                for (String petId : adopter.getFavoritePetIds()) {
                    writer.write(escapeField(adopter.getUserId()) + "," + escapeField(petId));
                    writer.newLine();
                }
            }
            writer.flush();
            try {
                replaceFileAtomically(tempFile, file);
            } catch (IOException e) {
                logError("Error saving favorites: " + e.getMessage());
            }
        } catch (IOException e) {
            logError("Error saving favorites: " + e.getMessage());
        }
    }
}

