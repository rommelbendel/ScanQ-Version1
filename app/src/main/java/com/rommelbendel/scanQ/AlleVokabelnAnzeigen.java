package com.rommelbendel.scanQ;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
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
import androidx.cardview.widget.CardView;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
import com.yuvraj.livesmashbar.enums.GravityView;
import com.yuvraj.livesmashbar.view.LiveSmashBar;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class AlleVokabelnAnzeigen extends AppCompatActivity {

    private VokabelViewModel vokabelViewModel;
    private LiveData<List<Vokabel>> vocabsLiveData;

    private TextView header;
    private ViewSwitcher toolbar;
    private ImageButton saveButton;
    private ImageButton cancelButton;
    private RecyclerView vocabTable;
    private RecyclerView vocabTableEdit;
    private ViewSwitcher tableSwitcher;


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.datenausgabe);

        header = findViewById(R.id.header);
        vocabTable = findViewById(R.id.tabelleVocView);
        vocabTableEdit = findViewById(R.id.tabelleVoc);
        toolbar = findViewById(R.id.toolbar);
        saveButton = findViewById(R.id.saveButton);
        cancelButton = findViewById(R.id.cancelButton);
        tableSwitcher = findViewById(R.id.tableSwitcher);

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
            tableSwitcher.setDisplayedChild(1);

            saveButton.setOnClickListener(v1 -> {
                int rowNum = vocabTable.getChildCount();
                for (int i = 0; i < rowNum; i++){
                    TableLayout tableLayoutOriginal = (TableLayout) vocabTable.getChildAt(i);
                    TableRow rowOriginal = (TableRow) tableLayoutOriginal.getChildAt(0);

                    CardView cardViewOrigENG = (CardView) rowOriginal.getChildAt(0);
                    TextView textViewOrigENG = (TextView) cardViewOrigENG.getChildAt(0);
                    String engoriginal = (String) textViewOrigENG.getText();

                    CardView cardViewOrigDE = (CardView) rowOriginal.getChildAt(1);
                    TextView textViewOrigDE = (TextView) cardViewOrigDE.getChildAt(0);
                    String deoriginal = (String) textViewOrigDE.getText();


                    TableLayout tableLayoutUpdated = (TableLayout) vocabTableEdit.getChildAt(i);
                    TableRow rowUpdated = (TableRow) tableLayoutUpdated.getChildAt(0);

                    CardView cardViewUpdatedENG = (CardView) rowUpdated.getChildAt(0);
                    TextView textViewUpdatedENG = (TextView) cardViewUpdatedENG.getChildAt(0);
                    String engupdated = textViewUpdatedENG.getText().toString().trim();

                    CardView cardViewUpdatedDE = (CardView) rowUpdated.getChildAt(1);
                    TextView textViewUpdatedDE = (TextView) cardViewUpdatedDE.getChildAt(0);
                    String deupdated = textViewUpdatedDE.getText().toString().trim();

                    if (!engupdated.equals(engoriginal) && !engupdated.isEmpty()) {
                        vokabelViewModel.updateVokabelENG(engoriginal, engupdated);
                    }

                    if (!deupdated.equals(deoriginal) && !deupdated.isEmpty()) {
                        vokabelViewModel.updateVokabelDE(deoriginal, deupdated);
                    }

                }

                AlleVokabelnAnzeigen.this.recreate();
            });

            cancelButton.setOnClickListener(v12 -> {
                toolbar.setDisplayedChild(0);
                tableSwitcher.setDisplayedChild(0);
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

        final TabelleVokabelAdapter stringVokabelAdapter = new TabelleVokabelAdapter(this,
                TabelleVokabelAdapter.OUTPUT_MODE_EDITABLE, vocabs, vokabelViewModel);
        vocabTableEdit.setAdapter(stringVokabelAdapter);
        vocabTableEdit.setLayoutManager(new LinearLayoutManager(this));
        stringVokabelAdapter.setVokabelCache(vocabs);

        final TabelleVokabelAdapter stringVokabelAdapterView = new TabelleVokabelAdapter(this,
                TabelleVokabelAdapter.OUTPUT_MODE_VIEWABLE, vocabs, vokabelViewModel);
        vocabTable.setAdapter(stringVokabelAdapterView);
        vocabTable.setLayoutManager(new LinearLayoutManager(this));
        stringVokabelAdapterView.setVokabelCache(vocabs);

    }

}
