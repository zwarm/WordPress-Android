package org.wordpress.android.ui.themes;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;

import org.wordpress.android.R;
import org.wordpress.android.fluxc.model.SiteModel;
import org.wordpress.android.fluxc.model.ThemeModel;
import org.wordpress.android.ui.ActivityLauncher;
import org.wordpress.android.ui.WPWebViewActivity;
import org.wordpress.android.util.AppLog;
import org.wordpress.android.util.ToastUtils;

public class ThemeWebActivity extends WPWebViewActivity {
    public static final String IS_CURRENT_THEME = "is_current_theme";
    public static final String THEME_NAME = "theme_name";
    private static final String THEME_DOMAIN_PUBLIC = "pub";
    private static final String THEME_DOMAIN_PREMIUM = "premium";
    private static final String THEME_URL_PREVIEW = "%s/wp-admin/customize.php?theme=%s/%s&hide_close=true";
    private static final String THEME_URL_SUPPORT = "https://wordpress.com/themes/%s/support/?preview=true&iframe=true";
    private static final String THEME_URL_DETAILS = "https://wordpress.com/themes/%s/%s/?preview=true&iframe=true";
    private static final String THEME_URL_DEMO_PARAMETER = "demo=true&iframe=true&theme_preview=true";
    private static final String THEME_HTTPS_PREFIX = "https://";

    public enum ThemeWebActivityType {
        PREVIEW,
        DEMO,
        DETAILS,
        SUPPORT
    }

    public static void openTheme(Activity activity, SiteModel site, ThemeModel theme, ThemeWebActivityType type,
                                 boolean isCurrentTheme) {
        if (theme == null) {
            ToastUtils.showToast(activity, R.string.could_not_load_theme);
            return;
        }

        String url = getUrl(site, theme, type, false);
        if (type == ThemeWebActivityType.PREVIEW) {
            // Do not open the Customizer with the in-app browser.
            // Customizer may need to access local files (mostly pictures) on the device storage,
            // and our internal webview doesn't handle this feature yet.
            // Ref: https://github.com/wordpress-mobile/WordPress-Android/issues/4934
            ActivityLauncher.openUrlExternal(activity, url);
        } else {
            openWPCOMURL(activity, url, theme, site, isCurrentTheme);
        }
    }

    public static String getUrl(SiteModel site, ThemeModel theme, ThemeWebActivityType type, boolean isPremium) {
        String url = "";
        String homeURL = site.getUrl();
        String domain = isPremium ? THEME_DOMAIN_PREMIUM : THEME_DOMAIN_PUBLIC;

        switch (type) {
            case PREVIEW:
                url = String.format(THEME_URL_PREVIEW, homeURL, domain, theme.getThemeId());
                break;
            case DEMO:
                url = theme.getDemoUrl();
                if (url.contains("?")) {
                    url = url + "&" + THEME_URL_DEMO_PARAMETER;
                } else {
                    url = url + "?" + THEME_URL_DEMO_PARAMETER;
                }
                break;
            case DETAILS:
                String currentURL = homeURL.replaceFirst(THEME_HTTPS_PREFIX, "");
                url = String.format(THEME_URL_DETAILS, currentURL, theme.getThemeId());
                break;
            case SUPPORT:
                url = String.format(THEME_URL_SUPPORT, theme.getThemeId());
                break;
            default:
                break;
        }

        return url;
    }

    private static void openWPCOMURL(Activity activity, String url, ThemeModel theme,
                                     SiteModel site, boolean isCurrentTheme) {
        if (activity == null) {
            AppLog.e(AppLog.T.THEMES, "Context is null");
            return;
        }

        if (TextUtils.isEmpty(url)) {
            AppLog.e(AppLog.T.THEMES, "Empty or null URL passed to openWPCOMURL");
            ToastUtils.showToast(activity, R.string.invalid_site_url_message, ToastUtils.Duration.SHORT);
            return;
        }

        String authURL = getSiteLoginUrl(site);
        Intent intent = new Intent(activity, ThemeWebActivity.class);
        intent.putExtra(WPWebViewActivity.URL_TO_LOAD, url);
        intent.putExtra(WPWebViewActivity.AUTHENTICATION_URL, authURL);
        intent.putExtra(WPWebViewActivity.LOCAL_BLOG_ID, site.getId());
        intent.putExtra(WPWebViewActivity.USE_GLOBAL_WPCOM_USER, true);
        intent.putExtra(IS_CURRENT_THEME, isCurrentTheme);
        intent.putExtra(THEME_NAME, theme.getName());
        intent.putExtra(ThemeBrowserActivity.THEME_ID, theme.getThemeId());

        activity.startActivityForResult(intent, ThemeBrowserActivity.ACTIVATE_THEME);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // don't add menu for current theme because the only action is to activate a theme
        if (!getIntent().getBooleanExtra(IS_CURRENT_THEME, false)) {
            getMenuInflater().inflate(R.menu.theme_web, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_activate) {
            Intent intent = new Intent();
            setResult(RESULT_OK, intent);
            intent.putExtra(ThemeBrowserActivity.THEME_ID, getIntent().getStringExtra(ThemeBrowserActivity.THEME_ID));
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void configureView() {
        setContentView(R.layout.theme_web_activity);

        // update action bar title
        String themeName = getIntent().getStringExtra(THEME_NAME);
        if (getSupportActionBar() != null && themeName != null) {
            getSupportActionBar().setTitle(themeName);
        }
    }
}
