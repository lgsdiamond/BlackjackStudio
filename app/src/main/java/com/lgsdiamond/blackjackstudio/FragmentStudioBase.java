package com.lgsdiamond.blackjackstudio;

import android.app.Activity;
import android.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public abstract class FragmentStudioBase extends Fragment implements View.OnClickListener {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private OnFragmentInteractionListener mListener;

    // for Blackjack Studio use
    protected String privateTag;

    private int mLayoutId;
    private Boolean mPreserve = false;

    public static FragmentStudioBase newInstance(FragmentStudioBase fragment, int layoutID) {
        Bundle args = new Bundle();
        fragment.setArguments(args);

        fragment.setLayoutId(layoutID);
        fragment.setPrivateTag();

        return fragment;
    }

    protected final void setLayoutId(int layoutId) {
        mLayoutId = layoutId;
    }

    protected final void setPreserve(Boolean preserve) {
        mPreserve = preserve;
    }

    public Boolean getPreserve() {
        return mPreserve;
    }

    public String getPrivateTag() {
        return privateTag;
    }

    public FragmentStudioBase() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected View mTopContainer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (container == null) return null;

        // Inflate the layout for this fragment
        return inflater.inflate(mLayoutId, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mTopContainer = view;

        initializeViews();
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    public View findViewById(int id) {
        return (mTopContainer == null) ? null : mTopContainer.findViewById(id);
    }

    protected abstract void initializeViews();

    protected abstract void setPrivateTag();
}
