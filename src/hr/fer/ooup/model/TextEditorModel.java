package hr.fer.ooup.model;

import hr.fer.ooup.commands.implementations.DeleteAfterAction;
import hr.fer.ooup.commands.implementations.DeleteBeforeAction;
import hr.fer.ooup.commands.implementations.DeleteRangeAction;
import hr.fer.ooup.commands.implementations.InsertAction;
import hr.fer.ooup.data.Location;
import hr.fer.ooup.data.LocationRange;
import hr.fer.ooup.commands.*;
import hr.fer.ooup.observers.CursorObserver;
import hr.fer.ooup.observers.SelectionObserver;
import hr.fer.ooup.observers.TextObserver;
import hr.fer.ooup.ui.TextEditor;

import java.util.*;
import java.util.stream.Collectors;

public class TextEditorModel {

    private List<String> lines;
    private Location cursorLocation;
    private LocationRange selectionRange;
    private UndoManager manager;
    private Set<CursorObserver> cursorObservers;
    private Set<SelectionObserver> selectionObservers;
    private Set<TextObserver> textObservers;

    public TextEditorModel(String text) {
        lines = Arrays.stream(text.split("\n")).collect(Collectors.toList());
        int rowIndex = lines.size() - 1;
        int offset = lines.get(rowIndex).length();
        cursorLocation = new Location(rowIndex, offset);
        manager = UndoManager.getInstance();
        cursorObservers = new HashSet<>();
        selectionObservers = new HashSet<>();
        textObservers = new HashSet<>();
    }

    public List<String> getLines() {
        return lines;
    }

    public void setLines(List<String> newLines) {
        lines.clear();
        lines.addAll(newLines);
        cursorLocation.setOffset(0);
        cursorLocation.setRowIndex(0);
        notifyTextObservers();
    }

    public Location getCursorLocation() {
        return cursorLocation;
    }

    public LocationRange getSelectionRange() {
        return selectionRange;
    }

    public void setSelectionRange(LocationRange selectionRange) {
        this.selectionRange = selectionRange;
        notifySelectionObservers(selectionRange != null);
    }

    public UndoManager getManager() {
        return manager;
    }

    public void deleteBefore() {
        EditAction action;
        if (selectionRange != null) {
            action = new DeleteRangeAction(this);
        } else {
            action = new DeleteBeforeAction(this, cursorLocation);
        }
        action.execute_do();
        manager.push(action);
    }

    public void deleteAfter() {
        EditAction action;
        if (selectionRange != null) {
            action = new DeleteRangeAction(this);
        } else {
            action = new DeleteAfterAction(this, cursorLocation);
        }
        action.execute_do();
        manager.push(action);
    }

    public void deleteRange() {
        EditAction action = new DeleteRangeAction(this);
        action.execute_do();
        manager.push(action);
    }

    public void insert(String text) {
        EditAction action = new InsertAction(this, text, cursorLocation);
        action.execute_do();
        manager.push(action);
    }

    public void moveCursorLeft() {
        int currRowIndex = cursorLocation.getRowIndex();
        int currOffset = cursorLocation.getOffset();

        if (currOffset != 0) {
            cursorLocation.setOffset(currOffset - 1);
        } else if (currRowIndex != 0) {
            cursorLocation.setRowIndex(currRowIndex - 1);
            cursorLocation.setOffset(lines.get(currRowIndex - 1).length());
        } else {
            return;
        }

        notifyCursorObservers();
    }

    public void moveCursorRight() {
        int currRowIndex = cursorLocation.getRowIndex();
        int currOffset = cursorLocation.getOffset();

        if (currOffset != lines.get(currRowIndex).length()) {
            cursorLocation.setOffset(currOffset + 1);
        } else if (currRowIndex != lines.size() - 1) {
            cursorLocation.setRowIndex(currRowIndex + 1);
            cursorLocation.setOffset(0);
        } else {
            return;
        }

        notifyCursorObservers();
    }

    public void moveCursorUp() {
        int currRowIndex = cursorLocation.getRowIndex();
        int currOffset = cursorLocation.getOffset();

        if (currRowIndex == 0) {
            return;
        } else if (currOffset > lines.get(currRowIndex - 1).length()) {
            cursorLocation.setOffset(lines.get(currRowIndex - 1).length());
        }
        cursorLocation.setRowIndex(currRowIndex - 1);

        notifyCursorObservers();
    }

    public void moveCursorDown() {
        int currRowIndex = cursorLocation.getRowIndex();
        int currOffset = cursorLocation.getOffset();

        if (currRowIndex == lines.size() - 1) {
            return;
        } else if (currOffset > lines.get(currRowIndex + 1).length()) {
            cursorLocation.setOffset(lines.get(currRowIndex + 1).length());
        }
        cursorLocation.setRowIndex(currRowIndex + 1);

        notifyCursorObservers();
    }

    public void moveCursorToDocumentStart() {
        cursorLocation.setOffset(0);
        cursorLocation.setRowIndex(0);
        notifyCursorObservers();
    }

    public void moveCursorToDocumentEnd() {
        cursorLocation.setRowIndex(lines.size() - 1);
        cursorLocation.setOffset(lines.get(lines.size() - 1).length());
        notifyCursorObservers();
    }

    public void attachTextObserver(TextObserver observer) {
        textObservers.add(observer);
    }

    public void detachTextObserver(TextObserver observer) {
        textObservers.remove(observer);
    }

    public void notifyTextObservers() {
        for (TextObserver observer : textObservers) {
            observer.updateText();
        }
    }

    public void attachSelectionObserver(SelectionObserver observer) {
        selectionObservers.add(observer);
    }

    public void detachSelectionObserver(SelectionObserver observer) {
        selectionObservers.remove(observer);
    }

    public void notifySelectionObservers(boolean selectionExists) {
        for (SelectionObserver observer : selectionObservers) {
            observer.selectionStateChanged(selectionExists);
        }
    }

    public void attachCursorObserver(CursorObserver observer) {
        cursorObservers.add(observer);
    }

    public void detachCursorObserver(CursorObserver observer) {
        cursorObservers.remove(observer);
    }

    public void notifyCursorObservers() {
        for (CursorObserver observer : cursorObservers) {
            observer.updateCursorLocation();
        }
    }

    public Iterator<String> allLines() {
        return new LineIterator(lines, 0, lines.size());
    }

    public Iterator<String> linesRange(int index1, int index2) {
        return new LineIterator(lines, index1, index2);
    }

    public String getSelectionText(LocationRange r) {
        LocationRange range = sortRange(r);

        int beginIndex = range.getCoordinate1().getRowIndex(), endIndex = range.getCoordinate2().getRowIndex();
        int beginOffset = range.getCoordinate1().getOffset(), endOffset = range.getCoordinate2().getOffset();

        if (beginIndex == endIndex) {
            return lines.get(beginIndex).substring(beginOffset, endOffset);
        } else {
            StringBuilder builder = new StringBuilder();

            for (int i = beginIndex; i <= endIndex; i++) {
                String line = lines.get(i);
                if (i == beginIndex) {
                    builder.append(line.substring(beginOffset)).append("\n");
                } else if (i == endIndex) {
                    builder.append(line.substring(0, endOffset));
                } else {
                    builder.append(line).append("\n");
                }
            }

            return builder.toString();
        }
    }

    public LocationRange sortRange(LocationRange r) {
        LocationRange range = new LocationRange();
        range.setCoordinate1(new Location(r.getCoordinate1().getRowIndex(), r.getCoordinate1().getOffset()));
        range.setCoordinate2(new Location(r.getCoordinate2().getRowIndex(), r.getCoordinate2().getOffset()));

        Location lBorder = range.getCoordinate1(), rBorder = range.getCoordinate2();
        if (lBorder.getRowIndex() > rBorder.getRowIndex() ||
                lBorder.getRowIndex() == rBorder.getRowIndex() && lBorder.getOffset() > rBorder.getOffset()) {
            Location tmp = rBorder;
            range.setCoordinate2(lBorder);
            range.setCoordinate1(tmp);
        }

        return range;
    }

    // potentially add invalid arguments error handling

    private static class LineIterator implements Iterator<String> {

        private List<String> lines;
        private int current;
        private int end;

        public LineIterator(List<String> lines, int index1, int index2) {
            this.lines = lines;
            current = index1;
            end = index2;
        }

        @Override
        public boolean hasNext() {
            return current < end;
        }

        @Override
        public String next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            return lines.get(current++);
        }

    }

}
