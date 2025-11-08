package com.blueapps.thoth;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
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
import com.blueapps.maat.BoundProperty;
import com.blueapps.thoth.cache.CacheStorage;

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

    private Thread renderThread = new Thread();
    private RenderRunnable renderRunnable;

    private CacheStorage storage;
    private RenderClass renderClass;

    // Paints
    private final Paint textPaint = new Paint();

    // Attributes
    private String glyphX = "<ancientText><v><sign id=\"r\"/><sign id=\"Z1\"/></v><v><sign id=\"n\"/><sign id=\"km\"/></v><sign id=\"m\"/><v><sign id=\"t\"/><sign id=\"O49\"/></v></ancientText>";
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
        altText = a.getString(R.styleable.ThothView_altText);
        altTextSize = a.getDimensionPixelSize(R.styleable.ThothView_altTextSize, height / 2);
        showAltText = a.getBoolean(R.styleable.ThothView_showAltText, true);
        altTextColor = a.getColor(R.styleable.ThothView_altTextColor, Color.BLACK);
        backgroundColor = a.getColor(R.styleable.ThothView_backgroundColor, Color.TRANSPARENT);
        primarySignColor = a.getColor(R.styleable.ThothView_primarySignColor, Color.BLACK);
        textSize = a.getDimensionPixelSize(R.styleable.ThothView_android_textSize, 200);
        verticalOrientation = a.getInteger(R.styleable.ThothView_verticalOrientation, 1);
        //writingDirection = a.getInteger(R.styleable.ThothView_writingDirection, 0);
        writingLayout = a.getInteger(R.styleable.ThothView_writingLayout, 0);

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

            renderRunnable = new RenderRunnable(this, renderClass);
            renderThread = new Thread(renderRunnable);
            renderThread.start();

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
                drawable.setBounds(storage.getBounds().get(counter));
                if (drawable instanceof VectorDrawable){
                    VectorDrawable drawable1 = (VectorDrawable) drawable;
                    drawable1.setTint(primarySignColor);
                    drawable1.draw(canvas);
                } else {
                    drawable.draw(canvas);
                }
                counter++;
            }
        } else {
            if (showAltText) {
                canvas.drawText(altText, 0, altTextSize + (height - altTextSize) / 2, textPaint);
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        if (writingLayout == 0) {
            height = textSize;
        } else {
            width = textSize;
        }

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

    public String getGlyphXText(){
        return glyphX;
    }

    public void setMdCText(String mdc){
        this.MdC = mdc;
        try {
            this.glyphX = GlyphConverter.convertToGlyphX(mdc);
            storage.setGlyphXContent(glyphX);
            storage.refreshCache();
            renderThread = new Thread(renderRunnable);
            renderThread.start();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void setGlyphXText(String glyphX){
        this.glyphX = glyphX;
        try {
            this.MdC = GlyphConverter.convertToMdC(glyphX);
            storage.setGlyphXContent(glyphX);
            storage.refreshCache();
            renderThread = new Thread(renderRunnable);
            renderThread.start();
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
        renderThread = new Thread(renderRunnable);
        renderThread.start();
    }

    public int getVerticalOrientation(){
        return verticalOrientation;
    }

    public void setVerticalOrientation(int verticalOrientation){
        if (verticalOrientation < 3 && verticalOrientation > -1) {
            this.verticalOrientation = verticalOrientation;
            storage.clearLayoutCache();
            renderThread = new Thread(renderRunnable);
            renderThread.start();
        }
    }

    public int getWritingDirection(){
        return writingDirection;
    }

    public int getWritingLayout(){
        return writingLayout;
    }

    public void setWritingLayout(int writingLayout){
        if (writingLayout == 0 || writingLayout == 1){
            this.writingLayout = writingLayout;
            storage.clearLayoutCache();
            renderThread = new Thread(renderRunnable);
            renderThread.start();
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
        renderThread = new Thread(renderRunnable);
        renderThread.start();
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

}