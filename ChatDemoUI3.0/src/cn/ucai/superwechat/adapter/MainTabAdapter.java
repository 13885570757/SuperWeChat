package cn.ucai.superwechat.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/11/3.
 */
public class MainTabAdapter extends FragmentStatePagerAdapter {
    private final List<Fragment> mFragment = new ArrayList<>();
    private final List<String> mFragmentTitle = new ArrayList();
    private FragmentManager fm;

    public MainTabAdapter(FragmentManager fm) {
        super(fm);
        this.fm = fm;
        this.saveState();
    }

    public void addFragment(Fragment fragment, String title) {
        mFragment.add(fragment);
        mFragmentTitle.add(title);
    }

    public void clear() {
        mFragmentTitle.clear();
        mFragment.clear();
        notifyDataSetChanged();
    }

    @Override
    public Fragment getItem(int position) {

        return mFragment.get(position);
    }

    @Override
    public int getCount() {
        return mFragment.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mFragmentTitle.get(position);
    }

    @Override
    public int getItemPosition(Object object) {
        return PagerAdapter.POSITION_NONE;
    }

}
