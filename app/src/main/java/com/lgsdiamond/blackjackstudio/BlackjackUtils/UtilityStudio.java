package com.lgsdiamond.blackjackstudio.BlackjackUtils;

import android.content.res.Resources;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.SoundPool;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.lgsdiamond.blackjackstudio.ActivityStudio;
import com.lgsdiamond.blackjackstudio.BlackjackElement.BjService;
import com.lgsdiamond.blackjackstudio.BlackjackElement.Card;
import com.lgsdiamond.blackjackstudio.BlackjackElement.Hand;
import com.lgsdiamond.blackjackstudio.BlackjackElement.PlayerHand;
import com.lgsdiamond.blackjackstudio.BlackjackElement.Rule;
import com.lgsdiamond.blackjackstudio.BuildConfig;
import com.lgsdiamond.blackjackstudio.FragmentCounting;
import com.lgsdiamond.blackjackstudio.R;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Random;

/**
 * Created by lgsdiamond on 2015-11-12.
 */
public class UtilityStudio {
    public static ActivityStudio sActivity;

    private static final Locale unitedStates = new Locale("us", "US");
    private static final NumberFormat usFormat = NumberFormat.getCurrencyInstance(unitedStates);
    private static Rule sRule;

    // random number generation for general use, no seed
    public static Random sRandom = new Random();

    // call this in the beginning of activity
    public static void preInitialize(ActivityStudio activity) {
        sActivity = activity;

        loadImages();
        loadColors();
        loadSounds();
    }

    public static void postInitialize(ActivityStudio activity) {
        sRule = sActivity.getTableRule();
    }


    public static String getUsCurrency(double value) {
        String currencyStr = usFormat.format(value);
        currencyStr = currencyStr.replace("US", "");
        return (currencyStr);
    }

    public static String getUsCurrencyWithNoCents(double value) {
        return getUsCurrency(value).replace(".00", "");
    }

    private static final String LGSTAG = "lgsdiamond";

    public static void LogD(String comment) {
        if (BuildConfig.DEBUG) Log.i(LGSTAG, comment);
    }

    public static void toast(String msg) {
        if (BjService.isAutoRunning()) return;
        Toast.makeText(sActivity, msg, Toast.LENGTH_SHORT).show();
    }

    // Image
    public static final int[] sImage_CardFronts =
            new int[Card.COUNT_CARD_IN_DECK];   // total 52 cards
    public static int sImage_CardBack;

    private static void loadImages() {
        String[] cardImages = sActivity.getResources().getStringArray(R.array.card_images);
        String cardBackImage = sActivity.getResources().getString(R.string.card_back_image);

        // loading and passing card image ids
        int index = 0;
        try {
            for (String name : cardImages) {
                sImage_CardFronts[index] =
                        sActivity.getResources().getIdentifier(name, "drawable",
                                sActivity.getPackageName());
                index++;
            }
            sImage_CardBack =
                    sActivity.getResources().getIdentifier(cardBackImage, "drawable",
                            sActivity.getPackageName());
        } catch (Exception e) {
            e.printStackTrace();
            LogD("Card Image was not found![" + index + "]");
        }
    }

    public static int getCardImage(Card card) {
        return card.isHidden() ? sImage_CardBack :
                sImage_CardFronts[card.getOrder()];
    }

    // Colors for table fragment
    public static int sColor_Blackjack;
    public static int sColor_Lost;
    public static int sColor_Win;
    public static int sColor_Unconfirmed;
    public static int sColor_Confirmed;
    public static int sColor_DealerNormal;
    public static int sColor_PlayerNormal;
    public static int sColor_PlayerWin;
    public static int sColor_PlayerLost;
    public static int sColor_PlayerPush;

    public static int sColor_HandHighlight;
    public static int sColor_BestAction;
    public static int sColor_NormalAction;

    // Colors for counting fragment
    public static int sColor_LowCard, sColor_MediumCard, sColor_HighCard, sColor_AceCard,
            sColor_Minus, sColor_Plus,
            sColor_Good, sColor_Bad;

    public static int sColor_TitleButton, sColor_ReferenceButton;


    private static void loadColors() {
        sColor_Blackjack = ContextCompat.getColor(sActivity, R.color.colorBlackjack);
        sColor_Lost = ContextCompat.getColor(sActivity, R.color.colorLost);
        sColor_Win = ContextCompat.getColor(sActivity, R.color.colorWin);
        sColor_Unconfirmed = ContextCompat.getColor(sActivity, R.color.colorUnconfirmed);
        sColor_Confirmed = ContextCompat.getColor(sActivity, R.color.colorConfirmed);

        sColor_DealerNormal = ContextCompat.getColor(sActivity, R.color.colorDealerNormal);

        sColor_PlayerNormal = ContextCompat.getColor(sActivity, R.color.colorPlayerNormal);
        sColor_PlayerWin = ContextCompat.getColor(sActivity, R.color.colorPlayerWin);
        sColor_PlayerLost = ContextCompat.getColor(sActivity, R.color.colorPlayerLost);
        sColor_PlayerPush = ContextCompat.getColor(sActivity, R.color.colorPlayerPush);

        sColor_HandHighlight = ContextCompat.getColor(sActivity, R.color.colorHandHighlight);

        sColor_BestAction = ContextCompat.getColor(sActivity, R.color.colorBestAction);
        sColor_NormalAction = ContextCompat.getColor(sActivity, R.color.colorNormalAction);

        // for counting fragment
        sColor_LowCard = ContextCompat.getColor(sActivity, R.color.colorLowCard);
        sColor_MediumCard = ContextCompat.getColor(sActivity, R.color.colorMediumCard);
        sColor_HighCard = ContextCompat.getColor(sActivity, R.color.colorHighCard);
        sColor_AceCard = ContextCompat.getColor(sActivity, R.color.colorAceCard);
        sColor_Minus = ContextCompat.getColor(sActivity, R.color.colorMinus);
        sColor_Plus = ContextCompat.getColor(sActivity, R.color.colorPlus);

        sColor_Good = ContextCompat.getColor(sActivity, R.color.colorGood);
        sColor_Bad = ContextCompat.getColor(sActivity, R.color.colorBad);

        sColor_TitleButton = ContextCompat.getColor(sActivity, R.color.colorTitleButton);
        sColor_ReferenceButton = ContextCompat.getColor(sActivity, R.color.colorReferenceButton);
    }

    // Sound
    public static final int NO_SOUND_RESOURCE = -1;

    public static SoundPool sBjSoundPool;
    public static int sSound_RoundOver;
    public static int sSound_StartRound;

    public static int sSound_Blackjack;
    public static int sSound_DealerBlackjack;
    public static int sSound_Shuffle;
    public static int sSound_Deal;

    public static int sSound_ThankYou;
    public static int sSound_Insurance;
    public static int sSound_GoodLuck;

    public static int sSound_Chip_On;
    public static int sSound_Hand_3, sSound_Hand_4, sSound_Hand_5, sSound_Hand_6,
            sSound_Hand_7, sSound_Hand_8, sSound_Hand_9, sSound_Hand_10,
            sSound_Hand_11, sSound_Hand_12, sSound_Hand_13, sSound_Hand_14, sSound_Hand_15,
            sSound_Hand_16, sSound_Hand_17, sSound_Hand_18, sSound_Hand_19, sSound_Hand_20,
            sSound_Hand_21;
    public static int sSound_Hand_Soft12, sSound_Hand_Soft13, sSound_Hand_Soft14, sSound_Hand_Soft15,
            sSound_Hand_Soft16, sSound_Hand_Soft17, sSound_Hand_Soft18, sSound_Hand_Soft19,
            sSound_Hand_Soft20;


    public static final int COUNT_DEALER_BUST_SOUND = 2;
    public static int[] sSound_Dealer_Bust = new int[COUNT_DEALER_BUST_SOUND];

    public static final int COUNT_BLACKJACK_SOUND = 2;
    public static int[] sSound_Hand_Blackjack = new int[COUNT_BLACKJACK_SOUND];

    public static final int COUNT_TOO_MANY_SOUND = 2;
    public static int[] sSound_Too_Many = new int[COUNT_TOO_MANY_SOUND];

    public static final int COUNT_HIT_SOUND = 4;
    public static int[] sSound_Hit = new int[COUNT_HIT_SOUND];

    public static final int COUNT_DOUBLE_SOUND = 3;
    public static int[] sSound_Double = new int[COUNT_DOUBLE_SOUND];

    public static final int COUNT_STAND_SOUND = 3;
    public static int[] sSound_Stand = new int[COUNT_STAND_SOUND];

    public static final int COUNT_SPLIT_SOUND = 2;
    public static int[] sSound_Split = new int[COUNT_SPLIT_SOUND];

    public static int sSound_Surrender;

    public static int sSound_Dealer_Ace, sSound_Dealer_2, sSound_Dealer_3, sSound_Dealer_4,
            sSound_Dealer_5, sSound_Dealer_6, sSound_Dealer_7, sSound_Dealer_8, sSound_Dealer_9,
            sSound_Dealer_10, sSound_Dealer_Jack, sSound_Dealer_Queen, sSound_Dealer_King;


    private static void loadSounds() {
/*
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_GAME)
                .build();

        sBjSoundPool = new SoundPool.Builder()
                .setMaxStreams(2)
                .setAudioAttributes(audioAttributes)
                .build();
*/
        sBjSoundPool = new SoundPool(2, AudioManager.STREAM_MUSIC, 0);

        sSound_Blackjack = sBjSoundPool.load(sActivity, R.raw.milton_blackjack, 1);
        sSound_DealerBlackjack = sBjSoundPool.load(sActivity, R.raw.milton_dealer_blackjack, 1);
        sSound_Shuffle = sBjSoundPool.load(sActivity, R.raw.milton_shuffle, 1);
        sSound_Deal = sBjSoundPool.load(sActivity, R.raw.deal, 1);
        sSound_ThankYou = sBjSoundPool.load(sActivity, R.raw.milton_thank_you, 1);
        sSound_Insurance = sBjSoundPool.load(sActivity, R.raw.milton_insurance, 1);
        sSound_GoodLuck = sBjSoundPool.load(sActivity, R.raw.goodluck_female, 1);

        sSound_RoundOver = sBjSoundPool.load(sActivity, R.raw.round_over, 1);
        sSound_StartRound = sBjSoundPool.load(sActivity, R.raw.start_round, 1);

        sSound_Chip_On = sBjSoundPool.load(sActivity, R.raw.chip_on, 1);

        sSound_Too_Many[0] = sBjSoundPool.load(sActivity, R.raw.too_many_female_1, 1);
        sSound_Too_Many[1] = sBjSoundPool.load(sActivity, R.raw.too_many_female_2, 1);
        // Action Sound

        sSound_Hit[0] = sBjSoundPool.load(sActivity, R.raw.hit_male_1, 1);
        sSound_Hit[1] = sBjSoundPool.load(sActivity, R.raw.hit_male_2, 1);
        sSound_Hit[2] = sBjSoundPool.load(sActivity, R.raw.hit_male_3, 1);
        sSound_Hit[3] = sBjSoundPool.load(sActivity, R.raw.hit_male_4, 1);

        sSound_Stand[0] = sBjSoundPool.load(sActivity, R.raw.stand_male_1, 1);
        sSound_Stand[1] = sBjSoundPool.load(sActivity, R.raw.stand_male_2, 1);
        sSound_Stand[2] = sBjSoundPool.load(sActivity, R.raw.stand_male_3, 1);

        sSound_Double[0] = sBjSoundPool.load(sActivity, R.raw.double_male_1, 1);
        sSound_Double[1] = sBjSoundPool.load(sActivity, R.raw.double_male_2, 1);
        sSound_Double[2] = sBjSoundPool.load(sActivity, R.raw.double_male_3, 1);

        sSound_Split[0] = sBjSoundPool.load(sActivity, R.raw.split_male_1, 1);
        sSound_Split[1] = sBjSoundPool.load(sActivity, R.raw.split_male_2, 1);

        sSound_Surrender = sBjSoundPool.load(sActivity, R.raw.surrender_male, 1);

        // Hand Sound
        sSound_Hand_3 = sBjSoundPool.load(sActivity, R.raw.hand_3_female, 1);
        sSound_Hand_4 = sBjSoundPool.load(sActivity, R.raw.hand_4_female, 1);
        sSound_Hand_5 = sBjSoundPool.load(sActivity, R.raw.hand_5_female, 1);
        sSound_Hand_6 = sBjSoundPool.load(sActivity, R.raw.hand_6_female, 1);
        sSound_Hand_7 = sBjSoundPool.load(sActivity, R.raw.hand_7_female, 1);
        sSound_Hand_8 = sBjSoundPool.load(sActivity, R.raw.hand_8_female, 1);
        sSound_Hand_9 = sBjSoundPool.load(sActivity, R.raw.hand_9_female, 1);
        sSound_Hand_10 = sBjSoundPool.load(sActivity, R.raw.hand_10_female, 1);
        sSound_Hand_11 = sBjSoundPool.load(sActivity, R.raw.hand_11_female, 1);
        sSound_Hand_12 = sBjSoundPool.load(sActivity, R.raw.hand_12_female, 1);
        sSound_Hand_13 = sBjSoundPool.load(sActivity, R.raw.hand_13_female, 1);
        sSound_Hand_14 = sBjSoundPool.load(sActivity, R.raw.hand_14_female, 1);
        sSound_Hand_15 = sBjSoundPool.load(sActivity, R.raw.hand_15_female, 1);
        sSound_Hand_16 = sBjSoundPool.load(sActivity, R.raw.hand_16_female, 1);
        sSound_Hand_17 = sBjSoundPool.load(sActivity, R.raw.hand_17_female, 1);
        sSound_Hand_18 = sBjSoundPool.load(sActivity, R.raw.hand_18_female, 1);
        sSound_Hand_19 = sBjSoundPool.load(sActivity, R.raw.hand_19_female, 1);
        sSound_Hand_20 = sBjSoundPool.load(sActivity, R.raw.hand_20_female, 1);

        sSound_Hand_Blackjack[0] = sBjSoundPool.load(sActivity, R.raw.hand_blackjack_female_1, 1);
        sSound_Hand_Blackjack[1] = sBjSoundPool.load(sActivity, R.raw.hand_blackjack_female_2, 1);

        sSound_Hand_21 = sBjSoundPool.load(sActivity, R.raw.hand_21_female, 1);

        sSound_Hand_Soft12 = sBjSoundPool.load(sActivity, R.raw.hand_soft12_female, 1);
        sSound_Hand_Soft13 = sBjSoundPool.load(sActivity, R.raw.hand_soft13_female, 1);
        sSound_Hand_Soft14 = sBjSoundPool.load(sActivity, R.raw.hand_soft14_female, 1);
        sSound_Hand_Soft15 = sBjSoundPool.load(sActivity, R.raw.hand_soft15_female, 1);
        sSound_Hand_Soft16 = sBjSoundPool.load(sActivity, R.raw.hand_soft16_female, 1);
        sSound_Hand_Soft17 = sBjSoundPool.load(sActivity, R.raw.hand_soft17_female, 1);
        sSound_Hand_Soft18 = sBjSoundPool.load(sActivity, R.raw.hand_soft18_female, 1);
        sSound_Hand_Soft19 = sBjSoundPool.load(sActivity, R.raw.hand_soft19_female, 1);
        sSound_Hand_Soft20 = sBjSoundPool.load(sActivity, R.raw.hand_soft20_female, 1);

        // dealer show--
        sSound_Dealer_Ace = sBjSoundPool.load(sActivity, R.raw.dealer_ace_female, 1);
        sSound_Dealer_2 = sBjSoundPool.load(sActivity, R.raw.dealer_2_female, 1);
        sSound_Dealer_3 = sBjSoundPool.load(sActivity, R.raw.dealer_3_female, 1);
        sSound_Dealer_4 = sBjSoundPool.load(sActivity, R.raw.dealer_4_female, 1);
        sSound_Dealer_5 = sBjSoundPool.load(sActivity, R.raw.dealer_5_female, 1);
        sSound_Dealer_6 = sBjSoundPool.load(sActivity, R.raw.dealer_6_female, 1);
        sSound_Dealer_7 = sBjSoundPool.load(sActivity, R.raw.dealer_7_female, 1);
        sSound_Dealer_8 = sBjSoundPool.load(sActivity, R.raw.dealer_8_female, 1);
        sSound_Dealer_9 = sBjSoundPool.load(sActivity, R.raw.dealer_9_female, 1);
        sSound_Dealer_10 = sBjSoundPool.load(sActivity, R.raw.dealer_10_female, 1);
        sSound_Dealer_Jack = sBjSoundPool.load(sActivity, R.raw.dealer_jack_female, 1);
        sSound_Dealer_Queen = sBjSoundPool.load(sActivity, R.raw.dealer_queen_female, 1);
        sSound_Dealer_King = sBjSoundPool.load(sActivity, R.raw.dealer_king_female, 1);

        sSound_Dealer_Bust[0] = sBjSoundPool.load(sActivity, R.raw.dealer_bust_male_1, 1);
        sSound_Dealer_Bust[1] = sBjSoundPool.load(sActivity, R.raw.dealer_bust_male_2, 1);
    }

    public static void playSound(int id) {
        if (!sRule.mUseSound || BjService.isAutoRunning() || (id == NO_SOUND_RESOURCE)) return;

        sBjSoundPool.play(id, 1.0f, 1.0f, 0, 0, 1);
    }

    public static int getResourceId(Resources resources, String pVariableName, String pResourceName,
                                    String pPackageName) {
        try {
            return resources.getIdentifier(pVariableName, pResourceName, pPackageName);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    // Favor Normalization Factor
    public static final double NORMALIZE_RANK = 1.0;            // 24 out of 320
    public static final double NORMALIZE_LOHI = 0.2;            // 120 out of 320
    public static final double NORMALIZE_RUN_COUNT = 30.0;        // range 0~30
    public static final double NORMALIZE_TRUE_COUNT = 5.0;      // range 0~5

    public static void setTextViewColorByValue(TextView view, double value,
                                               double normalizeFactor, FragmentCounting.PlayerFavor favor) {
        value = value / normalizeFactor;

        int colorEnhance = (int) (255 * value);
        if (colorEnhance < 0) colorEnhance = -colorEnhance;
        if (colorEnhance > 255) colorEnhance = 255;
        colorEnhance = 255 - colorEnhance;
        String hex = Integer.toHexString(colorEnhance);
        if (hex.length() == 1) hex = "0" + hex;

        String colorStr;
        if (favor == FragmentCounting.PlayerFavor.NONE) {
            colorStr = "#ffffff";                   // pure white
        } else if (((value >= 0.0) && (favor == FragmentCounting.PlayerFavor.FAVOR))
                || ((value < 0.0) && (favor == FragmentCounting.PlayerFavor.ANTI_FAVOR))) {
            colorStr = "#" + hex + "ff" + hex;      // green enhanced
        } else {
            colorStr = "#ff" + hex + hex;           // red enhanced
        }

        view.setBackgroundColor(Color.parseColor(colorStr));
        view.setTextColor((value >= 0) ? sColor_Plus : sColor_Minus);
    }

    public static DecimalFormat sDecimalFormat = null;

    public static String doubleToDotDecimal(double value, int dotDigits) {
        String formatString = (dotDigits > 0) ? "###,###,##0." : "###,###,##0";
        while (dotDigits-- > 0) formatString = formatString + "0";
        if (sDecimalFormat == null) sDecimalFormat = new DecimalFormat();
        sDecimalFormat.applyPattern(formatString);

        return sDecimalFormat.format(value);
    }

    private static final long SLEEP_SECOND_TIME = 500;

    public static void sleepMoment() {
        try {
            Thread.sleep(SLEEP_SECOND_TIME); // wait just a second
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void takeThreadPause(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void playBustSound() {
        if (BjService.isAutoRunning() || !sRule.mUseSound) return;

        int id = sRandom.nextInt(COUNT_TOO_MANY_SOUND);
        playSound(sSound_Too_Many[id]);
        takeThreadPause(1000);
    }

    public static void playDealerBlackjackSound() {
        if (BjService.isAutoRunning() || !sRule.mUseSound) return;

        int id = sRandom.nextInt(COUNT_BLACKJACK_SOUND);
        playSound(sSound_Hand_Blackjack[id]);
        takeThreadPause(1000);
    }

    public static void playBlackjackSound() {
        if (BjService.isAutoRunning() || !sRule.mUseSound) return;

        int id = sRandom.nextInt(COUNT_BLACKJACK_SOUND);
        playSound(sSound_Hand_Blackjack[id]);
        takeThreadPause(1000);
    }

    public static void playDealerBustSound() {
        if (BjService.isAutoRunning() || !sRule.mUseSound) return;

        int id = sRandom.nextInt(COUNT_DEALER_BUST_SOUND);
        playSound(sSound_Dealer_Bust[id]);
        takeThreadPause(1000);
    }

    public static void playTwentyOneSound() {
        if (BjService.isAutoRunning() || !sRule.mUseSound) return;

        playSound(sSound_Hand_21);
        takeThreadPause(1000);
    }

    public static void playSoftScoreSound(int score) {
        if (BjService.isAutoRunning() || !sRule.mUseSound) return;

        int id = NO_SOUND_RESOURCE;
        switch (score) {
            case 12:
                id = sSound_Hand_Soft12;
                break;
            case 13:
                id = sSound_Hand_Soft13;
                break;
            case 14:
                id = sSound_Hand_Soft14;
                break;
            case 15:
                id = sSound_Hand_Soft15;
                break;
            case 16:
                id = sSound_Hand_Soft16;
                break;
            case 17:
                id = sSound_Hand_Soft17;
                break;
            case 18:
                id = sSound_Hand_Soft18;
                break;
            case 19:
                id = sSound_Hand_Soft19;
                break;
            case 20:
                id = sSound_Hand_Soft20;
                break;
        }
        playSound(id);
        takeThreadPause(1000);
    }


    public static void playHardScoreSound(int score) {
        if (BjService.isAutoRunning() || !sRule.mUseSound) return;

        int id = NO_SOUND_RESOURCE;
        switch (score) {
            case 4:
                id = sSound_Hand_4;
                break;
            case 5:
                id = sSound_Hand_5;
                break;
            case 6:
                id = sSound_Hand_6;
                break;
            case 7:
                id = sSound_Hand_7;
                break;
            case 8:
                id = sSound_Hand_8;
                break;
            case 9:
                id = sSound_Hand_9;
                break;
            case 10:
                id = sSound_Hand_10;
                break;
            case 11:
                id = sSound_Hand_11;
                break;
            case 12:
                id = sSound_Hand_12;
                break;
            case 13:
                id = sSound_Hand_13;
                break;
            case 14:
                id = sSound_Hand_14;
                break;
            case 15:
                id = sSound_Hand_15;
                break;
            case 16:
                id = sSound_Hand_16;
                break;
            case 17:
                id = sSound_Hand_17;
                break;
            case 18:
                id = sSound_Hand_18;
                break;
            case 19:
                id = sSound_Hand_19;
                break;
            case 20:
                id = sSound_Hand_20;
                break;
        }
        playSound(id);
        takeThreadPause(1000);
    }

    public static void playDealingHandSound(Hand hand) {
        if (BjService.isAutoRunning() || !sRule.mUseSound) return;

        if (hand.isBlackjack()) {
            playBlackjackSound();
        } else if (hand.getScore() == 21) {
            playTwentyOneSound();
        } else if (hand.isSoft()) {
            playSoftScoreSound(hand.getScore());
        } else {
            playHardScoreSound(hand.getScore());
        }
    }

    public static void playPlayerActionSound(PlayerHand.Action action) {
        if (BjService.isAutoRunning() || !sRule.mUseSound) return;

        int soundIndex;
        int id = NO_SOUND_RESOURCE;

        switch (action) {
            case UNKNOWN:
                break;

            case HIT:
                soundIndex = sRandom.nextInt(COUNT_HIT_SOUND);
                id = sSound_Hit[soundIndex];
                break;

            case STAND:
                soundIndex = sRandom.nextInt(COUNT_STAND_SOUND);
                id = sSound_Stand[soundIndex];
                break;

            case SPLIT:
                soundIndex = sRandom.nextInt(COUNT_SPLIT_SOUND);
                id = sSound_Split[soundIndex];
                break;

            case DOUBLEDOWN:
                soundIndex = sRandom.nextInt(COUNT_DOUBLE_SOUND);
                id = sSound_Double[soundIndex];
                ActivityStudio.sStudioHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        playSound(sSound_GoodLuck);
                    }
                }, 750);
                break;

            case SURRENDER_OR_HIT:
            case SURRENDER_OR_STAND:
                id = sSound_Surrender;
                break;
        }
        playSound(id);
        takeThreadPause(1000);
    }

    public static void playDealerShowRankSound(Card.CardRank rank) {
        if (BjService.isAutoRunning() || !sRule.mUseSound) return;

        int id = NO_SOUND_RESOURCE;

        switch (rank) {
            case ACE:
                id = sSound_Dealer_Ace;
                break;
            case TWO:
                id = sSound_Dealer_2;
                break;
            case THREE:
                id = sSound_Dealer_3;
                break;
            case FOUR:
                id = sSound_Dealer_4;
                break;
            case FIVE:
                id = sSound_Dealer_5;
                break;
            case SIX:
                id = sSound_Dealer_6;
                break;
            case SEVEN:
                id = sSound_Dealer_7;
                break;
            case EIGHT:
                id = sSound_Dealer_8;
                break;
            case NINE:
                id = sSound_Dealer_9;
                break;
            case TEN:
                id = sSound_Dealer_10;
                break;
            case JACK:
                id = sSound_Dealer_Jack;
                break;
            case QUEEN:
                id = sSound_Dealer_Queen;
                break;
            case KING:
                id = sSound_Dealer_King;
                break;
        }
        playSound(id);
        takeThreadPause(1000);
    }
}
