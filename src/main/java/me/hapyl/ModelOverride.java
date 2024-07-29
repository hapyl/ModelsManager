package me.hapyl;

public class ModelOverride {

    private final Model parent;

    private int customModelData;
    private String model;

    ModelOverride(Model parent, int customModelData, String model) {
        this.parent = parent;
        this.customModelData = customModelData;
        this.model = model;
    }

    public int getCustomModelData() {
        return customModelData;
    }

    public void setCustomModelData(int customModelData) {
        this.customModelData = customModelData;
        this.parent.modify();
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
        this.parent.modify();
    }
}
