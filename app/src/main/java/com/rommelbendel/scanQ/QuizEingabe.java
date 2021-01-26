package com.rommelbendel.scanQ;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Collections;
import java.util.List;

public class QuizEingabe extends AppCompatActivity {

    private String categoryName;
    private int vocabNum;
    private boolean onlyUntrained;
    private int language_from_to;

    private VokabelViewModel vokabelViewModel;
    private LiveData<List<Vokabel>> quizVocabsLiveData;
    private List<Vokabel> quizVocabs;

    private TextView questionView;
    private TextInputEditText solutionView;
    private MaterialButton buttonCheck;
    private CardView dialogRight;
    private CardView dialogWrong;
    private TextView solutionRight;
    private TextView solutionWrong;
    private ImageButton buttonPreviousQuestion;
    private ImageButton buttonNextQuestion;
    private TextView pointsView;

    private int questionPointer = 0;
    private int pointsCounter = 0;
    private Bundle answers = new Bundle();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            vocabNum = extras.getInt(QuizSettings.ID_EXTRA_VOCAB_NUM);
            categoryName = extras.getString(QuizSettings.ID_EXTRA_CATEGORY);
            onlyUntrained = extras.getBoolean(QuizSettings.ID_EXTRA_ONLY_NEW);
            language_from_to = extras.getInt(QuizSettings.ID_EXTRA_FROM_TO);
        } else {
            finish();
        }

        if (language_from_to == QuizSettings.DE_TO_ENG) {
            setContentView(R.layout.quiz3);
        } else {
            setContentView(R.layout.quiz2);
        }

        questionView = findViewById(R.id.voc);
        solutionView = findViewById(R.id.loesung);
        buttonCheck = findViewById(R.id.check);
        dialogRight = findViewById(R.id.richtig);
        dialogWrong = findViewById(R.id.falsch);
        solutionRight = findViewById(R.id.solution_text_right);
        solutionWrong = findViewById(R.id.solution_text_wrong);
        buttonPreviousQuestion = findViewById(R.id.quizleft);
        buttonNextQuestion = findViewById(R.id.quizright);
        pointsView = findViewById(R.id.points);

        dialogRight.setVisibility(View.GONE);
        dialogWrong.setVisibility(View.GONE);


        vokabelViewModel = new ViewModelProvider(this).get(VokabelViewModel.class);
        if (onlyUntrained) {
            quizVocabsLiveData = vokabelViewModel.getUntrainedCategoryVocabs(categoryName);
        } else {
            quizVocabsLiveData = vokabelViewModel.getCategoryVocabs(categoryName);
        }

        quizVocabsLiveData.observe(this, vokabeln -> {
            if (vokabeln != null) {
                if (vokabeln.size() != 0) {
                    quizVocabs = vokabeln;
                    Collections.shuffle(quizVocabs);
                    if (quizVocabs.size() > vocabNum) {
                        QuizEingabe.this.quizVocabs = quizVocabs.subList(0, vocabNum);
                    }
                    loadQuestion();
                } else {
                    AlertDialog.Builder warnung = new AlertDialog.Builder(
                            QuizEingabe.this);
                    warnung.setTitle("Quiz kann nicht gestartet werden");
                    warnung.setMessage("Es sind keine Vokabeln vorhanden.");
                    warnung.setPositiveButton("OK", (dialog, which) -> {
                        dialog.cancel();
                        finish();
                    });
                    warnung.show();
                }
                quizVocabsLiveData.removeObservers(QuizEingabe.this);
            }
        });

        buttonPreviousQuestion.setOnClickListener(v -> previousQuestion());
        buttonNextQuestion.setOnClickListener(v -> nextQuestion());
    }

    private void previousQuestion() {
        if (questionPointer > 0) {
            questionPointer --;
        } else {
            questionPointer = quizVocabs.size() - 1;
        }
        loadQuestion();
    }

    private void nextQuestion() {
        if (questionPointer == quizVocabs.size() - 1) {
            questionPointer = 0;
        } else {
            questionPointer ++;
        }
        loadQuestion();
    }

    private void loadQuestion() {
        dialogRight.setVisibility(View.INVISIBLE);
        dialogWrong.setVisibility(View.INVISIBLE);

        final Vokabel questionVocab = quizVocabs.get(questionPointer);

        if (language_from_to == QuizSettings.DE_TO_ENG) {
            questionView.setText(questionVocab.getVokabelDE());
        } else {
            questionView.setText(questionVocab.getVokabelENG());
        }

        if (answers.containsKey(String.valueOf(questionPointer))) {
            String givenAnswer = answers.getString(String.valueOf(questionPointer));
            solutionView.setText(givenAnswer);
            solutionView.setEnabled(false);
            buttonCheck.setEnabled(false);
            buttonCheck.setOnClickListener(null);

            if (language_from_to == QuizSettings.DE_TO_ENG) {
                if (givenAnswer.equals(questionVocab.getVokabelENG())) {
                    dialogRight.setVisibility(View.VISIBLE);
                    solutionRight.setText(String.format("%s = %s", questionVocab.getVokabelDE(),
                            questionVocab.getVokabelENG()));
                } else {
                    dialogWrong.setVisibility(View.VISIBLE);
                    solutionWrong.setText(String.format("%s = %s", questionVocab.getVokabelDE(),
                            questionVocab.getVokabelENG()));
                }
            } else {
                if (givenAnswer.equals(questionVocab.getVokabelDE())) {
                    dialogRight.setVisibility(View.VISIBLE);
                    solutionRight.setText(String.format("%s = %s", questionVocab.getVokabelENG(),
                            questionVocab.getVokabelDE()));
                } else {
                    dialogWrong.setVisibility(View.VISIBLE);
                    solutionWrong.setText(String.format("%s = %s", questionVocab.getVokabelENG(),
                            questionVocab.getVokabelDE()));
                }
            }

        } else {
            solutionView.setText("");
            solutionView.setEnabled(true);
            buttonCheck.setEnabled(true);

            buttonCheck.setOnClickListener(v -> checkAnswer());
        }
    }

    public void checkAnswer() {
        final Vokabel questionVocab = quizVocabs.get(questionPointer);
        final String givenAnswer = solutionView.getText().toString().trim();

        vokabelViewModel.updateAnswered(questionVocab.getId(),
                questionVocab.getAnswered() + 1);

        answers.putString(String.valueOf(questionPointer), givenAnswer);
        buttonCheck.setEnabled(false);
        buttonCheck.setOnClickListener(null);
        solutionView.setEnabled(false);

        if (language_from_to == QuizSettings.DE_TO_ENG) {
            if (givenAnswer.equals(questionVocab.getVokabelENG())) {
                givePoint();
                dialogRight.setVisibility(View.VISIBLE);
                solutionRight.setText(String.format("%s = %s", questionVocab.getVokabelDE(),
                        questionVocab.getVokabelENG()));
                vokabelViewModel.updateCountRightAnswers(questionVocab.getId(),
                        questionVocab.getRichtig() + 1);
            } else {
                dialogWrong.setVisibility(View.VISIBLE);
                solutionWrong.setText(String.format("%s = %s", questionVocab.getVokabelDE(),
                        questionVocab.getVokabelENG()));
                vokabelViewModel.updateCountWrongAnswers(questionVocab.getId(),
                        questionVocab.getFalsch() + 1);
            }
        } else {
            if (givenAnswer.equals(questionVocab.getVokabelDE())) {
                givePoint();
                dialogRight.setVisibility(View.VISIBLE);
                solutionRight.setText(String.format("%s = %s", questionVocab.getVokabelENG(),
                        questionVocab.getVokabelDE()));
                vokabelViewModel.updateCountRightAnswers(questionVocab.getId(),
                        questionVocab.getRichtig() + 1);
            } else {
                dialogWrong.setVisibility(View.VISIBLE);
                solutionWrong.setText(String.format("%s = %s", questionVocab.getVokabelENG(),
                        questionVocab.getVokabelDE()));
                vokabelViewModel.updateCountWrongAnswers(questionVocab.getId(),
                        questionVocab.getFalsch() + 1);
            }
        }
        checkIfSolved();
    }

    private void checkIfSolved() {
        if (answers.size() == quizVocabs.size()) {
            buttonPreviousQuestion.setOnClickListener(null);
            buttonNextQuestion.setOnClickListener(null);
        }
    }

    public void givePoint() {
        pointsCounter ++;
        pointsView.setText(String.valueOf(pointsCounter));
    }
}
