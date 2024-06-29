package hr.fer.ooup.model;

import hr.fer.ooup.observers.ClipboardObserver;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

public class ClipboardStack {

    private Stack<String> texts = new Stack<>();
    private Set<ClipboardObserver> observers = new HashSet<>();

    public void push(String text) {
        texts.push(text);
        notifyClipboardObserver(texts.isEmpty());
    }

    public String pop() {
        String text = texts.pop();
        notifyClipboardObserver(texts.isEmpty());
        return text;
    }

    public String peek() {
        return texts.peek();
    }

    public boolean isEmpty() {
        return texts.empty();
    }

    public void delete() {
        texts.removeAllElements();
    }

    public void attachClipboardObserver(ClipboardObserver observer) {
        observers.add(observer);
    }

    public void detachClipboardObserver(ClipboardObserver observer) {
        observers.remove(observer);
    }

    public void notifyClipboardObserver(boolean isClipboardEmpty) {
        for (ClipboardObserver o : observers) {
            o.updateClipboard(isClipboardEmpty);
        }
    }

}
