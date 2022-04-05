package com.snu.muc.dogeeye.ui.photo;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class PhotoViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public PhotoViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is photo fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
    public void setText(String string) {
        mText.setValue(string);
    }

    public void postText(String string) {
        mText.postValue(string);
    }
}