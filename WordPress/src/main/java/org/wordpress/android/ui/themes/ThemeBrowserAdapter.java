package org.wordpress.android.ui.themes;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.wordpress.android.R;
import org.wordpress.android.models.Theme;
import org.wordpress.android.ui.prefs.AppPrefs;
import org.wordpress.android.widgets.HeaderGridView;
import org.wordpress.android.widgets.WPNetworkImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Displays a list of themes. Provides methods to add header rows to split the theme list into sections.
 */
class ThemeBrowserAdapter extends ArrayAdapter<Theme> {
    private static final String HEADER_ITEM_ID = "__wpandroid_list_header_item_";
    private static final int HEADER_TYPE = 0;
    private static final int THEME_TYPE = 1;

    interface ThemeBrowserCallback {
        void onActivateSelected(String themeId);
        void onTryAndCustomizeSelected(String themeId);
        void onViewSelected(String themeId);
        void onDetailsSelected(String themeId);
        void onSupportSelected(String themeId);
        void onSearchClicked();
    }

    private static final String THEME_IMAGE_PARAMETER = "?w=";

    private final LayoutInflater mInflater;
    private final ThemeBrowserCallback mCallback;
    private final List<List<Theme>> mSections = new ArrayList<>();

    private int mViewWidth;

    ThemeBrowserAdapter(Context context, ThemeBrowserCallback callback, @NonNull List<List<Theme>> sections) {
        super(context, R.layout.theme_grid_item);
        mInflater = LayoutInflater.from(context);
        mCallback = callback;
        mViewWidth = AppPrefs.getThemeImageSizeWidth();
        mSections.addAll(sections);

        if (getCount() == 0) {
            throw new IllegalArgumentException("ThemeBrowserAdapter must be initialize with at least 1 section");
        }
    }

    @Override
    public int getViewTypeCount() {
        return 2; // header rows and Theme item rows
    }

    @Override
    public int getCount() {
        int sum = 0;
        for (List<Theme> section : mSections) {
            sum += section.size();
        }
        return sum;
    }

    @Override
    public Theme getItem(int position) {
        int sum = 0;
        int i = 0;
        while (i < mSections.size() && sum + getSection(i).size() > position) {
            sum += getSection(i).size();
        }
        return mSections.get(i).get(sum - position);
    }

    @Override
    public int getItemViewType(int position) {
        Theme item = getItem(position);
        if (item != null && HEADER_ITEM_ID.equals(item.getId())) {
            return HEADER_TYPE;
        }
        return THEME_TYPE;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        final Theme item = getItem(position);
        if (item == null) {
            return convertView;
        }

        if (getItemViewType(position) == HEADER_TYPE) {
            if (convertView == null) {
                convertView = new TextView(getContext());
            }
            configureSectionHeaderView(item, (TextView) convertView);
        } else {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.theme_grid_item, parent);
            }
            configureThemeItemView(convertView, item);
        }
        return convertView;
    }

    public List<Theme> getSection(int position) {
        if (position > 0 && position < mSections.size()) {
            return mSections.get(position);
        }

        return null;
    }

    public void addSection(List<Theme> section, String sectionHeader, int position) {
        if (section == null || section.size() <= 0) {
            return;
        }

        // add a header item if a title is provided
        if (!TextUtils.isEmpty(sectionHeader)) {
        }
        mSections.add(position, section);
    }

    public int getSectionItemCount(int section) {
        if (section < 0 || section > mSections.size()) {
            return 0;
        }

        // calculate the number of items in a section
        int size = getSection(section).size();
        if (sectionHasHeader(section)) {
            // must subtract one for the placeholder header item
            --size;
        }
        return size;
    }

    public boolean sectionHasHeader(int section) {
        return getSection(section) != null && HEADER_ITEM_ID.equals(mSections.get(section).get(0).getId());
    }

    public void addSection(List<Theme> section, String sectionHeader) {
        addSection(section, sectionHeader, mSections.size());
    }

    private void configureSectionHeaderView(@NonNull final Theme theme, @NonNull TextView sectionHeaderView) {
        sectionHeaderView.setText(theme.getName());
    }

    private void configureThemeItemView(@NonNull View root, @NonNull final Theme theme) {
        configureImageView(root, theme);
        configureImageButton(root, theme);
        configureCardView(root, theme);
    }

    @SuppressWarnings("deprecation") // suppress getColor deprecation warning until min API is 23+
    private void configureCardView(@NonNull final View root, @NonNull final Theme theme) {
        final TextView activeView = (TextView) root.findViewById(R.id.theme_grid_item_active);
        final CardView cardView = (CardView) root.findViewById(R.id.theme_grid_card);
        final TextView priceView = (TextView) root.findViewById(R.id.theme_grid_item_price);
        final RelativeLayout detailsView = (RelativeLayout) root.findViewById(R.id.theme_grid_item_details);
        final TextView nameView = (TextView) root.findViewById(R.id.theme_grid_item_name);
        final Resources resources = getContext().getResources();

        nameView.setText(theme.getName());
        priceView.setText(theme.getPrice());

        if (theme.getIsCurrent()) {
            detailsView.setBackgroundColor(resources.getColor(R.color.blue_wordpress));
            nameView.setTextColor(resources.getColor(R.color.white));
            activeView.setVisibility(View.VISIBLE);
            cardView.setCardBackgroundColor(resources.getColor(R.color.blue_wordpress));
        } else {
            detailsView.setBackgroundColor(resources.getColor(
                    android.support.v7.cardview.R.color.cardview_light_background));
            nameView.setTextColor(resources.getColor(R.color.black));
            activeView.setVisibility(View.GONE);
            cardView.setCardBackgroundColor(resources.getColor(
                    android.support.v7.cardview.R.color.cardview_light_background));
        }
    }

    private void configureImageView(@NonNull final View root, @NonNull final Theme theme) {
        final WPNetworkImageView imageView = (WPNetworkImageView) root.findViewById(R.id.theme_grid_item_image);
        final FrameLayout frame = (FrameLayout) root.findViewById(R.id.theme_grid_item_image_layout);

        String requestURL = (String) imageView.getTag();
        if (requestURL == null) {
            requestURL = theme.getScreenshot();
            imageView.setDefaultImageResId(R.drawable.theme_loading);
            imageView.setTag(requestURL);
        }

        if (!requestURL.equals(theme.getScreenshot())) {
            requestURL = theme.getScreenshot();
        }

        imageView.setImageUrl(requestURL + THEME_IMAGE_PARAMETER + mViewWidth, WPNetworkImageView.ImageType.PHOTO);

        frame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (theme.getIsCurrent()) {
                    mCallback.onTryAndCustomizeSelected(theme.getId());
                } else {
                    mCallback.onViewSelected(theme.getId());
                }
            }
        });
    }

    private void configureImageButton(@NonNull final View root, @NonNull final Theme theme) {
        final ImageButton imageButton = (ImageButton) root.findViewById(R.id.theme_grid_item_image_button);
        final PopupMenu popupMenu = new PopupMenu(getContext(), imageButton);

        popupMenu.getMenuInflater().inflate(R.menu.theme_more, popupMenu.getMenu());
        configureMenuForTheme(popupMenu.getMenu(), theme.getIsCurrent());

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                final String themeId = theme.getId();
                int i = item.getItemId();
                if (i == R.id.menu_activate) {
                    if (theme.isPremium()) {
                        mCallback.onDetailsSelected(themeId);
                    } else {
                        mCallback.onActivateSelected(themeId);
                    }
                } else if (i == R.id.menu_try_and_customize) {
                    mCallback.onTryAndCustomizeSelected(themeId);
                } else if (i == R.id.menu_view) {
                    mCallback.onViewSelected(themeId);
                } else if (i == R.id.menu_details) {
                    mCallback.onDetailsSelected(themeId);
                } else {
                    mCallback.onSupportSelected(themeId);
                }

                return true;
            }
        });
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupMenu.show();
            }
        });
    }

    private void configureMenuForTheme(Menu menu, boolean isCurrent) {
        MenuItem activate = menu.findItem(R.id.menu_activate);
        MenuItem customize = menu.findItem(R.id.menu_try_and_customize);
        MenuItem view = menu.findItem(R.id.menu_view);

        if (activate != null) {
            activate.setVisible(!isCurrent);
        }
        if (customize != null) {
            if (isCurrent) {
                customize.setTitle(R.string.customize);
            } else {
                customize.setTitle(R.string.theme_try_and_customize);
            }
        }
        if (view != null) {
            view.setVisible(!isCurrent);
        }
    }

    private void configureThemeImageSize(ViewGroup parent) {
        HeaderGridView gridView = (HeaderGridView) parent.findViewById(R.id.theme_listview);
        int numColumns = gridView.getNumColumns();
        int screenWidth = gridView.getWidth();
        int imageWidth = screenWidth / numColumns;
        if (imageWidth > mViewWidth) {
            mViewWidth = imageWidth;
            AppPrefs.setThemeImageSizeWidth(mViewWidth);
        }
    }
}
