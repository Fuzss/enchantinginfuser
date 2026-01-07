package fuzs.enchantinginfuser.client.gui.components;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.network.chat.Component;

/**
 * A string widget that always renders text centered and handles overflowing text via scrolling.
 */
public class ScrollingStringWidget extends StringWidget {

    public ScrollingStringWidget(int posX, int posY, int width, int height, Component component, Font font) {
        super(posX, posY, width, height, component, font);
        // Any value above zero will work.
        this.setMaxWidth(1, TextOverflow.SCROLLING);
    }

    @Override
    public int getWidth() {
        // Bypass all the max width calculations to force the widget to simply render as normal, but with all the text scrolling.
        return this.width;
    }
}
