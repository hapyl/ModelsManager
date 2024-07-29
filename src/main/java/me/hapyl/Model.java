package me.hapyl;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.*;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Model {

    private final File file;
    private final String name;

    private final String parent;
    private final String texture;
    private final List<ModelOverride> overrides;

    private boolean modified;

    private Model(@Nonnull File file, @Nonnull JsonElement json) {
        this.file = file;
        this.name = file.getName().replace(".json", "");

        final JsonObject parent = json.getAsJsonObject();

        // Read members
        this.parent = parent.get("parent").getAsString();
        this.texture = parent.get("textures").getAsJsonObject().get("layer0").getAsString();

        this.overrides = new ArrayList<>();

        final JsonArray overrides = parent.get("overrides").getAsJsonArray();

        for (JsonElement override : overrides) {
            final JsonObject overrideObject = override.getAsJsonObject();
            final JsonObject predicateObject = overrideObject.get("predicate").getAsJsonObject();

            this.overrides.add(new ModelOverride(
                    this,
                    predicateObject.get("custom_model_data").getAsInt(),
                    overrideObject.get("model").getAsString()
            ));
        }
    }

    public void addOverride(int customModelData, String model) {
        this.overrides.add(new ModelOverride(this, customModelData, model));
        modify();
    }

    @Nonnull
    public String getName() {
        return name;
    }

    @Nonnull
    public String getParent() {
        return parent;
    }

    @Nonnull
    public String getTexture() {
        return texture;
    }

    @Nonnull
    public List<ModelOverride> getOverrides() {
        return overrides;
    }

    public void modify() {
        modified = true;
    }

    public boolean isModified() {
        return modified;
    }

    public void save() {
        modified = false;

        // Save the file
        try (final FileWriter writer = new FileWriter(file)) {
            final JsonObject parent = new JsonObject();

            parent.add("parent", new JsonPrimitive(this.parent));
            parent.add("textures", jsonObject("layer0", this.texture));

            final JsonArray overrides = new JsonArray();

            this.overrides.sort(Comparator.comparingInt(ModelOverride::getCustomModelData));

            for (ModelOverride override : this.overrides) {
                final JsonObject jOverride = new JsonObject();
                final JsonObject jPredicate = new JsonObject();

                jPredicate.addProperty("custom_model_data", override.getCustomModelData());

                jOverride.add("predicate", jPredicate);
                jOverride.add("model", new JsonPrimitive(override.getModel()));

                overrides.add(jOverride);
            }

            parent.add("overrides", overrides);

            Manager.GSON.toJson(parent, writer);
            Main.jInfo("Saved!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return name;
    }

    @Nullable
    public static Model load(@Nonnull File file) {
        final String fileName = file.getName();

        if (!fileName.endsWith(".json")) {
            Manager.LOGGER.warning("Invalid model file, skipping... (%s)".formatted(fileName));
            return null;
        }

        // Parse json
        try (final FileReader reader = new FileReader(file)) {
            final JsonObject object = Manager.GSON.fromJson(reader, JsonObject.class);

            return new Model(file, object);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static JsonObject jsonObject(String name, String value) {
        final JsonObject object = new JsonObject();
        object.addProperty(name, value);

        return object;
    }
}
