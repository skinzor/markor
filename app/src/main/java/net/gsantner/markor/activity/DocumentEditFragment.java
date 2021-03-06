package net.gsantner.markor.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import net.gsantner.markor.R;
import net.gsantner.markor.editor.HighlightingEditor;
import net.gsantner.markor.model.Constants;
import net.gsantner.markor.model.Document;
import net.gsantner.markor.ui.BaseFragment;
import net.gsantner.markor.util.AppSettings;
import net.gsantner.opoc.util.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Locale;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnTextChanged;

@SuppressWarnings("UnusedReturnValue")
public class DocumentEditFragment extends BaseFragment {
    public static final String FRAGMENT_TAG = "DocumentEditFragment";
    private static final String EXTRA_PATH = "EXTRA_PATH";
    private static final String EXTRA_PATH_IS_FOLDER = "EXTRA_PATH_IS_FOLDER";

    public static DocumentEditFragment newInstance(File path, boolean pathIsFolder) {
        DocumentEditFragment f = new DocumentEditFragment();
        Bundle args = new Bundle();
        args.putSerializable(EXTRA_PATH, path);
        args.putSerializable(EXTRA_PATH_IS_FOLDER, pathIsFolder);
        f.setArguments(args);
        return f;
    }


    @BindView(R.id.note__activity__note_content_editor)
    HighlightingEditor _contentEditor;

    @BindView(R.id.note__activity__markdownchar_bar)
    ViewGroup _markdownShortcutBar;

    private View _view;
    private Context _context;
    private Document _document;

    public DocumentEditFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.document__fragment__edit, container, false);
        ButterKnife.bind(this, view);
        _view = view;
        _context = view.getContext();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        Context c = getContext();
        setupMarkdownShortcutBar();
        setupAppearancePreferences();

        _document = loadDocument();
        _contentEditor.setText(_document.getContent());

        Activity activity = getActivity();
        if (activity != null && activity instanceof DocumentActivity) {
            DocumentActivity da = ((DocumentActivity) activity);
            da.setDocumentTitle(_document.getTitle());
        }
    }

    @OnTextChanged(value = R.id.note__activity__note_content_editor, callback = OnTextChanged.Callback.TEXT_CHANGED)
    public void onContentEditValueChanged(CharSequence text) {
        _document.setContent(text.toString());
    }

    @SuppressWarnings({"ConstantConditions", "ResultOfMethodCallIgnored"})
    private Document loadDocument() {
        if (_document != null) {
            return _document;
        }

        Document document = new Document();
        document.setDoHistory(false);
        document.setFileExtension(Constants.MD_EXT1_MD);
        Bundle arguments = getArguments();
        File extraPath = (File) arguments.getSerializable(EXTRA_PATH);
        File filePath = extraPath;

        // Generate random not existing filepath if filename not specified
        boolean extraPathIsFolder = arguments.getBoolean(EXTRA_PATH_IS_FOLDER);
        if (extraPathIsFolder) {
            extraPath.mkdirs();
            while (filePath.exists()) {
                filePath = new File(extraPath, getString(R.string.document_one) + " " + UUID.randomUUID().toString());
            }
        } else if (filePath.isFile() && filePath.canRead()) {
            // Extract existing extension
            for (String ext : Constants.EXTENSIONS) {
                if (filePath.getName().toLowerCase(Locale.getDefault()).endsWith(ext)) {
                    document.setFileExtension(ext);
                    break;
                }
            }

            // Extract content and title
            document.setTitle(Constants.MD_EXTENSION.matcher(filePath.getName()).replaceAll(""));
            document.setContent(FileUtils.readTextFile(filePath));
        }

        document.setFile(filePath);
        document.setDoHistory(true);
        return document;
    }

    private void setupMarkdownShortcutBar() {
        if (AppSettings.get().isShowMarkdownShortcuts() && _markdownShortcutBar.getChildCount() == 0) {
            // Smart Actions
            for (int[] actions : Constants.KEYBOARD_SMART_ACTIONS_ICON) {
                appendMarkdownShortcutToBar(actions[0], new KeyboardSmartActionsListener(Constants.KEYBOARD_SMART_ACTIONS[actions[1]]));
            }

            // Regular actions
            for (int[] actions : Constants.KEYBOARD_REGULAR_ACTIONS_ICONS) {
                appendMarkdownShortcutToBar(actions[0], new KeyboardRegularActionListener(Constants.KEYBOARD_REGULAR_ACTIONS[actions[1]]));
            }

            // Extra actions
            for (int[] actions : Constants.KEYBOARD_EXTRA_ACTIONS_ICONS) {
                appendMarkdownShortcutToBar(actions[0], new KeyboardExtraActionsListener(actions[1]));
            }
        } else if (!AppSettings.get().isShowMarkdownShortcuts()) {
            _view.findViewById(R.id.note__activity__scroll_markdownchar_bar).setVisibility(View.GONE);
        }
    }

    private void setupAppearancePreferences() {
        AppSettings as = AppSettings.get();
        _contentEditor.setTextSize(TypedValue.COMPLEX_UNIT_SP, as.getFontSize());
        _contentEditor.setTypeface(Typeface.create(as.getFontFamily(), Typeface.NORMAL));

        if (as.isDarkThemeEnabled()) {
            _contentEditor.setBackgroundColor(getResources().getColor(R.color.dark_grey));
            _contentEditor.setTextColor(getResources().getColor(android.R.color.white));
            _view.findViewById(R.id.note__activity__scroll_markdownchar_bar).setBackgroundColor(getResources().getColor(R.color.dark_grey));
        } else {
            _contentEditor.setBackgroundColor(getResources().getColor(android.R.color.white));
            _contentEditor.setTextColor(getResources().getColor(R.color.dark_grey));
            _view.findViewById(R.id.note__activity__scroll_markdownchar_bar)
                    .setBackgroundColor(getResources().getColor(R.color.lighter_grey));
        }
    }

    private void appendMarkdownShortcutToBar(int shortcut, View.OnClickListener l) {
        ImageView btn = (ImageView) getLayoutInflater().inflate(R.layout.ui__quick_keyboard_button, (ViewGroup) null);
        btn.setImageResource(shortcut);
        btn.setOnClickListener(l);

        boolean isDarkTheme = AppSettings.get().isDarkThemeEnabled();
        btn.setColorFilter(ContextCompat.getColor(_context,
                isDarkTheme ? android.R.color.white : R.color.grey));
        _markdownShortcutBar.addView(btn);
    }

    @Override
    public String getFragmentTag() {
        return FRAGMENT_TAG;
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }

    /**
     * Save the file to its directory
     */
    private void saveNote() {
        String filename = normalizeTitleForFilename() + _document.getFileExtension();
        _document.setDoHistory(true);
        _document.setFile(new File(_document.getFile().getParentFile(), filename));

        Document documentInitial = _document.getInitialVersion();
        if (!_document.getFile().equals(documentInitial.getFile())) {
            if (documentInitial.getFile().exists()) {
                FileUtils.renameFile(documentInitial.getFile(), _document.getFile());
            }
        }

        if (!_document.getContent().equals(documentInitial.getContent())) {
            FileUtils.writeFile(_document.getFile(), _document.getContent());
        }
        _document.getHistory().clear();

        //TODO: updateWidgets();

    }


    public String normalizeTitleForFilename() {
        String name = _document.getTitle();
        if (name.length() == 0) {
            if (_document.getContent().length() == 0) {
                return null;
            } else {
                String contentL1 = _document.getContent().split("\n")[0];
                if (contentL1.length() < Constants.MAX_TITLE_EXTRACTION_LENGTH) {
                    name = contentL1.substring(0, contentL1.length());
                } else {
                    name = contentL1.substring(0, Constants.MAX_TITLE_EXTRACTION_LENGTH);
                }
            }
        }
        name = name.replaceAll("[\\\\/:\"*?<>|]+", "").trim();

        if (name.isEmpty()) {
            name = getString(R.string.document_one) + " " + UUID.randomUUID().toString();
        }
        return name;
    }

    //
    //
    //
    //


    private class KeyboardRegularActionListener implements View.OnClickListener {
        String _action;

        public KeyboardRegularActionListener(String action) {
            _action = action;
        }

        @Override
        public void onClick(View v) {

            if (_contentEditor.hasSelection()) {
                String text = _contentEditor.getText().toString();
                int selectionStart = _contentEditor.getSelectionStart();
                int selectionEnd = _contentEditor.getSelectionEnd();

                //Check if Selection includes the shortcut characters
                if (text.substring(selectionStart, selectionEnd)
                        .matches("(>|#{1,3}|-|[1-9]\\.)(\\s)?[a-zA-Z0-9\\s]*")) {

                    text = text.substring(selectionStart + _action.length(), selectionEnd);
                    _contentEditor.getText()
                            .replace(selectionStart, selectionEnd, text);

                }
                //Check if Selection is Preceded by shortcut characters
                else if ((selectionStart >= _action.length()) && (text.substring(selectionStart - _action.length(), selectionEnd)
                        .matches("(>|#{1,3}|-|[1-9]\\.)(\\s)?[a-zA-Z0-9\\s]*"))) {

                    text = text.substring(selectionStart, selectionEnd);
                    _contentEditor.getText()
                            .replace(selectionStart - _action.length(), selectionEnd, text);

                }
                //Condition to insert shortcut preceding the selection
                else {
                    _contentEditor.getText().insert(selectionStart, _action);
                }
            } else {
                //Condition for Empty Selection
                _contentEditor.getText().insert(_contentEditor.getSelectionStart(), _action);
            }
        }
    }

    private class KeyboardSmartActionsListener implements View.OnClickListener {
        String _action;

        public KeyboardSmartActionsListener(String action) {
            _action = action;
        }

        @Override
        public void onClick(View v) {
            saveNote();

            if (_contentEditor.hasSelection()) {
                String text = _contentEditor.getText().toString();
                int selectionStart = _contentEditor.getSelectionStart();
                int selectionEnd = _contentEditor.getSelectionEnd();

                //Check if Selection includes the shortcut characters
                if ((text.substring(selectionStart, selectionEnd)
                        .matches("(\\*\\*|~~|_|`)[a-zA-Z0-9\\s]*(\\*\\*|~~|_|`)"))) {

                    text = text.substring(selectionStart + _action.length(),
                            selectionEnd - _action.length());
                    _contentEditor.getText()
                            .replace(selectionStart, selectionEnd, text);

                }
                //Check if Selection is Preceded and succeeded by shortcut characters
                else if (((selectionEnd <= (_contentEditor.length() - _action.length())) &&
                        (selectionStart >= _action.length())) &&
                        (text.substring(selectionStart - _action.length(),
                                selectionEnd + _action.length())
                                .matches("(\\*\\*|~~|_|`)[a-zA-Z0-9\\s]*(\\*\\*|~~|_|`)"))) {

                    text = text.substring(selectionStart, selectionEnd);
                    _contentEditor.getText()
                            .replace(selectionStart - _action.length(),
                                    selectionEnd + _action.length(), text);

                }
                //Condition to insert shortcut preceding and succeeding the selection
                else {
                    _contentEditor.getText().insert(selectionStart, _action);
                    _contentEditor.getText().insert(_contentEditor.getSelectionEnd(), _action);
                }
            } else {
                //Condition for Empty Selection
                _contentEditor.getText().insert(_contentEditor.getSelectionStart(), _action)
                        .insert(_contentEditor.getSelectionEnd(), _action);
                _contentEditor.setSelection(_contentEditor.getSelectionStart() - _action.length());
            }
        }

    }

    private class KeyboardExtraActionsListener implements View.OnClickListener {
        int _action;

        public KeyboardExtraActionsListener(int action) {
            _action = action;
        }

        @Override
        public void onClick(View view) {
            getAlertDialog(_action);
        }
    }

    private void getAlertDialog(int action) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final View view = getLayoutInflater().inflate(R.layout.format_dialog, (ViewGroup) null);

        final EditText link_name = view.findViewById(R.id.format_dialog_name);
        final EditText link_url = view.findViewById(R.id.format_dialog_url);
        link_name.setHint(getString(R.string.format_dialog_name_hint));
        link_url.setHint(getString(R.string.format_dialog_url_or_path_hint));

        //Insert Link Action
        if (action == 1) {
            builder.setView(view)
                    .setTitle(getString(R.string.format_link_dialog_title))
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            _contentEditor.getText().insert(_contentEditor.getSelectionStart(),
                                    String.format("[%s](%s)", link_name.getText().toString(),
                                            link_url.getText().toString()));
                        }
                    });
        }
        //Insert Image Action
        else if (action == 2) {
            builder.setView(view)
                    .setTitle(getString(R.string.format_image_dialog_title))
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            _contentEditor.getText().insert(_contentEditor.getSelectionStart(),
                                    String.format("![%s](%s)", link_name.getText().toString(),
                                            link_url.getText().toString()));
                        }
                    });
        }

        builder.show();
    }

    public Document getDocument() {
        return _document;
    }
}
