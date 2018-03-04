package com.lgsdiamond.blackjackstudio.BlackjackElement;

import android.widget.ImageView;

/**
 * Created by lgsdiamond on 2015-10-02.
 */
public class Card {
    public static final int COUNT_CARD_IN_DECK = 52;
    public static final int COUNT_RANK_IN_DECK = 13;
    public static final int INDEX_SPADE = 0;
    public static final int INDEX_DIAMOND = 1;
    public static final int INDEX_HEART = 2;
    public static final int INDEX_CLUB = 3;
    public static final int COUNT_SUIT_IN_DECK = 4;

    public enum CardSuit {SPADE, DIAMOND, HEART, CLUB}

    public enum CardRank {
        ACE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, TEN, JACK, QUEEN, KING
    }

    private final CardSuit mSuit;
    CardRank mRank;

    int mNumber;     // 1~13 for general use
    int mValue;     // 1~10 for blackjack use
    int mOrder;     // the order in the deck

    private boolean mIsHidden = false;

    private ImageView mImageView;

    public Card(CardSuit suit, CardRank rank) {
        mSuit = suit;
        mRank = rank;

        initializeCardValues();
    }

    private void initializeCardValues() {
        mNumber = mRank.ordinal() + 1;     // ACE =1, TWO=2, ..., KING=13
        mValue = (mNumber <= 10) ? mNumber : 10;

        int index = 0;
        switch (mSuit) {
            case SPADE:
                index = INDEX_SPADE;
                break;
            case DIAMOND:
                index = INDEX_DIAMOND;
                break;
            case HEART:
                index = INDEX_HEART;
                break;
            case CLUB:
                index = INDEX_CLUB;
                break;
        }
        mOrder = index * COUNT_RANK_IN_DECK + mRank.ordinal(); //SPADE-1=0, SPADE-2=2, ..., CLUB-KING=51

        mImageView = null;  // place holder
    }


    @Override
    public String toString() {
        return (mSuit.toString() + "-" + mRank.toString() + "(" + mNumber + ")-> " + mValue);
    }

    public boolean isHidden() {
        return mIsHidden;
    }

    public void setHidden(boolean hidden) {
        mIsHidden = hidden;
    }

    public CardRank getRank() {
        return mRank;
    }

    public int getOrder() {
        return mOrder;
    }

    public int getScore() {
        return mValue;
    }

    public void setImageView(ImageView imageView) {
        mImageView = imageView;
    }

    public ImageView getImageView() {
        return (mImageView);
    }

    // special testing
    public void setRank_Forced(CardRank rank) {
        mRank = rank;
        initializeCardValues();
    }
}