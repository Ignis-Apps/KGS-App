package de.kgs.vertretungsplan.Slide;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import de.kgs.vertretungsplan.R;
import de.kgs.vertretungsplan.CoverPlan.CoverItem;

/**
 * Created by Andreas on 17.01.2018.
 */

public class ListViewFragment extends Fragment {

    public ListView listView;
    public StableArrayAdapter arrayAdapter;
    public List<CoverItem> values;
    public boolean hasInterface;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        values = new ArrayList<>();
        arrayAdapter = new StableArrayAdapter(this.getContext(),values);
        View f = inflater.inflate(R.layout.listview_fragment,container,false);
        listView = f.findViewById(R.id.fragment_listview);
        listView.setAdapter(arrayAdapter);

        return f;
    }


    public void setDataset(List<CoverItem> values){
        arrayAdapter.setDataSet(values);
    }

    public void setDailyMessage(String title,String msg){
        arrayAdapter.setDailyMessage(title, msg);
    }

    public void setItemClickListene(AdapterView.OnItemClickListener i){
        if(listView!=null){
            listView.setOnItemClickListener(i);
            hasInterface=true;
        }

    }

    public List<CoverItem> getDataset(){
        return values;
    }


}
