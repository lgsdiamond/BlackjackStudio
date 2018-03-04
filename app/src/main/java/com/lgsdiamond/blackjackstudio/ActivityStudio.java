package com.lgsdiamond.blackjackstudio;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.lgsdiamond.blackjackstudio.BlackjackElement.Better;
import com.lgsdiamond.blackjackstudio.BlackjackElement.BjService;
import com.lgsdiamond.blackjackstudio.BlackjackElement.Rule;
import com.lgsdiamond.blackjackstudio.BlackjackElement.Strategy;
import com.lgsdiamond.blackjackstudio.BlackjackUtils.UtilityStudio;

import java.util.ArrayList;

public class ActivityStudio extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        FragmentTable.OnFragmentInteractionListener, FragmentManager.OnBackStackChangedListener {

    public static ActivityStudio sStudio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sStudio = this;

        //Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //Fix to portrait display
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setContentView(R.layout.activity_studio);

        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // initialize utilities first
        UtilityStudio.preInitialize(this);

        // initialize utilities first
        initializeCommonValues();

        // then, start binding service
        doBindService();
    }

    @Override
    public void onBackPressed() {
        if (mDrawer.isDrawerOpen(GravityCompat.START)) {
            mDrawer.closeDrawer(GravityCompat.START);
        } else {
            int nEntry = mFragmentManager.getBackStackEntryCount();
            if (nEntry > 0) {
                FragmentManager.BackStackEntry entry =
                        mFragmentManager.getBackStackEntryAt(nEntry - 1);
                String name = entry.getName();

                FragmentTransaction ft = mFragmentManager.beginTransaction();
                if (mCurrentFragment.getPreserve()) {
                    ft.hide(mCurrentFragment);
                } else {
                    ft.remove(mCurrentFragment);
                }
                ft.commit();
                mFragmentManager.executePendingTransactions();
                mFragmentManager.popBackStackImmediate(name,
                        FragmentManager.POP_BACK_STACK_INCLUSIVE);
                showBasicFragmentByTag(name);
            } else {
                AlertDialog.Builder alert_confirm = new AlertDialog.Builder(ActivityStudio.this);
                alert_confirm.setMessage("Exit from Blackjack Studio App?").setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finishNormal();
                            }
                        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        return;
                    }
                });
                AlertDialog alert = alert_confirm.create();
                alert.show();
            }
        }
    }

    private void finishNormal() {
        super.onBackPressed();

        unbindService(mConnection);
        UtilityStudio.playSound(UtilityStudio.sSound_ThankYou);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.studio, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switchFragmentByMenuId(item.getItemId());
        return true;
    }

    //=== Fragment handling ===

    private void switchFragmentByMenuId(int menuId) {
        // Handle navigation view item clicks here.
        FragmentStudioBase newFragment = null;

        switch (menuId) {
            default:
            case R.id.nav_table:
                newFragment = mBasicFragments.get(INDEX_TABLE);
                break;

            case R.id.nav_simulator:
                newFragment = mBasicFragments.get(INDEX_SIMULATOR);
                break;

            case R.id.nav_counting:
                newFragment = mBasicFragments.get(INDEX_COUNTING);
                break;

            case R.id.nav_setting:
                newFragment = FragmentSetting.newInstance(mService);
                break;

            case R.id.nav_strategy:
                newFragment = FragmentStrategy.newInstance();
                break;

            case R.id.nav_betting:
                newFragment = FragmentBetter.newInstance();
                break;

            case R.id.nav_learning:
                newFragment = FragmentLearning.newInstance();
                break;

            case R.id.nav_statistics:
                newFragment = FragmentStatistics.newInstance();
                break;

            case R.id.nav_about:
                newFragment = FragmentAbout.newInstance();
                break;
        }

        showFragment(newFragment);
        mDrawer.closeDrawer(GravityCompat.START);
    }

    //=== initialization ===
    private void SetupStudio() {
        UtilityStudio.LogD("Studio is being setup");

        // Create UI Handler
        sStudioHandler = new StudioHandler();

        initializeViews();

        initializeMenus();

        initializeFragment();

        UtilityStudio.postInitialize(this);
    }

    public static void post(Runnable code) {
        sStudioHandler.post(code);
    }


    ArrayList<Better> mBetters;
    static String[] sBettersTitles;
    ArrayList<Strategy> mStrategies;
    static String[] sStrategiesTitles;

    private void initializeCommonValues() {

        Better.setPredefinedBaseBet(100.0);
        mBetters = Better.getPredefinedBetters();
        mStrategies = Strategy.makePredefinedStrategies();
    }

    public ArrayList<Better> getBetters() {
        return mBetters;
    }

    public String[] getBettersTitles() {
        if (sBettersTitles == null) {
            sBettersTitles = new String[mBetters.size()];
            int index = 0;
            for (Better better : mBetters) {
                sBettersTitles[index++] = better.getTitle();
            }
        }
        return sBettersTitles;
    }

    public ArrayList<Strategy> getStrategies() {
        return mStrategies;
    }

    public String[] getStrategiesTitles() {
        if (sStrategiesTitles == null) {
            sStrategiesTitles = new String[mStrategies.size()];
            int index = 0;
            for (Strategy strategy : mStrategies) {
                sStrategiesTitles[index++] = strategy.getTitle();
            }
        }

        return sStrategiesTitles;
    }

    FrameLayout mLayoutContainer;
    DrawerLayout mDrawer;

    private void initializeViews() {
        mLayoutContainer = (FrameLayout) findViewById(R.id.mainFragmentContainer);
    }

    private void initializeMenus() {

    }

    private ArrayList<FragmentStudioBase> mBasicFragments;
    private FragmentManager mFragmentManager;
    private FragmentStudioBase mCurrentFragment;

    private static final int INDEX_TABLE = 0, INDEX_SIMULATOR = 1, INDEX_COUNTING = 2;

    private void initializeFragment() {
        // to manage fragments
        mFragmentManager = getFragmentManager();
        mFragmentManager.addOnBackStackChangedListener(this);

        // for common use
        FragmentTable table = FragmentTable.newInstance(mService);
        mService.setTable(table);

        // to manage basic preserved fragments
        mBasicFragments = new ArrayList<>();
        mBasicFragments.add(table);
        mBasicFragments.add(FragmentSimulator.newInstance(mService));
        mBasicFragments.add(FragmentCounting.newInstance(mService.pGameRule.mCountDecks));

        table.setFragmentCounting(getFragmentCounting());   // sync card counting

        // add three basic fragment and hide them all
        FragmentTransaction ft = mFragmentManager.beginTransaction();
        for (FragmentStudioBase fragment : mBasicFragments) {
            ft.add(R.id.mainFragmentContainer, fragment);
            ft.hide(fragment);
        }

        // show the first basic fragment
        mCurrentFragment = getFragmentTable();
        ft.show(mCurrentFragment);
        ft.commit();

        mFragmentManager.executePendingTransactions();

        // now, we have fragment view initialized
        getFragmentCounting().setStandAlone(false);
    }

    private FragmentTable getFragmentTable() {
        return (FragmentTable) mBasicFragments.get(INDEX_TABLE);
    }

    private FragmentSimulator getFragmentSimulator() {
        return (FragmentSimulator) mBasicFragments.get(INDEX_SIMULATOR);
    }

    private FragmentCounting getFragmentCounting() {
        return (FragmentCounting) mBasicFragments.get(INDEX_COUNTING);
    }

    @Override
    public void onBackStackChanged() {
        String name = "";
        int nEntry = mFragmentManager.getBackStackEntryCount();
        if (nEntry > 0) {
            FragmentManager.BackStackEntry entry = mFragmentManager.getBackStackEntryAt(nEntry - 1);
            name = entry.getName();
        }
    }

    private void showBasicFragmentByTag(String tagName) {
        if (tagName == null) return;

        FragmentTransaction ft = mFragmentManager.beginTransaction();
        for (FragmentStudioBase fragment : mBasicFragments) {
            if (fragment.getPrivateTag().equals(tagName)) {
                ft.show(fragment);
                mCurrentFragment = fragment;
            } else {
                ft.hide(fragment);
            }
        }

        ft.commit();
    }

    private void showFragment(FragmentStudioBase newFragment) {
        if ((newFragment == null) || (mCurrentFragment == newFragment)) return;

        FragmentTransaction ft = mFragmentManager.beginTransaction();

        if (mCurrentFragment != null) {
            if (mCurrentFragment.getPreserve()) {
                ft.hide(mCurrentFragment);
                ft.addToBackStack(mCurrentFragment.getPrivateTag());
            } else {
                ft.remove(mCurrentFragment);
            }
        }

        if (newFragment.getPreserve()) {
            ft.show(newFragment);
        } else {
            ft.add(R.id.mainFragmentContainer, newFragment, newFragment.getPrivateTag());
        }
        ft.commit();

        mFragmentManager.executePendingTransactions();

        mCurrentFragment = newFragment;
    }

    //=== binding service ===
    private BjService mService;
    Boolean mIsBound;

    private void doBindService() {
        UtilityStudio.LogD("Service is being setup");

        bindService(new Intent(ActivityStudio.this, BjService.class), mConnection,
                BIND_AUTO_CREATE);
        mIsBound = true;
    }

    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            UtilityStudio.LogD("Service Connection Established");

            // make references to communicate between each other
            mService = ((BjService.LocalBinder) service).getService();
            mService.setStudio(ActivityStudio.this);

            // make intent filter, and combine with broadcast receiver
            IntentFilter filter = new IntentFilter();

            filter.addAction(BjService.SERVICE_READY);

            // Round
            filter.addAction(BjService.ROUND_IDLE_STARTED);
            filter.addAction(BjService.ROUND_BETTING_STARTED);
            filter.addAction(BjService.ROUND_INITIAL_DEAL_STARTED);
            filter.addAction(BjService.ROUND_DEALING_STARTED);
            filter.addAction(BjService.ROUND_PAYING_STARTED);

            filter.addAction(BjService.ROUND_IDLE_ENDED);
            filter.addAction(BjService.ROUND_BETTING_ENDED);
            filter.addAction(BjService.ROUND_INITIAL_DEAL_ENDED);
            filter.addAction(BjService.ROUND_DEALING_ENDED);
            filter.addAction(BjService.ROUND_PAYING_ENDED);

            // Deal
            filter.addAction(BjService.ROUND_STAGE_CONTINUE);
            filter.addAction(BjService.ROUND_STAGE_FINISHED);
            filter.addAction(BjService.ROUND_PAYING_FINISHED);

            // TODO: add more filter

            ActivityStudio.this.registerReceiver(new StudioIntentReceiver(), filter);

            Rule.initialize();
            mService.setGameRule(Rule.sRuleRegular);

            mService.initialize(false);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            mIsBound = false;
        }
    };

    @Override
    public void onFragmentInteraction(Uri uri) {
        // TODO: implement more
        int a = 1;
    }

    public Rule getTableRule() {
        return mService.pGameRule;
    }

    private class StudioIntentReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Boolean needFragmentAction = true;

            switch (intent.getAction()) {
                case BjService.SERVICE_READY:
                    SetupStudio();
                    break;
            }

            if (needFragmentAction) {
                getFragmentTable().dispatchIntent(intent);
            }
        }
    }

    //=== handler ===
    public static Handler sStudioHandler;

    static class StudioHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                default:
                    break;
            }
        }
    }
}
