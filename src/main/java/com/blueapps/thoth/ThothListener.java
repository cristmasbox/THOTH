package com.blueapps.thoth;

public interface ThothListener {

    void OnRenderStart();

    void OnRender(float progress, int currentSign, int signCount);

    void OnRenderCancel();

    void OnRenderFinished();

}
