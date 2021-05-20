package com.example.pathfollowingcar;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.text.Html;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Locale;

public class Voice extends AppCompatActivity {

    public static final Integer RecordAudioRequestCode = 1;
    private SpeechRecognizer speechRecognizer;
    private EditText editText;
    private ImageView micButton;

    private ClientSocket connection;

    final class workerThread implements Runnable {
        private final String btMsg;
        public workerThread(String msg) { btMsg = msg; }
        public void run() {
            connection.sendMessage("[VOICE]" + btMsg);
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice);
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
            checkPermission();
        }

        setTitle("Voice control");

        editText = findViewById(R.id.text);
        micButton = findViewById(R.id.speech2);
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);


        connection = ClientSocket.getInstance(getApplicationContext());

        TextView controls = (TextView) findViewById(R.id.controls);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            controls.setText(Html.fromHtml("<ol>\n" +
                    "<li>Move Forward @number@ - the car will move forwards for a distance equal to @number@ in centimeters</li>\n" +
                    "<li>Move backwards @number@ - the car will move backwards for a distance equal to @number@ in centimeters</li>\n" +
                    "<li>Rotate clockwise - the car will rotate 15 degrees clockwise</li>\n" +
                    "<li>Rotate counterclockwise - the car will rotate 15 degrees counter clockwise</li>\n" +
                    "</ol>", Html.FROM_HTML_MODE_COMPACT));
        }


        final Intent speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {

            }

            @Override
            public void onBeginningOfSpeech() {
                editText.setText("");
                editText.setHint("Listening...");
            }

            @Override
            public void onRmsChanged(float v) {

            }

            @Override
            public void onBufferReceived(byte[] bytes) {

            }

            @Override
            public void onEndOfSpeech() {

            }

            @Override
            public void onError(int i) {

            }

            @Override
            public void onResults(Bundle bundle) {
                micButton.setImageResource(R.drawable.ic_mic_black_off);
                ArrayList<String> data = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                editText.setText(data.get(0));
                (new Thread(new workerThread(data.get(0)))).start();
            }

            @Override
            public void onPartialResults(Bundle bundle) {

            }

            @Override
            public void onEvent(int i, Bundle bundle) {

            }
        });

        micButton.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_UP){
                speechRecognizer.stopListening();
            }
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                micButton.setImageResource(R.drawable.ic_mic_black_off_24dp);
                speechRecognizer.startListening(speechRecognizerIntent);
            }
            return false;
        });


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        speechRecognizer.destroy();
    }

    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.RECORD_AUDIO},RecordAudioRequestCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RecordAudioRequestCode && grantResults.length > 0 ){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
                Toast.makeText(this,"Permission Granted",Toast.LENGTH_SHORT).show();
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch(item.getItemId()) {
            case R.id.draw:
                intent = new Intent(getApplicationContext(), Drawing.class);
                startActivity(intent);
                return true;
            case R.id.control:
                intent = new Intent(getApplicationContext(), Controls.class);
                startActivity(intent);
                return true;
            case R.id.voice:
                return true;
            case R.id.maps:
                intent = new Intent(getApplicationContext(), Maps.class);
                startActivity(intent);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}