package com.dua3.utility.fx.controls;

import com.dua3.utility.data.Color;
import com.dua3.utility.data.Image;
import com.dua3.utility.data.ImageUtil;
import com.dua3.utility.text.FragmentedText;
import com.dua3.utility.text.Style;
import com.dua3.utility.ui.RichTextPaneLayoutHelper;
import javafx.scene.image.ImageView;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

@Timeout(value = 30, unit = TimeUnit.SECONDS)
class TextPaneDisplayScaleLayoutTest extends FxTestBase {

    @Test
    void displayScaleAppliesToBaseAndExplicitRunFonts() throws Exception {
        runOnFxThreadAndWait(() -> {
            Style large = Style.create("display-scale-large", Map.entry(Style.FONT_SIZE, 24.0f));
            RichTextBuilderFx builder = new RichTextBuilderFx();
            builder.append("base ");
            builder.push(large).append("large").pop(large);

            TextPane pane = new TextPane(builder.toRichText());
            pane.setDisplayScale(2.5);
            RichTextPaneLayoutHelper.Layout<?> layout = pane.createLayout(1_000.0);

            FragmentedText.Fragment base = findFragment(layout, "base");
            FragmentedText.Fragment largeFragment = findFragment(layout, "large");
            assertEquals(pane.getFont().getSizeInPoints() * 2.5, base.font().getSizeInPoints(), 0.01);
            assertEquals(24.0 * 2.5, largeFragment.font().getSizeInPoints(), 0.01);
        });
    }

    @Test
    void displayScaleAppliesToInlineImageFitDimensions() throws Exception {
        runOnFxThreadAndWait(() -> {
            Image image = ImageUtil.getInstance().createImage(
                    4,
                    2,
                    new int[]{
                            Color.RED.argb(), Color.GREEN.argb(), Color.BLUE.argb(), Color.YELLOW.argb(),
                            Color.YELLOW.argb(), Color.BLUE.argb(), Color.GREEN.argb(), Color.RED.argb()
                    }
            );
            RichTextBuilderFx builder = new RichTextBuilderFx();
            builder.appendImage(image, 12.0f, 8.0f);

            TextPane pane = new TextPane(builder.toRichText());
            pane.setDisplayScale(2.0);
            addToScene(pane);
            pane.applyCss();
            pane.layout();

            ImageView imageView = assertInstanceOf(ImageView.class, pane.lookup(".image-view"));
            assertEquals(24.0, imageView.getFitWidth(), 0.01);
            assertEquals(16.0, imageView.getFitHeight(), 0.01);
        });
    }

    private static FragmentedText.Fragment findFragment(RichTextPaneLayoutHelper.Layout<?> layout, String text) {
        return layout.renderLines().stream()
                .flatMap(java.util.Collection::stream)
                .filter(fragment -> fragment.text().toString().contains(text))
                .findFirst()
                .orElseThrow();
    }
}
