package met.freehij.kareliq.injection;

import met.freehij.kareliq.module.render.Brightness;
import met.freehij.kareliq.module.render.OreViewer;
import met.freehij.kareliq.module.world.NoClip;
import met.freehij.kareliq.util.Utils;
import met.freehij.loader.annotation.Inject;
import met.freehij.loader.annotation.Injection;
import met.freehij.loader.mappings.Creator;
import met.freehij.loader.struct.Block;
import met.freehij.loader.util.InjectionHelper;

@Injection("Block")
public class BlockInjection {
    private static final int[] oreViewerBlocks = new int[] { 7, 14, 15, 16, 21, 56, 74, 74 };

    @Inject(method = "getBlockBrightness")
    public static void getBlockBrightness(InjectionHelper helper) {
        if (Brightness.INSTANCE.isToggled()) {
            helper.setReturnValue((float) Brightness.INSTANCE.getSettings()[0].getDouble());
            helper.setCancelled(true);
        }
    }

    @Inject(method = "shouldSideBeRendered")
    public static void shouldSideBeRendered(InjectionHelper helper) {
        if (OreViewer.INSTANCE.isToggled()) {
            if (Utils.containsValue(oreViewerBlocks, Creator.proxy(helper.getSelf().get(), Block.class).blockID())) {
                helper.setReturnValue(true);
            } else {
                helper.setReturnValue(false);
            }
            helper.setCancelled(true);
        }
    }

    @Inject(method = "getCollisionBoundingBoxFromPool")
    public static void getCollisionBoundingBoxFromPool(InjectionHelper helper) {
        if (NoClip.INSTANCE.isToggled()) {
            helper.setReturnValue(null);
            helper.setCancelled(true);
        }
    }
}
