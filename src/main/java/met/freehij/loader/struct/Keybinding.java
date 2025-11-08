package met.freehij.loader.struct;

import met.freehij.loader.mappings.ClassMapping;
import met.freehij.loader.mappings.FieldMapping;

@ClassMapping("net/minecraft/src/KeyBinding")
public interface Keybinding {
	@FieldMapping("keyDescription")
	public String keyDescription();
	
	@FieldMapping("keyCode")
	public int keyCode();
}
