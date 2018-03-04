package com.lgsdiamond.blackjackstudio.BlackjackElement;

/**
 * Created by lgsdiamond on 2015-10-02.
 */
public class DealerHand extends Hand {
    public boolean initialCheckDone() {
        return mInitialCheckDone;
    }

    public void setInitialCheckDone() {
        mInitialCheckDone = true;
    }

    public int getUpScore() {
        return getCardScoreAt(0);
    }

    private boolean mInsuranceOffered = false;

    public boolean insuranceOffered() {
        return mInsuranceOffered;
    }

    public void setInsuranceOffered() {
        mInsuranceOffered = true;
    }

    public enum PeekHoleResult {NO_PEEK, BLACKJACK, NO_BLACKJACK}

    private PeekHoleResult mPeekHoleResult = PeekHoleResult.NO_PEEK;

    private boolean mHiddenSecondCard = false;
    private boolean mInitialCheckDone = false;

    public DealerHand(BjService service, Dealer dealer) {
        super(service);
        assignPerson(dealer);
    }

    @Override
    protected void assignPerson(Person person) {
        mPerson = person;
    }

    public void openSecondCard() {
        mHiddenSecondCard = false;
        getCardAt(1).setHidden(false);

        updateValue();
    }

    public boolean hasHiddenSecondCard() {
        return mHiddenSecondCard;
    }

    @Override
    public boolean canBeBlackjack() {
        boolean canBe = super.canBeBlackjack();
        if (!canBe && (getCountCard() == 2)) {
            int upScore = getCardAt(0).getScore();
            if (mHiddenSecondCard && ((upScore == 1) || (upScore == 10))) {
                canBe = mPeekHoleResult != PeekHoleResult.NO_BLACKJACK;
            }
        }
        return canBe;
    }

    @Override
    public String getScoreText() {
        return (mHiddenSecondCard ?
                ("Up-" + ((mScore == 11) ?
                        "ACE" : String.valueOf(mScore))) : super.getScoreText());
    }

    @Override
    protected void updateValue() {
        if (mHiddenSecondCard) {
            Card secondCard = mCards.get(1);    // update values without second card
            mCards.remove(1);
            super.updateValue();
            mCards.add(1, secondCard);
        } else {
            super.updateValue();
        }
    }

    // Approach
    @Override
    public void evaluateStatus() {

        int nCards = getCountCard();
        if (mService.pGameRule.ruleSecondCardDeal == Rule.RuleSecondCardDeal.AT_ONCE) {
            if (nCards == 2) {
                mHiddenSecondCard = true;
                getCardAt(1).setHidden(true);
                updateValue();
            }
        }

        if (mScore >= 17) {
            setDealDone();
        }
    }
}
