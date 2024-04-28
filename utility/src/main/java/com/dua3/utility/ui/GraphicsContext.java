/*
 * Copyright 2016 Axel Howind.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.dua3.utility.ui;

import com.dua3.utility.data.Color;
import com.dua3.utility.math.geometry.Rectangle2f;

/**
 * The GraphicsContext interface provides methods for drawing various shapes and setting properties related to the graphics.
 */
public interface GraphicsContext {

    /**
     * Draws a line between two points.
     *
     * @param x1 the start point's x-coordinate
     * @param y1 the start point's y-coordinate
     * @param x2 the end point's x-coordinate
     * @param y2 the end point's y-coordinate
     */
    void drawLine(float x1, float y1, float x2, float y2);

    /**
     * Draws a rectangle with the specified position and size.
     *
     * @param x      the rectangle's upper-left corner x-coordinate
     * @param y      the rectangle's upper-left corner y-coordinate
     * @param width  the rectangle's width
     * @param height the rectangle's height
     */
    void drawRect(float x, float y, float width, float height);

    /**
     * Draws a rectangle with the same position and size as the given rectangle.
     *
     * @param r the rectangle to be drawn
     */
    default void drawRect(Rectangle2f r) {
        drawRect(r.xMin(), r.yMin(), r.width(), r.height());
    }

    /**
     * Fills a rectangle with the specified position and size.
     *
     * @param x      the rectangle's upper-left corner x-coordinate
     * @param y      the rectangle's upper-left corner y-coordinate
     * @param width  the rectangle's width
     * @param height the rectangle's height
     */
    void fillRect(float x, float y, float width, float height);

    /**
     * Fills a rectangle with the same position and size as the given rectangle.
     *
     * @param r the rectangle to be drawn
     */
    default void fillRect(Rectangle2f r) {
        fillRect(r.xMin(), r.yMin(), r.width(), r.height());
    }

    /**
     * Returns the bounds of the current clipping area, specified as a Rectangle object.
     *
     * @return the Rectangle object representing the boundaries of the current clipping area.
     */
    Rectangle2f getClipBounds();

    /**
     * Sets the current color.
     *
     * @param color the new color to be set.
     */
    void setColor(Color color);

    /**
     * Sets the stroke for drawing shapes.
     *
     * @param color the color of the stroke to be set.
     * @param width the width of the stroke to be set.
     */
    void setStroke(Color color, float width);

}
