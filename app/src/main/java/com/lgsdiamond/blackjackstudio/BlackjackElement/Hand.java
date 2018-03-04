package com.lgsdiamond.blackjackstudio.BlackjackElement;

import android.widget.TextView;

import com.lgsdiamond.blackjackstudio.BlackjackUtils.UtilityStudio;
import com.lgsdiamond.blackjackstudio.BuildConfig;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by lgsdiamond on 2015-10-02.
 */
public abstract class Hand implements Iterable<Card> {
    final ArrayList<Card> mCards;

    protected BjService mService;

    protected Person mPerson;
    protected int mScore;
    protected boolean mHasDealDone = false;

    protected boolean mIsTwentyOne;
    protected boolean mIsBlackjack;
    protected boolean mIsBust;
    protected boolean mIsSoft;

    protected double mRewardAmount;

    private TextView mHeadText = null;

    Hand(BjService service) {
        mService = service;
        mPerson = null;
        mCards = new ArrayList<>(); // never null

        mRewardAmount = 0.0;
    }

    protected abstract void assignPerson(Person person);

    @Override
    public Iterator<Card> iterator() {
        Iterator<Card> iterator = new Iterator<Card>() {

            private int currentIndex = 0;

            @Override
            public boolean hasNext() {
                return currentIndex < mCards.size();
            }

            @Override
            public Card next() {
                return mCards.get(currentIndex++);
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };

        return iterator;
    }

    @Override
    public String toString() {
        String handString = getCardCount() + " cards: [ ";
        for (Card card : mCards) handString += card.getScore() + " ";
        handString += "] -> " + getScoreText();
        handString += "{" + (mIsSoft ? "So" : "") + (mIsBlackjack ? "BJ" : "")
                + (mIsTwentyOne ? "21" : "") + (mIsBust ? "Bu" : "") + "}";
        return handString;
    }

    // getter
    public BjService getService() {
        return mService;
    }

    public int getCountCard() {
        return mCards.size();
    }

    public int getCardScoreAt(int i) {
        return mCards.get(i).getScore();
    }

    public final boolean isTwentyOne() {
        return mIsTwentyOne;
    }

    public final boolean isBlackjack() {
        return mIsBlackjack;
    }

    public final boolean isBust() {
        return mIsBust;
    }

    public final boolean isSoft() {
        return mIsSoft;
    }

    public Card getCardAt(int index) {
        if (BuildConfig.DEBUG && (index >= mCards.size()))
            throw new AssertionError("too high card index");

        return mCards.get(index);
    }

    public boolean canBeBlackjack() {
        int nCards = getCardCount();
        if ((nCards == 0) || mIsBlackjack) return true;

        int firstScore = mCards.get(0).getScore();
        if ((firstScore != 1) && (firstScore != 10)) return false;

        return (nCards == 1);
    }

    // checking conditions
    void updateValue() {
        mScore = 0;

        boolean hasAce = false;
        for (Card card : mCards) {
            if (card.mRank == Card.CardRank.ACE) hasAce = true;
            mScore += card.getScore();
        }

        mIsSoft = false;
        if (hasAce && mScore < 12) {
            mIsSoft = true;
            mScore += 10;
        }

        mIsBlackjack = false;
        mIsTwentyOne = false;
        if (mScore == 21) {
            mIsTwentyOne = true;
            mIsBlackjack = (mCards.size()) == 2;
        }

        mIsBust = (mScore > 21);
    }

    // manipulation
    public void addCard(Card card) {    // use this instead of add()
        mCards.add(card);
        updateValue();
    }

    public Card removeLastCard() {    // use this instead of remove()
        Card lastCard = null;
        int nCards = mCards.size();

        if (nCards > 0) {
            lastCard = mCards.get(nCards - 1);
            mCards.remove(nCards - 1);
            updateValue();
        }

        return lastCard;
    }

    public Card getLastCard() {
        int nCards = mCards.size();
        return (nCards == 0) ? null : mCards.get(nCards - 1);
    }

    public int getCardCount() {
        return mCards.size();
    }

    public int getScore() {
        return mScore;
    }

    public String getScoreText() {

        return mIsBust ? String.valueOf(mScore) + "-BUST" :
                (mIsBlackjack ? "Blackjack" :
                        (mIsTwentyOne ? "21" :
                                ((mIsSoft ? "S" : "") + String.valueOf(mScore))));
    }

    public double addRewardAmount(double reward) {
        return mRewardAmount += reward;
    }

    public double getRewardAmount() {
        return mRewardAmount;
    }

    public void setHeadTextView(TextView headText) {
        mHeadText = headText;
    }

    public int getHeadColor() {
        int headColor = UtilityStudio.sColor_DealerNormal;
        if (this instanceof PlayerHand) {
            BjService.RoundResult result = ((PlayerHand) this).getRoundResult();
            switch (result) {
                case UNKNOWN:
                    headColor = UtilityStudio.sColor_PlayerNormal;
                    break;
                case WIN:
                    headColor = UtilityStudio.sColor_PlayerWin;
                    break;
                case PUSH:
                    headColor = UtilityStudio.sColor_PlayerPush;
                    break;
                case LOST:
                    headColor = UtilityStudio.sColor_PlayerLost;
                    break;
            }
        }
        return headColor;
    }

    public void highlightHead(boolean highlight) {
        if (mHeadText != null) {
            int headColor = highlight ? UtilityStudio.sColor_HandHighlight : getHeadColor();
            mHeadText.setBackgroundColor(headColor);
        }
    }

    //=== Deal ===
    public boolean hasDealDone() {
        return mHasDealDone;
    }

    public void setDealDone() {
        mHasDealDone = true;
    }

    public void forcedSetDealDone(boolean dealDone) {
        mHasDealDone = dealDone;
    }

    public abstract void evaluateStatus();
}