package com.rommelbendel.scanQ;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.List;

public class QuizSettings extends AppCompatActivity {

    public static final String ID_EXTRA_CATEGORY = "CATEGORY";
    public static final String ID_EXTRA_VOCAB_NUM = "VOCAB_NUM";
    public static final String ID_EXTRA_ONLY_NEW = "ONLY_NEW";
    public static final String ID_EXTRA_FROM_TO = "FROM_TO";

    public static final int DE_TO_ENG = 1;
    public static final int ENG_TO_DE = 2;

    private Spinner categorySpinner;
    private RadioGroup queryModeSelection;
    private CheckBox onlyNewVocabs;

    private boolean quizModeSelected = false;
    private boolean categorySelected = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_settings);

        KategorienViewModel kategorienViewModel = new ViewModelProvider(this).get(KategorienViewModel.class);

        Spinner quizTypeSpinner = findViewById(R.id.quizModeSelection);
        categorySpinner = findViewById(R.id.categorySelection);
        TextView vocabNumText = findViewById(R.id.vocabNum);
        SeekBar vocabAmountSeekBar = findViewById(R.id.amountOfVocabs);
        //queryModeSelection = findViewById(R.id.queryMode);
        ImageButton startQuizButton = findViewById(R.id.startQuizButton);
        onlyNewVocabs = findViewById(R.id.onlyNewVocabs);

        ArrayAdapter<CharSequence> quizTypeAdapter = ArrayAdapter.createFromResource(this,
                R.array.QuizModeDropDown_Array, android.R.layout.simple_spinner_item);
        quizTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        quizTypeSpinner.setAdapter(quizTypeAdapter);

        LiveData<List<Kategorie>> alleKategorien = kategorienViewModel.getAlleKategorien();
        alleKategorien.observe(this, categories -> {
            if (categories != null) {
                if (!categories.isEmpty()) {
                    List<String> categoryNames = new ArrayList<>();
                    for (Kategorie category: categories) {
                        categoryNames.add(category.getName());
                    }
                    ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                            QuizSettings.this,
                            android.R.layout.simple_spinner_item, categoryNames);
                    categoryAdapter.setDropDownViewResource(
                            android.R.layout.simple_spinner_dropdown_item);
                    categorySpinner.setAdapter(categoryAdapter);
                } else {
                    AlertDialog.Builder warning = new AlertDialog.Builder(QuizSettings.this);
                    warning.setTitle("Keine Vokabeln vorhanden");
                    warning.setMessage("Möchtest du Vokabeln hinzufügen?");
                    warning.setPositiveButton("einscannen", (dialog, which) -> {
                        dialog.dismiss();
                        Intent scannIntent = new Intent(QuizSettings.this,
                                Haupt.class);
                        QuizSettings.this.startActivity(scannIntent);
                    });
                    warning.setNegativeButton("zurück", (dialog, which) -> {
                        dialog.dismiss();
                        Intent backIntent = new Intent(QuizSettings.this, home.class);
                        QuizSettings.this.startActivity(backIntent);
                    });
                    warning.show();
                }
            }
        });

        final TextView vocabNumTxt = vocabNumText;
        vocabAmountSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                vocabNumTxt.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                seekBar.setBackgroundColor(Color.parseColor("#096b6b"));
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                seekBar.setBackgroundColor(Color.parseColor("#ffffff"));
            }
        });
        vocabAmountSeekBar.setProgress(15);

        startQuizButton.setOnClickListener(v -> {
            final String category = categorySpinner.getSelectedItem().toString();
            final String quizType = quizTypeSpinner.getSelectedItem().toString();
            final int vocabNum = vocabAmountSeekBar.getProgress();
            final boolean onlyUntrained = onlyNewVocabs.isChecked();

            Intent startQuizIntent;

            switch (quizType) {
                case "Eingabe DE":
                    startQuizIntent = new Intent(QuizSettings.this, QuizEingabe.class);
                    startQuizIntent.putExtra(ID_EXTRA_FROM_TO, ENG_TO_DE);
                    break;
                case "Eingabe ENG":
                    startQuizIntent = new Intent(QuizSettings.this, QuizEingabe.class);
                    startQuizIntent.putExtra(ID_EXTRA_FROM_TO, DE_TO_ENG);
                    break;
                case "Multiple-Choice DE":
                    startQuizIntent = new Intent(QuizSettings.this, QuizMultipleChoice.class);
                    startQuizIntent.putExtra(ID_EXTRA_FROM_TO, ENG_TO_DE);
                    break;
                case "Multiple-Choice ENG":
                    startQuizIntent = new Intent(QuizSettings.this, QuizMultipleChoice.class);
                    startQuizIntent.putExtra(ID_EXTRA_FROM_TO, DE_TO_ENG);
                    break;
                case "Spracheingabe":
                    startQuizIntent = new Intent(QuizSettings.this, quiz_voice.class);
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + quizType);
            }

            startQuizIntent.putExtra(ID_EXTRA_CATEGORY, category);
            startQuizIntent.putExtra(ID_EXTRA_VOCAB_NUM, vocabNum);
            startQuizIntent.putExtra(ID_EXTRA_ONLY_NEW, onlyUntrained);

            QuizSettings.this.startActivity(startQuizIntent);
        });

    }
}
