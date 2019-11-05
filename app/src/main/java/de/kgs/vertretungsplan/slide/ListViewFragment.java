package de.kgs.vertretungsplan.slide;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import de.kgs.vertretungsplan.MainActivityInterface;
import de.kgs.vertretungsplan.R;
import de.kgs.vertretungsplan.coverPlan.CoverItem;

public class ListViewFragment extends Fragment {

    public ListView listView;
    public SwipeRefreshLayout refreshLayout;
    public StableArrayAdapter arrayAdapter;
    public List<CoverItem> values;
    private MainActivityInterface mainActivityInterface;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        values = new ArrayList<>();
        arrayAdapter = new StableArrayAdapter(this.getContext(),values);
        View f = inflater.inflate(R.layout.listview_fragment,container,false);
        listView = f.findViewById(R.id.fragment_listview);
        listView.setAdapter(arrayAdapter);



        refreshLayout = f.findViewById(R.id.fragment_listview_refresh);
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {

            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem, int visiblItemCount, int totalItemCount) {
                int topRowVerticalPosition = (listView == null || listView.getChildCount() == 0) ? 0 : listView.getChildAt(0).getTop();
                boolean enable = (firstVisibleItem == 0 && topRowVerticalPosition >= 0);
                refreshLayout.setEnabled(enable);
                //System.out.println("SWIPE LAYOUT ENABLED : " + enable);
            }
        });

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mainActivityInterface.onReloadRequested();
                refreshLayout.setRefreshing(false);
            }
        });


        //System.out.println("NEW FRAGMENT CREATED");

        return f;
    }


    public void setDataset(List<CoverItem> values){
        arrayAdapter.setDataSet(values);
    }

    public void setDailyMessage(String title,String msg){
        arrayAdapter.setDailyMessage(title, msg);
    }

    public void setItemClickListener(AdapterView.OnItemClickListener i){
        if(listView.getOnItemClickListener()==null){
            listView.setOnItemClickListener(i);
        }
    }

    public void setMainActivityInterface(MainActivityInterface i){
        this.mainActivityInterface = i;
    }

    public List<CoverItem> getDataset(){
        return values;
    }

    public boolean isCreated(){
        return arrayAdapter != null;
    }


}
