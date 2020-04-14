package de.kgs.vertretungsplan.ui.adapters;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import de.kgs.vertretungsplan.broadcaster.Broadcast;
import de.kgs.vertretungsplan.ui.fragments.BlackboardFragment;
import de.kgs.vertretungsplan.ui.fragments.CoverPlanFragment;

public class ViewPageAdapter extends FragmentStateAdapter {

    private Broadcast broadcast;

    public ViewPageAdapter(@NonNull FragmentActivity fragmentActivity, Broadcast broadcast) {
        super(fragmentActivity);
        this.broadcast = broadcast;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {

        switch (position) {
            case 0:
                return new BlackboardFragment(broadcast);
            case 1:
                return new CoverPlanFragment(broadcast, CoverPlanFragment.PresentedDataSet.TODAY);
            case 2:
                return new CoverPlanFragment(broadcast, CoverPlanFragment.PresentedDataSet.TOMORROW);
            default:
                throw new AssertionError("Fragment not provided");

        }
    }

    @Override
    public int getItemCount() {
        return 3; // blackboard, today, tomorrow
    }
}