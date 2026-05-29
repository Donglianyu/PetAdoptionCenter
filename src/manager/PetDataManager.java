package manager;

import model.Pet;
import model.Dog;
import model.Cat;
import exception.PetNotFoundException;
import util.FileHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PetDataManager implements Manageable<Pet> {
    private List<Pet> petList;
    private Map<String, Pet> petIdMap;

    public PetDataManager() {
        petList = new ArrayList<>();
        petIdMap = new HashMap<>();
    }

    @Override
    public boolean add(Pet item) {
        if (petIdMap.containsKey(item.getPetId())) {
            return false; // ID already exists
        }
        petList.add(item);
        petIdMap.put(item.getPetId(), item);
        FileHandler.savePets(petList); //
        return true;
    }

    @Override
    public boolean remove(String id) {
        Pet pet = petIdMap.remove(id);
        if (pet != null) {
            petList.remove(pet);
            FileHandler.savePets(petList);
            return true;
        }
        return false;
    }

    @Override
    public boolean update(Pet item) {
        Pet existing = petIdMap.get(item.getPetId());
        if (existing != null) {
            int index = petList.indexOf(existing);
            petList.set(index, item);
            petIdMap.put(item.getPetId(), item);
            FileHandler.savePets(petList);
            return true;
        }
        return false;
    }

    @Override
    public Pet findById(String id) {
        return petIdMap.get(id);
    }

    public Pet findByIdWithException(String id) throws PetNotFoundException {
        Pet pet = petIdMap.get(id);
        if (pet == null) {
            throw new PetNotFoundException("Pet with ID " + id + " was not found in the system.");
        }
        return pet;
    }

    public List<Pet> getAllPets() {
        return new ArrayList<>(petList);
    }

    public void setPetList(List<Pet> list) {
        this.petList = new ArrayList<>(list);
        petIdMap.clear();
        for (Pet p : list) {
            petIdMap.put(p.getPetId(), p);
        }
    }


    public List<Dog> getAllDogs() {
        List<Dog> dogs = new ArrayList<>();
        for (Pet p : petList) {
            if (p instanceof Dog) {
                dogs.add((Dog) p);
            }
        }
        return dogs;
    }

    public List<Cat> getAllCats() {
        List<Cat> cats = new ArrayList<>();
        for (Pet p : petList) {
            if (p instanceof Cat) {
                cats.add((Cat) p);
            }
        }
        return cats;
    }
}

