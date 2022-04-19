
package com.snu.muc.dogeeye;


import android.content.Context;
import android.content.Intent;

import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class sttModule {

    private String TAG = "STT";
    private SpeechRecognizer speechToText;
    private RecognitionListener listener;
    private Locale locale;
    private Context thisContext;
    public Intent sttIntent;
    public static String result_text = "default result";

    public int current_mode = 0;
    private int total_modes = 10;
    public List<Map<String, Integer>> action_maps;

    private Boolean over = true;

    public sttModule(Context context, Locale locale) {
        this.locale = locale;
        thisContext = context;
        speechToText = SpeechRecognizer.createSpeechRecognizer(context);

        sttIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        sttIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
        sttIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);

        action_maps = new ArrayList<>();
        // hard_coded for now
        Map<String, Integer> home_actions = new HashMap<String, Integer>();

        home_actions.put("photo", 1);
        home_actions.put("quest", 2);
        home_actions.put("record", 3);
        action_maps.add(home_actions);

        listener = new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {
                Log.e(TAG, "onReadyForSpeech");
            }

            @Override
            public void onBeginningOfSpeech() {
                over = false;
                Log.e(TAG, "onBeginningOfSpeech");
            }

            @Override
            public void onRmsChanged(float v) {

            }

            @Override
            public void onBufferReceived(byte[] bytes) {

            }

            @Override
            public void onEndOfSpeech() {
                Log.e(TAG, "onEndOfSpeech");

            }

            @Override
            public void onError(int i) {
                String message;
                switch (i) {
                    case SpeechRecognizer.ERROR_AUDIO:
                        message = "ERROR_AUDIO";
                        break;
                    case SpeechRecognizer.ERROR_CLIENT:
                        message = "ERROR_CLIENT";
                        break;
                    case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                        message = "ERROR_INSUFFICIENT_PERMISSIONS";
                        break;
                    case SpeechRecognizer.ERROR_NETWORK:
                        message = "ERROR_NETWORK";
                        break;
                    case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                        message = "ERROR_NETWORK_TIMEOUT";
                        break;
                    case SpeechRecognizer.ERROR_NO_MATCH:
                        message = "ERROR_NO_MATCH";
                        break;
                    case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                        message = "ERROR_RECOGNIZER_BUSY";
                        break;
                    case SpeechRecognizer.ERROR_SERVER:
                        message = "ERROR_SERVER";
                        break;
                    case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                        message = "ERROR_SPEECH_TIMEOUT";
                        break;
                    default:
                        message = "UNKNOWN ERROR";
                        break;
                }

                Log.e(TAG, message);
                over = true;
            }

            @Override
            public void onResults(Bundle bundle) {
                Log.e(TAG, "onResults");
                ArrayList<String> data = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                result_text = data.get(0);
                over = true;
                Log.e(TAG, "over");
            }

            @Override
            public void onPartialResults(Bundle bundle) {

            }

            @Override
            public void onEvent(int i, Bundle bundle) {
                Log.e(TAG, "onEvent");

            }
        };
        speechToText.setRecognitionListener(listener);
        Log.e(TAG, "module made");

    }

    public String listen() {
        speechToText.startListening(sttIntent);
        speechToText.stopListening();
        Log.e(TAG, "new text: " + result_text);
        return result_text;
    }

    public String listen2(Context context) {
        speechToText = SpeechRecognizer.createSpeechRecognizer(context);
        speechToText.setRecognitionListener(listener);
        speechToText.startListening(sttIntent);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {

            }
        }, 3000);
        Log.e(TAG, "new text: " + result_text);

        return result_text;
    }

    public int listen_and_give_code(Context context, int current_mode) {
        speechToText = SpeechRecognizer.createSpeechRecognizer(context);
        speechToText.setRecognitionListener(listener);
        speechToText.startListening(sttIntent);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {

            }
        }, 3000);

        int result_code;
        result_code = -1;

        for (Map.Entry<String, Integer> entry : action_maps.get(current_mode).entrySet()) {
            if(result_text.contains(entry.getKey())){
                result_code = entry.getValue();
            }
        }

        return result_code;
    }

}