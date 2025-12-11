package com.blueapps.thoth;

import static com.blueapps.maat.BoundProperty.WRITING_DIRECTION_RTL;
import static com.blueapps.thoth.RenderClass.convertToXmlDocument;
import static com.blueapps.thoth.RenderClass.convertToXmlString;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.lifecycle.ViewTreeViewModelStoreOwner;

import com.blueapps.glpyhconverter.GlyphConverter;
import com.blueapps.glpyhconverter.toglyphx.exceptions.MdCParseException;
import com.blueapps.maat.BoundProperty;
import com.blueapps.thoth.cache.CacheStorage;

import org.w3c.dom.Document;

import javax.xml.transform.TransformerException;

public class ThothView extends View {

    // Constants
    private static final String TAG = "ThothView";
    // Database Data
    public static final String FILENAME_DRAWABLE_IDS = "Databases/Drawable_Ids.csv";
    public static final String FILENAME_DRAWABLE_PATHS = "Databases/Drawable_Paths.csv";

    protected int width = 50;
    protected int height = 200;

    protected int widthMeasureSpec;
    protected int heightMeasureSpec;

    protected boolean unlockDrawing = false;

    private RenderRunnable renderRunnable;
    private TaskScheduler taskScheduler;

    private CacheStorage storage;
    private RenderClass renderClass;

    // Paints
    private final Paint textPaint = new Paint();

    // Attributes
    private Document glyphX;
    private String MdC = "r:Z1-n:km-m-t:O49";
    private String altText = "";//TODO
    private boolean showAltText = true;
    private @ColorInt int altTextColor = Color.BLACK;
    private @ColorInt int backgroundColor = Color.TRANSPARENT;
    private @ColorInt int primarySignColor = Color.BLACK;
    private int textSize = 200;
    private int altTextSize = textSize / 2;
    private int verticalOrientation = BoundProperty.VERTICAL_ORIENTATION_MIDDLE;
    private int writingDirection = BoundProperty.WRITING_DIRECTION_LTR;
    private int writingLayout = BoundProperty.WRITING_LAYOUT_LINES;
    private boolean drawLines = false;
    private float lineThickness = 2f;
    private float pagePaddingLeft = 0f;
    private float pagePaddingTop = 0f;
    private float pagePaddingRight = 0f;
    private float pagePaddingBottom = 0f;
    private float signPadding = 0f;
    private float layoutSignPadding = 0f;
    private float interLinePadding = 25f;

    private boolean testAltText = false;

    public ThothView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.ThothView, defStyleAttr, defStyleRes);
        setAttrs(a);
    }

    public ThothView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.ThothView, defStyleAttr, 0);
        setAttrs(a);
    }

    public ThothView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.ThothView);
        setAttrs(a);
    }

    public ThothView(Context context) {
        super(context);
    }

    private void setAttrs(TypedArray a){
        try {
            String tempMdc = a.getString(R.styleable.ThothView_android_text);
            if (tempMdc != null) {
                this.glyphX = GlyphConverter.convertToGlyphXDocument(tempMdc);
                MdC = tempMdc;
            }
        } catch (MdCParseException e){
            e.printStackTrace();
        }

        altText = a.getString(R.styleable.ThothView_altText);
        altTextSize = a.getDimensionPixelSize(R.styleable.ThothView_altTextSize, height / 2);
        showAltText = a.getBoolean(R.styleable.ThothView_showAltText, true);
        altTextColor = a.getColor(R.styleable.ThothView_altTextColor, Color.BLACK);
        backgroundColor = a.getColor(R.styleable.ThothView_backgroundColor, Color.TRANSPARENT);
        primarySignColor = a.getColor(R.styleable.ThothView_primarySignColor, Color.BLACK);
        textSize = a.getDimensionPixelSize(R.styleable.ThothView_android_textSize, 200);
        verticalOrientation = a.getInteger(R.styleable.ThothView_verticalOrientation, 1);
        writingDirection = a.getInteger(R.styleable.ThothView_writingDirection, 0);
        writingLayout = a.getInteger(R.styleable.ThothView_writingLayout, 0);

        // Create standard document
        glyphX = GlyphConverter.convertToGlyphXDocument(MdC);

        a.recycle();
    }

    @Override
    protected void onAttachedToWindow() {
        // For Android Studio Preview Render
        try {

            super.onAttachedToWindow();

            ViewModelStoreOwner owner = ViewTreeViewModelStoreOwner.get(this);
            if (owner != null && (storage == null || renderClass == null)) {
                storage = new ViewModelProvider(owner).get(CacheStorage.class);
                renderClass = new ViewModelProvider(owner).get(RenderClass.class);
            }

            renderClass.setParams(this);
            storage.setParams(this.getContext(), renderClass);
            setGlyphXText(glyphX);

            taskScheduler = new TaskScheduler();
            renderRunnable = new RenderRunnable(this, renderClass, taskScheduler);
            render();

            textPaint.setTextSize(altTextSize);
            textPaint.setColor(altTextColor);

        } catch (NullPointerException e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        canvas.drawARGB(Color.alpha(backgroundColor),
                Color.red(backgroundColor),
                Color.green(backgroundColor),
                Color.blue(backgroundColor));

        if (unlockDrawing){
            int counter = 0;
            for (String id : storage.getIds()) {
                Drawable drawable = storage.getDrawable(id);
                Rect bound = storage.getBounds().get(counter);
                drawable.setBounds(bound);
                if (writingDirection == WRITING_DIRECTION_RTL){
                    canvas.save();
                    // Flip canvas
                    canvas.scale(-1f, 1f, bound.centerX(), bound.centerY());
                }
                if (drawable instanceof VectorDrawable){
                    VectorDrawable drawable1 = (VectorDrawable) drawable;
                    drawable1.setTint(primarySignColor);
                    drawable1.draw(canvas);
                } else {
                    drawable.draw(canvas);
                }
                if (writingDirection == WRITING_DIRECTION_RTL){
                    canvas.restore();
                }
                counter++;
            }
        } else {
            if (showAltText) {
                canvas.drawText(altText, 0, altTextSize + (float) (height - altTextSize) / 2, textPaint);
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        height = (int) storage.getBoundCalculation().getHeight();
        width = (int) storage.getBoundCalculation().getWidth();

        if (!unlockDrawing && showAltText){
            width = (int) textPaint.measureText(altText);
        }

        this.widthMeasureSpec = widthMeasureSpec;
        this.heightMeasureSpec = heightMeasureSpec;

        int mWidth;
        int mHeight;

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        if (widthMode == MeasureSpec.EXACTLY) {
            mWidth = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            mWidth = Math.min(width, widthSize);
        } else {
            mWidth = width;
        }

        if (heightMode == MeasureSpec.EXACTLY) {
            mHeight = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            mHeight = Math.min(height, heightSize);
        } else {
            mHeight = height;
        }

        setMeasuredDimension(mWidth, mHeight);
    }

    public CacheStorage getStorage(){
        return storage;
    }

    public String getMdCText(){
        return MdC;
    }

    public Document getGlyphXText(){
        return glyphX;
    }

    public String getGlyphXTextString(){
        try {
            return convertToXmlString(glyphX);
        } catch (TransformerException e) {
            e.printStackTrace();
        }
        return "";
    }

    public void setMdCText(String mdc){
        this.MdC = mdc;
        try {
            this.glyphX = GlyphConverter.convertToGlyphXDocument(mdc);
            storage.setGlyphXDocument(glyphX);
            storage.refreshCache();
            render();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void setGlyphXText(String glyphX){
        try {
            this.glyphX = convertToXmlDocument(glyphX);
            this.MdC = GlyphConverter.convertToMdC(glyphX);
            storage.setGlyphXContent(glyphX);
            storage.refreshCache();
            render();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void setGlyphXText(Document glyphXDocument){
        try {
            this.glyphX = glyphXDocument;
            this.MdC = GlyphConverter.convertToMdC(glyphX);
            storage.setGlyphXDocument(glyphXDocument);
            storage.refreshCache();
            render();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public int getTextSize() {
        return textSize;
    }

    public void setTextSize(int textSize){
        this.textSize = textSize;
        storage.clearLayoutCache();
        render();
    }

    public int getVerticalOrientation(){
        return verticalOrientation;
    }

    public void setVerticalOrientation(int verticalOrientation){
        if (verticalOrientation < 3 && verticalOrientation > -1) {
            this.verticalOrientation = verticalOrientation;
            storage.clearLayoutCache();
            render();
        }
    }

    public int getWritingDirection(){
        return writingDirection;
    }

    public void setWritingDirection(int writingDirection){
        if (writingDirection == 0 || writingDirection == 1){
            this.writingDirection = writingDirection;
            storage.clearDirectionCache();
            render();
        }
    }

    public int getWritingLayout(){
        return writingLayout;
    }

    public void setWritingLayout(int writingLayout){
        if (writingLayout == 0 || writingLayout == 1){
            this.writingLayout = writingLayout;
            storage.clearLayoutCache();
            render();
        }
    }

    public @ColorInt int getPrimarySignColor(){
        return primarySignColor;
    }

    public void setPrimarySignColor(@ColorInt int color){
        this.primarySignColor = color;
        this.invalidate();
    }

    public @ColorInt int getBackgroundColor(){
        return backgroundColor;
    }

    public void setBackgroundColor(@ColorInt int color){
        this.backgroundColor = color;
        this.invalidate();
    }

    public @ColorInt int getAltTextColor(){
        return altTextColor;
    }

    public void setAltTextColor(@ColorInt int color){
        this.altTextColor = color;
        textPaint.setColor(color);
        this.invalidate();
    }

    public boolean isAltTextShown(){
        return showAltText;
    }

    public void showAltText(boolean b){
        this.showAltText = b;
        this.requestLayout();
        this.invalidate();
    }

    public boolean isAltTextTested(){
        return testAltText;
    }

    public void testAltText(boolean b){
        this.testAltText = b;
        storage.clearLayoutCache();
        render();
        unlockDrawing = false;
        this.requestLayout();
        this.invalidate();
    }

    public int getAltTextSize(){
        return altTextSize;
    }

    public void setAltTextSize(int size){
        this.altTextSize = size;
        textPaint.setTextSize(size);
        this.requestLayout();
        this.invalidate();
    }

    public String getAltText(){
        return altText;
    }

    public void setAltText(String text){
        this.altText = text;
        this.requestLayout();
        this.invalidate();
    }

    public float getLineThickness() {
        return lineThickness;
    }

    public void setLineThickness(float lineThickness) {
        this.lineThickness = lineThickness;
        if (isDrawLines()) {
            storage.clearLayoutCache();
            render();
        }
    }

    public boolean isDrawLines() {
        return drawLines;
    }

    public void setDrawLines(boolean drawLines) {
        this.drawLines = drawLines;
        storage.clearLayoutCache();
        render();
    }

    public float getPagePaddingLeft() {
        return pagePaddingLeft;
    }

    public void setPagePaddingLeft(float pagePaddingLeft) {
        this.pagePaddingLeft = pagePaddingLeft;
        storage.clearLayoutCache();
        render();
    }

    public float getPagePaddingTop() {
        return pagePaddingTop;
    }

    public void setPagePaddingTop(float pagePaddingTop) {
        this.pagePaddingTop = pagePaddingTop;
        storage.clearLayoutCache();
        render();
    }

    public float getPagePaddingRight() {
        return pagePaddingRight;
    }

    public void setPagePaddingRight(float pagePaddingRight) {
        this.pagePaddingRight = pagePaddingRight;
        storage.clearLayoutCache();
        render();
    }

    public float getPagePaddingBottom() {
        return pagePaddingBottom;
    }

    public void setPagePaddingBottom(float pagePaddingBottom) {
        this.pagePaddingBottom = pagePaddingBottom;
        storage.clearLayoutCache();
        render();
    }

    public float getSignPadding() {
        return signPadding;
    }

    public void setSignPadding(float signPadding) {
        this.signPadding = signPadding;
        storage.clearLayoutCache();
        render();
    }

    public float getLayoutSignPadding() {
        return layoutSignPadding;
    }

    public void setLayoutSignPadding(float layoutSignPadding) {
        this.layoutSignPadding = layoutSignPadding;
        storage.clearLayoutCache();
        render();
    }

    public float getInterLinePadding() {
        return interLinePadding;
    }

    public void setInterLinePadding(float interLinePadding) {
        this.interLinePadding = interLinePadding;
        storage.clearLayoutCache();
        render();
    }


    private void render(){
        taskScheduler.addTask(renderRunnable);
    }
}