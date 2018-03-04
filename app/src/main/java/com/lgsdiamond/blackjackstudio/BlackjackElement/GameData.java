package com.lgsdiamond.blackjackstudio.BlackjackElement;

import android.support.annotation.Nullable;
import android.widget.TextView;

import com.lgsdiamond.blackjackstudio.BlackjackUtils.UtilityStudio;
import com.lgsdiamond.blackjackstudio.FragmentTable;
import com.lgsdiamond.blackjackstudio.R;

import java.util.ArrayList;

/**
 * Created by lgsdiamond on 2015-10-12.
 */
public class GameData extends ArrayList<ArrayList<GameData.TextData>> {
    FragmentTable mTable;
    BjService mService;

    ShoeData mShoeData;
    DealerData mDealerData;
    PlayerData mPlayerData;

    AutoRunData mAutoRunData;

    boolean mDrawUiStuff = false;

    // constructor
    public GameData(FragmentTable table, BjService service) {
        mTable = table;
        mService = service;

        // should follow create Data
        add(mShoeData = new ShoeData());
        add(mDealerData = new DealerData());
        add(mPlayerData = new PlayerData());

        mAutoRunData = null;    // we create when we need AutoRun
    }

    public void populateAll() {
        if (BjService.isAutoRunning()) return;

        mDrawUiStuff = true;
        for (ArrayList<TextData> dataSet : this) {
            for (TextData textData : dataSet) {
                textData.Refresh();
            }
        }
        mDrawUiStuff = false;
    }

    public void populatePlayerBankroll() {
        if (BjService.isAutoRunning()) return;

        mDrawUiStuff = true;
        for (TextData textData : mPlayerData) {
            textData.Refresh();
        }
        mDrawUiStuff = false;
    }

    public boolean getDrawDataFlag() {
        return (mDrawUiStuff);
    }

    public void setDrawDataFlag(boolean fDraw) {
        mDrawUiStuff = fDraw;
    }

    public void resetData() {
        // shoe data
        mShoeData.mPotChange.setData(0.0);
        mShoeData.mShoeCount.setCountInitial(mService.pShoe.getCountInitial());
        mShoeData.mShoeCount.setData(mService.pShoe.getCountRemaining());

        // dealer data
        mDealerData.mBlackjackHands.setData(0);
        mDealerData.mBustHands.setData(0);
        mDealerData.mDealerRounds.setData(0);

        // player data
        mPlayerData.mTotalHands.setData(0);

        mPlayerData.mBlackjackHands.setData(0);
        mPlayerData.mBustHands.setData(0);
        mPlayerData.mLastTotalBet.setData(0.0);
        mPlayerData.mLastWinningChange.setData(0.0);

        mPlayerData.mLostHands.setData(0);
        mPlayerData.mPushHands.setData(0);
        mPlayerData.mWinHands.setData(0);
    }

    //=== sub-classes ===
    class TextData<T> {
        TextView mView;
        T mData;

        public TextData(T data, @Nullable TextView view) {
            mView = view;
            setData(data);
        }

        // getter
        public T getData() {
            return mData;
        }

        @Override
        public String toString() {
            String text;
            if (mData instanceof String) {
                text = (String) mData;
            } else {
                text = String.valueOf(mData); // +"("+String.format("%.1f", 12.34567)+")"
            }
            return text;
        }

        public void setData(T data) {
            mData = data;
            if (mDrawUiStuff && (mView != null)) {
                mView.post(new Runnable() {
                    @Override
                    public void run() {
                        setTextColor(UtilityStudio.sColor_Win);
                        if (mData instanceof Double) {
                            double value = (Double) mData;
                            if (value < 0.0) {
                                setTextColor(UtilityStudio.sColor_Lost);
                            }
                        }
                        mView.setText(toString());
                    }
                });
            }
        }

        public void setTextColor(int color) {
            if (mDrawUiStuff)
                mView.setTextColor(color);
        }

        public void Refresh() {
            setData(mData);
        }

        public void addOne() {
            if (mData instanceof Integer) {
                int oneBigger = (Integer) mData;
                mData = (T) Integer.valueOf(++oneBigger);
            }
        }
    }

    class CurrencyData extends TextData<Double> {
        public CurrencyData(double currency, TextView view) {
            super(currency, view);
        }

        @Override
        public String toString() {
            return UtilityStudio.getUsCurrency(mData);
        }
    }

    class PercentData extends TextData<Integer> {
        TextData<Integer> mDenominator;

        public PercentData(Integer data, TextView view, TextData<Integer> denominator) {
            super(data, view);
            mDenominator = denominator;
        }

        @Override
        public String toString() {
            String text = super.toString();
            if (mDenominator != null) {
                int denominator = mDenominator.getData();
                if (denominator != 0.0) {
                    text = String.format("%.0f", ((double) mData / (double) denominator) * 100.0);
                    text = super.toString() + "(" + text + "%)";
                }
            }
            return text;
        }
    }

    //=== Data set ===
    class PotCountRemaining extends TextData<Integer> {
        int mCountInitial;

        public PotCountRemaining(Integer countRemaining, TextView view) {
            super(countRemaining, view);
            setCountInitial(0);
        }

        @Override
        public String toString() {
            return (String.valueOf(mData) + "/" + String.valueOf(mCountInitial));
        }

        public void setCountInitial(int countInitial) {
            mCountInitial = countInitial;
            setData(mData);
        }
    }

    public class ShoeData extends ArrayList<TextData> {
        PotCountRemaining mShoeCount;
        CurrencyData mPotChange;

        public ShoeData() {
            add(mShoeCount = new PotCountRemaining
                    (0, (TextView) mTable.findViewById(R.id.tvShoeCount)));
            add(mPotChange = new CurrencyData
                    (0.0, (TextView) mTable.findViewById(R.id.tvPotChange)));
        }
    }

    class DealerData extends ArrayList<TextData> {
        TextData<Integer> mDealerRounds;
        PercentData mBustHands,
                mBlackjackHands;

        public DealerData() {
            add(mDealerRounds = new TextData<>
                    (0, (TextView) mTable.findViewById(R.id.tvDealerRounds)));
            add(mBustHands = new PercentData
                    (0, (TextView) mTable.findViewById(R.id.tvDealerBust), mDealerRounds));
            add(mBlackjackHands = new PercentData
                    (0, (TextView) mTable.findViewById(R.id.tvDealerBJ), mDealerRounds));
        }
    }

    class PlayerData extends ArrayList<TextData> {
        TextData<Integer> mTotalHands;
        PercentData mWinHands,
                mLostHands,
                mPushHands,
                mBustHands,
                mBlackjackHands;

        CurrencyData mBankroll,
                mBankrollChange,
                mLastTotalBet,
                mLastWinningChange;

        public PlayerData() {
            add(mTotalHands = new TextData<>
                    (0, (TextView) mTable.findViewById(R.id.tvTotalHands)));
            add(mWinHands = new PercentData
                    (0, (TextView) mTable.findViewById(R.id.tvWinHands), mTotalHands));
            add(mLostHands = new PercentData
                    (0, (TextView) mTable.findViewById(R.id.tvLostHands), mTotalHands));
            add(mPushHands = new PercentData
                    (0, (TextView) mTable.findViewById(R.id.tvPushHands), mTotalHands));
            add(mBustHands = new PercentData
                    (0, (TextView) mTable.findViewById(R.id.tvBustHands), mTotalHands));
            add(mBlackjackHands = new PercentData
                    (0, (TextView) mTable.findViewById(R.id.tvBJHands), mTotalHands));

            add(mBankroll = new CurrencyData
                    (0.0, (TextView) mTable.findViewById(R.id.tvBankroll)));
            add(mBankrollChange = new CurrencyData
                    (0.0, (TextView) mTable.findViewById(R.id.tvBankrollChange)));
            add(mLastTotalBet = new CurrencyData
                    (0.0, (TextView) mTable.findViewById(R.id.tvLastTotalBet)));
            add(mLastWinningChange = new CurrencyData
                    (0.0, (TextView) mTable.findViewById(R.id.tvLastWinning)));
        }

    }

    class AutoRunData extends ArrayList<TextData> {
        TextData<Integer> mTotalRuns;
        TextData<Integer> mFirstRunOut;
        CurrencyData mAverageBet;
        CurrencyData mMaxWin;
        CurrencyData mMaxLoss;

        public AutoRunData() {
            add(mTotalRuns = new TextData<>
                    (0, null));
            add(mFirstRunOut = new TextData<>
                    (0, null));
            add(mAverageBet = new CurrencyData
                    (0, null));
            add(mMaxWin = new CurrencyData
                    (0, null));
            add(mMaxWin = new CurrencyData
                    (0, null));
            add(mMaxLoss = new CurrencyData
                    (0, null));
        }
    }

    //=== counting ===
    // player data
    public void addPlayerWin() {
        mPlayerData.mWinHands.addOne();
        mPlayerData.mTotalHands.addOne();
    }

    public void addPlayerLost() {
        mPlayerData.mLostHands.addOne();
        mPlayerData.mTotalHands.addOne();
    }

    public void addPlayerPush() {
        mPlayerData.mPushHands.addOne();
        mPlayerData.mTotalHands.addOne();
    }

    public void addPlayerBlackjack() {
        mPlayerData.mBlackjackHands.addOne();
    }

    public void addPlayerBust() {
        mPlayerData.mBustHands.addOne();
    }

    // dealer data
    public void setDealerRound(int index) {
        mDealerData.mDealerRounds.setData(index);
    }

    public void addDealerBlackjack() {
        mDealerData.mBlackjackHands.addOne();
    }

    public void addDealerBust() {
        mDealerData.mBustHands.addOne();
    }

    // bankroll
    public void setBankRoll(double bankroll) {
        mPlayerData.mBankroll.setData(bankroll);
        if (mAutoRunData != null) setMaxWinLoss(bankroll);
    }

    public void setBankRollChange(double change) {
        mPlayerData.mBankrollChange.setData(change);
    }

    public void setLastTotalBet(double bet) {
        mPlayerData.mLastTotalBet.setData(bet);
    }

    public void setLastWinningChange(double winning) {
        mPlayerData.mLastWinningChange.setData(winning);
    }

    // shoe and pot
    public void setShoeRemainCount(int count) {
        mShoeData.mShoeCount.setData(count);
    }

    public void subtractShoeRemainCount() {
        mShoeData.mShoeCount.setData(mShoeData.mShoeCount.mData - 1);
    }

    public void setPotChange(double change) {
        mShoeData.mPotChange.setData(change);
    }

    // AutoRun Data
    public void readyAutoRun(int nRuns, double averageBet, double initialBankroll) {
        if (mAutoRunData == null)
            mAutoRunData = new AutoRunData();

        mAutoRunData.mTotalRuns.setData(nRuns);
        mAutoRunData.mAverageBet.setData(averageBet);
        mAutoRunData.mFirstRunOut.setData(0);
        mAutoRunData.mMaxWin.setData(initialBankroll);
        mAutoRunData.mMaxLoss.setData(initialBankroll);
    }

    public void setFirstRunOut() {
        int firstRunOut = mAutoRunData.mFirstRunOut.getData();
        if (firstRunOut == 0) { // only the first one
            mAutoRunData.mFirstRunOut.setData(mPlayerData.mTotalHands.getData());
        }
    }

    public void setMaxWinLoss(double bankroll) {
        double maxWIn = mAutoRunData.mMaxWin.getData();
        double maxLoss = mAutoRunData.mMaxLoss.getData();

        if (bankroll > maxWIn) mAutoRunData.mMaxWin.setData(bankroll);
        else if (bankroll < maxLoss) mAutoRunData.mMaxLoss.setData(bankroll);
    }
}