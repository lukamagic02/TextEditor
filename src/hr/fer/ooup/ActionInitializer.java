package hr.fer.ooup;

import hr.fer.ooup.ui.TextEditor;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class ActionInitializer {

    public static void initializeActions(Map<String, Action> actions, TextEditor editor) {
        AbstractAction action;

        action = new AbstractAction("Undo") {
            @Override
            public void actionPerformed(ActionEvent e) {
                editor.undo();
            }
        };
        action.setEnabled(false);
        actions.put("undo", action);

        action = new AbstractAction("Redo") {
            @Override
            public void actionPerformed(ActionEvent e) {
                editor.redo();
            }
        };
        action.setEnabled(false);
        actions.put("redo", action);

        action = new AbstractAction("Cut") {
            @Override
            public void actionPerformed(ActionEvent e) {
                editor.cut();
            }
        };
        action.setEnabled(false);
        actions.put("cut", action);

        action = new AbstractAction("Copy") {
            @Override
            public void actionPerformed(ActionEvent e) {
                editor.copy();
            }
        };
        action.setEnabled(false);
        actions.put("copy", action);

        action = new AbstractAction("Paste") {
            @Override
            public void actionPerformed(ActionEvent e) {
                editor.paste(false);
            }
        };
        action.setEnabled(false);
        actions.put("paste", action);


        action = new AbstractAction("Paste and take") {
            @Override
            public void actionPerformed(ActionEvent e) {
                editor.paste(true);
            }
        };
        action.setEnabled(false);
        actions.put("pasteAndTake", action);

        action = new AbstractAction("Delete") {
            @Override
            public void actionPerformed(ActionEvent e) {
                editor.delete();
            }
        };
        action.setEnabled(false);
        actions.put("delete", action);

        action = new AbstractAction("Clear") {
            @Override
            public void actionPerformed(ActionEvent e) {
                editor.clear();
            }
        };
        action.setEnabled(true);
        actions.put("clear", action);

        action = new AbstractAction("Cursor to document start") {
            @Override
            public void actionPerformed(ActionEvent e) {
                editor.moveCursorToDocumentStart();
            }
        };
        action.setEnabled(true);
        actions.put("moveStart", action);

        action = new AbstractAction("Cursor to document end") {
            @Override
            public void actionPerformed(ActionEvent e) {
                editor.moveCursorToDocumentEnd();
            }
        };
        action.setEnabled(true);
        actions.put("moveEnd", action);

        action = new AbstractAction("Open") {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                int returnValue = fileChooser.showOpenDialog(null);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    try {
                        List<String> newLines = Files.readAllLines(selectedFile.toPath());
                        editor.getModel().setLines(newLines);
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        };
        action.setEnabled(true);
        actions.put("open", action);

        action = new AbstractAction("Exit") {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        };
        action.setEnabled(true);
        actions.put("exit", action);

        action = new AbstractAction("Save") {
            @Override
            public void actionPerformed(ActionEvent e) {
                Path filePath = Paths.get("file.txt");

                try {
                    Files.write(filePath, editor.getModel().getLines());
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

            }
        };
        action.setEnabled(true);
        actions.put("save", action);
    }
}
