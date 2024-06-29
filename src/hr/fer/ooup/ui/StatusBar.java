package hr.fer.ooup.ui;

import hr.fer.ooup.model.TextEditorModel;
import hr.fer.ooup.observers.CursorObserver;
import hr.fer.ooup.observers.TextObserver;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;

public class StatusBar extends JPanel implements TextObserver, CursorObserver {

    private TextEditorModel model;
    private int rowNumber;
    private int rowIndex;
    private int offset;
    private JLabel statusLabel;

    public StatusBar(TextEditorModel model, int width) {
        this.model = model;
        rowNumber = model.getLines().size();
        rowIndex = model.getCursorLocation().getRowIndex();
        offset = model.getCursorLocation().getOffset();

        model.attachTextObserver(this);
        model.attachCursorObserver(this);

        initializeBarProperties(width);
    }

    private void initializeBarProperties(int width) {
        setBorder(new BevelBorder(BevelBorder.LOWERED));
        setPreferredSize(new Dimension(width, 16));
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        statusLabel = new JLabel("Row count: " + rowNumber +
                ", cursor position: (" + rowIndex + ", " + offset + ")");
        statusLabel.setHorizontalAlignment(SwingConstants.LEFT);
        add(statusLabel);
    }

    @Override
    public void updateCursorLocation() {
        rowIndex = model.getCursorLocation().getRowIndex();
        offset = model.getCursorLocation().getOffset();
        statusLabel.setText("Row count: " + rowNumber + ", cursor position: (" + rowIndex + ", " + offset + ")");
    }

    @Override
    public void updateText() {
        rowNumber = model.getLines().size();
        statusLabel.setText("Row count: " + rowNumber + ", cursor position: (" + rowIndex + ", " + offset + ")");
    }
}
