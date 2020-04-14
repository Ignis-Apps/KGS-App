package de.kgs.vertretungsplan.ui.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import de.kgs.vertretungsplan.R;
import de.kgs.vertretungsplan.broadcaster.Broadcast;
import de.kgs.vertretungsplan.broadcaster.BroadcastEvent;
import de.kgs.vertretungsplan.firebase.Analytics;
import de.kgs.vertretungsplan.firebase.FirebaseManager;
import de.kgs.vertretungsplan.storage.ApplicationData;
import de.kgs.vertretungsplan.storage.GlobalVariables;
import de.kgs.vertretungsplan.ui.NavigationItem;


public class BlackboardFragment extends Fragment implements OnClickListener {

    private Broadcast broadcast;

    public BlackboardFragment(Broadcast broadcast) {
        this.broadcast = broadcast;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.black_board_layout, container, false);

        view.findViewById(R.id.card_newspaper).setOnClickListener(this);
        view.findViewById(R.id.card_mensa).setOnClickListener(this);
        view.findViewById(R.id.card_contact).setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View view) {

        Context context = getContext();
        if (context == null)
            return;

        switch (view.getId()) {

            case R.id.card_mensa:
                Analytics.getInstance().logContentSelectEvent("mensa", FirebaseManager.ANALYTICS_BLACK_BOARD);
                context.startActivity(new Intent("android.intent.action.VIEW", Uri.parse(GlobalVariables.getInstance().school_mensa_url)));
                break;

            case R.id.card_contact:
                Analytics.getInstance().logContentSelectEvent("contact", FirebaseManager.ANALYTICS_BLACK_BOARD);
                Intent emailIntent = new Intent("android.intent.action.SENDTO", Uri.parse("mailto:ignis.apps@gmail.com"));
                emailIntent.putExtra("android.intent.extra.SUBJECT", "Neuer Eintrag - Schwarzes Brett");
                context.startActivity(Intent.createChooser(emailIntent, "Sende Email mit ..."));
                break;

            case R.id.card_newspaper:
                Analytics.getInstance().logContentSelectEvent("newspaper", FirebaseManager.ANALYTICS_BLACK_BOARD);
                ApplicationData.getInstance().setCurrentNavigationItem(NavigationItem.STUDENT_NEWS_PAPER);
                broadcast.send(BroadcastEvent.CURRENT_MENU_ITEM_CHANGED);
                break;
        }
    }
}
