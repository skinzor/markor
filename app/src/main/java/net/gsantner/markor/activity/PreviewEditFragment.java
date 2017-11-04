package net.gsantner.markor.activity;

import net.gsantner.markor.ui.BaseFragment;

/**
 * Created by gregor on 04.11.17.
 */

// TODO: Not implemented
public class PreviewEditFragment extends BaseFragment{
    public static final String FRAGMENT_TAG = "PreviewEditFragment";

    @Override
    public String getFragmentTag() {
        return FRAGMENT_TAG;
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }
}
