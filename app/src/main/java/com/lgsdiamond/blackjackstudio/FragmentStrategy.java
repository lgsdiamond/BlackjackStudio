package com.lgsdiamond.blackjackstudio;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.lgsdiamond.blackjackstudio.BlackjackElement.PlayerHand;
import com.lgsdiamond.blackjackstudio.BlackjackElement.Strategy;

import java.util.ArrayList;

public class FragmentStrategy extends FragmentStudioBase {
    public static FragmentStrategy newInstance() {
        FragmentStrategy fragment = new FragmentStrategy();
        FragmentStudioBase.newInstance(fragment, R.layout.fragment_strategy);

        fragment.mStrategies = ActivityStudio.sStudio.getStrategies();
        
        return fragment;
    }

    TextView mTvSelectedStrategyTitle;
    ListView mLvStrategies, mLvSelectedStrategy;

    ArrayList<Strategy> mStrategies;
    StrategyAdapter mStrategyAdapter;
    StrategyActionAdapter mStrategyActionAdapter;
    ArrayList<Strategy.SingleActions> mStrategyActions = new ArrayList<>();

    Button mBtnStrategy_New, mBtnStrategy_Save, mBtnStrategy_Delete, mBtnStrategy_Change;


    @Override
    protected void initializeViews() {
        setupStrategies();

        mBtnStrategy_New = (Button) findViewById(R.id.btnStrategy_New);
        mBtnStrategy_Save = (Button) findViewById(R.id.btnStrategy_Save);
        mBtnStrategy_Delete = (Button) findViewById(R.id.btnStrategy_Delete);
        mBtnStrategy_Change = (Button) findViewById(R.id.btnStrategy_Change);

        // selecting strategy
        mLvStrategies = (ListView) findViewById(R.id.lvStrategies);
        mStrategyAdapter = new StrategyAdapter(getActivity(), R.layout.row_strategy_action, mStrategies);
        mLvStrategies.setAdapter(mStrategyAdapter);
        mLvStrategies.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Strategy strategy = mStrategyAdapter.getItem(position);
                if (strategy != mStrategyActionAdapter.getStrategy()) {
                    mStrategyActionAdapter.setStrategy(strategy);
                }
            }
        });

        mTvSelectedStrategyTitle = (TextView) findViewById(R.id.tvSelectedStrategyTitle);
        mLvSelectedStrategy = (ListView) findViewById(R.id.lvSelectedStrategy);

        mStrategyActionAdapter = new StrategyActionAdapter(getActivity(), R.layout.row_strategy_action,
                mStrategyActions);
        mLvSelectedStrategy.setAdapter(mStrategyActionAdapter);
        mStrategyActionAdapter.setStrategy(mStrategies.get(0));

        mBtnStrategy_New.setOnClickListener(mStrategyActionAdapter);
        mBtnStrategy_Save.setOnClickListener(mStrategyActionAdapter);
        mBtnStrategy_Delete.setOnClickListener(mStrategyActionAdapter);
        mBtnStrategy_Change.setOnClickListener(mStrategyActionAdapter);
    }

    class StrategyAdapter extends ArrayAdapter<Strategy> {

        public StrategyAdapter(Context context, int resource, ArrayList<Strategy> objects) {
            super(context, resource, objects);
        }

        class StrategyDataHandler {
            TextView mTvTitle, mTvDescription;

            public StrategyDataHandler(View itemView) {
                mTvTitle = (TextView) itemView.findViewById(R.id.tvRowStartegyTitle);
                mTvDescription = (TextView) itemView.findViewById(R.id.tvRowStartegyDescription);
            }

            public void setData(int position) {
                Strategy strategy = getItem(position);

                mTvTitle.setTextColor(strategy.isEditable() ?
                        Color.rgb(50, 180, 50) : Color.rgb(50, 50, 180));
                mTvTitle.setText(strategy.getTitle());

                mTvDescription.setText(strategy.getDescription());
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View itemView;
            StrategyDataHandler handler;
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) this.getContext()
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                itemView = inflater.inflate(R.layout.row_strategy, parent, false);
                handler = new StrategyDataHandler(itemView);
                itemView.setTag(handler);
            } else {
                itemView = convertView;
                handler = (StrategyDataHandler) itemView.getTag();
            }

            handler.setData(position);
            return itemView;
        }
    }

    class StrategyActionAdapter extends ArrayAdapter<Strategy.SingleActions>
            implements View.OnClickListener {
        Strategy mStrategy;
        TextView mSelected = null, mSelected_prev = null;
        int mSelectedActionPosition, mSelectedUpIndex;
        int mSelectedActionPosition_prev, mSelectedUpIndex_prev;

        public Strategy getStrategy() {
            return mStrategy;
        }

        public PopupMenu mPlayerActionMenu;

        public StrategyActionAdapter(Context context, int resource,
                                     ArrayList<Strategy.SingleActions> objects) {
            super(context, resource, objects);
            mPlayerActionMenu = new PopupMenu(getActivity(), mBtnStrategy_Change);
            Menu menu = mPlayerActionMenu.getMenu();
            menu.add(0, 0, 0, "Hit(H)");
            menu.add(0, 1, 1, "Stand(S)");
            menu.add(0, 2, 2, "Split(P)");
            menu.add(0, 3, 3, "Doubledown(D)");
            menu.add(0, 4, 4, "Surrender or Hit(Rh)");
            menu.add(0, 5, 5, "Surrender or Stand(Rs)");

            mPlayerActionMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    PlayerHand.Action action;
                    if (mSelected != null) {
                        switch (item.getItemId()) {
                            case 0:
                                action = PlayerHand.Action.HIT;
                                break;
                            default:
                            case 1:
                                action = PlayerHand.Action.STAND;
                                break;
                            case 2:
                                action = PlayerHand.Action.SPLIT;
                                break;
                            case 3:
                                action = PlayerHand.Action.DOUBLEDOWN;
                                break;
                            case 4:
                                action = PlayerHand.Action.SURRENDER_OR_HIT;
                                break;
                            case 5:
                                action = PlayerHand.Action.SURRENDER_OR_STAND;
                                break;
                        }
                        mStrategyActions.get(mSelectedActionPosition).mActions[mSelectedUpIndex] = action;
                        mSelected.setText(Strategy.getCodeByAction(action));
                    }
                    return true;
                }
            });
        }

        public void setStrategy(Strategy strategy) {
            if (mStrategy == strategy) return;

            mStrategy = strategy;
            updateStrategyActions();
            mTvSelectedStrategyTitle.setBackgroundColor(mStrategy.isEditable() ?
                    Color.rgb(50, 180, 50) : Color.rgb(50, 50, 180));
            notifyDataSetChanged();
        }


        void selectActionCell(TextView v, int selectedActionPosition, int selectedUpIndex) {
            if (mSelected != v) {
                mSelected_prev = mSelected;
                mSelectedActionPosition_prev = mSelectedActionPosition;
                mSelectedUpIndex_prev = mSelectedUpIndex;

                mSelected = v;
                mSelectedActionPosition = selectedActionPosition;
                mSelectedUpIndex = selectedUpIndex;

                if (mSelected_prev != null) {
                    PlayerHand.Action action_prev =
                            getItem(mSelectedActionPosition_prev).mActions[mSelectedUpIndex_prev];
                    mSelected_prev.setBackgroundColor(Strategy.getColorByAction(action_prev));
                    mSelected_prev.setTextColor(Color.BLACK);
                }

                if (mSelected != null) {
                    mSelected.setBackgroundColor(Color.MAGENTA);
                    mSelected.setTextColor(Color.BLUE);
                }
            }
        }

        private void updateStrategyActions() {
            mStrategyActions.clear();

            for (Strategy.SingleActions actions : mStrategy.mHardActions) {
                mStrategyActions.add(actions);
            }
            for (Strategy.SingleActions actions : mStrategy.mSoftActions) {
                mStrategyActions.add(actions);
            }
            for (Strategy.SingleActions actions : mStrategy.mPairActions) {
                mStrategyActions.add(actions);
            }
            mSelected_prev = mSelected = null;
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btnStrategy_New:
                    Strategy newStrategy = null;
                    try {
                        newStrategy = (Strategy) mStrategy.clone();
                    } catch (CloneNotSupportedException e) {
                        e.printStackTrace();
                    }

                    if (newStrategy != null) {
                        mStrategyAdapter.add(newStrategy);
                        mStrategyActionAdapter.setStrategy(newStrategy);
                    }

                    break;
                case R.id.btnStrategy_Save:
                    break;
                case R.id.btnStrategy_Delete:
                    break;
                case R.id.btnStrategy_Change:
                    if (mSelected != null) {
                        mPlayerActionMenu.show();
                    }
                    break;
            }
        }

        class StrategyActionDataHandler {
            LinearLayout mLoActions;

            public StrategyActionDataHandler(View itemView, int position) {
                mLoActions = (LinearLayout) itemView.findViewById(R.id.loStrategyAction);

                for (int upIndex = 0; upIndex < Strategy.COUNT_UP_SCORES; upIndex++) {
                    TextView tvActionCode = (TextView) mLoActions.getChildAt(upIndex + 1);
                    final int selectedUpIndex = upIndex;
                    final int selectedActionsPosition = position;
                    tvActionCode.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            selectActionCell((TextView) v, selectedActionsPosition, selectedUpIndex);
                        }
                    });
                }
            }

            public void setData(int position) {
                if (mStrategy == null) return;

                mTvSelectedStrategyTitle.setText(mStrategy.getTitle());

                String title;
                int titleColor;
                if (position < Strategy.COUNT_HARD_ACTIONS) {
                    title = Strategy.sHardTitles[position];
                    titleColor = Color.rgb(200, 200, 200);
                } else if (position < (Strategy.COUNT_HARD_ACTIONS + Strategy.COUNT_SOFT_ACTIONS)) {
                    title = Strategy.sSoftTitles[position - Strategy.COUNT_HARD_ACTIONS];
                    titleColor = Color.rgb(230, 230, 230);
                } else {
                    title = Strategy.sPairTitles[position - Strategy.COUNT_HARD_ACTIONS
                            - Strategy.COUNT_SOFT_ACTIONS];
                    titleColor = Color.rgb(180, 240, 240);
                }

                TextView tvTitle = (TextView) mLoActions.getChildAt(0);
                tvTitle.setText(title);
                tvTitle.setBackgroundColor(titleColor);

                Strategy.SingleActions actions = getItem(position);

                for (int upIndex = 0; upIndex < Strategy.COUNT_UP_SCORES; upIndex++) {
                    TextView tvActionCode = (TextView) mLoActions.getChildAt(upIndex + 1);
                    if (tvActionCode == mSelected) {
                        mSelected.setBackgroundColor(Color.MAGENTA);
                        mSelected.setTextColor(Color.BLUE);
                    } else {
                        tvActionCode.setTextColor(Color.BLACK);
                        tvActionCode.setBackgroundColor(Strategy.getColorByAction
                                (actions.mActions[upIndex]));
                    }
                    tvActionCode.setText(Strategy.getCodeByAction
                            (actions.mActions[upIndex]));
                }

                mBtnStrategy_Save.setVisibility(mStrategy.isEditable() ? View.VISIBLE : View.GONE);
                mBtnStrategy_Delete.setVisibility(mStrategy.isEditable() ? View.VISIBLE : View.GONE);
                mBtnStrategy_Change.setVisibility(mStrategy.isEditable() ? View.VISIBLE : View.GONE);
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View itemView;
            StrategyActionDataHandler handler;
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) this.getContext()
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                itemView = inflater.inflate(R.layout.row_strategy_action, parent, false);
                handler = new StrategyActionDataHandler(itemView, position);
                itemView.setTag(handler);
            } else {
                itemView = convertView;
                handler = (StrategyActionDataHandler) itemView.getTag();
            }

            handler.setData(position);
            return itemView;
        }
    }


    @Override
    public void onClick(View v) {

    }

    @Override
    protected void setPrivateTag() {
        privateTag = "STRATEGY";
    }

    private void setupStrategies() {
        mStrategies = ActivityStudio.sStudio.getStrategies();
        mStrategies.addAll(Strategy.makePredefinedStrategies());

    }
}
