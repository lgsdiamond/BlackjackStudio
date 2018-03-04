package com.lgsdiamond.blackjackstudio;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.lgsdiamond.blackjackstudio.BlackjackElement.BjService;
import com.lgsdiamond.blackjackstudio.BlackjackElement.Dealer;
import com.lgsdiamond.blackjackstudio.BlackjackElement.Player;
import com.lgsdiamond.blackjackstudio.BlackjackElement.PlayerHand;
import com.lgsdiamond.blackjackstudio.BlackjackElement.Rule;
import com.lgsdiamond.blackjackstudio.BlackjackUtils.SettingAdapter;
import com.lgsdiamond.blackjackstudio.BlackjackUtils.SettingFactory;
import com.lgsdiamond.blackjackstudio.BlackjackUtils.UtilityStudio;
import com.lgsdiamond.outsource.mpchart.charts.CandleStickChart;
import com.lgsdiamond.outsource.mpchart.charts.Chart;
import com.lgsdiamond.outsource.mpchart.charts.HorizontalBarChart;
import com.lgsdiamond.outsource.mpchart.charts.LineChart;
import com.lgsdiamond.outsource.mpchart.components.LimitLine;
import com.lgsdiamond.outsource.mpchart.components.XAxis;
import com.lgsdiamond.outsource.mpchart.components.YAxis;
import com.lgsdiamond.outsource.mpchart.data.BarData;
import com.lgsdiamond.outsource.mpchart.data.BarDataSet;
import com.lgsdiamond.outsource.mpchart.data.BarEntry;
import com.lgsdiamond.outsource.mpchart.data.CandleData;
import com.lgsdiamond.outsource.mpchart.data.CandleDataSet;
import com.lgsdiamond.outsource.mpchart.data.CandleEntry;
import com.lgsdiamond.outsource.mpchart.data.DataSet;
import com.lgsdiamond.outsource.mpchart.data.Entry;
import com.lgsdiamond.outsource.mpchart.data.LineData;
import com.lgsdiamond.outsource.mpchart.data.LineDataSet;
import com.lgsdiamond.outsource.mpchart.formatter.ValueFormatter;
import com.lgsdiamond.outsource.mpchart.formatter.YAxisValueFormatter;
import com.lgsdiamond.outsource.mpchart.utils.ColorTemplate;
import com.lgsdiamond.outsource.mpchart.utils.ViewPortHandler;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class FragmentSimulator extends FragmentStudioBase {
    BjService mGameService;
    Rule mSimulatorRule;
    SettingFactory mSimulatorSettingFactory = null;

    // for simulation
    private final double DEFAULT_BASEBET = 1.0;
    private final double DEFAULT_MAXBET = 1000.0;
    private final double DEFAULT_MINBET = 1.0;
    private final double DEFAULT_BANKROLL = 10000.0;

    private final int DEFAULT_COUNT_DECK = 6;
    private final int DEFAULT_COUNT_BOX = 2;

    public static FragmentSimulator newInstance(BjService service) {
        FragmentSimulator fragment = new FragmentSimulator();
        FragmentStudioBase.newInstance(fragment, R.layout.fragment_simulator);

        fragment.mGameService = service;
        fragment.setPreserve(true);
        return fragment;
    }

    // getter
    public ArrayList<PlayerHand> getPlayerHands() {
        return mSimulatorService.pRound.getActingHands();
    }

    public ArrayList<Player> getPlayers() {
        return mSimulatorService.pRound.getSimulationPlayers();
    }

    private static int getAutoRunStepCount(int nRunCount) {
        return (nRunCount <= DEFAULT_UPDATE_EACH_RUN_COUNT) ?
                nRunCount : DEFAULT_COUNT_PROGRESS_STEPS;
    }

    //=== Auto Round===
    static AutoRunRounds sAutoRound;
    private static int DEFAULT_COUNT_PROGRESS_STEPS = 100;
    private static int DEFAULT_UPDATE_EACH_RUN_COUNT = (int) (DEFAULT_COUNT_PROGRESS_STEPS * 1.5);

    private class AutoRunRounds extends AsyncTask<Void, float[], Integer> {
        public AutoRunRounds() {
            sAutoRound = this;
        }

        @Override
        protected void onPreExecute() {
            mProgressBarAutoRun.setVisibility(View.VISIBLE);
            mIvSimulationResultContinue.setVisibility(View.GONE);
            mIvSimulationResultReset.setVisibility(View.GONE);

            mResultAdapter.setProgressDone(false);

            // make the beginning candlestick data
            mSimulatorService.pRound.openCandlestick();

            BjService.setAutoRunning(true);
        }

        @Override
        protected Integer doInBackground(Void... params) {
            int targetRun = mSimulatorRule.mCountSimulationRuns;

            double skipGap = (double) targetRun / getAutoRunStepCount(targetRun);

            double nextPublish = skipGap;

            // at each run, set candlestick shadow data
            for (int currentRun = 1; currentRun <= targetRun; currentRun++) {

                // this is the actual run of ONE round
                mSimulatorService.pRound.runAutoOneRound();
                mSimulatorService.pRound.setCandlestickShadow();

                if ((currentRun >= nextPublish) || (currentRun == targetRun)) {
                    nextPublish += skipGap;

                    // [0] progress count
                    float[] progressValues = new float[]{currentRun};   // starting from 1

                    //[1] close candlestick values before publishing
                    mSimulatorService.pRound.closeCandlestick();

                    if (!isCancelled() && currentRun < targetRun) {     // save last update for AfterRun
                        publishProgress(progressValues,     // [0]
                                getBankrollValues_Candle(), // [1]
                                getRoundResultValues(),     // [2]
                                getPlayerScoreValues(),     // [3])
                                getDealerScoreValues());    // [4]);
                    }

                    //[1] open candlestick values after publishing
                    mSimulatorService.pRound.openCandlestick();
                }
                if (isCancelled()) {
                    targetRun = currentRun; // TODO: currentRun increase in canceled case?
                    break;
                }
            }

            return targetRun;
        }

        @Override
        protected void onProgressUpdate(float[]... progress) {
            final int iRun = (int) progress[0][0];
            final int totalRun = mSimulatorRule.mCountSimulationRuns;

            // update progress bar and counting text
            mProgressBarAutoRun.setProgress(iRun);
            double percent = 100.0 * (double) iRun / totalRun;
            String progressStr = "Round: " + String.valueOf(iRun) + "/" +
                    String.valueOf(mSimulatorRule.mCountSimulationRuns) +
                    " (" + UtilityStudio.doubleToDotDecimal(percent, 1) + "%)";
            mTvTitleSimulationResult.setText(progressStr);

            // update all the chart
            mResultAdapter.updateResultProgress(iRun,
                    progress[1], progress[2], progress[3], progress[4]);
        }

        @Override
        protected void onPostExecute(Integer nRun) {
            afterAutoRun(nRun);
        }

        @Override
        protected void onCancelled(Integer nRun) {
            UtilityStudio.toast("Simulation Canceled at Round " + String.valueOf(nRun) + ".");
            afterAutoRun(nRun);
        }

        private void afterAutoRun(int nRun) {
            sAutoRound = null;

            mCurrentTotalRunCount += nRun;

            mResultAdapter.setProgressDone(true);

            BjService.setAutoRunning(false);

            mProgressBarAutoRun.setVisibility(View.GONE);
            mIvSimulationResultContinue.setVisibility(View.VISIBLE);
            mIvSimulationResultReset.setVisibility(View.VISIBLE);
            mTvTitleSimulationResult.setText("Simulation Result("
                    + UtilityStudio.doubleToDotDecimal(mCurrentTotalRunCount, 0) + " Rounds)");

            // run saved last update
            mResultAdapter.updateResultProgress(nRun,
                    getBankrollValues_Candle(), // [1]
                    getRoundResultValues(),     // [2]
                    getPlayerScoreValues(),     // [3])
                    getDealerScoreValues());    // [4]);
        }
    }

    //=== Compute Sound Results ===

    // [+] progress bankroll change
    private float[] getBankrollValues_Candle() {
        ArrayList<Player> players = getPlayers();
        float[] bankrollValues_Candle =     // extra for dealer balance
                new float[1 + COUNT_CANDLE_PER_PLAYER * mSimulatorRule.mCountBoxes];
        bankrollValues_Candle[0] = (float) mSimulatorService.pDealer.getBalance();

        int index = 1;
        for (Player player : players) {     // current-min-max, current bankroll
            Player.PlayerData record = player.mRecord;

            bankrollValues_Candle[index++] = (float) record.mShadowHighAmount;     // 0-shadow high
            bankrollValues_Candle[index++] = (float) record.mShadowLowAmount;      // 1-shadow low
            bankrollValues_Candle[index++] = (float) record.mOpenAmount;           // 2-open
            bankrollValues_Candle[index++] = (float) record.mCloseAmount;          // 3-close
        }
        return bankrollValues_Candle;
    }

    // [1] progress bankroll change
    private float[] getBankrollMaxMinValues() {
        final int COUNT_PER_PLAYER = 3;
        ArrayList<Player> players = getPlayers();
        float[] bankrollValues = new float[COUNT_PER_PLAYER * players.size()];
        int index = 0;
        for (Player player : players) {     // current-min-max, current bankroll
            bankrollValues[index++] =
                    (float) player.getBankroll();                                       // 0-current
            bankrollValues[index++] = (float) player.getBalanceInitial() +
                    (float) player.mRecord.minWinAmount;   // 1-min
            bankrollValues[index++] = (float) player.getBalanceInitial() +
                    (float) player.mRecord.maxWinAmount;   // 2-max
        }
        return bankrollValues;
    }

    // [2] betting
    private float[] getBettingMaxMinAvgValues() {
        final int COUNT_PER_PLAYER = 3;
        ArrayList<Player> players = getPlayers();
        float[] streakValues = new float[COUNT_PER_PLAYER * players.size()];
        int index = 0;
        for (Player player : players) {     // current-min-max, current bankroll
            Player.PlayerData record = player.mRecord;

            streakValues[index++] = (float) record.minBetAmount;        // 0-minimum bet
            streakValues[index++] = (float) record.maxBetAmount;        // 1-maximum bet
            streakValues[index++] = (float) record.averageBetAmount;    // 2-win streak
        }
        return streakValues;
    }

    // [3] progress win/push/lost ratio
    private float[] getRoundResultValues() {
        final int COUNT_RESULT_PER_PLAYER = 3;
        ArrayList<Player> players = getPlayers();
        float[] roundResultValues = new float[COUNT_RESULT_PER_PLAYER * players.size()];
        int index = 0;
        for (Player player : players) {     // for lost-push-win
            roundResultValues[index++] = (player.mRecord.totalHand == 0) ? 0.0f :    // 0-lost
                    100.0f * player.mRecord.countLost / player.mRecord.totalHand;
            roundResultValues[index++] = (player.mRecord.totalHand == 0) ? 0.0f :    // 1-push
                    100.0f * player.mRecord.countPush / player.mRecord.totalHand;
            roundResultValues[index++] = (player.mRecord.totalHand == 0) ? 0.0f :    // 2-win
                    100.0f * player.mRecord.countWin / player.mRecord.totalHand;
        }

        return roundResultValues;
    }

    // [4] final scores
    private float[] getPlayerScoreValues() {
        ArrayList<Player> players = getPlayers();
        float[] scoreValues = new float[COUNT_RESULT_PER_SCORE * COUNT_SCORE_PER_PLAYER
                * mSimulatorRule.mCountBoxes];
        int index = 0;
        for (Player player : players) {     // current-min-max, current bankroll
            Player.PlayerData record = player.mRecord;

            scoreValues[index++] = 100f * record.countSurrender / record.totalHand;            // 10-Surrender(Lost)
            scoreValues[index++] = 0.0f;                                                        // 10-Surrender(Push)
            scoreValues[index++] = 0.0f;                                                        // 10-Surrender(Win)

            scoreValues[index++] = 0.0f;                                                        // 9-Split(Lost)
            scoreValues[index++] = 100f * record.countSplit / record.totalHand;                // 9-Split(Push)
            scoreValues[index++] = 0.0f;                                                        // 9-Split(Win)

            scoreValues[index++] = 100f * record.countDoubledown_Lost / record.totalHand;      // 8-DD(Lost)
            scoreValues[index++] = 100f * record.countDoubledown_Push / record.totalHand;      // 8-DD(Push)
            scoreValues[index++] = 100f * record.countDoubledown_Win / record.totalHand;       // 8-DD(Win)

            scoreValues[index++] = 100f * record.countBust / record.totalHand;                 // 7-Bust(Lost)
            scoreValues[index++] = 0.0f;                                                        // 7-Bust(Push)
            scoreValues[index++] = 0.0f;                                                        // 7-Bust(Win)

            scoreValues[index++] = 0.0f;                                                        // 6-BJ(Lost)
            scoreValues[index++] = 100f * record.countBlackjack_Push / record.totalHand;       // 6-BJ(Push)
            scoreValues[index++] = 100f * record.countBlackjack_Win / record.totalHand;        // 6-BJ(Win)

            scoreValues[index++] = 100f * record.countScore21_Lost / record.totalHand;         // 5-21
            scoreValues[index++] = 100f * record.countScore21_Push / record.totalHand;         // 5-21
            scoreValues[index++] = 100f * record.countScore21_Win / record.totalHand;          // 5-21

            scoreValues[index++] = 100f * record.countScore20_Lost / record.totalHand;         // 4-20
            scoreValues[index++] = 100f * record.countScore20_Push / record.totalHand;         // 4-20
            scoreValues[index++] = 100f * record.countScore20_Win / record.totalHand;          // 4-20

            scoreValues[index++] = 100f * record.countScore19_Lost / record.totalHand;         // 3-19
            scoreValues[index++] = 100f * record.countScore19_Push / record.totalHand;         // 3-19
            scoreValues[index++] = 100f * record.countScore19_Win / record.totalHand;          // 3-19

            scoreValues[index++] = 100f * record.countScore18_Lost / record.totalHand;         // 2-18
            scoreValues[index++] = 100f * record.countScore18_Push / record.totalHand;         // 2-18
            scoreValues[index++] = 100f * record.countScore18_Win / record.totalHand;          // 2-18

            scoreValues[index++] = 100f * record.countScore17_Lost / record.totalHand;         // 1-17
            scoreValues[index++] = 100f * record.countScore17_Push / record.totalHand;         // 1-17
            scoreValues[index++] = 100f * record.countScore17_Win / record.totalHand;          // 1-17

            scoreValues[index++] = 100f * record.countScoreUnder17_Lost / record.totalHand;    // 0-17(-)
            scoreValues[index++] = 100f * record.countScoreUnder17_Push / record.totalHand;    // 0-17(-)
            scoreValues[index++] = 100f * record.countScoreUnder17_Win / record.totalHand;     // 0-17(-)
        }

        return scoreValues;
    }

    private float[] getDealerScoreValues() {
        float[] scoreValues = new float[COUNT_SCORE_PER_DEALER];
        Dealer.DealerData record = mSimulatorService.pDealer.mRecord;

        int index = 0;
        scoreValues[index++] = 100f * record.countBust / record.totalRound;         // 6-Bust
        scoreValues[index++] = 100f * record.countBlackjack / record.totalRound;    // 5-BJ
        scoreValues[index++] = 100f * record.countScore21 / record.totalRound;      // 4-21
        scoreValues[index++] = 100f * record.countScore20 / record.totalRound;      // 3-20
        scoreValues[index++] = 100f * record.countScore19 / record.totalRound;      // 2-19
        scoreValues[index++] = 100f * record.countScore18 / record.totalRound;      // 1-18
        scoreValues[index++] = 100f * record.countScore17 / record.totalRound;      // 0-17

        return scoreValues;
    }

    // [5] per score, lost-push-win ratio
    private float[] getScoreWinValues() {
        final int COUNT_PER_PLAYER = 24;
        ArrayList<Player> players = getPlayers();
        float[] scoreWinValues = new float[COUNT_PER_PLAYER * players.size()];
        int index = 0;
        for (Player player : players) {     // current-min-max, current bankroll
            Player.PlayerData record = player.mRecord;
            scoreWinValues[index++] = 100.0f * record.countScoreUnder17_Lost
                    / record.countScoreUnder17;                 // 0-17(-) Lost
            scoreWinValues[index++] = 100.0f * record.countScoreUnder17_Push
                    / record.countScoreUnder17;                 // 1-17(-) Push
            scoreWinValues[index++] = 100.0f * record.countScoreUnder17_Win
                    / record.countScoreUnder17;                 // 2-17(-) Win

            scoreWinValues[index++] = 100.0f * record.countScore17_Lost
                    / record.countScore17;                      // 3-17 Lost
            scoreWinValues[index++] = 100.0f * record.countScore17_Push
                    / record.countScore17;                      // 4-17 Push
            scoreWinValues[index++] = 100.0f * record.countScore17_Win
                    / record.countScore17;                      // 5-17 Win

            scoreWinValues[index++] = 100.0f * record.countScore18_Lost
                    / record.countScore18;                      // 6-18 Lost
            scoreWinValues[index++] = 100.0f * record.countScore18_Push
                    / record.countScore18;                      // 7-18 Push
            scoreWinValues[index++] = 100.0f * record.countScore18_Win
                    / record.countScore18;                      // 8-18 Win

            scoreWinValues[index++] = 100.0f * record.countScore19_Lost
                    / record.countScore19;                      // 9-19 Lost
            scoreWinValues[index++] = 100.0f * record.countScore19_Push
                    / record.countScore19;                      // 10-19 Push
            scoreWinValues[index++] = 100.0f * record.countScore19_Win
                    / record.countScore19;                      // 11-19 Win

            scoreWinValues[index++] = 100.0f * record.countScore20_Lost
                    / record.countScore20;                      // 12-20 Lost
            scoreWinValues[index++] = 100.0f * record.countScore20_Push
                    / record.countScore20;                      // 13-20 Push
            scoreWinValues[index++] = 100.0f * record.countScore20_Win
                    / record.countScore20;                      // 14-20 Win

            scoreWinValues[index++] = 100.0f * record.countScore21_Lost
                    / record.countScore21;                      // 15-21 Lost
            scoreWinValues[index++] = 100.0f * record.countScore21_Push
                    / record.countScore21;                      // 16-21 Push
            scoreWinValues[index++] = 100.0f * record.countScore21_Win
                    / record.countScore21;                      // 17-21 Win

            scoreWinValues[index++] = 0;                           // 18-BJ Lost
            scoreWinValues[index++] = 100.0f * record.countBlackjack_Push
                    / record.countBlackjack;                    // 19-BJ Push
            scoreWinValues[index++] = 100.0f * record.countScore21_Win
                    / record.countBlackjack;                    // 20-BJ Win

            scoreWinValues[index++] = 100.0f * record.countDoubledown_Lost
                    / record.countBlackjack;                    // 21-DD Lost
            scoreWinValues[index++] = 100.0f * record.countDoubledown_Push
                    / record.countBlackjack;                    // 22-DD Push
            scoreWinValues[index++] = 100.0f * record.countDoubledown_Win
                    / record.countBlackjack;                    // 23-DD Win
        }
        return scoreWinValues;
    }

    // [6] maximum streak count
    private float[] getStreakValues() {
        final int COUNT_PER_PLAYER = 4;
        ArrayList<Player> players = getPlayers();
        float[] streakValues = new float[COUNT_PER_PLAYER * players.size()];
        int index = 0;
        for (Player player : players) {     // current-min-max, current bankroll
            Player.PlayerData record = player.mRecord;

            streakValues[index++] = record.maxLostStreak;       // 0-lost streak
            streakValues[index++] = record.maxPushStreak;       // 1-push streak
            streakValues[index++] = record.maxWinStreak;        // 2-win streak

            streakValues[index++] = record.maxCountCards;       // 3-card count
        }
        return streakValues;
    }

    // [7] per streak length, count
    private float[] getStreakLengthValues() {
        final int COUNT_PER_PLAYER = 3;
        final int NUM_STREAKS = Player.PlayerData.MAX_COUNT_STREAKS;
        ArrayList<Player> players = getPlayers();
        float[] streakLengthValues = new float[NUM_STREAKS * COUNT_PER_PLAYER * players.size()];

        int index = 0;
        for (Player player : players) {
            Player.PlayerData record = player.mRecord;
            for (int length = 0; length < NUM_STREAKS; length++) {
                streakLengthValues[index++] = record.lostStreaks[length];       // 0-lost streak
                streakLengthValues[index++] = record.pushStreaks[length];       // 1-push streak
                streakLengthValues[index++] = record.winStreaks[length];        // 2-win streak
            }
        }
        return streakLengthValues;
    }

    //=== fragment view initialization===

    LinearLayout layoutSimulationSetting;
    TextView mTvTitleSimulationSetting;
    TextView mTvRuleTextSetting;
    ListView mLvSimulatorSetting;

    LinearLayout layoutSimulationResult;
    ProgressBar mProgressBarAutoRun;
    TextView mTvRuleTextResult;

    TextView mTvTitleSimulationResult;
    ImageView mIvSimulationResultReset;
    ImageView mIvSimulationResultContinue;

    ListView mLvSimulatorResult;
    ResultAdapter mResultAdapter;
    ArrayList<ResultData> mResultDataList;

    @Override
    protected void initializeViews() {
        if (mSimulatorRule == null) {
            try {
                mSimulatorRule = (Rule) mGameService.pGameRule.clone();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        }

        // simulation specific rule values, different form game table
        mSimulatorRule.useRandomShoe = true;    // use random shoe for simulation by default

        mSimulatorRule.mBaseBet = DEFAULT_BASEBET;
        mSimulatorRule.maxBet = DEFAULT_MAXBET;
        mSimulatorRule.minBet = DEFAULT_MINBET;
        mSimulatorRule.playerBankroll = DEFAULT_BANKROLL;
        mSimulatorRule.mCountDecks = DEFAULT_COUNT_DECK;
        mSimulatorRule.mCountBoxes = DEFAULT_COUNT_BOX;

        mSimulatorRule.allowNegativeBankroll = true;

        if (mSimulatorSettingFactory == null) {
            mSimulatorSettingFactory = new SettingFactory(mSimulatorRule);
            mSimulatorSettings = mSimulatorSettingFactory.getSimulatorSettings();
        }

        // for simulation setting
        layoutSimulationSetting = (LinearLayout) findViewById(R.id.loSimulationSetting);

        mTvTitleSimulationSetting = (TextView) findViewById(R.id.tvTitleSimulationSetting);
        mTvTitleSimulationSetting.setOnClickListener(this);

        mTvRuleTextSetting = (TextView) findViewById(R.id.tvRuleTextSimulatorSetting);
        mTvRuleTextSetting.setOnClickListener(this);
        mTvRuleTextSetting.setVisibility(View.GONE);

        mLvSimulatorSetting = (ListView) findViewById(R.id.lvSimulatorSetting);

        mSettingAdapter = new SettingAdapter(getActivity(),
                R.layout.row_setting, mSimulatorSettings, mTvRuleTextSetting);
        mLvSimulatorSetting.setAdapter(mSettingAdapter);
        mSimulatorSettingFactory.setSettingAdapter(mSettingAdapter);

        ImageView ivRun_Simulation = (ImageView) findViewById(R.id.ivSimulationRun);
        ivRun_Simulation.setOnClickListener(this);

        ImageView ivCancel_Simulation = (ImageView) findViewById(R.id.ivSimulationCancel);
        ivCancel_Simulation.setOnClickListener(this);


        // for simulation result layout
        layoutSimulationResult = (LinearLayout) findViewById(R.id.loSimulationResult);
        layoutSimulationResult.setVisibility(View.GONE);

        mProgressBarAutoRun = (ProgressBar) findViewById(R.id.pbAutoRun);
        mProgressBarAutoRun.setVisibility(View.GONE);

        mTvTitleSimulationResult = (TextView) findViewById(R.id.tvTitleSimulatorResult);
        mTvTitleSimulationResult.setOnClickListener(this);

        mTvRuleTextResult = (TextView) findViewById(R.id.tvRuleTextSimulatorResult);
        mTvRuleTextResult.setOnClickListener(this);
        mTvRuleTextResult.setVisibility(View.GONE);

        mLvSimulatorResult = (ListView) findViewById(R.id.lvSimulatorResult);

        mIvSimulationResultContinue = (ImageView) findViewById(R.id.ivSimulationResultContinue);
        mIvSimulationResultContinue.setOnClickListener(this);

        mIvSimulationResultReset = (ImageView) findViewById(R.id.ivSimulationResultReset);
        mIvSimulationResultReset.setOnClickListener(this);
    }

    private int mCountSimulationOptions;
    ArrayList<SettingFactory.BjSetting> mSimulatorSettings;
    ArrayList<SettingFactory.SimulationHandSetting> mHandSettings;
    SettingAdapter mSettingAdapter;

    SettingFactory.SpinnerSetting mShowProgressUpdateSetting;
    SettingFactory.SwitchSetting mUseRightYAxisSetting;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            // for simulation setting
            case R.id.ivSimulationRun:
                startSimulation();
                break;
            case R.id.ivSimulationCancel:
                cancelSimulation();
                break;

            // for simulation result
            case R.id.ivSimulationResultContinue:
                continueSimulation();
                break;
            case R.id.ivSimulationResultReset:
                resetSimulationResult();
                break;

            // show/hide rule test for setting
            case R.id.tvTitleSimulationSetting:
            case R.id.tvRuleTextSimulatorSetting:
                if (mTvRuleTextSetting.getVisibility() == View.GONE) {
                    mTvRuleTextSetting.setVisibility(View.VISIBLE);
                } else {
                    mTvRuleTextSetting.setVisibility(View.GONE);
                }
                break;

            // show/hide rule test for simulator
            case R.id.tvTitleSimulatorResult:
            case R.id.tvRuleTextSimulatorResult:
                if (mTvRuleTextResult.getVisibility() == View.GONE) {
                    mTvRuleTextResult.setVisibility(View.VISIBLE);
                } else {
                    mTvRuleTextResult.setVisibility(View.GONE);
                }
                break;
        }
    }

    @Override
    protected void setPrivateTag() {
        privateTag = "SIMULATOR";
    }

    // AutoRun
    BjService mSimulatorService;
    ArrayList<Player> mPlayers;

    private final int PROGRESS_INDEX_BANKROLL_CANDLE_VALUE = 0;
    private final int PROGRESS_INDEX_RESULT_VALUE = 1;
    private final int PROGRESS_INDEX_PLAYER_SCORE_VALUE = 2;
    private final int PROGRESS_INDEX_DEALER_SCORE_VALUE = 3;

    private int mCurrentTotalRunCount = 0;

    private void resetXVals_RunCount() {
        // if needed, create and add initial point
        if (mXVals_RunCount == null) mXVals_RunCount = new ArrayList<>();
        if (mXVals_RunCount.size() == 0) mXVals_RunCount.add("0");   // no % mark for 0

        // compute target number of steps
        int currentStepCount = mXVals_RunCount.size() - 1;  // 1 for initial point
        int targetStepCount = getAutoRunStepCount(mSimulatorRule.mCountSimulationRuns);
        if (mCurrentTotalRunCount > 0) targetStepCount += currentStepCount;

        // make currentStepCount equal to targetStepCount
        while (currentStepCount > targetStepCount) { // too big
            mXVals_RunCount.remove(mXVals_RunCount.size() - 1);
            currentStepCount--;
        }

        while (currentStepCount < targetStepCount) { // too small
            mXVals_RunCount.add("");
            currentStepCount++;
        }

        if (targetStepCount <= DEFAULT_UPDATE_EACH_RUN_COUNT) {
            for (int i = 1; i <= targetStepCount; i++) {
                mXVals_RunCount.set(i, String.valueOf(i));
            }
        } else {
            for (int i = 1; i <= targetStepCount; i++) {
                double progress = 100.0 * (double) i / targetStepCount;
                String percent = UtilityStudio.doubleToDotDecimal(progress, 0) + "%";
                mXVals_RunCount.set(i, percent);
            }
        }
    }

    private void continueSimulation() {
        resetXVals_RunCount();

        ActivityStudio.post(new Runnable() {
            @Override
            public void run() {
                new AutoRunRounds().execute();
            }
        });
    }

    private void startSimulation() {
        if (mSimulatorRule.mCountSimulationRuns < 1) return;

        mCurrentTotalRunCount = 0;

        // update rule, at first
        for (SettingFactory.BjSetting setting : mSimulatorSettings) {
            if (setting.needUpdate()) setting.settingRuleOut();
            setting.mTouched = false;
        }

        // change front layout
        layoutSimulationResult.setVisibility(View.VISIBLE);
        layoutSimulationSetting.setVisibility(View.GONE);
        mTvRuleTextResult.setText(mTvRuleTextSetting.getText());

        // make unbound service to use
        mSimulatorService = new BjService();

        mSimulatorService.setGameRule(mSimulatorRule);
        mSimulatorService.initialize(true);

        mSimulatorService.pRound.setAutoRound();        // specify that it is autoRound

        // make new simulation players with selected action & betting strategy
        int nHands = mSimulatorRule.mCountBoxes;
        mPlayers = new ArrayList<>();

        for (int index = 0; index < nHands; index++) {
            Player player = new Player(mSimulatorService, "Auto-" + String.valueOf(index + 1),
                    mSimulatorRule.playerBankroll);
            mPlayers.add(player);
        }

        // the players join the round
        mSimulatorService.pRound.setSimulationPlayers(mPlayers);

        // setting up result list view
        setupResultData();                      // initialize mResultDataList here

        mResultAdapter = new ResultAdapter(getActivity(),
                R.layout.row_simulator_chart, mResultDataList);
        mLvSimulatorResult.setAdapter(mResultAdapter);

        mProgressBarAutoRun.setMax(mSimulatorRule.mCountSimulationRuns);
        mProgressBarAutoRun.setProgress(0);

        ActivityStudio.post(new Runnable() {
            @Override
            public void run() {
                new AutoRunRounds().execute();
            }
        });
    }

    public void cancelSimulation() {
        getActivity().onBackPressed();
    }

    // for simulation result
    public void saveSimulationResult() {
    }

    public void resetSimulationResult() {
        layoutSimulationSetting.setVisibility(View.VISIBLE);
        layoutSimulationResult.setVisibility(View.GONE);
    }

    /**
     * simulator result list view handling
     */
    private static int[] sLineColors = ColorTemplate.JOYFUL_COLORS;
    private static int[] sScoreColors = ColorTemplate.PASTEL_COLORS;

    private static int[] sResultColors = new int[]      // lost-push-win
            {Color.rgb(200, 120, 120), Color.rgb(200, 200, 120), Color.rgb(120, 200, 120)};

    private static int[] sBackColors = ColorTemplate.PASTEL_COLORS;
    private ArrayList<String> mXVals_RunCount;
    private ArrayList<String> mXVals_Score, mXVals_Score_Dealer;
    private LimitLine mLimitLineInitial, mLimitLine3Q, mLimitLine2Q, mLimitLine1Q, mLimitLineZero;

    private YAxis.AxisDependency mPreferredYAxis = YAxis.AxisDependency.RIGHT;

    private int DEFAULT_ANIMATE_X = 1500, DEFAULT_ANIMATE_Y = 500;
    final float DEFAULT_DRAW_CIRCLE_LIMIT = 80;
    final float DEFAULT_DRAW_VALUE_LIMIT = 40;

    private void setupResultData() {
        // define for common use
        mResultDataList = new ArrayList<>();

        // generate xValues for common use - runCount
        resetXVals_RunCount();

        // generate xValues for common use - score
        mXVals_Score = new ArrayList<>();
        mXVals_Score.add("Sur.");  // 10 - total 11 index
        mXVals_Score.add("Split");      // 9
        mXVals_Score.add("DD");     // 8
        mXVals_Score.add("Bust");       // 7
        mXVals_Score.add("BJ");         // 6
        mXVals_Score.add("21");         // 5
        mXVals_Score.add("20");         // 4
        mXVals_Score.add("19");         // 3
        mXVals_Score.add("18");         // 2
        mXVals_Score.add("17");         // 1
        mXVals_Score.add("~16");        // 0

        // generate xValues for common use - dealer
        mXVals_Score_Dealer = new ArrayList<>();
        mXVals_Score_Dealer.add("Bust");  // 6 - total 7 index
        mXVals_Score_Dealer.add("BJ");    // 5
        mXVals_Score_Dealer.add("21");    // 4
        mXVals_Score_Dealer.add("20");    // 3
        mXVals_Score_Dealer.add("19");    // 2
        mXVals_Score_Dealer.add("18");    // 1
        mXVals_Score_Dealer.add("17");    // 0

        // Y-axis limit lines, for common use
        mLimitLineInitial = new LimitLine((float) mSimulatorRule.playerBankroll, "Initial");
        mLimitLineInitial.setLineColor(Color.GREEN);

        mLimitLine3Q = new LimitLine(0.75f * (float) mSimulatorRule.playerBankroll, "75%-Initial");
        mLimitLine3Q.setLineColor(Color.CYAN);

        mLimitLine2Q = new LimitLine(0.5f * (float) mSimulatorRule.playerBankroll, "50%-Initial");
        mLimitLine2Q.setLineColor(Color.CYAN);

        mLimitLine1Q = new LimitLine(0.25f * (float) mSimulatorRule.playerBankroll, "25%-Initial");
        mLimitLine1Q.setLineColor(Color.CYAN);

        mLimitLineZero = new LimitLine(0.0f * (float) mSimulatorRule.playerBankroll, "Bankrupt");
        mLimitLineZero.setLineColor(Color.RED);

        // [1]
        mResultBankroll_Line = makeBankroll_Line();
        mResultDataList.add(mResultBankroll_Line);

        // [2] - per player
        mResultBankroll_Candles = new ResultData[mSimulatorRule.mCountBoxes];
        for (int index = 0; index < mSimulatorRule.mCountBoxes; index++) {
            mResultBankroll_Candles[index] = makeBankroll_Candle(index);
            mResultDataList.add(mResultBankroll_Candles[index]);
        }

        // [3]
        mResultRound_StackBar = makeRound_StackBar();
        mResultDataList.add(mResultRound_StackBar);

        // [4]
        mResultDealerScore_StackBar = makeDealerScore_StackBar();
        mResultDataList.add(mResultDealerScore_StackBar);

        // [5]  - per player
        mResultPlayerScore_StackBars = new ResultData[mSimulatorRule.mCountBoxes];
        for (int index = 0; index < mSimulatorRule.mCountBoxes; index++) {
            mResultPlayerScore_StackBars[index] = makePlayerScore_StackBar(index);
            mResultDataList.add(mResultPlayerScore_StackBars[index]);
        }

        // chart initialization of all
        for (ResultData resultData : mResultDataList) {
            resultData.initializeChart();
        }
    }

    abstract private class ResultData {
        public String mTitle;
        public int mBoxIndex;           // if we need for indexing boxs
        public Chart mChart = null;     // Chart is also a ViewGroup
        public boolean mShowInProgress = true;
        public boolean mShowInFinal = true;

        public ResultData(String title, int boxIndex, boolean showInProgress, boolean showInFinal) {
            mTitle = title;
            mBoxIndex = boxIndex;
            mShowInProgress = showInProgress;
            mShowInFinal = showInFinal;
        }

        public ResultData(String title, boolean showInProgress, boolean showInFinal) {
            mTitle = title;
            mBoxIndex = -1;                        // we do not need indexing
            mShowInProgress = showInProgress;
            mShowInFinal = showInFinal;
        }

        public boolean shownInProgress() {
            return mShowInProgress;
        }

        public boolean shownInFinal() {
            return mShowInFinal;
        }

        public boolean needChartUpdate() {
            if (mChart == null) return false;
            return ((shownInProgress() && !mResultAdapter.hasProgressDone()) ||
                    (shownInFinal() && mResultAdapter.hasProgressDone()));
        }

        abstract void initializeChart();

        abstract float getChartHeightMultiplier();

        abstract void updateProgress(int currentRun, float[]... progressData);

        public void refreshChart() {
            if (mChart != null) {
                mChart.notifyDataSetChanged();
                mChart.invalidate();
            }
        }

        public void animateChart() {
            if (mChart != null) {
                if (mChart instanceof HorizontalBarChart) {
                    mChart.animateXY(DEFAULT_ANIMATE_Y, DEFAULT_ANIMATE_X);
                } else {
                    mChart.animateXY(DEFAULT_ANIMATE_X, DEFAULT_ANIMATE_Y);
                }
            }
        }

        public class DecimalValueFormatter implements ValueFormatter, YAxisValueFormatter {

            private DecimalFormat mFormat;

            public DecimalValueFormatter(int decimal) {
                String formatString = (decimal > 0) ? "###,###,##0." : "###,###,##0";
                while (decimal-- > 0) formatString = formatString + "0";
                mFormat = new DecimalFormat(formatString);
            }

            private String avoidMinusZero(String inStr) {
                if (inStr.replace("0", "").replace(".", "").replace(",", "").equals("-")) {
                    inStr = inStr.replace("-", "");
                }
                return inStr;
            }

            @Override
            public String getFormattedValue(float value, YAxis yAxis) {
                return avoidMinusZero(mFormat.format(value));
            }

            @Override
            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                return avoidMinusZero(mFormat.format(value));
            }
        }

        public class DollarValueFormatter extends DecimalValueFormatter {
            public DollarValueFormatter(int decimal) {
                super(decimal);
            }

            @Override
            public String getFormattedValue(float value, YAxis yAxis) {
                return ("$" + super.getFormattedValue(value, yAxis));
            }

            @Override
            public String getFormattedValue(float value, Entry entry, int dataSetIndex,
                                            ViewPortHandler viewPortHandler) {
                return ("$" + super.getFormattedValue(value, entry, dataSetIndex, viewPortHandler));
            }
        }

        public class PercentValueFormatter extends DecimalValueFormatter {
            boolean mHideZeroPercent;

            public PercentValueFormatter(int decimal, boolean hideZero) {
                super(decimal);
                mHideZeroPercent = hideZero;
            }

            @Override
            public String getFormattedValue(float value, YAxis yAxis) {
                String outStr = super.getFormattedValue(value, yAxis);
                if (mHideZeroPercent
                        && (outStr.replace("0", "").replace(".", "").isEmpty()))
                    return "";

                return (outStr + "%");
            }

            @Override
            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                String outStr = super.getFormattedValue(value, entry, dataSetIndex, viewPortHandler);
                if (mHideZeroPercent
                        && (outStr.replace("0", "").replace(".", "").isEmpty()))
                    return "";

                return (outStr + "%");
            }
        }

        public class RelativePercentFormatter extends PercentValueFormatter {
            float mBaseValue;

            public RelativePercentFormatter(float baseValue, int decimal, boolean hideZero) {
                super(decimal, hideZero);
                mBaseValue = baseValue;
            }

            @Override
            public String getFormattedValue(float value, YAxis yAxis) {
                if (mBaseValue != 0.0f) value = value * mBaseValue;
                return (super.getFormattedValue(value, yAxis));
            }

            @Override
            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                if (mBaseValue != 0.0f) value = value * mBaseValue;
                return (super.getFormattedValue(value, entry, dataSetIndex, viewPortHandler));
            }

            public void setBaseValue(float baseValue) {
                mBaseValue = baseValue;
            }

            public float getBaseValue() {
                return mBaseValue;
            }
        }
    }

    private static int sGetViewCount = 0;

    public class ResultAdapter extends ArrayAdapter<ResultData> {
        private boolean mProgressDone = false;

        public ResultAdapter(Context context, int resource, ArrayList<ResultData> data) {
            super(context, resource, data);
            sGetViewCount = 0;  // for log, TODO: remove later
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View itemView = null;
            ResultDataHandler handler = null;

            if (convertView != null) {
                handler = (ResultDataHandler) convertView.getTag();
                if (handler.mPosition == position)
                    itemView = convertView;
            }

            if (itemView == null) {
                LayoutInflater inflater = (LayoutInflater) this.getContext()
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                itemView = inflater.inflate(R.layout.row_simulator_chart, parent, false);

                handler = new ResultDataHandler(itemView, position);
                itemView.setTag(handler);
            }

            // shown or not shown by the condition setting
            ResultData resultData = ResultAdapter.this.getItem(position);

            if (resultData.needChartUpdate()) {
                itemView.setVisibility(View.VISIBLE);
                handler.setData(position);
            } else {
                itemView.setVisibility(View.GONE);
                return null;
            }
            return itemView;
        }

        public void setProgressDone(boolean done) {
            mProgressDone = done;
        }

        public boolean hasProgressDone() {
            return mProgressDone;
        }

        private class ResultDataHandler {
            int mPosition;
            FrameLayout mFrameLayoutChart;
            TextView mTvChartTitle;
            int mChartHeight;

            public ResultDataHandler(View view, int position) {
                mPosition = position;
                ResultData resultData = ResultAdapter.this.getItem(position);

                mTvChartTitle = (TextView) view.findViewById(R.id.tvRowChartTitle_Simulator);
                mTvChartTitle.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mFrameLayoutChart.getVisibility() == View.GONE) {
                            mFrameLayoutChart.setVisibility(View.VISIBLE);
                        } else {
                            mFrameLayoutChart.setVisibility(View.GONE);
                        }
                    }
                });

                mFrameLayoutChart = (FrameLayout) view.findViewById(R.id.flRowChart_Simulator);
                mChartHeight = (int) ((float) mFrameLayoutChart.getLayoutParams().height *
                        resultData.getChartHeightMultiplier());
                mFrameLayoutChart.setLayoutParams(mFrameLayoutChart.getLayoutParams());
            }

            public void setData(int position) {
                ResultData resultData = ResultAdapter.this.getItem(position);
                mTvChartTitle.setText(resultData.mTitle);

                View chart = resultData.mChart;
                if (chart == null) {    // no chart, then nothing needed in mFrameLayoutChart
                    mFrameLayoutChart.removeAllViews();
                } else {
                    ViewParent parent = chart.getParent();  // if parent, check if correct parent
                    if ((parent != null) && (parent != mFrameLayoutChart)) {
                        ((ViewGroup) parent).removeView(resultData.mChart);
                        parent = null;
                    }
                    if (parent == null) {
                        mFrameLayoutChart.removeAllViews();
                        mFrameLayoutChart.getLayoutParams().height = mChartHeight;
                        mFrameLayoutChart.setLayoutParams(mFrameLayoutChart.getLayoutParams());

                        mFrameLayoutChart.addView(chart);
                        resultData.refreshChart();
                    }
                }
            }
        }

        public void updateResultProgress(int currentRun, float[]... progress) {
            for (ResultData resultData : mResultDataList) {
                if (resultData.needChartUpdate()) {
                    resultData.updateProgress(currentRun, progress);
                    resultData.refreshChart();
                }
                if (currentRun == mSimulatorRule.mCountSimulationRuns) {
                    resultData.animateChart();
                }
            }
            notifyDataSetChanged();
        }
    }

    private void adjustDataSetDrawExtra(DataSet dataSet, int nCount) {
        if (mSimulatorRule.mCountSimulationRuns > DEFAULT_DRAW_CIRCLE_LIMIT) {
            dataSet.setDrawValues(false);
            if (dataSet instanceof LineDataSet) {
                ((LineDataSet) dataSet).setCircleSize(0f);
            }
        } else {
            dataSet.setDrawValues((nCount <= DEFAULT_DRAW_VALUE_LIMIT));
            if (dataSet instanceof LineDataSet) {
                ((LineDataSet) dataSet).setCircleSize((nCount <= DEFAULT_DRAW_CIRCLE_LIMIT) ? 1.5f : 0f);
            }
        }
    }

    //=== making various result data ==============================================================
    // [1]
    ResultData mResultBankroll_Line;

    private ResultData makeBankroll_Line() {
        boolean inProgress = mSimulatorRule.showProgressUpdateCondition !=
                Rule.ProgressUpdateCondition.JUST_FINAL;
        boolean inFinal = inProgress;

        return new ResultData("Bankroll Change", inProgress, inFinal) {
            LineChart mChartFocused;
            ArrayList<LineDataSet> mBankrollDataSets;

            @Override
            void initializeChart() {
                // make chart
                mChart = mChartFocused = new LineChart(getActivity());

                // chart basic information
                mChartFocused.setDescription("");   // to hide
                mChartFocused.setNoDataText("Bankroll-Line Not Working");

                // set initial chart data - line data set, per player
                mBankrollDataSets = new ArrayList<>();

                // set initial chart data - dealer balance dataSet and y Axis
                ArrayList<Entry> yVals = new ArrayList<>();       // initial entry at start
                Entry initialEntry = new Entry((float) mSimulatorService.pDealer.getBalance(), 0);
                yVals.add(initialEntry);
                LineDataSet dataSet = new LineDataSet(yVals, "Dealer");
                dataSet.setAxisDependency((mPreferredYAxis == YAxis.AxisDependency.RIGHT) ?
                        YAxis.AxisDependency.LEFT : YAxis.AxisDependency.RIGHT);    // not player
                YAxis dealerAxis = (mPreferredYAxis == YAxis.AxisDependency.RIGHT) ?
                        mChartFocused.getAxisLeft() : mChartFocused.getAxisRight();
                dealerAxis.setAxisLineColor(Color.BLUE);
                dealerAxis.setTextColor(Color.BLUE);
                dealerAxis.setStartAtZero(false);
                dealerAxis.setValueFormatter(new DollarValueFormatter(0));
                dataSet.setCircleColor(Color.BLUE);
                dataSet.setColor(Color.BLUE);        // red color for dealer, set axis later color
                dataSet.setLineWidth(1.5f);           // a little more thick
                mBankrollDataSets.add(dataSet);     // first dataSet is for dealer balance

                // set initial chart data - line data set, per player
                for (int index = 0; index < mSimulatorRule.mCountBoxes; index++) {

                    yVals = new ArrayList<>();       // initial entry at start
                    initialEntry = new Entry((float) mSimulatorRule.playerBankroll, 0);
                    yVals.add(initialEntry);
                    dataSet = new LineDataSet(yVals,
                            mSimulatorService.getBoxByIndex(index).getUniqueId());
                    dataSet.setAxisDependency(mPreferredYAxis);
                    mBankrollDataSets.add(dataSet);
                    dataSet.setCircleColor(sLineColors[index % sLineColors.length]);
                    dataSet.setColor(sLineColors[index % sLineColors.length]);
                }

                // set initial chart data - chart data
                LineData lineData = new LineData(mXVals_RunCount, mBankrollDataSets);
                mChartFocused.setData(lineData);

                // x-Axis setup
                XAxis xAxis = mChartFocused.getXAxis();
                xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                xAxis.setAvoidFirstLastClipping(true);

                // Y-axis format
                YAxis playerAxis = (mPreferredYAxis == YAxis.AxisDependency.RIGHT) ?
                        mChartFocused.getAxisRight() : mChartFocused.getAxisLeft();
                playerAxis.setValueFormatter(new DollarValueFormatter(0));

                // Y-axis limit lines
                playerAxis.addLimitLine(mLimitLineInitial);
                playerAxis.addLimitLine(mLimitLine3Q);
                playerAxis.addLimitLine(mLimitLine2Q);
                playerAxis.addLimitLine(mLimitLine1Q);
                playerAxis.addLimitLine(mLimitLineZero);

                // Y-axis initial max-min, and automatic
                playerAxis.setStartAtZero(false);
                mChartFocused.setAutoScaleMinMaxEnabled(true);
            }

            @Override
            public float getChartHeightMultiplier() {
                return 1.3f;
            }

            @Override
            void updateProgress(int currentRun, float[]... progressData) {
                float[] bankroll_candle_value = progressData[PROGRESS_INDEX_BANKROLL_CANDLE_VALUE];

                // get dataSet for dealer balance, and add x value if needed
                LineDataSet dataSet = mBankrollDataSets.get(0); // for dealer balance
                int nLast = dataSet.getEntryCount();
                if (nLast >= mXVals_RunCount.size())
                    mXVals_RunCount.add(String.valueOf(nLast));
                Entry entry = new Entry(bankroll_candle_value[0], nLast);
                dataSet.addEntry(entry);
                adjustDataSetDrawExtra(dataSet, nLast);

                for (int index = 0; index < mSimulatorRule.mCountBoxes; index++) {
                    dataSet = mBankrollDataSets.get(index + 1);     // 1 for dealer balance dataSet
                    float closedBankroll = bankroll_candle_value[1 + COUNT_CANDLE_PER_PLAYER * index + 3];
                    entry = new Entry(closedBankroll, nLast);
                    dataSet.addEntry(entry);
                    adjustDataSetDrawExtra(dataSet, nLast);
                }
            }
        };
    }

    // [2]
    private ResultData[] mResultBankroll_Candles;
    final int COUNT_CANDLE_PER_PLAYER = 4;

    private ResultData makeBankroll_Candle(int index) {
        boolean inProgress = mSimulatorRule.showProgressUpdateCondition !=
                Rule.ProgressUpdateCondition.JUST_FINAL;
        boolean inFinal = inProgress;

        return new ResultData("Bankroll of " +
                mSimulatorService.getBoxByIndex(index).getUniqueId(), index, inProgress, inFinal) {

            final float DEFAULT_X_VIEW_RANGE_MAX = 60f;
            final float DEFAULT_X_VIEW_RANGE_MIN = 10f;

            CandleStickChart mChartFocused;
            CandleDataSet mDataSetFocused;

            @Override
            void initializeChart() {
                // make chart view dynamically
                mChart = mChartFocused = new CandleStickChart(getActivity());

                // chart basic information
                mChartFocused.setDescription("");   // to hide
                mChartFocused.setNoDataText("Bankroll-Candle Not Working");

                // set initial chart data - y values
                ArrayList<CandleEntry> yVals = new ArrayList<>();   // initial entry at start
                float initialBankroll = (float) mSimulatorRule.playerBankroll;
                CandleEntry initialEntry = new CandleEntry(0, initialBankroll, initialBankroll,
                        initialBankroll, initialBankroll);
                yVals.add(initialEntry);

                mDataSetFocused = new CandleDataSet(yVals, mTitle);
                mDataSetFocused.setDrawValues(false);
                mDataSetFocused.setAxisDependency(mPreferredYAxis);

                // set initial chart data - chart data
                CandleData candleData = new CandleData(mXVals_RunCount, mDataSetFocused);
                mChartFocused.setData(candleData);          // setting candle data

                // x-Axis setup
                XAxis xAxis = mChartFocused.getXAxis();
                xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                xAxis.setAvoidFirstLastClipping(true);

                // no right Y-axis
                YAxis yAxis = (mPreferredYAxis == YAxis.AxisDependency.RIGHT) ?
                        mChartFocused.getAxisLeft() : mChartFocused.getAxisRight();
                yAxis.setEnabled(false);

                // Y-axis format
                yAxis = (mPreferredYAxis == YAxis.AxisDependency.RIGHT) ?
                        mChartFocused.getAxisRight() : mChartFocused.getAxisLeft();
                yAxis.setValueFormatter(new DollarValueFormatter(0));

                // Y-axis limit lines
                yAxis.addLimitLine(mLimitLineInitial);
                yAxis.addLimitLine(mLimitLine3Q);
                yAxis.addLimitLine(mLimitLine2Q);
                yAxis.addLimitLine(mLimitLine1Q);
                yAxis.addLimitLine(mLimitLineZero);

                // Y-axis initial max-min, and automatic min-max
                yAxis.setStartAtZero(false);
                mChartFocused.setAutoScaleMinMaxEnabled(true);

                // coloring elements
                mDataSetFocused.setIncreasingColor(Color.MAGENTA);
                mDataSetFocused.setDecreasingColor(Color.BLUE);
                mDataSetFocused.setShadowColor(Color.BLACK);

                // Legend
                mChartFocused.getLegend().setEnabled(false);

                // view
                mChartFocused.setVisibleXRangeMaximum(DEFAULT_X_VIEW_RANGE_MAX);
                mChartFocused.setVisibleXRangeMinimum(DEFAULT_X_VIEW_RANGE_MIN);
                mChartFocused.moveViewToX
                        (mDataSetFocused.getEntryCount() - DEFAULT_X_VIEW_RANGE_MAX);
            }

            @Override
            float getChartHeightMultiplier() {
                return 1.0f;
            }

            @Override
            void updateProgress(int currentRun, float[]... progressData) {
                float[] bankroll_candle_value = progressData[PROGRESS_INDEX_BANKROLL_CANDLE_VALUE];

                // add new entry at last
                int nLast = mDataSetFocused.getEntryCount();
                if (nLast >= mXVals_RunCount.size())
                    mXVals_RunCount.add(String.valueOf(nLast));

                int perIndex = 1 + COUNT_CANDLE_PER_PLAYER * mBoxIndex;    // 1 for first dealer balance
                CandleEntry newEntry = new CandleEntry(nLast, bankroll_candle_value[perIndex],
                        bankroll_candle_value[perIndex + 1],
                        bankroll_candle_value[perIndex + 2],
                        bankroll_candle_value[perIndex + 3]);
                mDataSetFocused.addEntry(newEntry);

                mChartFocused.setVisibleXRangeMaximum(DEFAULT_X_VIEW_RANGE_MAX);
                mChartFocused.setVisibleXRangeMinimum(DEFAULT_X_VIEW_RANGE_MIN);
                mChartFocused.moveViewToX                   // this calls invalidate()
                        (mDataSetFocused.getEntryCount() - DEFAULT_X_VIEW_RANGE_MAX);
            }
        };
    }

    // [3]
    private ResultData mResultRound_StackBar;

    private ResultData makeRound_StackBar() {
        boolean inProgress = mSimulatorRule.showProgressUpdateCondition ==
                Rule.ProgressUpdateCondition.ALL;
        boolean inFinal = true;

        return new ResultData("Round Results", inProgress, inFinal) {
            HorizontalBarChart mChartFocused;
            BarEntry[] mRoundEntry;

            @Override
            void initializeChart() {
                mRoundEntry = new BarEntry[mSimulatorRule.mCountBoxes];

                // make chart
                mChart = mChartFocused = new HorizontalBarChart(getActivity());

                // chart basic information
                mChartFocused.setDescription("");   // to hide
                mChartFocused.setNoDataText("Bar Chart Not Working");

                // set initial chart data - x values
                ArrayList<String> xVals = new ArrayList<>();
                for (int index = 0; index < mSimulatorRule.mCountBoxes; index++) {
                    xVals.add(mSimulatorService.getBoxByIndex(index).getUniqueId());
                }

                // set initial chart data - bar data set, per player

                ArrayList<BarEntry> roundData = new ArrayList<>();
                for (int index = 0; index < mSimulatorRule.mCountBoxes; index++) {
                    mRoundEntry[index] =
                            new BarEntry(new float[]{33, 33, 33}, index,
                                    mSimulatorService.getBoxByIndex(index).getUniqueId());
                    roundData.add(mRoundEntry[index]);
                }

                BarDataSet barDataSet = new BarDataSet(roundData, "Round Result");
                barDataSet.setColors(sResultColors);
                barDataSet.setStackLabels(new String[]{"Lost", "Push", "Win"});

                ArrayList<BarDataSet> dataSets;
                dataSets = new ArrayList<>();
                dataSets.add(barDataSet);

                // now, make bar data
                BarData barData = new BarData(xVals, dataSets);
                mChartFocused.setData(barData);

                mChartFocused.setDescription("");
                mChartFocused.setDescriptionColor(UtilityStudio.sColor_HandHighlight);
                barData.setValueFormatter(new PercentValueFormatter(2, true));      // hide zero

                // X-Axis format
                XAxis xAxis = mChartFocused.getXAxis();
                xAxis.setPosition((mPreferredYAxis == YAxis.AxisDependency.RIGHT) ?
                        XAxis.XAxisPosition.TOP : XAxis.XAxisPosition.BOTTOM);
                xAxis.setAvoidFirstLastClipping(true);

                // remove one Y axis
                YAxis rightAxis = mChartFocused.getAxisRight();
                rightAxis.setEnabled(false);

                // Y-axis format
                YAxis leftAxis = mChartFocused.getAxisLeft();
                leftAxis.setValueFormatter(new PercentValueFormatter(0, false));    // show zero

                // value position
                mChartFocused.setDrawValueAboveBar(false);

                // min-max
                mChartFocused.setAutoScaleMinMaxEnabled(true);
            }

            @Override
            float getChartHeightMultiplier() {
                if (mChart instanceof HorizontalBarChart) {
                    return (0.3f * mSimulatorRule.mCountBoxes);
                }
                return 1.0f;
            }

            @Override
            void updateProgress(int currentRun, float[]... progressData) {
                float[] resultValues = progressData[PROGRESS_INDEX_RESULT_VALUE];

                if (currentRun >= 0) {
                    int handIndex;
                    int index = 0;
                    for (handIndex = 0; handIndex < mSimulatorRule.mCountBoxes; handIndex++) {
                        mRoundEntry[handIndex].setVals(new float[]{resultValues[index++],
                                resultValues[index++], resultValues[index++]});
                    }
                }
            }
        };
    }

    // [5]
    private ResultData mResultDealerScore_StackBar;
    final int COUNT_SCORE_PER_DEALER = 7;

    private ResultData makeDealerScore_StackBar() {
        boolean inProgress = mSimulatorRule.showProgressUpdateCondition ==
                Rule.ProgressUpdateCondition.ALL;
        boolean inFinal = true;

        return new ResultData("Final Score of Dealer", inProgress, inFinal) {
            HorizontalBarChart mChartFocused;
            BarDataSet mDataSetFocused;
            YAxis mYAxis;
            float maxVal = Float.MIN_VALUE;

            @Override
            void initializeChart() {
                mChart = mChartFocused =
                        new HorizontalBarChart(getActivity());

                // chart basic information
                mChartFocused.setDescription("");   // to hide
                mChartFocused.setNoDataText("Bar Chart Not Working");

                ArrayList<BarEntry> yVals = new ArrayList<>();
                for (int index = 0; index < COUNT_SCORE_PER_DEALER; index++) {
                    yVals.add(new BarEntry(0, index));
                }
                mDataSetFocused = new BarDataSet(yVals, "Dealer");
                mDataSetFocused.setColors(sScoreColors);
                mDataSetFocused.setValueFormatter(new PercentValueFormatter(2, true));    // hide zero

                BarData barData = new BarData(mXVals_Score_Dealer, mDataSetFocused);
                mChartFocused.setData(barData);

                // X-Axis format
                XAxis xAxis = mChartFocused.getXAxis();
                xAxis.setPosition((mPreferredYAxis == YAxis.AxisDependency.RIGHT) ?
                        XAxis.XAxisPosition.TOP : XAxis.XAxisPosition.BOTTOM);
                xAxis.setAvoidFirstLastClipping(true);

                // no right Y-axis
                YAxis rightAxis = mChartFocused.getAxisRight();
                rightAxis.setEnabled(false);

                // Y-axis format
                mYAxis = mChartFocused.getAxisLeft();
                mYAxis.setValueFormatter(new PercentValueFormatter(0, false));    // show zero
                mYAxis.setAxisMinValue(0);

                // value position
                mChartFocused.setDrawValueAboveBar(true);

                // min-max
                mChartFocused.setAutoScaleMinMaxEnabled(true);

                // Legend
                mChartFocused.getLegend().setEnabled(false);
            }

            @Override
            float getChartHeightMultiplier() {
                if (mChart instanceof HorizontalBarChart) {
                    return (0.15f * COUNT_SCORE_PER_DEALER);
                }
                return 1.0f;
            }

            @Override
            void updateProgress(int currentRun, float[]... progressData) {
                float[] scoreValues = progressData[PROGRESS_INDEX_DEALER_SCORE_VALUE];

                int index = 0;
                for (int scoreIndex = 0; scoreIndex < COUNT_SCORE_PER_DEALER; scoreIndex++) {
                    if (scoreValues[index] > maxVal) maxVal = scoreValues[index];
                    BarEntry entry = mDataSetFocused.getEntryForXIndex(scoreIndex);
                    entry.setVal(scoreValues[index++]);
                }
                mYAxis.setAxisMaxValue(maxVal * 1.05f);
            }
        };
    }

    // [4]
    private ResultData[] mResultPlayerScore_StackBars;

    final int COUNT_SCORE_PER_PLAYER = 11;
    final int COUNT_RESULT_PER_SCORE = 3;

    private ResultData makePlayerScore_StackBar(int index) {
        boolean inProgress = mSimulatorRule.showProgressUpdateCondition ==
                Rule.ProgressUpdateCondition.ALL;
        boolean inFinal = true;

        return new ResultData("Final Score of " +
                mSimulatorService.getBoxByIndex(index).getUniqueId(), index, inProgress, inFinal) {
            HorizontalBarChart mChartFocused;
            BarDataSet mDataSetFocused;

            @Override
            void initializeChart() {
                mChart = mChartFocused =
                        new HorizontalBarChart(getActivity());

                // chart basic information
                mChartFocused.setDescription("");   // to hide
                mChartFocused.setNoDataText("Bar Chart Not Working");

                ArrayList<BarEntry> yVals = new ArrayList<>();
                for (int index = 0; index < COUNT_SCORE_PER_PLAYER; index++) {
                    yVals.add(new BarEntry(new float[]{20, 20, 20}, index));
                }
                mDataSetFocused = new BarDataSet(yVals,
                        mSimulatorService.getBoxByIndex(mBoxIndex).getUniqueId());
                mDataSetFocused.setColors(sResultColors);
                mDataSetFocused.setValueFormatter(new PercentValueFormatter(2, true));    // hide zero
                mDataSetFocused.setStackLabels(new String[]{"Lost", "Push", "Win"});

                BarData barData = new BarData(mXVals_Score, mDataSetFocused);
                mChartFocused.setData(barData);

                // X-Axis format
                XAxis xAxis = mChartFocused.getXAxis();
                xAxis.setPosition((mPreferredYAxis == YAxis.AxisDependency.RIGHT) ?
                        XAxis.XAxisPosition.TOP : XAxis.XAxisPosition.BOTTOM);
                xAxis.setLabelsToSkip(0);
                xAxis.setAvoidFirstLastClipping(true);

                // no right Y-axis
                YAxis rightAxis = mChartFocused.getAxisRight();
                rightAxis.setEnabled(false);

                // Y-axis format
                YAxis leftAxis = mChartFocused.getAxisLeft();
                leftAxis.setValueFormatter(new PercentValueFormatter(0, false));    // show zero

                // value position
                mChartFocused.setDrawValueAboveBar(false);

                // min-max
                mChartFocused.setAutoScaleMinMaxEnabled(true);
            }

            @Override
            float getChartHeightMultiplier() {
                if (mChart instanceof HorizontalBarChart) {
                    return (0.15f * COUNT_SCORE_PER_PLAYER);
                }
                return 1.0f;
            }

            @Override
            void updateProgress(int currentRun, float[]... progressData) {
                float[] scoreValues = progressData[PROGRESS_INDEX_PLAYER_SCORE_VALUE];

                int index = COUNT_RESULT_PER_SCORE * COUNT_SCORE_PER_PLAYER * mBoxIndex;

                for (int scoreIndex = 0; scoreIndex < COUNT_SCORE_PER_PLAYER; scoreIndex++) {
                    BarEntry entry = mDataSetFocused.getEntryForXIndex(scoreIndex);
                    float[] entryValues = new float[]
                            {scoreValues[index++], scoreValues[index++], scoreValues[index++]};
                    entry.setVals(entryValues);

                    float baseValue =
                            100.0f / (entryValues[0] + entryValues[1] + entryValues[2]); // normalize
                    entry.setValueFormatter(new RelativePercentFormatter(baseValue, 2, true)); // hide zero
                }
            }
        };
    }
}