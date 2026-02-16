package com.blueapps.thoth;

import android.annotation.SuppressLint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.Log;

import androidx.lifecycle.ViewModel;

import com.blueapps.maat.BoundCalculation;
import com.blueapps.maat.BoundProperty;
import com.blueapps.maat.ValuePair;
import com.blueapps.thoth.cache.CacheStorage;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class RenderClass extends ViewModel {

    // Constants
    private static final String TAG = "RenderClass";

    @SuppressLint("StaticFieldLeak")
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
        return storage.getBoundCalculation().getIds(thothView.getWritingLayout() == BoundProperty.WRITING_LAYOUT_LINES,
                                                    thothView.getWritingDirection() == BoundProperty.WRITING_DIRECTION_RTL);
    }

    public ArrayList<ValuePair<Float, Float>> renderDimensions(){
        return getDimensions(storage.getIds());
    }

    public ArrayList<Rect> renderBounds(){
        BoundProperty property = new BoundProperty(0, 0, thothView.getTextSize(),
                thothView.getVerticalOrientation(), thothView.getWritingDirection(), thothView.getWritingLayout(),
                thothView.isDrawLines(), thothView.getLineThickness(), thothView.getPagePaddingLeft(), thothView.getPagePaddingTop(), thothView.getPagePaddingRight(),
                thothView.getPagePaddingBottom(), thothView.getSignPadding(), thothView.getLayoutSignPadding(), thothView.getInterLinePadding());
        ArrayList<ValuePair<Float, Float>> dimensions = storage.getDimensions();

        return storage.getBoundCalculation().getBounds(dimensions, property);
    }

    public static Document convertToXmlDocument(String xml) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new InputSource(new StringReader(xml)));
    }

    public static String convertToXmlString(Document xml) throws TransformerException {
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();

            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(OutputKeys.INDENT, "no");

            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);

            DOMSource source = new DOMSource(xml);
            transformer.transform(source, result);

            return writer.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private ArrayList<ValuePair<Float, Float>> getDimensions(ArrayList<String> ids) {
        ArrayList<ValuePair<Float, Float>> dimensions = new ArrayList<>();
        for (String id : ids){
            Drawable drawable = storage.getDrawable(id);
            dimensions.add(new ValuePair<>(getDrawableWidth(drawable), getDrawableHeight(drawable)));
        }
        return dimensions;
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

}
