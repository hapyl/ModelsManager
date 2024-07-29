package me.hapyl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.annotation.Nonnull;
import javax.lang.model.element.ModuleElement;
import javax.swing.*;
import java.io.File;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.logging.Logger;

public class Manager {

    public static final String MODELS_PATH;
    public static final Logger LOGGER;
    public static final Gson GSON;

    static {
        MODELS_PATH = "\\assets\\minecraft\\models\\item";
        LOGGER = Logger.getLogger("ModelsManager");
        GSON = new GsonBuilder().setPrettyPrinting().create();
    }

    private final LinkedHashSet<Model> models;

    private File modelsFolder;

    Manager() {
        this.models = new LinkedHashSet<>();
    }

    public LinkedHashSet<Model> getModels() {
        return models;
    }

    public void load(@Nonnull File file) throws IllegalArgumentException {
        final String filePath = file.getPath();

        if (!file.isDirectory()) {
            throw new IllegalArgumentException("Provided path is not a folder!");
        }

        final File[] files = file.listFiles();

        if (files == null || files.length == 0) {
            throw new IllegalArgumentException("Provided folder is empty!");
        }

        final File packMeta = Util.findFile(files, "pack.mcmeta");

        if (packMeta == null) {
            throw new IllegalArgumentException("Provided folder is not a resource pack! (Must contain 'pack.mcmeta'!)");
        }

        // Find models folders
        modelsFolder = new File(filePath + MODELS_PATH);

        Util.mkdirs(modelsFolder);

        // Read models
        updateModels(true);
    }

    public void updateModels(boolean clear) {
        Objects.requireNonNull(modelsFolder, "Too soon!");

        if (clear) {
            this.models.clear();
        }

        final File[] files = modelsFolder.listFiles();

        if (files == null) {
            return;
        }

        for (File file : files) {
            // There should not be any folder but maybe mojang
            // will add in the future, just ignore for now
            if (!file.isFile()) {
                continue;
            }

            final Model model = Model.load(file);

            if (model != null) {
                this.models.add(model);
            }
        }
    }

    public ListModel<Model> getModelsAsListModel() {
        final DefaultListModel<Model> list = new DefaultListModel<>();

        for (Model model : this.models) {
            list.addElement(model);
        }

        return list;
    }
}
