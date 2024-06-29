package hr.fer.ooup.plugins.implementations;

import hr.fer.ooup.data.Location;
import hr.fer.ooup.data.LocationRange;
import hr.fer.ooup.model.ClipboardStack;
import hr.fer.ooup.model.TextEditorModel;
import hr.fer.ooup.model.UndoManager;
import hr.fer.ooup.plugins.Plugin;

import javax.swing.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class VelikoSlovo implements Plugin {

    @Override
    public String getName() {
        return "VelikoSlovo";
    }

    @Override
    public String getDescription() {
        return "Prolazi kroz dokument i svako prvo slovo riječi mijenja u veliko.";
    }

    @Override
    public void execute(TextEditorModel model, UndoManager undoManager, ClipboardStack clipboardStack) {
        List<String> lines = new LinkedList<>();
        lines.addAll(model.getLines());

        removeAllText(model);
        String updatedText = lines.stream().map(line -> {
            return Arrays.stream(line.split(" ")).map(word -> {
                char firstLetter = word.charAt(0);
                if (Character.isLowerCase(firstLetter)) {
                    StringBuilder builder = new StringBuilder(word);
                    builder.setCharAt(0, Character.toUpperCase(firstLetter));
                    return builder.toString();
                }
                return word;
            }).collect(Collectors.joining(" "));
        }).collect(Collectors.joining("\n"));
        model.insert(updatedText);
    }

    private void removeAllText(TextEditorModel model) {
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
}
