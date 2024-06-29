package hr.fer.ooup.plugins.implementations;

import hr.fer.ooup.model.ClipboardStack;
import hr.fer.ooup.model.TextEditorModel;
import hr.fer.ooup.model.UndoManager;
import hr.fer.ooup.plugins.Plugin;

import javax.swing.*;
import java.awt.*;

public class Statistika implements Plugin {

    @Override
    public String getName() {
        return "Statistika";
    }

    @Override
    public String getDescription() {
        return "Broji koliko ima redaka, riječi i slova u dokumentu i to prikazuje korisniku u dijalogu.";
    }

    @Override
    public void execute(TextEditorModel model, UndoManager undoManager, ClipboardStack clipboardStack) {
        int rowNumber = model.getLines().size();
        int wordNumber = model.getLines().stream().mapToInt(line -> line.split(" ").length).sum();
        int letterNumber = model.getLines().stream().mapToInt(line -> {
            int letterCount = 0;
            for (char c : line.toCharArray()) {
                if (Character.isLetter(c)) {
                    letterCount++;
                }
            }
            return letterCount;
        }).sum();

        JDialog dialog = new JDialog();
        dialog.setTitle("Statistika");
        dialog.setSize(300, 200);

        JLabel label = new JLabel("Row number: " + rowNumber + ", word number: "
                + wordNumber + ", letter number: " + letterNumber);
        label.setHorizontalAlignment(JLabel.LEFT);
        label.setVerticalAlignment(JLabel.TOP);

        dialog.getContentPane().add(label, BorderLayout.NORTH);

        dialog.setVisible(true);
    }

}
