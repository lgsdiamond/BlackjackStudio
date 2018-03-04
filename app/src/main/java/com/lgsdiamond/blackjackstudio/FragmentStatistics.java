package com.lgsdiamond.blackjackstudio;

import android.view.View;

public class FragmentStatistics extends FragmentStudioBase {
    public static FragmentStatistics newInstance() {
        FragmentStatistics fragment = new FragmentStatistics();
        FragmentStudioBase.newInstance(fragment, R.layout.fragment_statistics);
        return fragment;
    }

    @Override
    protected void initializeViews() {

    }

    @Override
    public void onClick(View v) {

    }

    @Override
    protected void setPrivateTag() {
        privateTag = "STATISTICS";
    }
}
