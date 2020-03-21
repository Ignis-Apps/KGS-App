package de.kgs.vertretungsplan.slide;

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
import de.kgs.vertretungsplan.views.dialogs.CoverItemInfo;


public class ListViewFragment extends Fragment implements OnItemClickListener {
    private StableArrayAdapter arrayAdapter;
    private Broadcast broadcast;
    private List<CoverItem> values = new ArrayList<>();

    public ListViewFragment(Broadcast broadcast2) {
        this.broadcast = broadcast2;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.arrayAdapter = new StableArrayAdapter(getContext(), this.values);
        View fragmentView = inflater.inflate(R.layout.listview_fragment, container, false);

        ListView listView = fragmentView.findViewById(R.id.fragment_listview);
        SwipeRefreshLayout refreshLayout = fragmentView.findViewById(R.id.fragment_listview_refresh);

        listView.setOnItemClickListener(this);
        listView.setAdapter(this.arrayAdapter);
        listView.setOnScrollListener(new RefreshHandler(refreshLayout, this.broadcast));

        return fragmentView;
    }

    public void setDataset(List<CoverItem> values) {
        this.arrayAdapter.setDataSet(values);
    }

    public void setDailyMessage(String title, String msg) {
        this.arrayAdapter.setDailyMessage(title, msg);
    }

    public List<CoverItem> getDataset() {
        return this.values;
    }

    public boolean isCreated() {
        return this.arrayAdapter != null;
    }

    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        String str = "android.intent.action.VIEW";
        if (position == 0 && this.arrayAdapter.hasDailyMessage()) {
            return;
        }
        if (position == this.values.size() - 1) {
            try {
                startActivity(new Intent(str, Uri.parse("market://details?id=de.kgs.vertretungsplan")));
            } catch (ActivityNotFoundException e) {
                startActivity(new Intent(str, Uri.parse("https://play.google.com/store/apps/details?id=de.kgs.vertretungsplan")));
            }
            return;
        }
        CoverItemInfo.showDialog(getContext(), (CoverItem) this.values.get(position));
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
            SwipeRefreshLayout swipeRefreshLayout = this.refreshLayout;
            if (topPosition >= 0) {
                z = true;
            }
            swipeRefreshLayout.setEnabled(z);
        }

        @Override
        public void onRefresh() {
            this.broadcaster.send(BroadcastEvent.REQUEST_DATA_RELOAD);
            this.refreshLayout.setRefreshing(false);
        }
    }
}
