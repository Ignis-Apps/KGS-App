package de.kgs.vertretungsplan.slide;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Andreas on 17.01.2018.
 */

public class ListViewPagerAdapter extends FragmentPagerAdapter {

    private List<Fragment> fragments = new ArrayList<>();

    public ListViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {

        if(position<fragments.size()){
            return fragments.get(position);
        }
        return null;

    }

    public void addFragment(Fragment f){
        fragments.add(f);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

}
