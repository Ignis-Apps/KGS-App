package de.kgs.vertretungsplan.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import de.kgs.vertretungsplan.R;
import de.kgs.vertretungsplan.broadcaster.Broadcast;
import de.kgs.vertretungsplan.broadcaster.BroadcastEvent;
import de.kgs.vertretungsplan.storage.ApplicationData;
import de.kgs.vertretungsplan.ui.adapters.CoverListAdapter;

public class CoverPlanFragment extends Fragment {

    private CoverListAdapter adapter;
    private Broadcast broadcast;
    private PresentedDataSet presentedDataSet;

    public CoverPlanFragment(Broadcast broadcast, PresentedDataSet presentedDataSet) {
        this.broadcast = broadcast;
        this.presentedDataSet = presentedDataSet;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View fragmentView = inflater.inflate(R.layout.listview_fragment, container, false);

        RecyclerView recyclerView = fragmentView.findViewById(R.id.recycler_view);

        // Prepare layout and setup list divider
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);

        // Create adapter and setup the recycler view
        adapter = new CoverListAdapter(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        // Configure swipe to refresh
        SwipeRefreshLayout refreshLayout = fragmentView.findViewById(R.id.recycler_view_refresh_layout);
        refreshLayout.setOnRefreshListener(() -> {
            broadcast.send(BroadcastEvent.REQUEST_DATA_RELOAD);
            refreshLayout.setRefreshing(false);
        });

        // Subscribe to broadcast events
        broadcast.subscribe(broadcastEvent -> refreshDataSet(),
                BroadcastEvent.DATA_PROVIDED,
                BroadcastEvent.CURRENT_GRADE_CHANGED,
                BroadcastEvent.CURRENT_CLASS_CHANGED);

        // Try to load data (if available)
        refreshDataSet();

        return fragmentView;
    }

    private void refreshDataSet() {

        switch (presentedDataSet) {
            case TODAY:
                adapter.setDataSet(ApplicationData.getInstance().getCoverPlanToday());
                return;
            case TOMORROW:
                adapter.setDataSet(ApplicationData.getInstance().getCoverPlanTomorrow());
        }

    }

    public enum PresentedDataSet {
        TODAY,
        TOMORROW
    }

}
