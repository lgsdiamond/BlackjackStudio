package com.lgsdiamond.blackjackstudio.BlackjackElement;

/**
 * Created by lgsdiamond on 2015-10-02.
 */
public class Player extends Person {
    public Player(BjService service, String name, double bankroll) {
        super(service, name, bankroll);
        mRecord = new PlayerData();
    }

    @Override
    void updateGameData_Balance() {
        if (BjService.isAutoRunning()) return;

        BjService.sGameData.setBankRoll(getBalance());
        BjService.sGameData.setBankRollChange(getBalanceChange());
    }

    public void readyRound() {
        mCountSplitAction = 0;
    }

    public double getBankroll() {
        return super.mBalance;
    }

    public boolean hasEnoughBankroll(double betAmount) {
        return (getBankroll() >= betAmount);
    }

    public BjService getService() {
        return mService;
    }

    private int mCountSplitAction;

    public void addSplitCountAction() {
        mCountSplitAction++;
    }

    // sub-class for player data
    public class PlayerData {
        public int totalRound;              // number of rounds

        public int maxCountCards;           // maximum number of cards

        public int totalHand;               // number of hands, including split hands
        public int countWin;                // number of WIN, among totalHand
        public int countPush;               // number of PUSH, among totalHand
        public int countLost;               // number of LOST, among totalHand

        public int countDoubledown;         // number of doubledown
        public int countDoubledown_Win;      // number of WIN of doubledown
        public int countDoubledown_Push;     // number of WIN of doubledown
        public int countDoubledown_Lost;     // number of WIN of doubledown

        public int countBlackjack;          // number of Blackjack, among win/push
        public int countBlackjack_Win;       // number of Blackjack Win, among blackjack
        public int countBlackjack_Push;      // number of Blackjack Win, among blackjack
        public int countSurrender;          // number of surrender, among lost
        public int countBust;               // number of BUST, among lost

        public int countScore21;                   // number of score-21, excluding Blackjack
        public int countScore20;                   // number of score-20
        public int countScore19;                   // number of score-19
        public int countScore18;                   // number of score-18
        public int countScore17;                   // number of score-17
        public int countScoreUnder17;              // number of score-17

        public int countScore21_Win;                   // number of score-21, excluding Blackjack
        public int countScore20_Win;                   // number of score-20
        public int countScore19_Win;                   // number of score-19
        public int countScore18_Win;                   // number of score-18
        public int countScore17_Win;                   // number of score-17
        public int countScoreUnder17_Win;              // number of score-17

        public int countScore21_Push;                   // number of score-21, excluding Blackjack
        public int countScore20_Push;                   // number of score-20
        public int countScore19_Push;                   // number of score-19
        public int countScore18_Push;                   // number of score-18
        public int countScore17_Push;                   // number of score-17
        public int countScoreUnder17_Push;              // number of score-17

        public int countScore21_Lost;                   // number of score-21, excluding Blackjack
        public int countScore20_Lost;                   // number of score-20
        public int countScore19_Lost;                   // number of score-19
        public int countScore18_Lost;                   // number of score-18
        public int countScore17_Lost;                   // number of score-17
        public int countScoreUnder17_Lost;              // number of score-17

        public int countSplit;                  // number of split chance

        public int currentWinStreak;            // current number of WIN streak
        public int currentPushStreak;           // current number of PUSH streak
        public int currentLostStreak;                  // current number of LOST streak
        public int maxWinStreak;                // maximum number of WIN streak
        public int maxPushStreak;               // maximum number of PUSH streak
        public int maxLostStreak;               // maximum number of LOST streak

        public static final int MAX_COUNT_STREAKS = 10;         // 0 means single streak
        public int[] winStreaks = new int[MAX_COUNT_STREAKS];    // count win streak up to 10
        public int[] pushStreaks = new int[MAX_COUNT_STREAKS];   // count push streak up to 10
        public int[] lostStreaks = new int[MAX_COUNT_STREAKS];   // count lost streak up to 10

        public double maxWinAmount;             // maximum bankroll - initial bankroll
        public double minWinAmount;             // minimum bankroll - initial bankroll

        public double mOpenAmount;              // for candlestick data
        public double mCloseAmount;             // for candlestick data
        public double mShadowHighAmount;        // for candlestick data
        public double mShadowLowAmount;         // for candlestick data

        public BjService.RoundResult lastRoundResult;
        public double maxBetAmount;             // maximum betting amount
        public double minBetAmount;             // minimum betting amount
        public double averageBetAmount;         // average betting amount

        public void openCandlestick() {
            mOpenAmount = mCloseAmount = mShadowHighAmount = mShadowLowAmount = getBankroll();
        }

        public void closeCandleStick() {
            setCandleStickShadow();
            mCloseAmount = getBankroll();
        }

        public void setCandleStickShadow() {
            double shadow = getBankroll();
            if (shadow > mShadowHighAmount) mShadowHighAmount = shadow;
            if (shadow < mShadowLowAmount) mShadowLowAmount = shadow;
        }
    }

    public PlayerData mRecord;

    public void recordData(PlayerHand hand) {
        // we can set number of hands here
        mRecord.totalHand++;

        // we can check count of cards here
        int nCards = hand.getCardCount();
        if (nCards > mRecord.maxCountCards) mRecord.maxCountCards = nCards;

        // we can check betAmount here
        double betAmount = hand.getBetAmount();
        if (hand.pDoubledowned) betAmount /= 2.0;

        if (betAmount > mRecord.maxBetAmount) mRecord.maxBetAmount = betAmount;
        if (mRecord.minBetAmount == 0.0) mRecord.minBetAmount = betAmount;
        else if (betAmount < mRecord.minBetAmount) mRecord.minBetAmount = betAmount;

        // win/push/lost/blackjackWin/blackjackPush/surrender
        mRecord.lastRoundResult = hand.getRoundResult();
        switch (mRecord.lastRoundResult) {
            case UNKNOWN:
                break;

            case WIN:
                mRecord.countWin++;
                if (hand.isBlackjack()) mRecord.countBlackjack_Win++;
                if (hand.pDoubledowned) mRecord.countDoubledown_Win++;
                break;

            case PUSH:
                mRecord.countPush++;
                if (hand.isBlackjack()) mRecord.countBlackjack_Push++;
                if (hand.pDoubledowned) mRecord.countDoubledown_Push++;
                break;

            case LOST:
                mRecord.countLost++;
                if (hand.pSurrendered) mRecord.countSurrender++;
                if (hand.pDoubledowned) mRecord.countDoubledown_Lost++;
                break;
        }

        // blackjack/bust
        if (hand.isBlackjack()) mRecord.countBlackjack++;
        else if (hand.isBust()) mRecord.countBust++;

        // blackjack/bust
        if (hand.pDoubledowned) mRecord.countDoubledown++;

        switch (hand.mScore) {
            case 17:
                mRecord.countScore17++;
                switch (hand.getRoundResult()) {
                    case UNKNOWN:
                        break;
                    case WIN:
                        mRecord.countScore17_Win++;
                        break;
                    case PUSH:
                        mRecord.countScore17_Push++;
                        break;
                    case LOST:
                        mRecord.countScore17_Lost++;
                        break;
                }
                break;
            case 18:
                mRecord.countScore18++;
                switch (hand.getRoundResult()) {
                    case UNKNOWN:
                        break;
                    case WIN:
                        mRecord.countScore18_Win++;
                        break;
                    case PUSH:
                        mRecord.countScore18_Push++;
                        break;
                    case LOST:
                        mRecord.countScore18_Lost++;
                        break;
                }
                break;
            case 19:
                mRecord.countScore19++;
                switch (hand.getRoundResult()) {
                    case UNKNOWN:
                        break;
                    case WIN:
                        mRecord.countScore19_Win++;
                        break;
                    case PUSH:
                        mRecord.countScore19_Push++;
                        break;
                    case LOST:
                        mRecord.countScore19_Lost++;
                        break;
                }
                break;
            case 20:
                mRecord.countScore20++;
                switch (hand.getRoundResult()) {
                    case UNKNOWN:
                        break;
                    case WIN:
                        mRecord.countScore20_Win++;
                        break;
                    case PUSH:
                        mRecord.countScore20_Push++;
                        break;
                    case LOST:
                        mRecord.countScore20_Lost++;
                        break;
                }
                break;
            case 21:
                if (!hand.isBlackjack()) {
                    mRecord.countScore21++;
                    switch (hand.getRoundResult()) {
                        case UNKNOWN:
                            break;
                        case WIN:
                            mRecord.countScore21_Win++;
                            break;
                        case PUSH:
                            mRecord.countScore21_Push++;
                            break;
                        case LOST:
                            mRecord.countScore21_Lost++;
                            break;
                    }
                }
                break;
            default:
                if (hand.mScore < 17) {
                    mRecord.countScoreUnder17++;
                    switch (hand.getRoundResult()) {
                        case UNKNOWN:
                            break;
                        case WIN:
                            mRecord.countScoreUnder17_Win++;
                            break;
                        case PUSH:
                            mRecord.countScoreUnder17_Push++;
                            break;
                        case LOST:
                            mRecord.countScoreUnder17_Lost++;
                            break;
                    }
                }
                break;
        }
    }

    public void defineStreak(double winAmount) {
        // we can set number of round here
        mRecord.totalRound++;

        // we can check bankroll here
        double winBalance = mBalance - mBalanceInitial;
        if (winBalance > mRecord.maxWinAmount) mRecord.maxWinAmount = winBalance;
        if (winBalance < mRecord.minWinAmount) mRecord.minWinAmount = winBalance;

        // split action
        mRecord.countSplit += mCountSplitAction;

        // win-push-lost streak
        if (winAmount > 0.0) {
            mRecord.currentWinStreak++;
            if (mRecord.currentWinStreak > mRecord.maxWinStreak)
                mRecord.maxWinStreak = mRecord.currentWinStreak;
            if ((mRecord.currentPushStreak > 0)
                    && (mRecord.currentPushStreak <= Player.PlayerData.MAX_COUNT_STREAKS)) {
                mRecord.pushStreaks[mRecord.currentPushStreak - 1]++;
                mRecord.currentPushStreak = 0;
            } else if ((mRecord.currentLostStreak > 0)
                    && (mRecord.currentLostStreak <= Player.PlayerData.MAX_COUNT_STREAKS)) {
                mRecord.lostStreaks[mRecord.currentLostStreak - 1]++;
                mRecord.currentLostStreak = 0;
            }
        } else if (winAmount == 0.0) {
            mRecord.currentPushStreak++;
            if (mRecord.currentPushStreak > mRecord.maxPushStreak)
                mRecord.maxPushStreak = mRecord.currentPushStreak;
            if ((mRecord.currentWinStreak > 0)
                    && (mRecord.currentWinStreak <= Player.PlayerData.MAX_COUNT_STREAKS)) {
                mRecord.winStreaks[mRecord.currentWinStreak - 1]++;
                mRecord.currentWinStreak = 0;
            } else if ((mRecord.currentLostStreak > 0)
                    && (mRecord.currentLostStreak <= Player.PlayerData.MAX_COUNT_STREAKS)) {
                mRecord.lostStreaks[mRecord.currentLostStreak - 1]++;
                mRecord.currentLostStreak = 0;
            }
        } else {
            mRecord.currentLostStreak++;
            if (mRecord.currentLostStreak > mRecord.maxLostStreak)
                mRecord.maxLostStreak = mRecord.currentLostStreak;
            if ((mRecord.currentPushStreak > 0)
                    && (mRecord.currentPushStreak <= Player.PlayerData.MAX_COUNT_STREAKS)) {
                mRecord.pushStreaks[mRecord.currentPushStreak - 1]++;
                mRecord.currentPushStreak = 0;
            } else if ((mRecord.currentWinStreak > 0)
                    && (mRecord.currentWinStreak <= Player.PlayerData.MAX_COUNT_STREAKS)) {
                mRecord.winStreaks[mRecord.currentWinStreak - 1]++;
                mRecord.currentWinStreak = 0;
            }
        }
    }
}