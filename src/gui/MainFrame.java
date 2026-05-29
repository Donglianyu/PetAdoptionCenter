package gui;

/**
 * MainFrame is the primary application window. It composes several role-based
 * panels (browse, adopter actions, staff management, analytics) and coordinates
 * interactions between UI controls and backend managers.
 */
import manager.*;
import model.*;
import model.AdoptionApplication.ApplicationStatus;
import exception.*;
import util.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

public class MainFrame extends JFrame {
    private PetDataManager petManager;
    private ApplicationManager appManager;
    private List<User> userList;
    private Map<String, Adopter> adopterMap;

    private JTabbedPane tabbedPane;
    private JTable petTable;
    private DefaultTableModel petTableModel;
    private JTable appTable;
    private DefaultTableModel appTableModel;
    private JTextArea statusArea;

    private JTextField keywordField;
    private JComboBox<String> speciesCombo;
    private JSpinner minAgeSpinner, maxAgeSpinner;
    private JCheckBox availableOnlyCheck;
    private JComboBox<String> sortCombo;

    public MainFrame(PetDataManager petManager, ApplicationManager appManager,
                     List<User> userList, Map<String, Adopter> adopterMap) {
        this.petManager = petManager;
        this.appManager = appManager;
        this.userList = userList;
        this.adopterMap = adopterMap;

        setTitle("Community Pet Adoption Center Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);

        initComponents();

        // Apply glass look after components are initialized
        UIUtils.applyGlassLook(this);
        // initial theme: blue for pre-login screens, pink for post-login
        if (SessionContext.getCurrentUser() == null) {
            UIUtils.applyBlueTheme(this);
        } else {
            UIUtils.applyPinkTheme(this);
        }

        // Save data on window close
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                FileHandler.saveAllData(petManager.getAllPets(), appManager.getAllApplications(),
                        userList, adopterMap);
            }
        });
    }

    private void initComponents() {
        // use BorderLayout for main frame content
        setLayout(new BorderLayout());

        // If there is no logged-in user yet, show an embedded login panel inside this single window.
        // This allows the application to run as a single-window UI (login and system in one frame).
        User currentUser = SessionContext.getCurrentUser();
        if (currentUser == null) {
            createEmbeddedLoginPanel();
            return;
        }

        // Top tabbed pane
        tabbedPane = new JTabbedPane();
        add(tabbedPane, BorderLayout.CENTER);

        // All users have a "Browse Pets" tab
        tabbedPane.addTab("Browse Pets", createBrowsePetsPanel());

        // Add role-specific tabs
        if (currentUser instanceof Adopter) {
            tabbedPane.addTab("My Applications & Favorites", createAdopterPanel());
        } else if (currentUser instanceof Staff) {
            tabbedPane.addTab("Pet Management", createStaffPetPanel());
            tabbedPane.addTab("Application Review", createStaffAppPanel());
            tabbedPane.addTab("Analytics Dashboard", createAnalyticsPanel());
        }

        // Bottom status area
        statusArea = new JTextArea(2, 80);
        statusArea.setEditable(false);
        statusArea.setBackground(new Color(240, 240, 240));
        JScrollPane statusScroll = new JScrollPane(statusArea);
        statusScroll.setPreferredSize(new Dimension(800, 50));
        add(statusScroll, BorderLayout.SOUTH);

        // Welcome message
        updateStatus("Welcome, " + currentUser.getName() + " (" + currentUser.displayRole() + ")");

        // Initial refresh of pet table
        refreshPetTable(petManager.getAllPets());
    }

    /**
     * Create an embedded login panel shown inside the main frame when no user is logged in.
     * On successful login this method will clear the frame and re-run initComponents to build the
     * main application UI, keeping the application as a single window.
     */
    private void createEmbeddedLoginPanel() {
        // use a gradient background for the login panel (vertical hot-pink -> cyan)
        GradientPanel loginPanel = new GradientPanel(new Color(255,110,199), new Color(0,245,255), new BorderLayout(10,10));
        loginPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Community Pet Adoption Center", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        loginPanel.add(titleLabel, BorderLayout.NORTH);

         JPanel inputPanel = new JPanel(new GridBagLayout());
         inputPanel.setOpaque(false); // let the gradient show through
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        inputPanel.add(new JLabel("User ID:"), gbc);
        gbc.gridx = 1;
        JTextField userIdField = new JTextField(15);
        inputPanel.add(userIdField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        inputPanel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        JPasswordField passwordField = new JPasswordField(15);
        inputPanel.add(passwordField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        inputPanel.add(new JLabel("Login as:"), gbc);
        gbc.gridx = 1;
        JPanel rolePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JRadioButton adopterRadio = new JRadioButton("Adopter", true);
        JRadioButton staffRadio = new JRadioButton("Staff");
        ButtonGroup roleGroup = new ButtonGroup();
        roleGroup.add(adopterRadio);
        roleGroup.add(staffRadio);
        rolePanel.add(adopterRadio);
        rolePanel.add(staffRadio);
        inputPanel.add(rolePanel, gbc);

        loginPanel.add(inputPanel, BorderLayout.CENTER);

         JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
         buttonPanel.setOpaque(false);
        JButton loginButton = new JButton("Login");
        JButton exitButton = new JButton("Exit");
        buttonPanel.add(loginButton);
        buttonPanel.add(exitButton);
        loginPanel.add(buttonPanel, BorderLayout.SOUTH);

         JLabel hintLabel = new JLabel("Default accounts: adopter1/123 or staff1/123", JLabel.CENTER);
         hintLabel.setForeground(new Color(40,40,48));
         // place hint below buttons but keep a translucent footer
         JPanel footer = new JPanel(new BorderLayout());
         footer.setOpaque(false);
         footer.add(hintLabel, BorderLayout.CENTER);
         loginPanel.add(footer, BorderLayout.SOUTH);

        // Add to frame
        getContentPane().removeAll();
        add(loginPanel, BorderLayout.CENTER);
        revalidate();
        repaint();

        // Action handlers
        exitButton.addActionListener(e -> System.exit(0));

        Runnable doLogin = () -> {
            String userId = userIdField.getText().trim();
            String password = new String(passwordField.getPassword());
            if (userId.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter User ID and Password.",
                        "Login Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            User matchedUser = null;
            for (User u : userList) {
                if (u.getUserId().equals(userId) && u.validatePassword(password)) {
                    if (adopterRadio.isSelected() && u instanceof Adopter) {
                        matchedUser = u;
                        break;
                    } else if (staffRadio.isSelected() && u instanceof Staff) {
                        matchedUser = u;
                        break;
                    }
                }
            }

            if (matchedUser != null) {
                SessionContext.setCurrentUser(matchedUser);
                // Rebuild the frame content for the logged-in user
                getContentPane().removeAll();
                initComponents();
                    // after login, switch to pink theme for all panels
                    UIUtils.applyPinkTheme(this);
                revalidate();
                repaint();
                updateStatus("User " + matchedUser.getName() + " logged in.");
            } else {
                JOptionPane.showMessageDialog(this,
                        "Invalid credentials or role mismatch.",
                        "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
        };

        loginButton.addActionListener(e -> doLogin.run());
        userIdField.addActionListener(e -> passwordField.requestFocus());
        passwordField.addActionListener(e -> doLogin.run());
    }

    // Note: this frame contains multiple panels for different roles.
    // Important methods: createBrowsePetsPanel (search UI), createStaffPetPanel (CRUD for pets),
    // createStaffAppPanel (application review), and createAnalyticsPanel (reports + charts).

    private void updateStatus(String message) {
        statusArea.append(message + "\n");
        statusArea.setCaretPosition(statusArea.getDocument().getLength());
    }

    // ---------- Browse Pets panel ----------
    private JPanel createBrowsePetsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setBackground(new Color(255, 240, 245));

        // Left filter panel
         JPanel filterPanel = new JPanel();
        filterPanel.setLayout(new BoxLayout(filterPanel, BoxLayout.Y_AXIS));
        filterPanel.setBorder(BorderFactory.createTitledBorder("Search & Filter"));
         filterPanel.setBackground(new Color(255, 245, 250));

        // Keyword
        filterPanel.add(new JLabel("Keyword:"));
        keywordField = new JTextField(15);
        keywordField.setMaximumSize(new Dimension(200, 25));
        filterPanel.add(keywordField);
        filterPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Species
        filterPanel.add(new JLabel("Species:"));
        speciesCombo = new JComboBox<>(new String[]{"All", "Dog", "Cat"});
        speciesCombo.setMaximumSize(new Dimension(200, 25));
        filterPanel.add(speciesCombo);
        filterPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Age range
        filterPanel.add(new JLabel("Min Age:"));
        minAgeSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 30, 1));
        minAgeSpinner.setMaximumSize(new Dimension(100, 25));
        filterPanel.add(minAgeSpinner);

        filterPanel.add(new JLabel("Max Age:"));
        maxAgeSpinner = new JSpinner(new SpinnerNumberModel(20, 0, 30, 1));
        maxAgeSpinner.setMaximumSize(new Dimension(100, 25));
        filterPanel.add(maxAgeSpinner);
        filterPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Show only available pets
        availableOnlyCheck = new JCheckBox("Show Only Available Pets", true);
        filterPanel.add(availableOnlyCheck);
        filterPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Sort options
        filterPanel.add(new JLabel("Sort by:"));
        sortCombo = new JComboBox<>(new String[]{"ID", "Name", "Age", "Species"});
        sortCombo.setMaximumSize(new Dimension(200, 25));
        filterPanel.add(sortCombo);
        filterPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Buttons
        JButton searchButton = new JButton("Search");
        searchButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        JButton resetButton = new JButton("Reset");
        resetButton.setAlignmentX(Component.LEFT_ALIGNMENT);

        filterPanel.add(searchButton);
        filterPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        filterPanel.add(resetButton);

        // Center table
        String[] columns = {"ID", "Name", "Species", "Age", "Size", "Health", "Available"};
        petTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        petTable = new JTable(petTableModel);
        petTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        // apply pastel tint to the pet info table and its scroll viewport so this area is colored
        petTable.setBackground(new Color(255, 250, 252));
        petTable.setForeground(new Color(30,30,36));
        if (petTable.getTableHeader() != null) {
            petTable.getTableHeader().setBackground(new Color(255, 110, 199));
            petTable.getTableHeader().setForeground(Color.BLACK);
        }
        JScrollPane tableScroll = new JScrollPane(petTable);
        tableScroll.getViewport().setBackground(new Color(255, 240, 245));

        // Right-side button panel (role-specific)
         JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
         buttonPanel.setBackground(new Color(255, 245, 250));

        JButton viewDetailButton = new JButton("View Details");
        viewDetailButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        viewDetailButton.addActionListener(e -> viewPetDetails());
        buttonPanel.add(viewDetailButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        User currentUser = SessionContext.getCurrentUser();
        if (currentUser instanceof Adopter) {
            JButton applyButton = new JButton("Apply for Adoption");
            applyButton.setAlignmentX(Component.CENTER_ALIGNMENT);
            applyButton.addActionListener(e -> applyForAdoption());
            buttonPanel.add(applyButton);
            buttonPanel.add(Box.createRigidArea(new Dimension(0, 10)));

            JButton favoriteButton = new JButton("Add to Favorites");
            favoriteButton.setAlignmentX(Component.CENTER_ALIGNMENT);
            favoriteButton.addActionListener(e -> addToFavorites());
            buttonPanel.add(favoriteButton);
            buttonPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        }

        // ----- Add Refresh button for Browse Pets panel (visible to all users) -----
        JButton refreshBrowseButton = new JButton("Refresh");
        refreshBrowseButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        refreshBrowseButton.addActionListener(e -> {
            resetSearch(); // Reset filters and reload all pets
            updateStatus("Browse panel refreshed.");
        });
        buttonPanel.add(refreshBrowseButton);
        // -------------------------------------------------------------------------

        // Assemble panels
         JPanel leftPanel = new JPanel(new BorderLayout());
         leftPanel.setBackground(new Color(255, 240, 245));
         leftPanel.add(filterPanel, BorderLayout.NORTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, tableScroll);
        splitPane.setDividerLocation(250);

         panel.add(splitPane, BorderLayout.CENTER);
         panel.add(buttonPanel, BorderLayout.EAST);

        // Event listeners
        searchButton.addActionListener(e -> performSearch());
        resetButton.addActionListener(e -> resetSearch());

        return panel;
    }

    private void performSearch() {
        String keyword = keywordField.getText().trim();
        String species = (String) speciesCombo.getSelectedItem();
        int minAge = (int) minAgeSpinner.getValue();
        int maxAge = (int) maxAgeSpinner.getValue();
        boolean availableOnly = availableOnlyCheck.isSelected();
        String sortBy = (String) sortCombo.getSelectedItem();

        List<Pet> allPets = petManager.getAllPets();
        List<Pet> result;

        //
        if (!keyword.isEmpty()) {
            result = SearchUtil.searchPets(allPets, keyword);
        } else if (!"All".equals(species) || minAge > 0 || maxAge < 30) {
            result = SearchUtil.searchPets(allPets, species, minAge, maxAge, "All");
        } else {
            result = new ArrayList<>(allPets);
        }

        //
        if (availableOnly) {
            result.removeIf(p -> !p.isAvailable());
        }

        //
        result.sort((p1, p2) -> {
            switch (sortBy) {
                case "Name":
                    return p1.getName().compareToIgnoreCase(p2.getName());
                case "Age":
                    return Integer.compare(p1.getAge(), p2.getAge());
                case "Species":
                    String s1 = (p1 instanceof Dog) ? "Dog" : "Cat";
                    String s2 = (p2 instanceof Dog) ? "Dog" : "Cat";
                    return s1.compareTo(s2);
                default:
                    return p1.getPetId().compareTo(p2.getPetId());
            }
        });

        refreshPetTable(result);
        updateStatus("Search completed. Found " + result.size() + " pets.");
    }

    private void resetSearch() {
        keywordField.setText("");
        speciesCombo.setSelectedIndex(0);
        minAgeSpinner.setValue(0);
        maxAgeSpinner.setValue(20);
        availableOnlyCheck.setSelected(true);
        sortCombo.setSelectedIndex(0);
        refreshPetTable(petManager.getAllPets());
        updateStatus("Search reset. Showing all pets.");
    }

    private void refreshPetTable(List<Pet> pets) {
        petTableModel.setRowCount(0);
        for (Pet p : pets) {
            String species = (p instanceof Dog) ? "Dog" : "Cat";
            petTableModel.addRow(new Object[]{
                    p.getPetId(), p.getName(), species, p.getAge(),
                    p.getSize(), p.getHealthStatus(), p.isAvailable() ? "Yes" : "No"
            });
        }
    }

    private void viewPetDetails() {
        int selectedRow = petTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a pet first.");
            return;
        }
        String petId = (String) petTableModel.getValueAt(selectedRow, 0);
        try {
            Pet pet = petManager.findByIdWithException(petId);
            showPetDetailDialog(pet);
        } catch (PetNotFoundException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showPetDetailDialog(Pet pet) {
        JDialog dialog = new JDialog(this, "Pet Details", true);
        dialog.setLayout(new BorderLayout(10, 10));

        JTextArea detailArea = new JTextArea();
        detailArea.setEditable(false);
        detailArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        StringBuilder sb = new StringBuilder();
        sb.append("ID: ").append(pet.getPetId()).append("\n");
        sb.append("Name: ").append(pet.getName()).append("\n");
        sb.append("Species: ").append(pet instanceof Dog ? "Dog" : "Cat").append("\n");
        if (pet instanceof Dog) {
            sb.append("Breed: ").append(((Dog) pet).getBreed()).append("\n");
        } else if (pet instanceof Cat) {
            sb.append("Indoor Only: ").append(((Cat) pet).isIndoorOnly() ? "Yes" : "No").append("\n");
        }
        sb.append("Age: ").append(pet.getAge()).append("\n");
        sb.append("Size: ").append(pet.getSize()).append("\n");
        sb.append("Health: ").append(pet.getHealthStatus()).append("\n");
        sb.append("Available: ").append(pet.isAvailable() ? "Yes" : "No").append("\n");
        sb.append("\nDescription:\n").append(pet.getDescription()).append("\n");
        sb.append("\nAdoption Requirements:\n").append(pet.getAdoptionRequirements()).append("\n");
        sb.append("\nSound: ").append(pet.makeSound()).append("\n");
        detailArea.setText(sb.toString());

        JScrollPane scrollPane = new JScrollPane(detailArea);
        scrollPane.setPreferredSize(new Dimension(400, 300));
        dialog.add(scrollPane, BorderLayout.CENTER);

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dialog.dispose());
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(closeButton);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void applyForAdoption() {
        int selectedRow = petTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a pet first.");
            return;
        }
        String petId = (String) petTableModel.getValueAt(selectedRow, 0);
        try {
            Pet pet = petManager.findByIdWithException(petId);
            if (!pet.isAvailable()) {
                JOptionPane.showMessageDialog(this, "This pet is no longer available.",
                        "Cannot Apply", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Adopter currentAdopter = (Adopter) SessionContext.getCurrentUser();

            // Create application dialog
            JDialog appDialog = new JDialog(this, "Submit Adoption Application", true);
            appDialog.setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.fill = GridBagConstraints.HORIZONTAL;

            gbc.gridx = 0;
            gbc.gridy = 0;
            appDialog.add(new JLabel("Pet:"), gbc);
            gbc.gridx = 1;
            appDialog.add(new JLabel(pet.getName() + " (" + petId + ")"), gbc);

            gbc.gridx = 0;
            gbc.gridy = 1;
            appDialog.add(new JLabel("Your Name:"), gbc);
            gbc.gridx = 1;
            JTextField nameField = new JTextField(currentAdopter.getName(), 20);
            appDialog.add(nameField, gbc);

            gbc.gridx = 0;
            gbc.gridy = 2;
            appDialog.add(new JLabel("Contact Info:"), gbc);
            gbc.gridx = 1;
            JTextField contactField = new JTextField(currentAdopter.getContactInfo(), 20);
            appDialog.add(contactField, gbc);

            gbc.gridx = 0;
            gbc.gridy = 3;
            appDialog.add(new JLabel("Home Environment:"), gbc);
            gbc.gridx = 1;
            JTextArea envArea = new JTextArea(currentAdopter.getHomeEnvironment(), 4, 20);
            envArea.setLineWrap(true);
            JScrollPane envScroll = new JScrollPane(envArea);
            appDialog.add(envScroll, gbc);

            gbc.gridx = 0;
            gbc.gridy = 4;
            gbc.gridwidth = 2;
            JButton submitButton = new JButton("Submit Application");
            submitButton.addActionListener(ev -> {
                String appId = "APP" + System.currentTimeMillis();
                AdoptionApplication app = new AdoptionApplication(
                        appId, petId, currentAdopter.getUserId(),
                        nameField.getText().trim(), contactField.getText().trim(),
                        envArea.getText().trim()
                );
                appManager.add(app);
                updateStatus("Application submitted successfully. ID: " + appId);
                appDialog.dispose();
                JOptionPane.showMessageDialog(this, "Application submitted! ID: " + appId);
            });
            appDialog.add(submitButton, gbc);

            appDialog.pack();
            appDialog.setLocationRelativeTo(this);
            appDialog.setVisible(true);

        } catch (PetNotFoundException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addToFavorites() {
        int selectedRow = petTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a pet first.");
            return;
        }
        String petId = (String) petTableModel.getValueAt(selectedRow, 0);
        Adopter adopter = (Adopter) SessionContext.getCurrentUser();
        adopter.addFavorite(petId);
        //
        util.FileHandler.saveFavorites(adopterMap);
        updateStatus("Added pet " + petId + " to favorites.");
        JOptionPane.showMessageDialog(this, "Added to favorites!");
    }

    // ---------- Adopter panel ----------
    private JPanel createAdopterPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(new Color(255, 245, 250));
        JTabbedPane adopterTab = new JTabbedPane();

        //
        JPanel appPanel = new JPanel(new BorderLayout());
        String[] appColumns = {"App ID", "Pet ID", "Status", "Submit Date", "Review Date"};
        DefaultTableModel appModel = new DefaultTableModel(appColumns, 0);
        JTable myAppTable = new JTable(appModel);
        appPanel.add(new JScrollPane(myAppTable), BorderLayout.CENTER);

        JButton refreshAppButton = new JButton("Refresh");
        refreshAppButton.addActionListener(e -> {
            appModel.setRowCount(0);
            Adopter adopter = (Adopter) SessionContext.getCurrentUser();
            List<AdoptionApplication> myApps = appManager.getApplicationsByAdopter(adopter.getUserId());
            for (AdoptionApplication app : myApps) {
                appModel.addRow(new Object[]{
                        app.getApplicationId(), app.getPetId(), app.getStatus().name(),
                        app.getFormattedSubmitTime(), app.getFormattedReviewTime()
                });
            }
        });
        appPanel.add(refreshAppButton, BorderLayout.SOUTH);

        // Favorites list
         JPanel favPanel = new JPanel(new BorderLayout());
         favPanel.setBackground(new Color(255, 250, 252));
        String[] favColumns = {"Pet ID", "Name", "Status"};
        DefaultTableModel favModel = new DefaultTableModel(favColumns, 0);
        JTable favTable = new JTable(favModel);
        favPanel.add(new JScrollPane(favTable), BorderLayout.CENTER);

        JPanel favButtonPanel = new JPanel();
        JButton refreshFavButton = new JButton("Refresh");
        JButton removeFavButton = new JButton("Remove Selected");
        favButtonPanel.add(refreshFavButton);
        favButtonPanel.add(removeFavButton);
        favPanel.add(favButtonPanel, BorderLayout.SOUTH);

        refreshFavButton.addActionListener(e -> {
            favModel.setRowCount(0);
            Adopter adopter = (Adopter) SessionContext.getCurrentUser();
            for (String petId : adopter.getFavoritePetIds()) {
                Pet p = petManager.findById(petId);
                if (p != null) {
                    favModel.addRow(new Object[]{petId, p.getName(), p.isAvailable() ? "Available" : "Adopted"});
                }
            }
        });

        removeFavButton.addActionListener(e -> {
            int row = favTable.getSelectedRow();
            if (row != -1) {
                String petId = (String) favModel.getValueAt(row, 0);
                Adopter adopter = (Adopter) SessionContext.getCurrentUser();
                adopter.removeFavorite(petId);
                //
                util.FileHandler.saveFavorites(adopterMap);
                refreshFavButton.doClick();
                updateStatus("Removed " + petId + " from favorites.");
            }
        });

        adopterTab.addTab("My Applications", appPanel);
        adopterTab.addTab("Favorites", favPanel);
         panel.add(adopterTab, BorderLayout.CENTER);

        // Initial load
        refreshAppButton.doClick();
        refreshFavButton.doClick();

        return panel;
    }

    // ---------- Staff  ----------
    private JPanel createStaffPetPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(new Color(255, 245, 250));

        String[] columns = {"ID", "Name", "Species", "Age", "Available"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        JTable table = new JTable(model);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

         JPanel buttonPanel = new JPanel();
         buttonPanel.setBackground(new Color(255, 250, 252));
        JButton addButton = new JButton("Add Pet");
        JButton editButton = new JButton("Edit Pet");
        JButton deleteButton = new JButton("Delete Pet");
        JButton refreshButton = new JButton("Refresh");
        JButton makeAvailableButton = new JButton("Mark as Available");  //

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(makeAvailableButton);  //
        buttonPanel.add(refreshButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        //
        Runnable refresh = () -> {
            model.setRowCount(0);
            for (Pet p : petManager.getAllPets()) {
                String species = (p instanceof Dog) ? "Dog" : "Cat";
                model.addRow(new Object[]{p.getPetId(), p.getName(), species, p.getAge(), p.isAvailable()});
            }
        };
        refresh.run();

        refreshButton.addActionListener(e -> refresh.run());

        //
        makeAvailableButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select a pet first.");
                return;
            }
            String petId = (String) model.getValueAt(selectedRow, 0);
            Pet pet = petManager.findById(petId);
            if (pet == null) {
                JOptionPane.showMessageDialog(this, "Pet not found.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (pet.isAvailable()) {
                JOptionPane.showMessageDialog(this, "Pet is already available.");
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Mark pet " + petId + " (" + pet.getName() + ") as available again?",
                    "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                pet.setAvailable(true);
                petManager.update(pet);
                FileHandler.savePets(petManager.getAllPets());
                refresh.run();
                updateStatus("Pet " + petId + " is now available for adoption.");
            }
        });

        addButton.addActionListener(e -> {
            showPetEditDialog(null);
            refresh.run();
        });

        editButton.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1) {
                String petId = (String) model.getValueAt(row, 0);
                Pet pet = petManager.findById(petId);
                showPetEditDialog(pet);
                refresh.run();
            }
        });

        deleteButton.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1) {
                String petId = (String) model.getValueAt(row, 0);
                int confirm = JOptionPane.showConfirmDialog(this, "Delete pet " + petId + "?",
                        "Confirm", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    petManager.remove(petId);
                    refresh.run();
                    updateStatus("Pet " + petId + " deleted.");
                }
            }
        });

        return panel;
    }

    private void showPetEditDialog(Pet existingPet) {
        JDialog dialog = new JDialog(this, existingPet == null ? "Add New Pet" : "Edit Pet", true);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        gbc.gridx = 0;
        gbc.gridy = row;
        dialog.add(new JLabel("Pet ID:"), gbc);
        gbc.gridx = 1;
        JTextField idField = new JTextField(15);
        if (existingPet != null) {
            idField.setText(existingPet.getPetId());
            idField.setEditable(false);
        }
        dialog.add(idField, gbc);

        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        dialog.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1;
        JTextField nameField = new JTextField(existingPet != null ? existingPet.getName() : "", 15);
        dialog.add(nameField, gbc);

        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        dialog.add(new JLabel("Species:"), gbc);
        gbc.gridx = 1;
        JComboBox<String> speciesCombo = new JComboBox<>(new String[]{"Dog", "Cat"});
        if (existingPet instanceof Dog) speciesCombo.setSelectedItem("Dog");
        else if (existingPet instanceof Cat) speciesCombo.setSelectedItem("Cat");
        dialog.add(speciesCombo, gbc);

        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        dialog.add(new JLabel("Age:"), gbc);
        gbc.gridx = 1;
        JSpinner ageSpinner = new JSpinner(new SpinnerNumberModel(
                existingPet != null ? existingPet.getAge() : 1, 0, 30, 1));
        dialog.add(ageSpinner, gbc);

        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        dialog.add(new JLabel("Size:"), gbc);
        gbc.gridx = 1;
        JComboBox<String> sizeCombo = new JComboBox<>(new String[]{"Small", "Medium", "Large"});
        if (existingPet != null) sizeCombo.setSelectedItem(existingPet.getSize());
        dialog.add(sizeCombo, gbc);

        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        dialog.add(new JLabel("Health:"), gbc);
        gbc.gridx = 1;
        JTextField healthField = new JTextField(existingPet != null ? existingPet.getHealthStatus() : "Healthy", 15);
        dialog.add(healthField, gbc);

        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        dialog.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1;
        JTextArea descArea = new JTextArea(existingPet != null ? existingPet.getDescription() : "", 3, 15);
        descArea.setLineWrap(true);
        dialog.add(new JScrollPane(descArea), gbc);

        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        dialog.add(new JLabel("Available:"), gbc);
        gbc.gridx = 1;
        JCheckBox availCheck = new JCheckBox();
        availCheck.setSelected(existingPet == null || existingPet.isAvailable());
        dialog.add(availCheck, gbc);

        //
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        dialog.add(new JLabel("Breed (Dog) / Indoor Only (Cat):"), gbc);
        gbc.gridx = 1;
        JPanel specificPanel = new JPanel(new CardLayout());
        JTextField breedField = new JTextField(15);
        JCheckBox indoorCheck = new JCheckBox("Indoor Only");
        specificPanel.add(breedField, "Dog");
        specificPanel.add(indoorCheck, "Cat");
        CardLayout cl = (CardLayout) specificPanel.getLayout();
        cl.show(specificPanel, (String) speciesCombo.getSelectedItem());
        speciesCombo.addActionListener(ev -> cl.show(specificPanel, (String) speciesCombo.getSelectedItem()));

        if (existingPet instanceof Dog) {
            breedField.setText(((Dog) existingPet).getBreed());
        } else if (existingPet instanceof Cat) {
            indoorCheck.setSelected(((Cat) existingPet).isIndoorOnly());
        }
        dialog.add(specificPanel, gbc);

        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(ev -> {
            String id = idField.getText().trim();
            if (id.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Pet ID cannot be empty.");
                return;
            }
            String name = nameField.getText().trim();
            int age = (int) ageSpinner.getValue();
            String size = (String) sizeCombo.getSelectedItem();
            String health = healthField.getText().trim();
            String desc = descArea.getText().trim();
            boolean available = availCheck.isSelected();

            Pet pet;
            if ("Dog".equals(speciesCombo.getSelectedItem())) {
                pet = new Dog(id, name, age, size, health, desc, available, breedField.getText().trim());
            } else {
                pet = new Cat(id, name, age, size, health, desc, available, indoorCheck.isSelected());
            }

            if (existingPet == null) {
                petManager.add(pet);
            } else {
                petManager.update(pet);
            }
            updateStatus("Pet " + id + " saved.");
            dialog.dispose();
        });
        dialog.add(saveButton, gbc);

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    // ---------- Staff application review panel ----------
    private JPanel createStaffAppPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(new Color(255, 245, 250));

        String[] columns = {"App ID", "Pet ID", "Applicant", "Status", "Submit Date"};
        appTableModel = new DefaultTableModel(columns, 0);
        appTable = new JTable(appTableModel);
        panel.add(new JScrollPane(appTable), BorderLayout.CENTER);

         JPanel buttonPanel = new JPanel();
         buttonPanel.setBackground(new Color(255, 250, 252));
        JButton approveButton = new JButton("Approve");
        JButton rejectButton = new JButton("Reject");
        JButton refreshButton = new JButton("Refresh");
        buttonPanel.add(approveButton);
        buttonPanel.add(rejectButton);
        buttonPanel.add(refreshButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        Runnable refreshApps = () -> {
            appTableModel.setRowCount(0);
            for (AdoptionApplication app : appManager.getPendingApplications()) {
                appTableModel.addRow(new Object[]{
                        app.getApplicationId(), app.getPetId(), app.getApplicantName(),
                        app.getStatus().name(), app.getFormattedSubmitTime()
                });
            }
        };
        refreshApps.run();
        refreshButton.addActionListener(e -> refreshApps.run());

        approveButton.addActionListener(e -> {
            int row = appTable.getSelectedRow();
            if (row != -1) {
                String appId = (String) appTableModel.getValueAt(row, 0);
                try {
                    // Check pet is still available
                    AdoptionApplication app = appManager.findById(appId);
                    Pet pet = petManager.findByIdWithException(app.getPetId());
                    if (!pet.isAvailable()) {
                        throw new InvalidApplicationStatusException("Pet is no longer available.");
                    }

                    appManager.approveApplication(appId, SessionContext.getCurrentUser().getUserId());
                    pet.setAvailable(false);
                    petManager.update(pet);

                    updateStatus("Application " + appId + " approved. Pet " + pet.getPetId() + " marked as adopted.");
                    refreshApps.run();
                    JOptionPane.showMessageDialog(this, "Application approved!");
                } catch (PetNotFoundException | InvalidApplicationStatusException ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        rejectButton.addActionListener(e -> {
            int row = appTable.getSelectedRow();
            if (row != -1) {
                String appId = (String) appTableModel.getValueAt(row, 0);
                String reason = JOptionPane.showInputDialog(this, "Enter rejection reason:");
                if (reason != null) {
                    try {
                        appManager.rejectApplication(appId, SessionContext.getCurrentUser().getUserId(), reason);
                        updateStatus("Application " + appId + " rejected. Reason: " + reason);
                        refreshApps.run();
                    } catch (InvalidApplicationStatusException ex) {
                        JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        return panel;
    }

    // ---------- Analytics panel ----------
    private JPanel createAnalyticsPanel() {
        // Build an analytics panel that contains both a textual report and a visual chart.
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(new Color(255, 245, 250));

        // Text area for textual analytics report (left side)
        JTextArea reportArea = new JTextArea();
        reportArea.setEditable(false);
        reportArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        // Lightweight chart panel (right side) - uses our BasicChartPanel implementation
        BasicChartPanel chartPanel = new BasicChartPanel();

        // Split pane to show text and chart side by side
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(reportArea), chartPanel);
        split.setDividerLocation(480);
        panel.add(split, BorderLayout.CENTER);

        // Control buttons at the bottom
        JPanel controlPanel = new JPanel();
        JButton generateButton = new JButton("Generate Report");
        JButton dateRangeButton = new JButton("Date Range Report");
        JButton toggleChartButton = new JButton("Toggle Chart");
        JButton refreshReportButton = new JButton("Refresh");

        controlPanel.add(generateButton);
        controlPanel.add(dateRangeButton);
        controlPanel.add(toggleChartButton);
        controlPanel.add(refreshReportButton);
        panel.add(controlPanel, BorderLayout.SOUTH);

        // When Generate is clicked, produce the textual report and update the chart with species counts
        generateButton.addActionListener(e -> {
            String report = AnalyticsService.generateAnalyticsReport(
                    appManager.getAllApplications(), petManager.getAllPets());
            reportArea.setText(report);

            // Update chart dataset (species counts)
            java.util.Map<String, Long> species = AnalyticsService.getSpeciesCountMap(
                    appManager.getAllApplications(), petManager.getAllPets());
            if (species.isEmpty()) {
                // if no adoption data, show available pets counts instead
                long dogs = petManager.getAllPets().stream().filter(p -> p instanceof model.Dog && p.isAvailable()).count();
                long cats = petManager.getAllPets().stream().filter(p -> p instanceof model.Cat && p.isAvailable()).count();
                Map<String, Long> fallback = new LinkedHashMap<>();
                fallback.put("Available Dogs", dogs);
                fallback.put("Available Cats", cats);
                chartPanel.setStringData(fallback);
                chartPanel.setTitle("Available Pets by Species");
            } else {
                chartPanel.setStringData(species);
                chartPanel.setTitle("Adoptions by Species");
            }
            chartPanel.setMode(BasicChartPanel.Mode.PIE);
        });

        // Date range report: show last 7 days textual report and bar chart of daily adoptions
        dateRangeButton.addActionListener(e -> {
            LocalDate end = LocalDate.now();
            LocalDate start = end.minusDays(7);
            String report = AnalyticsService.generateDateRangeReport(
                    appManager.getAllApplications(), start, end);
            reportArea.setText(report);

            Map<LocalDate, Long> daily = AnalyticsService.getDailyAdoptions(
                    appManager.getAllApplications(), 7);
            chartPanel.setDateData(daily);
            chartPanel.setTitle("Daily Adoptions (last 7 days)");
            chartPanel.setMode(BasicChartPanel.Mode.BAR);
        });

        // Toggle between bar and pie chart for the current dataset
        // Simplified toggle logic: switch to the opposite of the current mode.
        toggleChartButton.addActionListener(e -> {
            if (chartPanel != null) {
                BasicChartPanel.Mode current = chartPanel.getMode();
                BasicChartPanel.Mode newMode = (current == BasicChartPanel.Mode.PIE) ? BasicChartPanel.Mode.BAR : BasicChartPanel.Mode.PIE;
                chartPanel.setMode(newMode);
            }
        });

        refreshReportButton.addActionListener(e -> {
            generateButton.doClick(); // regenerate report and chart
            updateStatus("Analytics report refreshed.");
        });

        // Generate by default on panel creation
        generateButton.doClick();

        return panel;
    }
}

