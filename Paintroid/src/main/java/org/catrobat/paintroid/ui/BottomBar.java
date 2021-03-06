/**
 * Paintroid: An image manipulation application for Android.
 * Copyright (C) 2010-2015 The Catrobat Team
 * (<http://developer.catrobat.org/credits>)
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.catrobat.paintroid.ui;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.res.Configuration;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import org.catrobat.paintroid.MainActivity;
import org.catrobat.paintroid.NavigationDrawerMenuActivity;
import org.catrobat.paintroid.PaintroidApplication;
import org.catrobat.paintroid.R;
import org.catrobat.paintroid.dialog.InfoDialog;
import org.catrobat.paintroid.listener.BottomBarScrollListener;
import org.catrobat.paintroid.tools.Tool;
import org.catrobat.paintroid.tools.ToolFactory;
import org.catrobat.paintroid.tools.ToolType;

public class BottomBar implements View.OnClickListener, View.OnLongClickListener {

	private static final int SWITCH_TOOL_TOAST_Y_OFFSET = (int) NavigationDrawerMenuActivity.ACTION_BAR_HEIGHT + 25;
	private static final boolean ENABLE_CENTER_SELECTED_TOOL = true;
	private static final boolean ENABLE_START_SCROLL_ANIMATION = true;

	private MainActivity mMainActivity;
	private LinearLayout mToolsLayout;
	private Tool mCurrentTool;
	private Toast mToolNameToast;

	private enum ActionType {
		BUTTON_CLICK, LONG_BUTTON_CLICK
	}

	public BottomBar(MainActivity mainActivity) {
		mMainActivity = mainActivity;
		if(PaintroidApplication.currentTool == null) {
			mCurrentTool = ToolFactory.createTool(mainActivity, ToolType.BRUSH);
			getToolButtonByToolType(ToolType.BRUSH).setBackgroundResource(R.color.bottom_bar_button_activated);
			PaintroidApplication.currentTool = mCurrentTool;
		}
		else {
			mCurrentTool = ToolFactory.createTool(mainActivity, PaintroidApplication.currentTool.getToolType());
			PaintroidApplication.currentTool = mCurrentTool;
			getToolButtonByToolType(ToolType.BRUSH).setBackgroundResource(R.color.transparent);
			getToolButtonByToolType(mCurrentTool.getToolType()).setBackgroundResource(R.color.bottom_bar_button_activated);
		}
		mToolsLayout = (LinearLayout) mainActivity.findViewById(R.id.tools_layout);

		setBottomBarListener();

		if (ENABLE_START_SCROLL_ANIMATION) {
			startBottomBarAnimation();
		}
	}

	private void delayedAnimateSelectedTool(int startDelay) {
		ToolType toolType = PaintroidApplication.currentTool.getToolType();
		ImageButton button = getToolButtonByToolType(toolType);
		int color = ContextCompat.getColor(button.getContext(), R.color.bottom_bar_button_activated);
		int fadedColor = color & 0x00ffffff;
		ValueAnimator valueAnimator = ObjectAnimator.ofInt(button, "backgroundColor", color, fadedColor);
		valueAnimator.setEvaluator(new ArgbEvaluator());
		valueAnimator.setInterpolator(new LinearInterpolator());
		valueAnimator.setDuration(500);
		valueAnimator.setRepeatCount(5);
		valueAnimator.setRepeatMode(ValueAnimator.REVERSE);
		valueAnimator.setStartDelay(startDelay);
		valueAnimator.addListener(new Animator.AnimatorListener() {
			@Override
			public void onAnimationStart(Animator animation) {

			}

			@Override
			public void onAnimationEnd(Animator animation) {
				setActivatedToolButton(PaintroidApplication.currentTool);
			}

			@Override
			public void onAnimationCancel(Animator animation) {

			}

			@Override
			public void onAnimationRepeat(Animator animation) {

			}
		});
		valueAnimator.start();
	}

	private void startBottomBarAnimation() {
		final HorizontalScrollView horizontalScrollView = (HorizontalScrollView) mMainActivity.findViewById(R.id.bottom_bar_scroll_view);
		final ScrollView verticalScrollView = (ScrollView) mMainActivity.findViewById(R.id.bottom_bar_landscape_scroll_view);
		final int animationDuration = 1000;
		int orientation = mMainActivity.getResources().getConfiguration().orientation;
		if (orientation == Configuration.ORIENTATION_PORTRAIT) {
			horizontalScrollView.post(new Runnable() {
				public void run() {
					int scrollToX = (int) (getToolButtonByToolType(mCurrentTool.getToolType()).getX() - horizontalScrollView.getWidth() / 2.0f
							+ mMainActivity.getResources().getDimension(R.dimen.bottom_bar_button_width) / 2.0f);
					int scrollFromX = PaintroidApplication.isRTL ?
							horizontalScrollView.getChildAt(0).getLeft() :
							horizontalScrollView.getChildAt(0).getRight();
					horizontalScrollView.setScrollX(scrollFromX);
					ObjectAnimator.ofInt(horizontalScrollView, "scrollX", scrollToX).setDuration(animationDuration).start();
				}
			});
		} else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
			verticalScrollView.post(new Runnable() {
				public void run() {
					int scrollToY = (int) (getToolButtonByToolType(mCurrentTool.getToolType()).getY() - verticalScrollView.getHeight() / 2.0f
							+ mMainActivity.getResources().getDimension(R.dimen.bottom_bar_landscape_button_height) / 2.0f);
					int scrollFromY = verticalScrollView.getChildAt(0).getBottom();
					verticalScrollView.setScrollY(scrollFromY);
					ObjectAnimator.ofInt(verticalScrollView, "scrollY", scrollToY).setDuration(animationDuration).start();
				}
			});
		}

		delayedAnimateSelectedTool(animationDuration);
	}

	private void setBottomBarListener() {
		for (int i = 0; i < mToolsLayout.getChildCount(); i++) {
			mToolsLayout.getChildAt(i).setOnClickListener(this);
			mToolsLayout.getChildAt(i).setOnLongClickListener(this);
		}

		setBottomBarScrollerListener();
	}

	private void setBottomBarScrollerListener() {
		final ImageView next = (ImageView) mMainActivity.findViewById(R.id.bottom_next);
		final ImageView previous = (ImageView) mMainActivity.findViewById(R.id.bottom_previous);

		BottomBarHorizontalScrollView mScrollView = ((BottomBarHorizontalScrollView) mMainActivity.findViewById(R.id.bottom_bar_scroll_view));
		if(mScrollView == null )
			return;
		mScrollView.setScrollStateListener(new BottomBarScrollListener(previous, next));
	}

	public void setTool(Tool tool) {
		mCurrentTool = tool;
		showToolChangeToast();
		setActivatedToolButton(tool);

		if (ENABLE_CENTER_SELECTED_TOOL) {
			scrollToSelectedTool(tool);
		}
	}

	private void scrollToSelectedTool(Tool tool) {
		int orientation = mMainActivity.getResources().getConfiguration().orientation;

		if(orientation == Configuration.ORIENTATION_PORTRAIT) {
			HorizontalScrollView scrollView = (HorizontalScrollView) mMainActivity.findViewById(R.id.bottom_bar_scroll_view);
			scrollView.smoothScrollTo(
					(int) (getToolButtonByToolType(tool.getToolType()).getX() - scrollView.getWidth() / 2.0f
							+ mMainActivity.getResources().getDimension(R.dimen.bottom_bar_button_width) / 2.0f),
					(int) (getToolButtonByToolType(tool.getToolType()).getY()));
		}
		else if(orientation == Configuration.ORIENTATION_LANDSCAPE) {
			ScrollView scrollView = (ScrollView)mMainActivity.findViewById(R.id.bottom_bar_landscape_scroll_view);
			scrollView.smoothScrollTo((int) (getToolButtonByToolType(tool.getToolType()).getX()),
			(int) (getToolButtonByToolType(tool.getToolType()).getY() - scrollView.getHeight() / 2.0f
					+ mMainActivity.getResources().getDimension(R.dimen.bottom_bar_landscape_button_height) / 2.0f));
		}
	}

	private void showToolChangeToast() {
		if (mToolNameToast != null) {
			mToolNameToast.cancel();
		}

		mToolNameToast = Toast.makeText(mMainActivity, mMainActivity.getString(mCurrentTool.getToolType().getNameResource()), Toast.LENGTH_SHORT);
		mToolNameToast.setGravity(Gravity.TOP | Gravity.END, 0, SWITCH_TOOL_TOAST_Y_OFFSET);
		mToolNameToast.show();
	}

	@Override
	public void onClick(View view) {
		performToolButtonAction(view, ActionType.BUTTON_CLICK);
	}

	@Override
	public boolean onLongClick(View view) {
		return performToolButtonAction(view, ActionType.LONG_BUTTON_CLICK);
	}

	private boolean performToolButtonAction(View view, ActionType actionType) {
		ToolType toolType = null;

		for (ToolType type : ToolType.values()) {
			if (view.getId() == type.getToolButtonID()) {
				toolType = type;
				break;
			}
		}

		if (toolType == null) {
			return false;
		}
		else if (actionType == ActionType.BUTTON_CLICK) {
			if (PaintroidApplication.currentTool.getToolType() != toolType) {
				if(mMainActivity.isKeyboardShown()) {
					mMainActivity.hideKeyboard();
				} else {
					mMainActivity.switchTool(toolType);
				}
			} else {
				PaintroidApplication.currentTool.toggleShowToolOptions();
			}
		}
		else if (actionType == ActionType.LONG_BUTTON_CLICK) {
			new InfoDialog(InfoDialog.DialogType.INFO, toolType.getHelpTextResource(),
					toolType.getNameResource()).show(
					mMainActivity.getSupportFragmentManager(),
					"helpdialogfragmenttag");
		}
		return true;
	}

	private ImageButton getToolButtonByToolType(ToolType toolType) {
		return (ImageButton) mMainActivity.findViewById(toolType.getToolButtonID());
	}

	private void setActivatedToolButton(Tool tool) {
		for (int i = 0; i < mToolsLayout.getChildCount(); i++) {
			mToolsLayout.getChildAt(i).setBackgroundResource(R.color.transparent);
		}

		getToolButtonByToolType(tool.getToolType()).setBackgroundResource(R.color.bottom_bar_button_activated);
	}


}

