package com.crestron.aurora.server;

import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.AlignmentSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.SubscriptSpan;
import android.text.style.SuperscriptSpan;
import android.text.style.URLSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.widget.TextView;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * BBCode parser
 *
 * @author Pierre HUBERT
 */
public class BBCodeParser {

    /**
     * Debug tag
     */
    private static final String TAG = BBCodeParser.class.getSimpleName();

    /**
     * Recursion limit, default set to 10
     */
    private int mRecursionLimit = 10;

    private char[] text;

    /**
     * Construct new instance of BBCodeParser
     */
    public BBCodeParser() {

    }

    public int getRecursionLimit() {
        return mRecursionLimit;
    }

    public BBCodeParser setRecursionLimit(int recursionLimit) {
        this.mRecursionLimit = recursionLimit;
        return this;
    }


    /**
     * Parse a BBCode string for a {@link TextView}
     *
     * @param text The text to parse
     * @return Parsed string
     */
    public SpannableStringBuilder parse(@NonNull String text) {

        SpannableStringBuilder ssb = new SpannableStringBuilder();

        this.text = text.toCharArray();

        try {
            parseInternal(null, 0, 0, ssb);
        } catch (Exception e) {
            Log.e(TAG, "Unable to parse text!");
            e.printStackTrace();
            return new SpannableStringBuilder(text);
        }

        return ssb;

    }

    private int parseInternal(@Nullable String parentTag, int level, int pos, SpannableStringBuilder ssb) {

        int new_pos = pos;
        int childNumber = 0;
        while (new_pos < text.length) {

            boolean stop = false;
            while (new_pos < text.length && !stop) {
                for (; new_pos < text.length && text[new_pos] != '['; new_pos++)
                    ssb.append(text[new_pos]);
                new_pos++;

                if (new_pos > text.length) new_pos = text.length;


                if (level < mRecursionLimit)
                    for (int i = new_pos; i < text.length && !stop && text[i] != '[' && text[i] != ' '; i++)
                        if (text[i] == ']')
                            stop = true;

                if (!stop && text[new_pos - 1] == '[')
                    ssb.append('[');

            }

            if (new_pos >= text.length)
                return new_pos;

            int closeTagPos = findChar(new_pos, ']');

            //Process the new tag
            //Check if we have to exit current tag
            if (text[new_pos] == '/')
                return closeTagPos;


            //Determine tag
            String tag = String.copyValueOf(text, new_pos, closeTagPos - new_pos);

            int length_before = ssb.length();

            //Pre decoding
            preDecodeTag(childNumber, parentTag, tag, ssb, length_before);

            //Parse tag content
            int end_pos = parseInternal(tag, level + 1, closeTagPos + 1, ssb);
            int length_after = ssb.length();

            //Post decoding
            postDecodeTag(tag, ssb, length_before, length_after);


            new_pos = end_pos + 1;
            childNumber++;
        }

        return new_pos;
    }


    private void preDecodeTag(int childNumber, @Nullable String parentTag, String tag, SpannableStringBuilder ssb, int begin) {

        switch (tag) {


            case "ul":
            case "ol":
                ssb.append(System.getProperty("line.separator"));
                break;


            case "li":

                if (Objects.equals(parentTag, "ol")) {
                    ssb.append(String.valueOf(childNumber + 1)).append(". ");
                } else
                    ssb.append("\u2022 ");
                break;


            case "quote":
            case "code":
                ssb.append(System.getProperty("line.separator"));
                ssb.append(System.getProperty("line.separator"));
                break;

        }
    }


    private void postDecodeTag(String tag, SpannableStringBuilder ssb, int begin, int end) {

        Object span = null;
        Object span2 = null;

        String[] args = null;

        if (tag.contains("=")) {
            args = tag.split("=");
            tag = args[0];
        }


        switch (tag) {

            case "img":
                span = new URLSpan("https://www.w3schools.com/w3images/bandmember.jpg");
                break;

            case "big":
                span = new AbsoluteSizeSpan(20, true);
                break;

            case "small":
                span = new AbsoluteSizeSpan(10, true);
                break;

            //Decode italic and bold
            case "i":
            case "b":
                int style = tag.equals("i") ? Typeface.ITALIC : Typeface.BOLD;
                span = new StyleSpan(style);
                break;

            //Underline text
            case "u":
                span = new UnderlineSpan();
                break;

            //Strikethrough
            case "s":
                span = new StrikethroughSpan();
                break;


            //Superscript
            case "sup":
                span = new SuperscriptSpan();
                span2 = new RelativeSizeSpan(0.6f);
                break;

            //Subscript
            case "sub":
                span = new SubscriptSpan();
                span2 = new RelativeSizeSpan(0.6f);
                break;

            //Color
            case "color":
                if (args == null) {
                    Log.e(TAG, "Invalid color!");
                    break;
                }
                span = new ForegroundColorSpan(Color.parseColor(args[1]));
                break;


            //Lists - Lists are aligned to the left by default
            case "ul":
            case "ol":


                //Left alignment
            case "left":
                span = new AlignmentSpan.Standard(Layout.Alignment.ALIGN_NORMAL);
                break;

            //Center alignment
            case "center":
                span = new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER);
                break;

            //Right alignment
            case "right":
                span = new AlignmentSpan.Standard(Layout.Alignment.ALIGN_OPPOSITE);
                break;

            //Line break for list items
            case "li":
                ssb.append(System.getProperty("line.separator"));
                break;


            //Quotes
            case "quote":
            case "code":
                postDecodeTag("left", ssb, begin, end);
                postDecodeTag("i", ssb, begin, end);
                ssb.append(System.getProperty("line.separator"));
                ssb.append(System.getProperty("line.separator"));
                ssb.append(System.getProperty("line.separator"));
                break;

            default:
                Log.e(TAG, tag + " not recognized: " + tag);
                break;
        }

        if (span != null)
            ssb.setSpan(span, begin, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (span2 != null)
            ssb.setSpan(span2, begin, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }


    private int findChar(int start, char c) {
        for (int i = start; i < text.length; i++) {
            if (text[i] == c)
                return i;
        }

        return text.length;
    }
}
