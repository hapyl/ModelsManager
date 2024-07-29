package me.hapyl;

import javax.annotation.Nonnull;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PanelOverride extends JPanel {

    private final Model model;
    private final JTabbedPane overrides;

    public PanelOverride(Model model) {
        this.model = model;

        final JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        panel.add(new JLabel("Model: " + model.getName()));
        panel.add(Box.createVerticalStrut(10));

        // Immutable data
        panel.add(new JLabel("Parent: " + model.getParent()));
        panel.add(Box.createVerticalStrut(10));

        panel.add(new JLabel("Texture: " + model.getTexture()));
        panel.add(Box.createVerticalStrut(10));

        // Mutable data
        this.overrides = new JTabbedPane();
        this.overrides.setPreferredSize(new Dimension(600, 700));

        final InputMap inputMap = this.overrides.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        final ActionMap actionMap = this.overrides.getActionMap();

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK), "save");
        actionMap.put("save", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!model.isModified()) {
                    return;
                }

                model.save();
            }
        });

        updateFolders();

        add(panel);
        add(this.overrides);

        add(new Button("New Tab") {
            @Override
            public void onClick(@Nonnull MouseEvent ev) {
                int max = 0;

                final List<ModelOverride> overrides = PanelOverride.this.model.getOverrides();

                for (ModelOverride override : overrides) {
                    max = Math.max(max, override.getCustomModelData());
                }

                PanelOverride.this.model.addOverride((max / 1000 + 1) * 1000, "dev/null");
                updateFolders();
            }
        });

        add(new Button("Add Predicate") {
            @Override
            public void onClick(@Nonnull MouseEvent ev) {
                final String title = overrides.getTitleAt(overrides.getSelectedIndex());

                final int currentTab;

                try {
                    currentTab = Integer.parseInt(title);
                } catch (NumberFormatException e) {
                    return;
                }

                int max = 0;

                for (ModelOverride override : PanelOverride.this.model.getOverrides()) {
                    final int data = override.getCustomModelData();

                    if (data < currentTab || data >= (currentTab + Main.FOLDER_SIZE)) {
                        continue;
                    }

                    max = Math.max(max, data);
                }

                PanelOverride.this.model.addOverride(max + 1, "dev/null");
                updateFolders();
            }
        });
    }

    public void updateFolders() {
        final int selectedIndex = Math.max(0, overrides.getSelectedIndex());
        final String selectedTitle = overrides.getTabCount() == 0 ? null : overrides.getTitleAt(selectedIndex);

        overrides.removeAll();

        // Sort into folders
        final Map<Integer, List<ModelOverride>> folders = new HashMap<>();

        for (ModelOverride override : model.getOverrides()) {
            final int folder = (int) ((double) override.getCustomModelData() / Main.FOLDER_SIZE);

            folders.compute(folder, (index, list) -> {
                (list = list != null ? list : new ArrayList<>()).add(override);

                return list;
            });
        }

        // Create tabs
        folders.forEach((index, models) -> {
            final JPanel modelPanel = new JPanel();
            modelPanel.setLayout(new FlowLayout());

            models.forEach(mo -> {
                final JPanel panel = new JPanel();
                panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));

                panel.add(new JLabel("custom_model_data "));
                panel.add(new Button(mo.getCustomModelData()) {
                    @Override
                    public void onClick(@Nonnull MouseEvent ev) {
                        final String value = JOptionPane.showInputDialog("New data: ", mo.getCustomModelData());

                        if (value == null) {
                            return;
                        }

                        try {
                            final int intValue = Integer.parseInt(value);

                            if (intValue == mo.getCustomModelData()) {
                                return;
                            }

                            // Make sure we're in the same range
                            final int range = (int) ((double) mo.getCustomModelData() / 1000);
                            final int newRange = (int) ((double) intValue / 1000);

                            if (range != newRange) {
                                final int rangeMl = range * 1000;

                                Main.jError("Value must be within the range! (%s-%s)".formatted(rangeMl, rangeMl + 999));
                                return;
                            }

                            // Check if there is another model with the same value
                            for (ModelOverride m : models) {
                                if (!m.equals(mo) && m.getCustomModelData() == intValue) {
                                    Main.jError("This value is already set for '%s'!".formatted(m.getModel()));
                                    return;
                                }
                            }

                            mo.setCustomModelData(intValue);
                            updateFolders();
                        } catch (NumberFormatException ignored) {
                            Main.jError("Please provide a value integer.");
                        }
                    }
                });

                panel.add(new JLabel("     "));
                panel.add(new JLabel("model "));
                panel.add(new Button(mo.getModel()) {
                    @Override
                    public void onClick(@Nonnull MouseEvent ev) {
                        final String value = JOptionPane.showInputDialog("New model: ", mo.getModel());

                        // User clicked 'cancel'
                        if (value == null) {
                            return;
                        }

                        if (value.isBlank() || value.isEmpty()) {
                            Main.jError("Value cannot be blank or empty!");
                            return;
                        }

                        // If same value don't update the folders
                        if (mo.getModel().equals(value)) {
                            return;
                        }

                        mo.setModel(value);
                        updateFolders();
                    }
                });

                panel.add(new Button(" [x]") {
                    @Override
                    public void onClick(@Nonnull MouseEvent ev) {
                        PanelOverride.this.model.getOverrides().remove(mo);
                        PanelOverride.this.model.modify();

                        updateFolders();
                    }
                });

                modelPanel.add(panel);
            });

            overrides.addTab(String.valueOf(index * 1000), modelPanel);

            // Refocus if updating
            if (selectedTitle != null) {
                for (int i = 0; i < overrides.getTabCount(); i++) {
                    final String title = overrides.getTitleAt(i);

                    if (title.equalsIgnoreCase(selectedTitle)) {
                        overrides.setSelectedIndex(i);
                    }
                }
            }
        });
    }

}
