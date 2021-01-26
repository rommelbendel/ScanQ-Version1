package com.rommelbendel.scanQ;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatImageButton;

import org.jetbrains.annotations.NotNull;

public class ButtonVoiceInput extends AppCompatImageButton {

    private EditText input_voice_to_text = null;

    public ButtonVoiceInput(Context context) {
        super(context);
    }

    public ButtonVoiceInput(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public ButtonVoiceInput(Context context, @NotNull EditText inputVoiceToText) {
        super(context);
        setBackgroundResource(R.drawable.crop_image_menu_rotate_left);
        input_voice_to_text = inputVoiceToText;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Toast.makeText(getContext(), "Aufnahme gestartet", Toast.LENGTH_SHORT).show();
                return true;

            case MotionEvent.ACTION_UP:
                performClick();
                Toast.makeText(getContext(), "Aufnahme beendet", Toast.LENGTH_SHORT).show();
                record();
                return true;
        }
        return false;
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

    public void record() {
        //quiz_voice.start_record();
        if (input_voice_to_text != null) {
            input_voice_to_text.setText("aufgenommener Text...");
        }

    }
}
