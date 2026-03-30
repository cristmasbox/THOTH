package com.blueapps.thoth.cache;

import static com.blueapps.thoth.RenderClass.convertToXmlString;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.LruCache;

import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModel;

import com.blueapps.maat.BoundCalculation;
import com.blueapps.maat.ValuePair;
import com.blueapps.signprovider.SignProvider;
import com.blueapps.thoth.R;
import com.blueapps.thoth.RenderClass;
import com.blueapps.thoth.ThothListener;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

public class CacheStorage extends ViewModel {

    private RenderClass renderClass;
    private Context context;

    private int signCounter = 1;
    private ThothListener listener;

    // Cached Values
    private String GlyphXContent = "";
    private Document GlyphXDocument;
    private BoundCalculation boundCalculation;
    private ArrayList<String> ids;
    private LruCache<String, Drawable> drawables;
    private ArrayList<ValuePair<Float, Float>> dimensions;
    private ArrayList<Rect> bounds;


    public void setParams(Context context, RenderClass renderClass, ThothListener listener){
        this.renderClass = renderClass;
        this.context = context;
        this.listener = listener;
    }

    public void setGlyphXContent(String glyphXContent) {
        GlyphXContent = glyphXContent;
    }

    public String getGlyphXContent() {
        return GlyphXContent;
    }

    public Document getGlyphXDocument() {
        if (GlyphXDocument == null){
            try {
                this.GlyphXDocument = renderClass.renderGlyphXDocument();
            } catch (ParserConfigurationException | IOException | SAXException e) {
                e.printStackTrace();
            }
        }
        return GlyphXDocument;
    }

    public BoundCalculation getBoundCalculation() {
        if (boundCalculation == null){
            this.boundCalculation = renderClass.renderBoundCalculation();
        }
        return boundCalculation;
    }

    public ArrayList<String> getIds() {
        if (ids == null){
            this.ids = renderClass.renderIds();
        }
        if (ids.isEmpty()){
            this.ids = renderClass.renderIds();
        }
        return ids;
    }

    public Drawable getDrawable(String id, int max) {
        if (drawables == null) {
            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            int availableMemory = am.getMemoryClass() *1024 *1024;

            this.drawables = new LruCache<>(availableMemory / 8){
                @Override
                protected int sizeOf(String key, Drawable value){
                    return 1;
                }
            };
        }
        Drawable drawable = drawables.get(id);
        if (drawable == null){

            progress(max);
            try {
                SignProvider signProvider = new SignProvider(context);
                drawable = signProvider.getSign(id);
                drawables.put(id, drawable);
            } catch (IOException | XmlPullParserException e) {
                drawables.put(id, ContextCompat.getDrawable(context, R.drawable.not_found_sign));
                e.printStackTrace();
            }
            signCounter++;
        }
        return drawable;
    }

    public ArrayList<ValuePair<Float, Float>> getDimensions() {
        if (dimensions == null){
            this.dimensions = renderClass.renderDimensions();
        }
        if (dimensions.isEmpty()){
            this.dimensions = renderClass.renderDimensions();
        }
        return dimensions;
    }

    public ArrayList<Rect> getBounds() {
        if (bounds == null){
            this.bounds = renderClass.renderBounds();
        }
        if (bounds.isEmpty()){
            this.bounds = renderClass.renderBounds();
        }
        return bounds;
    }

    public void refreshCache(){
        this.GlyphXDocument = null;
        this.boundCalculation = null;
        this.ids = null;
        this.dimensions = null;
        this.bounds = null;
    }

    public void clearLayoutCache(){
        this.boundCalculation = null;
        this.bounds = null;
        this.dimensions = null;
    }

    public void clearDirectionCache(){
        this.boundCalculation = null;
        this.bounds = null;
        this.ids = null;
        this.dimensions = null;
    }

    public void setGlyphXDocument(Document glyphXDocument) {
        GlyphXDocument = glyphXDocument;
        try {
            GlyphXContent = convertToXmlString(glyphXDocument);
        } catch (TransformerException e) {
            e.printStackTrace();
        }
    }

    private void progress(int max){

        if (listener != null) {
            float progress = (float) signCounter / max;

            progress *= 100;

            listener.OnRender(progress, signCounter, max);
        }

    }
}
