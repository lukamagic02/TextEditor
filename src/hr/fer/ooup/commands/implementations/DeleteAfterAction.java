package hr.fer.ooup.commands.implementations;

import hr.fer.ooup.commands.EditAction;
import hr.fer.ooup.data.Location;
import hr.fer.ooup.model.TextEditorModel;

import java.util.List;

public class DeleteAfterAction implements EditAction {

    private TextEditorModel model;
    private char deletedSign;
    private Location cursor;
    private Location cursorLocationBeforeDeletion;

    public DeleteAfterAction(TextEditorModel model, Location cursor) {
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
        if (offset == lines.get(rowIndex).length()) {
            if (rowIndex == lines.size() - 1) {
                return;
            }
            String updatedCurrRow = lines.get(rowIndex) + lines.get(rowIndex + 1);
            lines.set(rowIndex, updatedCurrRow);
            lines.remove(rowIndex + 1);
        } else {
            StringBuilder builder = new StringBuilder(lines.get(rowIndex));
            deletedSign = builder.charAt(offset);
            builder.deleteCharAt(offset);
            lines.set(rowIndex, builder.toString());
        }

        model.notifyTextObservers();
    }

    @Override
    public void execute_undo() {
        List<String> lines = model.getLines();
        int rowIndex = cursorLocationBeforeDeletion.getRowIndex();
        int offset = cursorLocationBeforeDeletion.getOffset();

        if (deletedSign == '\u0000') {
            String deletedRow = lines.get(rowIndex).substring(offset);
            lines.set(rowIndex, lines.get(rowIndex).substring(0, offset));
            lines.add(rowIndex + 1, deletedRow);
        } else {
            StringBuilder builder = new StringBuilder(lines.get(rowIndex));
            builder.insert(offset, deletedSign);
            lines.set(rowIndex, builder.toString());
        }
        cursor.setRowIndex(rowIndex);
        cursor.setOffset(offset);

        model.notifyTextObservers();
    }
}
