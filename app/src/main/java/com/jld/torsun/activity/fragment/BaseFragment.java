package com.jld.torsun.activity.fragment;

import android.support.v4.app.Fragment;

public abstract class BaseFragment extends Fragment {


    private long currentTimeMillis = 0;

    protected boolean isFragmentHidden;
    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden){
            isFragmentHidden = true;
            onHideen();
        }else {
            isFragmentHidden = false;
            if (currentTimeMillis != 0 && (System.currentTimeMillis() - currentTimeMillis) < 3000) {
                currentTimeMillis = System.currentTimeMillis();
                return;
            }
            currentTimeMillis = System.currentTimeMillis();
            onShow();
        }

    }

    protected void onShow(){onShowLoad();}
    protected void onHideen(){}
    protected abstract void onShowLoad();
}
