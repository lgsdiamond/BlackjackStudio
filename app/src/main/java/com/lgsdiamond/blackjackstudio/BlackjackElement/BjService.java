package com.lgsdiamond.blackjackstudio.BlackjackElement;

import android.app.IntentService;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.lgsdiamond.blackjackstudio.ActivityStudio;
import com.lgsdiamond.blackjackstudio.BlackjackUtils.UtilityStudio;
import com.lgsdiamond.blackjackstudio.FragmentSimulator;
import com.lgsdiamond.blackjackstudio.FragmentTable;

import java.util.ArrayList;

public class BjService extends IntentService {
    public static final String SERVICE_TAG = "SERVICE";
    public static final String SERVICE_STAGE_TAG = "STAGE";
    public static final String SERVICE_ACTION_TAG = "ACTION";
    public static final String SERVICE_doStageAction = "doStageAction";
    public static final String SERVICE_startRoundStage = "startRoundStage";
    public static final String SERVICE_doPlayerDealAction = "doPlayerDealAction";
    public static final String SERVICE_assureInsuranceAcceptance = "assureInsuranceAcceptance";

    public void notifyCutCardDealt() {
        if (!isAutoRunning()) {
            FragmentTable.sAnimation_Done = false;
            FragmentTable.sendUiMessage(FragmentTable.UI_MESSAGE.CUT_CARD_DEALT);
            while (!FragmentTable.sAnimation_Done) ;
        }
    }

    public enum RoundStage {
        SETTING, IDLE, BETTING, INITIAL_DEAL, DEALING, PAYING
    }

    private static RoundStage getNextStage(RoundStage stage) {
        RoundStage nextStage = RoundStage.SETTING;

        switch (stage) {
            case SETTING:
                nextStage = RoundStage.IDLE;
                break;
            case IDLE:
                nextStage = RoundStage.BETTING;
                break;
            case BETTING:
                nextStage = RoundStage.INITIAL_DEAL;
                break;
            case INITIAL_DEAL:
                nextStage = RoundStage.DEALING;
                break;
            case DEALING:
                nextStage = RoundStage.PAYING;
                break;
            case PAYING:
                nextStage = RoundStage.IDLE;
                break;
        }
        return nextStage;
    }

    public enum RoundResult {UNKNOWN, WIN, PUSH, LOST}

    private static ArrayList<BettingData> sPrevBetting = new ArrayList<>();

    // constructor
    public BjService() {
        super("BlackjackService");
    }

    private final LocalBinder mBinder = new LocalBinder();

    //=== binding ===
    // Service Intent
    public static final String SERVICE_READY = "SERVICE_READY";

    // Round
    public static final String ROUND_IDLE_STARTED = "ROUND_IDLE_STARTED";
    public static final String ROUND_IDLE_ENDED = "ROUND_IDLE_ENDED";

    public static final String ROUND_BETTING_STARTED = "ROUND_BETTING_STARTED";
    public static final String ROUND_BETTING_ENDED = "ROUND_BETTING_ENDED";

    public static final String ROUND_INITIAL_DEAL_STARTED = "ROUND_INITIAL_DEAL_STARTED";
    public static final String ROUND_INITIAL_DEAL_ENDED = "ROUND_INITIAL_DEAL_ENDED";

    public static final String ROUND_DEALING_STARTED = "ROUND_DEALING_STARTED";
    public static final String ROUND_DEALING_ENDED = "ROUND_DEALING_ENDED";

    public static final String ROUND_PAYING_STARTED = "ROUND_PAYING_STARTED";
    public static final String ROUND_PAYING_ENDED = "ROUND_PAYING_ENDED";

    // Dealing
    public static final String ROUND_STAGE_CONTINUE = "ROUND_STAGE_CONTINUE";
    public static final String ROUND_STAGE_FINISHED = "ROUND_STAGE_FINISHED";

    public static final String ROUND_PAYING_FINISHED = "ROUND_PAYING_FINISHED";

    // TODO: add more intent

    private boolean mIsBound = false;

    @Override
    public IBinder onBind(Intent intent) {
        mIsBound = true;
        return mBinder;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String serviceTag;
        serviceTag = intent.getStringExtra(SERVICE_TAG);
        switch (serviceTag) {
            case SERVICE_doStageAction:
                doStageAction();
                break;

            case SERVICE_startRoundStage:
                int stageOrdinal = intent.getIntExtra(SERVICE_STAGE_TAG, 0);
                RoundStage stage = RoundStage.values()[stageOrdinal];
                startRoundStage(stage);
                break;

            case SERVICE_doPlayerDealAction:
                int actionOrdinal = intent.getIntExtra(SERVICE_ACTION_TAG, 0);
                PlayerHand.Action action = PlayerHand.Action.values()[actionOrdinal];
                onPlayerDealAction(action);
                break;


            case SERVICE_assureInsuranceAcceptance:
                assureInsuranceAcceptance();
                break;

            default:
                // TODO: Something wrong
                break;
        }
    }

    public class LocalBinder extends Binder {
        public BjService getService() {
            return BjService.this;
        }
    }

    // getter

    public boolean isBound() {
        return mIsBound;
    }

    public void readyPlayer() {
        pPlayer.readyRound();
    }

    public void readyDealer() {
        pDealer.readyRound();
    }

    // static members
    public static GameData sGameData = null;
    public static double DEFAULT_BANKROLL = 10000.0;

    // for binding service, reference to activity
    private ActivityStudio mStudio;

    // public member, to be public for general access
    public Rule pGameRule;

    public Shoe pShoe;
    public ArrayList<BettingBox> pBettingBoxes;
    public Dealer pDealer;
    public Player pPlayer;

    public Round pRound;
    private final ArrayList<PlayerHand> mActingHands = new ArrayList<>();

    // private member, to be safe
    private Hand mHandFocused = null, mHandFocused_prev = null;

    // getter & setter

    public void setStudio(ActivityStudio studio) {
        mStudio = studio;
    }

    public ActivityStudio getStudio() {
        return mStudio;
    }

    private FragmentTable mTable;
    private FragmentSimulator mSimulator;

    public void setTable(FragmentTable mFragmentTable) {
        mTable = mFragmentTable;
    }

    public FragmentTable getTable() {
        return mTable;
    }

    public DealerHand getDealerHand() {
        return pDealer.getHand();
    }

    public int getIndexByPlayerHand(PlayerHand hand) {
        return getActingHands().indexOf(hand);
    }

    public ArrayList<PlayerHand> getActingHands() {
        return pRound.getActingHands();
    }

    public Hand getHandFocused() {
        return mHandFocused;
    }

    public void setHandFocused(Hand hand) {
        if (mHandFocused != hand) {
            mHandFocused_prev = mHandFocused;
            mHandFocused = hand;
            FragmentTable.sendUiMessage(FragmentTable.UI_MESSAGE.HAND_FOCUSED_CHANGED);
        }
    }

    public Hand getHandFocused_prev() {
        return mHandFocused_prev;
    }

    public void setPlayerHandFocusedByIndex(int index) {
        setHandFocused(getActingHands().get(index));
    }

    //=== Rounds ===
    public BettingBox getBoxByIndex(int index) {
        return pBettingBoxes.get(index);
    }

    public PlayerHand.Action getBestPlayAction() {
        Hand hand = getHandFocused();
        if (hand instanceof DealerHand) return PlayerHand.Action.STAND; //?? never happen

        Strategy strategy = ((PlayerHand) hand).getBox().getStrategy();

        return (strategy.getBestAction(((PlayerHand) hand), getDealerHand().getUpScore()));
    }

    public void takeInsuranceAcceptance(PlayerHand hand, boolean isChecked) {
        if (isChecked) {
            hand.acceptInsurance();
        } else {
            hand.cancelInsurance();
        }
    }

    public void takeBestInsuranceAcceptance() {
        for (PlayerHand hand : pRound.getActingHands()) {
            takeInsuranceAcceptance(hand, hand.getBestInsuranceAction());
        }
    }

    public void assureInsuranceAcceptance() {
        getDealerHand().setInsuranceOffered();
        pRound.doStageLoop();
    }

    public void initializeGameData() {
        pShoe.reset();

        //reset balance
        pDealer.resetBalance();
        pPlayer.resetBalance();

        // reset accumulated game data
        sGameData.resetData();

        // update game data
        updateGameData();

        // display update
        FragmentTable.sendUiMessage(FragmentTable.UI_MESSAGE.POPULATE_GAME_DATA);
    }

    public void updateGameData() {
        sGameData.setBankRoll(pPlayer.getBankroll());
        sGameData.setBankRollChange(pPlayer.getBalanceChange());
        sGameData.setPotChange(pDealer.getBalanceChange());
    }

    public void sendServiceIntent(String intentStr) {
        if (isAutoRunning()) {
            pRound.dispatchIntentActionForAutoRun(intentStr);
        } else {
            Intent intent = new Intent(intentStr);
            sendBroadcast(intent);
        }
    }

    private static boolean sAutoRun = false;

    public static boolean isAutoRunning() {
        return (sAutoRun);
    }

    public static void setAutoRunning(boolean autoRun) {
        sAutoRun = autoRun;
    }

    //=== Setup ===
    public void setGameRule(Rule rule) {
        pGameRule = rule;
    }

    public void initialize(boolean isAutoRound) {
        // Shoe
        pShoe = new Shoe(this, pGameRule.mCountDecks);

        // Boxes
        pBettingBoxes = new ArrayList<>(pGameRule.mCountBoxes);

        for (int i = 0; i < pGameRule.mCountBoxes; i++) {
            pBettingBoxes.add(new BettingBox(i, pGameRule));
        }

        // Dealer
        pDealer = new Dealer(this, "Dealer-Madam", 0.0);            // actually infinitive
        pRound = new Round(isAutoRound);

        // Player, single player for now
        pPlayer = new Player(this, "Player-LGS", DEFAULT_BANKROLL);

        // announce that the "Service" is ready now
        if (mIsBound) {
            sendBroadcast(new Intent(BjService.SERVICE_READY));
            UtilityStudio.LogD("Service - Initialized");
        }
    }

    public void resetBoxes() {
        if (pGameRule.mCountBoxes == pBettingBoxes.size())
            return;

        if (pGameRule.mCountBoxes > pBettingBoxes.size()) {
            for (int i = pBettingBoxes.size(); i < pGameRule.mCountBoxes; i++) {
                pBettingBoxes.add(new BettingBox(i, pGameRule));
            }
        } else {
            while (pGameRule.mCountBoxes < pBettingBoxes.size()) {
                pBettingBoxes.remove(pBettingBoxes.size() - 1);
            }
        }
    }

    //=== Deal ===
    public void onPlayerDealAction(PlayerHand.Action action) {
        if (!isAutoRunning() && pGameRule.mUseSound) {
            FragmentTable.sendUiMessage(FragmentTable.UI_MESSAGE.PLAYER_ACTION_SOUND, action);
            UtilityStudio.takeThreadPause(1500);
        }
        pRound.onPlayerDealAction(action);
    }

    //=== Betting ===
    public void raiseBet_Hand(PlayerHand hand, double bet) {
        hand.placeBetMore(bet);
    }

    public void raiseBet(double bet) {
        if ((mHandFocused != null) && (mHandFocused instanceof PlayerHand))
            raiseBet_Hand((PlayerHand) mHandFocused, bet);
        FragmentTable.sendUiMessage(FragmentTable.UI_MESSAGE.PLAYER_BET_CHANGED);
    }

    public void resetBet_Hand(PlayerHand hand) {
        hand.resetPlayerBet();
    }

    public void resetBet() {
        if ((mHandFocused != null) && (mHandFocused instanceof PlayerHand))
            resetBet_Hand((PlayerHand) mHandFocused);
        FragmentTable.sendUiMessage(FragmentTable.UI_MESSAGE.PLAYER_BET_CHANGED);
    }

    public void confirmBet_Hand(PlayerHand hand) {
        hand.confirmBet();
    }

    public void confirmBet() {
        if ((mHandFocused != null) && (mHandFocused instanceof PlayerHand))
            confirmBet_Hand((PlayerHand) mHandFocused);

        FragmentTable.sendUiMessage(FragmentTable.UI_MESSAGE.PLAYER_BET_CHANGED);
    }

    //=== round related ===
    public void readyShoe() {
        pShoe.changeSeedCondition(pGameRule.useRandomShoe);

        if (pShoe.needShuffle())
            pShoe.shuffle();
    }

    public void startRoundStage(RoundStage stage) {
        if (pRound.getStage() != stage) {
            pRound.setStage(stage);
            pRound.beReady();
            switch (stage) {
                case SETTING:
                    break;
                case IDLE:
                    sendServiceIntent(BjService.ROUND_IDLE_STARTED);
                    break;
                case BETTING:
                    sendServiceIntent(BjService.ROUND_BETTING_STARTED);
                    break;
                case INITIAL_DEAL:
                    sendServiceIntent(BjService.ROUND_INITIAL_DEAL_STARTED);
                    break;
                case DEALING:
                    sendServiceIntent(BjService.ROUND_DEALING_STARTED);
                    break;
                case PAYING:
                    sendServiceIntent(BjService.ROUND_PAYING_STARTED);
                    break;
            }
        } else {
            pRound.doStageLoop();
            switch (stage) {
                case SETTING:
                    break;
                case IDLE:
                    sendServiceIntent(BjService.ROUND_IDLE_ENDED);
                    break;
                case BETTING:
                    sendServiceIntent(BjService.ROUND_BETTING_ENDED);
                    break;
                case INITIAL_DEAL:
                    break;
                case DEALING:
                    break;
                case PAYING:
                    sendServiceIntent(BjService.ROUND_PAYING_ENDED);
                    break;
            }
        }
    }

    //=== Round Life Cycle  ===
    //=== Testing ===
    public void increasePlayerBankroll() {
        pPlayer.ResetBankroll();
    }

    public void setGameDataDrawFlag(boolean drawFlag) {
        sGameData.setDrawDataFlag(drawFlag);
    }

    // Round
    private int mRoundIndex = 0;

    public void addRoundIndex() {
        mRoundIndex++;
    }

    public int getRoundIndex() {
        return mRoundIndex;
    }

    // Stage
    public void doStageAction() {
        pRound.doStageLoop();
    }

    public void forcedUpdateShoeCount() {
        boolean fDraw = sGameData.getDrawDataFlag();
        sGameData.setDrawDataFlag(true);
        sGameData.subtractShoeRemainCount();
        sGameData.setDrawDataFlag(fDraw);
    }

    //=== BettingData ===

    public class BettingData {
        int mBoxIndex;
        double mBetAmount;

        public BettingData(int boxIndex, double betAmount) {
            mBetAmount = betAmount;
            mBoxIndex = boxIndex;
        }
    }

    //=== Round ===

    public class Round {
        private RoundStage mStage;

        boolean mIsAutoRound;

        public Round(boolean isAutoRound) {
            mStage = RoundStage.SETTING;                 // beginning with SETTING
            boolean mIsAutoRound = isAutoRound;
        }

        // getter

        public boolean setAutoRound() {
            return mIsAutoRound = true;
        }

        public RoundStage getStage() {
            return mStage;
        }

        public void setStage(RoundStage stage) {
            mStage = stage;
        }

        public ArrayList<PlayerHand> getActingHands() {
            return mActingHands;
        }

        // ready action
        public void beReady(RoundStage stage) {
            setStage(stage);
            beReady();
        }

        public void beReady() {
            setGameDataDrawFlag(false);
            switch (mStage) {
                case SETTING:
                case DEALING:
                case PAYING:
                    break;

                case IDLE:  // IDLE stage will be used for game table, but never for simulation
                    readyShoe();
                    resetBoxes();
                    readyDealer();
                    readyPlayer();
                    readyInitialActingHands();
                    break;

                case BETTING:
                    if (!BjService.isAutoRunning())
                        setGameDataDrawFlag(true);
                    setPlayerHandFocusedByIndex(0);    // first hand focused automatically
                    break;

                case INITIAL_DEAL:
                    removeUnconfirmed();                // remove unconfirmed bet
                    makePrevBettingData();              // for next round
                    break;
            }
        }

        PlayerHand.Action mPlayerDealAction = PlayerHand.Action.UNKNOWN;

        public void onPlayerDealAction(PlayerHand.Action playerDealAction) {
            mPlayerDealAction = playerDealAction;
            if (mPlayerDealAction != PlayerHand.Action.UNKNOWN) doStageLoop();
        }

        private void updateRoundGameData() {
            double totalBet = 0;
            double totalWinning = 0;
            for (PlayerHand hand : mActingHands) {
                totalBet += hand.getBetAmount();
                totalBet += hand.getInsuredAmount();
                totalWinning += hand.getRewardAmount();
            }
            BjService.sGameData.setLastTotalBet(totalBet);
            BjService.sGameData.setLastWinningChange(totalWinning - totalBet);

            BjService.sGameData.setDealerRound(getRoundIndex());
        }

        //====
        private void readyInitialActingHands() {
            mActingHands.clear();

            for (BettingBox bettingBox : pBettingBoxes) {
                PlayerHand hand = new PlayerHand(BjService.this, pDealer, bettingBox, pPlayer, 0, 0);
                mActingHands.add(hand);
            }

            FragmentTable.sendUiMessage(FragmentTable.UI_MESSAGE.HANDS_DATA_CHANGED);
        }

        //=== Betting ===
        private void removeUnconfirmed() {      // remove unconfirmed-bet hands
            if (BjService.isAutoRunning())    // for auto-run, no need to check bet
                return;

            for (int i = 0; i < mActingHands.size(); i++) {
                PlayerHand hand = mActingHands.get(i);
                if (!hand.hasBetConfirmed()) {   // hanging bet
                    if (hand.getBetAmount() > 0.0) hand.takeBackBet();
                    mActingHands.remove(i);
                    i--;
                }
            }
        }

        public void makePrevBettingData() {
            sPrevBetting.clear();
            for (PlayerHand hand : mActingHands) {
                double betAmount = hand.getBetAmount();
                if ((betAmount > 0.0) && (hand.hasBetConfirmed())) {
                    sPrevBetting.add(new BettingData(hand.getBoxIndex(), hand.getBetAmount()));
                }

                BettingBox bettingBox = hand.getBox();
            }
        }

        public boolean hasPrevBetting() {
            return (sPrevBetting.size() > 0);
        }

        public void usePrevBetting(boolean confirm) {
            for (BettingData data : sPrevBetting) {
                PlayerHand hand = findHandByBoxID(data.mBoxIndex);
                if (hand != null) {
                    Player player = hand.getPlayer();
                    resetBet_Hand(hand);

                    BettingBox box = hand.getBox();
                    double nextBet = box.getNextBet();

                    raiseBet_Hand(hand, nextBet);
                    if (BjService.isAutoRunning()) {
                        if (player.getBankroll() < 0.0) {
                            BjService.sGameData.setFirstRunOut();
                        }
                    }
                }
            }

            if (confirm) confirmAllBetting();
        }

        public boolean hasAnyBet() {
            for (PlayerHand hand : mActingHands) {
                if (hand.getBetAmount() > 0.0) return true;
            }
            return false;
        }

        public boolean hasConfirmedBet() {
            for (PlayerHand hand : mActingHands) {
                if (hand.hasBetConfirmed()) return true;
            }
            return false;
        }

        public void useEasyBetting(boolean confirm) {
            for (PlayerHand hand : mActingHands) {
                Player player = hand.getPlayer();
                BettingBox box = hand.getBox();
                double nextBet = box.getNextBet();

                resetBet_Hand(hand);

                if (BjService.isAutoRunning()) {
                    raiseBet_Hand(hand, nextBet);
                    if (player.getBankroll() < 0.0) {
                        BjService.sGameData.setFirstRunOut();
                    }
                } else if (player.hasEnoughBankroll(nextBet)) {
                    raiseBet_Hand(hand, nextBet);
                }
            }

            if (confirm) confirmAllBetting();
        }

        public void confirmAllBetting() {
            for (PlayerHand hand : mActingHands) {
                if (hand.hasBet()) {
                    setHandFocused(hand);
                    confirmBet();
                }
            }
        }

        public PlayerHand findHandByBoxID(int boxIndex) {
            for (PlayerHand hand : mActingHands) {
                if (boxIndex == hand.getBoxIndex()) {
                    return hand;
                }
            }
            return null;
        }

        // General Approach
        public void treatDealingOneCard() {
            Hand hand = getHandFocused();
            Card card = pShoe.drawOneCard();

            if (!isAutoRunning()) {

                // if it is second card of dealer, it should be hidden
                if ((hand == getDealerHand()) &&
                        (pGameRule.ruleSecondCardDeal == Rule.RuleSecondCardDeal.AT_ONCE) &&
                        (hand.getCountCard() == 1)) {
                    card.setHidden(true);
                }

                // card animation starting
                mTable.sAnimation_Done = false;
                mTable.notifyLastCardDeal(hand, card);
                FragmentTable.sendUiMessage(FragmentTable.UI_MESSAGE.HAND_LAST_CARD_DEAL);

                // wait till card animation ended
                while (!mTable.sAnimation_Done) ;   // if animation end, it will be true
            }

            hand.addCard(card);   // addCard should only be here!
            hand.evaluateStatus();

            if (!isAutoRunning()) {
                if (hand instanceof PlayerHand) {
                    mTable.redrawPlayerHands();
                } else {
                    if (pGameRule.mUseSound) {
                        if (hand.getCardCount() == 1) {
                            FragmentTable.sendUiMessage(
                                    FragmentTable.UI_MESSAGE.DEALER_SHOW_CARD_SOUND, card);
                            UtilityStudio.takeThreadPause(1000);
                        }
                    }
                    mTable.redrawDealerHand();
                }

                DealerHand dealerHand = getDealerHand();
                if (hand.isBust()) {
                    if (hand == dealerHand) BjService.sGameData.addDealerBust();
                    else BjService.sGameData.addPlayerBust();
                } else if (hand.isBlackjack()) {
                    if (hand == dealerHand) BjService.sGameData.addDealerBlackjack();
                    else BjService.sGameData.addPlayerBlackjack();
                }
            }
        }

        //=== for auto-run ===
        private boolean mAutoRun_GoNextStage = false;

        private boolean mAutoRun_RoundDone = false;

        public void setAutoRun_GoNextStage() {
            mAutoRun_GoNextStage = true;
        }

        public void setAutoRun_DoneRound() {
            mAutoRun_RoundDone = true;
        }

        public void runAutoOneRound() {
            readyActingHandsFromSimulator();

            readyShoe();
            readyDealer();
            for (Player player : mSimulationPlayers) {
                player.readyRound();
            }

            setHandFocused(null);
            mStage = RoundStage.INITIAL_DEAL;
            mAutoRun_RoundDone = false;
            mAutoRun_GoNextStage = false;
            while (!mAutoRun_RoundDone) {

                doStageLoop();

                if (mAutoRun_GoNextStage) {
                    mAutoRun_GoNextStage = false;
                    mStage = getNextStage(mStage);
                    setHandFocused(null);
                }
            }
        }

        public void dispatchIntentActionForAutoRun(String intentStr) {
            switch (intentStr) {
                case BjService.ROUND_STAGE_CONTINUE:
                    break;
                case BjService.ROUND_STAGE_FINISHED:
                    setAutoRun_GoNextStage();
                    break;
                case BjService.ROUND_PAYING_FINISHED:
                    setAutoRun_DoneRound();
                    break;

                default:    // other Intent will be ignored
                    break;
            }
        }

        //=== Jobs for each stage ===
        // Loop for treating hands
        public void doStageLoop() {
            switch (mStage) {
                case SETTING:
                case IDLE:
                case BETTING:
                    break;

                case INITIAL_DEAL:
                    onStage_InitialDeal();
                    break;

                case DEALING:
                    onStage_Dealing();
                    break;

                case PAYING:
                    onStage_Paying();
                    break;
            }
        }

        // Initial Deal
        private void onStage_InitialDeal() {
            Hand handFocused = getHandFocused();
            DealerHand dealerHand = getDealerHand();

            if (handFocused != null) {
                treatDealingOneCard();
                if (handFocused != dealerHand) {
                    if (handFocused.isBlackjack()) {
                        if (!isAutoRunning() && pGameRule.mUseSound) {
                            FragmentTable.sendUiMessage(FragmentTable.UI_MESSAGE.DEALING_HAND_SOUND,
                                    handFocused);
                            UtilityStudio.takeThreadPause(1000);
                        }
                        if (!dealerHand.canBeBlackjack()) {
                            pDealer.payHandBet((PlayerHand) handFocused, true); // cool, pay now
                        }
                    }
                }
            }

            if ((handFocused == null) && dealerHand.insuranceOffered()) {
                // peek-Hole allowed and 2 cards, the peek Hole

                if (!isAutoRunning()) {
                    mTable.redrawPlayerHands();
                    UtilityStudio.sleepMoment();
                    FragmentTable.sAnimation_Done = false;
                    FragmentTable.sendUiMessage(FragmentTable.UI_MESSAGE.CHECK_PEEK_HOLE);
                    while (!FragmentTable.sAnimation_Done) ;
                }

                int nDealerCards = dealerHand.getCountCard();
                int upScore = dealerHand.getUpScore();
                if (pGameRule.allowDealerHole && (nDealerCards == 2)) {
                    Card secondCard = dealerHand.getCardAt(1);
                    int secondScore = secondCard.getScore();

                    boolean isDealerBlackjack = ((upScore + secondScore) == 11);
                    if (isDealerBlackjack) {
                        FragmentTable.sendUiMessage(FragmentTable.UI_MESSAGE.RING_DEALER_BLACKJACK);
                        dealerHand.openSecondCard();
                        for (PlayerHand hand : mActingHands) {
                            hand.setDealDone();     // no more cards
                        }
                    } else {
                        if (!isAutoRunning()) {
                            FragmentTable.sendUiMessage
                                    (FragmentTable.UI_MESSAGE.RING_DEALER_NO_BLACKJACK);
                            if (pGameRule.mUseSound) {
                                UtilityStudio.takeThreadPause(1000);
                            }
                        }
                        for (PlayerHand hand : mActingHands) {
                            pDealer.takeHandInsured(hand);  // insurance failed
                        }
                    }
                }

                sendServiceIntent(BjService.ROUND_STAGE_FINISHED);

            } else {
                Hand nextHand = getNextHand_InitialDeal(handFocused);

                if (nextHand == null) {
                    int upScore = dealerHand.getUpScore();
                    if ((upScore == 1) && !dealerHand.insuranceOffered()) {
                        setHandFocused(null);

                        if (BjService.isAutoRunning()) {
                            takeBestInsuranceAcceptance();
                            assureInsuranceAcceptance();
                        } else {
                            FragmentTable.sendUiMessage(FragmentTable.UI_MESSAGE.OFFERING_INSURANCE);
                        }

                    } else if (pGameRule.allowDealerHole
                            && (dealerHand.getCountCard() == 2)
                            && (upScore == 10)) {

                        // peek-hole
                        if (!isAutoRunning()) {
                            FragmentTable.sAnimation_Done = false;
                            UtilityStudio.sleepMoment();
                            FragmentTable.sendUiMessage(FragmentTable.UI_MESSAGE.CHECK_PEEK_HOLE);
                            while (!FragmentTable.sAnimation_Done) ;
                        }

                        Card secondCard = dealerHand.getCardAt(1);
                        int secondScore = secondCard.getScore();

                        boolean isDealerBlackjack = ((upScore + secondScore) == 11);
                        if (isDealerBlackjack) {
                            dealerHand.openSecondCard();
                            for (PlayerHand hand : mActingHands) {
                                hand.setDealDone();     // no more cards
                            }
                            FragmentTable.sendUiMessage
                                    (FragmentTable.UI_MESSAGE.RING_DEALER_BLACKJACK);
                        } else {
                            FragmentTable.sendUiMessage
                                    (FragmentTable.UI_MESSAGE.RING_DEALER_NO_BLACKJACK);
                        }

                        sendServiceIntent(BjService.ROUND_STAGE_FINISHED);
                    } else {
                        sendServiceIntent(BjService.ROUND_STAGE_FINISHED);
                    }
                } else {
                    setHandFocused(nextHand);
                    sendServiceIntent(BjService.ROUND_STAGE_CONTINUE);
                }
            }
        }

        // Dealing
        private void onStage_Dealing() {
            Hand handFocused = getHandFocused();
            DealerHand dealerHand = getDealerHand();

            if (handFocused != null) {

                if (handFocused == dealerHand) {    // dealer hand
                    if (!dealerHand.initialCheckDone()) {
                        boolean needDealerDeal = false;
                        boolean dealerCanBeBlackjack = dealerHand.canBeBlackjack();

                        for (PlayerHand playerHand : mActingHands) {    // deal dealer only when needed
                            if (playerHand.stillAlive()
                                    && !(!dealerCanBeBlackjack && playerHand.isBlackjack())) {
                                needDealerDeal = true;
                                break;
                            }

                            // player has alive insurance, and dealer can be Blackjack with more cards
                            if (dealerCanBeBlackjack && playerHand.needDealCauseInsured()) {
                                needDealerDeal = true;
                                break;
                            }
                        }

                        if (!needDealerDeal) dealerHand.setDealDone();
                        dealerHand.setInitialCheckDone();
                    }

                    if (!dealerHand.hasDealDone()) {
                        if (dealerHand.hasHiddenSecondCard()) {
                            if (!isAutoRunning()) {
                                FragmentTable.sAnimation_Done = false;
                                FragmentTable.
                                        sendUiMessage(FragmentTable.UI_MESSAGE.DEALER_HIDDEN_OPEN);
                                while (!FragmentTable.sAnimation_Done) ;
                            }

                            dealerHand.openSecondCard();
                            if (!isAutoRunning()) {
                                mTable.redrawDealerHand();
                            }
                        } else if (dealerHand.getCountCard() == 1) {
                            treatDealingOneCard();

                        } else if (dealerHand.getCountCard() == 2) {
                            if (dealerHand.getCardScoreAt(0) == 1) {
                                // treat still-alive insurance
                                for (PlayerHand playerHand : mActingHands) {
                                    if (playerHand.needDealCauseInsured()) {
                                        if (dealerHand.isBlackjack()) {
                                            pDealer.payHandInsurance(playerHand);
                                        } else {
                                            pDealer.takeHandInsured(playerHand);
                                        }
                                    }
                                }
                            }

                            // check if still-alive betting
                            boolean needDealerDeal = false;
                            for (PlayerHand playerHand : mActingHands) {
                                if (playerHand.stillAlive()) {
                                    // TODO: check validity here, player blackjack
                                    if (playerHand.isBlackjack() && !dealerHand.canBeBlackjack()) {
                                        pDealer.payHandBet(playerHand, true);
                                    } else {
                                        needDealerDeal = true;
                                    }
                                    break;
                                }
                            }

                            if (!needDealerDeal) {
                                dealerHand.setDealDone();
                            } else {
                                int score = dealerHand.getScore();
                                if ((score < 17)
                                        || (dealerHand.isSoft() && (score == 17)
                                        && pGameRule.hitOnDealerSoft17)) {
                                    treatDealingOneCard();
                                } else {
                                    if (!isAutoRunning() && pGameRule.mUseSound) {
                                        FragmentTable.sendUiMessage(
                                                FragmentTable.UI_MESSAGE.DEALER_DONE_HAND_SOUND,
                                                dealerHand);
                                        UtilityStudio.takeThreadPause(1000);
                                    }
                                    dealerHand.setDealDone();
                                }

                                if (dealerHand.isSoft() && (score == 17)) {
                                    pDealer.recordSoft17();
                                }
                            }
                        } else {
                            int score = dealerHand.getScore();
                            if ((score < 17)
                                    || (dealerHand.isSoft() && (score == 17)
                                    && pGameRule.hitOnDealerSoft17)) {
                                treatDealingOneCard();

                            } else {
                                if (!isAutoRunning() && pGameRule.mUseSound) {
                                    FragmentTable.sendUiMessage(
                                            FragmentTable.UI_MESSAGE.DEALER_DONE_HAND_SOUND,
                                            dealerHand);
                                    UtilityStudio.takeThreadPause(1000);
                                }
                                dealerHand.setDealDone();
                            }
                        }
                    }

                } else {    // player hand
                    PlayerHand playerHand = (PlayerHand) handFocused;
                    if (playerHand.getCountCard() < 2) {    // could be just one card..(split)
                        treatDealingOneCard();

                        int index = mActingHands.indexOf(playerHand);
                        int nSize = mActingHands.size();
                        if ((index < (nSize - 1))       //first split hand
                                && (mActingHands.get(index + 1).getCountCard() == 1)) {

                            if (pGameRule.ruleSecondCardDeal == Rule.RuleSecondCardDeal.AT_ONCE) {
                                playerHand.setDealDone();   // temporarily no more card, and will be back
                            }
                        } else {                        // second split hand
                            if (pGameRule.ruleSecondCardDeal == Rule.RuleSecondCardDeal.AT_ONCE) {

                                PlayerHand firstSplitHand = mActingHands.get(index - 1);

                                firstSplitHand.forcedSetDealDone(false);    // revert back
                                firstSplitHand.updateValue();
                                setHandFocused(firstSplitHand);
                                handFocused = getHandFocused();

                                playerHand = firstSplitHand;                // change the role back
                                playerHand.evaluateStatus();                // check deal-done
                            }
                        }
                    } else {
                        switch (mPlayerDealAction) {
                            case UNKNOWN:
                                break;

                            case HIT:
                                treatDealingOneCard();
                                break;

                            case STAND:
                                playerHand.setDealDone();
                                break;

                            case SPLIT:
                                playerHand.getPlayer().addSplitCountAction();
                                PlayerHand splitHand = playerHand.split();
                                int index = getIndexByPlayerHand(playerHand);

                                if (isAutoRunning()) {
                                    mActingHands.add(index + 1, splitHand); // add to next
                                } else {
                                    Card card = splitHand.removeLastCard();
                                    mActingHands.add(index + 1, splitHand); // add to next
                                    mTable.redrawPlayerHands();
                                    mTable.notifySplitHand(splitHand, card);

                                    FragmentTable.sAnimation_Done = false;
                                    FragmentTable.sendUiMessage
                                            (FragmentTable.UI_MESSAGE.PLAYER_HAND_SPLIT);

                                    while (!FragmentTable.sAnimation_Done) ;
                                    splitHand.addCard(card);
                                    splitHand.evaluateStatus();
                                    mTable.redrawPlayerHands();
                                }
                                break;

                            case DOUBLEDOWN:
                                double betAmount = playerHand.getBetAmount();
                                playerHand.placeBetMore(betAmount);
                                playerHand.setDoubledown();
                                treatDealingOneCard();
                                if (!isAutoRunning() && pGameRule.mUseSound) {
                                    FragmentTable.sendUiMessage(
                                            FragmentTable.UI_MESSAGE.PLAYER_DONE_HAND_SOUND,
                                            handFocused);
                                    UtilityStudio.takeThreadPause(1000);
                                }
                                playerHand.setDealDone();
                                break;

                            case SURRENDER_OR_HIT:
                            case SURRENDER_OR_STAND:
                                pDealer.takeHandSurrenderBet(playerHand);
                                playerHand.setDealDone();
                                break;
                        }
                    }
                }
            }

            Hand nextHand = getNextHand_Dealing(handFocused);

            if (nextHand != null) {
                setHandFocused(nextHand);
                if ((nextHand != dealerHand) && (nextHand.getCountCard() == 1)) {
                    mPlayerDealAction = PlayerHand.Action.UNKNOWN;
                    sendServiceIntent(BjService.ROUND_STAGE_CONTINUE);

                } else {
                    if (nextHand == dealerHand) {
                        mPlayerDealAction = PlayerHand.Action.UNKNOWN;
                        sendServiceIntent(BjService.ROUND_STAGE_CONTINUE);

                    } else {
                        if (BjService.isAutoRunning()) {
                            onPlayerDealAction(((PlayerHand) nextHand).getBestPlayAction(dealerHand.getUpScore()));

                        } else {
                            FragmentTable.sendUiMessage(FragmentTable.UI_MESSAGE.WAITING_PLAYER_ACTION);
                        }
                    }
                }
            } else {
                setHandFocused(null);
                sendServiceIntent(BjService.ROUND_STAGE_FINISHED);
            }
        }

        // Paying: initially and finally null, paying each NON-PAID hand
        private void onStage_Paying() {
            Hand handFocused = getHandFocused();
            DealerHand dealerHand = getDealerHand();

            if (handFocused != null) {
                int dealerScore = dealerHand.getScore();
                PlayerHand playerHand = (PlayerHand) handFocused;
                int playerScore = playerHand.getScore();

                if (dealerHand.isBust()) {          // alive players are all win
                    if (!isAutoRunning() && pGameRule.mUseSound) {
                        FragmentTable.sendUiMessage(FragmentTable.UI_MESSAGE.DEALER_DONE_HAND_SOUND,
                                dealerHand);
                        UtilityStudio.takeThreadPause(1000);
                    }
                    pDealer.payHandBet(playerHand, playerHand.isBlackjack());
                } else {                                // compare scores
                    if (dealerScore > playerScore) {    // player LOST
                        pDealer.takeHandBet(playerHand);

                    } else if (dealerScore < playerScore) {     // player WIN
                        pDealer.payHandBet(playerHand, playerHand.isBlackjack());

                    } else {    // PUSH, but check Blackjack for both side
                        if (!playerHand.isBlackjack() && dealerHand.isBlackjack()) {
                            pDealer.takeHandBet(playerHand);

                        } else if (playerHand.isBlackjack() && !dealerHand.isBlackjack()) {
                            pDealer.payHandBet(playerHand, true);

                        } else {
                            pDealer.payHandBetPush(playerHand);
                        }
                    }
                }

                // player might have unpaid insurance
                if (playerHand.needDealCauseInsured()) {
                    if (dealerHand.isBlackjack()) {
                        pDealer.payHandInsurance(playerHand);
                    } else {
                        pDealer.takeHandInsured(playerHand);
                    }
                }
            }

            Hand nextHand = getNextHand_Paying(handFocused);

            if (nextHand != null) {
                setHandFocused(nextHand);
                sendServiceIntent(BjService.ROUND_STAGE_CONTINUE);
            } else {
                addRoundIndex();
                setHandFocused(null);

                updateRoundGameData();

                recordRoundResultByBox();   // ?? TODO: Check validity
                recordRoundResult();

                sendServiceIntent(BjService.ROUND_PAYING_FINISHED);
            }
        }

        //== finding next hand for each stage ===
        // Initial-Deal: initially null, two cards for all player hands, and 1/2 for dealer
        Hand getNextHand_InitialDeal(Hand handFocused) {
            Hand nextHand = null;
            DealerHand dealerHand = getDealerHand();

            if ((handFocused == null) || (handFocused == dealerHand)) {
                nextHand = mActingHands.get(0);

            } else {
                PlayerHand playerHand = (PlayerHand) handFocused;

                int nSize = mActingHands.size();
                int index = mActingHands.indexOf(playerHand);
                if (index < (nSize - 1)) {
                    nextHand = mActingHands.get(index + 1);
                } else {
                    nextHand = dealerHand;
                }

                if ((nextHand == dealerHand) && (handFocused.getCountCard() == 2)) {
                    if (pGameRule.ruleSecondCardDeal == Rule.RuleSecondCardDeal.LATER) {
                        nextHand = null;
                    }
                }
            }

            // initial deal done
            if ((nextHand != null)
                    && (nextHand == mActingHands.get(0))
                    && (nextHand.getCountCard() == 2)) {
                nextHand = null;
            }

            return nextHand;
        }

        // Dealing: initially and finally dealerHand, and only NON-DoneDeal hand
        Hand getNextHand_Dealing(Hand handFocused) {
            Hand nextHand = null;
            DealerHand dealerHand = getDealerHand();

            if (handFocused == dealerHand) {
                nextHand = handFocused.hasDealDone() ? null : handFocused;

            } else if ((handFocused != null) && (!handFocused.hasDealDone())) {
                nextHand = handFocused;     // keep dealing with same hand

            } else {
                PlayerHand playerHand = (PlayerHand) handFocused;
                int nSize = mActingHands.size();
                int index = (handFocused == null) ? -1 : mActingHands.indexOf(playerHand);
                for (int i = index + 1; i < nSize; i++) {
                    PlayerHand tempHand = mActingHands.get(i);
                    if (!tempHand.hasDealDone()) {
                        nextHand = tempHand;
                        break;
                    }
                }

                // no more player hand to deal, then dealer hand will be the next
                if (nextHand == null) {
                    nextHand = dealerHand;
                }
            }

            return nextHand;
        }

        // Paying: initially and finally null, and only NON-PAID hand
        Hand getNextHand_Paying(Hand handFocused) {
            PlayerHand playerHand = (PlayerHand) handFocused;
            PlayerHand nextHand = null;

            int nSize = mActingHands.size();
            int index = (handFocused == null) ? -1 : mActingHands.indexOf(playerHand);

            for (int i = index + 1; i < nSize; i++) {
                PlayerHand tempHand = mActingHands.get(i);
                if (tempHand.stillAlive()) {
                    nextHand = tempHand;
                    break;
                }
            }

            return nextHand;
        }

        // for simulator
        private ArrayList<Player> mSimulationPlayers;

        public void setSimulationPlayers(ArrayList<Player> simulationPlayers) {
            mSimulationPlayers = simulationPlayers;
        }

        public ArrayList<Player> getSimulationPlayers() {
            return mSimulationPlayers;
        }

        public void readyActingHandsFromSimulator() {
            mActingHands.clear();

            int boxId = 0;
            for (Player player : mSimulationPlayers) {
                BettingBox bettingBox = getBoxByIndex(boxId++);
                bettingBox.setSittingPlayer(player);
                double betAmount = bettingBox.getNextBet();

                if (betAmount != 0.0) {
                    player.takeOutBalance(betAmount);
                    PlayerHand hand = new PlayerHand(BjService.this, pDealer, bettingBox,
                            player, betAmount, 0);
                    mActingHands.add(hand);
                    hand.confirmBet();
                }
            }
        }

        // record round result
        private void recordRoundResultByBox() {
            int nHands = mActingHands.size();

            // win-push-lost streak will be judged by sum of winning amount, by box
            int index = 0;
            while (index < nHands) {
                PlayerHand hand = mActingHands.get(index);
                BettingBox bettingBox = hand.getBox();
                double winAmount = hand.getWinAmount();
                int next = index + 1;
                while (next < nHands) {
                    PlayerHand nextHand = mActingHands.get(next);
                    BettingBox nextBettingBox = nextHand.getBox();
                    if (bettingBox == nextBettingBox) {
                        winAmount += nextHand.getWinAmount();
                        next++;
                    } else break;
                }
                index = next;
                bettingBox.setLastRoundResultByWinAmount(winAmount);
            }
        }

        private void recordRoundResult() {
            int nHands = mActingHands.size();

            // win-push-lost streak will be judged by sum of winning amount, by player
            int index = 0;
            while (index < nHands) {
                PlayerHand hand = mActingHands.get(index);
                Player player = hand.getPlayer();
                double winAmount = hand.getWinAmount();
                int next = index + 1;
                while (next < nHands) {
                    PlayerHand nextHand = mActingHands.get(next);
                    Player nextPlayer = nextHand.getPlayer();
                    if (player == nextPlayer) {
                        winAmount += nextHand.getWinAmount();
                        next++;
                    } else break;
                }
                index = next;
                player.defineStreak(winAmount);
            }

            for (PlayerHand hand : mActingHands) {
                Player player = hand.getPlayer();
                player.recordData(hand);
            }

            pDealer.recordData();
        }

        // handling candlestick data recording
        public void setCandlestickShadow() {
            for (Player player : mSimulationPlayers) {
                player.mRecord.setCandleStickShadow();
            }
        }

        public void closeCandlestick() {
            for (Player player : mSimulationPlayers) {
                player.mRecord.closeCandleStick();
            }

        }

        public void openCandlestick() {
            for (Player player : mSimulationPlayers) {
                player.mRecord.openCandlestick();
            }
        }
    }
}
