/*
 *   This file is part of Paintroid, a software part of the Catroid project.
 *   Copyright (C) 2010  Catroid development team
 *   <http://code.google.com/p/catroid/wiki/Credits>
 *
 *   Paintroid is free software: you can redistribute it and/or modify it
 *   under the terms of the GNU Affero General Public License as published
 *   by the Free Software Foundation, either version 3 of the License, or
 *   at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Affero General Public License for more details.
 *
 *   You should have received a copy of the GNU Affero General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package at.tugraz.ist.paintroid.tools;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.PointF;
import at.tugraz.ist.paintroid.MainActivity.ToolType;

public interface Tool {
	// standard stroke widths in pixels
	public static final int stroke1 = 1;
	public static final int stroke5 = 5;
	public static final int stroke15 = 15;
	public static final int stroke25 = 25;

	public boolean handleDown(PointF coordinate);

	public boolean handleMove(PointF coordinate);

	public boolean handleUp(PointF coordinate);

	public void changePaintColor(int color);

	public void changePaintStrokeWidth(int strokeWidth);

	public void changePaintStrokeCap(Cap cap);

	public void setDrawPaint(Paint paint);

	public Paint getDrawPaint();

	public void draw(Canvas canvas);

	public ToolType getToolType();

	public int getAttributeButtonResource(int buttonNumber);

	public int getAttributeButtonColor(int buttonNumber);

	public void attributeButtonClick(int buttonNumber);

	/**
	 * Called by the drawing thread when the tools effect has been applied to the bitmap. Tools must
	 * use this method to reset their internal state, like rewinding paths.
	 */
	public void onAppliedToBitmap();
}