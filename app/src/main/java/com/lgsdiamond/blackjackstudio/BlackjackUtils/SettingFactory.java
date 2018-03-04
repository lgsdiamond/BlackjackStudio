package com.lgsdiamond.blackjackstudio.BlackjackUtils;

import android.content.res.XmlResourceParser;
import android.widget.Spinner;
import android.widget.Switch;

import com.lgsdiamond.blackjackstudio.ActivityStudio;
import com.lgsdiamond.blackjackstudio.BlackjackElement.Rule;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;

public class SettingFactory {
    Rule mRuleReference;

    public SettingFactory(Rule ruleReference) {
        mRuleReference = ruleReference;
    }

    public ArrayList<BjSetting> getDealerSettings() {

        ArrayList<BjSetting> dealerSettings = new ArrayList<>();

        // Dealer Hit on Soft 17: HS17:SS17
        SwitchSetting dealerHitSoft17 = new SwitchSetting("Dealer Hit on Soft 17?",
                "dealer can hit or stand on Soft 17",
                new String[]{"STAND", "HIT"}) {

            @Override
            public boolean getGenuineCheckedValue() {
                return mRuleReference.hitOnDealerSoft17;
            }

            @Override
            public void settingRuleOut() {
                mRuleReference.hitOnDealerSoft17 = mIsChecked;
            }

            @Override
            public String getSubString(boolean isChecked) {
                return (isChecked ? "HS17" : "SS17");
            }
        };

        dealerHitSoft17.setSectionTop("Dealer Options");
        dealerSettings.add(dealerHitSoft17);

        // Surrender: Sur / N-Sur
        SwitchSetting dealerAllowSurrender = new SwitchSetting("Allow Surrender",
                "player can surrender with first two cards",
                new String[]{"NO", "YES"}) {
            @Override
            public boolean getGenuineCheckedValue() {
                return mRuleReference.allowSurrender;
            }

            @Override
            public void settingRuleOut() {
                mRuleReference.allowSurrender = mIsChecked;
            }

            @Override
            public String getSubString(boolean isChecked) {
                return (isChecked ? "Sur" : "N-Sur");
            }
        };
        dealerSettings.add(dealerAllowSurrender);

        // Second Card Deal: 2ndCard=LATER/AtOnce
        SwitchSetting dealerDealSecondCard = new SwitchSetting("Dealer/Split Hand second card deal",
                "dealer can deal the second card of dealer hand or split hand at once or later",
                new String[]{"ONCE", "LATER"}) {
            @Override
            public boolean getGenuineCheckedValue() {
                return (mRuleReference.ruleSecondCardDeal == Rule.RuleSecondCardDeal.LATER);
            }

            @Override
            public void settingRuleOut() {
                mRuleReference.ruleSecondCardDeal = mIsChecked ?
                        Rule.RuleSecondCardDeal.LATER : Rule.RuleSecondCardDeal.AT_ONCE;
            }

            @Override
            public String getSubString(boolean isChecked) {
                return ("2ndCard=" + (isChecked ? "LATER" : "ONCE"));
            }
        };
        dealerSettings.add(dealerDealSecondCard);

        // Peek Hole allowed? : PHole / N-PHole
        SwitchSetting dealerAllowPeek = new SwitchSetting("Peek-hole allowed",
                "Dealer can use peek-hole for Ace or 10 card",
                new String[]{"NO", "YES"}) {
            @Override
            public boolean getGenuineCheckedValue() {
                return (mRuleReference.allowDealerHole);
            }

            @Override
            public void settingRuleOut() {
                mRuleReference.allowDealerHole = mIsChecked;
            }

            @Override
            public String getSubString(boolean isChecked) {
                return (isChecked ? "Peek" : "N-Peek");
            }
        };
        dealerSettings.add(dealerAllowPeek);

        return dealerSettings;
    }

    public ArrayList<BjSetting> getDoubledownSettings() {
        ArrayList<BjSetting> doubledownSettings = new ArrayList<>();

        // Doubledown after Split: DaSp / N-DaSp
        SwitchSetting doubledownAfterSplit = new SwitchSetting("Doubledown after Split",
                "player can doubledown after card splitting",
                new String[]{"NO", "YES"}) {
            @Override
            public boolean getGenuineCheckedValue() {
                return (mRuleReference.allowDoubledownAfterSplit);
            }

            @Override
            public void settingRuleOut() {
                mRuleReference.allowDoubledownAfterSplit = mIsChecked;
            }

            @Override
            public String getSubString(boolean isChecked) {
                return (isChecked ? "DaSp" : "N-DaSp");
            }
        };
        doubledownAfterSplit.setSectionTop("Doubledown Options");
        doubledownSettings.add(doubledownAfterSplit);

        // Doubledown on
        SpinnerSetting doubledownOn = new SpinnerSetting("Doubledown on",
                "Double down on any 2 cards, 9/10/11, or 10/11",
                new String[]{"Any 2 cards", "9/10/11", "10/11"}) {

            @Override
            public int getGenuineIndexValue() {
                int index;
                switch (mRuleReference.ruleDoubledown) {
                    default:
                    case ANY_TWO_CARDS:
                        index = 0;
                        break;
                    case ONLY_NINE_TEN_ELEVEN:
                        index = 1;
                        break;
                    case ONLY_TEN_ELEVEN:
                        index = 2;
                        break;
                }

                return index;
            }

            @Override
            public void settingRuleOut() {
                mRuleReference.ruleDoubledown = getRuleDoubledownByIndex(mSelectedPosition);
            }

            Rule.RuleDoubledown getRuleDoubledownByIndex(int index) {
                Rule.RuleDoubledown rule;
                switch (index) {
                    default:
                    case 0:
                        rule = Rule.RuleDoubledown.ANY_TWO_CARDS;
                        break;
                    case 1:
                        rule = Rule.RuleDoubledown.ONLY_NINE_TEN_ELEVEN;
                        break;
                    case 2:
                        rule = Rule.RuleDoubledown.ONLY_TEN_ELEVEN;
                        break;
                }

                return rule;
            }

            @Override
            public String toString() {
                Rule.RuleDoubledown rule;

                rule = mTouched ?
                        getRuleDoubledownByIndex(mSelectedPosition) : mRuleReference.ruleDoubledown;
                return "DD=" +
                        ((rule == Rule.RuleDoubledown.ANY_TWO_CARDS) ? "Any2" :
                                ((rule == Rule.RuleDoubledown.ONLY_NINE_TEN_ELEVEN) ?
                                        "9/10/11" : "10/11"));
            }
        };
        doubledownSettings.add(doubledownOn);

        return (doubledownSettings);
    }

    public ArrayList<BjSetting> getSplitSettings() {
        ArrayList<BjSetting> splitSettings = new ArrayList<>();

        SpinnerSetting splitMaximum = new SpinnerSetting("Maximum number of split",
                "player can split",
                new String[]{"1-Split", "2-Split", "3-Split", "4-Split", "all the time"}) {

            public int getGenuineIndexValue() {
                int index;
                switch (mRuleReference.maxSplitCount) {
                    case 1:
                    case 2:
                    case 3:
                    case 4:
                        index = mRuleReference.maxSplitCount - 1;
                        break;
                    case 99:
                    default:
                        index = 4;
                        break;
                }
                return index;
            }

            @Override
            public void settingRuleOut() {
                mRuleReference.maxSplitCount = getMaxSplitByIndex(mSelectedPosition);
            }

            int getMaxSplitByIndex(int index) {
                int maxSplit;
                switch (index) {
                    case 0:
                    case 1:
                    case 2:
                    case 3:
                        maxSplit = index + 1;
                        break;
                    case 4:
                        maxSplit = 99;
                        break;
                    default:
                        maxSplit = 4;
                        break;
                }
                return maxSplit;
            }

            @Override
            public String toString() {
                int maxSplit = mTouched ?
                        getMaxSplitByIndex(mSelectedPosition) : mRuleReference.maxSplitCount;
                return "maxSp=" +
                        ((maxSplit == 99) ? "Any" : String.valueOf(maxSplit));
            }
        };
        splitMaximum.setSectionTop("Split Options");
        splitSettings.add(splitMaximum);

        // Allow split for different 10 value cards: SD10 / N-SD10
        SwitchSetting allowAceResplit = new SwitchSetting("Allow re-split for Ace?",
                "player can re-split Ace split hand",
                new String[]{"NO", "YES"}) {

            @Override
            public boolean getGenuineCheckedValue() {
                return mRuleReference.allowAceResplit;
            }

            @Override
            public void settingRuleOut() {
                mRuleReference.allowAceResplit = mIsChecked;
            }

            @Override
            public String getSubString(boolean isChecked) {
                return (isChecked ? "AceResplit" : "N-AceResplit");
            }
        };
        splitSettings.add(allowAceResplit);

        SwitchSetting splitDifferentTen = new SwitchSetting("Allow split different 10 values cards?",
                "player can split different 10 value cards: 10-Jack-Queen-King",
                new String[]{"NO", "YES"}) {

            @Override
            public boolean getGenuineCheckedValue() {
                return mRuleReference.allowSplitDifferentTenValues;
            }

            @Override
            public void settingRuleOut() {
                mRuleReference.allowSplitDifferentTenValues = mIsChecked;
            }

            @Override
            public String getSubString(boolean isChecked) {
                return (isChecked ? "SpD10" : "N-SpD10");
            }
        };
        splitSettings.add(splitDifferentTen);

        return splitSettings;
    }

    public ArrayList<BjSetting> getTableSettings(boolean isSimulation) {
        ArrayList<BjSetting> tableSettings = new ArrayList<>();

        // Number of Decks of Shoe: Deck=1 ~ Deck=8
        SpinnerSetting shoeDeckCount = new SpinnerSetting("Number of Decks in Shoe",
                "How many decks are used in Shoe",
                new String[]{"Single Deck", "Double Deck", "4 Decks", "6 Decks", "8 Decks"}) {

            @Override
            public int getGenuineIndexValue() {
                int index;
                switch (mRuleReference.mCountDecks) {
                    case 1:
                        index = 0;
                        break;
                    case 2:
                        index = 1;
                        break;
                    case 4:
                        index = 2;
                        break;
                    default:
                    case 6:
                        index = 3;
                        break;
                    case 8:
                        index = 4;
                        break;
                }
                return index;
            }

            @Override
            public void settingRuleOut() {
                mRuleReference.mCountDecks = getNumDecksByIndex(mSelectedPosition);
            }

            int getNumDecksByIndex(int index) {
                int nDecks;
                switch (index) {
                    case 0:
                        nDecks = 1;
                        break;
                    case 1:
                        nDecks = 2;
                        break;
                    case 2:
                        nDecks = 4;
                        break;
                    default:
                    case 3:
                        nDecks = 6;
                        break;
                    case 4:
                        nDecks = 8;
                        break;
                }
                return nDecks;
            }

            @Override
            public String toString() {
                int nDecks = mTouched ?
                        getNumDecksByIndex(mSelectedPosition) : mRuleReference.mCountDecks;
                return "Deck=" + String.valueOf(nDecks);
            }
        };
        shoeDeckCount.setSectionTop("Table Options");
        tableSettings.add(shoeDeckCount);

        // Max Number of Multi-Hand: Box-1 ~ Box-8
        SpinnerSetting tableBoxCount = new SpinnerSetting("Maximum number of multi-hand",
                "How many boxes for multi player hand",
                new String[]{"1 Box", "2 Boxes", "3 Boxes", "4 Boxes", "5 Boxes",
                        "6 Boxes", "7 Boxes", "8 Boxes", "20 Boxes"}) {

            @Override
            public int getGenuineIndexValue() {
                int index;
                switch (mRuleReference.mCountBoxes) {
                    case 1:
                    case 2:
                    case 3:
                    case 4:
                    default:
                        index = Math.max(mRuleReference.mCountBoxes - 1, 3);
                        break;
                }
                return index;
            }

            @Override
            public void settingRuleOut() {
                mRuleReference.mCountBoxes = getNumBoxesByIndex(mSelectedPosition);
            }

            int getNumBoxesByIndex(int index) {
                int numBoxes;
                switch (index) {
                    default:
                    case 0:
                    case 1:
                    case 2:
                    case 3:
                        numBoxes = index + 1;
                        break;
                }

                return numBoxes;
            }

            @Override
            public String toString() {
                int numBoxes;

                numBoxes = mTouched ?
                        getNumBoxesByIndex(mSelectedPosition) : mRuleReference.mCountBoxes;
                return "Boxes=" + String.valueOf(numBoxes);
            }
        };

        if (!isSimulation) {
            tableSettings.add(tableBoxCount);
        }

        SwitchSetting tableBlackjackPayout = new SwitchSetting("Blackjack Payout",
                "Casino pays player's Blackjack with payout ratio: 6 to 5, or 3 to 2",
                new String[]{"6-5", "3-2"}) {
            @Override
            public boolean getGenuineCheckedValue() {
                return (mRuleReference.blackjackPayout == 1.5);
            }

            @Override
            public void settingRuleOut() {
                mRuleReference.blackjackPayout = mIsChecked ? 1.5 : 1.2;
            }

            @Override
            public String getSubString(boolean isChecked) {
                return ("BJ Payout=" + (isChecked ? "3-2" : "6-5"));
            }
        };
        tableSettings.add(tableBlackjackPayout);

        SpinnerSetting tableMinBet = new SpinnerSetting("Minimum Betting",
                "minimum betting of players for each hand",
                new String[]{"$1", "$5", "$10"}) {
            @Override
            public int getGenuineIndexValue() {
                int index;
                if (mRuleReference.minBet == 1.0)
                    index = 0;
                else if (mRuleReference.minBet == 5.0)
                    index = 1;
                else
                    index = 2;

                return index;
            }

            @Override
            public void settingRuleOut() {
                mRuleReference.minBet = getMinBetByIndex(mSelectedPosition);
            }

            double getMinBetByIndex(int index) {
                double minBet;
                switch (index) {
                    default:
                    case 0:
                        minBet = 1.0;
                        break;
                    case 1:
                        minBet = 5.0;
                        break;
                    case 2:
                        minBet = 10.0;
                        break;
                }

                return minBet;
            }

            @Override
            public String toString() {
                double minBet;

                minBet = mTouched ?
                        getMinBetByIndex(mSelectedPosition) : mRuleReference.minBet;
                return "MinBet=" + UtilityStudio.getUsCurrencyWithNoCents(minBet);
            }
        };
        tableSettings.add(tableMinBet);

        SpinnerSetting tableMaxBet = new SpinnerSetting("Maximum Bet Amount",
                "maximum betting of players for each hand",
                new String[]{"$100", "$1,000", "$10,000", "No Limit"}) {
            @Override
            public int getGenuineIndexValue() {
                int index;
                if (mRuleReference.maxBet == 100.0)
                    index = 0;
                else if (mRuleReference.maxBet == 1000.0)
                    index = 1;
                else if (mRuleReference.maxBet == 10000.0)
                    index = 2;
                else
                    index = 3;

                return index;
            }

            @Override
            public void settingRuleOut() {
                mRuleReference.maxBet = getMaxBetByIndex(mSelectedPosition);
            }

            double getMaxBetByIndex(int index) {
                double maxBet;
                switch (index) {
                    case 0:
                        maxBet = 100.0;
                        break;
                    default:
                    case 1:
                        maxBet = 1000.0;
                        break;
                    case 2:
                        maxBet = 10000.0;
                        break;
                    case 3:
                        maxBet = Double.MAX_VALUE;
                        break;
                }

                return maxBet;
            }

            @Override
            public String toString() {
                double maxBet;

                maxBet = mTouched ?
                        getMaxBetByIndex(mSelectedPosition) : mRuleReference.maxBet;
                return "MaxBet=" + ((maxBet < Double.MAX_VALUE) ?
                        UtilityStudio.getUsCurrencyWithNoCents(maxBet) : "No-Limit");
            }
        };
        tableSettings.add(tableMaxBet);

        SwitchSetting tableAllowNegativeBankroll = new SwitchSetting("Negative Bankroll",
                "Player's bankroll can be negative(i.e., no limit bankroll)",
                new String[]{"No", "Yes"}) {
            @Override
            public boolean getGenuineCheckedValue() {
                return (mRuleReference.allowNegativeBankroll);
            }

            @Override
            public void settingRuleOut() {
                mRuleReference.allowNegativeBankroll = mIsChecked;
            }

            @Override
            public String getSubString(boolean isChecked) {
                return ("Negative Bankroll=" + (isChecked ? "OK" : "NO"));
            }
        };
        tableSettings.add(tableAllowNegativeBankroll);

        SpinnerSetting tablePlayerBankroll = new SpinnerSetting("Player's initial bankroll",
                "Player starts with bankroll",
                new String[]{"$100", "$1,000", "$10,000", "$100,000", "$1,000,000", "No-Limit"}) {

            @Override
            public int getGenuineIndexValue() {
                int index;
                if (mRuleReference.playerBankroll == 100.0)
                    index = 0;
                else if (mRuleReference.playerBankroll == 1000.0)
                    index = 1;
                else if (mRuleReference.playerBankroll == 10000.0)
                    index = 2;
                else if (mRuleReference.playerBankroll == 100000.0)
                    index = 3;
                else if (mRuleReference.playerBankroll == 1000000.0)
                    index = 4;
                else
                    index = 5;

                return index;
            }

            @Override
            public void settingRuleOut() {
                mRuleReference.playerBankroll = getBankrollByIndex(mSelectedPosition);
            }

            double getBankrollByIndex(int index) {
                double bankroll;
                switch (index) {
                    case 0:
                        bankroll = 100;
                        break;
                    case 1:
                        bankroll = 1000;
                        break;
                    default:
                    case 2:
                        bankroll = 10000;
                        break;
                    case 3:
                        bankroll = 100000;
                        break;
                    case 4:
                        bankroll = 1000000;
                        break;
                    case 5:
                        bankroll = Double.MAX_VALUE;
                        break;
                }

                return bankroll;
            }

            @Override
            public String toString() {
                double bankroll;

                bankroll = mTouched ?
                        getBankrollByIndex(mSelectedPosition) : mRuleReference.playerBankroll;
                return "Bankroll=" + ((bankroll < Double.MAX_VALUE) ?
                        UtilityStudio.getUsCurrencyWithNoCents(bankroll) : "No-Limit");
            }
        };
        tableSettings.add(tablePlayerBankroll);

        SwitchSetting useRandomShoe = new SwitchSetting("Shoe Random Seed",
                "shoe uses random seed at starting game",
                new String[]{"Fixed", "Random"}) {

            @Override
            public boolean getGenuineCheckedValue() {
                return mRuleReference.useRandomShoe;
            }

            @Override
            public void settingRuleOut() {
                mRuleReference.useRandomShoe = mIsChecked;
            }

            @Override
            public String getSubString(boolean isChecked) {
                return ("Shoe-" + (isChecked ? "Random" : "Fixed"));
            }
        };
        tableSettings.add(useRandomShoe);

        SpinnerSetting tableShoeBiased = new SpinnerSetting("Shoe bias for just fun",
                "from -1.0 to 1.0",
                new String[]{"-1.0", "-0.8", "-0.6", "-0.4", "-0.2", "0.0",
                        "0.2", "0.4", "0.6", "0.8", "1.0"}) {

            @Override
            public int getGenuineIndexValue() {
                int index = (int) ((mRuleReference.mBiasedShoe + 1.0) * 5.0);
                return index;
            }

            @Override
            public void settingRuleOut() {
                mRuleReference.mBiasedShoe = getBiasedByIndex(mSelectedPosition);
            }

            double getBiasedByIndex(int index) {
                double biased = (double) index / 5.0 - 1.0;

                return biased;
            }

            @Override
            public String toString() {
                double biased;

                biased = mTouched ?
                        getBiasedByIndex(mSelectedPosition) : mRuleReference.mBiasedShoe;
                return "Biased(Fun)=" + Double.toString(biased);
            }
        };
        tableSettings.add(tableShoeBiased);

        SwitchSetting useSound = new SwitchSetting("Play Sounds",
                "Game sound can be turned ON or OFF",
                new String[]{"OFF", "ON"}) {

            @Override
            public boolean getGenuineCheckedValue() {
                return mRuleReference.mUseSound;
            }

            @Override
            public void settingRuleOut() {
                mRuleReference.mUseSound = mIsChecked;
            }

            @Override
            public String getSubString(boolean isChecked) {
                return ("Sound-" + (isChecked ? "ON" : "OFF"));
            }
        };
        if (!isSimulation) {
            tableSettings.add(useSound);
        }

        SwitchSetting useAnimation = new SwitchSetting("Animations",
                "Card Animations can be turned ON or OFF",
                new String[]{"OFF", "ON"}) {

            @Override
            public boolean getGenuineCheckedValue() {
                return mRuleReference.mUseAnimation;
            }

            @Override
            public void settingRuleOut() {
                mRuleReference.mUseAnimation = mIsChecked;
            }

            @Override
            public String getSubString(boolean isChecked) {
                return ("Animation-" + (isChecked ? "ON" : "OFF"));
            }
        };
        if (!isSimulation) {
            tableSettings.add(useAnimation);
        }


        return tableSettings;
    }

    public ArrayList<SimulationHandSetting> getHandStrategySettings(Rule rule) {
        ArrayList<SimulationHandSetting> handStrategySettings = new ArrayList<>();

        for (int i = 0; i < Rule.MAX_COUNT_SIMULATION_HANDS; i++) {
            SimulationHandSetting handSetting = new SimulationHandSetting(i, rule);
            handStrategySettings.add(handSetting);
        }
        return handStrategySettings;
    }

    public ArrayList<BjSetting> getBasicSettingsForSimulation() {

        ArrayList<BjSetting> simulationSettings = new ArrayList<>();

        SpinnerSetting settingCountRounds = new SpinnerSetting("Number of Runs",
                "How many runs for simulation",
                new String[]{"1", "10", "100", "1,000", "10,000", "100,000", "1,000,000"}) {

            @Override
            public int getGenuineIndexValue() {
                int index;
                switch (mRuleReference.mCountSimulationRuns) {
                    case 1:
                        index = 0;
                        break;
                    case 10:
                        index = 1;
                        break;
                    case 100:
                        index = 2;
                        break;
                    case 1000:
                        index = 3;
                        break;
                    default:
                    case 10000:
                        index = 4;
                        break;
                    case 100000:
                        index = 5;
                        break;
                    case 1000000:
                        index = 6;
                        break;
                }
                return index;
            }

            @Override
            public void settingRuleOut() {
                mRuleReference.mCountSimulationRuns = getCountRuns(mSelectedPosition);
            }

            private int getCountRuns(int index) {
                int nRuns;
                switch (index) {
                    case 0:
                        nRuns = 1;
                        break;
                    case 1:
                        nRuns = 10;
                        break;
                    case 2:
                        nRuns = 100;
                        break;
                    case 3:
                        nRuns = 1000;
                        break;
                    default:
                    case 4:
                        nRuns = 10000;
                        break;
                    case 5:
                        nRuns = 100000;
                        break;
                    case 6:
                        nRuns = 1000000;
                        break;
                }
                return nRuns;
            }

            @Override
            public String toString() {
                int nRuns = mTouched ?
                        getCountRuns(mSelectedPosition) : mRuleReference.mCountSimulationRuns;
                return "Runs=" + String.valueOf(nRuns);
            }
        };
        settingCountRounds.setSectionTop("Simulation Options");
        simulationSettings.add(settingCountRounds);

        SwitchSetting settingBettingLimit = new SwitchSetting("Player's Betting Limit",
                "Simulation stops at the player's bankrupt",
                new String[]{"NO", "YES"}) {
            @Override
            public boolean getGenuineCheckedValue() {
                return (mRuleReference.mHasBettingLimit);
            }

            @Override
            public void settingRuleOut() {
                mRuleReference.mHasBettingLimit = mIsChecked;
            }

            @Override
            public String getSubString(boolean isChecked) {
                return ("BettingLimit=" + (isChecked ? "YES" : "NO"));
            }
        };
        simulationSettings.add(settingBettingLimit);

        return simulationSettings;
    }

    public void setSettingAdapter(SettingAdapter settingAdapter) {
        mSettingAdapter = settingAdapter;
    }

    // inner class

    public abstract class BjSetting {
        public String mSectionTitle = "";
        public String mTitle;
        public String mDescription;
        public String[] mDataStrings;
        public boolean mTouched = false;

        public BjSetting(String title, String description, String[] dataText) {
            mTitle = title;
            mDescription = description;
            mDataStrings = dataText;
        }

        public void setSectionTop(String sectionTitle) {
            mSectionTitle = sectionTitle;
        }

        abstract public boolean needUpdate();

        abstract public void settingRuleIn();

        abstract public void settingRuleOut();

        public void setTouched(boolean touched) {
            mTouched = touched;
        }
    }

    public abstract class SwitchSetting extends BjSetting {
        public static final int INDEX_OFF = 0, INDEX_ON = 1;
        public boolean mIsChecked, mOldChecked;
        public Switch mSwitch;

        public SwitchSetting(String title, String description, String[] dataText) {
            super(title, description, dataText);
        }

        @Override
        public final void settingRuleIn() {
            if (mTouched) {
                mSwitch.setChecked(mIsChecked);
            } else {
                mIsChecked = mOldChecked = getGenuineCheckedValue();
                mSwitch.setChecked(mOldChecked);
                mTouched = true;
            }
        }

        @Override
        public final boolean needUpdate() {
            return (mOldChecked != mIsChecked);
        }

        @Override
        public String toString() {
            return getSubString(mTouched ? mIsChecked : getGenuineCheckedValue());
        }

        public void setSwitch(Switch aSwitch) {
            mSwitch = aSwitch;
        }

        public Switch getSwitch() {
            return mSwitch;
        }

        public abstract boolean getGenuineCheckedValue();

        public abstract String getSubString(boolean isChecked);

        public void postSwitchCheckedChange() {
            // no nothing now
        }
    }

    public abstract class SpinnerSetting extends BjSetting {
        public int mSelectedPosition, mOldPosition;
        Spinner mSpinner;

        public SpinnerSetting(String title, String description, String[] dataText) {
            super(title, description, dataText);
        }

        @Override
        public void settingRuleIn() {
            if (mTouched) {
                mSpinner.setSelection(mSelectedPosition);
            } else {
                int index = getGenuineIndexValue();
                mSpinner.setSelection(index);
                mSelectedPosition = mOldPosition = mSpinner.getSelectedItemPosition();
                mTouched = true;
            }
        }

        @Override
        public boolean needUpdate() {
            return (mOldPosition != mSelectedPosition);
        }

        public abstract int getGenuineIndexValue();

        public void setSpinner(Spinner spinner) {
            mSpinner = spinner;
        }

        public Spinner getSpinner() {
            return mSpinner;
        }

        public void postSpinnerItemSelected() {
        }
    }

    public abstract class SpinnerTwoSetting extends SpinnerSetting {
        public int mSelectedPositionTwo, mOldPositionTwo;
        Spinner mSpinnerTwo;
        public String[] mDataStringsTwo;

        public SpinnerTwoSetting(String title, String description, String[] dataText, String[] dataTextTwo) {
            super(title, description, dataText);
            mDataStringsTwo = dataTextTwo;
        }

        @Override
        public final void settingRuleIn() {
            boolean mOldTouched = mTouched;
            super.settingRuleIn();

            if (mOldTouched) {
                mSpinnerTwo.setSelection(mSelectedPositionTwo);
            } else {
                int index = getGenuineIndexValueTwo();
                mSpinnerTwo.setSelection(index);
                mSelectedPositionTwo = mOldPositionTwo = mSpinnerTwo.getSelectedItemPosition();
                mTouched = true;
            }
        }

        @Override
        public final boolean needUpdate() {
            return (super.needUpdate() ||
                    (mOldPositionTwo != mSelectedPositionTwo));
        }

        public abstract int getGenuineIndexValue();

        public abstract int getGenuineIndexValueTwo();

        public void setSpinnerTwo(Spinner spinner) {
            mSpinnerTwo = spinner;
        }

        public Spinner getSpinnerTwo() {
            return mSpinnerTwo;
        }

        public void postSpinnerTwoItemSelected() {
            // Do nothing now
        }
    }

    private static GameSetting readGameSetting(XmlResourceParser parser) {
        final String nameSpace = null;

        GameSetting gameSetting = null;
        Boolean isSuccess = false;

        try {
            parser.require(XmlPullParser.START_TAG, nameSpace, TAG_GameSetting);
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        int eventType;
        String name;
        try {
            while (true) {
                eventType = parser.next();
                if (eventType == XmlPullParser.END_DOCUMENT) break;
                if ((eventType == XmlPullParser.END_TAG)
                        && (name = parser.getName()).equalsIgnoreCase(TAG_GameSetting)) break;

                if (eventType != XmlPullParser.START_TAG) continue;
                name = parser.getName();

                if (name.equals(TAG_GameSetting)) {
                    gameSetting = readSetting(parser);
                }
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return (gameSetting == null) ? new GameSetting() : gameSetting;
    }

    private static GameSetting readSetting(XmlResourceParser parser)
            throws XmlPullParserException, IOException {

        GameSetting gameSetting = new GameSetting();

        int eventType;
        String name, text;
        while (true) {
            eventType = parser.next();
            if (eventType == XmlPullParser.END_DOCUMENT) break;
            if ((eventType == XmlPullParser.END_TAG)
                    && (name = parser.getName()).equalsIgnoreCase(TAG_GameSetting)) break;

            if (eventType != XmlPullParser.START_TAG) continue;
            name = parser.getName();

            if (parser.next() != XmlPullParser.TEXT) continue;
            text = parser.getText();

            switch (name) {
                case TAG_BoxCount:
                    gameSetting.countBoxes = Integer.parseInt(text);
                    break;
                case TAG_MinBet:
                    gameSetting.minBet = Double.parseDouble(text);
                    break;
                case TAG_MaxBet:
                    gameSetting.maxBet = Double.parseDouble(text);
                    break;
                case TAG_Bankroll:
                    gameSetting.playerBankroll = Double.parseDouble(text);
                    break;
                case TAG_DealerName:
                    gameSetting.dealerName = parser.getText();
                    break;
                case TAG_PlayerName:
                    gameSetting.playerName = parser.getText();
                    break;
                default:
                    break;
            }
        }

        return gameSetting;
    }

    public static void loadGameSetting(XmlResourceParser parser) {
        try {
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
        try {
            parser.nextTag();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        readGameSetting(parser);

        parser.close(); // close resource
    }

    // for parsing
    private static final String TAG_GameSetting = "GameSetting";
    private static final String TAG_BoxCount = "BoxCount";
    private static final String TAG_MinBet = "MinBet";
    private static final String TAG_MaxBet = "MaxBet";
    private static final String TAG_Bankroll = "Bankroll";
    private static final String TAG_DealerName = "DealerName";
    private static final String TAG_PlayerName = "PlayerName";

    public static class GameSetting {
        // key members
        public int countBoxes;

        public double minBet;
        public double maxBet;
        public double playerBankroll;

        public String dealerName;
        public String playerName;
    }

    // special sub-class of SpinnerTwoSetting
    public class SimulationHandSetting extends SpinnerTwoSetting {
        Rule mRule;
        int mHandIndex;

        public SimulationHandSetting(int handIndex, Rule rule) {
            super("Strategy for Hand-" + String.valueOf(handIndex + 1),
                    "Action & Betting",
                    ActivityStudio.sStudio.getStrategiesTitles(),
                    ActivityStudio.sStudio.getBettersTitles());

            mHandIndex = handIndex;
            mRule = rule;
        }

        @Override
        public void settingRuleOut() {
            mRule.mHandStrategyIndex[mHandIndex] = mSelectedPosition;
            mRule.mHandBetterIndex[mHandIndex] = mSelectedPositionTwo;
        }

        @Override
        public int getGenuineIndexValue() {
            return mRule.mHandStrategyIndex[mHandIndex];
        }

        public int getGenuineIndexValueTwo() {
            return mRule.mHandBetterIndex[mHandIndex];
        }

        @Override
        public String toString() {
            return ("Action|Betting=" +
                    String.valueOf(mTouched ?
                            mSelectedPosition : mRule.mHandStrategyIndex[mHandIndex]) + "|" +
                    String.valueOf(mTouched ?
                            mSelectedPositionTwo : mRule.mHandBetterIndex[mHandIndex]));
        }
    }

    private int mCountSimulationOptions;
    ArrayList<BjSetting> mSimulatorSettings;
    ArrayList<SimulationHandSetting> mHandSettings;
    SettingAdapter mSettingAdapter;

    SpinnerSetting mShowProgressUpdateSetting;

    public ArrayList<BjSetting> getSimulatorSettings() {
        if (mSimulatorSettings != null) return mSimulatorSettings;

        mSimulatorSettings = getBasicSettingsForSimulation();
        mCountSimulationOptions = mSimulatorSettings.size();

        SpinnerSetting settingCountHands = new SpinnerSetting("Number of Hands",
                "How many player hands for simulation",
                new String[]{"1", "2", "3", "4", "5"}) {

            @Override
            public int getGenuineIndexValue() {
                int index;
                switch (mRuleReference.mCountBoxes) {
                    case 1:
                        index = 0;
                        break;
                    case 2:
                        index = 1;
                        break;
                    case 3:
                        index = 2;
                        break;
                    default:
                    case 4:
                        index = 3;
                        break;
                    case 5:
                        index = 4;
                        break;
                }
                return index;
            }

            @Override
            public void settingRuleOut() {
                mRuleReference.mCountBoxes = getCountHands(mSelectedPosition);
            }

            private int getCountHands(int index) {
                int nHands;
                switch (index) {
                    case 0:
                        nHands = 1;
                        break;
                    case 1:
                        nHands = 2;
                        break;
                    case 2:
                        nHands = 3;
                        break;
                    default:
                    case 3:
                        nHands = 4;
                        break;
                    case 4:
                        nHands = 5;
                        break;
                }
                return nHands;
            }

            @Override
            public String toString() {
                int nHands = mTouched ?
                        getCountHands(mSelectedPosition) : mRuleReference.mCountBoxes;
                return "Hands=" + String.valueOf(nHands);
            }

            @Override
            public void postSpinnerItemSelected() {
                super.postSpinnerItemSelected();
                updateHandOptions(getCountHands(mSelectedPosition));
            }
        };
        mSimulatorSettings.add(settingCountHands);

        mHandSettings = getHandStrategySettings(mRuleReference);
        for (int index = 0; index < mRuleReference.mCountBoxes; index++) {
            mSimulatorSettings.add(mHandSettings.get(index));
        }

        // Progress Update
        mShowProgressUpdateSetting = new SpinnerSetting("Progress Update",
                "What will be shown during simulation progress?",
                new String[]{"just final result", "only bankroll change", "all changes"}) {

            @Override
            public int getGenuineIndexValue() {
                int index;
                switch (mRuleReference.showProgressUpdateCondition) {
                    case JUST_FINAL:
                        index = 0;
                        break;
                    case BANKROLL:
                        index = 1;
                        break;
                    default:
                    case ALL:
                        index = 2;
                        break;
                }
                return index;
            }

            @Override
            public void settingRuleOut() {
                mRuleReference.showProgressUpdateCondition = getUpdateCondition(mSelectedPosition);
            }

            private Rule.ProgressUpdateCondition getUpdateCondition(int index) {
                Rule.ProgressUpdateCondition condition;

                switch (index) {
                    case 0:
                        condition = Rule.ProgressUpdateCondition.JUST_FINAL;
                        break;
                    case 1:
                        condition = Rule.ProgressUpdateCondition.BANKROLL;
                        break;
                    default:
                    case 2:
                        condition = Rule.ProgressUpdateCondition.ALL;
                        break;
                }
                return condition;
            }

            @Override
            public String toString() {
                return "";
            }

            @Override
            public void postSpinnerItemSelected() {
                super.postSpinnerItemSelected();
            }
        };
        mShowProgressUpdateSetting.setSectionTop("Simulation Progress Update");
        mSimulatorSettings.add(mShowProgressUpdateSetting);

        // add more options
        mSimulatorSettings.addAll(getDealerSettings());
        mSimulatorSettings.addAll(getDoubledownSettings());
        mSimulatorSettings.addAll(getSplitSettings());
        mSimulatorSettings.addAll(getTableSettings(true));

        return mSimulatorSettings;
    }

    private void updateHandOptions(int nHands) {
        int oldCountHands = mRuleReference.mCountBoxes;
        if (oldCountHands == nHands) return;

        if (oldCountHands < nHands) {
            for (int i = oldCountHands; i < nHands; i++) {
                mSimulatorSettings.add(mCountSimulationOptions + i + 1, mHandSettings.get(i));
            }
        } else if (oldCountHands > nHands) {
            for (int i = nHands; i < oldCountHands; i++) {
                mSimulatorSettings.remove(mCountSimulationOptions + nHands + 1);
            }
        }

        mRuleReference.mCountBoxes = nHands;
        if (mSettingAdapter != null) mSettingAdapter.notifyDataSetChanged();
    }
}
