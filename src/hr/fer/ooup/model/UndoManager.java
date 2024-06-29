package hr.fer.ooup.model;

import hr.fer.ooup.commands.EditAction;
import hr.fer.ooup.observers.RedoStackObserver;
import hr.fer.ooup.observers.UndoStackObserver;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

public class UndoManager {

    private static UndoManager manager;
    private Stack<EditAction> undoStack;
    private Stack<EditAction> redoStack;
    private Set<UndoStackObserver> undoStackObservers;
    private Set<RedoStackObserver> redoStackObservers;

    private UndoManager() {
        undoStack = new Stack<>();
        redoStack = new Stack<>();
        undoStackObservers = new HashSet<>();
        redoStackObservers = new HashSet<>();
    }

    public static UndoManager getInstance() {
        if (manager == null) {
            manager = new UndoManager();
        }
        return manager;
    }

    public void undo() {
        if (!undoStack.empty()) {
            EditAction action = undoStack.pop();
            redoStack.push(action);
            action.execute_undo();
            notifyUndoStackObservers();
            notifyRedoStackObservers();
        }
    }

    public void redo() {
        if (!redoStack.empty()) {
            EditAction action = redoStack.pop();
            undoStack.push(action);
            action.execute_do();
            notifyUndoStackObservers();
            notifyRedoStackObservers();
        }
    }

    public void push(EditAction c) {
        redoStack.removeAllElements();
        undoStack.push(c);
        notifyRedoStackObservers();
        notifyUndoStackObservers();
    }

    public void attachUndoStackObserver(UndoStackObserver o) {
        undoStackObservers.add(o);
    }

    public void detachUndoStackObserver(UndoStackObserver o) {
        undoStackObservers.remove(o);
    }

    private void notifyUndoStackObservers() {
        for (UndoStackObserver o : undoStackObservers) {
            o.undoStackContentChanged(undoStack.isEmpty());
        }
    }

    public void attachRedoStackObserver(RedoStackObserver o) {
        redoStackObservers.add(o);
    }

    public void detachRedoStackObserver(RedoStackObserver o) {
        redoStackObservers.remove(o);
    }

    private void notifyRedoStackObservers() {
        for (RedoStackObserver o : redoStackObservers) {
            o.redoStackContentChanged(redoStack.isEmpty());
        }
    }

}
