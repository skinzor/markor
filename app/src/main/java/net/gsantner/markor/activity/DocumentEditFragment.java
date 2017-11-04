package net.gsantner.markor.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.gsantner.markor.R;
import net.gsantner.markor.model.Document;
import net.gsantner.markor.ui.BaseFragment;

import java.io.File;

import butterknife.ButterKnife;

public class DocumentEditFragment extends BaseFragment {
    public static final String FRAGMENT_TAG = "DocumentEditFragment";
    private static final String EXTRA_PATH = "EXTRA_PATH";
    private static final String EXTRA_PATH_IS_FOLDER = "EXTRA_PATH_IS_FOLDER";

    public DocumentEditFragment newInstance(File path, boolean pathIsFolder) {
        DocumentEditFragment f = new DocumentEditFragment();
        Bundle args = new Bundle();
        args.putSerializable(EXTRA_PATH, path);
        args.putSerializable(EXTRA_PATH_IS_FOLDER, pathIsFolder);
        f.setArguments(args);
        return f;
    }

    private Document _document;

    public DocumentEditFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.document__fragment__edit, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        Context c = getContext();
        Bundle arguments = getArguments();
        if (arguments == null){
            return;
        }

        File extraPath = (File) arguments.getSerializable(EXTRA_PATH);
        if (_document == null) {
            _document = new Document();
        }
    }

    @Override
    public String getFragmentTag() {
        return FRAGMENT_TAG;
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }
}
