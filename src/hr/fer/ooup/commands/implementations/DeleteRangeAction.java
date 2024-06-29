package hr.fer.ooup.commands.implementations;

import hr.fer.ooup.commands.EditAction;
import hr.fer.ooup.data.Location;
import hr.fer.ooup.data.LocationRange;
import hr.fer.ooup.model.TextEditorModel;

import java.util.List;

public class DeleteRangeAction implements EditAction {

    private TextEditorModel model;
    private LocationRange deletedSelection;
    private String[] deletedContent;

    public DeleteRangeAction(TextEditorModel model) {
        this.model = model;

        deletedSelection = new LocationRange();
        Location coordinate1 = model.getSelectionRange().getCoordinate1();
        Location coordinate2 = model.getSelectionRange().getCoordinate2();
        deletedSelection.setCoordinate1(new Location(coordinate1.getRowIndex(), coordinate1.getOffset()));
        deletedSelection.setCoordinate2(new Location(coordinate2.getRowIndex(), coordinate2.getOffset()));

        LocationRange sortedDeletedSelection = model.sortRange(deletedSelection);
        deletedContent = model.getSelectionText(sortedDeletedSelection).split("\n");
    }

    @Override
    public void execute_do() {
        List<String> lines = model.getLines();

        LocationRange sortedDeletedSelection = model.sortRange(deletedSelection);
        int firstRowIndex = sortedDeletedSelection.getCoordinate1().getRowIndex(),
                lastRowIndex = sortedDeletedSelection.getCoordinate2().getRowIndex();
        int firstOffset = sortedDeletedSelection.getCoordinate1().getOffset(),
                lastOffset = sortedDeletedSelection.getCoordinate2().getOffset();
        String resultLine = lines.get(firstRowIndex).substring(0, firstOffset)
                + lines.get(lastRowIndex).substring(lastOffset);

        if (firstRowIndex == lastRowIndex) {
            lines.set(firstRowIndex, resultLine);
        } else {
            for (int i = 0; i <= lastRowIndex - firstRowIndex; i++) {
                lines.remove(firstRowIndex);
            }
            lines.add(firstRowIndex, resultLine);
        }
        Location cursor = model.getCursorLocation();
        cursor.setRowIndex(firstRowIndex);
        cursor.setOffset(firstOffset);

        model.setSelectionRange(null);
        model.notifyCursorObservers();
        model.notifyTextObservers();
    }

    @Override
    public void execute_undo() {
        List<String> lines = model.getLines();

        LocationRange sortedDeletedSelection = model.sortRange(deletedSelection);
        int firstRowIndex = sortedDeletedSelection.getCoordinate1().getRowIndex(),
                lastRowIndex = sortedDeletedSelection.getCoordinate2().getRowIndex();
        int firstOffset = sortedDeletedSelection.getCoordinate1().getOffset();

        String rowPartBeforeSelection = lines.get(firstRowIndex).substring(0, firstOffset);
        String rowPartAfterSelection = lines.get(firstRowIndex).substring(firstOffset);
        if (firstRowIndex == lastRowIndex) {
            lines.set(firstRowIndex, rowPartBeforeSelection + deletedContent[0] + rowPartAfterSelection);
        } else {
            lines.set(firstRowIndex, rowPartBeforeSelection + deletedContent[0]);
            for (int i = 1, n = lastRowIndex - firstRowIndex; i < n; i++) {
                lines.add(firstRowIndex + i, deletedContent[i]);
            }
            lines.add(lastRowIndex, deletedContent[lastRowIndex - firstRowIndex] + rowPartAfterSelection);
        }

        Location cursor = model.getCursorLocation();
        cursor.setRowIndex(deletedSelection.getCoordinate2().getRowIndex());
        cursor.setOffset(deletedSelection.getCoordinate2().getOffset());

        LocationRange selection = new LocationRange();
        selection.setCoordinate1(deletedSelection.getCoordinate1());
        selection.setCoordinate2(cursor);

        model.setSelectionRange(selection);
        model.notifyCursorObservers();
        model.notifyTextObservers();
    }

}
