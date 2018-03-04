package com.lgsdiamond.blackjackstudio.BlackjackUtils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.lgsdiamond.blackjackstudio.R;

import java.util.ArrayList;

public class SettingAdapter extends ArrayAdapter<SettingFactory.BjSetting> {
    ArrayList<SettingFactory.BjSetting> mSettings;
    TextView mTvSettingsText;

    public SettingAdapter(Context context, int resource, ArrayList<SettingFactory.BjSetting> settings,
                          TextView tvSettingText) {
        super(context, resource, settings);
        mSettings = settings;
        mTvSettingsText = tvSettingText;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View itemView;
        SettingDataHandler handler;
        SettingFactory.BjSetting setting = mSettings.get(position);

        // we do not use convert view this time
        int layoutId = -1;

        if (setting instanceof SettingFactory.SwitchSetting) {
            layoutId = R.layout.row_setting_switch;
        } else if (setting instanceof SettingFactory.SpinnerTwoSetting) {  // check this first, sub-class
            layoutId = R.layout.row_setting_spinners_two;
        } else if (setting instanceof SettingFactory.SpinnerSetting) {     // check this later, super-class
            layoutId = R.layout.row_setting_spinner;
        }

        if (layoutId == -1) return null;    // something wrong

        LayoutInflater inflater = (LayoutInflater) this.getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        itemView = inflater.inflate(layoutId, parent, false);

        handler = new SettingDataHandler(itemView, setting);

        itemView.setTag(handler);
        handler.setData();

        return itemView;
    }

    public void updateSettingsText() {
        if (mTvSettingsText != null)
            mTvSettingsText.setText(getRuleString());
    }

    public String getRuleString() {
        String ruleString = "";
        for (SettingFactory.BjSetting setting : mSettings) {
            ruleString += "[" + setting.toString() + "] ";
        }
        return ruleString;
    }

    public class SettingDataHandler {

        TextView mTvSectionTitle;
        TextView mTvTitle;
        TextView mTvDescription;
        Switch mSwitch;
        Spinner mSpinner, mSpinnerTwo;
        SettingFactory.BjSetting mSetting;

        public SettingDataHandler(View view, SettingFactory.BjSetting setting) {
            mSetting = setting;

            mTvSectionTitle = (TextView) view.findViewById(R.id.tvSettingRowSectionTitle);
            mTvTitle = (TextView) view.findViewById(R.id.tvSettingRowTitle);
            mTvDescription = (TextView) view.findViewById(R.id.tvSettingRowDescription);

            if (mSetting instanceof SettingFactory.SwitchSetting) {
                mSwitch = (Switch) view.findViewById(R.id.switchSettingRow);
                ((SettingFactory.SwitchSetting) setting).setSwitch(mSwitch);

                mSwitch.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        ((SettingFactory.SwitchSetting) mSetting).mIsChecked = isChecked;

                        if (mSetting.needUpdate()) {
                            mTvTitle.setTextColor(UtilityStudio.sColor_BestAction);
                        } else {
                            mTvTitle.setTextColor(UtilityStudio.sColor_Confirmed);
                        }

                        updateSettingsText();

                        ((SettingFactory.SwitchSetting) mSetting).postSwitchCheckedChange();
                    }
                });

            } else if (mSetting instanceof SettingFactory.SpinnerSetting) {    // spinner and spinnerTwo, common
                mSpinner = (Spinner) view.findViewById(R.id.spinnerSettingRow);
                ((SettingFactory.SpinnerSetting) setting).setSpinner(mSpinner);

                mSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        ((SettingFactory.SpinnerSetting) mSetting).mSelectedPosition = position;

                        if (mSetting.needUpdate()) {
                            mTvTitle.setTextColor(UtilityStudio.sColor_BestAction);
                        } else {
                            mTvTitle.setTextColor(UtilityStudio.sColor_Confirmed);
                        }

                        updateSettingsText();

                        ((SettingFactory.SpinnerSetting) mSetting).postSpinnerItemSelected();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });

                // additionally, for SpinnerToSetting
                if (mSetting instanceof SettingFactory.SpinnerTwoSetting) {    // spinnerTwo only
                    mSpinnerTwo = (Spinner) view.findViewById(R.id.spinnerTwoSettingRow);
                    ((SettingFactory.SpinnerTwoSetting) setting).setSpinnerTwo(mSpinnerTwo);

                    mSpinnerTwo.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            ((SettingFactory.SpinnerTwoSetting) mSetting).mSelectedPositionTwo = position;

                            if (mSetting.needUpdate()) {
                                mTvTitle.setTextColor(UtilityStudio.sColor_BestAction);
                            } else {
                                mTvTitle.setTextColor(UtilityStudio.sColor_Confirmed);
                            }

                            updateSettingsText();

                            ((SettingFactory.SpinnerTwoSetting) mSetting).postSpinnerTwoItemSelected();
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                        }
                    });
                }
            }
        }

        public void setData() {
            if (mSetting.mSectionTitle.isEmpty()) {
                mTvSectionTitle.setVisibility(View.GONE);
            } else {
                mTvSectionTitle.setVisibility(View.VISIBLE);
                mTvSectionTitle.setText(mSetting.mSectionTitle);
            }

            mTvTitle.setText(mSetting.mTitle);
            mTvDescription.setText(mSetting.mDescription);

            if (mSetting instanceof SettingFactory.SwitchSetting) {
//??                mSwitch.setShowText(true);
                mSwitch.setTextOff(mSetting.mDataStrings[SettingFactory.SwitchSetting.INDEX_OFF]);
                mSwitch.setTextOn(mSetting.mDataStrings[SettingFactory.SwitchSetting.INDEX_ON]);

            } else if (mSetting instanceof SettingFactory.SpinnerSetting) {    // spinner of spinnerTwo, common
                ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(getContext(),
                        android.R.layout.simple_spinner_dropdown_item,
                        mSetting.mDataStrings);
                dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                mSpinner.setAdapter(dataAdapter);

                if (mSetting instanceof SettingFactory.SpinnerTwoSetting) {    // spinnerTwo only
                    ArrayAdapter<String> dataAdapterTwo = new ArrayAdapter<>(getContext(),
                            android.R.layout.simple_spinner_dropdown_item,
                            ((SettingFactory.SpinnerTwoSetting) mSetting).mDataStringsTwo);
                    dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    mSpinnerTwo.setAdapter(dataAdapterTwo);
                }
            }

            mSetting.settingRuleIn();
        }
    }
}