package org.wordpress.android.ui.themes;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.wordpress.android.R;
import org.wordpress.android.fluxc.model.ThemeModel;
import org.wordpress.android.ui.prefs.AppPrefs;
import org.wordpress.android.widgets.WPNetworkImageView;

import java.util.ArrayList;
import java.util.List;

public class ThemesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    interface OnThemeAction {
        int ACTION_ACTIVATE = 0;
        int ACTION_CUSTOMIZE = 1;
        int ACTION_VIEW = 2;
        int ACTION_DETAILS = 3;
        int ACTION_SUPPORT = 4;

        void onThemeClicked(String themeId);
        void onThemeAction(int menuItemId, String themeId);
        void onSearchClicked();
    }

    private static final String THEME_IMAGE_PARAMETER = "?w=";

    private final List<ThemeModel> mThemes;
    private final LayoutInflater mLayoutInflater;
    private final OnThemeAction mCallback;
    private final int mViewWidth;

    public ThemesAdapter(Context context, OnThemeAction callback, List<ThemeModel> themes) {
        mLayoutInflater = LayoutInflater.from(context);
        mCallback = callback;
        mThemes = new ArrayList<>();
        mViewWidth = AppPrefs.getThemeImageSizeWidth();

        if (themes != null) {
            mThemes.addAll(themes);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mLayoutInflater.inflate(R.layout.theme_grid_item, parent, false);
        return new ThemeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ThemeViewHolder themeViewHolder = (ThemeViewHolder) holder;
        final ThemeModel theme = mThemes.get(position);

        themeViewHolder.nameView.setText(theme.getName());

        String requestURL = (String) themeViewHolder.imageView.getTag();
        if (requestURL == null) {
            requestURL = theme.getScreenshotUrl();
            themeViewHolder.imageView.setDefaultImageResId(R.drawable.theme_loading);
            themeViewHolder.imageView.setTag(requestURL);
        }

        if (!requestURL.equals(theme.getScreenshotUrl())) {
            requestURL = theme.getScreenshotUrl();
        }

        String imageUrl = requestURL + THEME_IMAGE_PARAMETER + mViewWidth;
        themeViewHolder.imageView.setImageUrl(imageUrl, WPNetworkImageView.ImageType.PHOTO);
        themeViewHolder.frameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.onThemeClicked(theme.getThemeId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return mThemes.size();
    }

    private class ThemeViewHolder extends RecyclerView.ViewHolder {
        private final CardView cardView;
        private final WPNetworkImageView imageView;
        private final TextView nameView;
        private final TextView activeView;
        private final TextView priceView;
        private final ImageButton imageButton;
        private final FrameLayout frameLayout;
        private final RelativeLayout detailsView;

        public ThemeViewHolder(View view) {
            super(view);
            cardView = (CardView) view.findViewById(R.id.theme_grid_card);
            imageView = (WPNetworkImageView) view.findViewById(R.id.theme_grid_item_image);
            nameView = (TextView) view.findViewById(R.id.theme_grid_item_name);
            priceView = (TextView) view.findViewById(R.id.theme_grid_item_price);
            activeView = (TextView) view.findViewById(R.id.theme_grid_item_active);
            imageButton = (ImageButton) view.findViewById(R.id.theme_grid_item_image_button);
            frameLayout = (FrameLayout) view.findViewById(R.id.theme_grid_item_image_layout);
            detailsView = (RelativeLayout) view.findViewById(R.id.theme_grid_item_details);
        }
    }
}
