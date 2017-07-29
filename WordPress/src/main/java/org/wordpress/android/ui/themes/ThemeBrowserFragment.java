package org.wordpress.android.ui.themes;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.RecyclerListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader.ImageContainer;
import com.android.volley.toolbox.ImageLoader.ImageListener;

import org.wordpress.android.R;
import org.wordpress.android.WordPress;
import org.wordpress.android.datasets.ThemeTable;
import org.wordpress.android.fluxc.model.SiteModel;
import org.wordpress.android.models.Theme;
import org.wordpress.android.util.NetworkUtils;
import org.wordpress.android.util.ToastUtils;
import org.wordpress.android.util.helpers.SwipeToRefreshHelper;
import org.wordpress.android.util.helpers.SwipeToRefreshHelper.RefreshListener;
import org.wordpress.android.util.widgets.CustomSwipeRefreshLayout;
import org.wordpress.android.widgets.HeaderGridView;
import org.wordpress.android.widgets.WPNetworkImageView;

/**
 * A fragment display the themes on a grid view.
 */
public class ThemeBrowserFragment extends Fragment implements RecyclerListener, AbsListView.OnScrollListener {
    protected static final String BUNDLE_PAGE = "BUNDLE_PAGE";

    protected SwipeToRefreshHelper mSwipeToRefreshHelper;
    protected ThemeBrowserActivity mThemeBrowserActivity;
    private String mCurrentThemeId;
    private HeaderGridView mGridView;
    private RelativeLayout mEmptyView;
    private TextView mNoResultText;
    private TextView mCurrentThemeTextView;
    private ThemeBrowserAdapter mAdapter;
    private ThemeBrowserAdapter.ThemeBrowserCallback mCallback;
    private int mPage = 1;
    private boolean mShouldRefreshOnStart;
    private TextView mEmptyTextView;
    private ProgressBar mProgressBar;

    private SiteModel mSite;

    public static ThemeBrowserFragment newInstance(SiteModel site) {
        ThemeBrowserFragment fragment = new ThemeBrowserFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(WordPress.SITE, site);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            mSite = (SiteModel) getArguments().getSerializable(WordPress.SITE);
        } else {
            mSite = (SiteModel) savedInstanceState.getSerializable(WordPress.SITE);
        }

        if (mSite == null) {
            ToastUtils.showToast(getActivity(), R.string.blog_not_found, ToastUtils.Duration.SHORT);
            getActivity().finish();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mCallback = (ThemeBrowserAdapter.ThemeBrowserCallback) context;
            mThemeBrowserActivity = (ThemeBrowserActivity) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement ThemeBrowserCallback");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallback = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.theme_browser_fragment, container, false);

        setRetainInstance(true);
        mNoResultText = (TextView) view.findViewById(R.id.theme_no_search_result_text);
        mEmptyTextView = (TextView) view.findViewById(R.id.text_empty);
        mEmptyView = (RelativeLayout) view.findViewById(R.id.empty_view);
        mProgressBar = (ProgressBar) view.findViewById(R.id.theme_loading_progress_bar);

        configureGridView(inflater, view);
        configureSwipeToRefresh(view);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (this instanceof ThemeSearchFragment) {
            mThemeBrowserActivity.setThemeSearchFragment((ThemeSearchFragment) this);
        } else {
            mThemeBrowserActivity.setThemeBrowserFragment(this);
        }
        Cursor cursor = fetchThemes(getSpinnerPosition());

        if (cursor == null) {
            return;
        }

        mAdapter = new ThemeBrowserAdapter(mThemeBrowserActivity, mCallback);
        setEmptyViewVisible(mAdapter.getCount() == 0);
        mGridView.setAdapter(mAdapter);
        restoreState(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        mThemeBrowserActivity.fetchCurrentTheme();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mGridView != null) {
            outState.putInt(BUNDLE_PAGE, mPage);
        }
        outState.putSerializable(WordPress.SITE, mSite);
    }

    public TextView getEmptyTextView() {
        return mEmptyTextView;
    }

    public TextView getCurrentThemeTextView() {
        return mCurrentThemeTextView;
    }

    public void setCurrentThemeId(String currentThemeId) {
        mCurrentThemeId = currentThemeId;
    }

    public int getPage() {
        return mPage;
    }

    protected void addHeaderViews(LayoutInflater inflater) {
        addMainHeader(inflater);
        configureAndAddSearchHeader(inflater);
    }

    protected void configureSwipeToRefresh(View view) {
        mSwipeToRefreshHelper = new SwipeToRefreshHelper(mThemeBrowserActivity, (CustomSwipeRefreshLayout) view.findViewById(
                R.id.ptr_layout), new RefreshListener() {
            @Override
            public void onRefreshStarted() {
                if (!isAdded()) {
                    return;
                }
                if (!NetworkUtils.checkConnection(mThemeBrowserActivity)) {
                    mSwipeToRefreshHelper.setRefreshing(false);
                    mEmptyTextView.setText(R.string.no_network_title);
                    return;
                }
            }
        });
        mSwipeToRefreshHelper.setRefreshing(mShouldRefreshOnStart);
    }

    private void configureGridView(LayoutInflater inflater, View view) {
        mGridView = (HeaderGridView) view.findViewById(R.id.theme_listview);
        addHeaderViews(inflater);
        mGridView.setRecyclerListener(this);
        mGridView.setOnScrollListener(this);
    }

    private void addMainHeader(LayoutInflater inflater) {
        @SuppressLint("InflateParams")
        View header = inflater.inflate(R.layout.theme_grid_cardview_header, null);
        mCurrentThemeTextView = (TextView) header.findViewById(R.id.header_theme_text);

        setThemeNameIfAlreadyAvailable();
        mThemeBrowserActivity.fetchCurrentTheme();
        LinearLayout customize = (LinearLayout) header.findViewById(R.id.customize);
        customize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.onTryAndCustomizeSelected(mCurrentThemeId);
            }
        });

        LinearLayout details = (LinearLayout) header.findViewById(R.id.details);
        details.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.onDetailsSelected(mCurrentThemeId);
            }
        });

        LinearLayout support = (LinearLayout) header.findViewById(R.id.support);
        support.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.onSupportSelected(mCurrentThemeId);
            }
        });

        mGridView.addHeaderView(header);
    }

    private void setThemeNameIfAlreadyAvailable() {
        Theme currentTheme = mThemeBrowserActivity.getCurrentTheme();
        if (currentTheme != null) {
            mCurrentThemeTextView.setText(currentTheme.getName());
        }
    }

    public void setRefreshing(boolean refreshing) {
        mShouldRefreshOnStart = refreshing;
        if (mSwipeToRefreshHelper != null) {
            mSwipeToRefreshHelper.setRefreshing(refreshing);
            if (!refreshing) {
            }
        }
    }

    private void configureAndAddSearchHeader(LayoutInflater inflater) {
        @SuppressLint("InflateParams")
        View headerSearch = inflater.inflate(R.layout.theme_grid_cardview_header_search, null);
        headerSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.onSearchClicked();
            }
        });
        mGridView.addHeaderView(headerSearch);
        ImageButton searchButton = (ImageButton) headerSearch.findViewById(R.id.theme_search);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.onSearchClicked();
            }
        });
    }

    private void restoreState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mPage = savedInstanceState.getInt(BUNDLE_PAGE, 1);
        }
    }

    private void setEmptyViewVisible(boolean visible) {
        if (getView() == null || !isAdded()) {
            return;
        }
        mEmptyView.setVisibility(visible ? RelativeLayout.VISIBLE : RelativeLayout.GONE);
        mGridView.setVisibility(visible ? View.GONE : View.VISIBLE);
        if (visible && !NetworkUtils.isNetworkAvailable(mThemeBrowserActivity)) {
            mEmptyTextView.setText(R.string.no_network_title);
        }
    }

    protected void refreshView(int position) {
        Cursor cursor = fetchThemes(position);
        if (cursor == null) {
            return;
        }
        if (mAdapter == null) {
            mAdapter = new ThemeBrowserAdapter(mThemeBrowserActivity, mCallback);
        }
        if (mNoResultText.isShown()) {
            mNoResultText.setVisibility(View.GONE);
        }
        mAdapter.notifyDataSetChanged();
        setEmptyViewVisible(mAdapter.getCount() == 0);
        mProgressBar.setVisibility(View.GONE);
    }

    private boolean shouldFetchThemesOnScroll(int lastVisibleCount, int totalItemCount) {
        if (totalItemCount < ThemeBrowserActivity.THEME_FETCH_MAX) {
            return false;
        } else {
            int numberOfColumns = mGridView.getNumColumns();
            return lastVisibleCount >= totalItemCount - numberOfColumns;
        }
    }

    @Override
    public void onMovedToScrapHeap(View view) {
        // cancel image fetch requests if the view has been moved to recycler.
        WPNetworkImageView niv = (WPNetworkImageView) view.findViewById(R.id.theme_grid_item_image);
        if (niv != null) {
            // this tag is set in the ThemeBrowserAdapter class
            String requestUrl = (String) niv.getTag();
            if (requestUrl != null) {
                // need a listener to cancel request, even if the listener does nothing
                ImageContainer container = WordPress.sImageLoader.get(requestUrl, new ImageListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }

                    @Override
                    public void onResponse(ImageContainer response, boolean isImmediate) {
                    }

                });
                container.cancelRequest();
            }
        }
    }

    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (mSpinner != null) {
            refreshView(position);
        }
    }

    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (shouldFetchThemesOnScroll(firstVisibleItem + visibleItemCount, totalItemCount) && NetworkUtils.isNetworkAvailable(getActivity())) {
            mPage++;
            mThemeBrowserActivity.fetchThemes();
            mProgressBar.setVisibility(View.VISIBLE);
        }
    }
}
