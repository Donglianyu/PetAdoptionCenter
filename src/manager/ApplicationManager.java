package manager;

import model.AdoptionApplication;
import model.AdoptionApplication.ApplicationStatus;
import exception.InvalidApplicationStatusException;
import util.FileHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ApplicationManager implements Manageable<AdoptionApplication> {
    private List<AdoptionApplication> applicationList;
    private Map<String, AdoptionApplication> applicationIdMap;

    public ApplicationManager() {
        applicationList = new ArrayList<>();
        applicationIdMap = new HashMap<>();
    }

    @Override
    public boolean add(AdoptionApplication item) {
        if (applicationIdMap.containsKey(item.getApplicationId())) {
            return false;
        }
        applicationList.add(item);
        applicationIdMap.put(item.getApplicationId(), item);
        FileHandler.saveApplications(applicationList);
        return true;
    }

    @Override
    public boolean remove(String id) {
        AdoptionApplication app = applicationIdMap.remove(id);
        if (app != null) {
            applicationList.remove(app);
            FileHandler.saveApplications(applicationList);
            return true;
        }
        return false;
    }

    @Override
    public boolean update(AdoptionApplication item) {
        AdoptionApplication existing = applicationIdMap.get(item.getApplicationId());
        if (existing != null) {
            int index = applicationList.indexOf(existing);
            applicationList.set(index, item);
            applicationIdMap.put(item.getApplicationId(), item);
            FileHandler.saveApplications(applicationList);
            return true;
        }
        return false;
    }

    @Override
    public AdoptionApplication findById(String id) {
        return applicationIdMap.get(id);
    }


    public void approveApplication(String appId, String staffId)
            throws InvalidApplicationStatusException {
        AdoptionApplication app = findById(appId);
        if (app == null) {
            throw new IllegalArgumentException("Application not found.");
        }
        app.updateStatus(ApplicationStatus.APPROVED, staffId, null);
        update(app);
    }

    public void rejectApplication(String appId, String staffId, String reason)
            throws InvalidApplicationStatusException {
        AdoptionApplication app = findById(appId);
        if (app == null) {
            throw new IllegalArgumentException("Application not found.");
        }
        app.updateStatus(ApplicationStatus.REJECTED, staffId, reason);
        update(app);
    }

    public List<AdoptionApplication> getAllApplications() {
        return new ArrayList<>(applicationList);
    }

    public List<AdoptionApplication> getPendingApplications() {
        return applicationList.stream()
                .filter(app -> app.getStatus() == ApplicationStatus.PENDING)
                .collect(Collectors.toList());
    }

    public List<AdoptionApplication> getApplicationsByAdopter(String adopterId) {
        return applicationList.stream()
                .filter(app -> app.getAdopterId().equals(adopterId))
                .collect(Collectors.toList());
    }

    public List<AdoptionApplication> getApplicationsByPet(String petId) {
        return applicationList.stream()
                .filter(app -> app.getPetId().equals(petId))
                .collect(Collectors.toList());
    }

    public void setApplicationList(List<AdoptionApplication> list) {
        this.applicationList = new ArrayList<>(list);
        applicationIdMap.clear();
        for (AdoptionApplication app : list) {
            applicationIdMap.put(app.getApplicationId(), app);
        }
    }
}

