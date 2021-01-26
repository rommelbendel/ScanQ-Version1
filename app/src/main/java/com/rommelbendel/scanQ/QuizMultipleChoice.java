package com.rommelbendel.scanQ;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QuizMultipleChoice extends AppCompatActivity {

    private TextView questionField;
    private MaterialButton buttonAnswer1;
    private MaterialButton buttonAnswer2;
    private MaterialButton buttonAnswer3;
    private MaterialButton buttonAnswer4;
    private ImageButton buttonPreviousQuestion;
    private ImageButton buttonNextQuestion;
    private TextView pointsCounterView;
    private CardView resultCard;
    private TextView resultCountRight;
    private TextView resultCountWrong;
    private MaterialButton restartButton;

    private VokabelViewModel vokabelViewModel;
    private LiveData<List<Vokabel>> quizVocabsLiveData;
    private List<Vokabel> quizVocabs;
    private LiveData<List<Vokabel>> allVocabsLiveData;

    private String categoryName;
    private int vocabNum;
    private boolean onlyUntrained;
    private int language_from_to;

    private int pointsCounter = 0;
    private int questionPointer = 0;
    private List<List<String>> questions;
    private Bundle answers = new Bundle();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.quiz);

        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            vocabNum = extras.getInt(QuizSettings.ID_EXTRA_VOCAB_NUM);
            categoryName = extras.getString(QuizSettings.ID_EXTRA_CATEGORY);
            onlyUntrained = extras.getBoolean(QuizSettings.ID_EXTRA_ONLY_NEW);
            language_from_to = extras.getInt(QuizSettings.ID_EXTRA_FROM_TO);
        } else {
            finish();
        }

        if (vocabNum < 4) {
            resultCard = findViewById(R.id.resultCard1);
            resultCard.setVisibility(View.GONE);

            AlertDialog.Builder warnung = new AlertDialog.Builder(
                    QuizMultipleChoice.this);
            warnung.setTitle("Quiz kann nicht gestartet werden");
            warnung.setMessage("Bitte wähle mindestens 4 Vokabeln aus.");
            warnung.setPositiveButton("OK", (dialog, which) -> {
                dialog.cancel();
                finish();
            });
            warnung.show();
        } else {
            questionField = findViewById(R.id.voc);
            buttonAnswer1 = findViewById(R.id.AnswerButton_1);
            buttonAnswer2 = findViewById(R.id.AnswerButton_2);
            buttonAnswer3 = findViewById(R.id.AnswerButton_3);
            buttonAnswer4 = findViewById(R.id.AnswerButton_4);
            buttonPreviousQuestion = findViewById(R.id.quizleft);
            buttonNextQuestion = findViewById(R.id.quizright);
            pointsCounterView = findViewById(R.id.points);
            resultCard = findViewById(R.id.resultCard1);
            resultCountRight = findViewById(R.id.countRight);
            resultCountWrong = findViewById(R.id.countWrong);
            restartButton = findViewById(R.id.restart1);

            resultCard.setVisibility(View.GONE);

            vokabelViewModel = new ViewModelProvider(this).get(VokabelViewModel.class);
            if (onlyUntrained) {
                quizVocabsLiveData = vokabelViewModel.getUntrainedCategoryVocabs(categoryName);
            } else {
                quizVocabsLiveData = vokabelViewModel.getCategoryVocabs(categoryName);
            }

            quizVocabsLiveData.observe(this, vokabeln -> {
                if (vokabeln != null) {
                    if (vokabeln.size() != 0) {
                        quizVocabs = quizVocabsLiveData.getValue();
                        assert quizVocabs != null;
                        Collections.shuffle(quizVocabs);
                        if (quizVocabs.size() > vocabNum) {
                            QuizMultipleChoice.this.quizVocabs = quizVocabs.subList(0, vocabNum);
                        }
                        generateQuestions();
                    } else {
                        AlertDialog.Builder warnung = new AlertDialog.Builder(
                                QuizMultipleChoice.this);
                        warnung.setTitle("Quiz kann nicht gestartet werden");
                        warnung.setMessage("Es sind keine Vokabeln vorhanden.");
                        warnung.setPositiveButton("OK", (dialog, which) -> {
                            dialog.cancel();
                            finish();
                        });
                        warnung.show();
                    }
                    quizVocabsLiveData.removeObservers(QuizMultipleChoice.this);
                }
            });

            buttonPreviousQuestion.setOnClickListener(v -> previousQuestion());
            buttonNextQuestion.setOnClickListener(v -> nextQuestion());
        }

    }

    public void generateQuestions() {
        questions = new ArrayList<>();

        allVocabsLiveData = vokabelViewModel.getAlleVokabeln();
        allVocabsLiveData.observe(this, allVocabs -> {
            if (allVocabs != null) {
                if (allVocabs.size() > 4) {
                    for (Vokabel quizVocab: quizVocabs) {
                        List<Vokabel> vocabs = new ArrayList<>(quizVocabs); //essential not redundant!!!
                        vocabs.remove(quizVocab);
                        Collections.shuffle(vocabs);
                        List<String> question = new ArrayList<>();
                        List<String> answers = new ArrayList<>();
                        if (language_from_to == QuizSettings.DE_TO_ENG) {
                            question.add(quizVocab.getVokabelDE());
                            answers.add(quizVocab.getVokabelENG());
                            for (int i = 0; i < 3; i++) {
                                answers.add(vocabs.get(0).getVokabelENG());
                                Log.d("answers Englisch add", vocabs.get(0).getVokabelENG());
                                vocabs.remove(0);
                            }
                        } else {
                            question.add(quizVocab.getVokabelENG());
                            answers.add(quizVocab.getVokabelDE());
                            for (int i = 0; i < 3; i++) {
                                answers.add(vocabs.get(0).getVokabelDE());
                                Log.d("answers german add", vocabs.get(0).getVokabelDE());
                                vocabs.remove(0);
                            }
                        }
                        Collections.shuffle(answers);
                        question.addAll(answers);

                        questions.add(question);
                        loadQuestion();
                        allVocabsLiveData.removeObservers(QuizMultipleChoice.this);
                    }
                } else {
                    AlertDialog.Builder warnung = new AlertDialog.Builder(
                            QuizMultipleChoice.this);
                    warnung.setTitle("Quiz kann nicht gestartet werden");
                    warnung.setMessage("Es sind nicht genug Vokabeln vorhanden.");
                    warnung.setPositiveButton("OK", (dialog, which) -> {
                        dialog.cancel();
                        Intent backIntent = new Intent(QuizMultipleChoice.this,
                                QuizSettings.class);
                        QuizMultipleChoice.this.startActivity(backIntent);
                        finish();
                    });
                    warnung.show();
                    allVocabsLiveData.removeObservers(QuizMultipleChoice.this);
                }
            }
        });
    }

    public void loadQuestion() {
        List<String> question = questions.get(questionPointer);

        questionField.setText(question.get(0));

        buttonAnswer1.setText(question.get(1));
        buttonAnswer2.setText(question.get(2));
        buttonAnswer3.setText(question.get(3));
        buttonAnswer4.setText(question.get(4));

        buttonAnswer1.setBackgroundColor(Color.parseColor("#219a95"));
        buttonAnswer2.setBackgroundColor(Color.parseColor("#219a95"));
        buttonAnswer3.setBackgroundColor(Color.parseColor("#219a95"));
        buttonAnswer4.setBackgroundColor(Color.parseColor("#219a95"));

        if (answers.containsKey(String.valueOf(questionPointer))) {
            buttonAnswer1.setOnClickListener(null);
            buttonAnswer2.setOnClickListener(null);
            buttonAnswer3.setOnClickListener(null);
            buttonAnswer4.setOnClickListener(null);

            int givenAnswer = answers.getInt(String.valueOf(questionPointer));

            MaterialButton answerButton;
            switch (givenAnswer) {
                case 1:
                    answerButton = buttonAnswer1;
                    break;
                case 2:
                    answerButton = buttonAnswer2;
                    break;
                case 3:
                    answerButton = buttonAnswer3;
                    break;
                case 4:
                    answerButton = buttonAnswer4;
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + givenAnswer);
            }

            String answerText = question.get(givenAnswer);
            String correctAnswer;
            if (language_from_to == QuizSettings.DE_TO_ENG) {
                correctAnswer = quizVocabs.get(questionPointer).getVokabelENG();
            } else {
                correctAnswer = quizVocabs.get(questionPointer).getVokabelDE();
            }

            if (answerText.equals(correctAnswer)) {
                answerButton.setBackgroundColor(Color.rgb(88, 214, 141));
            } else {
                answerButton.setBackgroundColor(Color.rgb(236, 112, 99));
            }

        } else {
            buttonAnswer1.setOnClickListener(v -> checkAnswer((MaterialButton) v));
            buttonAnswer2.setOnClickListener(v -> checkAnswer((MaterialButton) v));
            buttonAnswer3.setOnClickListener(v -> checkAnswer((MaterialButton) v));
            buttonAnswer4.setOnClickListener(v -> checkAnswer((MaterialButton) v));
        }

    }

    private void previousQuestion() {
        if (questionPointer > 0) {
            questionPointer --;
        } else {
            questionPointer = questions.size() - 1;
        }
        loadQuestion();
    }

    private void nextQuestion() {
        if (questionPointer == questions.size() - 1) {
            questionPointer = 0;
        } else {
            questionPointer ++;
        }
        loadQuestion();
    }

    private void checkAnswer(MaterialButton answerButton) {
        buttonAnswer1.setOnClickListener(null);
        buttonAnswer2.setOnClickListener(null);
        buttonAnswer3.setOnClickListener(null);
        buttonAnswer4.setOnClickListener(null);

        String answerText = (String) answerButton.getText();
        String correctAnswer;
        Vokabel quizVocab = quizVocabs.get(questionPointer);

        vokabelViewModel.updateAnswered(quizVocab.getId(), quizVocab.getAnswered() + 1);

        if (language_from_to == QuizSettings.DE_TO_ENG) {
            correctAnswer = quizVocab.getVokabelENG();
        } else {
            correctAnswer = quizVocab.getVokabelDE();
        }

        if (answerText.equals(correctAnswer)) {
            givePoint();
            answerButton.setBackgroundColor(Color.rgb(88, 214, 141));
            vokabelViewModel.updateCountRightAnswers(quizVocab.getId(),
                    quizVocab.getRichtig() + 1);
        } else {
            answerButton.setBackgroundColor(Color.rgb(236, 112, 99));
            vokabelViewModel.updateCountWrongAnswers(quizVocab.getId(),
                    quizVocab.getFalsch() + 1);
        }

        int answerButtonNum;
        if (answerButton.equals(buttonAnswer1)) {
            answerButtonNum = 1;
        } else if (answerButton.equals(buttonAnswer2)) {
            answerButtonNum = 2;
        } else if (answerButton.equals(buttonAnswer3)) {
            answerButtonNum = 3;
        } else {
            answerButtonNum = 4;
        }

        answers.putInt(String.valueOf(questionPointer), answerButtonNum);

        checkIfSolved();
    }

    private void checkIfSolved() {
        if (answers.size() == questions.size()) {
            buttonPreviousQuestion.setOnClickListener(null);
            buttonNextQuestion.setOnClickListener(null);

            resultCard.setVisibility(View.VISIBLE);
            resultCountRight.setText(String.format("%s Fragen richtig", pointsCounter));
            resultCountWrong.setText(String.format("%s Fragen falsch",
                    (questions.size() - pointsCounter)));

            restartButton.setOnClickListener(v -> finish());
        }
    }

    private void givePoint() {
        pointsCounter ++;
        pointsCounterView.setText(String.valueOf(pointsCounter));
    }
}
