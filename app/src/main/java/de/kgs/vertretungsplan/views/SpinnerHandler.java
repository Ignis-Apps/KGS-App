package de.kgs.vertretungsplan.views;

import android.app.Activity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import de.kgs.vertretungsplan.R;
import de.kgs.vertretungsplan.broadcaster.Broadcast;
import de.kgs.vertretungsplan.broadcaster.BroadcastEvent;
import de.kgs.vertretungsplan.singetones.DataStorage;

public class SpinnerHandler {

    private Broadcast broadcast;
    private DataStorage dataStorage = DataStorage.getInstance();
    private Spinner spinnerClass;

    public SpinnerHandler(Activity activity, Broadcast broadcast) {
        this.broadcast = broadcast;
        setupClassSpinner(activity);
        setupGradeSpinner(activity);
    }

    private void setupGradeSpinner(Activity activity) {
        Spinner spinnerGradeLevel = activity.findViewById(R.id.spinnerGrade);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(activity, R.array.spinner_array, R.layout.spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGradeLevel.setAdapter(adapter);
        spinnerGradeLevel.setSelection(dataStorage.currentGradeLevel);
        spinnerGradeLevel.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
                Grade grade = Grade.getGradeByName(adapterView.getItemAtPosition(pos).toString());
                if (grade != null) {
                    spinnerClass.setVisibility(grade.hasSubClasses() ? View.VISIBLE : View.GONE);
                    dataStorage.currentGrade = grade;
                    broadcast.send(BroadcastEvent.CURRENT_GRADE_CHANGED);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    private void setupClassSpinner(Activity activity) {
        this.spinnerClass = activity.findViewById(R.id.spinnerClass);
        ArrayAdapter<CharSequence> adapterClass = ArrayAdapter.createFromResource(activity, R.array.spinner_array_class, R.layout.spinner_item);
        adapterClass.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerClass.setAdapter(adapterClass);
        spinnerClass.setSelection(dataStorage.currentClass);
        spinnerClass.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
                GradeSubClass subClass = GradeSubClass.getByClassName(adapterView.getItemAtPosition(pos).toString());
                if (subClass != null) {
                    dataStorage.currentGradeSubClass = subClass;
                    broadcast.send(BroadcastEvent.CURRENT_CLASS_CHANGED);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }
}
