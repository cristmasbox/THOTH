package com.blueapps.thoth;

import static com.blueapps.thoth.ThothView.FILENAME_DRAWABLE_IDS;
import static com.blueapps.thoth.ThothView.FILENAME_DRAWABLE_PATHS;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.util.TypedValue;

import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModel;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import com.blueapps.maat.BoundCalculation;
import com.blueapps.maat.BoundProperty;
import com.blueapps.maat.ValuePair;
import com.blueapps.thoth.cache.CacheStorage;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParserException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Objects;
import java.util.regex.PatternSyntaxException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class RenderClass extends ViewModel {

    // Constants
    private static final String TAG = "RenderClass";

    private ThothView thothView;
    private CacheStorage storage;

    public void setParams(ThothView thothView){

        this.thothView = thothView;
        this.storage = thothView.getStorage();

    }

    public Document renderGlyphXDocument() throws ParserConfigurationException, IOException, SAXException {
        return convertToXmlDocument(storage.getGlyphXContent());
    }

    public BoundCalculation renderBoundCalculation(){
        return new BoundCalculation(storage.getGlyphXDocument());
    }

    public ArrayList<String> renderIds(){
        return storage.getBoundCalculation().getIds();
    }

    public ArrayList<ValuePair<Float, Float>> renderDimensions(){
        return getDimensions(storage.getIds());
    }

    public ArrayList<Rect> renderBounds(){
        BoundProperty property = new BoundProperty(0, 0, thothView.getTextSize(),
                thothView.getVerticalOrientation(), thothView.getWritingDirection(), thothView.getWritingLayout());
        ArrayList<ValuePair<Float, Float>> dimensions = storage.getDimensions();

        return storage.getBoundCalculation().getBounds(dimensions, property);
    }

    private static Document convertToXmlDocument(String xml) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new InputSource(new StringReader(xml)));
    }

    private ArrayList<ValuePair<Float, Float>> getDimensions(ArrayList<String> ids) {
        ArrayList<ValuePair<Float, Float>> dimensions = new ArrayList<>();
        for (String id : ids){
            Drawable drawable = storage.getDrawable(id);
            dimensions.add(new ValuePair<>(getDrawableWidth(drawable), getDrawableHeight(drawable)));
        }
        return dimensions;
    }

    public Drawable getSignDrawable(String Id) throws IOException, XmlPullParserException {

        Drawable signDrawable = null;

        // get the filename of the drawable
        String drawableFileName;
        try {
            drawableFileName = getDrawableFileName(Id);

            Drawable drawable;
            try {
                drawable = getXMLDrawable(drawableFileName);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                drawable = ContextCompat.getDrawable(thothView.getContext(), R.drawable.not_found_sign);
            }

            if (drawableFileName.isEmpty()) {
                drawable = ContextCompat.getDrawable(thothView.getContext(), R.drawable.not_found_sign);
            }

            signDrawable = drawable;

        } catch (CsvValidationException e) {
            e.printStackTrace();
        }

        return signDrawable;

    }

    private float getDrawableWidth(Drawable drawable){
        float width = drawable.getIntrinsicWidth();
        float density = thothView.getContext().getResources().getDisplayMetrics().density;

        width = width / density;

        Log.i(TAG, "Density: " + density);

        return width;
    }

    private float getDrawableHeight(Drawable drawable){
        float height = drawable.getIntrinsicHeight();
        float density = thothView.getContext().getResources().getDisplayMetrics().density;

        height = height / density;

        Log.i(TAG, "Density: " + density);

        return height;
    }

    private String getDrawableFileName(String id) throws IOException, CsvValidationException {
        Log.i(TAG, "Sign: id=" + id);

        CSVReader reader = new CSVReader(new InputStreamReader(thothView.getContext().getAssets().open(FILENAME_DRAWABLE_IDS)));
        String alternativeId = search(reader, id, false);
        String path;

        CSVReader PathReader = new CSVReader(new InputStreamReader(thothView.getContext().getAssets().open(FILENAME_DRAWABLE_PATHS)));

        if (alternativeId.isEmpty()){

            path = search(PathReader, id, true);

        } else {

            path = search(PathReader, alternativeId, true);
            Log.d(TAG, alternativeId);

        }

        if (Objects.equals(path, "")){
            return "";
        } else {
            path = path + ".xml";
            return path;
        }
    }

    private Drawable getXMLDrawable(String fileName) throws IOException, XmlPullParserException {
        XmlResourceParser parser = thothView.getContext().getAssets().openXmlResourceParser(fileName);
        return VectorDrawableCompat.createFromXml(thothView.getContext().getResources(), parser);
    }

    private static String search(CSVReader reader, String s, boolean fullPath) throws IOException, CsvValidationException {
        try {
            String[] nextLine;
            while ((nextLine = reader.readNext()) != null) {
                for (String field : nextLine) {
                    String[] row = field.split(";");
                    if (row[0].matches(s)) {
                        Log.i(TAG, "Found: " + field);
                        if (row.length > 1) {
                            Log.i(TAG, "Sign: " + row[1]);
                            if (fullPath) {
                                return "assets/Unicode/" + row[1];
                            } else {
                                return row[1];
                            }
                        }
                    }
                }
            }
        } catch (PatternSyntaxException e){
            e.printStackTrace();
        }
        return "";
    }

}
