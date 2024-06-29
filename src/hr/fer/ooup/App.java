package hr.fer.ooup;

import hr.fer.ooup.model.TextEditorModel;
import hr.fer.ooup.plugins.Plugin;
import hr.fer.ooup.plugins.PluginFactory;
import hr.fer.ooup.ui.StatusBar;
import hr.fer.ooup.ui.TextEditor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class App extends JFrame {

    private TextEditor editor;
    private Map<String, Action> actions;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            App textEditor = new App();
            textEditor.setVisible(true);
        });
    }

    public App() {
        setTitle("Text editor");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 500);
        setVisible(true);
        initializeGUI();
    }

    private void initializeGUI() {
        setLayout(new BorderLayout());

        TextEditorModel model = new TextEditorModel("Sto je danas\nlijep i suncan dan!\nOna danas\nDolazi iz Zagreba!");
        editor = new TextEditor(model);
        add(editor, BorderLayout.CENTER);
        actions = new HashMap<>();

        ActionInitializer.initializeActions(actions, editor);
        initializeObservers();

        JToolBar toolBar = new JToolBar();
        initializeToolBar(toolBar);
        add(toolBar, BorderLayout.NORTH);

        JMenuBar menuBar = new JMenuBar();
        initializeMenuBar(menuBar);
        loadAvailablePlugins(menuBar);
        setJMenuBar(menuBar);

        StatusBar statusBar = new StatusBar(model, getWidth());
        add(statusBar, BorderLayout.SOUTH);

        editor.requestFocusInWindow();
    }

    private void initializeToolBar(JToolBar toolBar) {
        toolBar.add(new JButton(actions.get("undo")));
        toolBar.add(new JButton(actions.get("redo")));
        toolBar.add(new JButton(actions.get("cut")));
        toolBar.add(new JButton(actions.get("copy")));
        toolBar.add(new JButton(actions.get("paste")));
    }

    private void initializeMenuBar(JMenuBar menuBar) {
        JMenu fileMenu = new JMenu("File");
        fileMenu.add(actions.get("open"));
        fileMenu.add(actions.get("save"));
        fileMenu.add(actions.get("exit"));
        menuBar.add(fileMenu);

        JMenu editMenu = new JMenu("Edit");
        editMenu.add(actions.get("undo"));
        editMenu.add(actions.get("redo"));
        editMenu.add(actions.get("cut"));
        editMenu.add(actions.get("copy"));
        editMenu.add(actions.get("paste"));
        editMenu.add(actions.get("pasteAndTake"));
        editMenu.add(actions.get("delete"));
        editMenu.add(actions.get("clear"));
        menuBar.add(editMenu);

        JMenu moveMenu = new JMenu("Move");
        moveMenu.add(actions.get("moveStart"));
        moveMenu.add(actions.get("moveEnd"));
        menuBar.add(moveMenu);
    }

    private void initializeObservers() {
        editor.getModel().getManager()
                .attachRedoStackObserver(isStackEmpty -> actions.get("redo").setEnabled(!isStackEmpty));
        editor.getModel().getManager()
                .attachUndoStackObserver(isStackEmpty -> actions.get("undo").setEnabled(!isStackEmpty));
        editor.getModel()
                .attachSelectionObserver(selectionExists -> actions.get("cut").setEnabled(selectionExists));
        editor.getModel()
                .attachSelectionObserver(selectionExists -> actions.get("copy").setEnabled(selectionExists));
        editor.getModel()
                .attachSelectionObserver(selectionExists -> actions.get("delete").setEnabled(selectionExists));
        editor.getClipboard()
                .attachClipboardObserver(isClipboardEmpty -> actions.get("paste").setEnabled(!isClipboardEmpty));
        editor.getClipboard()
                .attachClipboardObserver(isClipboardEmpty -> actions.get("pasteAndTake").setEnabled(!isClipboardEmpty));
    }

    private void loadAvailablePlugins(JMenuBar menuBar) {
        JMenu pluginsMenu = new JMenu("Plugins");
        List<Plugin> plugins = Arrays.stream(new File("src/hr/fer/ooup/plugins/implementations").listFiles())
                .map(File::getName)
                .map(this::removeFileExtension)
                .map(PluginFactory::newInstance)
                .toList();
        for (Plugin plugin : plugins) {
            pluginsMenu.add(new AbstractAction(plugin.getName()) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    plugin.execute(editor.getModel(), editor.getModel().getManager(), editor.getClipboard());
                }
            });
        }
        menuBar.add(pluginsMenu);
    }

    private String removeFileExtension(String fileName) {
        int pointIndex = fileName.lastIndexOf(".");
        return fileName.substring(0, pointIndex);
    }

}
