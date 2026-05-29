# Community Pet Adoption Center System

A Java Swing-based desktop application for managing pet adoptions, designed as the final project for the Object-Oriented Programming course. The system connects animal shelters with potential adopters, supporting full CRUD operations, role-based access control, file-based persistence, and analytics reporting.

## Features

### Common Features (All Users)
- **Browse Pets**: View a list of all adoptable pets with details such as species, age, size, health status, and availability.
- **Search & Filter**: Search by keyword, filter by species and age range, sort by different criteria.
- **View Pet Details**: See complete pet information including adoption requirements and a fun "sound" demonstration.

### Adopter Features
- **Submit Adoption Application**: Apply to adopt a selected pet by providing contact information and home environment description.
- **Manage Favorites**: Add or remove pets from a personal favorites list.
- **Track Applications**: View the status of submitted applications (Pending, Approved, Rejected).

### Shelter Staff Features
- **Pet Management**: Add, edit, delete, and mark pets as available again after adoption returns.
- **Application Review**: Approve or reject pending adoption applications with optional rejection reasons.
- **Analytics Dashboard**: Generate statistical reports showing total adoptions, daily/monthly counts, species breakdowns, and pending application counts.

## Object-Oriented Design Highlights

The project strictly follows OOP principles as required:

| Concept | Implementation |
|--------|---------------|
| **Interface** | `Manageable<T>` �C defines standard CRUD operations implemented by `PetDataManager` and `ApplicationManager`. |
| **Abstract Classes** | `User` (with subclasses `Adopter`, `Staff`); `Pet` (with subclasses `Dog`, `Cat`). |
| **Inheritance** | Two distinct hierarchies: User hierarchy and Pet hierarchy. |
| **Polymorphism** | `ArrayList<Pet>` stores both `Dog` and `Cat` objects; `makeSound()` and `getAdoptionRequirements()` are dispatched dynamically. |
| **Method Overloading** | `SearchUtil.searchPets()` provides three overloaded versions for different search criteria. |
| **Method Overriding** | Subclasses override `makeSound()`, `getAdoptionRequirements()`, `hasPermission()`, etc. |
| **Custom Exceptions** | `PetNotFoundException`, `InvalidApplicationStatusException`. |
| **File I/O** | Data persisted to text files in the `data/` directory with atomic write operations. |
| **Collections** | `ArrayList`, `HashMap`, `LinkedHashMap` for efficient data management and sorting. |

## Technology Stack

- **Language**: Java 8+
- **GUI Framework**: Java Swing
- **Data Persistence**: Plain text files (CSV-like format with escaping)
- **Date/Time API**: `java.time.LocalDateTime`




## How to Run

1. **Clone or download** the project source code.
2. Open the project in **IntelliJ IDEA** (or any Java IDE).
3. Ensure the `src` folder is marked as **Sources Root**.
4. Set the **working directory** to the project root (in IntelliJ: `Run �� Edit Configurations �� Working directory �� $MODULE_WORKING_DIR$`).
5. Run the `main.Main` class.
6. Log in using one of the default accounts below.

## Default Accounts

| Role     | User ID   | Password |
|----------|-----------|----------|
| Staff    | `staff1`  | `123`    |
| Adopter  | `adopter1`| `123`    |

> Additional accounts can be created by editing `data/users.txt` or extending the registration functionality.

## Data Files

All data is stored in the `data/` folder (created automatically at first run):

- `pets.txt` �C Pet inventory with type identifier (`DOG` or `CAT`) and attributes.
- `users.txt` �C User profiles with role identifier (`ADOPTER` or `STAFF`).
- `applications.txt` �C Adoption application records including status and timestamps.
- `favorites.txt` �C Mapping of adopter IDs to favorite pet IDs.

## Key Operations Demonstrated

- **CRUD**: Fully implemented for pets and applications.
- **Sorting**: Sort pets by ID, name, age, or species in the browse panel.
- **Search**: Keyword search and multi-criteria filtering using overloaded methods.
- **Analytics**: Real-time statistics with date range reports.

## Demo Video Requirements

A short demo video (��5 minutes) should showcase:
- Login process with both roles.
- Adopter: browsing, applying, adding favorites, checking application status.
- Staff: adding/editing pets, reviewing applications, generating reports, marking pets available.
- Data persistence across restarts.

## Author

**Name**: Qin Zijun 
**Student ID**: 2530034055 
**Course**: Object-Oriented Programming Final Project  
**Date**: May 2026

---

*This project satisfies all requirements outlined in the OOPProjectRequirements.pdf document.*