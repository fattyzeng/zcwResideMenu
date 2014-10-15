package com.zcw.ResideMenu;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.special.ResideMenu.R;

public class ResideMenuItem extends LinearLayout {

	private TextView tv_title;
	private ImageView iv_icon;

	public ResideMenuItem(Context context) {
		super(context);
		initView(context);
	}

	public ResideMenuItem(Context context, int icon, String title) {
		super(context);
		initView(context);
		setTitle(title);
		SetIcon(icon);
	}

	public ResideMenuItem(Context context, int title, int icon) {
		super(context);
		initView(context);
		SetIcon(icon);
		setTitle(title);
	}

	private void initView(Context context) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.residemenu_item, null);
		tv_title = (TextView) view.findViewById(R.id.tv_title);
		iv_icon = (ImageView) view.findViewById(R.id.iv_icon);
	}

	public void setTitle(String title) {
		if (tv_title != null) {
			tv_title.setText(title);
		}
	}

	public void setTitle(int title) {
		if (tv_title != null) {
			tv_title.setText(title);
		}
	}

	public void SetIcon(int icon) {
		if (iv_icon != null) {
			iv_icon.setImageResource(icon);
		}
	}
}
