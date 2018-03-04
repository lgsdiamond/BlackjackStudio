package com.lgsdiamond.blackjackstudio.BlackjackElement;

import com.lgsdiamond.blackjackstudio.FragmentTable;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by lgsdiamond on 2015-10-02.
 */
public class Shoe extends ArrayList<Card> {
    private static final float DEFAULT_CUT_CARD_POSITION = 0.85f;  // default position for cut card
    private static final float DEFAULT_CUT_CARD_POSITION_UNDER_DOUBLEDECK = 0.6f;

    private BjService mService;

    private static final long DEFAULT_SHOE_RANDOM_SEED = 100;
    private Random mRandom;   // use for only shuffling purpose
    private boolean mUseRandomSeed;

    private final int mNumDecks;
    private final int mCutPos;
    private int mCountDrawn;
    private int mCountInitial;


    public Shoe(BjService service, int numDecks) {

        //=== Deck ===
        class Deck extends ArrayList<Card> {
            public Deck() {
                super(Card.COUNT_CARD_IN_DECK);

                for (Card.CardSuit suit : Card.CardSuit.values()) {
                    for (Card.CardRank rank : Card.CardRank.values()) {
                        add(new Card(suit, rank));
                    }
                }
            }
        }

        mService = service;
        mRandom = new Random();

        mNumDecks = numDecks;
        mCountInitial = Card.COUNT_CARD_IN_DECK * mNumDecks;

        for (int i = 0; i < mNumDecks; i++)
            addAll(new Deck());

        // for testing
        int nBiased = (int) (mService.pGameRule.mBiasedShoe * 20.0 * numDecks);

        if (nBiased > 0) {
            Random random = new Random();
            while (true) {
                for (int iRank = 2; iRank <= 6; iRank++) {
                    for (int iSuit = 0; iSuit < 4; iSuit++) {
                        Card card = get(iSuit * 52 + iRank - 1);
                        int kTenRank = 10 + random.nextInt(5);
                        if (kTenRank == 14) kTenRank = 1;   // ACE
                        card.setRank_Forced(Card.CardRank.values()[kTenRank - 1]);
                    }
                }
                nBiased--;
                if (nBiased == 0) break;
            }
        } else if (nBiased < 0) {
            nBiased = -nBiased;
            Random random = new Random();
            while (true) {
                for (int iRank = 10; iRank <= 14; iRank++) {
                    for (int iSuit = 0; iSuit < 4; iSuit++) {
                        if (iRank == 14) iRank = 1; // ACE
                        Card card = get(iSuit * 52 + iRank - 1);
                        int kLowRank = 2 + random.nextInt(5);
                        card.setRank_Forced(Card.CardRank.values()[kLowRank - 1]);
                        if (iRank == 1) iRank = 14; // back to 14
                    }
                }
                nBiased--;
                if (nBiased == 0) break;
            }
        }

        mCountDrawn = 0;
        if (mNumDecks > 2) {
            mCutPos = (int) (DEFAULT_CUT_CARD_POSITION * mCountInitial);
        } else {
            mCutPos = (int) (DEFAULT_CUT_CARD_POSITION_UNDER_DOUBLEDECK * mCountInitial);
        }

        reset();
    }

    // getter
    public int getCutPos() {
        return mCutPos;
    }

    public int getCountInitial() {
        return mCountInitial;
    }

    public int getCountRemaining() {
        return mCountInitial - mCountDrawn;
    }


    @Override
    public String toString() {
        return mCountDrawn + "/" + size() + "(" + mCutPos + ") -> "
                + (mCutPos - mCountDrawn) + " remaining";
    }

    public void reset() {
        mUseRandomSeed = !mService.pGameRule.useRandomShoe;
        changeSeedCondition(mService.pGameRule.useRandomShoe);
    }

    public void changeSeedCondition(boolean useRandomSeed) {
        if (mUseRandomSeed == useRandomSeed) return;
        mUseRandomSeed = useRandomSeed;

        mRandom = new Random();
        if (!mUseRandomSeed) {
            mRandom.setSeed(DEFAULT_SHOE_RANDOM_SEED);
        }
        shuffle();
    }

    // actions

    public void shuffle() {
        for (int i = size() - 1; i > 0; i--) {
            int index = mRandom.nextInt(i + 1);
            Card temp = get(index);
            set(index, get(i));
            set(i, temp);
        }
        mCountDrawn = 0;  // no card has been drawn

        if (BjService.sGameData != null)
            BjService.sGameData.setShoeRemainCount(mCountInitial);

        if (mService.isBound()) {
            FragmentTable.sendUiMessage(FragmentTable.UI_MESSAGE.SHOE_SHUFFLED);
        }
    }

    public Card drawOneCard() {
        if (mCountDrawn >= size()) {
            // Emergency Shuffle
            shuffle();
        }
        if (mCountDrawn == mCutPos) {
            mService.notifyCutCardDealt();
        }
        return get(mCountDrawn++);
    }

    public boolean needShuffle() {
        return (mCountDrawn >= mCutPos);
    }

    //=== Testing ===
    public boolean changeSeedCondition() {
        mService.pGameRule.useRandomShoe = !mService.pGameRule.useRandomShoe;
        reset();
        return (mUseRandomSeed);
    }
}