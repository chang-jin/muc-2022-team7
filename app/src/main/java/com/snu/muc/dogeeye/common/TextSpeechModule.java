package com.snu.muc.dogeeye.common;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TextSpeechModule extends UtteranceProgressListener implements TextToSpeech.OnInitListener {
    private static final Logger log = new Logger();

    private TextToSpeech textToSpeech;
    private Context mContext;
    private Locale mLocale;
    private SpeechRecognizer speechToText;
    private RecognitionListener listener;
    public Intent sttIntent;
    public static String result_text = "default result";

    public int current_mode = 0;
    private int total_modes = 10;
    public List<Map<String, Integer>> action_maps;

    private Boolean over = true;

    private TextSpeechModule() {
    }

    @SuppressLint("StaticFieldLeak")
    private static class SingleTonHolder {
        private static final TextSpeechModule instance = new TextSpeechModule();
    }

    public static TextSpeechModule getInstance() {
        return SingleTonHolder.instance;
    }

    public void init(Context context, Locale locale) {
        mContext = context;
        mLocale = locale;

        textToSpeech = new TextToSpeech(context, this);
        textToSpeech.setOnUtteranceProgressListener(this);
        initSTTModule();
    }

    private void initSTTModule() {
        speechToText = SpeechRecognizer.createSpeechRecognizer(mContext);

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


        listener = initSTTListener();
        speechToText.setRecognitionListener(listener);
    }

    private RecognitionListener initSTTListener() {
        return new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {
                log.e("onReadyForSpeech");
            }

            @Override
            public void onBeginningOfSpeech() {
                over = false;
                log.e("onBeginningOfSpeech");
            }

            @Override
            public void onRmsChanged(float v) {
            }

            @Override
            public void onBufferReceived(byte[] bytes) {
            }

            @Override
            public void onEndOfSpeech() {
                log.e( "onEndOfSpeech");
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
                log.e(message);
                over = true;
            }

            @Override
            public void onResults(Bundle bundle) {
                log.e("onResults");
                ArrayList<String> data = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                result_text = data.get(0);
                over = true;
                log.e("over");
            }

            @Override
            public void onPartialResults(Bundle bundle) {

            }

            @Override
            public void onEvent(int i, Bundle bundle) {
                log.e("onEvent");
            }
        };
    }

    public String listen() {
        speechToText.startListening(sttIntent);
        speechToText.stopListening();
        log.e("new text: " + result_text);
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
        log.e("new text: " + result_text);

        return result_text;
    }

    public int listenAndGiveCode(Context context, int current_mode) {
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

    public void textToSpeech(String text) {
        if (textToSpeech != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                String myUtteranceID = "myUtteranceID";
                textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, myUtteranceID);
            } else {
                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "myUtteranceID");
                textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, hashMap);
            }
        }
    }

    public void stopTTS() {
        textToSpeech.stop();
    }

    public void shutdownTTS() {
        textToSpeech.shutdown();
    }

    public boolean isSpeaking() {
        return textToSpeech.isSpeaking();
    }

    @Override
    public void onStart(String utteranceId) {
        log.d("onStart / utteranceID = " + utteranceId);
    }

    @Override
    public void onDone(String utteranceId) {
        log.d("onDone / utteranceID = " + utteranceId);
    }

    @Override
    public void onError(String utteranceId) {
        log.d("onError / utteranceID = " + utteranceId);
    }

    @Override
    public void onInit(int status) {
        if (status != TextToSpeech.ERROR) {
            log.e("TTS init");
            textToSpeech.setLanguage(mLocale);
        } else {
            log.e("TTS init error");
        }
    }
}
