package com.snu.muc.dogeeye;
import android.content.Context;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import java.util.HashMap;
import java.util.Locale;

public class ttsModule extends UtteranceProgressListener implements TextToSpeech.OnInitListener{

    private String TAG = "TTS";
    private TextToSpeech textToSpeech;
    private Locale locale;
    private Context thisContext;
    private static Context mContext;


    public ttsModule(Context context, Locale locale) {
        this.locale = locale;
//        installVoiceData();
        Log.e(TAG, "module made");
        textToSpeech = new TextToSpeech(context, this);
        textToSpeech.setOnUtteranceProgressListener(this);
        thisContext = context;

    }


    public void speak(String text) {
        if(textToSpeech != null) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                String myUtteranceID = "myUtteranceID";
                textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, myUtteranceID);
            }
            else {
                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "myUtteranceID");
                textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, hashMap);
            }
        }
    }

    public void stop() {
        textToSpeech.stop();
    }

    public void shutdown() {
        textToSpeech.shutdown();
    }

    public boolean isSpeaking() {
        return textToSpeech.isSpeaking();
    }

    @Override
    public void onStart(String utteranceId) {
        Log.d(TAG, "onStart / utteranceID = " + utteranceId);
    }

    @Override
    public void onDone(String utteranceId) {
        Log.d(TAG, "onDone / utteranceID = " + utteranceId);
    }

    @Override
    public void onError(String utteranceId) {
        Log.d(TAG, "onError / utteranceID = " + utteranceId);
    }

    @Override
    public void onInit(int status) {
        if(status != TextToSpeech.ERROR){
            textToSpeech.setLanguage(locale);
            Log.e(TAG,"TTS init");

        }
        else{
            Log.e(TAG,"TTS init error");
        }

    }

}