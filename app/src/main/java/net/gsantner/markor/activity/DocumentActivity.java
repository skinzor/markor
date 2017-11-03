package net.gsantner.markor.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import net.gsantner.markor.R;
import net.gsantner.markor.ui.BaseFragment;
import net.gsantner.markor.util.AndroidBug5497Workaround;
import net.gsantner.markor.util.AppSettings;
import net.gsantner.markor.util.ContextUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DocumentActivity extends AppCompatActivity {

    @BindView(R.id.document__placeholder_fragment)
    FrameLayout _fragPlaceholder;

    @BindView(R.id.toolbar)
    Toolbar _toolbar;
    @BindView(R.id.note__activity__header_view_switcher)
    ViewSwitcher _toolbarSwitcher;
    @BindView(R.id.note__activity__edit_note_title)
    EditText _toolbarTitleEdit;
    @BindView(R.id.note__activity__text_note_title)
    TextView _toolbarTitleText;

    private FragmentManager _fragManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ContextUtils.get().setAppLanguage(AppSettings.get().getLanguage());
        AppSettings _appSettings = AppSettings.get();
        if (_appSettings.isEditorStatusBarHidden()) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        setContentView(R.layout.document__activity);
        ButterKnife.bind(this);
        if (_appSettings.isEditorStatusBarHidden()) {
            AndroidBug5497Workaround.assistActivity(this);
        }

        setSupportActionBar(_toolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setHomeAsUpIndicator(ContextCompat.getDrawable(this, R.drawable.ic_arrow_back_white_24dp));
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setDisplayShowTitleEnabled(false);
        }

        _fragManager = getSupportFragmentManager();
        showEditor();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.document__menu, menu);
        ContextUtils cu = ContextUtils.get();


        cu.tintMenuItems(menu, true, Color.WHITE);
        cu.setSubMenuIconsVisiblity(menu, true);
        return true;
    }

    public void showFragment(BaseFragment fragment) {
        BaseFragment currentTop = (BaseFragment) _fragManager.findFragmentById(R.id.main__activity__fragment_placeholder);

        if (currentTop == null || !currentTop.getFragmentTag().equals(fragment.getFragmentTag())) {
            _fragManager.beginTransaction().addToBackStack(null).replace(R.id.document__placeholder_fragment
                    , fragment, fragment.getFragmentTag()).commit();
        }
        supportInvalidateOptionsMenu();
    }

    public void setDocumentTitle(final String title) {
        _toolbarTitleEdit.setText(title);
        _toolbarTitleText.setText(title);
    }

    public void showEditor() {
        setDocumentTitle("hello world");
        Toast.makeText(this, getTitle(), Toast.LENGTH_LONG).show();
        showFragment(new DocumentEditFragment());
    }
}
