package com.lgsdiamond.blackjackstudio.BlackjackElement;

import com.lgsdiamond.blackjackstudio.ActivityStudio;

import java.util.ArrayList;

/**
 * Created by lgsdiamond on 2015-10-02.
 */
public class Rule implements Cloneable {
    private final double DEFAULT_BASEBET = 100.0;
    private final double DEFAULT_MAXBET = 1000.0;
    private final double DEFAULT_MINBET = 1.0;
    private final double DEFAULT_BANKROLL = 10000.0;

    private final int DEFAULT_COUNT_DECK = 6;
    private final int DEFAULT_COUNT_BOX = 4;

    // static members
    public static Rule sRuleRegular;
    public static Rule sRuleSpecial;
    public static final int MAX_COUNT_SIMULATION_HANDS = 5;
    public double mBaseBet;
    public boolean allowNegativeBankroll;

    public enum ProgressUpdateCondition {JUST_FINAL, BANKROLL, ALL}

    public ProgressUpdateCondition showProgressUpdateCondition;

    // public members
    public enum RuleDoubledown {
        ANY_TWO_CARDS, ONLY_NINE_TEN_ELEVEN, ONLY_TEN_ELEVEN
    }

    public enum RuleSecondCardDeal {AT_ONCE, LATER}


    // dealer settings
    public RuleDoubledown ruleDoubledown;
    public RuleSecondCardDeal ruleSecondCardDeal;

    public boolean allowDealerHole;

    public int maxSplitCount;
    public boolean allowAceResplit;

    // doubledown settings
    public boolean allowDoubledownAfterSplit;

    // split settings
    public boolean allowSplitDifferentTenValues;
    public boolean allowSurrender;
    public boolean hitOnDealerSoft17;

    // table settings

    public double blackjackPayout;

    public int mCountBoxes;
    public int mCountDecks;

    public double maxBet;
    public double minBet;
    public double playerBankroll;

    public boolean useRandomShoe;

    public double mBiasedShoe = 0.0;

    public boolean mUseSound = true;
    public boolean mUseAnimation = true;

    // simulation settings
    public boolean mHasBettingLimit = false;
    public int mCountSimulationRuns = 10000;
    public int[] mHandStrategyIndex = new int[MAX_COUNT_SIMULATION_HANDS];
    public int[] mHandBetterIndex = new int[MAX_COUNT_SIMULATION_HANDS];

    public Rule(RuleDoubledown ruleDoubledown, RuleSecondCardDeal ruleSecondCardDeal, boolean basic) {
        setRuleBasic();
        if (!basic) setRuleSpecial();

        this.ruleDoubledown = ruleDoubledown;
        this.ruleSecondCardDeal = ruleSecondCardDeal;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public static void initialize() {
        sRuleRegular = new Rule(RuleDoubledown.ANY_TWO_CARDS,
                RuleSecondCardDeal.AT_ONCE, true);
        sRuleSpecial = new Rule(RuleDoubledown.ONLY_NINE_TEN_ELEVEN,
                RuleSecondCardDeal.LATER, false);
    }

    @Override
    public String toString() {
        String ruleStr = (hitOnDealerSoft17 ? "H17" : "S17");
        ruleStr += "-" + (ruleSecondCardDeal == RuleSecondCardDeal.AT_ONCE ? "AtOnce" : "Later");
        ruleStr += "-" + (allowDoubledownAfterSplit ? "DAS" : "nDAS");
        ruleStr += "-" + (allowSurrender ? "SrA" : "SrN");
        ruleStr += "-MaxS(" + maxSplitCount + ")";
        ruleStr += "-Dd(" + (ruleDoubledown == RuleDoubledown.ANY_TWO_CARDS ? "Any2)" :
                (ruleDoubledown == RuleDoubledown.ONLY_TEN_ELEVEN ? "10/11" : "9/10/11)"));

        return ruleStr;
    }

    private void setRuleBasic() {
        // basic rules
        allowDoubledownAfterSplit = true;
        hitOnDealerSoft17 = false;
        allowSplitDifferentTenValues = true;
        allowSurrender = false;

        allowDealerHole = true;

        maxSplitCount = 3;  // 3 splits, maximum 4 hands
        allowAceResplit = false;  // only one card for Ace-split hand

        blackjackPayout = 1.5; // 150% of bet

        // table rules
        mCountBoxes = DEFAULT_COUNT_BOX;    // number of boxes
        mCountDecks = DEFAULT_COUNT_DECK;    // number of boxes

        allowNegativeBankroll = false;

        // betting limits
        mBaseBet = DEFAULT_BASEBET;
        maxBet = DEFAULT_MAXBET;
        minBet = DEFAULT_MINBET;
        playerBankroll = DEFAULT_BANKROLL;

        // for shoe card generation
        useRandomShoe = true;

        // for simulation setting
        showProgressUpdateCondition = ProgressUpdateCondition.ALL;

        ArrayList<Strategy> strategies = ActivityStudio.sStudio.getStrategies();
        ArrayList<Better> bettings = ActivityStudio.sStudio.getBetters();
        for (int index = 0; index < MAX_COUNT_SIMULATION_HANDS; index++) {
            mHandStrategyIndex[index] = index % strategies.size();
            mHandBetterIndex[index] = index % bettings.size();
        }
    }

    private void setRuleSpecial() {
        // special rules
        allowDoubledownAfterSplit = false;
        hitOnDealerSoft17 = true;
        allowSplitDifferentTenValues = false;
        allowSurrender = true;

        allowDealerHole = true;

        maxSplitCount = 4;          // 3 splits, maximum 4 hands
    }
}
