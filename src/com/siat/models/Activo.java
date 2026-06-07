package com.siat.models;

public class Activo {
    private int assetId;
    private String sku;
    private String name;
    private String category;
    private String location;
    private String status;
    private String lifecycleStage;
    private String purchaseDate;
    private String decommissionDate;
    private int lifecycleDays;
    private String maintenance;
    private int quantity;
    private String warrantyExpiry;
    private double cost;
    private int createdBy;
    private int responsibleId;
    private int unresolvedAlerts;

    public int getAssetId() { return assetId; }
    public void setAssetId(int assetId) { this.assetId = assetId; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getLifecycleStage() { return lifecycleStage; }
    public void setLifecycleStage(String lifecycleStage) { this.lifecycleStage = lifecycleStage; }

    public String getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(String purchaseDate) { this.purchaseDate = purchaseDate; }

    public String getDecommissionDate() { return decommissionDate; }
    public void setDecommissionDate(String decommissionDate) { this.decommissionDate = decommissionDate; }

    public int getLifecycleDays() { return lifecycleDays; }
    public void setLifecycleDays(int lifecycleDays) { this.lifecycleDays = lifecycleDays; }

    public String getMaintenance() { return maintenance; }
    public void setMaintenance(String maintenance) { this.maintenance = maintenance; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getWarrantyExpiry() { return warrantyExpiry; }
    public void setWarrantyExpiry(String warrantyExpiry) { this.warrantyExpiry = warrantyExpiry; }

    public double getCost() { return cost; }
    public void setCost(double cost) { this.cost = cost; }

    public int getCreatedBy() { return createdBy; }
    public void setCreatedBy(int createdBy) { this.createdBy = createdBy; }

    public int getResponsibleId() { return responsibleId; }
    public void setResponsibleId(int responsibleId) { this.responsibleId = responsibleId; }

    public int getUnresolvedAlerts() { return unresolvedAlerts; }
    public void setUnresolvedAlerts(int unresolvedAlerts) { this.unresolvedAlerts = unresolvedAlerts; }
}
