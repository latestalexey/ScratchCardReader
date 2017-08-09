
package com.google.android.gms.samples.vision.ocrreader;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.support.v7.app.AlertDialog;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.samples.vision.ocrreader.ui.camera.GraphicOverlay;
import com.google.android.gms.vision.text.Text;
import com.google.android.gms.vision.text.TextBlock;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.List;
import java.util.Objects;

/**
 * Graphic instance for rendering TextBlock position, size, and ID within an associated graphic
 * overlay view.
 */
public class OcrGraphic extends GraphicOverlay.Graphic implements View.OnClickListener{

    public static final String TAG = OcrGraphic.class.getSimpleName();

    public static String PIN = "";
    public static String SERIAL = "";
    public static String LASTSERIAL = "";

    private static final int TEXT_COLOR = Color.WHITE;
    private static Paint sRectPaint;
    private static Paint sTextPaint;

    private static String SERIALBUF;
    private static String PINBUF;

    private final TextBlock mText;
    private int mId;
    private static AlertDialog dialog;
    private OcrCaptureActivity ocrCaptureActivity;

    private EditText pin;
    private EditText serial;
    private TextView textView;
    private Button add;
    private Button cancel;

    private Button yes;
    private Button no;
    private TextView textMessage;
    private EditText editText;

    OcrGraphic(GraphicOverlay<OcrGraphic> overlay, TextBlock text, OcrCaptureActivity ocrCaptureActivity) {
        super(overlay);

        this.ocrCaptureActivity = ocrCaptureActivity;
        mText = text;

        if (sRectPaint == null) {
            sRectPaint = new Paint();
            sRectPaint.setColor(TEXT_COLOR);
            sRectPaint.setStyle(Paint.Style.STROKE);
            sRectPaint.setStrokeWidth(4.0f);
        }

        if (sTextPaint == null) {
            sTextPaint = new Paint();
            sTextPaint.setColor(TEXT_COLOR);
            sTextPaint.setTextSize(54.0f);
        }
        postInvalidate();
    }

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        this.mId = id;
    }

    public TextBlock getTextBlock() {
        return mText;
    }

    public boolean contains(float x, float y) {
        if (mText == null) {
            return false;
        }
        RectF rect = new RectF(mText.getBoundingBox());
        rect.left = translateX(rect.left);
        rect.top = translateY(rect.top);
        rect.right = translateX(rect.right);
        rect.bottom = translateY(rect.bottom);
        return (rect.left < x && rect.right > x && rect.top < y && rect.bottom > y);
    }

    @Override
    public void draw(Canvas canvas) {

        if (mText == null) {
            return;
        }

        String text = mText.getValue();
        if (Utils.isPIN(text)) {
            if(dialog != null) {
                if(!dialog.isShowing()) {
                    Log.d("Text", mText.getValue());
                    PIN = text;
                    drawText(canvas);
                }
            }
            else {
                Log.d("Text", mText.getValue());
                PIN = text;
                drawText(canvas);
            }
        }

        if (Utils.isSerial(text)) {
            if(dialog != null) {
                if(!dialog.isShowing()) {
                    Log.d("Text", mText.getValue());
                    drawText(canvas);
                    SERIAL = text;
                }
            }
            else {
                Log.d("Text", mText.getValue());
                SERIAL = text;
                drawText(canvas);
            }
        }

        Log.d("Text", "(" + SERIAL + ";" + PIN + ")");
        if(!Objects.equals(PIN, "") && !Objects.equals(SERIAL, "")) {
            PIN = PIN.replace(" ", "");
            SERIAL = SERIAL.replace(" ", "");
            if(!Objects.equals(LASTSERIAL, "")) {
                if (Integer.parseInt(LASTSERIAL) + 1 != Integer.parseInt(SERIAL)) {
                    if (dialog != null) {
                        if (!dialog.isShowing()) {
                            playBeep();
                            dialog = createUserErrorDialog();
                            initializeUserErrorView();
                            dialog.show();

                            PIN = "";
                            SERIAL = "";
                            Log.d(TAG, "null");
                        }
                    } else {
                        playBeep();
                        dialog = createUserErrorDialog();
                        initializeUserErrorView();
                        dialog.show();

                        PIN = "";
                        SERIAL = "";
                        Log.d(TAG, "null");
                    }
                }
            }
            if (dialog != null) {
                if (!dialog.isShowing()) {

                    playBeep();

                    dialog = createDialog();
                    initializeView();
                    dialog.show();

                    Log.d(TAG, "show");

                    PIN = "";
                    SERIAL = "";
                }
            } else {

                playBeep();
                dialog = createDialog();
                initializeView();
                dialog.show();

                PIN = "";
                SERIAL = "";
                Log.d(TAG, "null");
            }
        }
    }

    private AlertDialog createDialog()
    {

        AlertDialog.Builder builder = new AlertDialog.Builder(ocrCaptureActivity);
        View view = ocrCaptureActivity.getLayoutInflater().inflate(R.layout.dialog_enroll, null);

        add = (Button) view.findViewById(R.id.add);
        cancel = (Button) view.findViewById(R.id.cancel);
        pin = (EditText) view.findViewById(R.id.PIN);
        serial = (EditText) view.findViewById(R.id.Serial);
        textView = (TextView) view.findViewById(R.id.textView);

        //ButterKnife.bind(view);

        builder.setView(view);
        return builder.create();
    }

    private AlertDialog createUserErrorDialog()
    {

        AlertDialog.Builder builder = new AlertDialog.Builder(ocrCaptureActivity);
        View view = ocrCaptureActivity.getLayoutInflater().inflate(R.layout.dialog_user_error, null);

        yes = (Button) view.findViewById(R.id.yes);
        yes.setClickable(false);
        no = (Button) view.findViewById(R.id.no);
        textMessage = (TextView) view.findViewById(R.id.text_message);
        editText = (EditText) view.findViewById(R.id.editText);

        //ButterKnife.bind(view);

        builder.setView(view);
        return builder.create();
    }

    private void playBeep()
    {

        MediaPlayer mPlayer = MediaPlayer.create(ocrCaptureActivity, R.raw.beep);
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mPlayer.start();
    }

    private void initializeUserErrorView()
    {
        SERIALBUF = SERIAL;
        PINBUF = PIN;
        textMessage.setText(String.format(ocrCaptureActivity
                .getResources()
                .getString(R.string.user_error),
                LASTSERIAL,
                String .valueOf(Integer.parseInt(LASTSERIAL) + 1)));
        yes.setOnClickListener(this);
        no.setOnClickListener(this);
    }

    private void initializeView()
    {
        pin.setText(PIN);
        serial.setText(SERIAL);
        add.setOnClickListener(this);
        cancel.setOnClickListener(this);
    }
    private String getReminingTime() {
        String delegate = " dd MMM KK:mm:ss aa";
        return (String) DateFormat.format(delegate, Calendar.getInstance().getTime());
    }

    private void drawText(Canvas canvas) {
        RectF rect = new RectF(mText.getBoundingBox());
        rect.left = translateX(rect.left);
        rect.top = translateY(rect.top);
        rect.right = translateX(rect.right);
        rect.bottom = translateY(rect.bottom);
        canvas.drawRect(rect, sRectPaint);

        // Break the text into multiple lines and draw each one according to its own bounding box.
        List<? extends Text> textComponents = mText.getComponents();
        for (Text currentText : textComponents) {
            float left = translateX(currentText.getBoundingBox().left);
            float bottom = translateY(currentText.getBoundingBox().bottom);
            canvas.drawText(currentText.getValue(), left, bottom, sTextPaint);
        }
    }

    @Override
    public void onClick(View v) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference();
        switch (v.getId())
        {
            case R.id.add:


                myRef.child(String.valueOf(serial.getText()))
                        .setValue(String.valueOf(pin.getText()));

                LASTSERIAL = String.valueOf(serial.getText());
                PIN = "";
                SERIAL = "";
                dialog.dismiss();
                break;
            case R.id.cancel:
                PIN = "";
                SERIAL = "";
                dialog.dismiss();
                break;
            case R.id.yes:
                if(Objects.equals(String.valueOf(editText.getText()), "1234")) {
                    myRef.child(SERIALBUF).setValue(PINBUF);
                    LASTSERIAL = SERIALBUF;
                    PIN = "";
                    SERIAL = "";
                    dialog.dismiss();
                }
                else
                {
                    Toast.makeText(ocrCaptureActivity,"Write 1234 in field", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.no:
                PIN = "";
                SERIAL = "";
                dialog.dismiss();
                break;
        }
    }
}
