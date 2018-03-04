package com.lgsdiamond.blackjackstudio.BlackjackElement;

/**
 * Created by lgsdiamond on 2015-10-02.
 */
public class Dealer extends Person {
    private DealerHand mHand;

    public Dealer(BjService service, String name, double potBalance) {
        super(service, name, potBalance);
        mHand = new DealerHand(service, Dealer.this);

        mRecord = new DealerData();
    }

    public void readyRound() {
        mHand = new DealerHand(mService, this);
    }

    @Override
    public void updateGameData_Balance() {
        if (BjService.isAutoRunning()) return;

        BjService.sGameData.setPotChange(getBalanceChange());
    }

    // getter
    @Override
    public String toString() {
        return "[" + getName() + "] " + mHand.toString();
    }

    public DealerHand getHand() {
        return mHand;
    }

    public int getUpCardScore() {
        Card card = mHand.getCardAt(0);
        return (card.getScore());
    }

    //=== Betting ===
    public void payWinPrize(Player player, double betAmount, double winPrize) {
        takeOutBalance(winPrize);
        player.putInBalance(betAmount + winPrize);
    }

    //=== Paying ===
    public void takeHandInsured(PlayerHand hand) {
        double insured = hand.getInsuredAmount();
        putInBalance(insured);

        hand.setInsuredCovered();
    }

    public void payHandInsurance(PlayerHand hand) {
        double insured = hand.getInsuredAmount();
        double winInsured = insured * 2.0;
        payWinPrize(hand.getPlayer(), insured, winInsured);
        hand.addRewardAmount(insured + winInsured);

        hand.setInsuredCovered();
    }

    public void takeHandBet(PlayerHand hand) {
        double betAmount = hand.getBetAmount();
        putInBalance(betAmount);

        hand.setRoundResult(BjService.RoundResult.LOST);
        hand.setWinAmount(-betAmount);
    }

    public void payHandBet(PlayerHand hand, boolean isBlackjack) {
        double betAmount = hand.getBetAmount();
        double winPrize = betAmount * (isBlackjack ? mService.pGameRule.blackjackPayout : 1.0);
        payWinPrize(hand.getPlayer(), betAmount, winPrize);
        hand.addRewardAmount(betAmount + winPrize);

        hand.setRoundResult(BjService.RoundResult.WIN);
        hand.setWinAmount(winPrize);
    }

    public void payHandBetPush(PlayerHand hand) {
        double betAmount = hand.getBetAmount();
        hand.getPlayer().putInBalance(betAmount);
        hand.addRewardAmount(betAmount);

        hand.setRoundResult(BjService.RoundResult.PUSH);
        hand.setWinAmount(0.0);
    }

    public void takeHandSurrenderBet(PlayerHand hand) {
        double halfBet = 0.5 * hand.getBetAmount();
        putInBalance(halfBet);                  // half bet to dealer's pot
        hand.getPlayer().putInBalance(halfBet); // half bet to player's bankroll
        hand.addRewardAmount(halfBet);

        hand.setRoundResult(BjService.RoundResult.LOST);
        hand.setWinAmount(-halfBet);
    }

    public class DealerData {
        public int totalRound;                     // number of rounds

        public int maxCountCards;           // maximum number of cards

        public int countScore21;                   // number of Soft-21, excluding Blackjack
        public int countScore20;                   // number of Soft-20
        public int countScore19;                   // number of Soft-19
        public int countScore18;                   // number of Soft-18
        public int countScore17;                   // number of 17
        public int countScoreSoft17;               // number of Soft-17, among 17

        public int countBlackjack;                 // number of blackjack
        public int countBust;                      // number of bust

    }

    public DealerData mRecord;

    public void recordData() {
        mRecord.totalRound++;

        int nCards = mHand.getCardCount();
        if (nCards > mRecord.maxCountCards) mRecord.maxCountCards = nCards;

        switch (mHand.getScore()) {
            case 17:
                mRecord.countScore17++;
                break;
            case 18:
                mRecord.countScore18++;
                break;
            case 19:
                mRecord.countScore19++;
                break;
            case 20:
                mRecord.countScore20++;
                break;
            case 21:
                if (!mHand.isBlackjack()) mRecord.countScore21++;
                break;
        }

        if (mHand.isBlackjack()) mRecord.countBlackjack++;
        else if (mHand.isBust()) mRecord.countBust++;
    }

    public void recordSoft17() {
        mRecord.countScoreSoft17++;
    }
}