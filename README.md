# Community Pet Adoption Center System

A Java Swing-based desktop application for managing pet adoptions.
The system connects animal shelters with potential adopters and supports full CRUD operations, role-based access control, file-based persistence, and analytics reporting.

---

## Features

### Common Features

* Browse adoptable pets with detailed information
* Search and filter pets by species, age, and keywords
* Sort pets using different criteria
* View adoption requirements and pet details

### Adopter Features

* Submit adoption applications
* Manage favorite pets
* Track application status:

  * Pending
  * Approved
  * Rejected

### Shelter Staff Features

* Add, edit, and delete pet records
* Review adoption applications
* Approve or reject applications
* Generate analytics reports and statistics
* Mark returned pets as available again

---

## Object-Oriented Design Highlights

| Concept            | Implementation                                              |
| ------------------ | ----------------------------------------------------------- |
| Interface          | `Manageable<T>` defines CRUD operations                     |
| Abstract Classes   | `User`, `Pet`                                               |
| Inheritance        | `Adopter`, `Staff`, `Dog`, `Cat`                            |
| Polymorphism       | `ArrayList<Pet>` stores different pet types                 |
| Method Overloading | Multiple `searchPets()` implementations                     |
| Method Overriding  | Overridden behaviors in subclasses                          |
| Custom Exceptions  | `PetNotFoundException`, `InvalidApplicationStatusException` |
| File I/O           | Persistent storage using text files                         |
| Collections        | `ArrayList`, `HashMap`, `LinkedHashMap`                     |

---

## Technology Stack

* Java 8+
* Java Swing
* File-based persistence
* `java.time` API

---

## Project Structure

```text
src/
data/
README.md
```

### Data Files

The application automatically creates and maintains files inside the `data/` directory:

* `pets.txt`
* `users.txt`
* `applications.txt`
* `favorites.txt`

---

## How to Run

1. Clone or download this repository
2. Open the project in IntelliJ IDEA (or another Java IDE)
3. Mark the `src` folder as Sources Root
4. Set the working directory to the project root
5. Run `main.Main`

---

## Default Accounts

| Role    | User ID    | Password |
| ------- | ---------- | -------- |
| Staff   | `staff1`   | `123`    |
| Adopter | `adopter1` | `123`    |

---

## Key Functionalities

### CRUD Operations

* Full pet management
* Application management

### Search & Sorting

* Search by keyword
* Multi-criteria filtering
* Sorting by:

  * ID
  * Name
  * Age
  * Species

### Analytics

* Adoption statistics
* Pending application reports
* Species distribution analysis

---

## Persistence

All application data is stored locally and persists across program restarts.

---

## Notes

This project was developed as an object-oriented programming practice project focusing on:

* OOP principles
* GUI development
* File persistence
* Java collections
* Exception handling
* Software structure and maintainability

---

## License

This project is for educational purposes.
