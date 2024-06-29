package hr.fer.ooup.ui;

import hr.fer.ooup.model.ClipboardStack;
import hr.fer.ooup.data.Location;
import hr.fer.ooup.data.LocationRange;
import hr.fer.ooup.model.TextEditorModel;
import hr.fer.ooup.observers.CursorObserver;
import hr.fer.ooup.observers.TextObserver;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.Set;

public class TextEditor extends JComponent implements CursorObserver, TextObserver {

    private TextEditorModel model;
    private ClipboardStack clipboard;

    public TextEditor(TextEditorModel model) {
        this.model = model;
        model.attachCursorObserver(this);
        model.attachTextObserver(this);
        clipboard = new ClipboardStack();

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                TextEditor.this.requestFocusInWindow();
            }
        });

    }

    public TextEditorModel getModel() {
        return model;
    }

    public void setModel(TextEditorModel model) {
        this.model = model;
    }

    public ClipboardStack getClipboard() {
        return clipboard;
    }

    public void setClipboard(ClipboardStack clipboard) {
        this.clipboard = clipboard;
    }

    @Override
    protected void paintComponent(Graphics g) {
        g.setColor(Color.BLACK);

        FontMetrics metrics = g.getFontMetrics();
        int lineHeight = metrics.getHeight();

        Integer x = 10;
        Integer y = lineHeight;

        drawLines(g, x, y, lineHeight);
        drawCursor(g, metrics, x, y, lineHeight);

        if (model.getSelectionRange() != null) {
            drawSelectionArea(g, metrics, x, y, lineHeight);
        }

    }

    private void drawLines(Graphics g, Integer x, Integer y, int lineHeight) {
        Iterator<String> it = model.allLines();
        while (it.hasNext()) {
            g.drawString(it.next(), x, y);
            y += lineHeight;
        }
    }

    private void drawCursor(Graphics g, FontMetrics metrics, Integer x, Integer y, int lineHeight) {
        Location cursorLocation = model.getCursorLocation();
        int cursorRowIndex = cursorLocation.getRowIndex();
        Iterator<String> it = model.linesRange(cursorRowIndex, cursorRowIndex + 1);

        if (it.hasNext()) {
            x += metrics.stringWidth(it.next().substring(0, cursorLocation.getOffset()));
        }
        y = cursorRowIndex * lineHeight;

        g.setColor(Color.RED);
        g.drawLine(x, y, x, y + lineHeight);
    }

    private void drawSelectionArea(Graphics g, FontMetrics metrics, Integer x, Integer y, int lineHeight) {
        g.setColor(new Color(0, 0, 255, 40));
        LocationRange range = model.getSelectionRange();

        Location coordinate1 = range.getCoordinate1(), coordinate2 = range.getCoordinate2();
        if (coordinate1.getRowIndex() > coordinate2.getRowIndex() ||
                coordinate1.getRowIndex() == coordinate2.getRowIndex()
                        && coordinate1.getOffset() > coordinate2.getOffset()) {
            Location tmp = coordinate2;
            coordinate2 = coordinate1;
            coordinate1 = tmp;
        }

        Iterator<String> it = model.linesRange(coordinate1.getRowIndex(), coordinate2.getRowIndex() + 1);

        int i = coordinate1.getRowIndex();
        while (it.hasNext()) {
            String line = it.next();

            int startOffset = (i == coordinate1.getRowIndex()) ? coordinate1.getOffset() : 0;
            int endOffset = (i == coordinate2.getRowIndex()) ? coordinate2.getOffset() : line.length();

            x = metrics.stringWidth(line.substring(0, startOffset)) + 10;
            y = i * lineHeight;
            int width = metrics.stringWidth(line.substring(startOffset, endOffset));
            g.fillRect(x, y, width, lineHeight);

            i++;
        }
    }

    @Override
    protected void processKeyEvent(KeyEvent e) {
        if (e.getID() != KeyEvent.KEY_PRESSED) {
            return;
        }

        if (e.isControlDown()) {
            handleControlCommand(e);
        } else {
            handleKeyPress(e);
        }
    }

    private void handleControlCommand(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_C:
                copy();
                break;
            case KeyEvent.VK_X:
                cut();
                break;
            case KeyEvent.VK_V:
                paste(e.isShiftDown());
                break;
            case KeyEvent.VK_Z:
                undo();
                break;
            case KeyEvent.VK_Y:
                redo();
        }
    }

    private void handleKeyPress(KeyEvent e) {
        Set<Integer> arrowButtons = Set.of(KeyEvent.VK_DOWN, KeyEvent.VK_UP, KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT);

        if (arrowButtons.contains(e.getKeyCode())) {
            preprocessArrowKeyPress(e);
        }

        keyPress(e);
    }

    private void preprocessArrowKeyPress(KeyEvent e) {
        boolean isShiftDown = e.isShiftDown();
        LocationRange selection = model.getSelectionRange();

        if (isShiftDown && selection == null) {
            selection = new LocationRange();
            Location cursor = model.getCursorLocation();
            Location coordinate1 = new Location(cursor.getRowIndex(), cursor.getOffset());

            selection.setCoordinate1(coordinate1);
            selection.setCoordinate2(model.getCursorLocation());

            model.setSelectionRange(selection);
        } else if (!isShiftDown && selection != null) {
            model.setSelectionRange(null);
        }
    }

    private void keyPress(KeyEvent e) {
        int keyCode = e.getKeyCode();

        if (keyCode == KeyEvent.VK_UP) {
            model.moveCursorUp();
        } else if (keyCode == KeyEvent.VK_DOWN) {
            model.moveCursorDown();
        } else if (keyCode == KeyEvent.VK_LEFT) {
            model.moveCursorLeft();
        } else if (keyCode == KeyEvent.VK_RIGHT) {
            model.moveCursorRight();
        } else if (keyCode == KeyEvent.VK_BACK_SPACE) {
            model.deleteBefore();
        } else if (keyCode == KeyEvent.VK_DELETE) {
            model.deleteAfter();
        } else if (keyCode >= 32 && keyCode <= 126 || keyCode == 10) {
            model.insert(String.valueOf(e.getKeyChar()));
        }
    }

    public void copy() {
        LocationRange selection = model.getSelectionRange();
        if (selection != null) {
            String text = model.getSelectionText(selection);
            clipboard.push(text);
        }
    }

    public void cut() {
        LocationRange selection = model.getSelectionRange();
        if (selection != null) {
            String text = model.getSelectionText(selection);
            clipboard.push(text);
            model.deleteRange();
        }
    }

    public void paste(boolean isShiftDown) {
        if (!clipboard.isEmpty()) {
            String text = isShiftDown ? clipboard.pop() : clipboard.peek();
            model.insert(text);
        }
    }

    public void delete() {
        model.deleteRange();
    }

    public void clear() {
        Location cursor = model.getCursorLocation();
        LocationRange locationRange = new LocationRange();

        model.moveCursorToDocumentStart();
        Location beginning = new Location(cursor.getRowIndex(), cursor.getOffset());
        model.moveCursorToDocumentEnd();

        locationRange.setCoordinate1(beginning);
        locationRange.setCoordinate2(model.getCursorLocation());

        model.setSelectionRange(locationRange);
        model.deleteRange();
    }

    public void undo() {
        model.getManager().undo();
    }

    public void redo() {
        model.getManager().redo();
    }

    public void moveCursorToDocumentStart() {
        model.moveCursorToDocumentStart();
    }

    public void moveCursorToDocumentEnd() {
        model.moveCursorToDocumentEnd();
    }

    @Override
    public void updateCursorLocation() {
        this.repaint();
    }

    @Override
    public void updateText() {
        this.repaint();
    }

}
