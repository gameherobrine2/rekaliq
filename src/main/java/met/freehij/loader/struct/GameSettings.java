package met.freehij.loader.struct;

import met.freehij.loader.mappings.AccessUtils;
import met.freehij.loader.mappings.ClassMapping;
import met.freehij.loader.mappings.FieldMapping;

@ClassMapping("net/minecraft/src/GameSettings")
public interface GameSettings extends AccessUtils{
	@FieldMapping("keyBindSneak")
	public Keybinding keyBindSneak();
	
	@FieldMapping("keyBindJump")
	public Keybinding keyBindJump();
}
