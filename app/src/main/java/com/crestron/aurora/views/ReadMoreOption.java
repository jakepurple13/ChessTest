package com.crestron.aurora.views;


import android.animation.LayoutTransition;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import androidx.annotation.NonNull;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.crestron.aurora.Loged;

/**
 * Created by ${Deven} on 6/1/18.
 */
public class ReadMoreOption {

    private static final String TAG = ReadMoreOption.class.getSimpleName();
    public static final int TYPE_LINE = 1;
    public static final int TYPE_CHARACTER = 2;

    // required
    private Context context;
    // optional
    private int textLength;
    private int textLengthType;
    private String moreLabel;
    private String lessLabel;
    private int moreLabelColor;
    private int lessLabelColor;
    private boolean labelUnderLine;
    private boolean expandAnimation;

    private ReadMoreOption(Builder builder) {
        this.context = builder.context;
        this.textLength = builder.textLength;
        this.textLengthType = builder.textLengthType;
        this.moreLabel = builder.moreLabel;
        this.lessLabel = builder.lessLabel;
        this.moreLabelColor = builder.moreLabelColor;
        this.lessLabelColor = builder.lessLabelColor;
        this.labelUnderLine = builder.labelUnderLine;
        this.expandAnimation = builder.expandAnimation;
    }

    public void addReadMoreTo(final TextView textView, final String text) {

        if (textLengthType == TYPE_CHARACTER) {
            if (text.length() <= textLength) {
                textView.setText(text);
                return;
            }
        } else {
            // If TYPE_LINE
            textView.setLines(textLength);
            textView.setText(text);
        }

        textView.post(new Runnable() {
            @Override
            public void run() {

                int textLengthNew = textLength;

                if (textLengthType == TYPE_LINE) {

                    if (textView.getLayout().getLineCount() <= textLength) {
                        textView.setText(text);
                        return;
                    }

                    ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) textView.getLayoutParams();
                    Loged.INSTANCE.wtf(text, Loged.INSTANCE.getTAG(), true);
                    String subString = text.substring(textView.getLayout().getLineStart(0),
                            textView.getLayout().getLineEnd(textLength - 1));
                    textLengthNew = subString.length() - (moreLabel.length() + 4 + (lp.rightMargin / 6));
                }

                SpannableString ss = new SpannableString(text.substring(0, textLengthNew) + "... " + moreLabel);
                ClickableSpan clickableSpan = new ClickableSpan() {
                    @Override
                    public void onClick(@NonNull View view) {
                        addReadLess(textView, text);
                    }

                    @Override
                    public void updateDrawState(@NonNull TextPaint ds) {
                        super.updateDrawState(ds);
                        ds.setUnderlineText(labelUnderLine);
                        ds.setColor(moreLabelColor);
                    }
                };
                ss.setSpan(clickableSpan, ss.length() - moreLabel.length(), ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                if (expandAnimation) {
                    LayoutTransition layoutTransition = new LayoutTransition();
                    layoutTransition.enableTransitionType(LayoutTransition.CHANGING);
                    ((ViewGroup) textView.getParent()).setLayoutTransition(layoutTransition);
                }

                textView.setText(ss);
                textView.setMovementMethod(LinkMovementMethod.getInstance());
            }
        });


    }

    private void addReadLess(final TextView textView, final String text) {
        textView.setMaxLines(Integer.MAX_VALUE);
        SpannableString ss = new SpannableString(text + " " + lessLabel);
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View view) {
                new Handler().post(() -> addReadMoreTo(textView, text));
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(labelUnderLine);
                ds.setColor(lessLabelColor);
            }
        };
        ss.setSpan(clickableSpan, ss.length() - lessLabel.length(), ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        textView.setText(ss);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    public static class Builder {
        // required
        private Context context;
        // optional
        private int textLength = 100;
        private int textLengthType = ReadMoreOption.TYPE_CHARACTER;
        private String moreLabel = "read more";
        private String lessLabel = "read less";
        private int moreLabelColor = Color.parseColor("#ff00ff");
        private int lessLabelColor = Color.parseColor("#ff00ff");
        private boolean labelUnderLine = false;
        private boolean expandAnimation = false;

        public Builder(Context context) {
            this.context = context;
        }

        /**
         * @param length         can be no. of line OR no. of characters - default is 100 character
         * @param textLengthType ReadMoreOption.TYPE_LINE for no. of line OR
         *                       ReadMoreOption.TYPE_CHARACTER for no. of character
         *                       - default is ReadMoreOption.TYPE_CHARACTER
         * @return Builder obj
         */
        public Builder textLength(int length, int textLengthType) {
            this.textLength = length;
            this.textLengthType = textLengthType;
            return this;
        }

        public Builder moreLabel(String moreLabel) {
            this.moreLabel = moreLabel;
            return this;
        }

        public Builder lessLabel(String lessLabel) {
            this.lessLabel = lessLabel;
            return this;
        }

        public Builder moreLabelColor(int moreLabelColor) {
            this.moreLabelColor = moreLabelColor;
            return this;
        }

        public Builder lessLabelColor(int lessLabelColor) {
            this.lessLabelColor = lessLabelColor;
            return this;
        }

        public Builder labelUnderLine(boolean labelUnderLine) {
            this.labelUnderLine = labelUnderLine;
            return this;
        }

        /**
         * @param expandAnimation either true to enable animation on expand or false to disable animation
         *                        - default is false
         * @return Builder obj
         */
        public Builder expandAnimation(boolean expandAnimation) {
            this.expandAnimation = expandAnimation;
            return this;
        }

        public ReadMoreOption build() {
            return new ReadMoreOption(this);
        }

    }

}