package hr.fer.ooup.commands.implementations;

import hr.fer.ooup.commands.EditAction;
import hr.fer.ooup.data.Location;
import hr.fer.ooup.model.TextEditorModel;

import java.util.List;

public class InsertAction implements EditAction {

    private TextEditorModel model;
    private String text;
    private Location originalCursorLocation;
    private Location currentCursorLocation;

    public InsertAction(TextEditorModel model, String text, Location cursor) {
        this.model = model;
        this.text = text;
        originalCursorLocation = new Location(cursor.getRowIndex(), cursor.getOffset());
        currentCursorLocation = cursor;
    }

    @Override
    public void execute_do() {
        if (model.getSelectionRange() != null) {
            model.deleteRange();
        }

        List<String> lines = model.getLines();
        int offset = currentCursorLocation.getOffset();
        int rowIndex = currentCursorLocation.getRowIndex();

        if (text.length() == 1) {
            handleSignInsert(lines, rowIndex, offset);
        } else {
            handleSequenceInsert(lines, rowIndex, offset);
        }

        for (int i = 0; i < text.length(); i++) {
            model.moveCursorRight();
        }
        model.notifyTextObservers();
    }

    private void handleSignInsert(List<String> lines, int rowIndex, int offset) {
        String currRow = lines.get(rowIndex);

        if (text.equals("\n")) {
            lines.add(rowIndex + 1, currRow.substring(offset));
            lines.set(rowIndex, currRow.substring(0, offset));
        } else {
            String newCurrRow = currRow.substring(0, offset) + text + currRow.substring(offset);
            lines.set(rowIndex, newCurrRow);
        }
    }

    private void handleSequenceInsert(List<String> lines, int rowIndex, int offset) {
        String currRow = lines.get(rowIndex);
        lines.remove(rowIndex);

        String newCurrRow = currRow.substring(0, offset) + text + currRow.substring(offset);
        String[] newRows = newCurrRow.split("\n");

        for (String row : newRows) {
            lines.add(rowIndex, row);
            rowIndex++;
        }

    }

    @Override
    public void execute_undo() {
        List<String> lines = model.getLines();
        int rowIndex = originalCursorLocation.getRowIndex();
        int offset = originalCursorLocation.getOffset();

        if (text.length() == 1) {
            handleSignRemoval(lines, rowIndex, offset);
        } else {
            handleSequenceRemoval(lines, rowIndex, offset);
        }

        model.notifyTextObservers();
    }

    private void handleSignRemoval(List<String> lines, int rowIndex, int offset) {
        String currRow = lines.get(rowIndex);

        if (text.equals("\n")) {
            String nextRow = lines.get(rowIndex + 1);
            lines.set(rowIndex, currRow + nextRow);
            lines.remove(rowIndex + 1);
        } else {
            String newCurrRow = currRow.substring(0, offset) + currRow.substring(offset + 1);
            lines.set(rowIndex, newCurrRow);
        }

        for (int i = 0; i < text.length(); i++) {
            model.moveCursorLeft();
        }
    }

    private void handleSequenceRemoval(List<String> lines, int rowIndex, int offset) {
        String currRow = lines.get(rowIndex);

        String[] splits = text.split("\n");
        int size = splits.length;
        int beginIndex = size == 1 ? offset + text.length() : splits[size - 1].length();
        int endIndex = size == 1 ? currRow.length() : lines.get(rowIndex + splits.length - 1 ).length();

        String oldRowFirstPart = currRow.substring(0, offset);
        String oldRowSecondPart = lines.get(rowIndex + splits.length - 1).substring(beginIndex, endIndex);

        for (int i = 0; i < size; i++) {
            lines.remove(rowIndex);
        }

        lines.add(rowIndex, oldRowFirstPart + oldRowSecondPart);
        currentCursorLocation.setRowIndex(rowIndex);
        currentCursorLocation.setOffset(offset);
    }
}
