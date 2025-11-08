package met.freehij.kareliq.injection;

import met.freehij.kareliq.module.render.Brightness;
import met.freehij.kareliq.module.world.NoClip;
import met.freehij.kareliq.module.world.WaterWalking;
import met.freehij.loader.annotation.Inject;
import met.freehij.loader.annotation.Injection;
import met.freehij.loader.mappings.Creator;
import met.freehij.loader.struct.AxisAlignedBB;
import met.freehij.loader.struct.Block;
import met.freehij.loader.struct.EntityPlayerSP;
import met.freehij.loader.struct.Minecraft;
import met.freehij.loader.util.InjectionHelper;
import met.freehij.loader.util.Reflector;
import met.freehij.loader.util.mappings.ClassMappings;
import met.freehij.loader.util.mappings.FieldMappings;
import met.freehij.loader.util.mappings.MethodMappings;
import met.freehij.loader.util.mappings.util.MethodMapping;
import org.lwjgl.input.Keyboard;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

@Injection("BlockFluid")
public class BlockFluidInjection {

    @Inject(method = "getBlockBrightness")
    public static void getBlockBrightness(InjectionHelper helper) {
        if (Brightness.INSTANCE.isToggled()) {
            helper.setReturnValue((float) Brightness.INSTANCE.getSettings()[0].getDouble());
            helper.setCancelled(true);
        }
    }

    @Inject(method = "getCollisionBoundingBoxFromPool")
    public static void getCollisionBoundingBoxFromPool(InjectionHelper helper) throws ClassNotFoundException {
        if (NoClip.INSTANCE.isToggled()) return;
        if (!WaterWalking.INSTANCE.isToggled()) return;
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayerSP player = mc.thePlayer();
        Block block = Creator.proxy(helper.getSelf().get(), Block.class);
        if (Keyboard.isKeyDown(mc.gameSettings().keyBindSneak().keyCode())) {
            return;
        }
        if (player.fallDistance() > 2.F) return;
        if (player.isInWater()) {
        	player.jump();
            return;
        }
        AxisAlignedBB aabb = AxisAlignedBB.nul().getBoundingBoxFromPool(
        	(int) helper.getArg(2) + block.minX(),
            (int) helper.getArg(3) + block.minY(),
            (int) helper.getArg(4) + block.minZ(),
            (int) helper.getArg(2) + block.maxX(),
            (int) helper.getArg(3) + block.maxY(),
            (int) helper.getArg(4) + block.maxZ()
        );
        
        helper.setReturnValue(aabb._this());
        
        helper.setCancelled(true);
    }
}
