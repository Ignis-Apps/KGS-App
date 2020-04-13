package de.kgs.vertretungsplan.views.fragments;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener;

import java.util.ArrayList;
import java.util.List;

import de.kgs.vertretungsplan.R;
import de.kgs.vertretungsplan.broadcaster.Broadcast;
import de.kgs.vertretungsplan.broadcaster.BroadcastEvent;
import de.kgs.vertretungsplan.coverPlan.CoverItem;
import de.kgs.vertretungsplan.coverPlan.CoverPlan;
import de.kgs.vertretungsplan.manager.firebase.Analytics;
import de.kgs.vertretungsplan.singetones.ApplicationData;
import de.kgs.vertretungsplan.views.adapters.CoverItemListAdapter;
import de.kgs.vertretungsplan.views.dialogs.CoverItemInfo;

public class CoverPlanFragment extends Fragment implements OnItemClickListener {

    public enum PresentedDataSet {
        TODAY,
        TOMORROW
    }

    private CoverItemListAdapter arrayAdapter;
    private Broadcast broadcast;
    private PresentedDataSet presentedDataSet;
    private List<CoverItem> values = new ArrayList<>();
    private RefreshHandler refreshHandler;

    public CoverPlanFragment(Broadcast broadcast, PresentedDataSet presentedDataSet) {
        this.broadcast = broadcast;
        this.presentedDataSet = presentedDataSet;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.arrayAdapter = new CoverItemListAdapter(getContext(), this.values);
        View fragmentView = inflater.inflate(R.layout.listview_fragment, container, false);

        ListView listView = fragmentView.findViewById(R.id.fragment_listview);
        SwipeRefreshLayout refreshLayout = fragmentView.findViewById(R.id.fragment_listview_refresh);

        listView.setOnItemClickListener(this);
        listView.setAdapter(this.arrayAdapter);
        refreshHandler = new RefreshHandler(refreshLayout, this.broadcast);
        listView.setOnScrollListener(refreshHandler);

        refreshDataSet();

        broadcast.subscribe(broadcastEvent -> refreshDataSet(),
                BroadcastEvent.DATA_PROVIDED,
                BroadcastEvent.CURRENT_GRADE_CHANGED,
                BroadcastEvent.CURRENT_CLASS_CHANGED);

        return fragmentView;
    }

    private void refreshDataSet() {

        CoverPlan coverPlan = null;
        if (presentedDataSet == PresentedDataSet.TODAY)
            coverPlan = ApplicationData.getInstance().getCoverPlanToday();
        else if (presentedDataSet == PresentedDataSet.TOMORROW)
            coverPlan = ApplicationData.getInstance().getCoverPlanTomorrow();

        if (coverPlan == null)
            return;

        arrayAdapter.setDataSet(coverPlan.getCoverItemsFiltered());
        arrayAdapter.setDailyMessage(coverPlan.getDailyInfoHead(), coverPlan.getDailyInfoMessage());
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

        if (position == 0 && this.arrayAdapter.hasDailyMessage()) {
            return;
        }
        if (position == this.values.size() - 1) {
            Analytics.getInstance().logEvent("rate_app");
            try {
                startActivity(new Intent("android.intent.action.VIEW", Uri.parse("market://details?id=de.kgs.vertretungsplan")));
            } catch (ActivityNotFoundException e) {
                startActivity(new Intent("android.intent.action.VIEW", Uri.parse("https://play.google.com/store/apps/details?id=de.kgs.vertretungsplan")));
            }
            return;
        }
        CoverItemInfo.showDialog(getContext(), this.values.get(position));
    }

    public void refreshLayoutSetEnabled(boolean enabled) {
        if (refreshHandler == null)
            return;
        refreshHandler.refreshLayout.setEnabled(enabled);
    }

    private static class RefreshHandler implements OnScrollListener, OnRefreshListener {

        private Broadcast broadcaster;
        private SwipeRefreshLayout refreshLayout;

        RefreshHandler(SwipeRefreshLayout refreshLayout, Broadcast broadcast) {
            this.refreshLayout = refreshLayout;
            this.broadcaster = broadcast;
            refreshLayout.setOnRefreshListener(this);
        }

        @Override
        public void onScrollStateChanged(AbsListView absListView, int i) {
        }

        @Override
        public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleCount, int totalItemCount) {

            int topPosition = 0;
            boolean z = false;
            if (absListView != null && totalItemCount > 0) {
                topPosition = absListView.getChildAt(0).getTop();
            }

            if (topPosition >= 0) {
                z = true;
            }

            if (z)
                System.out.println("ENABLING");

            refreshLayout.setEnabled(z);
        }

        @Override
        public void onRefresh() {
            broadcaster.send(BroadcastEvent.REQUEST_DATA_RELOAD);
            this.refreshLayout.setRefreshing(false);
        }
    }
}
