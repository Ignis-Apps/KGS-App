package de.kgs.vertretungsplan;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import de.kgs.vertretungsplan.coverPlan.CoverPlanLoader;
import de.kgs.vertretungsplan.coverPlan.CoverPlanLoaderCallback;
import de.kgs.vertretungsplan.singetones.DataStorage;

import static de.kgs.vertretungsplan.storage.StorageKeys.PASSWORD;
import static de.kgs.vertretungsplan.storage.StorageKeys.SHARED_PREF;
import static de.kgs.vertretungsplan.storage.StorageKeys.USERNAME;

public class LoginActivity extends AppCompatActivity implements CoverPlanLoaderCallback {
    public static final int SUCCESS_RC = 111;
    private static final String TAG = "LoginActivity";

    private DataStorage ds = DataStorage.getInstance();
    public SharedPreferences sharedPreferences;
    public SharedPreferences.Editor sharedEditor;

    private Button loginButton;
    private EditText usernameText;
    private EditText passwordText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginButton =  findViewById(R.id.btn_login);
        usernameText = findViewById(R.id.input_username);
        passwordText = findViewById(R.id.input_password);

        loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                login();
            }
        });
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    public void login() {
        Log.d(TAG, "Login");

        if (!validate()) {
            return;
        }

        loginButton.setEnabled(false);

        ds.username = usernameText.getText().toString().trim();
        ds.password = passwordText.getText().toString().trim();

        CoverPlanLoader loader = new CoverPlanLoader(this,this, true);
        loader.execute();
    }

    public boolean validate() {
        boolean valid = true;

        String email = usernameText.getText().toString();
        String password = passwordText.getText().toString();

        if (email.trim().isEmpty()) {
            usernameText.setError("Gib einen Benutzernamen ein!");
            valid = false;
        } else {
            usernameText.setError(null);
        }

        if (password.trim().isEmpty()) {
            passwordText.setError("Gib ein Passwort ein!");
            valid = false;
        } else {
            passwordText.setError(null);
        }

        return valid;
    }

    @Override
    public void loaderFinishedWithResponseCode(int ResponseCode) {
        loginButton.setEnabled(true);
        if(ResponseCode == CoverPlanLoader.RC_LATEST_DATASET){
            sharedPreferences = this.getSharedPreferences(SHARED_PREF, 0);
            sharedEditor = sharedPreferences.edit();
            sharedEditor.commit();
            sharedEditor.putString(PASSWORD, passwordText.getText().toString().trim());
            sharedEditor.putString(USERNAME, usernameText.getText().toString().trim());
            sharedEditor.commit();
            ds.responseCode = ResponseCode;
            setResult(SUCCESS_RC);
            finish();
        } else if (ResponseCode == CoverPlanLoader.RC_NO_INTERNET_DATASET_EXIST || ResponseCode == CoverPlanLoader.RC_NO_INTERNET_NO_DATASET){
            Toast.makeText(getBaseContext(), "Keine Internetverbindung!", Toast.LENGTH_LONG).show();
        } else if(ResponseCode == CoverPlanLoader.RC_LOGIN_REQUIRED) {
            Toast.makeText(getBaseContext(), "Falscher Nutzername oder Passwort!", Toast.LENGTH_LONG).show();
        } else if(ResponseCode == CoverPlanLoader.RC_ERROR) {
            Toast.makeText(getBaseContext(), "Ein Fehler ist aufgetreten! Versuche es später erneut.", Toast.LENGTH_LONG).show();
        }
    }
}