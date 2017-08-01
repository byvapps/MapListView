package com.inlacou.byvapps.maplistlib.general;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.style.URLSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by inlacou on 2/11/16.
 */

public class ViewUtils {

	private static String DEBUG_TAG = ViewUtils.class.getName();

	public static void setMargins(View v, int l, int t, int r, int b) {
		if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
			ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
			p.setMargins(l, t, r, b);
			v.requestLayout();
		}else{
			Log.d(DEBUG_TAG, "Margins not set");
		}
	}

	public static void setPaddings(View v, int l, int t, int r, int b) {
		v.setPadding(l, t, r, b);
		v.requestLayout();
	}

	public static int dpToPixel(Context context, int pixels){
		Resources r = context.getResources();
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, pixels, r.getDisplayMetrics());
	}

	public static int pixelToDp(Activity activity, int dp){
		DisplayMetrics metrics = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
		float logicalDensity = metrics.density;
		return (int) Math.ceil(dp * logicalDensity);
	}

	/*public static void resizeView(View view, int newWidth, int newHeight) {
		Log.d(DEBUG_TAG+".resizeView", "newWidth: " + newWidth + " | newHeight: " + newHeight);
		Log.d(DEBUG_TAG+".resizeView", "newWidth: " + (newWidth==-1?view.getWidth():newWidth) + " | newHeight: " + (newHeight==-1?view.getHeight():newHeight));
		try {
			Constructor<? extends ViewGroup.LayoutParams> ctor = view.getLayoutParams().getClass().getDeclaredConstructor(int.class, int.class);
			view.setLayoutParams(ctor.newInstance(newWidth==-1?view.getWidth():newWidth, newHeight==-1?view.getHeight():newHeight));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}*/

	public static void stripUnderlines(TextView textView) {
		Spannable s = new SpannableString(textView.getText());
		URLSpan[] spans = s.getSpans(0, s.length(), URLSpan.class);
		for (URLSpan span: spans) {
			int start = s.getSpanStart(span);
			int end = s.getSpanEnd(span);
			s.removeSpan(span);
			span = new URLSpanNoUnderline(span.getURL());
			s.setSpan(span, start, end, 0);
		}
		textView.setText(s);
	}

	/**
	 * Este es el bueno
	 * @param view to modify
	 * @param width in pixels
	 * @param height in pixels
	 */
	public static void resizeView2(View view, int width, int height) {
		ViewGroup.LayoutParams lp = view.getLayoutParams();
		if(width>-1) {
			lp.width = width;
		}
		if(height>-1) {
			lp.height = height;
		}
		view.setLayoutParams(lp);
	}

	private static class URLSpanNoUnderline extends URLSpan {
		public URLSpanNoUnderline(String url) {
			super(url);
		}
		@Override
		public void updateDrawState(TextPaint ds) {
			super.updateDrawState(ds);
			ds.setUnderlineText(false);
		}
	}

	// Convert a view to bitmap
	public static Bitmap createDrawableFromView(Context context, View view) {
		DisplayMetrics displayMetrics = new DisplayMetrics();
		((AppCompatActivity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		view.setLayoutParams(new LinearLayoutCompat.LayoutParams(LinearLayoutCompat.LayoutParams.WRAP_CONTENT, LinearLayoutCompat.LayoutParams.WRAP_CONTENT));
		view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
		view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
		view.buildDrawingCache();
		Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);

		Canvas canvas = new Canvas(bitmap);
		view.draw(canvas);

		return bitmap;
	}

	public static int getScreenWidthPixels(AppCompatActivity activity){
		Display display = activity.getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		return size.x;
	}

	public static int getScreenHeightPixels(AppCompatActivity activity){
		Display display = activity.getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		return size.y;
	}

	public static int pxToDp(int px) {
		return (int) (px / Resources.getSystem().getDisplayMetrics().density);
	}

	public static int dpToPx(int dp) {
		return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
	}

	public static void setCenterVertical(View v, boolean centerVertical){
		RelativeLayout.LayoutParams layoutParams =
				(RelativeLayout.LayoutParams)v.getLayoutParams();
		layoutParams.addRule(RelativeLayout.CENTER_VERTICAL, centerVertical?-1:1);
	}
	
	public static LinearLayout.LayoutParams getLinearLayoutParamsWithMargins(int top, int left, int right, int bottom){
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		
		layoutParams.setMargins(left, top, right, bottom);
		return layoutParams;
	}

}
