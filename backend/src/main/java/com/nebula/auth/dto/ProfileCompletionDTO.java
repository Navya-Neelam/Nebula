package com.nebula.auth.dto;

import java.util.List;

public class ProfileCompletionDTO {

    private int completionPercentage;
    private List<String> completedItems;
    private List<String> pendingItems;

    public ProfileCompletionDTO() {
    }

    public ProfileCompletionDTO(int completionPercentage, List<String> completedItems, List<String> pendingItems) {
        this.completionPercentage = completionPercentage;
        this.completedItems = completedItems;
        this.pendingItems = pendingItems;
    }

    public int getCompletionPercentage() {
        return completionPercentage;
    }

    public void setCompletionPercentage(int completionPercentage) {
        this.completionPercentage = completionPercentage;
    }

    public List<String> getCompletedItems() {
        return completedItems;
    }

    public void setCompletedItems(List<String> completedItems) {
        this.completedItems = completedItems;
    }

    public List<String> getPendingItems() {
        return pendingItems;
    }

    public void setPendingItems(List<String> pendingItems) {
        this.pendingItems = pendingItems;
    }
}
