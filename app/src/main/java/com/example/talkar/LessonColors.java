package com.example.talkar;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.ar.core.Anchor;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

public class LessonColors extends AppCompatActivity {

    public String userSpokenText = "";
    public String tutorSpokenText = "";
    private ArFragment arFragment;
    private TextToSpeech textToSpeech;
    Button replay, next;
    int currentColorCount, colorsCompleted;
    String currentModel;
    String[] colorModels, currentColorOptions, color;

    TextView colorsText;

    String[] quizQuestions, quizAnswers;
    int quiz, quizCurrent, quizLength, quizFlag=0;
    public static final String sp_lesson_color_quiz = "ColorsQuiz";

    private static final String SHARED_PREFS = "sharedPrefs";
    public static final String sp_lesson_color = "ColorsCompleted";
    private static final String sp_username = "Username";

    private String username;

    private DatabaseReference reference;

    @Override
    protected void onResume() {
        super.onResume();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson_colors);

        reference = FirebaseDatabase.getInstance().getReference("lessons");

        SharedPreferences sharedPreferences = this.getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        colorsCompleted = sharedPreferences.getInt(sp_lesson_color, 0);
        username = sharedPreferences.getString(sp_username, "");

        colorModels = getResources().getStringArray(R.array.modelColor_array);
        color = getResources().getStringArray(R.array.color_array);

        colorsText = findViewById(R.id.colorsText);

        // Check if quiz for module is completed
        if (quiz == 1) {
            callDialog();
        } else {
            initLesson(colorsCompleted);
        }

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Colors");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        // AR Environment
        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.arFragment);
        arFragment.setOnTapArPlaneListener((hitResult, plane, motionEvent) -> {
            Anchor anchor = hitResult.createAnchor();
            ModelRenderable.builder()
                    .setSource(this, Uri.parse(currentModel))
                    .build()
                    .thenAccept(modelRenderable -> addModelToScene(anchor, modelRenderable))
                    .exceptionally(throwable -> {
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setMessage(throwable.getMessage())
                                .show();
                        return null;
                    });
        });


        // TextToSpeech
        textToSpeech = new TextToSpeech(this, status -> {
            if(status==TextToSpeech.SUCCESS){
                textToSpeech.setLanguage(Locale.GERMAN);
//                textToSpeech.setLanguage(new Locale("nl_NL"));
                speak(tutorSpokenText);
            }
        });

        replay = findViewById(R.id.replay);
        replay.setOnClickListener(v ->{
            textToSpeech.setLanguage(Locale.GERMAN);
//            textToSpeech.setLanguage(new Locale("nl_NL"));
            textToSpeech.speak(tutorSpokenText, TextToSpeech.QUEUE_FLUSH,null, null);
        });

        next = findViewById(R.id.next);
        next.setOnClickListener(v -> {
            if (quizFlag == 1) {
                initQuiz();
            } else if (quizFlag == 2 || quizFlag == 3){
                speak("Quiz cannot be skipped!");
            } else {
                proceedLesson();
            }
        });

    }

    private void initLesson(int colorsCompleted) {
        if (colorsCompleted < colorModels.length) {
            currentColorCount = colorsCompleted;
            setCurrentColorOptions(currentColorCount);
            currentModel = colorModels[colorsCompleted] + ".sfb";
            tutorSpokenText = color[colorsCompleted];
            colorsText.setText(colorModels[colorsCompleted]);
        } else {
            tutorSpokenText = "Congratulation, you've completed the Colors Lesson. Time for a small quiz! Tap on Next to proceed.";
            quizFlag = 1;
        }
    }

    private void setCurrentColorOptions(int currentColorCount) {
        switch (currentColorCount){
            case 0:
                currentColorOptions = getResources().getStringArray(R.array.answerColor_0);
                break;
            case 1:
                currentColorOptions = getResources().getStringArray(R.array.answerColor_1);
                break;
            case 2:
                currentColorOptions = getResources().getStringArray(R.array.answerColor_2);
                break;
            case 3:
                currentColorOptions = getResources().getStringArray(R.array.answerColor_3);
                break;
            case 4:
                currentColorOptions = getResources().getStringArray(R.array.answerColor_4);
                break;
            case 5:
                currentColorOptions = getResources().getStringArray(R.array.answerColor_5);
                break;
            case 6:
                currentColorOptions = getResources().getStringArray(R.array.answerColor_6);
                break;
        }
    }


    private void proceedLesson() {
        if (verifySpeech()){
            if (currentColorCount != colorModels.length-1){
                currentColorCount += 1;
                setCurrentColorOptions(currentColorCount);
                currentModel = colorModels[currentColorCount]+".sfb";
                tutorSpokenText = color[currentColorCount];
                textToSpeech.setLanguage(Locale.GERMAN);
//                textToSpeech.setLanguage(new Locale("nl_NL"));
                speak(tutorSpokenText);
                colorsText.setText(colorModels[currentColorCount]);
                updateSharedPrefs(currentColorCount);
                updateDatabase(currentColorCount);
            }
            else {
                updateSharedPrefs(currentColorCount +1);
                updateDatabase(currentColorCount +1);
                colorsText.setText("Congratulations!");
                tutorSpokenText = "Congratulation, you've completed the Colors Lesson. Time for a small quiz! Tap on Next to proceed.";
                textToSpeech.setLanguage(Locale.ENGLISH);
                speak(tutorSpokenText);
                quizFlag = 1;
            }
        }
        else{
            textToSpeech.setLanguage(Locale.ENGLISH);
            speak("Try again!");
            Toast.makeText(this, "Try again!", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateDatabase(int currentColorCount) {
        reference.child(username).child("colors").setValue(currentColorCount);
    }

    private void updateSharedPrefs(int currentColorCount) {
        SharedPreferences sharedPreferences = this.getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(sp_lesson_color, currentColorCount);
        editor.apply();
    }

    private boolean verifySpeech() {
        if (!userSpokenText.equals("")) {
            if (Arrays.asList(currentColorOptions).contains(userSpokenText)){
                textToSpeech.setLanguage(Locale.ENGLISH);
                speak("Correct answer!");
                Toast.makeText(this, "Correct answer!", Toast.LENGTH_LONG).show();
                try {
                    TimeUnit.SECONDS.sleep(2);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return true;
            }
            return false;
        }
        return false;
    }

    // AR function
    private void addModelToScene(Anchor anchor, ModelRenderable modelRenderable) {
        AnchorNode anchorNode = new AnchorNode(anchor);
        TransformableNode transformableNode = new TransformableNode(arFragment.getTransformationSystem());
        transformableNode.setParent(anchorNode);
        transformableNode.setRenderable(modelRenderable);
        arFragment.getArSceneView().getScene().addChild(anchorNode);
        transformableNode.select();
    }

    // SpeechToText functions
    public void getSpeechInput(View view) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
//        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.GERMAN);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "de-DE");
//        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "nl_NL");

        if (intent.resolveActivity(getPackageManager()) != null){
            startActivityForResult(intent, 10);
        } else {
            Toast.makeText(this, "Your device does not support speech input", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 10) {
            if (resultCode == RESULT_OK && data != null) {
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
//                textView.setText(result.get(0));
                userSpokenText = result.get(0).toLowerCase();
                if (quizFlag == 2) {
                    proceedQuiz();
                } else {
                    proceedLesson();
                }
            }
        }
    }

    private void initQuiz() {

        quizQuestions = getResources().getStringArray(R.array.modelColorQuiz_array);
        quizAnswers = getResources().getStringArray(R.array.answerColorQuiz_array);
        quizCurrent = 0;
        quizLength = quizQuestions.length;
        tutorSpokenText = "What are the following colors called in German?";
        speak(tutorSpokenText);
        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        currentModel = quizQuestions[quizCurrent]+".sfb";
        tutorSpokenText = quizQuestions[quizCurrent];
        speak(tutorSpokenText);
        quizFlag = 2;

    }

    private void proceedQuiz() {
        if (verifyQuiz()) {
            if (quizCurrent < quizLength-1) {
                quizCurrent += 1;
                currentModel = quizQuestions[quizCurrent]+".sfb";
                tutorSpokenText = quizQuestions[quizCurrent];
//                textToSpeech.setLanguage(Locale.GERMAN);
                textToSpeech.setLanguage(new Locale("nl_NL"));
                speak(tutorSpokenText);
            } else {
                tutorSpokenText = "Great! You've successfully completed the quiz, congratulations!";
                speak(tutorSpokenText);
                quizFlag = 3;
                quiz = 1;

                // Update shared prefs
                SharedPreferences sharedPreferences = this.getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt(sp_lesson_color_quiz, 1);
                editor.apply();

                // Update database
                reference.child(username).child("quizColors").setValue(1);

                callDialog();
            }
        } else {
            speak("Wrong answer, try again.");
            Toast.makeText(this, "Try again!", Toast.LENGTH_SHORT).show();
        }
    }

    private void callDialog() {
        ModuleCompletedDialog moduleCompletedDialog = new ModuleCompletedDialog();
        moduleCompletedDialog.show(getSupportFragmentManager(), "Module completed");
    }

    private boolean verifyQuiz() {
        if (!userSpokenText.equals("")) {
            String []answerOptions = quizAnswers[quizCurrent].split("\\|");
            if (Arrays.asList(answerOptions).contains(userSpokenText)){
                textToSpeech.setLanguage(Locale.ENGLISH);
                speak("Correct answer!");
                Toast.makeText(this, "Correct answer!", Toast.LENGTH_LONG).show();
                try {
                    TimeUnit.SECONDS.sleep(2);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return true;
            }
            return false;
        }
        return false;
    }


    // TextToSpeech function
    public void speak(String s){
        textToSpeech.speak(s,TextToSpeech.QUEUE_FLUSH,null, null);
    }

    @Override
    protected void onDestroy() {
        // Shutdown TextToSpeech to release resources
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
            textToSpeech = null;  // Set to null to avoid further interactions
        }
        super.onDestroy();
    }

}
