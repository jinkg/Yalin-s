package com.jin.fidotest.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;


import com.jin.fidotest.R;
import com.jin.fidotest.data.User;
import com.jin.fidotest.widget.NavDrawerItemView;

import java.util.ArrayList;

/**
 * Created by YaLin on 2015/12/23.
 */
public abstract class BaseActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener, OnClickListener {
    private static final Interpolator INTERPOLATOR = new DecelerateInterpolator();
    private static final int NAVDRAWER_LAUNCH_DELAY = 250;
    private static final int MAIN_CONTENT_FADEOUT_DURATION = 150;
    private static final int MAIN_CONTENT_FADEIN_DURATION = 250;

    protected static final int NAVDRAWER_ITEM_MAIN = 0;
    protected static final int NAVDRAWER_ITEM_EXPLORE = 1;
    protected static final int NAVDRAWER_ITEM_SETTINGS = 2;
    protected static final int NAVDRAWER_ITEM_INVALID = -1;
    protected static final int NAVDRAWER_ITEM_SEPARATOR = -2;
    protected static final int NAVDRAWER_ITEM_SEPARATOR_SPECIAL = -3;

    private ArrayList<Integer> mNavDrawerItems = new ArrayList<>();
    private static final int[] NAVDRAWER_ICON_RES_ID = new int[]{
            R.drawable.ic_navview_my_schedule,  // My Schedule
            R.drawable.ic_navview_explore,  // Explore
            R.drawable.ic_navview_settings
    };

    private static final int[] NAVDRAWER_TITLE_RES_ID = new int[]{
            R.string.navdrawer_item_my_schedule,
            R.string.navdrawer_item_explore,
            R.string.navdrawer_item_settings,
    };

    private boolean mActionBarAutoHideEnabled = false;

    private int mThemedStatusBarColor;
    private int mNormalStatusBarColor;

    private Toolbar mActionBarToolbar;
    private DrawerLayout mDrawerLayout;
    private ViewGroup mDrawerItemsListContainer;
    private View[] mNavDrawerItemViews = null;

    private Handler mHandler;

    private DrawerHeaderViewHolder drawerHeaderViewHolder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mHandler = new Handler();

        User.getUserSp(getApplicationContext()).registerOnSharedPreferenceChangeListener(this);

        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        mThemedStatusBarColor = getResources().getColor(R.color.theme_primary_dark);
        mNormalStatusBarColor = mThemedStatusBarColor;
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);
        getActionBarToolbar();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setupNavDrawer();
        initLoginStat();
        trySetupSwipeRefresh();

        View mainContent = findViewById(R.id.main_content);
        if (mainContent != null) {
            mainContent.setAlpha(0);
            mainContent.animate().alpha(1).setDuration(MAIN_CONTENT_FADEIN_DURATION);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        User.getUserSp(getApplicationContext()).unregisterOnSharedPreferenceChangeListener(this);
    }

    protected void initLoginStat() {
        if (drawerHeaderViewHolder != null) {
            if (User.isLogin(getApplicationContext())) {
                drawerHeaderViewHolder.tvUsername.setText(User.getUsername(getApplicationContext()));
                drawerHeaderViewHolder.llLogin.setVisibility(View.VISIBLE);
                drawerHeaderViewHolder.llNotLogin.setVisibility(View.GONE);
            } else {
                drawerHeaderViewHolder.llNotLogin.setVisibility(View.VISIBLE);
                drawerHeaderViewHolder.llLogin.setVisibility(View.GONE);
            }
            drawerHeaderViewHolder.btnLogin.setOnClickListener(this);
            drawerHeaderViewHolder.btnLogout.setOnClickListener(this);
        }
    }

    protected void onLoginStatChanged() {

    }

    protected Toolbar getActionBarToolbar() {
        if (mActionBarToolbar == null) {
            mActionBarToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
            if (mActionBarToolbar != null) {
                setSupportActionBar(mActionBarToolbar);
            }
        }
        return mActionBarToolbar;
    }

    private void setupNavDrawer() {
        int selfItem = getSelfNavDrawerItem();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (mDrawerLayout == null) {
            return;
        }
        mDrawerLayout.setStatusBarBackgroundColor(getResources().getColor(R.color.theme_primary_dark));
        ScrollView navDrawer = (ScrollView) findViewById(R.id.navdrawer);
        if (selfItem == NAVDRAWER_ITEM_INVALID) {
            if (navDrawer != null) {
                ((ViewGroup) navDrawer.getParent()).removeView(navDrawer);
            }
            mDrawerLayout = null;
            return;
        }

        if (navDrawer != null) {
            View view = findViewById(R.id.login_root);
            if (view != null) {
                drawerHeaderViewHolder = new DrawerHeaderViewHolder(view);
            }
        }

        if (mActionBarToolbar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            mActionBarToolbar.setNavigationOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mDrawerLayout.openDrawer(GravityCompat.START);
                }
            });

            ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(
                    this,
                    mDrawerLayout,
                    mActionBarToolbar,
                    R.string.drawer_open,
                    R.string.drawer_close) {
                @Override
                public void onDrawerSlide(View drawerView, float slideOffset) {
                    onNavDrawerSlide(slideOffset);
                }

                @Override
                public void onDrawerOpened(View drawerView) {
                    onNavDrawerStateChanged(true, false);
                }

                @Override
                public void onDrawerClosed(View drawerView) {
                    if (mDrawerLayout != null) {
                        mDrawerLayout.closeDrawer(GravityCompat.START);
                    }
                }

                @Override
                public void onDrawerStateChanged(int newState) {
                    onNavDrawerStateChanged(isNavDrawerOpen(), newState != DrawerLayout.STATE_IDLE);
                }
            };
            mDrawerLayout.setDrawerListener(drawerToggle);
            drawerToggle.setDrawerIndicatorEnabled(true);
            drawerToggle.syncState();
        }

        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        populateNavDrawer();
    }

    private void populateNavDrawer() {
        mNavDrawerItems.clear();

        mNavDrawerItems.add(NAVDRAWER_ITEM_MAIN);
        mNavDrawerItems.add(NAVDRAWER_ITEM_EXPLORE);
        mNavDrawerItems.add(NAVDRAWER_ITEM_SEPARATOR_SPECIAL);
        mNavDrawerItems.add(NAVDRAWER_ITEM_SETTINGS);

        createNavDrawerItems();
    }

    private void createNavDrawerItems() {
        mDrawerItemsListContainer = (ViewGroup) findViewById(R.id.navdrawer_items_list);
        if (mDrawerItemsListContainer == null) {
            return;
        }

        mNavDrawerItemViews = new View[mNavDrawerItems.size()];
        mDrawerItemsListContainer.removeAllViews();
        int i = 0;
        for (int itemId : mNavDrawerItems) {
            mNavDrawerItemViews[i] = makeNavDrawerItem(itemId, mDrawerItemsListContainer);
            mDrawerItemsListContainer.addView(mNavDrawerItemViews[i]);
            ++i;
        }
    }

    private View makeNavDrawerItem(final int itemId, ViewGroup container) {
        if (isSeparator(itemId)) {
            return getLayoutInflater().inflate(R.layout.navdrawer_separator, container, false);
        }

        NavDrawerItemView item = (NavDrawerItemView) getLayoutInflater().inflate(
                R.layout.navdrawer_item, container, false);
        item.setContent(NAVDRAWER_ICON_RES_ID[itemId], NAVDRAWER_TITLE_RES_ID[itemId]);
        item.setActivated(getSelfNavDrawerItem() == itemId);
        if (item.isActivated()) {
            item.setContentDescription(getString(R.string.navdrawer_selected_menu_item_a11y_wrapper,
                    getString(NAVDRAWER_TITLE_RES_ID[itemId])));
        } else {
            item.setContentDescription(getString(R.string.navdrawer_menu_item_a11y_wrapper,
                    getString(NAVDRAWER_TITLE_RES_ID[itemId])));
        }

        item.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onNavDrawerItemClicked(itemId);
            }
        });
        return item;
    }

    private void setSelectedNavDrawerItem(int itemId) {
        if (mNavDrawerItemViews != null) {
            for (int i = 0; i < mNavDrawerItemViews.length; i++) {
                if (i < mNavDrawerItems.size()) {
                    int thisItemId = mNavDrawerItems.get(i);
                    mNavDrawerItemViews[i].setActivated(itemId == thisItemId);
                }
            }
        }
    }

    private boolean isSeparator(int itemId) {
        return itemId == NAVDRAWER_ITEM_SEPARATOR || itemId == NAVDRAWER_ITEM_SEPARATOR_SPECIAL;
    }

    private boolean isSpecialItem(int itemId) {
//        return itemId == NAVDRAWER_ITEM_SETTINGS;
        return false;
    }

    private void trySetupSwipeRefresh() {

    }

    protected boolean isNavDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(GravityCompat.START);
    }

    protected void closeNavDrawer() {
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    protected void onNavDrawerSlide(float offset) {
    }

    protected void onNavDrawerStateChanged(boolean isOpen, boolean isAnimating) {
        if (mActionBarAutoHideEnabled && isOpen) {
            autoShowOrHideActionBar(true);
        }
    }

    private void onNavDrawerItemClicked(final int itemId) {
        if (itemId == getSelfNavDrawerItem()) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
            return;
        }

        if (isSpecialItem(itemId)) {
            goToNavDrawerItem(itemId);
        } else {
            // launch the target Activity after a short delay, to allow the close animation to play
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    goToNavDrawerItem(itemId);
                }
            }, NAVDRAWER_LAUNCH_DELAY);

            // change the active item on the list so the user can see the item changed
            setSelectedNavDrawerItem(itemId);
            // fade out the main content
            View mainContent = findViewById(R.id.main_content);
            if (mainContent != null) {
                mainContent.animate().alpha(0).setDuration(MAIN_CONTENT_FADEOUT_DURATION);
            }
        }

        mDrawerLayout.closeDrawer(GravityCompat.START);
    }

    private void goToNavDrawerItem(int item) {
        switch (item) {
            case NAVDRAWER_ITEM_MAIN:
                startActivity(new Intent(this, MainActivity.class));
                finish();
                break;
            case NAVDRAWER_ITEM_EXPLORE:
                createBackStack(new Intent(this, RegRecordActivity.class));
                break;
            case NAVDRAWER_ITEM_SETTINGS:
                createBackStack(new Intent(this, SettingsActivity.class));
                break;
        }
    }

    protected void autoShowOrHideActionBar(boolean show) {

    }

    protected int getSelfNavDrawerItem() {
        return NAVDRAWER_ITEM_INVALID;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key != null && key.equals(User.USERNAME_KEY)) {
            initLoginStat();
            onLoginStatChanged();
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }

    @Override
    public void onBackPressed() {
        if (isNavDrawerOpen()) {
            closeNavDrawer();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onClick(View v) {
        final int[] startingLocation = new int[]{0, 0};
        switch (v.getId()) {
            case R.id.btn_login:
                closeNavDrawer();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        AccountActivity.requestLogin(BaseActivity.this, startingLocation);
                    }
                }, NAVDRAWER_LAUNCH_DELAY);
                break;
            case R.id.btn_logout:
                User.deleteUserInfo(getApplicationContext());
                animateLogout();
                break;
            case R.id.empty_btn_login:
                AccountActivity.requestLogin(BaseActivity.this, com.jin.uitoolkit.util.Utils.getViewCenterXY(v));
                break;
        }
    }

    private void animateLogout() {
        if (drawerHeaderViewHolder != null) {
            int height = drawerHeaderViewHolder.itemView.getHeight();
            drawerHeaderViewHolder.ivProfile.setTranslationY(-height);
            drawerHeaderViewHolder.llRoot.setTranslationY(-height);
            drawerHeaderViewHolder.ivProfile.animate().translationY(0)
                    .setDuration(MAIN_CONTENT_FADEIN_DURATION)
                    .setStartDelay(100)
                    .setInterpolator(INTERPOLATOR);

            drawerHeaderViewHolder.llRoot.animate().translationY(0)
                    .setDuration(MAIN_CONTENT_FADEIN_DURATION)
                    .setStartDelay(200)
                    .setInterpolator(INTERPOLATOR).start();
        }
    }


    private void createBackStack(Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            TaskStackBuilder builder = TaskStackBuilder.create(this);
            builder.addNextIntentWithParentStack(intent);
            builder.startActivities();
        } else {
            startActivity(intent);
            finish();
        }
    }

    static class DrawerHeaderViewHolder {
        View itemView;
        LinearLayout llRoot;
        LinearLayout llLogin;
        LinearLayout llNotLogin;
        ImageView ivProfile;
        Button btnLogin;
        Button btnLogout;
        TextView tvUsername;

        public DrawerHeaderViewHolder(View view) {
            itemView = view;
            initView(view);
        }

        void initView(View view) {
            llRoot = (LinearLayout) view.findViewById(R.id.username_root_view);
            llLogin = (LinearLayout) view.findViewById(R.id.ll_login);
            llNotLogin = (LinearLayout) view.findViewById(R.id.ll_not_login);
            ivProfile = (ImageView) view.findViewById(R.id.profile_image);
            btnLogin = (Button) view.findViewById(R.id.btn_login);
            btnLogout = (Button) view.findViewById(R.id.btn_logout);
            tvUsername = (TextView) view.findViewById(R.id.tv_username);
        }
    }
}
