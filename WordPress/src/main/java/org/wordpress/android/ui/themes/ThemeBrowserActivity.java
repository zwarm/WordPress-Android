package org.wordpress.android.ui.themes;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;

import com.android.volley.AuthFailureError;
import com.android.volley.VolleyError;
import com.wordpress.rest.RestRequest.ErrorListener;
import com.wordpress.rest.RestRequest.Listener;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONObject;
import org.wordpress.android.R;
import org.wordpress.android.WordPress;
import org.wordpress.android.analytics.AnalyticsTracker;
import org.wordpress.android.fluxc.Dispatcher;
import org.wordpress.android.fluxc.generated.ThemeActionBuilder;
import org.wordpress.android.fluxc.model.SiteModel;
import org.wordpress.android.fluxc.model.ThemeModel;
import org.wordpress.android.fluxc.store.ThemeStore;
import org.wordpress.android.fluxc.store.ThemeStore.OnThemeActivated;
import org.wordpress.android.fluxc.store.ThemeStore.ActivateThemePayload;
import org.wordpress.android.ui.ActivityId;
import org.wordpress.android.ui.themes.ThemeBrowserFragment.ThemeBrowserFragmentCallback;
import org.wordpress.android.util.AnalyticsUtils;
import org.wordpress.android.util.AppLog;
import org.wordpress.android.util.AppLog.T;
import org.wordpress.android.util.NetworkUtils;
import org.wordpress.android.util.ToastUtils;
import org.wordpress.android.widgets.WPAlertDialogFragment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

public class ThemeBrowserActivity extends AppCompatActivity implements ThemeBrowserFragmentCallback {
    public static final int THEME_FETCH_MAX = 100;
    public static final int ACTIVATE_THEME = 1;
    public static final String THEME_ID = "theme_id";

    private static final String IS_IN_SEARCH_MODE = "is_in_search_mode";
    private static final String ALERT_TAB = "alert";

    private boolean mFetchingThemes = false;
    private boolean mIsRunning;
    private ThemeBrowserFragment mThemeBrowserFragment;
    private ThemeSearchFragment mThemeSearchFragment;
    private boolean mIsInSearchMode;

    private SiteModel mSite;
    private ThemeModel mCurrentTheme;

    @Inject private Dispatcher mDispatcher;
    @Inject private ThemeStore mThemeStore;

    /** Theme browsing is only supported on WP.com sites and Jetpack sites using the WP.com REST API. */
    public static boolean isAccessible(SiteModel site) {
        return site != null && site.isUsingWpComRestApi() && site.getHasCapabilityEditThemeOptions();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            mSite = (SiteModel) getIntent().getSerializableExtra(WordPress.SITE);
        } else {
            mSite = (SiteModel) savedInstanceState.getSerializable(WordPress.SITE);
            mIsInSearchMode = savedInstanceState.getBoolean(IS_IN_SEARCH_MODE);
        }

        if (mSite == null) {
            ToastUtils.showToast(this, R.string.blog_not_found, ToastUtils.Duration.SHORT);
            finish();
            return;
        }

        ((WordPress) getApplication()).component().inject(this);

        setContentView(R.layout.theme_browser_activity);

        if (savedInstanceState == null) {
            AnalyticsUtils.trackWithSiteDetails(AnalyticsTracker.Stat.THEMES_ACCESSED_THEMES_BROWSER, mSite);
            mThemeBrowserFragment = ThemeBrowserFragment.newInstance(mSite);
            mThemeSearchFragment = ThemeSearchFragment.newInstance(mSite);
            addBrowserFragment();
        }

        setCurrentThemeFromDB();
        showToolbar();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mDispatcher.register(this);
        mIsRunning = true;
        showCorrectToolbar();
        ActivityId.trackLastActivity(ActivityId.THEMES);
        fetchThemesIfNoneAvailable();
        fetchPurchasedThemes();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mDispatcher.unregister(this);
        mIsRunning = false;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(IS_IN_SEARCH_MODE, mIsInSearchMode);
        outState.putSerializable(WordPress.SITE, mSite);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        FragmentManager fm = getFragmentManager();
        if (fm.getBackStackEntryCount() > 0) {
            fm.popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ACTIVATE_THEME && resultCode == RESULT_OK && data != null) {
            String themeId = data.getStringExtra(THEME_ID);
            if (!TextUtils.isEmpty(themeId)) {
                activateTheme(themeId);
            }
        }
    }

    @Override
    public void onActivateSelected(String themeId) {
        activateTheme(themeId);
    }

    @Override
    public void onTryAndCustomizeSelected(String themeId) {
        startWebActivity(themeId, ThemeWebActivity.ThemeWebActivityType.PREVIEW);
    }

    @Override
    public void onViewSelected(String themeId) {
        startWebActivity(themeId, ThemeWebActivity.ThemeWebActivityType.DEMO);
    }

    @Override
    public void onDetailsSelected(String themeId) {
        startWebActivity(themeId, ThemeWebActivity.ThemeWebActivityType.DETAILS);
    }

    @Override
    public void onSupportSelected(String themeId) {
        startWebActivity(themeId, ThemeWebActivity.ThemeWebActivityType.SUPPORT);
    }

    @Override
    public void onSearchClicked() {
        mIsInSearchMode = true;
        AnalyticsUtils.trackWithSiteDetails(AnalyticsTracker.Stat.THEMES_ACCESSED_SEARCH, mSite);
        addSearchFragment();
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onThemeActivated(OnThemeActivated event) {
        if (event.isError()) {
            AppLog.e(T.THEMES, "Error activating theme: " + event.error.message);
            ToastUtils.showToast(this, R.string.theme_activation_error, ToastUtils.Duration.SHORT);
        } else {
            mCurrentTheme = event.theme;

            Map<String, Object> themeProperties = new HashMap<>();
            themeProperties.put(THEME_ID, mCurrentTheme.getThemeId());
            AnalyticsUtils.trackWithSiteDetails(AnalyticsTracker.Stat.THEMES_CHANGED_THEME, mSite, themeProperties);

            if (!isFinishing()) {
                showAlertDialogOnNewSettingNewTheme(mCurrentTheme);
            }
        }
    }

    public void setIsInSearchMode(boolean isInSearchMode) {
        mIsInSearchMode = isInSearchMode;
    }

    public void fetchThemes() {
    }

    public void searchThemes(String searchTerm) {
        mFetchingThemes = true;
        int page = 1;
        if (mThemeSearchFragment != null) {
            page = mThemeSearchFragment.getPage();
        }

        WordPress.getRestClientUtilsV1_2().getFreeSearchThemes(mSite.getSiteId(), THEME_FETCH_MAX, page, searchTerm,
                new Listener() {
                    @Override
                    public void onResponse(JSONObject response) {
                    }
                }, new ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError response) {
                        if (response.toString().equals(AuthFailureError.class.getName())) {
                            String errorTitle = getString(R.string.theme_auth_error_title);
                            String errorMsg = getString(R.string.theme_auth_error_message);

                            if (mIsRunning) {
                                FragmentTransaction ft = getFragmentManager().beginTransaction();
                                WPAlertDialogFragment fragment = WPAlertDialogFragment.newAlertDialog(errorMsg,
                                        errorTitle);
                                ft.add(fragment, ALERT_TAB);
                                ft.commitAllowingStateLoss();
                            }
                            AppLog.d(T.THEMES, getString(R.string.theme_auth_error_authenticate));
                        }
                        mFetchingThemes = false;
                    }
                }
        );
    }

    public void fetchCurrentTheme() {
    }

    protected ThemeModel getCurrentTheme() {
        return mCurrentTheme;
    }

    protected void setThemeBrowserFragment(ThemeBrowserFragment themeBrowserFragment) {
        mThemeBrowserFragment = themeBrowserFragment;
    }

    protected void setThemeSearchFragment(ThemeSearchFragment themeSearchFragment) {
        mThemeSearchFragment = themeSearchFragment;
    }

    protected void showToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.themes);
            findViewById(R.id.toolbar).setVisibility(View.VISIBLE);
            findViewById(R.id.toolbar_search).setVisibility(View.GONE);
        }
    }

    private void setCurrentThemeFromDB() {
    }

    private void fetchThemesIfNoneAvailable() {
    }

    private void fetchPurchasedThemes() {
        if (NetworkUtils.isNetworkAvailable(this)) {
            WordPress.getRestClientUtilsV1_1().getPurchasedThemes(mSite.getSiteId(), new Listener() {
                @Override
                public void onResponse(JSONObject response) {
                }
            }, new ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    AppLog.d(T.THEMES, error.getMessage());
                }
            });

            //do not interact with theme browser fragment if we are in search mode
            if (!mIsInSearchMode) {
                mThemeBrowserFragment.setRefreshing(true);
            }
        }
    }

    private void showCorrectToolbar() {
        if (mIsInSearchMode) {
            showSearchToolbar();
        } else {
            hideSearchToolbar();
        }
    }

    private void showSearchToolbar() {
        Toolbar toolbarSearch = (Toolbar) findViewById(R.id.toolbar_search);
        setSupportActionBar(toolbarSearch);
        toolbarSearch.setTitle("");
        findViewById(R.id.toolbar).setVisibility(View.GONE);
        findViewById(R.id.toolbar_search).setVisibility(View.VISIBLE);
    }

    private void hideSearchToolbar() {
        findViewById(R.id.toolbar).setVisibility(View.VISIBLE);
        findViewById(R.id.toolbar_search).setVisibility(View.GONE);
    }

    private void addBrowserFragment() {
        if (mThemeBrowserFragment == null) {
            mThemeBrowserFragment = new ThemeBrowserFragment();
        }
        showToolbar();
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.theme_browser_container, mThemeBrowserFragment);
        fragmentTransaction.commit();
    }

    private void addSearchFragment() {
        if (mThemeSearchFragment == null) {
            mThemeSearchFragment = ThemeSearchFragment.newInstance(mSite);
        }
        showSearchToolbar();
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.theme_browser_container, mThemeSearchFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    // TODO: this should be in FluxC
    private ThemeModel getThemeFromThemeId(final @NonNull String themeId) {
        List<ThemeModel> themes = mThemeStore.getWpThemes();
        for (ThemeModel theme : themes) {
            if (themeId.equals(theme.getThemeId())) {
                return theme;
            }
        }

        return null;
    }

    private void activateTheme(final String themeId) {
        final ThemeModel theme = getThemeFromThemeId(themeId);
        if (theme != null) {
            ActivateThemePayload payload = new ActivateThemePayload(mSite, theme);
            mDispatcher.dispatch(ThemeActionBuilder.newActivateThemeAction(payload));
        } else {
            AppLog.w(T.THEMES, "Could not find theme to activate: themeId=" + themeId);
            ToastUtils.showToast(this, R.string.theme_activation_error, ToastUtils.Duration.SHORT);
        }
    }

    private void showAlertDialogOnNewSettingNewTheme(ThemeModel newTheme) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);

        String thanksMessage = String.format(getString(R.string.theme_prompt), newTheme.getName());
        if (!newTheme.getAuthorName().isEmpty()) {
            String append = String.format(getString(R.string.theme_by_author_prompt_append), newTheme.getAuthorName());
            thanksMessage = thanksMessage + " " + append;
        }

        dialogBuilder.setMessage(thanksMessage);
        dialogBuilder.setNegativeButton(R.string.theme_done, null);
        dialogBuilder.setPositiveButton(R.string.theme_manage_site, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }

    private void startWebActivity(String themeId, ThemeWebActivity.ThemeWebActivityType type) {
        String toastText = getString(R.string.no_network_message);

        if (NetworkUtils.isNetworkAvailable(this)) {
            if (mCurrentTheme != null && !TextUtils.isEmpty(themeId)) {
                boolean isCurrentTheme = mCurrentTheme.getThemeId().equals(themeId);
                Map<String, Object> themeProperties = new HashMap<>();
                themeProperties.put(THEME_ID, themeId);

                switch (type) {
                    case PREVIEW:
                        AnalyticsUtils.trackWithSiteDetails(AnalyticsTracker.Stat.THEMES_PREVIEWED_SITE,
                                mSite, themeProperties);
                        break;
                    case DEMO:
                        AnalyticsUtils.trackWithSiteDetails(AnalyticsTracker.Stat.THEMES_DEMO_ACCESSED,
                                mSite, themeProperties);
                        break;
                    case DETAILS:
                        AnalyticsUtils.trackWithSiteDetails(AnalyticsTracker.Stat.THEMES_DETAILS_ACCESSED,
                                mSite, themeProperties);
                        break;
                    case SUPPORT:
                        AnalyticsUtils.trackWithSiteDetails(AnalyticsTracker.Stat.THEMES_SUPPORT_ACCESSED,
                                mSite, themeProperties);
                        break;
                }
                return;
            } else {
                toastText = getString(R.string.could_not_load_theme);
            }
        }

        ToastUtils.showToast(this, toastText, ToastUtils.Duration.SHORT);
    }

}
