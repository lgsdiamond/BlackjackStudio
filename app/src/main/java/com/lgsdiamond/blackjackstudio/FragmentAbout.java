package com.lgsdiamond.blackjackstudio;

import android.view.View;

public class FragmentAbout extends FragmentStudioBase {
    public static FragmentAbout newInstance() {
        FragmentAbout fragment = new FragmentAbout();
        FragmentStudioBase.newInstance(fragment, R.layout.fragment_about);
        return fragment;
    }

    @Override
    protected void initializeViews() {

    }

    @Override
    protected void setPrivateTag() {
        privateTag = "ABOUT";
    }

    @Override
    public void onClick(View v) {

    }
}
