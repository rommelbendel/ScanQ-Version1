package com.rommelbendel.scanQ;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class home extends AppCompatActivity {

    private FloatingActionButton up, down;
    private Button neu, data, quiz, settings, tutorial;
    private CardView help;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        up = findViewById(R.id.up);
        down = findViewById(R.id.down);
        neu = findViewById(R.id.homeToNew);
        quiz = findViewById(R.id.homeToQuiz);
        settings = findViewById(R.id.homeToEinstellungen);
        tutorial = findViewById(R.id.homeToTutorial);
        data = findViewById(R.id.homeToData);
        help = findViewById(R.id.help);

        down.setOnClickListener(view -> {
            help.setVisibility(View.VISIBLE);
            up.setVisibility(View.VISIBLE);
            neu.setVisibility(View.GONE);
            data.setVisibility(View.GONE);
            down.setVisibility(View.GONE);
        });

        up.setOnClickListener(view -> {
            help.setVisibility(View.GONE);
            up.setVisibility(View.GONE);
            neu.setVisibility(View.VISIBLE);
            data.setVisibility(View.VISIBLE);
            down.setVisibility(View.VISIBLE);
        });

        neu.setOnClickListener(view -> {
            Intent myIntent = new Intent(home.this, Haupt1.class);
            home.this.startActivity(myIntent);
        });

        data.setOnClickListener(view -> {
            Intent myIntent = new Intent(home.this, OnlineSocketReceiver.class);//vokabelset
            myIntent.putExtra("quiz", 4);
            home.this.startActivity(myIntent);
        });

        quiz.setOnClickListener(view -> {
            Intent myIntent = new Intent(home.this, OnlineQuiz.class);//quizmenu
            home.this.startActivity(myIntent);
        });

        settings.setOnClickListener(view -> {
            Intent myIntent = new Intent(home.this, Einstellungen.class);
            home.this.startActivity(myIntent);
        });

        tutorial.setOnClickListener(view -> {
            Intent myIntent = new Intent(home.this, AppIntro.class);
            home.this.startActivity(myIntent);
        });

        /*logOut.setOnClickListener(view -> {
            SharedPreferences mySPR = getSharedPreferences("login", 0);
            SharedPreferences.Editor editor = mySPR.edit();
            editor.putString("myKey1", "false");
            editor.apply();

            Intent myIntent = new Intent(home.this, login.class);
            home.this.startActivity(myIntent);
            finish();
        });
*/

        //Google Account Info
        /*GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            String personName = account.getDisplayName();
            String personGivenName = account.getGivenName();
            String personFamilyName = account.getFamilyName();
            String personEmail = account.getEmail();
            String personId = account.getId();
            Uri personPhoto = account.getPhotoUrl();
        }*/
    }
}
