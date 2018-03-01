package de.kgs.vertretungsplan.slide;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import de.kgs.vertretungsplan.DataStorage;
import de.kgs.vertretungsplan.manager.FirebaseManager;
import de.kgs.vertretungsplan.R;

public class BlackboardFragment extends Fragment{

    private CardView newspaper;
    private CardView mensa;
    private CardView powerCreative;
    private CardView contact;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.black_board_layout,container,false);

        newspaper = v.findViewById(R.id.card_newspaper);
        mensa = v.findViewById(R.id.card_mensa);
        powerCreative = v.findViewById(R.id.card_power_creative);
        contact = v.findViewById(R.id.card_contact);

        return v;
    }

   public void setOnClickListener(View.OnClickListener onClickListener){
        newspaper.setOnClickListener(onClickListener);
        mensa.setOnClickListener(onClickListener);
        powerCreative.setOnClickListener(onClickListener);
        contact.setOnClickListener(onClickListener);
   }

   public void onClick(View view, Context context, final FirebaseManager firebaseManager, DataStorage ds){
       if(view == mensa){
           firebaseManager.logEventSelectContent("mensa", FirebaseManager.ANALYTICS_BLACK_BOARD);

           Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(ds.school_mensa_url));
           startActivity(browserIntent);
       }else if(view == powerCreative){
           firebaseManager.logEventSelectContent("power.creative", FirebaseManager.ANALYTICS_BLACK_BOARD);

           LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

           AlertDialog.Builder powerCreativeInfoDialogBuilder = new AlertDialog.Builder(context);
           powerCreativeInfoDialogBuilder.setView(inflater.inflate(R.layout.alertdialog_power_creative, null));
           powerCreativeInfoDialogBuilder.setPositiveButton("Anschreiben", new DialogInterface.OnClickListener() {
               @Override
               public void onClick(DialogInterface dialog, int which) {
                   firebaseManager.logEventSelectContent("power.creative_email", FirebaseManager.ANALYTICS_BLACK_BOARD);

                   Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:power.creative@outlook.de"));
                   startActivity(Intent.createChooser(emailIntent, "Sende Nachricht mit ..."));
               }
           });
           powerCreativeInfoDialogBuilder.setNegativeButton("Instagram", new DialogInterface.OnClickListener() {
               @Override
               public void onClick(DialogInterface dialog, int which) {
                   firebaseManager.logEventSelectContent("power.creative_instagram", FirebaseManager.ANALYTICS_BLACK_BOARD);

                   Uri uri = Uri.parse("http://instagram.com/_u/power.creative");
                   Intent likeIng = new Intent(Intent.ACTION_VIEW, uri);
                   likeIng.setPackage("com.instagram.android");

                   try {
                       startActivity(likeIng);
                   } catch (ActivityNotFoundException e) {
                       startActivity(new Intent(Intent.ACTION_VIEW,
                               Uri.parse("http://instagram.com/power.creative")));
                   }
               }
           });

           powerCreativeInfoDialogBuilder.create().show();

       }else if(view == contact){
           firebaseManager.logEventSelectContent("contact", FirebaseManager.ANALYTICS_BLACK_BOARD);

           Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:ignis.apps@gmail.com"));
           emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Neuer Eintrag - Schwarzes Brett");
           startActivity(Intent.createChooser(emailIntent, "Sende Email mit ..."));
       }else if(view == newspaper){
           firebaseManager.logEventSelectContent("newspaper", FirebaseManager.ANALYTICS_BLACK_BOARD);
       }
   }

}
