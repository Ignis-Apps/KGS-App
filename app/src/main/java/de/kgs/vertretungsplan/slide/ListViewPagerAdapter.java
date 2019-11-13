package de.kgs.vertretungsplan.slide;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

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
