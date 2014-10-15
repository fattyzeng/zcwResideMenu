package com.zcw.ResideMenu;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

public class TouchDisableView extends ViewGroup {

	private boolean mTouchDisable;
	private View mContentView;

	public boolean ismTouchDisable() {
		return mTouchDisable;
	}

	public void setmTouchDisable(boolean mTouchDisable) {
		this.mTouchDisable = mTouchDisable;
	}

	public View getmContentView() {
		return mContentView;
	}

	public void setContentView(View mContentView) {
		if (mContentView != null) {
			removeView(mContentView);
		}
		this.mContentView = mContentView;
		addView(mContentView);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		return mTouchDisable;
	}

	public TouchDisableView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public TouchDisableView(Context context) {
		this(context, null);
	}

	@Override
	protected void onLayout(boolean arg0, int arg1, int arg2, int arg3, int arg4) {
		int width = arg3 - arg1;
		int height = arg4 - arg2;
		mContentView.layout(0, 0, width, height);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int width = getDefaultSize(0, widthMeasureSpec);
		int height = getDefaultSize(0, heightMeasureSpec);
		setMeasuredDimension(width, height);
		int contentWidth = getChildMeasureSpec(0, 0, width);
		int contentHeight = getChildMeasureSpec(0, 0, height);
		mContentView.measure(contentWidth, contentHeight);
	}

}
