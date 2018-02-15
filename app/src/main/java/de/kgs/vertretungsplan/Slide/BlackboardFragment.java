package de.kgs.vertretungsplan.Slide;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import de.kgs.vertretungsplan.R;

/**
 * Created by Andreas on 15.02.2018.
 */

public class BlackboardFragment extends Fragment{

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.black_board_layout,container,false);
        return v;
    }

   public void setOnClickListener(View.OnClickListener onClickListener){

   }

}
