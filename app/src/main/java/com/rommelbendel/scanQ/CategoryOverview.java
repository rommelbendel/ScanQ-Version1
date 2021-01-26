package com.rommelbendel.scanQ;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CategoryOverview extends AppCompatActivity {

    private LinearLayout categoryList;
    private TextView loadingText;
    private ProgressBar loadingSymbol;

    private VokabelViewModel vokabelViewModel;
    private KategorienViewModel kategorienViewModel;
    private LiveData<List<Kategorie>> alleKategorien;
    private List<Kategorie> kategorienAktuell;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_overview);

        categoryList = findViewById(R.id.categorySelection);
        loadingText = findViewById(R.id.loadingText);
        loadingSymbol = findViewById(R.id.loadingSymbol);
        ImageButton addCategoryButton = findViewById(R.id.addButton);

        vokabelViewModel = new ViewModelProvider(this).get(VokabelViewModel.class);
        kategorienViewModel = new ViewModelProvider(this).get(
                KategorienViewModel.class);

        alleKategorien = kategorienViewModel.getAlleKategorien();

        alleKategorien.observe(this, kategories -> {
            if (kategories != null) {
                if (kategories.size() > 0) {
                    kategorienAktuell = kategories;
                    displayCategoryList(kategories);
                } else {
                    Toast.makeText(CategoryOverview.this,
                            "Keine Kategorien vorhanden.", Toast.LENGTH_LONG).show();
                    loadingText.setText(R.string.keine_kategorien_vorhanden);
                    loadingSymbol.setVisibility(View.GONE);
                }

                alleKategorien.removeObservers(CategoryOverview.this);
                alleKategorien.observe(CategoryOverview.this, kategories1 -> {
                    if (kategories1 != null) {
                        if (kategories1 != kategorienAktuell && kategories1.size() > 0)
                            CategoryOverview.this.recreate();
                    }
                });
            }
        });

        addCategoryButton.setOnClickListener(v -> {
            final AlertDialog.Builder optionsMenu = new AlertDialog.Builder(
                    CategoryOverview.this);
            optionsMenu.setTitle("Was möchtest du machen?");
            optionsMenu.setMessage("Kategorie erstellen oder Vokabeln einscannen?");
            optionsMenu.setPositiveButton("Kategorie", (dialog, which) -> {
                dialog.dismiss();
                Intent addCategoryIntent = new Intent(CategoryOverview.this,
                        CategoryManager.class);
                addCategoryIntent.putExtra("Mode", CategoryManager.NEW_CATEGORY);
                CategoryOverview.this.startActivity(addCategoryIntent);
            });
            optionsMenu.setNegativeButton("Abbrechen", (dialog, which) -> dialog.dismiss());
            optionsMenu.setNeutralButton("Vokabeln", (dialog, which) -> {
                dialog.dismiss();
                Intent scanIntent = new Intent(CategoryOverview.this,
                        Haupt.class);
                CategoryOverview.this.startActivity(scanIntent);
            });
            optionsMenu.show();
        });
    }

    private void displayCategoryList(@NotNull List<Kategorie> categories) {
        loadingText.setVisibility(View.GONE);
        loadingSymbol.setVisibility(View.GONE);

        for (Kategorie category: categories) {
            Button categoryButton = new Button(this);
            categoryButton.setText(category.getName());
            categoryButton.setBackgroundResource(R.drawable.list_button);
            categoryButton.setTextSize(25);
            categoryButton.setTextColor(Color.parseColor("#ffffff"));
            categoryButton.setOnClickListener(v -> {
                String category12 = (String) ((Button) v).getText();
                Intent showVocabsIntent = new Intent(CategoryOverview.this,
                        AlleVokabelnAnzeigen.class);
                showVocabsIntent.putExtra("Category", category12);
                CategoryOverview.this.startActivity(showVocabsIntent);
            });
            categoryButton.setOnLongClickListener(v -> {
                final String category1 = (String) ((Button) v).getText();

                AlertDialog.Builder dialog = new AlertDialog.Builder(CategoryOverview.this);
                dialog.setTitle("Optionen");
                dialog.setMessage("Möchtest du die diese Kategorie bearbeiten?");
                dialog.setPositiveButton("bearbeiten", (dialog1, which) -> {
                    dialog1.dismiss();
                    Intent addCategoryIntent = new Intent(CategoryOverview.this,
                            CategoryManager.class);
                    addCategoryIntent.putExtra("Mode", CategoryManager.EDIT_CATEGORY);
                    addCategoryIntent.putExtra("Category", category1);
                    CategoryOverview.this.startActivity(addCategoryIntent);
                });
                dialog.setNeutralButton("löschen", (dialog12, which) -> {
                    dialog12.dismiss();
                    AlertDialog.Builder deletitionAlert = new AlertDialog.Builder(this);
                    deletitionAlert.setTitle("Kategorie löschen?");
                    deletitionAlert.setMessage("Möchtest du die Kategorie und die dazugehörigen " +
                            "Vokabeln wirklich löschen?");
                    deletitionAlert.setPositiveButton("ja", (dialog14, which1) -> {
                        dialog14.dismiss();
                        kategorienViewModel.deleteWithName(category1);
                        vokabelViewModel.deleteCategory(category1);
                    });
                    deletitionAlert.setNegativeButton("nein", (dialog15, which12) -> {
                        dialog15.dismiss();
                    });
                    deletitionAlert.show();
                });
                dialog.setNegativeButton("abbrechen", (dialog13, which) -> dialog13.dismiss());
                dialog.show();

                return true;
            });
            categoryList.addView(categoryButton);

            //placeholder between the buttons
            TextView placeholder = new TextView(this);
            //change the constant factor (10) which is the dp value for the placeholder to change the margin
            int margin = (int) (10 * this.getResources().getDisplayMetrics().density);
            placeholder.setHeight(margin);
            categoryList.addView(placeholder);
        }
    }
}
