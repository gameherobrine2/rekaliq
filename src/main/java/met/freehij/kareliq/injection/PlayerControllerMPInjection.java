package met.freehij.kareliq.injection;

import met.freehij.kareliq.module.world.FastBreak;
import met.freehij.loader.annotation.Inject;
import met.freehij.loader.annotation.Injection;
import met.freehij.loader.mappings.Creator;
import met.freehij.loader.struct.PlayerControllerMP;
import met.freehij.loader.util.InjectionHelper;

@Injection("PlayerControllerMP")
public class PlayerControllerMPInjection {
    @Inject(method = "clickBlock")
    public static void clickBlock(InjectionHelper helper) {
        if (FastBreak.INSTANCE.isToggled()) Creator.proxy(helper.getSelf().get(), PlayerControllerMP.class).curBlockDamageMP(1.0f);
    }
}
