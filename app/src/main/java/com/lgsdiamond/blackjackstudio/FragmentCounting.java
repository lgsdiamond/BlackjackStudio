package com.lgsdiamond.blackjackstudio;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lgsdiamond.blackjackstudio.BlackjackElement.Card;
import com.lgsdiamond.blackjackstudio.BlackjackUtils.UtilityStudio;

import java.util.ArrayList;

public class FragmentCounting extends FragmentStudioBase {

    public static FragmentCounting newInstance(int countDecks) {
        FragmentCounting fragment = new FragmentCounting();
        fragment.mCountDecks = countDecks;

        FragmentStudioBase.newInstance(fragment, R.layout.fragment_counting);
        fragment.setPreserve(true);
        fragment.mIsStandAlone = false;

        return fragment;
    }

    private ListView lvCardData;
    private CCardAdapter mCardDataAdapter;
    private ArrayList<CCard> mCountingCardArray = new ArrayList<>();

    private ListView lvScheme;
    private CSchemeAdapter mCSchemeAdapter;
    private ArrayList<CScheme> mSchemeArray = new ArrayList<>();

    private int mCountDecks;
    private int mCountInitial;
    private int mCountDrawn;

    private DrawHistory mDrawHistory;

    private Boolean mIsStandAlone;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initialize_Shoe();

        mDrawHistory = new DrawHistory();
    }

    // getter
    public int getCountInitial_Shoe() {
        return mCountInitial;
    }

    public int getCountDrawn_Shoe() {
        return mCountDrawn;
    }

    public int getCountRemaining_Shoe() {
        return (mCountInitial - mCountDrawn);
    }

    public void setStandAlone(Boolean standAlone) {
        mIsStandAlone = standAlone;

        if (mIsStandAlone) {
            layoutCountButtons.setVisibility(View.VISIBLE);
        } else {
            layoutCountButtons.setVisibility(View.GONE);
        }

        btnDataTile.setEnabled(mIsStandAlone);
        btnDataReference.setEnabled(mIsStandAlone);
    }

    public double getReferenceRankRemaining_Shoe(@Nullable CountingRank rank) {
        double count = CCard.COUNT_SUIT_IN_DECK * mCountDecks *
                (getCountRemaining_Shoe() / (double) mCountInitial);

        if ((rank != null) && (rank == CountingRank.TEN_HIGH)) {
            count *= CCard.COUNT_TEN_HIGH_RANK;
        }

        return (count);
    }

    LinearLayout layoutDataTile, layoutDataReference;

    Button btnDataTile, btnDataReference;
    TextView tvTitleRemaining, tvReferenceRemaining_Shoe;
    TextView tvShoeData, tvLowData, tvMediumData, tvHighData, tvAceData;

    RelativeLayout layoutCountButtons;
    Button btnUndo, btnRedo;
    Button btnCount_2, btnCount_3, btnCount_4, btnCount_5, btnCount_6;
    Button btnCount_7, btnCount_8, btnCount_9;
    Button btnCount_Ten_High, btnCount_Ace;

    @Override
    protected void initializeViews() {

        btnUndo = (Button) findViewById(R.id.btnCountingUndo);
        btnUndo.setOnClickListener(this);
        btnRedo = (Button) findViewById(R.id.btnCountingRedo);
        btnRedo.setOnClickListener(this);

        layoutDataTile = (LinearLayout) findViewById(R.id.loDataTile);
        btnDataTile = (Button) layoutDataTile.findViewById(R.id.btnDrawCard);
        btnDataTile.setBackgroundColor(UtilityStudio.sColor_TitleButton);
        btnDataTile.setText("I");
        btnDataTile.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetCounting();
            }
        });
        tvTitleRemaining = (TextView) layoutDataTile.findViewById(R.id.tvCountRemaining);
        tvTitleRemaining.setText("Remain");

        layoutDataReference = (LinearLayout) findViewById(R.id.loDataReference);
        btnDataReference = (Button) layoutDataReference.findViewById(R.id.btnDrawCard);
        btnDataReference.setBackgroundColor(UtilityStudio.sColor_TitleButton);
        btnDataReference.setText("R");
        tvReferenceRemaining_Shoe = (TextView) layoutDataReference.findViewById
                (R.id.tvCountRemaining);

        lvCardData = (ListView) findViewById(R.id.lvCardData);

        // other than ACE
        int size = CountingRank.values().length;

        for (int i = 0; i < size; i++) {
            CountingRank rank = getRankByIndex(i); // first goes TWO
            CCard countingCard = new CCard(mCountDecks, rank);
            mCountingCardArray.add(countingCard);
        }

        // it is not stand alone, it is synced with table
        mCardDataAdapter = new CCardAdapter(getActivity().getApplicationContext(),
                R.layout.row_counting, mCountingCardArray, this, false);
        lvCardData.setAdapter(mCardDataAdapter);

        // Scheme
        lvScheme = (ListView) findViewById(R.id.lvScheme);
        for (KnownScheme scheme : KnownScheme.values()) {
            CScheme schemeData = new CScheme(scheme);
            mSchemeArray.add(schemeData);
        }

        mCSchemeAdapter = new CSchemeAdapter(getActivity().getApplicationContext(),
                R.layout.row_counting_scheme, mSchemeArray, this);
        lvScheme.setAdapter(mCSchemeAdapter);

        // common data
        tvShoeData = (TextView) findViewById(R.id.tvShoeData);
        tvLowData = (TextView) findViewById(R.id.tvLowData);
        tvMediumData = (TextView) findViewById(R.id.tvMediumData);
        tvHighData = (TextView) findViewById(R.id.tvHighData);
        tvAceData = (TextView) findViewById(R.id.tvAceData);

        // draw card buttons
        layoutCountButtons = (RelativeLayout) findViewById(R.id.loCountButtons);
        btnCount_2 = (Button) findViewById(R.id.btnCount_2);
        btnCount_3 = (Button) findViewById(R.id.btnCount_3);
        btnCount_4 = (Button) findViewById(R.id.btnCount_4);
        btnCount_5 = (Button) findViewById(R.id.btnCount_5);
        btnCount_6 = (Button) findViewById(R.id.btnCount_6);
        btnCount_7 = (Button) findViewById(R.id.btnCount_7);
        btnCount_8 = (Button) findViewById(R.id.btnCount_8);
        btnCount_9 = (Button) findViewById(R.id.btnCount_9);
        btnCount_Ten_High = (Button) findViewById(R.id.btnCount_Ten_High);
        btnCount_Ace = (Button) findViewById(R.id.btnCount_Ace);

        btnCount_2.setBackgroundColor(UtilityStudio.sColor_LowCard);
        btnCount_3.setBackgroundColor(UtilityStudio.sColor_LowCard);
        btnCount_4.setBackgroundColor(UtilityStudio.sColor_LowCard);
        btnCount_5.setBackgroundColor(UtilityStudio.sColor_LowCard);
        btnCount_6.setBackgroundColor(UtilityStudio.sColor_LowCard);
        btnCount_7.setBackgroundColor(UtilityStudio.sColor_MediumCard);
        btnCount_8.setBackgroundColor(UtilityStudio.sColor_MediumCard);
        btnCount_9.setBackgroundColor(UtilityStudio.sColor_MediumCard);
        btnCount_Ten_High.setBackgroundColor(UtilityStudio.sColor_HighCard);
        btnCount_Ace.setBackgroundColor(UtilityStudio.sColor_AceCard);

        btnCount_2.setOnClickListener(this);
        btnCount_3.setOnClickListener(this);
        btnCount_4.setOnClickListener(this);
        btnCount_5.setOnClickListener(this);
        btnCount_6.setOnClickListener(this);
        btnCount_7.setOnClickListener(this);
        btnCount_8.setOnClickListener(this);
        btnCount_9.setOnClickListener(this);
        btnCount_Ten_High.setOnClickListener(this);
        btnCount_Ace.setOnClickListener(this);

        // populate data
        setDataReference();
    }

    @Override
    protected void setPrivateTag() {
        privateTag = "COUNTING";
    }

    private void initialize_Shoe() {
        mCountInitial = mCountDecks * CCard.COUNT_CARD_IN_DECK;
        mCountDrawn = 0;
    }

    // draw card
    public void drawRankFromShoe(CountingRank rank, boolean redoing) {
        if (mCountDrawn >= mCountInitial) return;
        mCountDrawn++;
        if (mCardDataAdapter.drawRankAndUpdate(rank, getCountRemaining_Shoe())) {
            mCSchemeAdapter.drawRankAndUpdate(rank, getCountRemaining_Shoe());
            if (!redoing) {
                mDrawHistory.addRank(rank);
            }
            updateAllDataDisplay();
        } else {
            mCountDrawn--;
        }
    }

    public void putBackRankToShoe(CountingRank rank) {
        if (mCountDrawn <= 0) return;
        mCountDrawn--;
        if (mCardDataAdapter.putBackRankAndUpdate(rank, getCountRemaining_Shoe())) {
            mCSchemeAdapter.putBackRankAndUpdate(rank, getCountRemaining_Shoe());
            updateAllDataDisplay();
        } else {
            mCountDrawn++;
        }
    }

    // update data

    public void updateAllDataDisplay() {
        getActivity().runOnUiThread(new Thread(new Runnable() {
            public void run() {
                setDataReference();

                mCardDataAdapter.notifyDataSetChanged();
                mCSchemeAdapter.notifyDataSetChanged();

                updateButtons();
            }
        }));
    }

    private void updateButtons() {
        getActivity().runOnUiThread(new Thread(new Runnable() {
            public void run() {
                btnUndo.setEnabled(mDrawHistory.isUndoable());
                btnRedo.setEnabled(mDrawHistory.isRedoable());
            }
        }));

    }

    private void setDataTitle() {
    }

    private void setDataReference() {
        double rankRemaining = getReferenceRankRemaining_Shoe(null);
        String resultStr = String.format("%.2f", rankRemaining);
        tvReferenceRemaining_Shoe.setText(resultStr);

        setShoeData();
        setLowData();
        setMediumData();
        setHighData();
        setAceData();
        tvShoeData = (TextView) findViewById(R.id.tvShoeData);
        tvLowData = (TextView) findViewById(R.id.tvLowData);
        tvMediumData = (TextView) findViewById(R.id.tvMediumData);
        tvHighData = (TextView) findViewById(R.id.tvHighData);
        tvAceData = (TextView) findViewById(R.id.tvAceData);
    }

    private void setShoeData() {
        int countRemaining = getCountRemaining_Shoe();
        double ratio = getShoeRemainingRatio();
        String resultStr = Integer.toString(countRemaining) + "/" + Integer.toString(mCountInitial);
        resultStr = UtilityStudio.doubleToDotDecimal(ratio, 2) + "(" + resultStr + ")";
        tvShoeData.setText(resultStr);
    }

    private double getShoeRemainingRatio() {
        return ((double) getCountRemaining_Shoe()) / mCountInitial;
    }

    private void setLowData() {
        int countRemaining = 0;
        int indexTwo = getIndexByRank(CountingRank.TWO);
        int indexSeven = getIndexByRank(CountingRank.SEVEN);
        for (int index = indexTwo; index < indexSeven; index++) {
            countRemaining += mCountingCardArray.get(index).getCountRemaining();
        }

        double referenceRemaining = (indexSeven - indexTwo) * CCard.COUNT_SUIT_IN_DECK *
                mCountDecks * getShoeRemainingRatio();

        double ratio = countRemaining / referenceRemaining;

        double overCount = ratio - 1.0;

        UtilityStudio.setTextViewColorByValue(tvLowData, overCount,
                UtilityStudio.NORMALIZE_LOHI, PlayerFavor.ANTI_FAVOR);

        String resultStr = Integer.toString(countRemaining) + "/"
                + UtilityStudio.doubleToDotDecimal(referenceRemaining, 1);

        resultStr = UtilityStudio.doubleToDotDecimal(overCount, 2) + "(" + resultStr + ")";

        tvLowData.setText(resultStr);
    }

    private void setMediumData() {
        int countRemaining = 0;
        int indexSeven = getIndexByRank(CountingRank.SEVEN);
        int indexTen_High = CCard.INDEX_TEN_HIGH;
        for (int index = indexSeven; index < indexTen_High; index++) {
            countRemaining += mCountingCardArray.get(index).getCountRemaining();
        }

        double referenceRemaining = (indexTen_High - indexSeven) * CCard.COUNT_SUIT_IN_DECK *
                mCountDecks * getShoeRemainingRatio();

        double ratio = countRemaining / referenceRemaining;

        double overCount = ratio - 1.0;

        UtilityStudio.setTextViewColorByValue(tvMediumData, overCount,
                UtilityStudio.NORMALIZE_LOHI, PlayerFavor.NONE);

        String resultStr = Integer.toString(countRemaining) + "/"
                + UtilityStudio.doubleToDotDecimal(referenceRemaining, 1);

        resultStr = UtilityStudio.doubleToDotDecimal(overCount, 2) + "(" + resultStr + ")";

        tvMediumData.setText(resultStr);
    }

    private void setHighData() {
        int countRemaining = mCountingCardArray.get(CCard.INDEX_TEN_HIGH).getCountRemaining();
        countRemaining += mCountingCardArray.get(CCard.INDEX_ACE).getCountRemaining();

        double referenceRemaining = (CCard.COUNT_TEN_HIGH_RANK + 1) * CCard.COUNT_SUIT_IN_DECK *
                mCountDecks * getShoeRemainingRatio();

        double ratio = countRemaining / referenceRemaining;

        double overCount = ratio - 1.0;

        UtilityStudio.setTextViewColorByValue(tvHighData, overCount,
                UtilityStudio.NORMALIZE_LOHI, PlayerFavor.FAVOR);

        String resultStr = Integer.toString(countRemaining) + "/"
                + UtilityStudio.doubleToDotDecimal(referenceRemaining, 1);

        resultStr = UtilityStudio.doubleToDotDecimal(overCount, 2) + "(" + resultStr + ")";

        tvHighData.setText(resultStr);
    }

    private void setAceData() {
        int indexAce = getIndexByRank(CountingRank.ACE);
        int countRemaining = mCountingCardArray.get(indexAce).getCountRemaining();

        double referenceRemaining = CCard.COUNT_SUIT_IN_DECK *
                mCountDecks * getShoeRemainingRatio();

        double ratio = countRemaining / referenceRemaining;

        double overCount = ratio - 1.0;

        UtilityStudio.setTextViewColorByValue(tvAceData, overCount,
                UtilityStudio.NORMALIZE_RANK, PlayerFavor.FAVOR);

        String resultStr = Integer.toString(countRemaining) + "/"
                + UtilityStudio.doubleToDotDecimal(referenceRemaining, 1);

        resultStr = UtilityStudio.doubleToDotDecimal(overCount, 2) + "(" + resultStr + ")";

        tvAceData.setText(resultStr);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnCountingUndo:
                undoLastDraw();
                break;
            case R.id.btnCountingRedo:
                redoLastDraw();
                break;

            // Counting
            case R.id.btnCount_2:
                drawRankFromShoe(CountingRank.TWO, false);
                break;
            case R.id.btnCount_3:
                drawRankFromShoe(CountingRank.THREE, false);
                break;
            case R.id.btnCount_4:
                drawRankFromShoe(CountingRank.FOUR, false);
                break;
            case R.id.btnCount_5:
                drawRankFromShoe(CountingRank.FIVE, false);
                break;
            case R.id.btnCount_6:
                drawRankFromShoe(CountingRank.SIX, false);
                break;
            case R.id.btnCount_7:
                drawRankFromShoe(CountingRank.SEVEN, false);
                break;
            case R.id.btnCount_8:
                drawRankFromShoe(CountingRank.EIGHT, false);
                break;
            case R.id.btnCount_9:
                drawRankFromShoe(CountingRank.NINE, false);
                break;
            case R.id.btnCount_Ten_High:
                drawRankFromShoe(CountingRank.TEN_HIGH, false);
                break;
            case R.id.btnCount_Ace:
                drawRankFromShoe(CountingRank.ACE, false);
                break;
        }
    }

    private void undoLastDraw() {
        CountingRank rank = mDrawHistory.undoLastRank();
        if (rank != null) {
            putBackRankToShoe(rank);
            updateAllDataDisplay();
        }
    }

    private void redoLastDraw() {
        CountingRank rank = mDrawHistory.redoLastRank();
        if (rank != null) {
            drawRankFromShoe(rank, true);
            updateAllDataDisplay();
        }
    }

    public void resetCounting() {
        mCountDrawn = 0;
        mCardDataAdapter.reset();
        mCSchemeAdapter.reset();

        mDrawHistory.reset();

        updateAllDataDisplay();
    }

    public CountingRank rankConvert(Card.CardRank rankIn) {
        CountingRank rankOut;

        switch (rankIn) {
            case ACE:
                rankOut = CountingRank.ACE;
                break;
            case TWO:
                rankOut = CountingRank.TWO;
                break;
            case THREE:
                rankOut = CountingRank.THREE;
                break;
            case FOUR:
                rankOut = CountingRank.FOUR;
                break;
            case FIVE:
                rankOut = CountingRank.FIVE;
                break;
            case SIX:
                rankOut = CountingRank.SIX;
                break;
            case SEVEN:
                rankOut = CountingRank.SEVEN;
                break;
            case EIGHT:
                rankOut = CountingRank.EIGHT;
                break;
            case NINE:
                rankOut = CountingRank.NINE;
                break;
            default:
            case TEN:
            case JACK:
            case QUEEN:
            case KING:
                rankOut = CountingRank.TEN_HIGH;
                break;
        }

        return rankOut;
    }

    private class DrawHistory {
        ArrayList<CountingRank> mHistory = new ArrayList<>();
        int mCurrentIndex;

        public DrawHistory() {
            mCurrentIndex = 0;
        }

        public void reset() {
            mHistory.clear();
            mCurrentIndex = 0;
        }

        void addRank(CountingRank rank) {
            int size = mHistory.size();
            if (mCurrentIndex < size) {
                for (int index = size - 1; index >= mCurrentIndex; index--) {
                    mHistory.remove(index);
                }
            }
            mHistory.add(rank);
            mCurrentIndex++;
        }

        CountingRank undoLastRank() {
            if (!isUndoable()) return null;

            CountingRank rank = mHistory.get(mCurrentIndex - 1);
            mCurrentIndex--;
            return rank;
        }

        CountingRank redoLastRank() {
            if (!isRedoable()) return null;

            CountingRank rank = mHistory.get(mCurrentIndex);
            mCurrentIndex++;
            return rank;
        }

        public boolean isUndoable() {
            return (mCurrentIndex > 0);
        }

        public boolean isRedoable() {
            return (mCurrentIndex < mHistory.size());
        }
    }

    // inner class
    public class CSchemeAdapter extends ArrayAdapter<CScheme> {
        ArrayList<CScheme> mSchemeArray;
        FragmentCounting mCounting;

        public CSchemeAdapter(Context context, int resource, ArrayList<CScheme> schemeArray,
                              FragmentCounting activity) {
            super(context, resource, schemeArray);

            mCounting = activity;
            mSchemeArray = schemeArray;
        }

        public void reset() {
            for (CScheme scheme : mSchemeArray) {
                scheme.reset();
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View itemView;
            SchemeDataHandler dataHandler;

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) this.getContext()
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                itemView = inflater.inflate(R.layout.row_counting_scheme, parent, false);
                dataHandler = new SchemeDataHandler(itemView, position);
                itemView.setTag(dataHandler);

            } else {
                itemView = convertView;
                dataHandler = (SchemeDataHandler) itemView.getTag();
            }

            dataHandler.setData(position);

            return itemView;
        }

        public void drawRankAndUpdate(CountingRank rank, int countRemaining) {
            for (CScheme schemeData : mSchemeArray) {
                schemeData.computeCountsByDrawnCard(rank, countRemaining);
            }
        }

        public void putBackRankAndUpdate(CountingRank rank, int countRemaining) {
            for (CScheme schemeData : mSchemeArray) {
                schemeData.revertCountsByCanceledCard(rank, countRemaining);
            }
        }

        class SchemeDataHandler {
            private final TextView tvSchemeLabel, tvRCount, tvTCount;


            public SchemeDataHandler(View view, final int position) {
                tvSchemeLabel = (TextView) view.findViewById(R.id.tvSchemeLabel);
                tvRCount = (TextView) view.findViewById(R.id.tvRCount);
                tvTCount = (TextView) view.findViewById(R.id.tvTCount);
            }

            public void setData(int position) {
                CScheme schemeData = mSchemeArray.get(position);

                setSchemeLabel(schemeData);
                setRunningCount(schemeData);
                setTrueCount(schemeData);
            }

            private void setSchemeLabel(CScheme schemeData) {
                tvSchemeLabel.setText(schemeData.mTitle);
            }

            private void setRunningCount(CScheme schemeData) {
                double count = schemeData.getRunningCount();

                UtilityStudio.setTextViewColorByValue(tvRCount, count,
                        UtilityStudio.NORMALIZE_RUN_COUNT, PlayerFavor.FAVOR);
                tvRCount.setText(UtilityStudio.doubleToDotDecimal(count, 1));
            }

            private void setTrueCount(CScheme schemeData) {
                double count = schemeData.getTrueCount();

                UtilityStudio.setTextViewColorByValue(tvTCount, count,
                        UtilityStudio.NORMALIZE_TRUE_COUNT, PlayerFavor.FAVOR);
                tvTCount.setText(UtilityStudio.doubleToDotDecimal(count, 2));
            }
        }
    }

    public enum PlayerFavor {FAVOR, NONE, ANTI_FAVOR}   // FAVOR if remains in shoe

    public enum CountingRank {TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, TEN_HIGH, ACE}

    public class CCard {
        public static final int COUNT_TEN_HIGH_RANK = 4;    // 10, J, K, Q
        public static final int COUNT_CARD_IN_DECK = 52;    // A~K(13) times 4 = 52

        public static final int INDEX_TEN_HIGH = 8;
        public static final int INDEX_ACE = 9;

        public static final int COUNT_SUIT_IN_DECK = 4;

        private PlayerFavor mFavor;

        private final int mCountDecks;
        private final CountingRank mRank;

        private final int mCountInitial;
        private int mCountDrawn = 0;

        public CCard(int countDecks, CountingRank rank) {
            mCountDecks = countDecks;
            mRank = rank;
            if (rank == CountingRank.TEN_HIGH) {
                mCountInitial = COUNT_TEN_HIGH_RANK * mCountDecks * CCard.COUNT_SUIT_IN_DECK;
            } else {
                mCountInitial = mCountDecks * CCard.COUNT_SUIT_IN_DECK;
            }

            mFavor = checkFavorRank(rank);
        }

        // getter
        public CountingRank getRank() {
            return mRank;
        }

        public PlayerFavor getFavor() {
            return mFavor;
        }

        private PlayerFavor checkFavorRank(CountingRank rank) {
            PlayerFavor favor;
            switch (rank) {
                case ACE:
                case TEN_HIGH:
                    favor = PlayerFavor.FAVOR;
                    break;
                case TWO:
                case THREE:
                case FOUR:
                case FIVE:
                case SIX:
                    favor = PlayerFavor.ANTI_FAVOR;
                    break;
                default:
                case SEVEN:
                case EIGHT:
                case NINE:
                    favor = PlayerFavor.NONE;
                    break;
            }
            return favor;
        }

        public void reset() {
            mCountDrawn = 0;
        }

        // getter
        public int getCountInitial() {
            return mCountInitial;
        }

        public int getCountDrawn() {
            return mCountDrawn;
        }

        public int getCountRemaining() {
            return (mCountInitial - mCountDrawn);
        }

        public Boolean drawOne() {
            if (mCountDrawn >= mCountInitial) return false;

            mCountDrawn++;

            return true;
        }

        public boolean putBackOne() {
            if (mCountDrawn <= 0) return false;

            mCountDrawn--;

            return true;
        }

        double mCountOver, mCountOverTrue;

        public void updateData(int countRemainingInShoe) {
            mCountOver = ((double) getCountRemaining() -
                    ((double) countRemainingInShoe / Card.COUNT_RANK_IN_DECK));

            mCountOverTrue = (mCountOver / (countRemainingInShoe / Card.COUNT_CARD_IN_DECK)) /
                    CCard.COUNT_SUIT_IN_DECK;
        }
    }

    public static int getIndexByRank(CountingRank rank) {
        return rank.ordinal();
    }

    public static CountingRank getRankByIndex(int index) {
        return (CountingRank.values()[index]);
    }

    public class CCardAdapter extends ArrayAdapter<CCard> {
        ArrayList<CCard> mCountingCardArray;
        FragmentCounting mCounting;
        Boolean mIsStandAlone;

        public CCardAdapter(Context context, int resource, ArrayList<CCard> countingCardArray,
                            FragmentCounting counting, Boolean isStandAlone) {
            super(context, resource, countingCardArray);

            mCounting = counting;
            mCountingCardArray = countingCardArray;

            mIsStandAlone = isStandAlone;
        }

        public void reset() {
            for (CCard countingCard : mCountingCardArray) {
                countingCard.reset();
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View itemView;
            CardDataHandler dataHandler;

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) this.getContext()
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                itemView = inflater.inflate(R.layout.row_counting, parent, false);
                dataHandler = new CardDataHandler(itemView, position, mIsStandAlone);
                itemView.setTag(dataHandler);

            } else {
                itemView = convertView;
                dataHandler = (CardDataHandler) itemView.getTag();
            }

            dataHandler.setData(position);

            return itemView;
        }

        public Boolean drawRankAndUpdate(CountingRank rank, int countRemainingInShoe) {

            int index = getIndexByRank(rank);

            if (mCountingCardArray.get(index).drawOne()) {
                for (CCard countingCard : mCountingCardArray) {
                    countingCard.updateData(countRemainingInShoe);
                }
                return true;
            } else {
                return false;
            }
        }

        public boolean putBackRankAndUpdate(CountingRank rank, int countRemainingInShoe) {
            int index = getIndexByRank(rank);

            if (mCountingCardArray.get(index).putBackOne()) {
                for (CCard countingCard : mCountingCardArray) {
                    countingCard.updateData(countRemainingInShoe);
                }
                return true;
            } else {
                return false;
            }
        }

        class CardDataHandler {
            private final Button btnDrawCard;
            private final TextView tvCountRemaining;

            public CardDataHandler(View view, final int position, Boolean isStandAlone) {
                btnDrawCard = (Button) view.findViewById(R.id.btnDrawCard);

                btnDrawCard.setEnabled(isStandAlone);
                if (isStandAlone) {
                    btnDrawCard.setOnClickListener(new Button.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            CountingRank rank = getRankByIndex(position);
                            mCounting.drawRankFromShoe(rank, false);    // not re-doing
                        }
                    });

                    btnDrawCard.setOnLongClickListener(new Button.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            CountingRank rank = getRankByIndex(position);
                            mCounting.putBackRankToShoe(rank);

                            return true;
                        }
                    });
                }

                tvCountRemaining = (TextView) view.findViewById(R.id.tvCountRemaining);
            }

            private void setCardButton(int index) {
                btnDrawCard.setText(getTextByIndex(index));
                if (index < 5) {
                    btnDrawCard.setBackgroundColor(UtilityStudio.sColor_LowCard);
                } else if (index < 8) {
                    btnDrawCard.setBackgroundColor(UtilityStudio.sColor_MediumCard);
                } else if (index == 8) {
                    btnDrawCard.setBackgroundColor(UtilityStudio.sColor_HighCard);
                } else {
                    btnDrawCard.setBackgroundColor(UtilityStudio.sColor_AceCard);
                }
            }

            public void setData(int position) {
                CCard countingCard = mCountingCardArray.get(position);

                setCardButton(position);

                setCountRemaining(countingCard);
            }

            private void setCountRemaining(CCard countingCard) {
                double refRemaining = mCounting.getReferenceRankRemaining_Shoe(countingCard.getRank());
                int countRemaining = countingCard.getCountRemaining();
                double overCount = (double) countRemaining / refRemaining - 1.0;


                UtilityStudio.setTextViewColorByValue(tvCountRemaining, overCount,
                        UtilityStudio.NORMALIZE_RANK, countingCard.getFavor());

                String resultStr = UtilityStudio.doubleToDotDecimal(overCount, 2);
                resultStr = Integer.toString(countRemaining) + "(" + resultStr + ")";

                tvCountRemaining.setText(resultStr);
            }
        }

        private String getTextByIndex(int position) {
            String rankText;
            switch (position) {
                case 0:
                    rankText = "[2]";
                    break;
                case 1:
                    rankText = "[3]";
                    break;
                case 2:
                    rankText = "[4]";
                    break;
                case 3:
                    rankText = "[5]";
                    break;
                case 4:
                    rankText = "[6]";
                    break;
                case 5:
                    rankText = "[7]";
                    break;
                case 6:
                    rankText = "[8]";
                    break;
                case 7:
                    rankText = "[9]";
                    break;
                default:
                case 8:
                    rankText = "[10+]";
                    break;
                case 9:
                    rankText = "[A]";
                    break;
            }
            return rankText;
        }
    }

    public enum KnownScheme {HI_LO, HI_OPT_I, HI_OPT_II, KO, OMEGA_II, RED_7, HALVES, ZEN_COUNT}

    public class CScheme {
        KnownScheme mScheme;
        String mTitle;
        double[] countValues;
        double mRunningCount = 0.0;
        double mTrueCount = 0.0;

        public CScheme(KnownScheme scheme) {
            mScheme = scheme;
            countValues = new double[Card.COUNT_RANK_IN_DECK];

            initialize();
        }

        public void reset() {
            mRunningCount = 0.0;
            mTrueCount = 0.0;
        }

        public double getRunningCount() {
            return mRunningCount;
        }

        public double getTrueCount() {
            return mTrueCount;
        }

        public void computeCountsByDrawnCard(CountingRank rank, int countCardsInShoe) {
            mRunningCount += countValues[getIndexByRank(rank)];
            mTrueCount = mRunningCount / ((double) countCardsInShoe / Card.COUNT_CARD_IN_DECK);
        }

        public void revertCountsByCanceledCard(CountingRank rank, int countCardsInShoe) {
            mRunningCount -= countValues[getIndexByRank(rank)];
            mTrueCount = mRunningCount / ((double) countCardsInShoe / Card.COUNT_CARD_IN_DECK);
        }

        private void initialize() {
            switch (mScheme) {
                case HI_LO:
                    mTitle = "Hi-Lo";
                    countValues[getIndexByRank(CountingRank.TWO)] = 1.0;
                    countValues[getIndexByRank(CountingRank.THREE)] = 1.0;
                    countValues[getIndexByRank(CountingRank.FOUR)] = 1.0;
                    countValues[getIndexByRank(CountingRank.FIVE)] = 1.0;
                    countValues[getIndexByRank(CountingRank.SIX)] = 1.0;
                    countValues[getIndexByRank(CountingRank.SEVEN)] = 0.0;
                    countValues[getIndexByRank(CountingRank.EIGHT)] = 0.0;
                    countValues[getIndexByRank(CountingRank.NINE)] = 0.0;
                    countValues[getIndexByRank(CountingRank.TEN_HIGH)] = -1.0;
                    countValues[getIndexByRank(CountingRank.ACE)] = -1.0;
                    break;
                case HI_OPT_I:
                    mTitle = "Hi-Opt I";
                    countValues[getIndexByRank(CountingRank.TWO)] = 0.0;
                    countValues[getIndexByRank(CountingRank.THREE)] = 1.0;
                    countValues[getIndexByRank(CountingRank.FOUR)] = 1.0;
                    countValues[getIndexByRank(CountingRank.FIVE)] = 1.0;
                    countValues[getIndexByRank(CountingRank.SIX)] = 1.0;
                    countValues[getIndexByRank(CountingRank.SEVEN)] = 0.0;
                    countValues[getIndexByRank(CountingRank.EIGHT)] = 0.0;
                    countValues[getIndexByRank(CountingRank.NINE)] = 0.0;
                    countValues[getIndexByRank(CountingRank.TEN_HIGH)] = -1.0;
                    countValues[getIndexByRank(CountingRank.ACE)] = 0.0;
                    break;
                case HI_OPT_II:
                    mTitle = "Hi-Opt II";
                    countValues[getIndexByRank(CountingRank.TWO)] = 1.0;
                    countValues[getIndexByRank(CountingRank.THREE)] = 1.0;
                    countValues[getIndexByRank(CountingRank.FOUR)] = 2.0;
                    countValues[getIndexByRank(CountingRank.FIVE)] = 2.0;
                    countValues[getIndexByRank(CountingRank.SIX)] = 1.0;
                    countValues[getIndexByRank(CountingRank.SEVEN)] = 1.0;
                    countValues[getIndexByRank(CountingRank.EIGHT)] = 0.0;
                    countValues[getIndexByRank(CountingRank.NINE)] = 0.0;
                    countValues[getIndexByRank(CountingRank.TEN_HIGH)] = -2.0;
                    countValues[getIndexByRank(CountingRank.ACE)] = 0.0;
                    break;
                case KO:
                    mTitle = "KO";
                    countValues[getIndexByRank(CountingRank.TWO)] = 1.0;
                    countValues[getIndexByRank(CountingRank.THREE)] = 1.0;
                    countValues[getIndexByRank(CountingRank.FOUR)] = 1.0;
                    countValues[getIndexByRank(CountingRank.FIVE)] = 1.0;
                    countValues[getIndexByRank(CountingRank.SIX)] = 1.0;
                    countValues[getIndexByRank(CountingRank.SEVEN)] = 1.0;
                    countValues[getIndexByRank(CountingRank.EIGHT)] = 0.0;
                    countValues[getIndexByRank(CountingRank.NINE)] = 0.0;
                    countValues[getIndexByRank(CountingRank.TEN_HIGH)] = -1.0;
                    countValues[getIndexByRank(CountingRank.ACE)] = -1.0;
                    break;
                case OMEGA_II:
                    mTitle = "Omega II";
                    countValues[getIndexByRank(CountingRank.TWO)] = 1.0;
                    countValues[getIndexByRank(CountingRank.THREE)] = 1.0;
                    countValues[getIndexByRank(CountingRank.FOUR)] = 2.0;
                    countValues[getIndexByRank(CountingRank.FIVE)] = 2.0;
                    countValues[getIndexByRank(CountingRank.SIX)] = 2.0;
                    countValues[getIndexByRank(CountingRank.SEVEN)] = 1.0;
                    countValues[getIndexByRank(CountingRank.EIGHT)] = 0.0;
                    countValues[getIndexByRank(CountingRank.NINE)] = -1.0;
                    countValues[getIndexByRank(CountingRank.TEN_HIGH)] = -2.0;
                    countValues[getIndexByRank(CountingRank.ACE)] = 0.0;
                    break;
                case RED_7:
                    mTitle = "Red 7";
                    countValues[getIndexByRank(CountingRank.TWO)] = 1.0;
                    countValues[getIndexByRank(CountingRank.THREE)] = 1.0;
                    countValues[getIndexByRank(CountingRank.FOUR)] = 1.0;
                    countValues[getIndexByRank(CountingRank.FIVE)] = 1.0;
                    countValues[getIndexByRank(CountingRank.SIX)] = 1.0;
                    countValues[getIndexByRank(CountingRank.SEVEN)] = 1.0;
                    countValues[getIndexByRank(CountingRank.EIGHT)] = 0.0;
                    countValues[getIndexByRank(CountingRank.NINE)] = 0.0;
                    countValues[getIndexByRank(CountingRank.TEN_HIGH)] = -1.0;
                    countValues[getIndexByRank(CountingRank.ACE)] = -1.0;
                    break;
                case HALVES:
                    mTitle = "Halves";
                    countValues[getIndexByRank(CountingRank.TWO)] = 0.5;
                    countValues[getIndexByRank(CountingRank.THREE)] = 1.0;
                    countValues[getIndexByRank(CountingRank.FOUR)] = 1.0;
                    countValues[getIndexByRank(CountingRank.FIVE)] = 1.5;
                    countValues[getIndexByRank(CountingRank.SIX)] = 1.0;
                    countValues[getIndexByRank(CountingRank.SEVEN)] = 0.5;
                    countValues[getIndexByRank(CountingRank.EIGHT)] = 0.0;
                    countValues[getIndexByRank(CountingRank.NINE)] = -0.5;
                    countValues[getIndexByRank(CountingRank.TEN_HIGH)] = -1.0;
                    countValues[getIndexByRank(CountingRank.ACE)] = -1.0;
                    break;
                case ZEN_COUNT:
                    mTitle = "Zen Count";
                    countValues[getIndexByRank(CountingRank.TWO)] = 1.0;
                    countValues[getIndexByRank(CountingRank.THREE)] = 1.0;
                    countValues[getIndexByRank(CountingRank.FOUR)] = 2.0;
                    countValues[getIndexByRank(CountingRank.FIVE)] = 2.0;
                    countValues[getIndexByRank(CountingRank.SIX)] = 2.0;
                    countValues[getIndexByRank(CountingRank.SEVEN)] = 1.0;
                    countValues[getIndexByRank(CountingRank.EIGHT)] = 0.0;
                    countValues[getIndexByRank(CountingRank.NINE)] = 0.0;
                    countValues[getIndexByRank(CountingRank.TEN_HIGH)] = -2.0;
                    countValues[getIndexByRank(CountingRank.ACE)] = -1.0;
                    break;
            }
        }
    }
}
