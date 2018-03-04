package com.lgsdiamond.blackjackstudio;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import com.lgsdiamond.blackjackstudio.BlackjackElement.Better;
import com.lgsdiamond.blackjackstudio.BlackjackUtils.SettingFactory;

import java.util.ArrayList;

public class FragmentBetter extends FragmentStudioBase {
    public ArrayList<SettingFactory.BjSetting> mBetterSettings = new ArrayList<>();

    public static FragmentBetter newInstance() {
        FragmentBetter fragment = new FragmentBetter();
        FragmentStudioBase.newInstance(fragment, R.layout.fragment_better);

        fragment.mBetters = ActivityStudio.sStudio.getBetters();

        return fragment;
    }

    ListView mLvBetters;
    BetterAdapter mBetterAdapter;


    EditText mEtTitle, mEtDescription, mEtActionValue, mEtMaxLevelCount, mEtSequence;
    TextView mTvLabelTitle, mTvLabelDescription, mTvLabelActionValue, mTvLabelMaxLevelCount,
            mTvLabelActionBase, mTvLabelSequence;
    Switch mSwitchActionBase;


    @Override
    protected void initializeViews() {

        // listView and adapter
        mLvBetters = (ListView) findViewById(R.id.lvBetterSetting_Betting);
        mBetterAdapter = new BetterAdapter(getActivity(), R.layout.row_better, mBetters);
        mLvBetters.setAdapter(mBetterAdapter);
        mLvBetters.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Better better = mBetters.get(position);
                mEtTitle.setText(better.getTitle());
                mEtDescription.setText(better.getDescription());
                mEtActionValue.setText(String.valueOf(better.getActionValue()));

                // actionValue
                if ((better.mAction == Better.BetterAction.STRONG_MARTINGALE) ||
                        (better.mAction == Better.BetterAction.FLAT) ||
                        (better.mAction == Better.BetterAction.OSCAR) ||
                        (better.mAction == Better.BetterAction.SEQUENCE)) {
                    mTvLabelActionValue.setVisibility(View.GONE);
                    mEtActionValue.setVisibility(View.GONE);
                } else {
                    mTvLabelActionValue.setVisibility(View.VISIBLE);
                    mEtActionValue.setVisibility(View.VISIBLE);
                }

                if (mEtActionValue.getVisibility() == View.VISIBLE) {
                    mEtActionValue.setText(String.valueOf(better.mActionValue));
                }

                // maxCutLevelCount
                if ((better.mAction == Better.BetterAction.FLAT) ||
                        (better.mAction == Better.BetterAction.PROPORTIONAL) ||
                        (better.mAction == Better.BetterAction.SEQUENCE)) {
                    mTvLabelMaxLevelCount.setVisibility(View.GONE);
                    mEtMaxLevelCount.setVisibility(View.GONE);
                } else {
                    mTvLabelMaxLevelCount.setVisibility(View.VISIBLE);
                    mEtMaxLevelCount.setVisibility(View.VISIBLE);
                }

                if (mEtMaxLevelCount.getVisibility() == View.VISIBLE) {
                    mEtMaxLevelCount.setText(String.valueOf(better.mMaxCutLevelCount));
                }

                // actionBase
                if ((better.mAction == Better.BetterAction.FLAT) ||
                        (better.mAction == Better.BetterAction.OSCAR) ||
                        (better.mAction == Better.BetterAction.PROPORTIONAL) ||
                        (better.mAction == Better.BetterAction.STRONG_MARTINGALE)) {
                    mTvLabelActionBase.setVisibility(View.GONE);
                    mSwitchActionBase.setVisibility(View.GONE);
                } else {
                    mTvLabelActionBase.setVisibility(View.GONE);
                    mSwitchActionBase.setVisibility(View.GONE);
                }

                if (mSwitchActionBase.getVisibility() == View.VISIBLE) {
                    mSwitchActionBase.setChecked(better.mBetterActionBase ==
                            Better.BetterActionBase.WIN);
                }

                // sequence
                if (better.mAction == Better.BetterAction.SEQUENCE) {
                    mTvLabelSequence.setVisibility(View.VISIBLE);
                    mEtSequence.setVisibility(View.VISIBLE);
                } else {
                    mTvLabelSequence.setVisibility(View.GONE);
                    mEtSequence.setVisibility(View.GONE);
                }

                if (mEtSequence.getVisibility() == View.VISIBLE) {
                    mEtSequence.setText(better.getSequenceString());
                }
            }
        });
        mLvBetters.setOnItemSelectedListener(new ListView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // listView and adapter
        mTvLabelTitle = (TextView) findViewById(R.id.tvLabelTitle_Better);
        mEtTitle = (EditText) findViewById(R.id.etTitle_Better);

        mTvLabelDescription = (TextView) findViewById(R.id.tvLabelDescription_Better);
        mEtDescription = (EditText) findViewById(R.id.etDescription_Better);

        mTvLabelActionValue = (TextView) findViewById(R.id.tvLabelActionValue_Better);
        mEtActionValue = (EditText) findViewById(R.id.etActionValue_Better);

        mTvLabelMaxLevelCount = (TextView) findViewById(R.id.tvLabelMaxLevelCount_Better);
        mEtMaxLevelCount = (EditText) findViewById(R.id.etMaxLevelCount_Better);

        mTvLabelMaxLevelCount = (TextView) findViewById(R.id.tvLabelMaxLevelCount_Better);
        mEtMaxLevelCount = (EditText) findViewById(R.id.etMaxLevelCount_Better);


        mTvLabelActionBase = (TextView) findViewById(R.id.tvLabelActionBase_Better);
        mSwitchActionBase = (Switch) findViewById(R.id.switchActionBase_Banner);
        mSwitchActionBase.setShowText(true);
        mSwitchActionBase.setTextOff("Lost");
        mSwitchActionBase.setTextOn("Win");

        mTvLabelSequence = (TextView) findViewById(R.id.tvLabelSequence_Better);
        mEtSequence = (EditText) findViewById(R.id.etSequence_Better);

        Button btnTest = (Button) findViewById(R.id.btnTest_Betting);
        btnTest.setOnClickListener(this);
    }

    String[] mDefinedBettersStrings;

    public String[] getDefinedBettersStrings() {
        if (mDefinedBettersStrings == null) {
            int index = 0;
            mDefinedBettersStrings = new String[mBetters.size()];
            for (Better better : mBetters) {
                mDefinedBettersStrings[index++] = better.getTitle();
            }
        }

        return mDefinedBettersStrings;
    }

    ArrayList<Better> mBetters;


    private String[] getDefinedBetterStrings() {
        String[] defStrings = new String[mBetters.size()];
        int index = 0;
        for (Better better : mBetters) {
            defStrings[index++] = better.getTitle();
        }

        return defStrings;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnTest_Betting:
                break;
        }
    }

    @Override
    protected void setPrivateTag() {
        privateTag = "BETTING";
    }

    public class BetterAdapter extends ArrayAdapter<Better> {
        public BetterAdapter(Context context, int resource, ArrayList<Better> objects) {
            super(context, resource, objects);
        }

        public class BetterDataHandler {
            TextView tvTitle, tvDescription;

            public BetterDataHandler(View view) {
                tvTitle = (TextView) view.findViewById(R.id.tvTitle_Better);
                tvDescription = (TextView) view.findViewById(R.id.tvDescription_Better);
            }

            public void setData(int position) {
                Better better = getItem(position);
                tvTitle.setText(better.getTitle());
                tvDescription.setText(better.getDescription());
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View itemView;
            BetterDataHandler dataHandler;
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) this.getContext()
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                itemView = inflater.inflate(R.layout.row_better, parent, false);
                dataHandler = new BetterDataHandler(itemView);
                itemView.setTag(dataHandler);
            } else {
                itemView = convertView;
                dataHandler = (BetterDataHandler) convertView.getTag();
            }
            dataHandler.setData(position);

            return itemView;
        }
    }
}