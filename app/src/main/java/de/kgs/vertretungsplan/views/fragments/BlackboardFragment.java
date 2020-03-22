package de.kgs.vertretungsplan.views.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import de.kgs.vertretungsplan.MainActivity;
import de.kgs.vertretungsplan.R;
import de.kgs.vertretungsplan.singetones.GlobalVariables;


public class BlackboardFragment extends Fragment implements OnClickListener {

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
                context.startActivity(new Intent("android.intent.action.VIEW", Uri.parse(GlobalVariables.getInstance().school_mensa_url)));
                break;

            case R.id.card_contact:
                Intent emailIntent = new Intent("android.intent.action.SENDTO", Uri.parse("mailto:ignis.apps@gmail.com"));
                emailIntent.putExtra("android.intent.extra.SUBJECT", "Neuer Eintrag - Schwarzes Brett");
                context.startActivity(Intent.createChooser(emailIntent, "Sende Email mit ..."));
                break;

            case R.id.card_newspaper:
                MainActivity m = (MainActivity) getContext();
                if (m != null) {
                    m.webViewHandler.loadWebPage(GlobalVariables.getInstance().student_newspaper, true);
                    // TODO
                    // m.toolBar.setTitle("freistunde.blog");
                }
                break;
        }
    }
}
