package com.lgsdiamond.blackjackstudio.BlackjackElement;

import android.support.annotation.Nullable;

import com.lgsdiamond.blackjackstudio.ActivityStudio;

import java.util.ArrayList;

/**
 * Created by lgsdiamond on 2015-10-02.
 */
public class BettingBox {
    private Rule mRule;
    private final int mIndex;
    private Player mSittingPlayer;
    private Strategy mStrategy = null;
    private Better mBetter = null;
    private double mBaseBet;
    private double mLastBet;
    private double mProposedNextBet;

    private double mCycleWin;

    private BjService.RoundResult mLastRoundResult;
    private int mLastCutLevelCount;

    public BoxData mRecord;

    public BettingBox(int index, Rule rule) {
        mIndex = index;
        mRule = rule;

        mBaseBet = Math.max(rule.mBaseBet, rule.minBet);
        mLastBet = 0.0;
        mLastRoundResult = BjService.RoundResult.UNKNOWN;

        mRecord = new BoxData();

        ArrayList<Strategy> strategies = ActivityStudio.sStudio.getStrategies();
        ArrayList<Better> bettings = ActivityStudio.sStudio.getBetters();
        int strategyIndex = index % rule.mHandStrategyIndex.length;
        int bettingIndex = index % rule.mHandBetterIndex.length;

        Strategy action = strategies.get(rule.mHandStrategyIndex[strategyIndex]);
        Better betting = bettings.get(rule.mHandBetterIndex[bettingIndex]);

        setStrategy(action);
        setBetter(betting);
    }

    public class BoxData {
        int betCount;
        double totalBet;
        double maxBet;
        double minBet;

        public BoxData() {
            betCount = 0;
            totalBet = 0.0;
            maxBet = Double.MIN_VALUE;
            minBet = Double.MAX_VALUE;
        }
    }

    public String getUniqueId() {
        String uniqueId = "";
        if (mStrategy != null) uniqueId += mStrategy.getUniqueId();
        uniqueId += ":";
        if (mBetter != null) uniqueId += mBetter.getUniqueId();
        return uniqueId;
    }

    public int getIndex() {
        return mIndex;
    }

    //=== Strategy ===
    public void setStrategy(@Nullable Strategy strategy) {
        mStrategy = strategy;
    }

    public Strategy getStrategy() {
        return mStrategy;
    }

    //=== Better ===
    public void setBetter(@Nullable Better better) {
        mBetter = better;
    }

    public Better getBetter() {
        return mBetter;
    }

    public void setSittingPlayer(Player player) {
        mSittingPlayer = player;
    }

    public Player getSittingPlayer() {
        return mSittingPlayer;
    }

    public double getNextBet() {
        double nextBet;

        if (mBaseBet < mRule.minBet) {
            // if minBet is bigger than baseBet, baseBet need to be adjusted to minBet
            mBaseBet = mRule.minBet;
            resetCycle();
            nextBet = mBaseBet;
        } else {
            nextBet = (mBetter == null) ?
                    ((mLastBet == 0.0) ? mBaseBet : mLastBet) : mBetter.getNextBet(this);

            nextBet = makeValidBet(nextBet);
            mProposedNextBet = nextBet;

        }
        return nextBet;
    }

    private double makeValidBet(double bet) {
        // check min $ max bet of the table
        if (bet > mRule.maxBet) bet = mRule.maxBet;
        if (bet < mRule.minBet) bet = mRule.minBet;

        // check player's bankroll
        if ((bet > mSittingPlayer.getBankroll()) &&
                !mRule.allowNegativeBankroll) {   // not enough bankroll
            bet = mSittingPlayer.getBankroll();
        }

        // make more natural bet
        bet = bet - (bet % 1.0);        // no decimal point value
        if (bet >= 100.0) {
            bet = bet - (bet % 10.0);   // multiple of 10
        } else if (bet >= 50.0) {
            bet = bet - (bet % 5.0);    // multiple of 5
        }

        return bet;
    }

    public double getLastBet() {
        return mLastBet;
    }

    public BjService.RoundResult getLastRoundResult() {
        return mLastRoundResult;
    }

    public void setLastRoundResultByWinAmount(double winAmount) {

        if (winAmount > 0.0) {
            mLastRoundResult = BjService.RoundResult.WIN;
            addCycleWin(winAmount);
        } else if (winAmount < 0.0) {
            mLastRoundResult = BjService.RoundResult.LOST;
            addCycleWin(winAmount);
        } else {
            mLastRoundResult = BjService.RoundResult.PUSH;
        }
    }

    public double getBankroll() {
        return mSittingPlayer.getBankroll();
    }

    public int getLastCutLevelCount() {
        return mLastCutLevelCount;
    }

    public int increaseLastCutLevelCount() {
        mLastCutLevelCount++;
        return mLastCutLevelCount;
    }

    public void setLastCutLevelCount(int count) {
        mLastCutLevelCount = count;
    }

    public void confirmBet(double bet) {
        if (bet != mProposedNextBet) {
            mProposedNextBet = 0.0;             // no use any longer
            mBaseBet = bet;                     // new baseBet
            resetCycle();                       // start new cycle
        }

        mLastBet = bet;

        mRecord.betCount++;
        mRecord.totalBet += bet;
        if (bet > mRecord.maxBet) mRecord.maxBet = bet;
        if (bet < mRecord.minBet) mRecord.minBet = bet;
    }

    public double getBaseBet() {
        return mBaseBet;
    }

    public double getCycleWin() {
        return mCycleWin;
    }

    public void addCycleWin(double win) {
        mCycleWin += win;
    }

    public void resetCycle() {
        mLastCutLevelCount = 0;
        mCycleWin = 0.0;
    }
}