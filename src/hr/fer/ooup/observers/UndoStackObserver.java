package hr.fer.ooup.observers;

public interface UndoStackObserver {

    void undoStackContentChanged(boolean isStackEmpty);
}
