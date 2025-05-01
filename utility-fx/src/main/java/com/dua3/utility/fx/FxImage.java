package com.dua3.utility.fx;

import com.dua3.utility.data.Image;

/**
 * A record representing an image in JavaFX.
 * This class implements the {@link Image} interface.
 */
public interface FxImage extends Image {
    /**
     * Get the JavaFX Image instance.
     * @return the JavaFX Image instance
     */
    javafx.scene.image.Image fxImage();
}
