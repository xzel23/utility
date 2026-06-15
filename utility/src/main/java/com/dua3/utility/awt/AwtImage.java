package com.dua3.utility.awt;

import com.dua3.utility.data.Image;

import java.awt.image.RenderedImage;

/**
 * An interface that represents an image using the Swing BufferedImage API.
 * It implements the Image interface.
 */
public interface AwtImage extends Image, RenderedImage {}
