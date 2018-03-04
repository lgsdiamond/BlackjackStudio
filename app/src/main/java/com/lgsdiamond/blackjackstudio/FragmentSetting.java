package com.lgsdiamond.blackjackstudio;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.lgsdiamond.blackjackstudio.BlackjackElement.BjService;
import com.lgsdiamond.blackjackstudio.BlackjackUtils.SettingAdapter;
import com.lgsdiamond.blackjackstudio.BlackjackUtils.SettingFactory;

import java.util.ArrayList;

public class FragmentSetting extends FragmentStudioBase {
    BjService mService;
    SettingFactory mSettingFactory = null;

    public static FragmentSetting newInstance(BjService service) {
        FragmentSetting fragment = new FragmentSetting();
        FragmentStudioBase.newInstance(fragment, R.layout.fragment_setting);

        fragment.setService(service);

        return fragment;
    }

    private void setService(BjService service) {
        mService = service;
    }

    TextView mTvRuleText;
    ListView mLvSetting;

    @Override
    protected void initializeViews() {
        if (mSettingFactory == null) {
            mSettingFactory = new SettingFactory(mService.pGameRule);
            mGameSetting.addAll(mSettingFactory.getDealerSettings());
            mGameSetting.addAll(mSettingFactory.getDoubledownSettings());
            mGameSetting.addAll(mSettingFactory.getSplitSettings());
            mGameSetting.addAll(mSettingFactory.getTableSettings(false));
        }

        mTvRuleText = (TextView) findViewById(R.id.tvRuleText_Setting);

        ImageView ivAccept = (ImageView) findViewById(R.id.ivSettingAccept);
        ivAccept.setOnClickListener(this);
        ImageView ivCancel = (ImageView) findViewById(R.id.ivSettingCancel);
        ivCancel.setOnClickListener(this);

        mLvSetting = (ListView) findViewById(R.id.lvSetting);

        mSettingAdapter = new SettingAdapter(getActivity(),
                R.id.loSettingRow, mGameSetting, mTvRuleText);
        mLvSetting.setAdapter(mSettingAdapter);

        mSettingFactory.setSettingAdapter(mSettingAdapter);
    }

    @Override
    protected void setPrivateTag() {
        privateTag = "SETTING";
    }

    SettingAdapter mSettingAdapter;
    ArrayList<SettingFactory.BjSetting> mGameSetting = new ArrayList<>();

    @Override
    public void onClick(View v) {
        boolean needUpdate = needUpdate();

        switch (v.getId()) {

            case R.id.ivSettingAccept:
                if (needUpdate) {
                    AlertDialog.Builder alert_confirm = new AlertDialog.Builder(getActivity());
                    alert_confirm.setMessage("Save changed settings?").setCancelable(false)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    updateSetting();
                                    getActivity().onBackPressed();
                                }
                            }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            return;
                        }
                    });
                    AlertDialog alert = alert_confirm.create();
                    alert.show();
                } else {
                    getActivity().onBackPressed();
                }
                break;
            case R.id.ivSettingCancel:
                if (needUpdate) {
                    AlertDialog.Builder alert_confirm = new AlertDialog.Builder(getActivity());
                    alert_confirm.setMessage("Discard changed settings?").setCancelable(false)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    getActivity().onBackPressed();
                                }
                            }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            return;
                        }
                    });
                    AlertDialog alert = alert_confirm.create();
                    alert.show();
                } else {
                    getActivity().onBackPressed();
                }
                break;
        }
    }

    public boolean needUpdate() {
        boolean needUpdate = false;

        for (SettingFactory.BjSetting setting : mGameSetting) {
            if (setting.needUpdate()) {
                needUpdate = true;
                break;
            }
        }

        return needUpdate;
    }

    //=== adding settings ===
    private void updateSetting() {
        for (SettingFactory.BjSetting setting : mGameSetting) {
            if (setting.needUpdate()) setting.settingRuleOut();
            setting.mTouched = false;
        }
    }
}
