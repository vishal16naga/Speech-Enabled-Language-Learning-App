package com.example.talkar;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
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
import com.google.firebase.database.annotations.Nullable;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class LessonAlphabets extends AppCompatActivity {

    public String userSpokenText = "";
    public String tutorSpokenText = "";
    private ArFragment arFragment;
    private TextToSpeech textToSpeech;
    Button replay, next;
    int currentAlphabetCount, alphabetsCompleted;
    String currentModel;
    String[] alphabetModels, currentAlphabetOptions;

    String[] quizQuestions, quizAnswers;
    int quiz, quizCurrent, quizLength, quizFlag=0;
    public static final String sp_lesson_alphabet_quiz = "AlphabetsQuiz";

    private static final String SHARED_PREFS = "sharedPrefs";
    public static final String sp_lesson_alphabet = "AlphabetsCompleted";
    private static final String sp_username = "Username";

    private String username;
    private DatabaseReference reference;
    TextView textresult;

    private SpeechRecognizer speechRecognizer;
    private Intent speechRecognizerIntent;

    // Alphabet map
    private static final Map<String, Character> ALPHABET_MAP;
    static {
        ALPHABET_MAP = new HashMap<>();
        ALPHABET_MAP.put("A", 'A');
        ALPHABET_MAP.put("B", 'B');
        ALPHABET_MAP.put("C", 'C');
        ALPHABET_MAP.put("D", 'D');
        ALPHABET_MAP.put("E", 'E');
        ALPHABET_MAP.put("F", 'F');
        ALPHABET_MAP.put("G", 'G');
        ALPHABET_MAP.put("H", 'H');
        ALPHABET_MAP.put("I", 'I');
        ALPHABET_MAP.put("J", 'J');
        ALPHABET_MAP.put("K", 'K');
        ALPHABET_MAP.put("L", 'L');
        ALPHABET_MAP.put("M", 'M');
        ALPHABET_MAP.put("N", 'N');
        ALPHABET_MAP.put("O", 'O');
        ALPHABET_MAP.put("P", 'P');
        ALPHABET_MAP.put("Q", 'Q');
        ALPHABET_MAP.put("R", 'R');
        ALPHABET_MAP.put("S", 'S');
        ALPHABET_MAP.put("T", 'T');
        ALPHABET_MAP.put("U", 'U');
        ALPHABET_MAP.put("V", 'V');
        ALPHABET_MAP.put("W", 'W');
        ALPHABET_MAP.put("X", 'X');
        ALPHABET_MAP.put("Y", 'Y');
        ALPHABET_MAP.put("Z", 'Z');
    }

    @Override
    protected void onResume() {
        super.onResume();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson_alphabets);
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {}

            @Override
            public void onBeginningOfSpeech() {}

            @Override
            public void onRmsChanged(float rmsdB) {}

            @Override
            public void onBufferReceived(byte[] buffer) {}

            @Override
            public void onEndOfSpeech() {}

            @Override
            public void onError(int error) {
                Toast.makeText(LessonAlphabets.this, "Error recognizing speech", Toast.LENGTH_SHORT).show();
                Log.e("voice","error "+error);
            }

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null) {
                    String recognizedText = matches.get(0);
                    handleRecognizedText(recognizedText);
                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {}

            @Override
            public void onEvent(int eventType, Bundle params) {}

        });
        speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);





    reference = FirebaseDatabase.getInstance().getReference("lessons");

        SharedPreferences sharedPreferences = this.getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        alphabetsCompleted = sharedPreferences.getInt(sp_lesson_alphabet, 0);
        username = sharedPreferences.getString(sp_username, "");
        quiz = sharedPreferences.getInt(sp_lesson_alphabet_quiz, 0);

        alphabetModels = getResources().getStringArray(R.array.modelAlphabet_array);

        // Check if quiz for module is completed
        if (quiz == 1) {
            callDialog();
        } else {
            initLesson(alphabetsCompleted);
        }

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Alphabets");
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
                textToSpeech.setLanguage(new Locale("nl_NL"));
                speak(tutorSpokenText);
            }
        });

        replay = findViewById(R.id.replay);
        replay.setOnClickListener(v ->{
            startListening();
            textToSpeech.setLanguage(Locale.GERMAN);
            textToSpeech.setLanguage(new Locale("nl_NL"));
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
    private void startListening() {
        speechRecognizer.startListening(speechRecognizerIntent);
    }
    private void handleRecognizedText(String recognizedText) {
        StringBuilder alphabetBuilder = new StringBuilder();

        // Split the recognized text into words
        String[] words = recognizedText.split(" ");
        for (String word : words) {
            Character alphabet = ALPHABET_MAP.get(word.toUpperCase());
            if (alphabet != null) {
                alphabetBuilder.append(alphabet);
            }
        }

        String recognizedAlphabets = alphabetBuilder.toString();
        Toast.makeText(this, "Recognized Alphabets: " + recognizedAlphabets, Toast.LENGTH_LONG).show();
    }

    private void initLesson(int alphabetsCompleted) {
        if (alphabetsCompleted < alphabetModels.length) {
            currentAlphabetCount = alphabetsCompleted;
            setCurrentAlphabetOptions(currentAlphabetCount);
            currentModel = alphabetModels[alphabetsCompleted]+".sfb";
            tutorSpokenText = alphabetModels[alphabetsCompleted];
        } else {
            tutorSpokenText = "Congratulation, you've completed the Alphabets Lesson. Time for a small quiz! Tap on Next to proceed.";
            quizFlag = 1;
        }
    }

    private void setCurrentAlphabetOptions(int currentAlphabetCount) {
        switch (currentAlphabetCount){
            case 0:
                currentAlphabetOptions = getResources().getStringArray(R.array.answerAlphabet_0);
                break;
            case 1:
                currentAlphabetOptions = getResources().getStringArray(R.array.answerAlphabet_1);
                break;
            case 2:
                currentAlphabetOptions = getResources().getStringArray(R.array.answerAlphabet_2);
                break;
            case 3:
                currentAlphabetOptions = getResources().getStringArray(R.array.answerAlphabet_3);
                break;
            case 4:
                currentAlphabetOptions = getResources().getStringArray(R.array.answerAlphabet_4);
                break;
            case 5:
                currentAlphabetOptions = getResources().getStringArray(R.array.answerAlphabet_5);
                break;
            case 6:
                currentAlphabetOptions = getResources().getStringArray(R.array.answerAlphabet_6);
                break;
            case 7:
                currentAlphabetOptions = getResources().getStringArray(R.array.answerAlphabet_7);
                break;
            case 8:
                currentAlphabetOptions = getResources().getStringArray(R.array.answerAlphabet_8);
                break;
            case 9:
                currentAlphabetOptions = getResources().getStringArray(R.array.answerAlphabet_9);
                break;
            case 10:
                currentAlphabetOptions = getResources().getStringArray(R.array.answerAlphabet_10);
                break;
            case 11:
                currentAlphabetOptions = getResources().getStringArray(R.array.answerAlphabet_11);
                break;
            case 12:
                currentAlphabetOptions = getResources().getStringArray(R.array.answerAlphabet_12);
                break;
            case 13:
                currentAlphabetOptions = getResources().getStringArray(R.array.answerAlphabet_13);
                break;
            case 14:
                currentAlphabetOptions = getResources().getStringArray(R.array.answerAlphabet_14);
                break;
            case 15:
                currentAlphabetOptions = getResources().getStringArray(R.array.answerAlphabet_15);
                break;
            case 16:
                currentAlphabetOptions = getResources().getStringArray(R.array.answerAlphabet_16);
                break;
            case 17:
                currentAlphabetOptions = getResources().getStringArray(R.array.answerAlphabet_17);
                break;
            case 18:
                currentAlphabetOptions = getResources().getStringArray(R.array.answerAlphabet_18);
                break;
            case 19:
                currentAlphabetOptions = getResources().getStringArray(R.array.answerAlphabet_19);
                break;
            case 20:
                currentAlphabetOptions = getResources().getStringArray(R.array.answerAlphabet_20);
                break;
            case 21:
                currentAlphabetOptions = getResources().getStringArray(R.array.answerAlphabet_21);
                break;
            case 22:
                currentAlphabetOptions = getResources().getStringArray(R.array.answerAlphabet_22);
                break;
            case 23:
                currentAlphabetOptions = getResources().getStringArray(R.array.answerAlphabet_23);
                break;
            case 24:
                currentAlphabetOptions = getResources().getStringArray(R.array.answerAlphabet_24);
                break;
            case 25:
                currentAlphabetOptions = getResources().getStringArray(R.array.answerAlphabet_25);
                break;
            case 29:
                currentAlphabetOptions = getResources().getStringArray(R.array.answerAlphabet_29);
                break;
        }
    }

    private void proceedLesson() {
        if (verifySpeech()){
            if (currentAlphabetCount != alphabetModels.length-1){
                currentAlphabetCount += 1;
                setCurrentAlphabetOptions(currentAlphabetCount);
                currentModel = alphabetModels[currentAlphabetCount]+".sfb";
                tutorSpokenText = alphabetModels[currentAlphabetCount];
                textToSpeech.setLanguage(Locale.GERMAN);
                textToSpeech.setLanguage(new Locale("nl_NL"));
                speak(tutorSpokenText);
                updateSharedPrefs(currentAlphabetCount);
                updateDatabase(currentAlphabetCount);
            }
            else {
                updateSharedPrefs(currentAlphabetCount+1);
                updateDatabase(currentAlphabetCount+1);
                tutorSpokenText = "Congratulation, you've completed the Alphabets Lesson. Time for a small quiz! Tap on Next to proceed.";
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

    private void updateDatabase(int currentAlphabetCount) {
        reference.child(username).child("alphabets").setValue(currentAlphabetCount);
    }

    private void updateSharedPrefs(int currentAlphabetCount) {
        SharedPreferences sharedPreferences = this.getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(sp_lesson_alphabet, currentAlphabetCount);
        editor.apply();
    }

    private boolean verifySpeech() {
        if (!userSpokenText.equals("")) {
            if (Arrays.asList(currentAlphabetOptions).contains(userSpokenText)){
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
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "de-DE");
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "nl_NL");

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
                textresult.setText(result.get(0));
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

        quizQuestions = getResources().getStringArray(R.array.modelAlphabetQuiz_array);
        quizAnswers = getResources().getStringArray(R.array.answerAlphabetQuiz_array);
        quizCurrent = 0;
        quizLength = quizQuestions.length;
        tutorSpokenText = "What are the following alphabets called in German?";
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
                textToSpeech.setLanguage(Locale.GERMAN);
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
                editor.putInt(sp_lesson_alphabet_quiz, 1);
                editor.apply();

                // Update database
                reference.child(username).child("quizAlphabets").setValue(1);

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
