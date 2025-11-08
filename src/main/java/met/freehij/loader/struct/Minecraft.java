package met.freehij.loader.struct;

import met.freehij.loader.mappings.ClassMapping;
import met.freehij.loader.mappings.Creator;
import met.freehij.loader.mappings.FieldMapping;
import met.freehij.loader.mappings.MethodInvoker;
import met.freehij.kareliq.ClientMain;
import met.freehij.loader.mappings.AccessUtils;
import met.freehij.loader.mappings.StaticFieldMapping;
import met.freehij.loader.mappings.StaticMethodInvoker;

@ClassMapping("net/minecraft/client/Minecraft")
public interface Minecraft extends AccessUtils{
	
	public static Minecraft getMinecraft() {
		return Creator.proxy(null, Minecraft.class, false).theMinecraft();
	}
	
	@StaticMethodInvoker("isDebugInfoEnabled")
	public boolean isDebugInfoEnabled();
	
	@StaticFieldMapping("theMinecraft")
	public Minecraft theMinecraft();
	
	@FieldMapping("thePlayer")
	public EntityPlayerSP thePlayer();
	
	@FieldMapping("renderGlobal")
	public RenderGlobal renderGlobal();
	
	@FieldMapping("gameSettings")
	public GameSettings gameSettings();

	@MethodInvoker("displayGuiScreen")
	public void displayGuiScreen(GuiScreen screen);
	@MethodInvoker("isMultiplayerWorld")
	public boolean isMultiplayerWorld();
	
	
	@FieldMapping("fontRenderer")
	public FontRenderer fontRenderer();
	
	@FieldMapping("currentScreen")
	public GuiScreen currentScreen();
	
	@FieldMapping("ingameGUI")
	public GuiIngame ingameGUI();
}
