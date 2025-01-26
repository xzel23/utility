Approximate elliptical Arcs using cubic Bézier Splines
======================================================

The arcTo() method is using a Bézier Spline base approximation. The approximation error should be less than a single
pixel on a 4K screen.

Input parameter

- `Vector2f p0`: start point
- `Vector2f p1`: end point
- `Vector2f r`: x- and y-radius
- `float angle`: the rotation angle
- `boolean sweep`: `true` for counterclockwise
- `boolean longarc`: to select the long arc segment

Algorithm overview
------------------

1. Rotate by `-angle` to align the ellipse with the coordinate system
2. Determine the center of the ellipse
3. Split arc into multiple segments
4. Generate Bézier segments

Implementation
--------------

### Rotate and sclae the coordinate system so that the ellipse becomes a circle

Define the transformation matrix `M`:

```
    M = AffineTransformation.combine(
        AffineTransformation2f.rotate(-angle)
        AffineTransformation2f.scale(1/rx, 1/ry)
    )
```

Then transform the points:

- `Vector2f p0L`: start point in local coordinates
- `Vector2f p1L`: end point in local coordinates

### Calculate the center

The circle is defined by

$$
(x - c_x)^2 + (y - c_y)^2 = 1
$$

$$
x^2 - 2 x c_x + c_x^2 + y^2 - 2 y c_y + y^2 = 1
$$

$$
x_0^2 - 2 x_0 c_x + c_x^2 + y_0^2 - 2 y_0 c_y + y_0^2 = 1
$$
$$
x_1^2 - 2 x_1 c_x + c_x^2 + y_1^2 - 2 y_1 c_y + y_1^2 = 1
$$

$$
x_0^2 - x_1^2 - 2 x_0 c_x + 2 x_1 c_x + y_0^2 - y_1^2 + 2 y_0 c_y - 2 y_1 c_y + y_1^2 = 0
$$

$$
(x_0 - x_1)(x_0 + x_1) - 2 c_x (x_0 - x_1) + (y_0 - y_1)(y_0 + y_1) - 2 c_y (y_0 - y_1) + (y_0^2 -y_1^2) = 0
$$

$$
2 c_x (x_0 - x_1) = (x_0 - x_1)(x_0 + x_1) + (y_0 - y_1)(y_0 + y_1)  + (y_0^2 -y_1^2) + 2 c_y (y_0 - y_1)
$$
