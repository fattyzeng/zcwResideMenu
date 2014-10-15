package com.zcw.ResideMenu;

import java.util.ArrayList;
import java.util.List;

import android.R.anim;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.view.ViewHelper;
import com.special.ResideMenu.R;

@SuppressLint("NewApi")
public class ResideMenu extends FrameLayout {
	public static final int DIRECTION_LEFT = 0;
	public static final int DIRECTION_RIGHT = 1;
	private static final int PRESSED_MOVE_HORIZANTAL = 2;
	private static final int PRESSED_DOWN = 3;
	private static final int PRESSED_DONE = 4;
	private static final int PRESSED_MOVE_VERTICAL = 5;
	private ImageView imageViewShadow;
	private ImageView imageViewBackground;
	private LinearLayout layoutLeftMenu;
	private LinearLayout layoutRightMenu;
	private ScrollView scrollViewLeftMenu;
	private ScrollView scrollViewRightMenu;
	private ScrollView scrollViewMenu;
	private Activity activity;
	private ViewGroup viewDecor;
	private TouchDisableView viewActivity;
	private boolean isOpened;
	private float shadowAdjustScaleX;
	private float shadowAdjustScaleY;
	private List<View> ignoredViews;
	private List<ResideMenuItem> leftMenuItems;
	private List<ResideMenuItem> rightMenuItems;
	private DisplayMetrics displayMetrics = new DisplayMetrics();
	private OnMenuListener menuListener;
	private float lastRawX;
	private boolean isInIgnoredView = false;
	private int scaleDirection = DIRECTION_LEFT;
	private int pressedState = PRESSED_DOWN;
	private List<Integer> disabledSwipeDirection = new ArrayList<Integer>();
	private float mScaleValue = 0.5f;

	public ResideMenu(Context context) {
		super(context);
		initView(context);

	}

	private void initView(Context context) {
		LayoutInflater layoutInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = layoutInflater.inflate(R.layout.residemenu, null);
		scrollViewLeftMenu = (ScrollView) view.findViewById(R.id.sv_left_menu);
		scrollViewRightMenu = (ScrollView) view
				.findViewById(R.id.sv_right_menu);
		imageViewBackground = (ImageView) view.findViewById(R.id.iv_background);
		imageViewShadow = (ImageView) view.findViewById(R.id.iv_shadow);
		layoutLeftMenu = (LinearLayout) view
				.findViewById(R.id.layout_left_menu);
		layoutRightMenu = (LinearLayout) view
				.findViewById(R.id.layout_right_menu);
	}

	public interface OnMenuListener {
		void menuOpened();

		void menuClosed();

	}

	public void setShadowAdjustScaleXByOrientation() {
		int orientation = activity.getResources().getConfiguration().orientation;
		if (orientation == Configuration.ORIENTATION_PORTRAIT) {
			shadowAdjustScaleX = 0.034f;
			shadowAdjustScaleY = 0.12f;
		} else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
			shadowAdjustScaleX = 0.06f;
			shadowAdjustScaleY = 0.07f;
		}
	}

	private void initValue(Activity activity) {
		this.activity = activity;
		viewActivity = new TouchDisableView(activity);
		viewDecor = (ViewGroup) activity.getWindow().getDecorView();
		ignoredViews = new ArrayList<View>();
		leftMenuItems = new ArrayList<ResideMenuItem>();
		rightMenuItems = new ArrayList<ResideMenuItem>();
		View view = viewDecor.getChildAt(0);
		viewDecor.removeView(view);
		viewActivity.setContentView(view);
		addView(viewDecor);
		ViewGroup viewGroup = (ViewGroup) this.scrollViewLeftMenu.getParent();
		viewGroup.removeView(scrollViewLeftMenu);
		viewGroup.removeView(scrollViewRightMenu);
	}

	public void setViewPadding() {
		this.setPadding(viewActivity.getLeft(), viewActivity.getTop(),
				viewActivity.getRight(), viewActivity.getBottom());
	}

	public void attachToActivity(Activity activity) {
		initValue(activity);
		viewDecor.addView(this, 0);
		setShadowAdjustScaleXByOrientation();
		setViewPadding();
	}

	public void setBackground(int resId) {
		this.imageViewBackground.setImageResource(resId);
	}

	public void setShadowVisible(boolean isVisible) {
		if (isVisible) {
			imageViewShadow.setVisibility(View.VISIBLE);
			imageViewShadow.setImageResource(R.drawable.shadow);
		} else {
			imageViewShadow.setVisibility(View.GONE);
		}
	}

	public void addMenuItem(ResideMenuItem menuItem, int direction) {
		if (direction == DIRECTION_LEFT) {
			this.leftMenuItems.add(menuItem);
			this.layoutLeftMenu.addView(menuItem, 0);
		} else if (direction == DIRECTION_RIGHT) {
			this.rightMenuItems.add(menuItem);
			this.layoutRightMenu.addView(menuItem, 0);
		}
	}

	public void setMenuItems(List<ResideMenuItem> menuItems, int direction) {
		if (direction == DIRECTION_LEFT) {
			leftMenuItems = menuItems;
		} else {
			rightMenuItems = menuItems;
		}
		rebuildMenuItem();
	}

	private void rebuildMenuItem() {
		this.layoutLeftMenu.removeAllViews();
		this.layoutRightMenu.removeAllViews();
		for (int i = 0; i < leftMenuItems.size(); i++) {
			this.layoutLeftMenu.addView(leftMenuItems.get(i), i);
		}
		for (int i = 0; i < rightMenuItems.size(); i++) {
			this.layoutRightMenu.addView(rightMenuItems.get(i), i);
		}
	}

	public List<ResideMenuItem> getMenuItems(int direction) {
		if (direction == DIRECTION_LEFT) {
			return leftMenuItems;
		} else {
			return rightMenuItems;
		}
	}

	public OnMenuListener getMenuListener() {
		return menuListener;
	}

	public void setMenuListener(OnMenuListener menuListener) {
		this.menuListener = menuListener;
	}

	private AnimatorSet buildMenuAnimation(View target, float alpha) {
		AnimatorSet animatorSet = new AnimatorSet();
		animatorSet
				.playTogether(ObjectAnimator.ofFloat(target, "alpha", alpha));
		animatorSet.setDuration(250);
		return animatorSet;
	}

	private AnimatorSet buildScaleUpAnimation(View target, float targetScaleX,
			float targetScaleY) {
		AnimatorSet animatorSet = new AnimatorSet();
		animatorSet.playTogether(ObjectAnimator.ofFloat(target, "scaleX",
				targetScaleX));
		animatorSet.playTogether(ObjectAnimator.ofFloat(target, "scaleY",
				targetScaleY));
		animatorSet.setDuration(250);
		return animatorSet;
	}

	private AnimatorSet buildScaleDownAnimation(View target,
			float targetScaleX, float targetScaleY) {
		AnimatorSet animatorSet = new AnimatorSet();
		animatorSet.playTogether(ObjectAnimator.ofFloat(target, "scaleX",
				targetScaleX));
		animatorSet.playTogether(ObjectAnimator.ofFloat(target, "scaleY",
				targetScaleY));
		animatorSet.setInterpolator(AnimationUtils.loadInterpolator(activity,
				anim.accelerate_interpolator));
		animatorSet.setDuration(250);
		return animatorSet;
	}

	public void addIgnoredView(View v) {
		this.ignoredViews.add(v);
	}

	public void removeIgnoredView(View v) {
		this.ignoredViews.remove(v);
	}

	public void clearIgnoredViewList() {
		this.ignoredViews.clear();
	}

	private boolean isInIgnoredView(MotionEvent ev) {
		Rect rect = new Rect();
		for (View view : ignoredViews) {
			view.setClipBounds(rect);
			if (rect.contains((int) ev.getX(), (int) ev.getY())) {
				return true;
			}
		}
		return false;
	}

	private void setScaleDirectionByRawX(float currentRawX) {
		if (lastRawX < currentRawX) {
			scaleDirection = DIRECTION_RIGHT;
		} else {
			scaleDirection = DIRECTION_LEFT;
		}
	}

	public void setSwipeDirectionDisable(int direction) {
		disabledSwipeDirection.add(direction);
	}

	private boolean isInDisableDirection(int direction) {
		return disabledSwipeDirection.contains(direction);
	}

	private void setScaleDirection(int direction) {

		int screenWidth = getScreenWidth();
		float pivotX;
		float pivotY = getScreenHeight() * 0.5f;

		if (direction == DIRECTION_LEFT) {
			scrollViewMenu = scrollViewLeftMenu;
			pivotX = screenWidth * 1.5f;
		} else {
			scrollViewMenu = scrollViewRightMenu;
			pivotX = screenWidth * -0.5f;
		}

		ViewHelper.setPivotX(viewActivity, pivotX);
		ViewHelper.setPivotY(viewActivity, pivotY);
		ViewHelper.setPivotX(imageViewShadow, pivotX);
		ViewHelper.setPivotY(imageViewShadow, pivotY);
		scaleDirection = direction;
	}

	public int getScreenHeight() {
		activity.getWindowManager().getDefaultDisplay()
				.getMetrics(displayMetrics);
		return displayMetrics.heightPixels;
	}

	public int getScreenWidth() {
		activity.getWindowManager().getDefaultDisplay()
				.getMetrics(displayMetrics);
		return displayMetrics.widthPixels;
	}

	public void setScaleValue(float scaleValue) {
		mScaleValue = scaleValue;
	}

	public void openMenu(int direction) {
		setScaleDirection(direction);
		isOpened = true;
		AnimatorSet scaleDown_activity = buildScaleDownAnimation(viewActivity,
				mScaleValue, mScaleValue);
		AnimatorSet scaleDown_shadow = buildScaleDownAnimation(imageViewShadow,
				mScaleValue + shadowAdjustScaleX, mScaleValue
						+ shadowAdjustScaleY);
		AnimatorSet alpha_menu = buildMenuAnimation(scrollViewMenu, 1.0f);
		scaleDown_shadow.addListener(animationListener);
		scaleDown_activity.playTogether(scaleDown_shadow);
		scaleDown_activity.playTogether(alpha_menu);
		scaleDown_activity.start();
	}

	public void closeMenu() {
		isOpened = false;
		AnimatorSet scaleUp_activity = buildScaleUpAnimation(viewActivity,
				1.0f, 1.0f);
		AnimatorSet scaleUp_shadow = buildScaleUpAnimation(imageViewShadow,
				1.0f, 1.0f);
		AnimatorSet alpha_menu = buildMenuAnimation(scrollViewMenu, 0.0f);
		scaleUp_activity.addListener(animationListener);
		scaleUp_activity.playTogether(scaleUp_shadow);
		scaleUp_activity.playTogether(alpha_menu);
		scaleUp_activity.start();
	}

	private Animator.AnimatorListener animationListener = new Animator.AnimatorListener() {
		@Override
		public void onAnimationStart(Animator animation) {
			if (isOpened()) {
				showScrollViewMenu();
				if (menuListener != null)
					menuListener.menuOpened();
			}
		}

		@Override
		public void onAnimationEnd(Animator animation) {
			if (isOpened()) {
				viewActivity.setmTouchDisable(true);
				viewActivity.setOnClickListener(viewActivityOnClickListener);
			} else {
				viewActivity.setmTouchDisable(false);
				viewActivity.setOnClickListener(null);
				hideScrollViewMenu();
				if (menuListener != null)
					menuListener.menuClosed();
			}
		}

		@Override
		public void onAnimationCancel(Animator animation) {

		}

		@Override
		public void onAnimationRepeat(Animator animation) {

		}
	};

	protected boolean isOpened() {
		return isOpened;
	}

	private void showScrollViewMenu() {
		if (scrollViewMenu != null && scrollViewMenu.getParent() == null) {
			addView(scrollViewMenu);
		}
	}

	private void hideScrollViewMenu() {
		if (scrollViewMenu != null && scrollViewMenu.getParent() != null) {
			removeView(scrollViewMenu);
		}
	}

	private OnClickListener viewActivityOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View view) {
			if (isOpened())
				closeMenu();
		}
	};

	private float getTargetScale(float currentRawX) {
		float scaleFloatX = ((currentRawX - lastRawX) / getScreenWidth()) * 0.75f;
		scaleFloatX = scaleDirection == DIRECTION_RIGHT ? -scaleFloatX
				: scaleFloatX;
		float targetScale = ViewHelper.getScaleX(viewActivity) - scaleFloatX;
		targetScale = targetScale > 1.0f ? 1.0f : targetScale;
		targetScale = targetScale < 0.5f ? 0.5f : targetScale;
		return targetScale;
	}

	private float lastActionDownX, lastActionDownY;

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		float currentActivityScaleX = ViewHelper.getScaleX(viewActivity);
		if (currentActivityScaleX == 1.0f)
			setScaleDirectionByRawX(ev.getRawX());

		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			lastActionDownX = ev.getX();
			lastActionDownY = ev.getY();
			isInIgnoredView = isInIgnoredView(ev) && !isOpened();
			pressedState = PRESSED_DOWN;
			break;

		case MotionEvent.ACTION_MOVE:
			if (isInIgnoredView || isInDisableDirection(scaleDirection))
				break;

			if (pressedState != PRESSED_DOWN
					&& pressedState != PRESSED_MOVE_HORIZANTAL)
				break;

			int xOffset = (int) (ev.getX() - lastActionDownX);
			int yOffset = (int) (ev.getY() - lastActionDownY);

			if (pressedState == PRESSED_DOWN) {
				if (yOffset > 25 || yOffset < -25) {
					pressedState = PRESSED_MOVE_VERTICAL;
					break;
				}
				if (xOffset < -50 || xOffset > 50) {
					pressedState = PRESSED_MOVE_HORIZANTAL;
					ev.setAction(MotionEvent.ACTION_CANCEL);
				}
			} else if (pressedState == PRESSED_MOVE_HORIZANTAL) {
				if (currentActivityScaleX < 0.95) {
					showScrollViewMenu();
				}
				float targetScale = getTargetScale(ev.getRawX());
				ViewHelper.setScaleX(viewActivity, targetScale);
				ViewHelper.setScaleY(viewActivity, targetScale);
				ViewHelper.setScaleX(imageViewShadow, targetScale
						+ shadowAdjustScaleX);
				ViewHelper.setScaleY(imageViewShadow, targetScale
						+ shadowAdjustScaleY);
				ViewHelper.setAlpha(scrollViewMenu, (1 - targetScale) * 2.0f);
				lastRawX = ev.getRawX();
				return true;
			}

			break;

		case MotionEvent.ACTION_UP:
			if (isInIgnoredView)
				break;
			if (pressedState != PRESSED_MOVE_HORIZANTAL)
				break;

			pressedState = PRESSED_DONE;
			if (isOpened()) {
				if (currentActivityScaleX > 0.56f) {
					closeMenu();
				} else {
					openMenu(scaleDirection);
				}
			} else {
				if (currentActivityScaleX < 0.94f) {
					openMenu(scaleDirection);
				} else {
					closeMenu();
				}
			}

			break;

		}
		lastRawX = ev.getRawX();
		return super.dispatchTouchEvent(ev);
	}

}
