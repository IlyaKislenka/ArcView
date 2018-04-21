package com.example.i_kislenko.arcview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Build;
import android.support.annotation.ColorRes;
import android.support.annotation.DimenRes;
import android.support.annotation.FontRes;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Ilya Kislenko on 02.02.18.
 * <p>
 *      This view was designed for specific case and you can't : set any custom font for text,
 * set circle width or progress view width;
 *
 *      This view always try to drawing in the center of provided place, is weight support disabled;
 *
 *      All in this view was designed with attitude some brilliant size from elements inside. That mean that if
 * you passed the wrap_content as one of the layout sizes,  layout takes this size as brilliant (getting it from dimensions).
 *
 *      I suppose that you put into the dimensions these two sizes: brilliant view dimension (one side) and brilliant circle view width,
 * which named as : R.dimen.base_arc_view_size && R.dimen.external_arc_width;
 *
 *      This view set up fonts from res/font (it will work only with android-studio 3.0 and gradle 3.0.1 and later).
 *
 *      I expect that you put them on the res/fonts and it will:
 *      oswald_light.ttf - for percent view,
 *      avenir_roman.otf - for footer
 *      alternategotno1dregular.ttf - for center percentage text;
 *
 *      Also if you would like to correctly render your view in preview you should put copy of fonts which i notice above on assets/fonts;
 *
 *      I can guarantee that only with this fonts all will be okay.
 *
 * <p>
 *
 *      isWeightSupportEnabled() - this attribute was make view width and height the same, and view
 * will receive square shape. It may be useful when you want using width or height as weight.
 *      Example: you was set width or height as weight (width == 0 && weight == 1)  the second size will be the same;
 */
public class ArcView extends View {
    
    private final String PERCENTAGE = "0",
            HUNDRED_PERCENTAGE = "100",
            PERCENT = "%",
            FOOTER = "footer";


    private String percentage = "0", footer = "";
    private int circleColor, progressColor, shadowColor;
    private boolean isWeightSupportEnabled = false;

    private float externalArcWidth,
            internalArcWidth,
            progressLineLength = 0,
            currentPercentage,
            lineAttitude,
            startAngle,
            artLengthDegrees,
            shadowWidth,
            drawViewSize;

    private Typeface percentageTypeface, percentTypeface, footerTypeface;

    private Map<String, Float> textBoxHeights, textBoxHeightAttitudes, baseFontsAttitudes;

    public ArcView(Context context) {
        super(context);
        setLayerType(LAYER_TYPE_SOFTWARE, null);
    }

    public ArcView(Context context, @Nullable AttributeSet attrs) {

        super(context, attrs);
        parseAttributes(context, attrs);
        setLayerType(LAYER_TYPE_SOFTWARE, null);
    }

    public ArcView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {

        super(context, attrs, defStyleAttr);
        parseAttributes(context, attrs);
        setLayerType(LAYER_TYPE_SOFTWARE, null);
    }

    private void parseAttributes(Context context, AttributeSet attrs) {

        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.ArcView);

        circleColor = array.getColor(R.styleable.ArcView_circleArcColor, getColorViaId(context, R.color.externalColor));
        progressColor = array.getColor(R.styleable.ArcView_progressArcColor, getColorViaId(context, R.color.internalColor));
        shadowColor = array.getColor(R.styleable.ArcView_shadowColor, getColorViaId(context, R.color.blackShadowColor));

        shadowWidth = array.getDimensionPixelSize(R.styleable.ArcView_shadowWidth, 9);

        isWeightSupportEnabled = array.getBoolean(R.styleable.ArcView_enableWeightsSupport, false);

        startAngle = array.getInt(R.styleable.ArcView_startArcAngle, 135);
        artLengthDegrees = array.getInt(R.styleable.ArcView_artLengthDegrees, 270);

        footer = getFooterFromAttributes(array);

        initTypefaces(context);

        array.recycle();
    }

    private String getFooterFromAttributes(TypedArray array) {

        String attributedFooter = array.getString(R.styleable.ArcView_footerText);

        return attributedFooter != null ? attributedFooter : "";
    }

    private void initTypefaces(Context context) {

        if (isInEditMode()) {
            percentageTypeface = Typeface.createFromAsset(context.getAssets(), "fonts/alternategotno1dregular.ttf");
            percentTypeface = Typeface.createFromAsset(context.getAssets(), "fonts/oswald_light.ttf");
            footerTypeface = Typeface.createFromAsset(context.getAssets(), "fonts/avenir_roman.otf");
        } else {
            percentageTypeface = getTypefaceFromReference(context, R.font.alternategotno1dregular);
            percentTypeface = getTypefaceFromReference(context, R.font.oswald_light);
            footerTypeface = getTypefaceFromReference(context, R.font.avenir_roman);
        }
    }

    /**
     * Expected passing percentage from 0.0 to 1.0, to show progress;
     *
     * @param percentageToShow
     */
    public void setPercentage(float percentageToShow) {

        if (percentageToShow > 1.05f) {
            percentageToShow = 1f;
        }

        progressLineLength = artLengthDegrees * percentageToShow;
        currentPercentage = percentageToShow;
        int value = (int) (percentageToShow * 100);
        percentage = String.valueOf(value);
        invalidate();
    }

    public float getPercentage() {
        return currentPercentage;
    }

    public void setFooter(String footer) {

        this.footer = footer;
        invalidate();
    }

    public String getFooter() {

        return footer;
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        initConstants(width, height);
    }

    private void initConstants(int width, int height) {

        int baseSize = getResources().getDimensionPixelSize(R.dimen.base_arc_view_size);

        width = width == 0 ? baseSize : width;
        height = height == 0 ? baseSize : height;

        drawViewSize = width < height ? width : height;

        initAttitudes();
    }

    private void initAttitudes() {

        int brilliantViewSize = getResources().getDimensionPixelSize(R.dimen.base_arc_view_size);
        int brilliantLineWidth = getResources().getDimensionPixelOffset(R.dimen.external_arc_width);

        lineAttitude = brilliantViewSize / brilliantLineWidth;

        measureBaseTypefaceHeights(percentage);
        measureFontsAttitudes();

        externalArcWidth = drawViewSize / lineAttitude;
        internalArcWidth = externalArcWidth * 0.6f;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int minSize = getResources().getDimensionPixelSize(R.dimen.base_arc_view_size);

        int width = widthMeasureSpec <= 0 ? minSize : widthMeasureSpec;
        int height = heightMeasureSpec <= 0 ? minSize : heightMeasureSpec;

        if (isWeightSupportEnabled) {

            if (width != minSize && height == minSize)
                super.onMeasure(width, width);
            else if (width == minSize && height != minSize)
                super.onMeasure(height, height);
            else {
                setUpMeasurement(width, height, minSize);
                super.onMeasure(width, height);
            }

        } else {
            setUpMeasurement(width, height, minSize);
            super.onMeasure(width, height);
        }

    }

    private void setUpMeasurement(int width, int height, int minSize) {

        if (width == minSize)
            setMinimumWidth(minSize);
        if (height == minSize)
            setMinimumHeight(height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawArc(provideRectF(), startAngle, artLengthDegrees, false, makeArcPaint(externalArcWidth, circleColor));
        canvas.drawArc(provideRectF(), startAngle, artLengthDegrees, false, makeArcPaint(externalArcWidth - shadowWidth, shadowColor));
        canvas.drawArc(provideRectF(), startAngle, progressLineLength, false, makeArcPaint(internalArcWidth, progressColor));

        drawText(percentage, PERCENT, footer, canvas);

    }

    private RectF provideRectF() {

        float left, right, top, bottom;

        float padding = (externalArcWidth / 2) + (shadowWidth / 2);
        float height = getMeasuredHeight(), width = getMeasuredWidth();

        if (drawViewSize == getMeasuredWidth()) {

            left = padding;
            right = drawViewSize - padding;
            top = (height / 2) - (drawViewSize / 2) + padding;
            bottom = (height / 2) + (drawViewSize / 2) - padding;

        } else {

            left = (width / 2) - (drawViewSize / 2) + padding;
            right = (width / 2) + (drawViewSize / 2) - padding;
            top = padding;
            bottom = drawViewSize - padding;
        }

        return new RectF(left, top, right, bottom);
    }

    private Paint makeArcPaint(float paintWidth, int colorId) {

        Paint paint = new Paint();

        if (colorId == shadowColor) {
            paint.setColor(circleColor);
            paint.setShadowLayer(shadowWidth, 0, 0, shadowColor);
        } else
            paint.setColor(colorId);

        boolean isAbsoluteCircle = artLengthDegrees >= 360;

        paint.setAntiAlias(true);
        paint.setStrokeCap(isAbsoluteCircle ? Paint.Cap.ROUND : Paint.Cap.SQUARE);
        paint.setStrokeWidth(paintWidth);
        paint.setStyle(Paint.Style.STROKE);
        return paint;
    }

    private void drawText(String percentage, String percent, String footer, Canvas canvas) {


        float drawTextXPos = getXPositionForPercentage();

        Paint percentagePaint = getPercentagePaint();
        float percentXPosition = drawTextXPos + (percentagePaint.measureText(percentage) - 2);


        canvas.drawText(percentage, 0, percentage.length(), drawTextXPos, getYPositionForSpecificText(true), percentagePaint);

        canvas.drawText(percent, 0, 1, percentXPosition, getYPositionForSpecificText(true), getPercentPaint());

        canvas.drawText(footer, 0, footer.length(), getXPosForFooter(), getYPositionForSpecificText(false), getFooterPaint());
    }

    private Float getMeasuredFontSizeForPercentage(String percentage) {

        float percentageValue = Float.valueOf(percentage);

        float heightAttitude = (percentageValue < 100) ? textBoxHeightAttitudes.get(PERCENTAGE) : textBoxHeightAttitudes.get(HUNDRED_PERCENTAGE);
        float fontAttitude = (percentageValue < 100) ? baseFontsAttitudes.get(PERCENTAGE) : baseFontsAttitudes.get(HUNDRED_PERCENTAGE);

        float expectedHeight = drawViewSize / heightAttitude;
        float expectedFontSize = expectedHeight / fontAttitude;

        return expectedFontSize;
    }

    private Float getMeasuredFontSizeForPercent() {

        float heightAttitude = textBoxHeightAttitudes.get(PERCENT);
        float fontAttitude = baseFontsAttitudes.get(PERCENT);

        float expectedHeight = drawViewSize / heightAttitude;
        float expectedFontSize = expectedHeight / fontAttitude;

        return expectedFontSize;
    }

    private Float getMeasuredFontSizeForFooter() {

        float heightAttitude = textBoxHeightAttitudes.get(FOOTER);
        float fontAttitude = baseFontsAttitudes.get(FOOTER);

        float expectedHeight = drawViewSize / heightAttitude;
        float expectedFontSize = expectedHeight / fontAttitude;

        return expectedFontSize;
    }

    private float getXPositionForPercentage() {

        Paint percentagePaint = getPercentagePaint();

        float percentageWidth = percentagePaint.measureText(percentage);
        float correction = percentage.length() > 2 ? 1.7f : percentage.length() < 2 ? 2f : 1.8f;

        return (getMeasuredWidth() / 2) - (percentageWidth / correction);
    }

    private float getYPositionForSpecificText(boolean isCenterText) {

        return (getMeasuredHeight() / 2) + (drawViewSize * (isCenterText ? 0.16f : 0.4f));
    }

    private float getXPosForFooter() {

        TextPaint paint = getFooterPaint();
        float footerWidth = paint.measureText(footer);

        return (getMeasuredWidth() / 2) - (footerWidth / 2);
    }

    private TextPaint getPercentagePaint() {

        return makeTextPaint(getMeasuredFontSizeForPercentage(percentage), percentageTypeface);
    }

    private TextPaint getPercentPaint() {

        return makeTextPaint(getMeasuredFontSizeForPercent(), percentTypeface);
    }

    private TextPaint getFooterPaint() {

        return makeTextPaint(getMeasuredFontSizeForFooter(), footerTypeface);
    }

    private void measureBaseTypefaceHeights(String percentage) {

        Paint percentagePaint = getPercentagePaintFromLength(percentage.length());
        Paint percentPaint = makeTextPaintForMeasuring(R.dimen.text_size_percent, percentTypeface);
        Paint footerPaint = makeTextPaintForMeasuring(R.dimen.text_size_footer, footerTypeface);
        Paint hundredPercentagePaint = getPercentagePaintFromLength(3);

        float percentageTextHeight = getTextSizeFromPaint(percentagePaint);
        float hundredPercentageHeight = getTextSizeFromPaint(hundredPercentagePaint);
        float percentTextHeight = getTextSizeFromPaint(percentPaint);
        float footerTextHeight = getTextSizeFromPaint(footerPaint);

        textBoxHeights = new HashMap<>(4);
        textBoxHeights.put(PERCENTAGE, percentageTextHeight);
        textBoxHeights.put(HUNDRED_PERCENTAGE, hundredPercentageHeight);
        textBoxHeights.put(PERCENT, percentTextHeight);
        textBoxHeights.put(FOOTER, footerTextHeight);

        float percentageTextHeightAttitude = getAttitudeFromBrilliantSize(percentageTextHeight);
        float hundredPercentageHeightAttitude = getAttitudeFromBrilliantSize(hundredPercentageHeight);
        float percentTextHeightAttitude = getAttitudeFromBrilliantSize(percentTextHeight);
        float footerTextHeightAttitude = getAttitudeFromBrilliantSize(footerTextHeight);

        textBoxHeightAttitudes = new HashMap<>(4);
        textBoxHeightAttitudes.put(PERCENTAGE, percentageTextHeightAttitude);
        textBoxHeightAttitudes.put(HUNDRED_PERCENTAGE, hundredPercentageHeightAttitude);
        textBoxHeightAttitudes.put(PERCENT, percentTextHeightAttitude);
        textBoxHeightAttitudes.put(FOOTER, footerTextHeightAttitude);
    }


    private void measureFontsAttitudes() {

        float percentageFontSize = getFontSizeFromDimensions(R.dimen.text_size_percentage);
        float hundredPercentageFontSize = getFontSizeFromDimensions(R.dimen.text_size_center_percentage_hundred);
        float percentFontSize = getFontSizeFromDimensions(R.dimen.text_size_percent);
        float footerFontSize = getFontSizeFromDimensions(R.dimen.text_size_footer);

        float percentageTextHeight = textBoxHeights.get(PERCENTAGE);
        float hundredPercentageTextHeight = textBoxHeights.get(HUNDRED_PERCENTAGE);
        float percentTextHeight = textBoxHeights.get(PERCENT);
        float footerTextHeight = textBoxHeights.get(FOOTER);

        float percentageFontSizeAttitude = getAttitudeFromParams(percentageTextHeight, percentageFontSize);
        float hundredPercentageFontSizeAttitude = getAttitudeFromParams(hundredPercentageTextHeight, hundredPercentageFontSize);
        float percentFontSizeAttitude = getAttitudeFromParams(percentTextHeight, percentFontSize);
        float footerFontSizeAttitude = getAttitudeFromParams(footerTextHeight, footerFontSize);

        baseFontsAttitudes = new HashMap<>(4);
        baseFontsAttitudes.put(PERCENTAGE, percentageFontSizeAttitude);
        baseFontsAttitudes.put(HUNDRED_PERCENTAGE, hundredPercentageFontSizeAttitude);
        baseFontsAttitudes.put(PERCENT, percentFontSizeAttitude);
        baseFontsAttitudes.put(FOOTER, footerFontSizeAttitude);
    }

    private Paint getPercentagePaintFromLength(int length) {

        return makeTextPaintForMeasuring(length != 3 ? R.dimen.text_size_percentage : R.dimen.text_size_center_percentage_hundred, percentageTypeface);
    }

    private float getTextSizeFromPaint(Paint paint) {
        return (-paint.ascent() + paint.descent());
    }

    private float getAttitudeFromBrilliantSize(float sizeToMeasure) {

        float brilliantSize = getResources().getDimensionPixelSize(R.dimen.base_arc_view_size);
        return brilliantSize / sizeToMeasure;
    }

    private float getAttitudeFromParams(float height, float fontSize) {
        return height / fontSize;
    }

    private float getFontSizeFromDimensions(@DimenRes int fontSize) {
        return getResources().getDimensionPixelOffset(fontSize);
    }

    private int getColorViaId(Context context, @ColorRes int colorId) {
        return ContextCompat.getColor(context, colorId);
    }

    private Typeface getTypefaceFromReference(Context context, @FontRes int fontId) {
        return ResourcesCompat.getFont(context, fontId);
    }

    private TextPaint makeTextPaintForMeasuring(@DimenRes Integer textSize, Typeface typeface) {

        TextPaint paint = new TextPaint();
        paint.setTextSize(getResources().getDimensionPixelSize(textSize));
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setColor(ContextCompat.getColor(getContext(), R.color.fontColor));
        paint.setTypeface(typeface);
        paint.setFlags(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT ?
                Paint.EMBEDDED_BITMAP_TEXT_FLAG | Paint.DEV_KERN_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG :
                Paint.DEV_KERN_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
        return paint;
    }

    private TextPaint makeTextPaint(Float textSize, Typeface typeface) {

        TextPaint paint = new TextPaint();
        paint.setTextSize(textSize);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setColor(ContextCompat.getColor(getContext(), R.color.fontColor));
        paint.setTypeface(typeface);
        paint.setFlags(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT ?
                Paint.EMBEDDED_BITMAP_TEXT_FLAG | Paint.DEV_KERN_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG :
                Paint.DEV_KERN_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
        return paint;
    }
}
