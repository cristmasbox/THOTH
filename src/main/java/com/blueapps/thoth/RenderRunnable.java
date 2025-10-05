package com.blueapps.thoth;

import android.os.Handler;
import android.os.Looper;

import com.blueapps.thoth.cache.CacheStorage;

import org.xml.sax.SAXException;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

public class RenderRunnable implements Runnable {

    private ThothView thothView;
    private CacheStorage storage;
    private RenderClass renderClass;

    public RenderRunnable(ThothView thothView, RenderClass renderClass) {
        this.thothView = thothView;
        this.storage = thothView.getStorage();
        this.renderClass = renderClass;
    }

    @Override
    public void run() {

        try {
            renderClass.renderGlyphXDocument();

            storage.getBounds();

            thothView.width = (int) storage.getBoundCalculation().getXCursor();
            thothView.height = (int) storage.getBoundCalculation().getYCursor();

            thothView.unlockDrawing = true;

            new Handler(Looper.getMainLooper()).post(() -> {
                thothView.requestLayout();
                thothView.invalidate();
            });

        } catch (ParserConfigurationException | IOException | SAXException e){
            e.printStackTrace();
        }

    }
}
