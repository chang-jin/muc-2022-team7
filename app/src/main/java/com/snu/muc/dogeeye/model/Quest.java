package com.snu.muc.dogeeye.model;

public class Quest {
    private String mGmsId;
    private String mTitle;
    private String mDescription;

    public Quest(String id, String title, String description) {
        mGmsId = id;
        mTitle = title;
        mDescription = description;
    }

    public String getId() {
        return mGmsId;
    }

    public void setId(String mGmsId) {
        this.mGmsId = mGmsId;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String mDescription) {
        this.mDescription = mDescription;
    }
}
