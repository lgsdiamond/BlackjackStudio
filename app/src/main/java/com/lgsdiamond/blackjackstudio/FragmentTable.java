package com.lgsdiamond.blackjackstudio;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.GestureDetectorCompat;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lgsdiamond.blackjackstudio.BlackjackElement.Better;
import com.lgsdiamond.blackjackstudio.BlackjackElement.BettingBox;
import com.lgsdiamond.blackjackstudio.BlackjackElement.BjService;
import com.lgsdiamond.blackjackstudio.BlackjackElement.Card;
import com.lgsdiamond.blackjackstudio.BlackjackElement.DealerHand;
import com.lgsdiamond.blackjackstudio.BlackjackElement.GameData;
import com.lgsdiamond.blackjackstudio.BlackjackElement.Hand;
import com.lgsdiamond.blackjackstudio.BlackjackElement.Player;
import com.lgsdiamond.blackjackstudio.BlackjackElement.PlayerHand;
import com.lgsdiamond.blackjackstudio.BlackjackElement.Rule;
import com.lgsdiamond.blackjackstudio.BlackjackElement.Strategy;
import com.lgsdiamond.blackjackstudio.BlackjackUtils.UtilityStudio;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class FragmentTable extends FragmentStudioBase implements View.OnLongClickListener,
        AdapterView.OnItemClickListener, View.OnTouchListener {

    BjService mService;

    public static FragmentTable newInstance(BjService service) {
        FragmentTable fragment = new FragmentTable();
        FragmentStudioBase.newInstance(fragment, R.layout.fragment_table);

        fragment.setService(service);
        fragment.setPreserve(true);

        return fragment;
    }

    FragmentCounting mCounting = null;

    public void setFragmentCounting(FragmentCounting counting) {
        mCounting = counting;
    }

    private TextView tvLabelShoeCount;
    private ImageView ivDealerPicture, ivPlayerPicture;
    private ListView lvHands;
    private LinearLayout layoutDealer, layoutPlayButton, layoutPlayerOption;
    private FrameLayout layoutLine;
    private RelativeLayout layoutTable, layoutBetButton, layoutPlayer, layoutShoe;

    @Override
    protected void initializeViews() {
        if (mTopContainer == null) return;

        // for UI message handling
        sTableHandler = new TableHandler(FragmentTable.this);

        // Views
        tvLabelShoeCount = (TextView) findViewById(R.id.tvLabelShoeCount);

        // ImagesView
        ivDealerPicture = (ImageView) findViewById(R.id.ivDealerPicture);
        ivDealerPicture.setOnLongClickListener(FragmentTable.this);
        ivDealerPicture.setOnClickListener(FragmentTable.this);

        ivPlayerPicture = (ImageView) findViewById(R.id.ivPlayerPicture);
        ivPlayerPicture.setOnClickListener(FragmentTable.this);
        ivPlayerPicture.setOnLongClickListener(FragmentTable.this);

        // Layout
        layoutTable = (RelativeLayout) findViewById(R.id.loTable);
        layoutDealer = (LinearLayout) findViewById(R.id.loDealer);
        layoutLine = (FrameLayout) findViewById(R.id.fvLine);
        layoutBetButton = (RelativeLayout) findViewById(R.id.loBetButton);
        layoutPlayButton = (LinearLayout) findViewById(R.id.loPlayButton);
        layoutPlayer = (RelativeLayout) findViewById(R.id.loPlayer);
        layoutShoe = (RelativeLayout) findViewById(R.id.loShoe);
        layoutPlayerOption = (LinearLayout) findViewById(R.id.loPlayerOption);

        // setting for dynamic card image view creation
        ImageView ivFirst = (ImageView) layoutDealer.findViewById(R.id.ivRowCard0);
        ImageView ivSecond = (ImageView) layoutDealer.findViewById(R.id.ivRowCard1);

        FrameLayout.LayoutParams firstParams = (FrameLayout.LayoutParams) ivFirst.getLayoutParams();
        FrameLayout.LayoutParams secondParams = (FrameLayout.LayoutParams) ivSecond
                .getLayoutParams();
        FrameLayout.LayoutParams defaultParams = new FrameLayout.LayoutParams(secondParams);

        defaultParams.setMarginStart(firstParams.getMarginStart());
        int startGap = secondParams.leftMargin - firstParams.leftMargin;
        int topGap = secondParams.topMargin - firstParams.topMargin;

        // data handler's parameters
        setCardImageViewParams(defaultParams, startGap, topGap);

        // Hand ListView and Adapter
        lvHands = (ListView) findViewById(R.id.lvHands);

        // Hand ListView and Adapter
        mHandAdapter = new HandAdapter(getActivity(), R.layout.row_table_hand,
                mService, layoutDealer, lvHands);
        lvHands.setAdapter(mHandAdapter);
        lvHands.setOnItemClickListener(FragmentTable.this);

        // TextData to populate
        BjService.sGameData = new GameData(this, mService);

        // buttons
        btnHit = (Button) findViewById(R.id.btnHit);
        btnHit.setOnClickListener(this);
        btnStand = (Button) findViewById(R.id.btnStand);
        btnStand.setOnClickListener(this);
        btnSplit = (Button) findViewById(R.id.btnSplit);
        btnSplit.setOnClickListener(this);
        btnDoubledown = (Button) findViewById(R.id.btnDoubledown);
        btnDoubledown.setOnClickListener(this);
        btnSurrender = (Button) findViewById(R.id.btnSurrender);
        btnSurrender.setOnClickListener(this);

        btnBet0001 = (Button) findViewById(R.id.btnBet0001);
        btnBet0001.setOnClickListener(this);
        btnBet0005 = (Button) findViewById(R.id.btnBet0005);
        btnBet0005.setOnClickListener(this);
        btnBet0025 = (Button) findViewById(R.id.btnBet0025);
        btnBet0025.setOnClickListener(this);
        btnBet0100 = (Button) findViewById(R.id.btnBet0100);
        btnBet0100.setOnClickListener(this);
        btnBet0250 = (Button) findViewById(R.id.btnBet0250);
        btnBet0250.setOnClickListener(this);
        btnBet1000 = (Button) findViewById(R.id.btnBet1000);
        btnBet1000.setOnClickListener(this);
        btnBet1000.setOnClickListener(this);

        btnBetReset = (Button) findViewById(R.id.btnBetReset);
        btnBetReset.setOnClickListener(this);
        btnBetConfirm = (Button) findViewById(R.id.btnBetConfirm);
        btnBetConfirm.setOnClickListener(this);

        btnDeal = (Button) findViewById(R.id.btnDeal);
        btnDeal.setOnClickListener(this);

        btnStartRound = (Button) findViewById(R.id.btnStartRound);
        btnStartRound.setOnClickListener(this);

        btnReBet = (Button) findViewById(R.id.btnReBet);
        btnReBet.setOnClickListener(this);

        btnSetting = (Button) findViewById(R.id.btnIncreaseBankroll);
        btnSetting.setOnClickListener(this);

        btnSeedChange = (Button) findViewById(R.id.btnSeedChange);
        btnSeedChange.setOnClickListener(this);
        setText_btnSeedChange();

        // initial view adjustment
        layoutBetButton.setVisibility(View.GONE);
        layoutPlayButton.setVisibility(View.GONE);

        btnStartRound.setVisibility(View.GONE);

        // gesture listener for layoutPlayer
        layoutPlayer.setOnTouchListener(this);

        ActionDetector actionDetector = new ActionDetector();
        mGestureScanner = new GestureDetectorCompat(getActivity(), actionDetector);
        mGestureScanner.setOnDoubleTapListener(actionDetector);
    }

    GestureDetectorCompat mGestureScanner;
    boolean mListeningActionGesture = false;
    boolean mSplitActionDetected = false;

    private boolean handleActionGesture(MotionEvent event) {
        int pointerCount = event.getPointerCount();
        int action = event.getActionMasked();

        if ((pointerCount == 2) && (action == MotionEvent.ACTION_POINTER_DOWN)) {
            mSplitActionDetected = true;
        } else if ((pointerCount == 3) && (action == MotionEvent.ACTION_POINTER_DOWN)) {
            mSplitActionDetected = false;
        } else if (mSplitActionDetected && (pointerCount == 2) && (action == MotionEvent.ACTION_POINTER_UP)) {
            mSplitActionDetected = false;
            sendUiMessage(UI_MESSAGE.GESTURE_SPLIT);
            return true;
        }
        return mGestureScanner.onTouchEvent(event);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (v.getId()) {
            case R.id.loPlayer:
                return handleActionGesture(event);
        }
        return false;
    }

    public void notifyLastCardDeal(Hand hand, Card card) {      // card not added to hand yet
        mHandAdapter.setCardDealt(hand, card);
        scrollToHand(hand);

        // run card counting
        if (mCounting != null) {
            mCounting.drawRankFromShoe(mCounting.rankConvert(card.getRank()), false);
        }
    }

    public void notifySplitHand(PlayerHand splitHand, Card card) {      // card not added to hand yet
        mHandAdapter.setCardDealt(splitHand, card);
    }

    public class ActionDetector extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {      // need  this, return true
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (!mListeningActionGesture) return false;

            float VALID_RATIO = 0.5f;
            float ratio = Math.abs(velocityY / velocityX);
            if (ratio < VALID_RATIO) {
                sendUiMessage(UI_MESSAGE.GESTURE_STAND);
                return true;
            }
            if (velocityY > 0.0) {
                ratio = Math.abs(velocityX / velocityY);
                if (ratio < VALID_RATIO) {
                    sendUiMessage(UI_MESSAGE.GESTURE_HIT);
                    return true;
                }
            }
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            if (!mListeningActionGesture) return;
            sendUiMessage(UI_MESSAGE.GESTURE_SURRENDER);
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if (!mListeningActionGesture) return false;
            sendUiMessage(UI_MESSAGE.GESTURE_DOUBLEDOWN);
            return true;        // I want this
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            sendUiMessage(UI_MESSAGE.GESTURE_CONTINUE_DEFAULT);
            return true;        // I want this
        }
    }

    private void setService(BjService service) {
        mService = service;
    }

    public Rule getGameRule() {
        return mService.pGameRule;
    }

    private Button btnHit, btnStand, btnSplit, btnDoubledown, btnSurrender;
    private Button btnBet0001, btnBet0005, btnBet0025, btnBet0100, btnBet0250, btnBet1000;
    private Button btnBetReset, btnBetConfirm, btnDeal;
    private Button btnStartRound, btnReBet, btnSetting, btnSeedChange;

    @Override
    protected void setPrivateTag() {
        privateTag = "TABLE";
    }

    //=== logging ===
    private HandAdapter mHandAdapter;

    // Handler
    public static TableHandler sTableHandler;

    public void getShoeLocationOnScreen(int[] shoeLocation) {
        tvLabelShoeCount.getLocationOnScreen(shoeLocation);
        shoeLocation[0] += tvLabelShoeCount.getWidth();
        shoeLocation[1] += tvLabelShoeCount.getHeight();
    }

    private static class TableHandler extends Handler {
        private final WeakReference<FragmentTable> mTable;

        public TableHandler(FragmentTable table) {
            mTable = new WeakReference<>(table);
        }

        @Override
        public void handleMessage(Message msg) {
            FragmentTable table = mTable.get();
            if (table != null) {
                switch (UI_MESSAGE.values()[msg.what]) {
                    case DEALING_HAND_SOUND:
                        table.onUi_DealingHandSound((Hand) msg.obj);
                        break;

                    case PLAYER_DONE_HAND_SOUND:
                        table.onUi_PlayerDoneHandSound((PlayerHand) msg.obj);
                        break;

                    case DEALER_SHOW_CARD_SOUND:
                        table.onUi_DealerShowCardSound((Card) msg.obj);
                        break;

                    case DEALER_DONE_HAND_SOUND:
                        table.onUi_DealerDoneHandSound((DealerHand) msg.obj);
                        break;

                    case PLAYER_BUST_SOUND:
                        table.onUi_PlayerBustSound();
                        break;

                    case PLAYER_ACTION_SOUND:
                        table.onUi_PlayerActionSound((PlayerHand.Action) msg.obj);
                        break;

                    case HAND_LAST_CARD_DEAL:
                        table.onUi_HandLastCardDeal();
                        break;

                    case DEALER_HIDDEN_OPEN:
                        table.onUi_OpenDealerHidden();
                        break;

                    case PLAYER_HAND_SPLIT:
                        table.onUI_PlayerHandSplit();
                        break;

                    case HANDS_DATA_CHANGED:
                        table.onUi_HandsDataChanged();
                        break;

                    case HAND_FOCUSED_CHANGED:
                        table.onUi_HandFocusedChanged();
                        break;

                    case CHECK_PEEK_HOLE:
                        table.onUi_CheckPeekHole();
                        break;

                    case RING_DEALER_BLACKJACK:
                        table.onUi_RingDealerBlackjack(true);
                        break;

                    case RING_DEALER_NO_BLACKJACK:
                        table.onUi_RingDealerBlackjack(false);
                        break;

                    case OFFERING_INSURANCE:
                        table.onUi_OfferingInsurance();
                        break;

                    case WAITING_PLAYER_ACTION:
                        table.onUi_WaitingPlayerAction();
                        break;

                    case PLAYER_BET_CHANGED:
                        table.onUi_PlayerBetChanged();
                        break;

                    case POPULATE_GAME_DATA:
                        table.onUi_PopulateGameData();
                        break;

                    case SHOE_SHUFFLED:
                        table.onUi_ShoeShuffled();
                        break;

                    case CUT_CARD_DEALT:
                        table.onUi_CutCardDealt();
                        break;

                    // Gesture Actions
                    case GESTURE_STAND:
                        if (table.btnStand.isEnabled()) {
                            table.mListeningActionGesture = false;
                            table.onClick(table.btnStand);
                        }
                        break;
                    case GESTURE_HIT:
                        if (table.btnHit.isEnabled()) {
                            table.mListeningActionGesture = false;
                            table.onClick(table.btnHit);
                        }
                        break;
                    case GESTURE_DOUBLEDOWN:
                        if (table.btnDoubledown.isEnabled()) {
                            table.mListeningActionGesture = false;
                            table.onClick(table.btnDoubledown);
                        } else if (table.btnDoubledown.isShown()) {
                            UtilityStudio.toast("Invalid Doubledown");
                        }
                        break;
                    case GESTURE_SPLIT:
                        if (table.btnSplit.isEnabled()) {
                            table.mListeningActionGesture = false;
                            table.onClick(table.btnSplit);
                        } else if (table.btnSplit.isShown()) {
                            UtilityStudio.toast("Invalid Split");
                        }
                        break;
                    case GESTURE_SURRENDER:
                        if (table.btnSurrender.isEnabled()) {
                            table.mListeningActionGesture = false;
                            table.onClick(table.btnSurrender);
                        } else if (table.btnSurrender.isShown()) {
                            UtilityStudio.toast("Invalid Surrender");
                        }
                        break;
                    case GESTURE_CONTINUE_DEFAULT:
                        table.controlRound();
                        break;
                }
            }
        }
    }

    //=== UI Message ===
    public enum UI_MESSAGE {
        DEALING_HAND_SOUND, PLAYER_DONE_HAND_SOUND, PLAYER_ACTION_SOUND,
        DEALER_SHOW_CARD_SOUND, DEALER_DONE_HAND_SOUND, PLAYER_BUST_SOUND,

        HAND_LAST_CARD_DEAL, DEALER_HIDDEN_OPEN, PLAYER_HAND_SPLIT, HANDS_DATA_CHANGED,
        HAND_FOCUSED_CHANGED, CHECK_PEEK_HOLE, RING_DEALER_BLACKJACK, RING_DEALER_NO_BLACKJACK,
        OFFERING_INSURANCE, WAITING_PLAYER_ACTION, PLAYER_BET_CHANGED,
        POPULATE_GAME_DATA, CUT_CARD_DEALT,

        GESTURE_STAND, GESTURE_HIT, GESTURE_DOUBLEDOWN, GESTURE_SPLIT, GESTURE_SURRENDER,
        GESTURE_CONTINUE_DEFAULT,

        SHOE_SHUFFLED,

        RULE_UPDATED
    }

    public static void sendUiMessage(UI_MESSAGE msgId, int arg1, int arg2) {
        if (BjService.isAutoRunning()) return;

        Message msg = sTableHandler.obtainMessage(msgId.ordinal(), arg1, arg2);
        sTableHandler.sendMessage(msg);
    }

    public static void sendUiMessage(UI_MESSAGE msgId, Object obj) {
        if (BjService.isAutoRunning()) return;

        Message msg = sTableHandler.obtainMessage(msgId.ordinal(), obj);
        sTableHandler.sendMessage(msg);
    }

    public static void sendUiMessage(UI_MESSAGE msgId) {
        if (BjService.isAutoRunning()) return;

        if (sTableHandler != null) {
            Message msg = sTableHandler.obtainMessage(msgId.ordinal());
            sTableHandler.sendMessage(msg);
        }
    }

    // Listener
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.lvHands:
                BjService.RoundStage stage = mService.pRound.getStage();
                if (stage == BjService.RoundStage.BETTING)
                    mService.setPlayerHandFocusedByIndex((int) id);
                break;
        }
    }

    @Override
    public boolean onLongClick(View v) {

        switch (v.getId()) {
            case R.id.ivDealerPicture:                  // Dealer Pic
                onLongClick_DealerPic();
                return true;
            case R.id.ivPlayerPicture:                  // Player Pic
                onLongClick_PlayerPic();
                return true;

            case R.id.loRowHandBox:                     // Hand Box
                onLongClick_HandBox();
                return true;
        }
        return false;
    }

    public void forcedUpdateShoeCount() {
        mService.forcedUpdateShoeCount();
    }

    public void setHandViewClip(boolean clip) {
        View view = lvHands;
        View root = lvHands.getRootView();

        while (view != root) {
            ViewGroup parent = (ViewGroup) view.getParent();
            parent.setClipChildren(clip);
            parent.setClipToPadding(clip);

            view = parent;
        }

        if (root instanceof ViewGroup) {
            ((ViewGroup) root).setClipChildren(clip);
            ((ViewGroup) root).setClipToPadding(clip);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnHit:
                startService_onPlayerDealAction(PlayerHand.Action.HIT);
                layoutPlayButton.setVisibility(View.GONE);
                break;
            case R.id.btnStand:
                startService_onPlayerDealAction(PlayerHand.Action.STAND);
                layoutPlayButton.setVisibility(View.GONE);
                break;
            case R.id.btnSplit:
                startService_onPlayerDealAction(PlayerHand.Action.SPLIT);
                layoutPlayButton.setVisibility(View.GONE);
                break;
            case R.id.btnDoubledown:
                startService_onPlayerDealAction(PlayerHand.Action.DOUBLEDOWN);
                layoutPlayButton.setVisibility(View.GONE);
                break;
            case R.id.btnSurrender:
                startService_onPlayerDealAction(PlayerHand.Action.SURRENDER_OR_HIT);
                layoutPlayButton.setVisibility(View.GONE);
                break;

            case R.id.btnBet0001:
                raiseBet_Table(1.0);
                break;
            case R.id.btnBet0005:
                raiseBet_Table(5.0);
                break;
            case R.id.btnBet0025:
                raiseBet_Table(25.0);
                break;
            case R.id.btnBet0100:
                raiseBet_Table(100.0);
                break;
            case R.id.btnBet0250:
                raiseBet_Table(250.0);
                break;
            case R.id.btnBet1000:
                raiseBet_Table(1000.0);
                break;

            case R.id.btnBetReset:
                mService.resetBet();
                mHandAdapter.notifyDataSetChanged();
                break;
            case R.id.btnBetConfirm:
                mService.confirmBet();
                btnDeal.setEnabled(true);
                mHandAdapter.notifyDataSetChanged();
                break;

            case R.id.btnDeal:
                if (mService.pRound.hasConfirmedBet()) {
                    startRound(BjService.RoundStage.INITIAL_DEAL);
                    layoutPlayerOption.setVisibility(View.INVISIBLE);
                }
                break;

            case R.id.btnStartRound:
                if (getShowInsuranceCheckbox()) {
                    setShowInsuranceCheckbox(false);
                    mHandAdapter.notifyDataSetChanged();
                    btnStartRound.setVisibility(View.GONE);
                    btnStartRound.setText("Start");

                    startService_assureInsuranceAcceptance();
                } else {
                    startRound(BjService.RoundStage.IDLE);
                }
                break;

            case R.id.btnReBet:
                if (mService.pRound.hasPrevBetting()) mService.pRound.usePrevBetting(false);
                else mService.pRound.useEasyBetting(false);

                mHandAdapter.notifyDataSetChanged();
                break;

            case R.id.btnIncreaseBankroll:
                increaseBankroll();
                break;

            case R.id.btnSeedChange:
                changeShoeSeedCondition();
                break;

            case R.id.ivDealerPicture:
                controlRound();
                break;

            case R.id.ivPlayerPicture:
                testProcess();
                break;
        }
    }

    //=== setting up UI stuff ===
    private void setText_btnSeedChange() {
        btnSeedChange.setText(
                mService.pGameRule.useRandomShoe ? "Fixed" : "Random");
    }

    private AnimationCardDeal mAnimateCardDeal;
    private final int CARD_ANIMATION_DURATION = 500;

    private void onLongClick_DealerPic() {
        if (mService.pRound.getStage() != BjService.RoundStage.BETTING) return;

        AlertDialog.Builder alert_confirm = new AlertDialog.Builder(getActivity());
        alert_confirm.setMessage("Do you want to initialize the game data?").setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mService.initializeGameData();
                        UtilityStudio.toast("Game data initialized.");
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                return;
            }
        });
        AlertDialog alert = alert_confirm.create();
        alert.show();
    }

    private void onLongClick_PlayerPic() {
        //show splash screen
        startActivity(new Intent(getActivity(), ActivitySplash.class));
    }

    private void onLongClick_HandBox() {
        // Long Click on Hand Box
    }

    //=== Betting ===

    private void raiseBet_Table(double betAmount) {
        mService.raiseBet(betAmount);
        mHandAdapter.notifyDataSetChanged();
    }

    private void setBettingButtonEnable(boolean enabled) {
        if (layoutBetButton.getVisibility() != View.VISIBLE) return;

        btnBet0001.setEnabled(enabled);
        btnBet0005.setEnabled(enabled);
        btnBet0025.setEnabled(enabled);
        btnBet0100.setEnabled(enabled);
        btnBet0250.setEnabled(enabled);
        btnBet1000.setEnabled(enabled);

        btnBetReset.setEnabled(enabled);
        btnBetConfirm.setEnabled(enabled);
    }

    //=== view update ===
    private void setPlayingButtonEnable(boolean enable) {
        btnHit.setEnabled(enable);
        btnStand.setEnabled(enable);
        btnSplit.setEnabled(enable);
        btnDoubledown.setEnabled(enable);
        btnSurrender.setEnabled(enable);
    }

    //=== Update ===
    private int whereInHandView(int index) {
        int iFirst = lvHands.getFirstVisiblePosition();
        int iLast = lvHands.getLastVisiblePosition();
        return ((index < iFirst) ? -1 : ((index > iLast) ? 1 : 0));
    }

    //=== Round ===
    private void controlRound() {
        BjService.RoundStage stage = mService.pRound.getStage();
        switch (stage) {
            case SETTING:
                startRound(BjService.RoundStage.IDLE);
                break;

            case IDLE:
                startRound(BjService.RoundStage.BETTING);
                break;

            case BETTING:
                if (mService.pRound.hasAnyBet()) {
                    mService.pRound.confirmAllBetting();
                } else if (mService.pRound.hasPrevBetting()) {
                    mService.pRound.usePrevBetting(false);
                } else {
                    mService.pRound.useEasyBetting(false);
                }

                if (mService.pRound.hasConfirmedBet()) {
                    startRound(BjService.RoundStage.INITIAL_DEAL);
                } else {
                    mHandAdapter.notifyDataSetChanged();
                }
                break;

            case INITIAL_DEAL:
                if (getShowInsuranceCheckbox()) {
                    setShowInsuranceCheckbox(false);
                    mHandAdapter.notifyDataSetChanged();
                    btnStartRound.setVisibility(View.GONE);
                    btnStartRound.setText("Start");

                    startService_assureInsuranceAcceptance();
                }
                break;

            case DEALING:
                if (layoutPlayButton.getVisibility() == View.VISIBLE) {
                    PlayerHand hand = (PlayerHand) mService.getHandFocused();
                    PlayerHand.Action action = hand.getBestPlayAction
                            (mService.getDealerHand().getUpScore());

                    startService_onPlayerDealAction(action);

                    layoutPlayButton.setVisibility(View.GONE);
                }
                break;

            case PAYING:
                startRound(BjService.RoundStage.IDLE);
                break;
        }
    }

    //=== Dealer actions ===

    // Round - Idle

    private void scrollToHand(Hand hand) {
        if (!(hand instanceof PlayerHand)) return;

        int index = mService.getIndexByPlayerHand((PlayerHand) hand);
        scrollToHand(index);
    }

    private void scrollToHand(final int position) {
        int count = mHandAdapter.getCount();
        if (position >= count) return;

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (whereInHandView(position) != 0) {
                    lvHands.smoothScrollToPosition(position);
                    lvHands.setSelection(position);
                }
            }
        });
    }

    public void beReady_Table(BjService.RoundStage stage) {
        switch (stage) {
            case SETTING:
                break;

            case IDLE:
                System.gc();        // garbage collection

                scrollToHand(0);
                UtilityStudio.playSound(UtilityStudio.sSound_StartRound);
                setAllHandsAreaVisibility(View.INVISIBLE);
                btnStartRound.setVisibility(View.VISIBLE);
                startService_startRoundStage(BjService.RoundStage.IDLE);
                break;

            case BETTING:
                btnStartRound.setVisibility(View.GONE);
                setAllHandsAreaVisibility(View.VISIBLE);
                btnStartRound.setVisibility(View.GONE);
                layoutBetButton.setVisibility(View.VISIBLE);
                layoutPlayerOption.setVisibility(View.VISIBLE);

                setBettingButtonEnable(false);
                btnDeal.setEnabled(false);

                sendUiMessage(UI_MESSAGE.POPULATE_GAME_DATA);

                startService_startRoundStage(BjService.RoundStage.BETTING);
                break;

            case INITIAL_DEAL:
                layoutBetButton.setVisibility(View.GONE);
                layoutPlayerOption.setVisibility(View.INVISIBLE);

                startService_startRoundStage(BjService.RoundStage.INITIAL_DEAL);
                break;

            case DEALING:
                mService.setHandFocused(null);      // no hand_focused when dealing starts
                startService_startRoundStage(BjService.RoundStage.DEALING);
                break;

            case PAYING:
                mHandAdapter.setDealerMessage("");
                startService_startRoundStage(BjService.RoundStage.PAYING);
                break;
        }
    }

    public void doAction_Table(BjService.RoundStage stage) {
        switch (stage) {
            case SETTING:
                break;

            case IDLE:
                startService_startRoundStage(BjService.RoundStage.IDLE);
                redrawDealerHand();
                break;

            case BETTING:
                startService_startRoundStage(BjService.RoundStage.BETTING);
                mService.setPlayerHandFocusedByIndex(0);
                break;

            case INITIAL_DEAL:
                startService_startRoundStage(BjService.RoundStage.INITIAL_DEAL);
                break;

            case DEALING:
                mHandAdapter.setDealerMessage("");
                startService_startRoundStage(BjService.RoundStage.DEALING);
                break;

            case PAYING:
                startService_startRoundStage(BjService.RoundStage.PAYING);
                break;
        }
    }

    private void startRound(BjService.RoundStage stage) {
        if (mService.pRound.getStage() != stage) {
            beReady_Table(stage);
        } else {
            mService.setHandFocused(null);
            doAction_Table(stage);
        }
    }

    private void endingRound_Dealing() {
        // start paying automatically
        startRound(BjService.RoundStage.PAYING);
    }

    private void endingRound_Paying() {
        // scroll to first hand
        scrollToHand(0);

        // round ending sound
        UtilityStudio.playSound(UtilityStudio.sSound_RoundOver);

        // show start round button
        btnStartRound.setVisibility(View.VISIBLE);

        sendUiMessage(UI_MESSAGE.POPULATE_GAME_DATA);
    }

    private void setAllHandsAreaVisibility(int visibility) {
        layoutDealer.setVisibility(visibility);
        layoutLine.setVisibility(visibility);
        lvHands.setVisibility(visibility);
    }

    public void redrawPlayerHands() {
        lvHands.post(new Runnable() {
            @Override
            public void run() {
                mHandAdapter.notifyDataSetChanged();
            }
        });
    }

    public void redrawDealerHand() {
        layoutDealer.post(new Runnable() {
            @Override
            public void run() {
                mHandAdapter.setDealerData();
                layoutDealer.invalidate();
            }
        });
    }

    public void redrawHand(Hand hand) {
        if (hand instanceof DealerHand) redrawDealerHand();
        if (hand instanceof PlayerHand) redrawPlayerHands();
    }

    private void increaseBankroll() {
        mService.increasePlayerBankroll();
        UtilityStudio.toast("Player Bankroll has been increased.");

        sendUiMessage(UI_MESSAGE.PLAYER_BET_CHANGED);
    }

    //=== Setting ===

    private void changeShoeSeedCondition() {
        mService.pShoe.changeSeedCondition();
        setText_btnSeedChange();

        UtilityStudio.toast(mService.pGameRule.useRandomShoe ?
                "Random Shoe will be used." : "Fixed Shoe will be used.");
    }

    //== Dispatch intent ===
    public void dispatchIntent(Intent intent) {
        switch (intent.getAction()) {
            case BjService.SERVICE_READY:
                setupInitialTable();
                startRound(BjService.RoundStage.IDLE);
                break;

            // Round
            case BjService.ROUND_IDLE_STARTED:
                startRound(BjService.RoundStage.IDLE);
                break;
            case BjService.ROUND_IDLE_ENDED:
                startRound(BjService.RoundStage.BETTING);
                break;

            case BjService.ROUND_BETTING_STARTED:
                startRound(BjService.RoundStage.BETTING);
                break;
            case BjService.ROUND_BETTING_ENDED:
                break;

            case BjService.ROUND_INITIAL_DEAL_STARTED:
                startRound(BjService.RoundStage.INITIAL_DEAL);
                break;
            case BjService.ROUND_INITIAL_DEAL_ENDED:
                startRound(BjService.RoundStage.DEALING);
                break;

            case BjService.ROUND_DEALING_STARTED:
                startRound(BjService.RoundStage.DEALING);
                break;
            case BjService.ROUND_DEALING_ENDED:
                endingRound_Dealing();
                break;

            case BjService.ROUND_PAYING_STARTED:
                startRound(BjService.RoundStage.PAYING);
                break;
            case BjService.ROUND_PAYING_ENDED:
                endingRound_Paying();
                break;

            // Dealing
            case BjService.ROUND_STAGE_CONTINUE:
                startService_doStageAction();
                break;

            case BjService.ROUND_STAGE_FINISHED:
                startNextRoundStage();
                break;

            case BjService.ROUND_PAYING_FINISHED:
                sendUiMessage(UI_MESSAGE.POPULATE_GAME_DATA);
                break;
        }
    }

    private void startNextRoundStage() {
        BjService.RoundStage stage = mService.pRound.getStage();
        switch (stage) {
            case SETTING:
                startRound(BjService.RoundStage.IDLE);
                break;
            case IDLE:
                startRound(BjService.RoundStage.BETTING);
                break;
            case BETTING:
                startRound(BjService.RoundStage.INITIAL_DEAL);
                break;
            case INITIAL_DEAL:
                startRound(BjService.RoundStage.DEALING);
                break;
            case DEALING:
                startRound(BjService.RoundStage.PAYING);
                break;
            case PAYING:
                startRound(BjService.RoundStage.IDLE);
                break;
        }
    }

    //=== UI-Update ===


    private void onUi_PlayerDoneHandSound(PlayerHand playerHand) {
        if (playerHand.isBlackjack()) {
            int soundIndex = UtilityStudio.sRandom.nextInt(UtilityStudio.COUNT_BLACKJACK_SOUND);
            UtilityStudio.playSound(UtilityStudio.sSound_Hand_Blackjack[soundIndex]);
        } else if (playerHand.isBust()) {
            int soundIndex = UtilityStudio.sRandom.nextInt(UtilityStudio.COUNT_TOO_MANY_SOUND);
            UtilityStudio.playSound(UtilityStudio.sSound_Too_Many[soundIndex]);
        } else {
            onUi_DealingHandSound(playerHand);
        }
    }

    private void onUi_DealerShowCardSound(Card card) {
        UtilityStudio.playDealerShowRankSound(card.getRank());
    }

    private void onUi_DealerDoneHandSound(DealerHand dealerHand) {
        if (dealerHand.isBlackjack()) {
            UtilityStudio.playDealerBlackjackSound();
        } else if (dealerHand.isBust()) {
            UtilityStudio.playDealerBustSound();
        } else {
            onUi_DealingHandSound(dealerHand);
        }
    }

    private void onUi_PlayerBustSound() {
        UtilityStudio.playBustSound();
    }

    private void onUi_PlayerActionSound(PlayerHand.Action action) {
        UtilityStudio.playPlayerActionSound(action);
    }

    private void onUi_DealingHandSound(Hand hand) {
        UtilityStudio.playDealingHandSound(hand);
    }

    private void onUi_HandLastCardDeal() {
        ImageView cardView = mHandAdapter.getCardViewDealt();
        if (cardView == null) {
            sAnimation_Done = true;
        } else {
            if (mAnimateCardDeal == null) mAnimateCardDeal = new AnimationCardDeal();
            cardView.setVisibility(View.VISIBLE);
            mAnimateCardDeal.doAnimation(cardView, false);
        }
    }

    private void onUi_CutCardDealt() {
        ImageView cutView = mHandAdapter.getCutCardView();
        if (cutView == null) {
            sAnimation_Done = true;
        } else {
            if (mAnimateCardDeal == null) mAnimateCardDeal = new AnimationCardDeal();
            cutView.setVisibility(View.VISIBLE);
            mAnimateCardDeal.doAnimation(cutView, true);
        }
    }

    private AnimationCardFlip mAnimationCardFlip = null;

    private void onUi_OpenDealerHidden() {
        if (mAnimationCardFlip == null) mAnimationCardFlip = new AnimationCardFlip();
        mAnimationCardFlip.doAnimation();
    }

    private void onUI_PlayerHandSplit() {
        AnimationCardSplit animationCardSplit = new AnimationCardSplit();
        animationCardSplit.doAnimation();
    }

    private void onUi_HandsDataChanged() {

    }

    private void onUi_HandFocusedChanged() {
        Hand focused_prev = mService.getHandFocused_prev();
        Hand focused = mService.getHandFocused();

        if (focused instanceof PlayerHand) scrollToHand(focused);

        if (focused_prev != null) focused_prev.highlightHead(false);
        if (focused != null) focused.highlightHead(true);

        if (mService.pRound.getStage() == BjService.RoundStage.BETTING) {
            sendUiMessage(UI_MESSAGE.PLAYER_BET_CHANGED);
        }

        redrawHand(focused_prev);
        redrawHand(focused);
    }

    private void onUi_RingDealerBlackjack(boolean blackjack) {
        if (blackjack) {
            UtilityStudio.playSound(UtilityStudio.sSound_DealerBlackjack);

            if (mAnimationCardFlip == null) mAnimationCardFlip = new AnimationCardFlip();
            mAnimationCardFlip.doAnimation();
        } else {
            mHandAdapter.setDealerMessage("NO BLACKJACK");
        }
    }

    private void onUi_CheckPeekHole() {
        AnimationPeekHole animationPeekHole = new AnimationPeekHole();
        animationPeekHole.doAnimation();
    }

    private void onUi_OfferingInsurance() {
        UtilityStudio.toast("Dealer Up-Card is ACE. Insurance offered.");
        UtilityStudio.playSound(UtilityStudio.sSound_Insurance);

        setShowInsuranceCheckbox(true);
        btnStartRound.setVisibility(View.VISIBLE);
        btnStartRound.setText("Confirm");
        mHandAdapter.notifyDataSetChanged();
    }

    public void onUi_WaitingPlayerAction() {     // call from service
        btnSurrender.setVisibility(mService.pGameRule.allowSurrender ? View.VISIBLE : View.GONE);

        Hand handFocused = mService.getHandFocused();
        if ((handFocused == null) || (handFocused instanceof DealerHand)) {
            layoutPlayButton.setVisibility(View.GONE);
            return;
        }

        onUi_DealingHandSound(handFocused);

        layoutPlayButton.setVisibility(View.VISIBLE);

        PlayerHand hand = (PlayerHand) handFocused;

        setPlayingButtonEnable(false);

        if (mService.pRound.getStage() != BjService.RoundStage.DEALING) return;

        if (!hand.isBust() && !hand.isTwentyOne()) {
            btnHit.setEnabled(true);
            btnStand.setEnabled(true);

            if (hand.canSplit()) btnSplit.setEnabled(true);
            if (hand.canDoubledown()) btnDoubledown.setEnabled(true);
            if (hand.canSurrender()) btnSurrender.setEnabled(true);
        }

        // hint
        PlayerHand.Action hint = hand.getBestPlayAction(mService.getDealerHand().getUpScore());

        btnHit.setTextColor(UtilityStudio.sColor_NormalAction);
        btnStand.setTextColor(UtilityStudio.sColor_NormalAction);
        btnSplit.setTextColor(UtilityStudio.sColor_NormalAction);
        btnDoubledown.setTextColor(UtilityStudio.sColor_NormalAction);
        btnSurrender.setTextColor(UtilityStudio.sColor_NormalAction);

        switch (hint) {
            case HIT:
                btnHit.setTextColor(UtilityStudio.sColor_BestAction);
                break;
            case STAND:
                btnStand.setTextColor(UtilityStudio.sColor_BestAction);
                break;
            case SPLIT:
                btnSplit.setTextColor(UtilityStudio.sColor_BestAction);
                break;
            case DOUBLEDOWN:
                btnDoubledown.setTextColor(UtilityStudio.sColor_BestAction);
                break;
            case SURRENDER_OR_HIT:
            case SURRENDER_OR_STAND:
                btnSurrender.setTextColor(UtilityStudio.sColor_BestAction);
                break;
        }

        // listening action gesture
        mListeningActionGesture = true;
    }

    public void onUi_PlayerBetChanged() {
        Hand handFocused = mService.getHandFocused();
        if ((handFocused == null) || (handFocused instanceof DealerHand)) return;

        if (mService.pRound.getStage() != BjService.RoundStage.BETTING) return;

        PlayerHand hand = (PlayerHand) handFocused;

        if (hand.hasBetConfirmed()) {
            setBettingButtonEnable(false);
            return;
        }

        Player player = mService.pPlayer;

        double betAmount = hand.getBetAmount();
        double bankroll = player.getBankroll();
        double max = mService.pGameRule.maxBet;

        if ((bankroll >= 1000.0) && (betAmount + 1000.0) <= max) {
            btnBet1000.setEnabled(true);
            btnBet0250.setEnabled(true);
            btnBet0100.setEnabled(true);
            btnBet0025.setEnabled(true);
            btnBet0005.setEnabled(true);
            btnBet0001.setEnabled(true);
        } else if ((bankroll >= 250.0) && (betAmount + 250.0) <= max) {
            btnBet1000.setEnabled(false);

            btnBet0250.setEnabled(true);
            btnBet0100.setEnabled(true);
            btnBet0025.setEnabled(true);
            btnBet0005.setEnabled(true);
            btnBet0001.setEnabled(true);
        } else if ((bankroll >= 100.0) && (betAmount + 100.0) <= max) {
            btnBet1000.setEnabled(false);
            btnBet0250.setEnabled(false);

            btnBet0100.setEnabled(true);
            btnBet0025.setEnabled(true);
            btnBet0005.setEnabled(true);
            btnBet0001.setEnabled(true);
        } else if ((bankroll >= 25.0) && (betAmount + 25.0) <= max) {
            btnBet1000.setEnabled(false);
            btnBet0250.setEnabled(false);
            btnBet0100.setEnabled(false);

            btnBet0025.setEnabled(true);
            btnBet0005.setEnabled(true);
            btnBet0001.setEnabled(true);
        } else if ((bankroll >= 5.0) && (betAmount + 5.0) <= max) {
            btnBet1000.setEnabled(false);
            btnBet0250.setEnabled(false);
            btnBet0100.setEnabled(false);
            btnBet0025.setEnabled(false);

            btnBet0005.setEnabled(true);
            btnBet0001.setEnabled(true);
        } else if ((bankroll >= 1.0) && (betAmount + 1.0) <= max) {
            btnBet1000.setEnabled(false);
            btnBet0250.setEnabled(false);
            btnBet0100.setEnabled(false);
            btnBet0025.setEnabled(false);
            btnBet0005.setEnabled(false);

            btnBet0001.setEnabled(true);
        }

        if (betAmount > 0.0) {
            btnBetConfirm.setEnabled(true);
            btnBetReset.setEnabled(true);
        } else {
            btnBetConfirm.setEnabled(false);
            btnBetReset.setEnabled(false);
        }

        BjService.sGameData.populatePlayerBankroll();
    }

    private void onUi_PopulateGameData() {
        BjService.sGameData.populateAll();
    }

    // notified "card shuffled" from Service
    private void onUi_ShoeShuffled() {
        UtilityStudio.toast("Shuffling the Shoe");
        UtilityStudio.playSound(UtilityStudio.sSound_Shuffle);

        if (mCounting != null) {
            mCounting.resetCounting();
        }
    }

    // called after SERVICE_READY, before starting game
    private void setupInitialTable() {
        mService.initializeGameData();
    }

    //=== Test ===
    private void testProcess() {
        testProcess_swap_rule();
    }

    private void testProcess_swap_rule() {
        if (mService.pRound.getStage() != BjService.RoundStage.BETTING) return;

        if (mService.pGameRule == Rule.sRuleRegular) {
            mService.pGameRule = Rule.sRuleSpecial;
        } else {
            mService.pGameRule = Rule.sRuleRegular;
        }
        UtilityStudio.toast("Rule: " + mService.pGameRule.toString());
    }

    // Card Animation
    public class AnimationCardDeal extends Animation implements Animation.AnimationListener {
        AlphaAnimation mAlpha;
        RotateAnimation mRotate;
        AccelerateInterpolator mAccelerate;
        ScaleAnimation mScale;

        // Animation
        public AnimationCardDeal() {
            mAlpha = new AlphaAnimation(0.5F, 1.0F);
            mRotate = new RotateAnimation(180, 0);
            mAccelerate = new AccelerateInterpolator(2.0f);
            mScale = new ScaleAnimation(0.3f, 1.0f, 0.3f, 1.0f);
        }

        @Override
        public void onAnimationStart(Animation animation) {
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            setHandViewClip(true);
            forcedUpdateShoeCount();
            sAnimation_Done = true;
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }

        private class CardAnimationSet extends AnimationSet {
            public CardAnimationSet(boolean test) {
                super(test);
            }
        }

        public void doAnimation(final View cardView, final boolean isCutCard) {
            if (!getGameRule().mUseAnimation || (cardView == null)) {
                sAnimation_Done = true;
                return;
            }

            int[] shoeLocation = new int[2];
            int[] location = new int[2];
            getShoeLocationOnScreen(shoeLocation);
            cardView.getLocationOnScreen(location);
            shoeLocation[0] -= location[0];
            shoeLocation[1] -= location[1];

            TranslateAnimation translate = new TranslateAnimation(
                    Animation.ABSOLUTE, shoeLocation[0],
                    Animation.RELATIVE_TO_SELF, 0,
                    Animation.ABSOLUTE, shoeLocation[1],
                    Animation.RELATIVE_TO_SELF, 0);
            translate.setInterpolator(mAccelerate);

            final CardAnimationSet animation = new CardAnimationSet(true);
            animation.addAnimation(mAlpha);
            animation.addAnimation(mRotate);
            animation.addAnimation(mScale);
            animation.addAnimation(translate);
            animation.setDuration(CARD_ANIMATION_DURATION);

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    UtilityStudio.playSound(isCutCard ?
                            UtilityStudio.sSound_Deal : UtilityStudio.sSound_Deal);
                    animation.setAnimationListener(AnimationCardDeal.this);

                    cardView.setVisibility(View.VISIBLE);

                    setHandViewClip(false);
                    cardView.startAnimation(animation);
                }
            });
        }
    }

    // check animation is still on going
    public static boolean sAnimation_Done = true;

    // inner class HandAdapter
    private static FrameLayout.LayoutParams sCardViewParam;
    private static int sCardStartGap;
    private static int sCardTopGap;

    public static void setCardImageViewParams(FrameLayout.LayoutParams defaultParams, int
            startGap, int topGap) {
        sCardViewParam = defaultParams;
        sCardStartGap = startGap;
        sCardTopGap = topGap;
    }

    private static boolean sShowInsuranceCheckbox = false;
    static int sCountCardView = 0;

    public static void setShowInsuranceCheckbox(boolean show) {
        sShowInsuranceCheckbox = show;
    }

    public static boolean getShowInsuranceCheckbox() {
        return sShowInsuranceCheckbox;
    }

    public class HandAdapter extends ArrayAdapter<PlayerHand> {
        private final BjService mService;
        ListView mLvHands;
        private HandDataHandler mDealerDataHandler;

        public HandAdapter(Context context, int resource, BjService service, View layoutDealer,
                           ListView lvHands) {
            super(context, resource, service.getActingHands());
            mService = service;
            mLvHands = lvHands;

            // Dealer data handler
            mDealerDataHandler = new HandDataHandler(layoutDealer, mService, false);
            layoutDealer.setTag(mDealerDataHandler);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View itemView;
            HandDataHandler handler;

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) this.getContext()
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                itemView = inflater.inflate(R.layout.row_table_hand, parent, false);
                handler = new HandDataHandler(itemView, mService, true);

                itemView.setTag(handler);

            } else {
                itemView = convertView;
                handler = (HandDataHandler) itemView.getTag();
            }

            handler.setData(position);

            return itemView;
        }

        public void setDealerMessage(String msg) {
            mDealerDataHandler.setMessageText(msg);
        }

        Hand mHandDealt;
        Card mCardDealt;
        int mSoundCardDealt = -1;   // -1 default = no Sound

        public void setCardDealt(Hand hand, Card card) {
            mHandDealt = hand;
            mCardDealt = card;

            scrollToHand(hand);
        }

        public ImageView getCutCardView() {
            return mDealerDataHandler.ivCutCard;
        }

        public ImageView getCardViewDealt() {
            HandDataHandler handler = null;
            ImageView cardView = null;

            if (mHandDealt instanceof DealerHand) {
                handler = mDealerDataHandler;
            } else {
                int index = mService.getIndexByPlayerHand((PlayerHand) mHandDealt);
                int iFirst = mLvHands.getFirstVisiblePosition();
                index -= iFirst;
                View itemView = mLvHands.getChildAt(index);
                if (itemView != null) handler = (HandDataHandler) itemView.getTag();
            }

            if (handler != null) {
                cardView = handler.ivRowCards[mHandDealt.getCardCount()];
                cardView.setImageResource(UtilityStudio.getCardImage(mCardDealt));
                mCardDealt.setImageView(cardView);
            }

            return cardView;
        }

        public void setDealerData() {
            mDealerDataHandler.setData(0);
        }

        //===
        public class HandDataHandler {
            private static final int MAX_COUNT_CARD_VIEW = 12;     // maximum count of card imageViews

            private final TextView tvRowBoxId;
            private final TextView tvRowStrategy;
            private final TextView tvRowBetter;
            private final TextView tvRowHandBet;
            private final TextView tvRowInsured;
            private final TextView tvRowHandReward;
            private final ImageView[] ivRowCards = new ImageView[MAX_COUNT_CARD_VIEW];
            private final ImageView ivCutCard;
            private final TextView tvRowHandMessage;
            private final TextView tvRowHandScore;
            private final TextView tvRowHandResult;
            private final CheckBox cbRowInsurance;        // for insurance check

            private final RelativeLayout layoutRowHandBox;            // view group
            private final LinearLayout layoutRowHandDisplay;          // view group
            private final FrameLayout layoutHandCards;                // sub view group
            private final RelativeLayout layoutHandResult;              // sub view group

            private final BjService mService;
            private final boolean mIsForPlayer;

            public HandDataHandler(View view, BjService service, boolean isForPlayer) {

                mService = service;             // to access service
                mIsForPlayer = isForPlayer;     // treat some aspects differently

                // in hand-box layout
                layoutRowHandBox = (RelativeLayout) view.findViewById(R.id.loRowHandBox);
                layoutRowHandBox.setOnLongClickListener(FragmentTable.this);

                tvRowBoxId = (TextView) view.findViewById(R.id.tvRowBoxId);             // id
                tvRowStrategy = (TextView) view.findViewById(R.id.tvRowStrategy);       // strategy
                tvRowBetter = (TextView) view.findViewById(R.id.tvRowBetter);           // better
                tvRowHandBet = (TextView) view.findViewById(R.id.tvRowHandBet);         // insurance
                tvRowInsured = (TextView) view.findViewById(R.id.tvRowInsured);         // bet
                tvRowHandReward = (TextView) view.findViewById(R.id.tvRowHandReward);   // reward

                // in  Hand Display viewGroup
                layoutRowHandDisplay = (LinearLayout) view.findViewById(R.id.loRowHandDisplay);

                // inside of layoutRowHandDisplay, there are two layout(1 frame + 1 linear)
                // this is first frame layout
                layoutHandCards = (FrameLayout) view.findViewById(R.id.loHandCards);
                ivCutCard = (ImageView) layoutHandCards.findViewById(R.id.ivCutCard);
                layoutHandCards.removeAllViews();
                for (int index = 0; index < MAX_COUNT_CARD_VIEW; index++) {
                    ImageView cardView = new ImageView(getContext());
                    FrameLayout.LayoutParams nextParams = new FrameLayout.LayoutParams(sCardViewParam);
                    nextParams.setMarginStart(sCardStartGap * index);
                    nextParams.setMarginStart(sCardStartGap * index);
                    nextParams.setMargins(0, sCardTopGap * index, 0, 0);
                    cardView.setLayoutParams(nextParams);

                    layoutHandCards.addView(cardView);    // add view at last
                    ivRowCards[index] = cardView;
                }
                layoutHandCards.addView(ivCutCard);    // add cutCard view at last

                // this is second subView
                layoutHandResult = (RelativeLayout) view.findViewById(R.id.loHandResult);
                tvRowHandMessage = (TextView) view.findViewById(R.id.tvRowHandMessage);
                tvRowHandMessage.setText("");
                tvRowHandScore = (TextView) view.findViewById(R.id.tvRowHandScore);
                tvRowHandResult = (TextView) view.findViewById(R.id.tvRowHandResult);
                cbRowInsurance = (CheckBox) view.findViewById(R.id.cbRowInsurance);
                cbRowInsurance.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        PlayerHand hand = (PlayerHand) buttonView.getTag();
                        mService.takeInsuranceAcceptance(hand, isChecked);
                    }
                });
            }

            private void setAllViewsHide() {
                int nChild = layoutHandCards.getChildCount();
                while (nChild > 0) {
                    layoutHandCards.getChildAt(--nChild).setVisibility(View.GONE);
                }
                tvRowHandScore.setVisibility(View.INVISIBLE);
                tvRowHandResult.setVisibility(View.INVISIBLE);
                cbRowInsurance.setVisibility(View.INVISIBLE);
            }

            private void setHandBoxText(int index, Hand hand) {                   // box number
                int backColor;
                if (hand == mService.getHandFocused()) {
                    backColor = UtilityStudio.sColor_HandHighlight;
                } else {
                    backColor = hand.getHeadColor();
                }

                if (mIsForPlayer) {
                    int boxIndex = mService.getActingHands().get(index).getBoxIndex();
                    BettingBox box = mService.getActingHands().get(index).getBox();
                    Strategy strategy = box.getStrategy();
                    String textStrategy = (strategy == null) ? "" : strategy.getUniqueId();
                    Better better = box.getBetter();
                    String textBetter = (better == null) ? "" : better.getUniqueId();

                    tvRowBoxId.setText("Box " + String.valueOf(boxIndex + 1));
                    tvRowStrategy.setText(textStrategy);
                    tvRowBetter.setText(textBetter);
                } else {
                    tvRowBoxId.setText("Dealer");
                    tvRowStrategy.setVisibility(View.INVISIBLE);
                    tvRowBetter.setVisibility(View.INVISIBLE);
                }

                tvRowBoxId.setBackgroundColor(backColor);
            }

            private void setBetText(double betAmount, boolean confirmed, boolean failed) {       // bet amount
                if (!confirmed) {
                    tvRowHandBet.setTextColor(UtilityStudio.sColor_Unconfirmed);
                } else if (failed) {
                    tvRowHandBet.setTextColor(UtilityStudio.sColor_Lost);
                } else {
                    tvRowHandBet.setTextColor(Color.WHITE);
                }

                tvRowHandBet.setText((betAmount > 0.0) ? UtilityStudio.getUsCurrency(betAmount) : "");
            }

            private void setInsuredText(double insuredAmount, boolean failed) { // insurance

                tvRowInsured.setTextColor(failed ? UtilityStudio.sColor_Lost : UtilityStudio.sColor_Unconfirmed);
                tvRowInsured.setText((insuredAmount > 0.0) ? UtilityStudio.getUsCurrency(insuredAmount) : "");
            }

            private void setRewardText(double rewardAmount) {    // bet amount

                tvRowHandReward.setTextColor(UtilityStudio.sColor_Win);
                tvRowHandReward.setText((rewardAmount > 0.0) ? UtilityStudio.getUsCurrency(rewardAmount) : "");
            }

            private void setMessageText(String msg) {                   // message
                if (msg.equals("")) {
                    tvRowHandMessage.setVisibility(View.INVISIBLE);
                } else {
                    tvRowHandMessage.setVisibility(View.VISIBLE);
                    tvRowHandMessage.setText(msg);
                }
            }

            private void setScoreText(Hand hand) {    // score in result display
                if (hand.getScore() == 0) {
                    tvRowHandScore.setVisibility(View.INVISIBLE);
                } else {
                    tvRowHandScore.setVisibility(View.VISIBLE);
                    tvRowHandScore.setText(hand.getScoreText());
                }
            }

            private void setResultText(BjService.RoundResult result) {
                switch (result) {
                    case WIN:
                        tvRowHandResult.setVisibility(View.VISIBLE);
                        tvRowHandResult.setTextColor(UtilityStudio.sColor_Win);
                        tvRowHandResult.setText("WIN");
                        break;
                    case PUSH:
                        tvRowHandResult.setVisibility(View.VISIBLE);
                        tvRowHandResult.setTextColor(UtilityStudio.sColor_Unconfirmed);
                        tvRowHandResult.setText("PUSH");
                        break;
                    case LOST:
                        tvRowHandResult.setVisibility(View.VISIBLE);
                        tvRowHandResult.setTextColor(UtilityStudio.sColor_Lost);
                        tvRowHandResult.setText("LOST");
                        break;
                    default:
                        tvRowHandResult.setVisibility(View.INVISIBLE);
                        break;
                }
            }

            private void setDealerResultText(DealerHand hand) {
                if (hand.isBlackjack()) {
                    tvRowHandResult.setTextColor(UtilityStudio.sColor_Blackjack);
                    tvRowHandResult.setText("BLACKJACK!");
                } else if (hand.isBust()) {
                    tvRowHandResult.setTextColor(UtilityStudio.sColor_Lost);
                    tvRowHandResult.setText("BUST!");
                } else {
                    tvRowHandResult.setTextColor(UtilityStudio.sColor_Confirmed);
                    tvRowHandResult.setText("");
                }
            }

            public void setData(int position) {
                Hand hand;
                PlayerHand playerHand = null;
                DealerHand dealerHand = null;

                ArrayList<PlayerHand> actingHands = mService.getActingHands();

                hand = mIsForPlayer ? actingHands.get(position) : mService.getDealerHand();

                if (mIsForPlayer) playerHand = (PlayerHand) hand;
                else dealerHand = (DealerHand) hand;

                hand.setHeadTextView(tvRowBoxId);
                setHandBoxText(position, hand);                         // handBox always visible

                setRewardText(hand.getRewardAmount());                  // bet always visible

                setAllViewsHide();

                if (sShowInsuranceCheckbox) {
                    if (mIsForPlayer) {
                        cbRowInsurance.setVisibility(View.VISIBLE);
                        cbRowInsurance.setTag(hand);
                        cbRowInsurance.setChecked(playerHand.getBestInsuranceAction());
                    }
                }

                if (playerHand != null) {   // player
                    setBetText(playerHand.getBetAmount(), playerHand.hasBetConfirmed(), !playerHand.stillAlive());
                    setInsuredText(playerHand.getInsuredAmount(), playerHand.hasInsuredCovered());

                } else {                    // dealer
                    setBetText(0.0, false, false);
                    setInsuredText(0.0, false);

                    ivCutCard.setVisibility(mService.pShoe.needShuffle() ? View.VISIBLE : View.INVISIBLE);
                }

                int nCards = hand.getCardCount();
                if (nCards > 0) {
                    int index = 0;
                    for (Card card : hand) {
                        ImageView cardView = ivRowCards[index++];
                        cardView.setImageResource(UtilityStudio.getCardImage(card));
                        card.setImageView(cardView);
                        cardView.setVisibility(View.VISIBLE);
                    }

                    setScoreText(hand);
                    if (playerHand != null) {
                        setResultText(playerHand.getRoundResult());
                    } else {
                        setDealerResultText(dealerHand);
                    }
                }
            }
        }
    }

    //=== IntentService Request ===
    private void startService_doStageAction() {
        Intent msgIntent = new Intent(getActivity(), BjService.class);
        msgIntent.putExtra(BjService.SERVICE_TAG, BjService.SERVICE_doStageAction);
        mService.startService(msgIntent);
    }

    private void startService_startRoundStage(BjService.RoundStage stage) {
        Intent msgIntent = new Intent(getActivity(), BjService.class);
        msgIntent.putExtra(BjService.SERVICE_TAG, BjService.SERVICE_startRoundStage);
        msgIntent.putExtra(BjService.SERVICE_STAGE_TAG, stage.ordinal());   // mService.startRoundStage(stage)
        mService.startService(msgIntent);
    }

    private void startService_onPlayerDealAction(PlayerHand.Action action) {
        Intent msgIntent = new Intent(getActivity(), BjService.class);
        msgIntent.putExtra(BjService.SERVICE_TAG, BjService.SERVICE_doPlayerDealAction);        // -1 for mService.doStageAction();
        msgIntent.putExtra(BjService.SERVICE_ACTION_TAG, action.ordinal());   // mService.onPlayerDealAction(action)
        mService.startService(msgIntent);
    }

    private void startService_assureInsuranceAcceptance() {
        Intent msgIntent = new Intent(getActivity(), BjService.class);
        msgIntent.putExtra(BjService.SERVICE_TAG, BjService.SERVICE_assureInsuranceAcceptance);
        mService.startService(msgIntent);
    }

    private class AnimationCardFlip implements Animation.AnimationListener {
        Animation mAnimationTo, mAnimationFrom;
        boolean isBackOfCardShowing = true;

        public AnimationCardFlip() {
            mAnimationTo = AnimationUtils.loadAnimation(getActivity(), R.anim.flip_to_middle);
            mAnimationTo.setAnimationListener(this);
            mAnimationFrom = AnimationUtils.loadAnimation(getActivity(), R.anim.flip_from_middle);
            mAnimationFrom.setAnimationListener(this);
        }

        public void doAnimation() {
            if (!getGameRule().mUseAnimation) {
                sAnimation_Done = true;
                return;
            }

            isBackOfCardShowing = true;
            ImageView cardView = mService.getDealerHand().getCardAt(1).getImageView();
            cardView.clearAnimation();
            mAnimationTo.reset();
            cardView.setAnimation(mAnimationTo);
            cardView.startAnimation(mAnimationTo);
        }

        @Override
        public void onAnimationStart(Animation animation) {
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            Card card = mService.getDealerHand().getCardAt(1);
            ImageView cardView = card.getImageView();

            if (animation == mAnimationTo) {
                if (isBackOfCardShowing) {
                    cardView.setImageResource(UtilityStudio.sImage_CardFronts[card.getOrder()]);
                } else {
                    cardView.setImageResource(UtilityStudio.sImage_CardBack);
                }
                cardView.clearAnimation();
                mAnimationFrom.reset();
                cardView.setAnimation(mAnimationFrom);
                cardView.startAnimation(mAnimationFrom);
            } else {
                isBackOfCardShowing = !isBackOfCardShowing;
                sAnimation_Done = true;
            }
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    }

    private class AnimationPeekHole implements Animation.AnimationListener {
        Animation mAnimationPeekStart, mAnimationPeekDone;

        public AnimationPeekHole() {
            mAnimationPeekStart = AnimationUtils.loadAnimation(getActivity(), R.anim.peek_to_middle);
            mAnimationPeekDone = AnimationUtils.loadAnimation(getActivity(), R.anim.peek_from_middle);
            mAnimationPeekStart.setAnimationListener(this);
            mAnimationPeekDone.setAnimationListener(this);
        }

        public void doAnimation() {
            if (!getGameRule().mUseAnimation) {
                sAnimation_Done = true;
                return;
            }

            ImageView cardView = mService.getDealerHand().getCardAt(1).getImageView();

            cardView.clearAnimation();
            mAnimationPeekStart.reset();
            cardView.setAnimation(mAnimationPeekStart);
            cardView.startAnimation(mAnimationPeekStart);
        }

        @Override
        public void onAnimationStart(Animation animation) {
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            ImageView cardView = mService.getDealerHand().getCardAt(1).getImageView();

            if (animation == mAnimationPeekStart) {
                cardView.clearAnimation();
                mAnimationPeekDone.reset();
                cardView.setAnimation(mAnimationPeekDone);
                cardView.startAnimation(mAnimationPeekDone);
            } else {
                sAnimation_Done = true;
            }
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    }

    private class AnimationCardSplit implements Animation.AnimationListener {
        AnimationSet mAnimationSplit = null;

        public AnimationCardSplit() {
            int splitDeltaY = layoutDealer.getHeight() - sCardStartGap + lvHands.getDividerHeight();
            TranslateAnimation translate = new TranslateAnimation(
                    Animation.ABSOLUTE, sCardStartGap,
                    Animation.RELATIVE_TO_SELF, 0,
                    Animation.ABSOLUTE, -splitDeltaY,
                    Animation.RELATIVE_TO_SELF, 0);

            mAnimationSplit = new AnimationSet(true);
            mAnimationSplit.addAnimation(translate);
            mAnimationSplit.setAnimationListener(AnimationCardSplit.this);
            mAnimationSplit.setDuration(CARD_ANIMATION_DURATION);
        }

        public void doAnimation() {
            if (!getGameRule().mUseAnimation) {
                sAnimation_Done = true;
                return;
            }

            ImageView cardView = mHandAdapter.getCardViewDealt();
            if (cardView == null) {
                sAnimation_Done = true;
                return;
            }

            cardView.setVisibility(View.VISIBLE);

            cardView.clearAnimation();
            cardView.setAnimation(mAnimationSplit);
        }

        @Override
        public void onAnimationStart(Animation animation) {
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            sAnimation_Done = true;
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }
    }
}
