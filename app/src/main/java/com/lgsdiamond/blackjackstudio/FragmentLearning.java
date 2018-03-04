package com.lgsdiamond.blackjackstudio;

import android.view.View;

public class FragmentLearning extends FragmentStudioBase {
    public static FragmentLearning newInstance() {
        FragmentLearning fragment = new FragmentLearning();
        FragmentStudioBase.newInstance(fragment, R.layout.fragment_learning);
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
        privateTag = "LEARNING";
    }
}
