package com.rommelbendel.scanQ;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.cardview.widget.CardView;
import androidx.viewpager.widget.PagerAdapter;

import com.google.android.material.button.MaterialButton;
import com.yuvraj.livesmashbar.enums.GravityView;
import com.yuvraj.livesmashbar.view.LiveSmashBar;

import java.util.ArrayList;
import java.util.List;

public class CardPagerAdapter extends PagerAdapter implements CardAdapter {

    private List<CardView> mViews;
    private List<CardItem> mData;
    private float mBaseElevation;

    private Activity activity;

    public CardPagerAdapter(Activity act) {
        activity = act;

        mData = new ArrayList<>();
        mViews = new ArrayList<>();
    }

    public void addCardItem(CardItem item) {
        mViews.add(null);
        mData.add(item);
    }

    public float getBaseElevation() {
        return mBaseElevation;
    }

    @Override
    public CardView getCardViewAt(int position) {
        return mViews.get(position);
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view = LayoutInflater.from(container.getContext())
                .inflate(R.layout.adapter_intro_fragment, container, false);
        container.addView(view);
        bind(mData.get(position), view);

        CardView cardView = view.findViewById(R.id.cardView);
        MaterialButton button = view.findViewById(R.id.intro_finish_button);
        EditText editText = view.findViewById(R.id.into_name);

        button.setOnClickListener( v -> {
            TinyDB tb = new TinyDB(v.getContext());

            if(editText.getText().toString().trim().length() != 0) {
                tb.putString("username", editText.getText().toString());

                Intent i = new Intent(v.getContext(), home.class);
                v.getContext().startActivity(i);
            } else {
                new LiveSmashBar.Builder(activity)
                        .title("Bitte gib deinen Namen ein")
                        .titleColor(Color.WHITE)
                        .backgroundColor(Color.parseColor("#541111"))
                        .gravity(GravityView.BOTTOM)
                        .primaryActionText("Ok")
                        .primaryActionEventListener(LiveSmashBar::dismiss)
                        .duration(3000)
                        .show();
            }
        });

        if (mBaseElevation == 0) {
            mBaseElevation = cardView.getCardElevation();
        }

        cardView.setMaxCardElevation(mBaseElevation * MAX_ELEVATION_FACTOR);
        mViews.set(position, cardView);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
        mViews.set(position, null);
    }

    private void bind(CardItem item, View view) {

        LinearLayout ll = view.findViewById(item.getLinearLayout());
        ll.setVisibility(View.VISIBLE);
    }
}