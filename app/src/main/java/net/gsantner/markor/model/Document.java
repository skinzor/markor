/*
 * ------------------------------------------------------------------------------
 * Gregor Santner <gsantner.net> wrote this. You can do whatever you want
 * with it. If we meet some day, and you think it is worth it, you can buy me a
 * coke in return. Provided as is without any kind of warranty. Do not blame or
 * sue me if something goes wrong. No attribution required.    - Gregor Santner
 *
 * License: Creative Commons Zero (CC0 1.0)
 *  http://creativecommons.org/publicdomain/zero/1.0/
 * ----------------------------------------------------------------------------
 */
package net.gsantner.markor.model;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

@SuppressWarnings({"WeakerAccess", "UnusedReturnValue", "unused"})
public class Document implements Serializable {
    private ArrayList<Document> _history = new ArrayList<>();
    private File _filePath; // Full filepath (path + filename + extension)
    private String _title;  // The title of the document. May lead to a rename at save
    private String _content = "";
    private boolean _doHistory = true;
    private int _historyPosition;
    private Date _lastChanged = new Date();

    public Document() {
    }

    public Document(File filePath) {
        _filePath = filePath;
    }

    public synchronized Document cloneDocument() {
        return fromDocumentToDocument(this, new Document());
    }

    public synchronized Document loadFromDocument(Document source) {
        return fromDocumentToDocument(source, this);
    }

    public synchronized static Document fromDocumentToDocument(Document source, Document target) {
        target.setDoHistory(false);
        target.setFilePath(source.getFilePath());
        target.setTitle(source.getTitle());
        target.setContent(source.getContent());
        target.setDoHistory(true);
        return target;
    }

    public synchronized boolean canGoToEarlierVersion() {
        // Position 5, History is 5 big, yes
        // Position 3, History is 5 big, yes
        // Position 0, History is 5 big, no
        // Position 0, History is 0 big, no
        return _historyPosition > 0 && _history.size() > 0;
    }

    public synchronized boolean canGoToNewerVersion() {
        // Position 5, History is 5 big, no
        // Position 3, History is 5 big, yes
        // Position 0, History is 5 big, yes
        // Position 0, History is 0 big, no
        return _historyPosition < _history.size() - 1;
    }

    public synchronized void goToEarlierVersion() {
        if (canGoToEarlierVersion()) {
            // If we are at the current state, but this was not saved yet -> save current state
            if (hasUnversionedChanges()) {
                addToHistory(true);
                _historyPosition--;
            }

            _historyPosition--;
            loadFromDocument(_history.get(_historyPosition));
        }
    }

    public boolean hasUnversionedChanges() {
        return _historyPosition == _history.size() && !_history.get(_history.size() - 1).equals(this);
    }

    public synchronized void goToNewerVersion() {
        if (canGoToNewerVersion()) {
            _historyPosition++;
            loadFromDocument(_history.get(_historyPosition));
        }
    }

    private synchronized void addToHistory(boolean immediately) {
        if (_doHistory) {
            while (_historyPosition != _history.size() && _history.size() != 0) {
                _history.remove(_history.size() - 1);
            }
            _history.add(cloneDocument());
            _historyPosition++;
        }
    }

    public synchronized File getFilePath() {
        return _filePath;
    }

    public synchronized void setFilePath(File filePath) {
        addToHistory(false);
        _filePath = filePath;
    }

    public synchronized String getTitle() {
        return _title;
    }

    public synchronized void setTitle(String title) {
        addToHistory(false);
        _title = title;
    }

    public synchronized String getContent() {
        return _content;
    }

    public synchronized void setContent(String content) {
        addToHistory(false);
        _content = content;
    }

    public boolean isDoHistory() {
        return _doHistory;
    }

    public void setDoHistory(boolean doHistory) {
        _doHistory = doHistory;
    }

    public ArrayList<Document> getHistory() {
        return _history;
    }

    public void setHistory(ArrayList<Document> history) {
        _history = history;
    }

    public int getHistoryPosition() {
        return _historyPosition;
    }

    public void setHistoryPosition(int historyPosition) {
        _historyPosition = historyPosition;
    }

    public Date getLastChanged() {
        return _lastChanged;
    }

    public void setLastChanged(Date lastChanged) {
        _lastChanged = lastChanged;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Document) {
            Document other = ((Document) obj);
            return nuquals(getFilePath(), other.getFilePath())
                    && nuquals(getTitle(), other.getTitle())
                    && nuquals(getContent(), other.getContent());
        }
        return super.equals(obj);
    }

    private static boolean nuquals(Object o1, Object o2) {
        return (o1 == null && o2 == null) || o1 != null && o1.equals(o2);
    }
}
