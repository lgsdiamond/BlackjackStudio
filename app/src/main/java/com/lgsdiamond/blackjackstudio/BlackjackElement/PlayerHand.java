package com.lgsdiamond.blackjackstudio.BlackjackElement;

import com.lgsdiamond.blackjackstudio.BlackjackUtils.UtilityStudio;

/**
 * Created by lgsdiamond on 2015-10-02.
 */
public class PlayerHand extends Hand {

    public enum Action {UNKNOWN, HIT, STAND, SPLIT, DOUBLEDOWN, SURRENDER_OR_HIT, SURRENDER_OR_STAND}

    private Dealer mDealer;
    private int mSplitCount;
    private boolean mCanSplit;
    private boolean mCanDoubledown;
    private boolean mCanSurrender;
    private boolean mCanHit;

    private double mBetAmount;
    private boolean mBetConfirmed = false;
    private double mWinAmount = 0.0;

    private double mInsuredAmount = 0.0;

    private BjService.RoundResult mRoundResult = BjService.RoundResult.UNKNOWN;
    private boolean mHasBetCovered = false;
    private boolean mHasInsuredCovered = false;

    private final BettingBox mBettingBox;

    // for record data
    public boolean pSurrendered = false;
    public boolean pDoubledowned = false;

    public PlayerHand(BjService service, Dealer dealer, BettingBox bettingBox, Player player,
                      double betAmount, int splitCount) {
        super(service);

        mDealer = dealer;
        mBettingBox = bettingBox;
        assignPerson(player);

        mBetAmount = betAmount;
        mSplitCount = splitCount;

        bettingBox.setSittingPlayer(player);      // the player is sitting on the box
    }

    @Override
    protected void assignPerson(Person person) {
        mPerson = person;
    }

    @Override
    protected void updateValue() {
        super.updateValue();

        if (mIsBlackjack && (mSplitCount != 0)) mIsBlackjack = false;   // no split-blackjack

        mCanSplit = checkSplit();
        mCanDoubledown = checkDoubleDown();
        mCanSurrender = checkSurrender();
        mCanHit = checkHit();
    }

    // getter
    @Override
    public String toString() {
        String handString = super.toString();
        handString += "<" + (canDoubledown() ? "D" : "") + (mCanSplit ? "Sp" : "")
                + (mCanSurrender ? "Sr" : "") + (mCanHit ? "H" : "")
                + (isInsured() ? "I" : "") + ">";
        return handString;
    }

    public double getWinAmount() {
        return mWinAmount;
    }

    public void setWinAmount(double amount) {
        mWinAmount = amount;
    }

    public boolean hasBet() {
        return (mBetAmount > 0.0);
    }

    public int getBoxIndex() {
        return mBettingBox.getIndex();
    }

    public BettingBox getBox() {
        return mBettingBox;
    }

    public Player getPlayer() {
        return ((Player) super.mPerson);
    }

    public boolean canSplit() {
        return mCanSplit;
    }

    public boolean canDoubledown() {
        return mCanDoubledown;
    }

    public boolean canSurrender() {
        return mCanSurrender;
    }

    public boolean isInsured() {
        return (mInsuredAmount > 0.0);
    }

    public double getInsuredAmount() {
        return mInsuredAmount;
    }

    public BjService.RoundResult getRoundResult() {
        return mRoundResult;
    }

    public void setRoundResult(BjService.RoundResult result) {
        mRoundResult = result;
        setBetCovered();

        switch (mRoundResult) {
            case WIN:
                BjService.sGameData.addPlayerWin();
                break;
            case PUSH:
                BjService.sGameData.addPlayerPush();
                break;
            case LOST:
                BjService.sGameData.addPlayerLost();
                break;
        }
    }

    public void setBetCovered() {
        mHasBetCovered = true;
    }

    public void setInsuredCovered() {
        mHasInsuredCovered = true;
    }

    public boolean hasInsuredCovered() {
        return mHasInsuredCovered;
    }

    public boolean needDealCauseInsured() {
        return (isInsured() && !mHasInsuredCovered);
    }

    public boolean stillAlive() {
        return (!mHasBetCovered);
    }

    public int getSplitCount() {
        return mSplitCount;
    }

    public void setDoubledown() {
        pDoubledowned = true;
    }

    // checking conditions
    private boolean checkHit() {
        if (isBust() || isTwentyOne()) return false;

        if (getCardCount() == 0) return true;

        return !((mSplitCount > 0)
                && (mCards.get(0).mRank == Card.CardRank.ACE)
                && (getCountCard() > 1));

    }

    private boolean checkSplit() {
        if ((mCards.size() != 2)    // not 2 cards
                || (mSplitCount >= mService.pGameRule.maxSplitCount) // over maximum split count
                || (mCards.get(0).getScore() != mCards.get(1).getScore()))  // different values
            return false;

        if ((!mService.pGameRule.allowSplitDifferentTenValues)  // different number split not allowed
                && (mCards.get(0).getRank() != mCards.get(1).getRank())) // different number
            return false;

        if ((mSplitCount > 0)
                && (mCards.get(0).mRank == Card.CardRank.ACE)   // it is Ace-split
                && !mService.pGameRule.allowAceResplit)                // Ace re-split not allowed
            return false;

        if (!BjService.isAutoRunning()) {         // in autoRun, do not care bankroll
            if (!getPlayer().hasEnoughBankroll(mBetAmount)) return false;
        }

        return true;
    }

    private boolean checkDoubleDown() {
        if ((mCards.size() != 2)
                || ((mSplitCount > 0) && !mService.pGameRule.allowDoubledownAfterSplit))
            return false;

        if ((mSplitCount > 0) && (mCards.get(0).mRank == Card.CardRank.ACE))
            return false;   // no doubledown allowed for Ace-Split card

        switch (mService.pGameRule.ruleDoubledown) {
            case ANY_TWO_CARDS:
                if (mIsTwentyOne) return false;     // 21 can not doubledown
                break;
            case ONLY_NINE_TEN_ELEVEN:
                if ((mScore != 9) && (mScore != 10) && (mScore != 11)) return false;
                break;
            case ONLY_TEN_ELEVEN:
                if ((mScore != 10) && (mScore != 11)) return false;
                break;
        }

        if (!BjService.isAutoRunning()) {         // in autoRun, do not care bankroll
            if (!getPlayer().hasEnoughBankroll(mBetAmount)) return false;
        }

        return true;
    }

    private boolean checkSurrender() {
        return (mService.pGameRule.allowSurrender
                && (mCards.size() == 2)
                && !mIsTwentyOne);
    }

    //=== Betting ===

    public void placeBetMore(double betAmount) {
        getPlayer().takeOutBalance(betAmount);
        mBetAmount += betAmount;
    }

    public void resetPlayerBet() {
        getPlayer().putInBalance(mBetAmount);
        mBetAmount = 0.0;
        mBetConfirmed = false;
    }

    public void confirmBet() {
        mBetConfirmed = true;
        mBettingBox.confirmBet(mBetAmount);
    }

    public boolean hasBetConfirmed() {
        return mBetConfirmed;
    }

    public double getBetAmount() {
        return mBetAmount;
    }

    public void setBetAmount(double betAmount) {
        mBetAmount = betAmount;
    }

    public void takeBackBet() {
        getPlayer().putInBalance(mBetAmount);
        setBetAmount(0.0);
        mBetConfirmed = false;  // no confirm with no bet
    }

    public void acceptInsurance() {
        Player player = getPlayer();
        double insured = getBetAmount() * 0.5;
        player.takeOutBalance(insured);
        mInsuredAmount = insured;
    }

    public void cancelInsurance() {
        Player player = getPlayer();
        player.putInBalance(mInsuredAmount);
        mInsuredAmount = 0.0;
    }

    //=== Dealing ===
    public PlayerHand split() {
        Player player = getPlayer();
        player.takeOutBalance(mBetAmount);

        PlayerHand splitHand = new PlayerHand(getService(), mDealer, mBettingBox,
                getPlayer(), mBetAmount, (++mSplitCount));
        splitHand.confirmBet();
        Card card = removeLastCard();
        splitHand.addCard(card);

        return splitHand;
    }

    // Approach

    @Override
    public void evaluateStatus() {
        if (isBust()) {
            mDealer.takeHandBet(this);
            setDealDone();
            UtilityStudio.playBustSound();
        } else if (!mCanHit) {
            setDealDone();
        }
    }

    // Strategy
    public Action getBestPlayAction(int upScore) {
        PlayerHand.Action action;
        Strategy strategy = mBettingBox.getStrategy();
        if (strategy == null) {
            action = mService.getBestPlayAction();
        } else {
            action = strategy.getBestAction(this, upScore);
        }
        return action;
    }

    public boolean getBestInsuranceAction() {
        Strategy strategy = mBettingBox.getStrategy();
        if (strategy == null) return false;           //not taking insurance

        return strategy.getBestInsuranceAction();
    }
}