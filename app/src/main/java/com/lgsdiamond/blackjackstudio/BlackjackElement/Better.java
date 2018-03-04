package com.lgsdiamond.blackjackstudio.BlackjackElement;

import com.lgsdiamond.blackjackstudio.BlackjackUtils.UtilityStudio;

import java.util.ArrayList;

/**
 * Created by lgsdiamond on 2015-12-09.
 */
public class Better {
    static final int DEFAULT_MAX_CUT_LEVEL_COUNT = 10;
    static final int DEFAULT_MAX_CUT_LEVEL_COUNT_FOR_ANTI_MARTINGALE = 5;

    static String[] sBetterActionStrings;
    static ArrayList<Better> sPredefinedBetters;
    static double sPredefinedBaseBet = 1.0;
    String mTitle = "";
    String mUniqueId = "";
    String mDescription = "";

    // members for computing nextBet just one time only
    BettingBox mBoxBasedOn;
    double mProposedNextBet, mLastBet, mBaseBet;
    BjService.RoundResult mLastResult;

    public static String[] getFundamentalBetterActionStrings() {
        if (sBetterActionStrings == null) {
            sBetterActionStrings = new String[BetterAction.values().length];
            int index = 0;
            for (BetterAction action : BetterAction.values()) {
                sBetterActionStrings[index++] = action.name();
            }
        }
        return sBetterActionStrings;
    }

    @Override
    public String toString() {
        return mTitle + "[" + mUniqueId + "]";
    }

    public static void setPredefinedBaseBet(double baseBet) {
        sPredefinedBaseBet = baseBet;
    }

    public static ArrayList<Better> getPredefinedBetters() {
        if (sPredefinedBetters == null) {
            sPredefinedBetters = new ArrayList<>();

            //[1, FLAT] FLAT, forever flat
            sPredefinedBetters.add(newFlatBatter("FLAT"));

            //[2, OSCAR(10)] OSCAR, with DEFAULT_MAX_CUT_LEVEL_COUNT(=10)
            sPredefinedBetters.add(newOscarBetter("OSCAR(10)", DEFAULT_MAX_CUT_LEVEL_COUNT));

            //[3, Ma(10)] Martingale, with DEFAULT_MAX_CUT_LEVEL_COUNT(=10)
            sPredefinedBetters.add(newMartingaleBetter("Ma(10)", 2.0, DEFAULT_MAX_CUT_LEVEL_COUNT));

            //[4, 1-3-2-4] 1-3-2-4 Sequence, with WIN action-based
            sPredefinedBetters.add(newSequenceBetter("1-3-2-4", new double[]{1, 3, 2, 4},
                    BetterActionBase.WIN));

            //[5, Tenth] Proportional with factor 10
            sPredefinedBetters.add(newProportionalBetter("Tenth", 10.0));

            //[6, AMa(5)] Anti-Martingale, with DEFAULT_MAX_CUT_LEVEL_COUNT_FOR_ANTI_MARTINGALE(=5)
            sPredefinedBetters.add(newAntiMartingaleBetter("AMa(5)", 2.0,
                    DEFAULT_MAX_CUT_LEVEL_COUNT_FOR_ANTI_MARTINGALE));

            //[7, SMa(10)] Strong Martingale
            sPredefinedBetters.add(newStrongMartingaleBetter("SMa(10)", DEFAULT_MAX_CUT_LEVEL_COUNT));
        }

        return sPredefinedBetters;
    }

    public double[] getSequence() {
        return mSequence;
    }

    public String getSequenceString() {
        String sequenceString = "";
        double[] sequence = getSequence();

        if (sequence != null) {
            for (double val : sequence) {
                sequenceString += String.valueOf(val) + "-";
            }
            sequenceString += ")";
            sequenceString = sequenceString.replace("-)", "");
        }
        return sequenceString;
    }

    public enum BetterAction {FLAT, MULTIPLY, INCREASE, PROPORTIONAL, SEQUENCE, OSCAR, STRONG_MARTINGALE}

    public enum BetterActionBase {WIN, LOST}

    public BetterAction mAction;
    public BetterActionBase mBetterActionBase;
    public double mActionValue;
    public int mMaxCutLevelCount;      // number of levels of betting action stops

    // for SEQUENCE
    double[] mSequence;

    public void setSequence(double[] sequence) {
        mSequence = sequence;
    }

    public Better(String title, String uniqueID, String description,
                  BetterAction action, BetterActionBase betterActionBase, double actionValue,
                  int maxCutLevelCount) {
        mTitle = title;
        mUniqueId = uniqueID;

        mDescription = description;
        mAction = action;
        mBetterActionBase = betterActionBase;
        mActionValue = actionValue;
        mMaxCutLevelCount = maxCutLevelCount;
    }

    // getter
    public double getActionValue() {
        return mActionValue;
    }

    public void setActionValue(double actionValue) {
        mActionValue = actionValue;
    }

    public int getMaxCutLevelCount() {
        return mMaxCutLevelCount;
    }

    public void getMaxCutLevelCount(int maxLevelCount) {
        mMaxCutLevelCount = maxLevelCount;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String description) {
        mDescription = description;
    }

    public static Better newFlatBatter(String uniqueId) {
        return new Better("Flat", uniqueId, "bet equal amount all the time",
                BetterAction.FLAT, null, 1.0, Integer.MAX_VALUE);
    }

    public static Better newOscarBetter(String uniqueId, int maxCutLevelCount) {
        if (maxCutLevelCount < 0) maxCutLevelCount = Integer.MAX_VALUE;
        return new Better("OSCAR", uniqueId, "increase by 1 unit when WIN, stay last bet when LOST",
                BetterAction.OSCAR, BetterActionBase.WIN, 1.0, maxCutLevelCount);
    }

    public static Better newProportionalBetter(String uniqueId, double proportion) {
        String title, description;
        if (proportion == 1) {
            title = "All-In";
            description = "bet all bankroll";
        } else if (proportion == 10) {
            title = "Tenth";
            description = "bet 1/10 of bankroll";
        } else if (proportion == 4) {
            title = "Quarter";
            description = "bet 1/4 of bankroll";
        } else if (proportion == 8) {
            title = "Eighth";
            description = "bet 1/8 of bankroll";
        } else {
            String denominator = UtilityStudio.doubleToDotDecimal(proportion, 2);
            title = "Proportion[1/" + String.valueOf(denominator) + "]";
            description = "bet 1/" + String.valueOf(denominator) + " of bankroll";
        }

        return new Better(title, uniqueId, description,
                BetterAction.PROPORTIONAL, null, 1.0 / proportion, Integer.MAX_VALUE);
    }

    // use multiplier < 1.0, for divide better
    public static Better newMartingaleBetter(String uniqueId, double multiplier,
                                             int maxCutLevelCount) {
        if (maxCutLevelCount < 0) maxCutLevelCount = Integer.MAX_VALUE;

        return new Better("Martingale", uniqueId, "bet increase by factor of " +
                String.valueOf(multiplier) + " on LOST, up to "
                + String.valueOf(maxCutLevelCount) + " level",
                BetterAction.MULTIPLY, BetterActionBase.LOST, multiplier, maxCutLevelCount);
    }

    // use multiplier<1.0, for divide better
    public static Better newStrongMartingaleBetter(String uniqueId, int maxCutCount) {
        if (maxCutCount < 0) maxCutCount = Integer.MAX_VALUE;

        return new Better("Strong Martingale", uniqueId, "bet all the loss + base bet",
                BetterAction.STRONG_MARTINGALE, BetterActionBase.LOST, 2.0, maxCutCount);
    }

    public static Better newAntiMartingaleBetter(String uniqueId, double multiplier,
                                                 int maxCutLevelCount) {
        if (maxCutLevelCount < 0) maxCutLevelCount = Integer.MAX_VALUE;

        return new Better("Anti-Martingale", uniqueId, "bet increase by factor of " +
                String.valueOf(multiplier) + " on WIN, up to "
                + String.valueOf(maxCutLevelCount) + " level",
                BetterAction.MULTIPLY, BetterActionBase.WIN, multiplier, maxCutLevelCount);
    }

    public static Better newSequenceBetter(String uniqueId, double[] sequence,
                                           BetterActionBase betterActionBase) {
        String seqString = "[";
        for (double value : sequence) {
            seqString = seqString + String.valueOf(value) + "-";
        }
        seqString += "]";
        seqString.replace(",]", "]");

        Better better = new Better("Sequence", uniqueId,
                "bet increase by sequence " + seqString +
                        " on " + ((betterActionBase == BetterActionBase.WIN) ? "WIN" : "LOST"),
                BetterAction.SEQUENCE, betterActionBase, 1.0, sequence.length);
        better.setSequence(sequence);

        return better;
    }

    // use negative increase, for decrease better
    public static Better newIncreaseBatter(String uniqueId, BetterActionBase betterActionBase,
                                           double increase, int maxCutLevelCount) {
        return new Better("Increase Better", uniqueId, "bet will be added by "
                + String.valueOf(increase) +
                "on " + ((betterActionBase == BetterActionBase.WIN) ? "WIN" : "LOST") +
                " up to " + String.valueOf(maxCutLevelCount) + " level",
                BetterAction.INCREASE, betterActionBase, increase, maxCutLevelCount);
    }

    public String getUniqueId() {
        makeUniqueId();
        return mUniqueId;
    }

    private void makeUniqueId() {
        if (mUniqueId.equals("")) {
            switch (mAction) {
                case FLAT:
                    mUniqueId = "FLAT";
                    break;

                case MULTIPLY:
                    mUniqueId = "M[" + String.valueOf(mActionValue) +
                            ((mBetterActionBase == BetterActionBase.WIN) ? "W]" : "L]") +
                            "(" + ((mMaxCutLevelCount == Integer.MAX_VALUE) ? "inf"
                            : String.valueOf(mMaxCutLevelCount)) + ")";
                    break;

                case INCREASE:
                    mUniqueId = "I[" + String.valueOf(mActionValue) +
                            ((mBetterActionBase == BetterActionBase.WIN) ? "W]" : "L]") +
                            "(" + ((mMaxCutLevelCount == Integer.MAX_VALUE) ? "inf"
                            : String.valueOf(mMaxCutLevelCount)) + ")";
                    break;

                case PROPORTIONAL:
                    if (mActionValue == 1) {
                        mUniqueId = "All-In";
                    } else if (mActionValue == 10) {
                        mUniqueId = "Tenth";
                    } else if (mActionValue == 4) {
                        mUniqueId = "Quarter";
                    } else if (mActionValue == 8) {
                        mUniqueId = "Eighth";
                    } else {
                        mUniqueId = "P[" + String.valueOf(mActionValue) + "]";
                    }
                    break;

                case SEQUENCE:
                    mUniqueId = "S";
                    if (mSequence != null) {
                        mUniqueId += "[";
                        for (double value : mSequence) {
                            mUniqueId += String.valueOf(value) + "-";
                        }
                        mUniqueId += "]";
                        mUniqueId = mUniqueId.replace("-]",
                                ((mBetterActionBase == BetterActionBase.WIN) ? "W]" : "L]"));
                    }
                    break;

                case OSCAR:
                    mUniqueId += "OSCAR(" + ((mMaxCutLevelCount == Integer.MAX_VALUE) ? "inf"
                            : String.valueOf(mMaxCutLevelCount)) + ")";
                    break;

                case STRONG_MARTINGALE:
                    mUniqueId += "SMa(" + ((mMaxCutLevelCount == Integer.MAX_VALUE) ? "inf"
                            : String.valueOf(mMaxCutLevelCount)) + ")";
                    break;
            }
        }
    }

    public final double getNextBet(BettingBox box) {

        // for reference for this time only
        mBoxBasedOn = box;
        mLastBet = box.getLastBet();
        mBaseBet = box.getBaseBet();

        // if lastBet is zero, new betting cycle is starting
        // we do not care lastResult in this case
        if (mLastBet == 0) {
            box.resetCycle();
            return mBaseBet;
        }

        // for FLAT better, simply return lastBet, or baseBet
        if ((mAction == BetterAction.FLAT) ||
                ((mAction == BetterAction.SEQUENCE) && (mSequence == null))) {
            return mLastBet;
        }

        // for PROPORTIONAL better, simply return proportion of current bankroll
        if (mAction == BetterAction.PROPORTIONAL) {
            double bankroll = box.getBankroll();
            return (bankroll > 0.0 ? bankroll * mActionValue : mBaseBet);
        }

        // if lastResult is PUSH, return lastBet
        mLastResult = box.getLastRoundResult();
        if (mLastResult == BjService.RoundResult.PUSH) {
            return mLastBet;
        }

        if (isMetActionBase()) {
            onSuccessActionBase();
        } else {
            onFailActionBase();
        }

        return mProposedNextBet;
    }

    protected void onSuccessActionBase() {
        if (mAction == BetterAction.STRONG_MARTINGALE) {
            double winAmount = mBoxBasedOn.getCycleWin();
            mProposedNextBet = -winAmount + mBaseBet;   // all the loss + baseBet
        } else if (mAction == BetterAction.OSCAR) {
            double winAmount = mBoxBasedOn.getCycleWin();
            if (winAmount >= mBaseBet) {
                mBoxBasedOn.resetCycle();
                mProposedNextBet = mBaseBet;
            } else {
                mProposedNextBet = mLastBet + mActionValue * mBaseBet;
            }
        } else {
            int lastLevelCount = mBoxBasedOn.increaseLastCutLevelCount();
            if (lastLevelCount >= mMaxCutLevelCount) {
                mBoxBasedOn.resetCycle();
                mProposedNextBet = mBaseBet;
            } else {
                switch (mAction) {
                    default:    // FLAT & PROPORTIONAL case handled already
                        mProposedNextBet = mBoxBasedOn.getBaseBet();
                        break;

                    case MULTIPLY:
                        mProposedNextBet = mLastBet * mActionValue;
                        break;

                    case INCREASE:
                        mProposedNextBet = mLastBet + mActionValue * mBoxBasedOn.getBaseBet();
                        break;

                    case SEQUENCE:
                        mProposedNextBet = mSequence[mBoxBasedOn.getLastCutLevelCount()] *
                                mBoxBasedOn.getBaseBet();
                        break;
                }
            }
        }
    }

    protected void onFailActionBase() {
        if (mAction == BetterAction.OSCAR) {
            mProposedNextBet = mLastBet;
        } else {
            mBoxBasedOn.resetCycle();
            mProposedNextBet = mBaseBet;
        }
    }

    protected boolean isMetActionBase() {
        // for strong martingale, the cycle win amount is the prime factor
        if (mAction == BetterAction.STRONG_MARTINGALE) return (mBoxBasedOn.getCycleWin() < 0.0);

        // for OSCAR, action is based on WIN
        if (mAction == BetterAction.OSCAR) return (mLastResult == BjService.RoundResult.WIN);

        // for the rest, action base should meet last round result
        BetterActionBase playerResult = (mLastResult == BjService.RoundResult.WIN) ?
                BetterActionBase.WIN : BetterActionBase.LOST;

        return (playerResult == mBetterActionBase);
    }
}