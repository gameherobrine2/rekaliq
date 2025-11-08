package met.freehij.kareliq.injection;

import met.freehij.loader.annotation.Inject;
import met.freehij.loader.annotation.Injection;
import met.freehij.loader.mappings.Creator;
import met.freehij.loader.struct.Gui;
import met.freehij.loader.struct.GuiButton;
import met.freehij.loader.util.InjectionHelper;

@Injection("Gui")
public class GuiButtonInjection {
    public static byte buttonMode = 0;

    @Inject(method = "drawTexturedModalRect")
    public static void drawTexturedModalRect(InjectionHelper helper) {
        if (buttonMode == 0) return;
        Gui gui = Creator.proxy(helper.getSelf().get(), Gui.class);
        if(gui._instanceof(GuiButton.class)) {
        	GuiButton button = Creator.proxy(helper.getSelf().get(), GuiButton.class);
        	int x = button.xPosition();
        	int y = button.yPosition();
        	button.drawRect(x, y, x+button.width(), y+button.height(), 0x80000000);
        	helper.setCancelled(true);
        }
    }
}
