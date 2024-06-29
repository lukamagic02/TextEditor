package hr.fer.ooup.commands.implementations;

import hr.fer.ooup.commands.EditAction;
import hr.fer.ooup.data.Location;
import hr.fer.ooup.model.TextEditorModel;

import java.util.List;

public class DeleteBeforeAction implements EditAction {

    private TextEditorModel model;
    private char deletedSign;
    private Location cursor;
    private Location cursorLocationBeforeDeletion;
    private Location cursorLocationAfterDeletion;

    public DeleteBeforeAction(TextEditorModel model, Location cursor) {
        this.model = model;
        this.cursor = cursor;
        cursorLocationBeforeDeletion = new Location(cursor.getRowIndex(), cursor.getOffset());
    }

    @Override
    public void execute_do() {
        List<String> lines = model.getLines();
        int rowIndex = cursorLocationBeforeDeletion.getRowIndex();
        int offset = cursorLocationBeforeDeletion.getOffset();

        cursor.setRowIndex(rowIndex);
        cursor.setOffset(offset);
        if (offset == 0) {
            if (rowIndex == 0) {
                return;
            }

            String currRow = lines.get(rowIndex);
            String updatedPrevRow = lines.get(rowIndex - 1) + lines.get(rowIndex);
            lines.set(rowIndex - 1, updatedPrevRow);
            lines.remove(rowIndex);

            for (int i = 0; i < currRow.length() + 1; i++) {
                model.moveCursorLeft();
            }
        } else {
            StringBuilder builder = new StringBuilder(lines.get(rowIndex));
            deletedSign = builder.charAt(offset - 1);
            builder.deleteCharAt(offset - 1);
            lines.set(rowIndex, builder.toString());
            model.moveCursorLeft();
        }

        cursorLocationAfterDeletion = new Location(cursor.getRowIndex(), cursor.getOffset());
        model.notifyTextObservers();
    }

    @Override
    public void execute_undo() {
        List<String> lines = model.getLines();
        int rowIndex = cursorLocationAfterDeletion.getRowIndex();
        int offset = cursorLocationAfterDeletion.getOffset();

        cursor.setRowIndex(rowIndex);
        cursor.setOffset(offset);
        model.notifyCursorObservers();

        if (deletedSign == '\u0000') {
            String deletedRow = lines.get(rowIndex).substring(offset);
            lines.set(rowIndex, lines.get(rowIndex).substring(0, offset));
            lines.add(rowIndex + 1, deletedRow);

            model.moveCursorRight();
        } else {
            StringBuilder builder = new StringBuilder(lines.get(rowIndex));
            builder.insert(offset, deletedSign);
            lines.set(rowIndex, builder.toString());
            model.moveCursorRight();
        }

        model.notifyTextObservers();
    }
}
