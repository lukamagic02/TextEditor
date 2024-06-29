package hr.fer.ooup.plugins;

import hr.fer.ooup.model.ClipboardStack;
import hr.fer.ooup.model.TextEditorModel;
import hr.fer.ooup.model.UndoManager;

public interface Plugin {

    String getName();
    String getDescription();
    void execute(TextEditorModel model, UndoManager undoManager, ClipboardStack clipboardStack);
}
