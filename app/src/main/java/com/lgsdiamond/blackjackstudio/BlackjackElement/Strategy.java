package com.lgsdiamond.blackjackstudio.BlackjackElement;

import android.graphics.Color;

import java.util.ArrayList;

/**
 * Created by lgsdi on 2015-12-26.
 */
public class Strategy implements Cloneable {
    @Override
    public Object clone() throws CloneNotSupportedException {
        Strategy clone = (Strategy) super.clone();

        clone.setEditable(true);

        clone.mHardActions = new SingleActions[COUNT_HARD_ACTIONS];
        for (int index = 0; index < COUNT_HARD_ACTIONS; index++) {
            clone.mHardActions[index] = new SingleActions();
            for (int upIndex = 0; upIndex < COUNT_UP_SCORES; upIndex++) {
                (clone.mHardActions[index]).mActions[upIndex] =
                        mHardActions[index].mActions[upIndex];
            }
        }

        clone.mSoftActions = new SingleActions[COUNT_SOFT_ACTIONS];
        for (int index = 0; index < COUNT_SOFT_ACTIONS; index++) {
            clone.mSoftActions[index] = new SingleActions();
            for (int upIndex = 0; upIndex < COUNT_UP_SCORES; upIndex++) {
                (clone.mSoftActions[index]).mActions[upIndex] =
                        mSoftActions[index].mActions[upIndex];
            }
        }

        clone.mPairActions = new SingleActions[COUNT_PAIR_ACTIONS];
        for (int index = 0; index < COUNT_PAIR_ACTIONS; index++) {
            clone.mPairActions[index] = new SingleActions();
            for (int upIndex = 0; upIndex < COUNT_UP_SCORES; upIndex++) {
                (clone.mPairActions[index]).mActions[upIndex] =
                        mPairActions[index].mActions[upIndex];
            }
        }

        clone.mTitle = "New-" + clone.mTitle;

        return clone;
    }

    String mUniqueId;
    boolean mEditable = false;  // default: not editable

    public boolean isEditable() {
        return mEditable;
    }

    public void setEditable(Boolean editable) {
        mEditable = editable;
    }

    public static final int COUNT_UP_SCORES = 10;       // 2,3,4, ..., 10, A

    public static final int COUNT_HARD_ACTIONS = 12;    // 7-, 8, 9, ..., 16, 17, 18+
    public static final int COUNT_SOFT_ACTIONS = 8;     // A2, A3, A4, A5, A6, A7, A8, A9
    public static final int COUNT_PAIR_ACTIONS = 10;    // (2,2), (3,3), ..., (9,9), (10,10), (A,A)

    public static int getColorByAction(PlayerHand.Action action) {
        int color;
        switch (action) {
            default:
            case UNKNOWN:
                color = Color.rgb(255, 255, 255);
                break;
            case HIT:
                color = Color.rgb(240, 200, 200);
                break;
            case STAND:
                color = Color.rgb(240, 240, 200);
                break;
            case SPLIT:
                color = Color.rgb(190, 240, 190);
                break;
            case DOUBLEDOWN:
                color = Color.rgb(180, 100, 100);
                break;
            case SURRENDER_OR_HIT:
                color = Color.rgb(210, 180, 180);
                break;
            case SURRENDER_OR_STAND:
                color = Color.rgb(210, 210, 180);
                break;
        }
        return color;
    }

    public static ArrayList<Strategy> makePredefinedStrategies() {
        ArrayList<Strategy> strategies = new ArrayList<>();
        strategies.add(newVegasStripStrategy());
        strategies.add(newAtlanticCityStrategy());
        strategies.add(newClassicGoldStrategy());
        strategies.add(newTypicalNoviceStrategy());

        return strategies;
    }

    public static final String
            mCode_UNKNOWN = "?", mCode_HIT = "H", mCode_STAND = "S",
            mCode_SPLIT = "P", mCode_DOUBLEDOWN = "D",
            mCode_SURRENDER_OR_HIT = "Rh", mCode_SURRENDER_OR_STAND = "Rs";

    public static PlayerHand.Action getActionByCode(String code) {
        PlayerHand.Action strategyAction;
        switch (code) {
            default:
            case mCode_UNKNOWN:
                strategyAction = PlayerHand.Action.UNKNOWN;
                break;
            case mCode_HIT:
                strategyAction = PlayerHand.Action.HIT;
                break;
            case mCode_STAND:
                strategyAction = PlayerHand.Action.STAND;
                break;
            case mCode_SPLIT:
                strategyAction = PlayerHand.Action.SPLIT;
                break;
            case mCode_DOUBLEDOWN:
                strategyAction = PlayerHand.Action.DOUBLEDOWN;
                break;
            case mCode_SURRENDER_OR_HIT:
                strategyAction = PlayerHand.Action.SURRENDER_OR_HIT;
                break;
            case mCode_SURRENDER_OR_STAND:
                strategyAction = PlayerHand.Action.SURRENDER_OR_STAND;
                break;
        }
        return strategyAction;
    }

    public static String getCodeByAction(PlayerHand.Action strategyAction) {
        String code;
        switch (strategyAction) {
            default:
            case UNKNOWN:
                code = mCode_UNKNOWN;
                break;
            case HIT:
                code = mCode_HIT;
                break;
            case STAND:
                code = mCode_STAND;
                break;
            case SPLIT:
                code = mCode_SPLIT;
                break;
            case DOUBLEDOWN:
                code = mCode_DOUBLEDOWN;
                break;
            case SURRENDER_OR_HIT:
                code = mCode_SURRENDER_OR_HIT;
                break;
            case SURRENDER_OR_STAND:
                code = mCode_SURRENDER_OR_STAND;
                break;
        }
        return code;
    }

    public String getUniqueId() {
        return mUniqueId;
    }

    public boolean getBestInsuranceAction() {
        return false;   // never take insurance
    }

    public class SingleActions {
        public PlayerHand.Action[] mActions = new PlayerHand.Action[COUNT_UP_SCORES];
    }

    public SingleActions[] mHardActions;   // total 12
    public static String[] sHardTitles = new String[]{"Hard 7-", "Hard 8", "Hard 9", "Hard 10",
            "Hard 11", "Hard 12", "Hard 13", "Hard 14", "Hard 15", "Hard 16", "Hard 17", "Hard 18+"};

    public SingleActions[] mSoftActions;   // total 8
    public static String[] sSoftTitles = new String[]{"Soft 13", "Soft 14", "Soft 15", "Soft 16",
            "Soft 17", "Soft 18", "Soft 19", "Soft 20"};

    public SingleActions[] mPairActions;   // total 10
    public static String[] sPairTitles = new String[]{"2-2", "3-3", "4-4", "5-5", "6-6",
            "7-7", "8-8", "9-9", "10-10", "A-A",};

    private String mTitle;
    private String mDescription;

    // basic constructor
    public Strategy(String title, String description, String uniqueId) {

        mTitle = title;
        mDescription = description;
        mUniqueId = uniqueId;

        mHardActions = new SingleActions[COUNT_HARD_ACTIONS];
        for (int index = 0; index < mHardActions.length; index++) {
            mHardActions[index] = new SingleActions();
        }

        mSoftActions = new SingleActions[COUNT_SOFT_ACTIONS];
        for (int index = 0; index < mSoftActions.length; index++) {
            mSoftActions[index] = new SingleActions();
        }

        mPairActions = new SingleActions[COUNT_PAIR_ACTIONS];
        for (int index = 0; index < mPairActions.length; index++) {
            mPairActions[index] = new SingleActions();
        }
    }

    // getter
    public String getTitle() {
        return mTitle;
    }

    public String getDescription() {
        return mDescription;
    }

    public PlayerHand.Action getBestAction(PlayerHand hand, int dealerUpScore) {
        int dealerUpIndex = ((dealerUpScore == 1) ? 9 : dealerUpScore - 2);
        int playerIndex;

        SingleActions[] goodActions;

        if (hand.canSplit()) {  // must be pair
            goodActions = mPairActions;
            int score = hand.getCardAt(0).getScore();
            if (score == 1) {   // A-A
                playerIndex = 9;
            } else {
                playerIndex = score - 2;
            }
        } else if (hand.isSoft()) {
            goodActions = mSoftActions;
            int score = hand.getScore();    // A-A handled already, so no score-12 here
            playerIndex = score - 13;       // A2->0, A3->1, ..., A9->7(20-13)
        } else {
            goodActions = mHardActions;     // default
            int score = hand.getScore();
            if (score < 8) {
                playerIndex = 0;
            } else if (score < 18) {
                playerIndex = score - 7;
            } else {
                playerIndex = 11;
            }
        }

        return (goodActions[playerIndex]).mActions[dealerUpIndex];
    }

    static Strategy createByActionCodeArray(String title, String description, String uniqueId,
                                            String[][] hardActionCodes, String[][] softActionCodes,
                                            String[][] pairActionCodes) {

        Strategy strategy = new Strategy(title, description, uniqueId);

        int countSize = hardActionCodes.length;
        int countActions = hardActionCodes[0].length;

        if ((countSize != COUNT_HARD_ACTIONS) || (countActions != COUNT_UP_SCORES))
            return null;

        countSize = softActionCodes.length;
        countActions = softActionCodes[0].length;
        if ((countSize != COUNT_SOFT_ACTIONS) || (countActions != COUNT_UP_SCORES))
            return null;

        countSize = pairActionCodes.length;
        countActions = pairActionCodes[0].length;
        if ((countSize != COUNT_PAIR_ACTIONS) || (countActions != COUNT_UP_SCORES))
            return null;

        int index = 0;
        for (String[] actionCodes : hardActionCodes) {
            int upScoreIndex = 0;
            for (String code : actionCodes) {
                PlayerHand.Action action = getActionByCode(code);
                if (action == PlayerHand.Action.UNKNOWN) return null;

                strategy.mHardActions[index].mActions[upScoreIndex] = action;
                upScoreIndex++;
            }
            index++;
        }

        index = 0;
        for (String[] actionCodes : softActionCodes) {
            int upScoreIndex = 0;
            for (String code : actionCodes) {
                PlayerHand.Action action = getActionByCode(code);
                if (action == PlayerHand.Action.UNKNOWN) return null;

                strategy.mSoftActions[index].mActions[upScoreIndex] = action;
                upScoreIndex++;
            }
            index++;
        }

        index = 0;
        for (String[] actionCodes : pairActionCodes) {
            int upScoreIndex = 0;
            for (String code : actionCodes) {
                PlayerHand.Action action = getActionByCode(code);
                if (action == PlayerHand.Action.UNKNOWN) return null;

                strategy.mPairActions[index].mActions[upScoreIndex] = action;
                upScoreIndex++;
            }
            index++;
        }

        return strategy;
    }

    // strategy known

    /**
     * CLASSIC BLACKJACK GOLD STRATEGY CHART - Microgaming Software - 0.13% House Edge
     * (Same rules apply to SINGLE DECK BLACKJACK - Amaya software)
     * (Single deck, no peek, Dealer stand on soft 17, Multiple cards to split Aces, Double on hard 9,10,11 only)
     **/

    public static Strategy newClassicGoldStrategy() {
        // hard table = 10 by 12
        String[][] hardActionCodes = {                                          // [10][10]
                {"H", "H", "H", "H", "H", "H", "H", "H", "H", "H"},             // 7-
                {"H", "H", "H", "H", "H", "H", "H", "H", "H", "H"},             // 8
                {"D", "D", "D", "D", "D", "H", "H", "H", "H", "H"},             // 9
                {"D", "D", "D", "D", "D", "D", "D", "D", "H", "H"},             // 10
                {"D", "D", "D", "D", "D", "D", "D", "D", "H", "H"},             // 11
                {"H", "H", "S", "S", "S", "H", "H", "H", "H", "H"},             // 12
                {"S", "S", "S", "S", "S", "H", "H", "H", "H", "H"},             // 13
                {"S", "S", "S", "S", "S", "H", "H", "H", "H", "H"},             // 14
                {"S", "S", "S", "S", "S", "H", "H", "H", "H", "H"},             // 15
                {"S", "S", "S", "S", "S", "H", "H", "H", "H", "H"},             // 16
                {"S", "S", "S", "S", "S", "S", "S", "S", "S", "S"},             // 17
                {"S", "S", "S", "S", "S", "S", "S", "S", "S", "S"}};            // 18+

        // soft table = 10 by 8
        String[][] softActionCodes = {                                          // [10][7]
                // hard table = 8 by 10
                {"H", "H", "H", "H", "H", "H", "H", "H", "H", "H"},             // A2
                {"H", "H", "H", "H", "H", "H", "H", "H", "H", "H"},             // A3
                {"H", "H", "H", "H", "H", "H", "H", "H", "H", "H"},             // A4
                {"H", "H", "H", "H", "H", "H", "H", "H", "H", "H"},             // A5
                {"H", "H", "H", "H", "H", "H", "H", "H", "H", "H"},             // A6
                {"S", "S", "S", "S", "S", "S", "S", "H", "H", "S"},             // A7
                {"S", "S", "S", "S", "S", "S", "S", "S", "S", "S"},             // A8
                {"S", "S", "S", "S", "S", "S", "S", "S", "S", "S"}};            // A9

        // pair table = 10 by 8
        String[][] pairActionCodes = {                                          // [10][8]
                {"H", "P", "P", "P", "P", "P", "H", "H", "H", "H"},             // 2,2
                {"H", "H", "P", "P", "P", "P", "H", "H", "H", "H"},             // 3,3
                {"H", "H", "H", "P", "P", "H", "H", "H", "H", "H"},             // 4,4
                {"D", "D", "D", "D", "D", "D", "D", "D", "H", "H"},             // 5,5 = hard 10
                {"P", "P", "P", "P", "P", "H", "H", "H", "H", "H"},             // 6,6
                {"P", "P", "P", "P", "P", "P", "H", "H", "S", "H"},             // 7,7
                {"P", "P", "P", "P", "P", "P", "P", "P", "S", "S"},             // 8,8
                {"P", "P", "P", "P", "P", "S", "P", "P", "S", "S"},             // 9,9
                {"S", "S", "S", "S", "S", "S", "S", "S", "S", "S"},             // 10,10
                {"P", "P", "P", "P", "P", "P", "P", "P", "P", "P"}};            // A,A


        Strategy strategy = createByActionCodeArray("Classic Gold",
                "Single deck, no peek, Dealer stand on soft 17, Multiple cards to split Aces, Double on hard 9,10,11 only",
                "Classic",
                hardActionCodes, softActionCodes, pairActionCodes);

        return strategy;
    }

    /**
     * VEGAS STRIP BLACKJACK STRATEGY CHART - Microgaming Software - 0.36% house edge
     * (4 decks, Dealer stands on soft 17, Hole Card - dealer peeks)
     **/

    public static Strategy newVegasStripStrategy() {
        // hard table = 10 by 12
        String[][] hardActionCodes = {                                          // [10][10]
                {"H", "H", "H", "H", "H", "H", "H", "H", "H", "H"},             // 7-
                {"H", "H", "H", "H", "H", "H", "H", "H", "H", "H"},             // 8
                {"H", "D", "D", "D", "D", "H", "H", "H", "H", "H"},             // 9
                {"D", "D", "D", "D", "D", "D", "D", "D", "H", "H"},             // 10
                {"D", "D", "D", "D", "D", "D", "D", "D", "D", "H"},             // 11
                {"H", "H", "S", "S", "S", "H", "H", "H", "H", "H"},             // 12
                {"S", "S", "S", "S", "S", "H", "H", "H", "H", "H"},             // 13
                {"S", "S", "S", "S", "S", "H", "H", "H", "H", "H"},             // 14
                {"S", "S", "S", "S", "S", "H", "H", "H", "H", "H"},             // 15
                {"S", "S", "S", "S", "S", "H", "H", "H", "H", "H"},             // 16
                {"S", "S", "S", "S", "S", "S", "S", "S", "S", "S"},             // 17
                {"S", "S", "S", "S", "S", "S", "S", "S", "S", "S"}};            // 18+

        // soft table = 10 by 8
        String[][] softActionCodes = {                                          // [10][8]
                {"H", "H", "H", "D", "D", "H", "H", "H", "H", "H"},             // A2
                {"H", "H", "H", "D", "D", "H", "H", "H", "H", "H"},             // A3
                {"H", "H", "D", "D", "D", "H", "H", "H", "H", "H"},             // A4
                {"H", "H", "D", "D", "D", "H", "H", "H", "H", "H"},             // A5
                {"H", "D", "D", "D", "D", "H", "H", "H", "H", "H"},             // A6
                {"S", "D", "D", "D", "D", "S", "S", "H", "H", "H"},             // A7
                {"S", "S", "S", "S", "S", "S", "S", "S", "S", "S"},             // A8
                {"S", "S", "S", "S", "S", "S", "S", "S", "S", "S"}};            // A9

        // pair table = 10 by 10
        String[][] pairActionCodes = {                                          // [10][8]
                {"P", "P", "P", "P", "P", "P", "H", "H", "H", "H"},             // 2,2
                {"P", "P", "P", "P", "P", "P", "H", "H", "H", "H"},             // 3,3
                {"H", "H", "H", "P", "P", "H", "H", "H", "H", "H"},             // 4,4
                {"D", "D", "D", "D", "D", "D", "D", "D", "H", "H"},             // 5,5 = hard 10
                {"P", "P", "P", "P", "P", "H", "H", "H", "H", "H"},             // 6,6
                {"P", "P", "P", "P", "P", "P", "H", "H", "H", "H"},             // 7,7
                {"P", "P", "P", "P", "P", "P", "P", "P", "P", "P"},             // 8,8
                {"P", "P", "P", "P", "P", "S", "P", "P", "S", "S"},             // 9,9
                {"S", "S", "S", "S", "S", "S", "S", "S", "S", "S"},             // 10,10
                {"P", "P", "P", "P", "P", "P", "P", "P", "P", "P"}};            // A,A


        Strategy strategy = createByActionCodeArray("Vegas Strip",
                "4 decks, Dealer stands on soft 17, Hole Card - dealer peeks", "Vegas",
                hardActionCodes, softActionCodes, pairActionCodes);

        return strategy;
    }

    /**
     * ATLANTIC CITY BLACKJACK STRATEGY CHART - Microgaming Software - 0.36% house edge
     * (8 decks, Surrender option, Hole Card, Dealer must stand on Soft 17)
     **/

    public static Strategy newAtlanticCityStrategy() {
        // hard table = 10 by 12
        String[][] hardActionCodes = {                                          // [10][10]
                {"H", "H", "H", "H", "H", "H", "H", "H", "H", "H"},             // 7-
                {"H", "H", "H", "H", "H", "H", "H", "H", "H", "H"},             // 8
                {"H", "D", "D", "D", "D", "H", "H", "H", "H", "H"},             // 9
                {"D", "D", "D", "D", "D", "D", "D", "D", "H", "H"},             // 10
                {"D", "D", "D", "D", "D", "D", "D", "D", "D", "H"},             // 11
                {"H", "H", "S", "S", "S", "H", "H", "H", "H", "H"},             // 12
                {"S", "S", "S", "S", "S", "H", "H", "H", "H", "H"},             // 13
                {"S", "S", "S", "S", "S", "H", "H", "H", "H", "H"},             // 14
                {"S", "S", "S", "S", "S", "H", "H", "H", "Rh", "H"},            // 15
                {"S", "S", "S", "S", "S", "H", "H", "Rh", "Rh", "Rh"},          // 16
                {"S", "S", "S", "S", "S", "S", "S", "S", "S", "S"},             // 17
                {"S", "S", "S", "S", "S", "S", "S", "S", "S", "S"}};            // 18+

        // soft table = 10 by 8
        String[][] softActionCodes = {                                          // [10][7]
                // hard table = 8 by 10
                {"H", "H", "H", "D", "D", "H", "H", "H", "H", "H"},             // A2
                {"H", "H", "H", "D", "D", "H", "H", "H", "H", "H"},             // A3
                {"H", "H", "D", "D", "D", "H", "H", "H", "H", "H"},             // A4
                {"H", "H", "D", "D", "D", "H", "H", "H", "H", "H"},             // A5
                {"H", "D", "D", "D", "D", "H", "H", "H", "H", "H"},             // A6
                {"S", "D", "D", "D", "D", "S", "S", "H", "H", "H"},             // A7
                {"S", "S", "S", "S", "S", "S", "S", "S", "S", "S"},             // A8
                {"S", "S", "S", "S", "S", "S", "S", "S", "S", "S"}};            // A9

        // pair table = 10 by 10
        String[][] pairActionCodes = {                                          // [10][8]
                {"P", "P", "P", "P", "P", "P", "H", "H", "H", "H"},             // 2,2
                {"P", "P", "P", "P", "P", "P", "H", "H", "H", "H"},             // 3,3
                {"H", "H", "H", "P", "P", "H", "H", "H", "H", "H"},             // 4,4
                {"D", "D", "D", "D", "D", "D", "D", "D", "H", "H"},             // 5,5 = hard 10
                {"P", "P", "P", "P", "P", "H", "H", "H", "H", "H"},             // 6,6
                {"P", "P", "P", "P", "P", "P", "H", "H", "H", "H"},             // 7,7
                {"P", "P", "P", "P", "P", "P", "P", "P", "P", "P"},             // 8,8
                {"P", "P", "P", "P", "P", "S", "P", "P", "S", "S"},             // 9,9
                {"S", "S", "S", "S", "S", "S", "S", "S", "S", "S"},             // 10,10
                {"P", "P", "P", "P", "P", "P", "P", "P", "P", "P"}};            // A,A

        Strategy strategy = createByActionCodeArray("Atlantic City",
                "8 decks, Surrender option, Hole Card, Dealer must stand on Soft 17", "Atlantic",
                hardActionCodes, softActionCodes, pairActionCodes);

        return strategy;
    }

    /**
     * TYPICAL NOVICE
     * (no hit on Hard 15-16, do doubledown on soft hand, stand on soft 17-18, no doubledown on high up-card)
     **/

    public static Strategy newTypicalNoviceStrategy() {
        // hard table = 10 by 12
        String[][] hardActionCodes = {                                          // [10][10]
                {"H", "H", "H", "H", "H", "H", "H", "H", "H", "H"},             // 7-
                {"H", "H", "H", "H", "H", "H", "H", "H", "H", "H"},             // 8
                {"H", "D", "D", "D", "D", "H", "H", "H", "H", "H"},             // 9
                {"D", "D", "D", "D", "D", "H", "H", "H", "H", "H"},             // 10
                {"D", "D", "D", "D", "D", "H", "H", "H", "H", "H"},             // 11
                {"H", "H", "S", "S", "S", "H", "H", "H", "H", "H"},             // 12
                {"S", "S", "S", "S", "S", "H", "H", "H", "H", "H"},             // 13
                {"S", "S", "S", "S", "S", "H", "H", "H", "H", "H"},             // 14
                {"S", "S", "S", "S", "S", "S", "S", "S", "S", "S"},             // 15
                {"S", "S", "S", "S", "S", "S", "S", "S", "S", "S"},             // 16
                {"S", "S", "S", "S", "S", "S", "S", "S", "S", "S"},             // 17
                {"S", "S", "S", "S", "S", "S", "S", "S", "S", "S"}};            // 18+

        // soft table = 10 by 8
        String[][] softActionCodes = {                                          // [10][7]
                // hard table = 8 by 10
                {"H", "H", "H", "H", "H", "H", "H", "H", "H", "H"},             // A2
                {"H", "H", "H", "H", "H", "H", "H", "H", "H", "H"},             // A3
                {"H", "H", "H", "H", "H", "H", "H", "H", "H", "H"},             // A4
                {"H", "H", "H", "H", "H", "H", "H", "H", "H", "H"},             // A5
                {"S", "S", "S", "S", "S", "S", "S", "S", "S", "S"},             // A6
                {"S", "S", "S", "S", "S", "S", "S", "S", "S", "S"},             // A7
                {"S", "S", "S", "S", "S", "S", "S", "S", "S", "S"},             // A8
                {"S", "S", "S", "S", "S", "S", "S", "S", "S", "S"}};            // A9

        // pair table = 10 by 10
        String[][] pairActionCodes = {                                          // [10][8]
                {"P", "P", "P", "P", "P", "P", "H", "H", "H", "H"},             // 2,2
                {"P", "P", "P", "P", "P", "P", "H", "H", "H", "H"},             // 3,3
                {"H", "H", "H", "P", "P", "H", "H", "H", "H", "H"},             // 4,4
                {"D", "D", "D", "D", "D", "H", "H", "H", "H", "H"},             // 5,5 = hard 10
                {"P", "P", "P", "P", "P", "H", "H", "H", "H", "H"},             // 6,6
                {"P", "P", "P", "P", "P", "P", "H", "H", "H", "H"},             // 7,7
                {"P", "P", "P", "P", "P", "P", "P", "P", "P", "P"},             // 8,8
                {"P", "P", "P", "P", "P", "S", "P", "P", "S", "S"},             // 9,9
                {"S", "S", "S", "S", "S", "S", "S", "S", "S", "S"},             // 10,10
                {"P", "P", "P", "P", "P", "P", "P", "P", "P", "P"}};            // A,A

        Strategy strategy = createByActionCodeArray("Typical Novice",
                "no hit on Hard 15-16, do doubledown on soft hand, stand on soft 17-18, no doubledown on high up-card",
                "Novice",
                hardActionCodes, softActionCodes, pairActionCodes);

        return strategy;
    }
}