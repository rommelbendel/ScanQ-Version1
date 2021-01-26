package com.rommelbendel.scanQ;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

public class Startbildschirm extends AppCompatActivity {
    public final int ladezeit = 1000;
    public static Startbildschirm instance = null;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.startbildschirm);
        TextView text3 = findViewById(R.id.text3);

        Window window = Startbildschirm.this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(ContextCompat.getColor(Startbildschirm.this, R.color.white));

        TinyDB tb = new TinyDB(getApplicationContext());

        if(!Objects.equals(tb.getString("username").trim(), "")) {
            if(tb.getBoolean("hello"))
                text3.setText("Hey " + tb.getString("username") + "!");

            new Handler().postDelayed(() -> {
                Intent myIntent = new Intent(Startbildschirm.this, home.class);
                Startbildschirm.this.startActivity(myIntent);
                finish();
            },ladezeit);
        }else {
            new Handler().postDelayed(() -> {
                Intent myIntent = new Intent(Startbildschirm.this, AppIntro.class);
                Startbildschirm.this.startActivity(myIntent);
                finish();
            },ladezeit);
        }

        //FirebaseMessaging.getInstance().subscribeToTopic();

        /*FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w("TAG", "Fetching FCM registration token failed", task.getException());
                        return;
                    }

                    // Get new FCM registration token
                    String token = task.getResult();
                    tb.putString("t", token);
                });*/
        instance = this;
        saveTessData();
    }

    public String getTessDataDirectory() {
        return Objects.requireNonNull(Startbildschirm.instance.getExternalFilesDir(null)).getAbsolutePath();
    }

    private String getTessPath() {
        return Startbildschirm.instance.getExternalFilesDir(null) + "/tessdata/";
    }

    private void saveTessData() {
        Runnable runnable = () -> {
            AssetManager am = Startbildschirm.instance.getAssets();
            OutputStream outEng = null;
            OutputStream outDeu = null;

            try{
                InputStream inEng = am.open("eng.traineddata");
                InputStream inDeu = am.open("deu.traineddata");
                String tesspath = instance.getTessPath();
                File tessFolder = new File(tesspath);

                if(!tessFolder.exists())
                    tessFolder.mkdirs();

                String tessDataEng = tesspath + "/" + "eng.traineddata";
                String tessDataDeu = tesspath + "/" + "deu.traineddata";
                File tessFileEng = new File(tessDataEng);
                File tessFileDeu = new File(tessDataDeu);


                //Todo spalte if in zwei auf, um nach beiden einzeln zu prüfen
                if(!tessFileEng.exists() && !tessFileDeu.exists()) {
                    outEng = new FileOutputStream(tessDataEng);
                    outDeu = new FileOutputStream(tessDataDeu);
                    byte[] bufferEng = new byte[1024];
                    byte[] bufferDeu = new byte[1024];
                    int readEng = inEng.read(bufferEng);
                    int readDeu = inDeu.read(bufferDeu);

                    while (readEng != -1) {
                        outEng.write(bufferEng, 0, readEng);
                        readEng = inEng.read(bufferEng);
                    }

                    while (readDeu != -1) {
                        outDeu.write(bufferDeu, 0, readDeu);
                        readDeu = inDeu.read(bufferDeu);
                    }
                }
            } catch (Exception e) {
                Log.e("tag", Objects.requireNonNull(e.getMessage()));
            } finally {
                try {
                    if(outEng != null)
                        outEng.close();

                    if(outDeu != null)
                        outDeu.close();
                } catch (Exception ignored) {}
            }
        };
        new Thread(runnable).start();
    }
}
