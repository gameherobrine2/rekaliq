package met.freehij.kareliq.injection;

import met.freehij.kareliq.util.BackgroundUtils;
import met.freehij.loader.annotation.Inject;
import met.freehij.loader.annotation.Injection;
import met.freehij.loader.mappings.Creator;
import met.freehij.loader.struct.GuiButton;
import met.freehij.loader.struct.GuiMainMenu;
import met.freehij.loader.struct.GuiScreen;
import met.freehij.loader.struct.Minecraft;
import met.freehij.loader.util.InjectionHelper;

@Injection("GuiMainMenu")
public class GuiMainMenuInjection {
    @Inject(method = "initGui")
    public static void initGui(InjectionHelper helper) throws ClassNotFoundException {
    	GuiMainMenu dis = Creator.proxy(helper.getSelf().get(), GuiMainMenu.class);
    	dis.controlList().add(InjectionHelper.getClazz("GuiButton").newInstance("IIIIILjava/lang/String;", 67, dis.width() - 102, 2, 100, 20, "Main menu settings").get());
    }

    @Inject(method = "actionPerformed")
    public static void actionPerformed(InjectionHelper helper) throws InstantiationException, IllegalAccessException {
    	GuiButton button = Creator.proxy(helper.getArg(1), GuiButton.class);
        if (button.id() == 67) {
        	Minecraft.getMinecraft().displayGuiScreen(Creator.proxy(BackgroundUtils.guiEditMainMenu.newInstance(), GuiScreen.class));
        }
    }
}
