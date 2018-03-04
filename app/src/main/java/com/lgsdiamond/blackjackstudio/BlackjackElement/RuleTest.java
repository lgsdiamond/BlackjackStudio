package com.lgsdiamond.blackjackstudio.BlackjackElement;

import android.content.res.XmlResourceParser;

import com.lgsdiamond.blackjackstudio.BlackjackUtils.UtilityStudio;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by lgsdiamond on 2015-10-02.
 */
public class RuleTest {
    // static members
    public static ArrayList<RuleTest> sRules = new ArrayList<>();

    // public members
    public enum RuleDoubledownOn {
        ANY_TWO_CARDS, ONLY_NINE_TEN_ELEVEN, ONLY_TEN_ELEVEN
    }

    public enum RuleSecondCardDeal {AT_ONCE, LATER}

    // key members
    String mTitle;
    String mDescription;
    DealerRule mDealerRule;
    DoubledownRule mDoubledownRule;
    SplitRule mSplitRule;

    public RuleTest() {
        mTitle = "Default Rule";
        mDescription = "created by code, not by XML";

        mDealerRule = new DealerRule();
        mDoubledownRule = new DoubledownRule();
        mSplitRule = new SplitRule();
    }

    abstract class BjRule {

    }

    class DealerRule extends BjRule {
        public int mCountDecks;
        public boolean hitOnSoft17;
        public boolean allowSurrender;
        public RuleSecondCardDeal ruleSecondCardDeal;
        public boolean allowPeekHole;
        public double blackjackPayout;

        public DealerRule() {
            mCountDecks = 6;
            hitOnSoft17 = false;
            allowSurrender = false;
            ruleSecondCardDeal = RuleSecondCardDeal.LATER;
            allowPeekHole = false;
            blackjackPayout = 1.5;
        }
    }

    class DoubledownRule extends BjRule {
        public boolean allowAfterSplit;
        public RuleDoubledownOn DoubledownOn;

        public DoubledownRule() {
            allowAfterSplit = true;
            DoubledownOn = RuleDoubledownOn.ANY_TWO_CARDS;
        }
    }

    @Override
    public String toString() {
        return mTitle + ": " + mDescription;
    }

    class SplitRule extends BjRule {
        public int maxCount;
        public boolean allowDifferent10;
        public boolean allowAceResplit;

        public SplitRule() {
            maxCount = 3;
            allowDifferent10 = true;
            allowAceResplit = false;
        }
    }

    //=== for parsing ===
    public static ArrayList<RuleTest> sRules_Test = null;


    private static final String TAG_BlackjackRules = "BlackjackRules";
    private static final String TAG_Title = "Title";
    private static final String TAG_Description = "Description";

    private static final String TAG_Rule = "Rule";

    private static final String TAG_RuleCategory = "RuleCategory";

    private static final String TAG_DeckCount = "DeckCount";
    private static final String TAG_HitOnSoft17 = "HitOnSoft17";
    private static final String TAG_AllowSurrender = "AllowSurrender";
    private static final String TAG_SecondCardDeal = "SecondCardDeal";
    private static final String TAG_AllowPeekHone = "AllowPeekHone";
    private static final String TAG_BlackjackPayout = "BlackjackPayout";

    private static final String TAG_DoubledownAfterSplit = "DoubledownAfterSplit";
    private static final String TAG_DoubledownOn = "DoubledownOn";

    private static final String TAG_MaxSplit = "MaxSplit";
    private static final String TAG_SplitDifferent10 = "SplitDifferent10";
    private static final String TAG_ResplitAce = "ResplitAce";

    private static ArrayList<RuleTest> readRules(XmlResourceParser parser) {
        ArrayList<RuleTest> rules = null;
        final String nameSpace = null;

        Boolean isSuccess = false;

        try {
            parser.require(XmlPullParser.START_TAG, nameSpace, TAG_BlackjackRules);
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        int eventType;
        String name;
        try {
            while (true) {
                if ((eventType = parser.next()) == XmlPullParser.END_DOCUMENT) break;

                if ((eventType == XmlPullParser.END_TAG)
                        && ((name = parser.getName()) == TAG_BlackjackRules)) break;

                if (eventType != XmlPullParser.START_TAG) continue;

                name = parser.getName();
                if (name.equals(TAG_BlackjackRules)) {
                    rules = new ArrayList<>();
                } else if (name.equals(TAG_Rule)) {
                    RuleTest oneRule = readOneRule(parser);
                    if (oneRule != null) {
                        rules.add(oneRule);
                    }
                }
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        UtilityStudio.toast("Rule Parsing: " + (isSuccess ? "SUCCESS" : "FAILED"));

        return rules;
    }

    private static RuleTest readOneRule(XmlResourceParser parser)
            throws IOException, XmlPullParserException {
        RuleTest rule = new RuleTest();

        String name, text;
        int eventType;
        while (true) {
            if ((eventType = parser.next()) == XmlPullParser.END_DOCUMENT) break;

            if ((eventType == XmlPullParser.END_TAG)
                    && ((name = parser.getName()).equals(TAG_Rule)))
                break;

            if (eventType != XmlPullParser.START_TAG) continue;
            name = parser.getName();

            if (parser.next() != XmlPullParser.TEXT) continue;
            text = parser.getText();

            switch (name) {
                // begins a rule
                case TAG_Rule:
                    break;

                case TAG_Title:
                    rule.mTitle = text;
                    break;

                case TAG_Description:
                    rule.mDescription = text;
                    break;

                // begins a category
                case TAG_RuleCategory:
                    break;

                // dealer rule
                case TAG_DeckCount:
                    rule.mDealerRule.mCountDecks = Integer.parseInt(text);
                    break;
                case TAG_HitOnSoft17:
                    rule.mDealerRule.hitOnSoft17 = text.equalsIgnoreCase("true");
                    break;
                case TAG_AllowSurrender:
                    rule.mDealerRule.allowSurrender = text.equalsIgnoreCase("true");
                    break;
                case TAG_SecondCardDeal:
                    rule.mDealerRule.ruleSecondCardDeal = text.equalsIgnoreCase("Later") ?
                            RuleSecondCardDeal.LATER : RuleSecondCardDeal.AT_ONCE;
                    break;
                case TAG_AllowPeekHone:
                    rule.mDealerRule.allowPeekHole = text.equalsIgnoreCase("true");
                    break;
                case TAG_BlackjackPayout:
                    rule.mDealerRule.blackjackPayout = text.equalsIgnoreCase("6to5") ?
                            1.5 : 1.2;
                    break;

                // doubledown rule
                case TAG_DoubledownAfterSplit:
                    rule.mDoubledownRule.allowAfterSplit = text.equalsIgnoreCase("true");
                    break;
                case TAG_DoubledownOn:
                    rule.mDoubledownRule.DoubledownOn = text.equalsIgnoreCase("9-10-11") ?
                            RuleDoubledownOn.ONLY_NINE_TEN_ELEVEN : (text.equalsIgnoreCase("10-11") ?
                            RuleDoubledownOn.ONLY_TEN_ELEVEN : RuleDoubledownOn.ANY_TWO_CARDS);
                    break;

                // split rule
                case TAG_MaxSplit:
                    rule.mSplitRule.maxCount = Integer.parseInt(text);
                    break;
                case TAG_SplitDifferent10:
                    rule.mSplitRule.allowDifferent10 = text.equalsIgnoreCase("true");
                    break;
                case TAG_ResplitAce:
                    rule.mSplitRule.allowAceResplit = text.equalsIgnoreCase("true");
                    break;

                default:
                    break;
            }
        }

        return rule;
    }

    public static void loadRules(XmlResourceParser parser) {
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

        sRules_Test = readRules(parser);

        parser.close(); // close resource
    }
}
