package me.hapyl;

import javax.annotation.Nonnull;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.io.File;

public class Main {

    public static final int FOLDER_SIZE = 1000;

    private static Main main;

    private final Manager manager;

    private final JFrame frame;
    private final JTabbedPane tabbed;
    private final JList<Model> models;

    private Main() {
        this.manager = new Manager();

        this.frame = new JFrame("Model Manager");

        this.frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.frame.setSize(1200, 800);

        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        final Dimension frameSize = frame.getSize();

        frame.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);

        final Container content = this.frame.getContentPane();
        content.setLayout(new BorderLayout());

        this.models = new JList<>();
        this.models.setVisibleRowCount(10);
        this.models.setFixedCellWidth(150);

        this.models.addMouseListener(new MouseListenerClick() {
            @Override
            public void mouseClicked(MouseEvent e) {
                final int clickedIndex = models.locationToIndex(e.getPoint());

                if (clickedIndex < 0) {
                    return;
                }

                final Model model = models.getModel().getElementAt(clickedIndex);
                final String modelName = model.getName();

                // Check if already opened and focus, instead of opening 69 tabs
                for (int i = 0; i < tabbed.getTabCount(); i++) {
                    // .contains because we're changing modified model names
                    if (tabbed.getTitleAt(i).contains(modelName)) {
                        tabbed.setSelectedIndex(i);
                        return;
                    }
                }

                final PanelOverride panel = new PanelOverride(model);

                tabbed.addTab(modelName, panel);
                tabbed.setSelectedComponent(panel);
            }
        });

        final JButton buttonFolder = new JButton("Choose Folder");

        buttonFolder.addActionListener((ev) -> {
            do {
                final JFileChooser fileChooser = new JFileChooser(new File(System.getProperty("user.home"), "Desktop"));
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

                if (fileChooser.showOpenDialog(frame) != JFileChooser.APPROVE_OPTION) {
                    jError("Please select a folder.");
                    continue;
                }

                final File file = fileChooser.getSelectedFile();

                if (file == null) {
                    jError("Please select a valid folder.");
                    continue;
                }

                try {
                    manager.load(file);
                } catch (Exception e) {
                    jError(e.getMessage());
                    continue;
                }

                this.models.setModel(manager.getModelsAsListModel());
                break;
            } while (true);

        });

        this.tabbed = new JTabbedPane();

        // Panels
        final JPanel right = new JPanel(new BorderLayout());
        right.add(buttonFolder, BorderLayout.NORTH);
        right.add(new JScrollPane(models), BorderLayout.CENTER);

        final JPanel left = new JPanel(new BorderLayout());
        left.add(tabbed, BorderLayout.CENTER);

        content.add(right, BorderLayout.EAST);
        content.add(left, BorderLayout.CENTER);

        this.frame.setVisible(true);
    }

    public static void jError(@Nonnull String message) {
        JOptionPane.showMessageDialog(main.frame, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void jInfo(@Nonnull String message) {
        JOptionPane.showMessageDialog(main.frame, message, "Save", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> main = new Main());
    }
}