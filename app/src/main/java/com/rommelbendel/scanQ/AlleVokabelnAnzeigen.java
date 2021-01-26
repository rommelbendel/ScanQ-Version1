package com.rommelbendel.scanQ;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;

import java.util.Collections;
import java.util.List;

public class AlleVokabelnAnzeigen extends AppCompatActivity {

    private VokabelViewModel vokabelViewModel;
    private LiveData<List<Vokabel>> vocabsLiveData;

    private TextView header;
    private TableLayout vocabTable;
    private ViewSwitcher toolbar;
    private ImageButton saveButton;
    private ImageButton cancelButton;


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.datenausgabe);

        header = findViewById(R.id.header);
        vocabTable = findViewById(R.id.vocabTable);
        toolbar = findViewById(R.id.toolbar);
        saveButton = findViewById(R.id.saveButton);
        cancelButton = findViewById(R.id.cancelButton);

        vokabelViewModel = new ViewModelProvider(this).get(VokabelViewModel.class);

        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            String category = extras.getString("Category");

            assert category != null;
            if (!category.isEmpty()) {
                loadCategory(category);
            } else {
                loadAll();
            }
        } else {
            loadAll();
        }

        final ImageButton addButton = findViewById(R.id.addButton);
        final ImageButton editButton = findViewById(R.id.editButton);
        ScrollView scrollView = findViewById(R.id.ScrollView);
        scrollView.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            if (oldScrollY > scrollY) {
                addButton.setVisibility(View.VISIBLE);
                editButton.setVisibility(View.VISIBLE);
                toolbar.setVisibility(View.VISIBLE);
            }
            else {
                addButton.setVisibility(View.INVISIBLE);
                editButton.setVisibility(View.INVISIBLE);
                toolbar.setVisibility(View.INVISIBLE);
            }
        });

        addButton.setOnClickListener(v -> {
            final AlertDialog.Builder neueVokabelDialogBuilder = new AlertDialog.Builder(
                    AlleVokabelnAnzeigen.this);
            neueVokabelDialogBuilder.setTitle("Vokabel hinzufügen");
            neueVokabelDialogBuilder.setMessage("Wie möchtest du fortfahren?");

            neueVokabelDialogBuilder.setPositiveButton("manuell", (dialog, which) -> {
                dialog.cancel();

                Intent intentManuell = new Intent(AlleVokabelnAnzeigen.this,
                        VokabelManuell.class);
                AlleVokabelnAnzeigen.this.startActivity(intentManuell);
                finish();
            });

            neueVokabelDialogBuilder.setNeutralButton("scannen", (dialog, which) -> {
                Toast toast = Toast.makeText(AlleVokabelnAnzeigen.this, "scannen",
                        Toast.LENGTH_SHORT);
                toast.show();
                dialog.cancel();
            });

            neueVokabelDialogBuilder.setNegativeButton("abbrechen",
                    (dialog, which) -> dialog.cancel());

            neueVokabelDialogBuilder.show();
        });

        editButton.setOnClickListener(v -> {
            toolbar.setDisplayedChild(1);
            int rowNum = vocabTable.getChildCount();
            for (int i = 0; i < rowNum; i++) {
                TableRow row = (TableRow) vocabTable.getChildAt(i);
                ((ViewSwitcher) row.getChildAt(0)).setDisplayedChild(1);
                ((ViewSwitcher) row.getChildAt(1)).setDisplayedChild(1);
            }

            saveButton.setOnClickListener(v1 -> {
                int rowNum1 = vocabTable.getChildCount();
                for (int i = 0; i < rowNum1; i++) {
                    TableRow row = (TableRow) vocabTable.getChildAt(i);
                    ViewSwitcher english = ((ViewSwitcher) row.getChildAt(0));
                    ViewSwitcher german = ((ViewSwitcher) row.getChildAt(1));

                    String engOriginal = (String) ((TextView) english.getChildAt(0))
                            .getText();
                    String engEdited = ((EditText) english.getChildAt(1)).getText()
                            .toString().trim();

                    if (!engEdited.equals(engOriginal) && !engEdited.isEmpty()) {
                        vokabelViewModel.updateVokabelENG(engOriginal, engEdited);
                    }

                    String deOriginal = (String) ((TextView) german.getChildAt(0)).getText();
                    String deEdited = ((EditText) german.getChildAt(1)).getText()
                            .toString().trim();

                    if (!deEdited.equals(deOriginal) && !engEdited.isEmpty()) {
                        vokabelViewModel.updateVokabelDE(deOriginal, deEdited);
                    }
                }

                AlleVokabelnAnzeigen.this.recreate();
            });

            cancelButton.setOnClickListener(v12 -> {
                toolbar.setDisplayedChild(0);
                int rowNum12 = vocabTable.getChildCount();
                for (int i = 0; i < rowNum12; i++) {
                    TableRow row = (TableRow) vocabTable.getChildAt(i);
                    ((ViewSwitcher) row.getChildAt(0)).setDisplayedChild(0);
                    ((ViewSwitcher) row.getChildAt(1)).setDisplayedChild(0);
                }
            });
        });

    }

    private void loadCategory(String categoryName) {
        header.setText(categoryName);
        vocabsLiveData = vokabelViewModel.getCategoryVocabs(categoryName);
        vocabsLiveData.observe(this, vocabs -> {
            if (vocabs != null) {
                if (!vocabs.isEmpty()) {
                    insertInTable(vocabs);
                } else {
                    Toast.makeText(AlleVokabelnAnzeigen.this,
                            "keine Vokabeln vorhanden", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadAll() {
        header.setText(R.string.alle_vokabeln);
        vocabsLiveData = vokabelViewModel.getAlleVokabeln();
        vocabsLiveData.observe(this, vocabs -> {
            if (vocabs != null) {
                if (!vocabs.isEmpty()) {
                    insertInTable(vocabs);
                } else {
                    Toast.makeText(AlleVokabelnAnzeigen.this,
                            "keine Vokabeln vorhanden", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void insertInTable(List<Vokabel> vocabs) {
        Collections.sort(vocabs, (voc1, voc2) -> voc1.getVokabelENG()
                .compareToIgnoreCase(voc2.getVokabelENG()));
        for (final Vokabel vocab: vocabs) {
            TableRow vocabRow = new TableRow(this);
            vocabRow.setTag(vocab);
            vocabRow.setPadding(5, 5, 5, 5);
            vocabRow.setFocusable(true);
            vocabRow.setClickable(true);
            vocabRow.setLongClickable(true);

            vocabRow.setOnLongClickListener(v -> {
                final Vokabel vocabRequested = (Vokabel) v.getTag();

                AlertDialog.Builder markingMenu = new AlertDialog.Builder(
                        AlleVokabelnAnzeigen.this);
                markingMenu.setTitle(vocabRequested.getVokabelENG());
                if (!vocabRequested.isMarkiert()) {
                    markingMenu.setMessage("Soll die Vokabel markiert werden?");
                    markingMenu.setPositiveButton("ja", (dialog, which) -> {
                        dialog.dismiss();
                        vokabelViewModel.updateMarking(vocab.getVokabelDE(), true);
                        AlleVokabelnAnzeigen.this.recreate();
                    });
                    markingMenu.setNegativeButton("nein", (dialog, which) -> dialog.dismiss());
                } else {
                    markingMenu.setMessage("Soll die Markierung entfernt werden?");
                    markingMenu.setPositiveButton("ja", (dialog, which) -> {
                        dialog.dismiss();
                        vokabelViewModel.updateMarking(vocab.getVokabelDE(), false);
                        AlleVokabelnAnzeigen.this.recreate();
                    });
                    markingMenu.setNegativeButton("nein", (dialog, which) -> dialog.dismiss());
                }
                markingMenu.show();
                return false;
            });

            TableRow.LayoutParams marginParams = new TableRow.LayoutParams();
            marginParams.setMargins(0, 0, 5, 0);

            ViewSwitcher viewSwitcherENG = new ViewSwitcher(this);

            TextView english = new TextView(this);
            english.setText(vocab.getVokabelENG());
            english.setTextColor(Color.WHITE);
            english.setTextSize(20);
            english.setBackgroundColor(Color.TRANSPARENT);
            english.setPadding(15, 5, 15, 5);
            english.setLayoutParams(marginParams);

            EditText englishEdit = new EditText(this);
            englishEdit.setText(vocab.getVokabelENG());
            englishEdit.setHint("Englisch");
            englishEdit.setTextColor(Color.WHITE);
            englishEdit.setTextSize(20);
            englishEdit.setBackgroundColor(Color.TRANSPARENT);
            englishEdit.setPadding(15, 5, 15, 5);
            englishEdit.setLayoutParams(marginParams);

            viewSwitcherENG.addView(english, 0);
            viewSwitcherENG.addView(englishEdit, 1);
            viewSwitcherENG.setDisplayedChild(0);


            marginParams.setMargins(5, 0, 0, 0);

            ViewSwitcher viewSwitcherDE = new ViewSwitcher(this);

            TextView german = new TextView(this);
            german.setText(vocab.getVokabelDE());
            german.setTextColor(Color.WHITE);
            german.setTextSize(20);
            german.setBackgroundColor(Color.TRANSPARENT);
            german.setPadding(15, 5, 15, 5);
            german.setLayoutParams(marginParams);

            EditText germanEdit = new EditText(this);
            germanEdit.setText(vocab.getVokabelDE());
            germanEdit.setHint("Deutsch");
            germanEdit.setTextColor(Color.WHITE);
            germanEdit.setTextSize(20);
            germanEdit.setBackgroundColor(Color.TRANSPARENT);
            germanEdit.setPadding(15, 5, 15, 5);
            germanEdit.setLayoutParams(marginParams);

            viewSwitcherDE.addView(german, 0);
            viewSwitcherDE.addView(germanEdit, 1);
            viewSwitcherDE.setDisplayedChild(0);


            if (vocab.isMarkiert()) {
                english.setBackgroundColor(Color.parseColor("#9b59b6"));
                englishEdit.setBackgroundColor(Color.parseColor("#9b59b6"));
                german.setBackgroundColor(Color.parseColor("#9b59b6"));
                germanEdit.setBackgroundColor(Color.parseColor("#9b59b6"));
            } else {
                english.setBackgroundColor(Color.parseColor("#13b5a4"));
                englishEdit.setBackgroundColor(Color.parseColor("#13b5a4"));
                german.setBackgroundColor(Color.parseColor("#13b5a4"));
                germanEdit.setBackgroundColor(Color.parseColor("#13b5a4"));
            }

            vocabRow.addView(viewSwitcherENG);
            vocabRow.addView(viewSwitcherDE);

            vocabTable.addView(vocabRow);
        }
    }
}
