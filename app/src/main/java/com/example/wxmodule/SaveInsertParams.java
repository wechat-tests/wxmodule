package com.example.wxmodule;

import android.content.ContentValues;

public class SaveInsertParams {
    String paramString1;
    String paramString2;
    ContentValues paramContentValues;
    String content;

    public String getParamString1() {
        return paramString1;
    }

    public void setParamString1(String paramString1) {
        this.paramString1 = paramString1;
    }

    public String getParamString2() {
        return paramString2;
    }

    public void setParamString2(String paramString2) {
        this.paramString2 = paramString2;
    }

    public ContentValues getParamContentValues() {
        return paramContentValues;
    }

    public void setParamContentValues(ContentValues paramContentValues) {
        this.paramContentValues = paramContentValues;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
