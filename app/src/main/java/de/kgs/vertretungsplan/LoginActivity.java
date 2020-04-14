package de.kgs.vertretungsplan;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import de.kgs.vertretungsplan.loader.CoverPlanLoader;
import de.kgs.vertretungsplan.loader.CoverPlanLoaderCallback;
import de.kgs.vertretungsplan.loader.LoaderResponseCode;
import de.kgs.vertretungsplan.storage.Credentials;
import de.kgs.vertretungsplan.storage.GlobalVariables;


public class LoginActivity extends AppCompatActivity implements CoverPlanLoaderCallback {
    public static final int SUCCESS_RC = 111;
    private static final String TAG = "LoginActivity";
    private Credentials credentials = Credentials.getInstance();
    private Button loginButton;
    private EditText usernameText;
    private EditText passwordText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginButton = findViewById(R.id.btn_login);
        usernameText = findViewById(R.id.input_username);
        passwordText = findViewById(R.id.input_password);

        loginButton.setOnClickListener(v -> login());
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

        credentials.setUsername(usernameText.getText().toString().trim());
        credentials.setPassword(passwordText.getText().toString().trim());

        CoverPlanLoader loader = new CoverPlanLoader(this, this, true);
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
    public void loaderFinishedWithResponseCode(LoaderResponseCode ResponseCode) {
        loginButton.setEnabled(true);
        if (ResponseCode == LoaderResponseCode.LATEST_DATA_SET) {

            credentials.setUsername(passwordText.getText().toString().trim());
            credentials.setPassword(usernameText.getText().toString().trim());
            credentials.saveCredentials(this);

            GlobalVariables.getInstance().responseCode = ResponseCode;
            setResult(SUCCESS_RC);
            finish();
        } else if (ResponseCode == LoaderResponseCode.NO_INTERNET_DATA_SET_EXISTS || ResponseCode == LoaderResponseCode.NO_INTERNET_NO_DATA_SET) {
            Toast.makeText(getBaseContext(), "Keine Internetverbindung!", Toast.LENGTH_LONG).show();
        } else if (ResponseCode == LoaderResponseCode.LOGIN_REQUIRED) {
            Toast.makeText(getBaseContext(), "Falscher Nutzername oder Passwort!", Toast.LENGTH_LONG).show();
        } else if (ResponseCode == LoaderResponseCode.ERROR) {
            Toast.makeText(getBaseContext(), "Ein Fehler ist aufgetreten! Versuche es später erneut.", Toast.LENGTH_LONG).show();
        } else if (ResponseCode == LoaderResponseCode.COVER_PLAN_NOT_PROVIDED) {

            Toast.makeText(getBaseContext(), "Der Vertretungsplan ist im Moment nicht verfügbar!", Toast.LENGTH_LONG).show();
            credentials.setUsername(passwordText.getText().toString().trim());
            credentials.setPassword(usernameText.getText().toString().trim());
            credentials.saveCredentials(this);
            setResult(SUCCESS_RC);
            finish();
        }
    }
}