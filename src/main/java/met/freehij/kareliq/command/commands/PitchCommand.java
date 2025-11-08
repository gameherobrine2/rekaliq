package met.freehij.kareliq.command.commands;

import met.freehij.kareliq.command.Command;
import met.freehij.loader.struct.Minecraft;

public class PitchCommand extends Command {
    public PitchCommand() {
        super("pitch", "<value>");
    }

    @Override
    public boolean execute(String[] args) {
        if (args.length < 1) return false;
        try {
        	Minecraft.getMinecraft().thePlayer().rotationPitch(Float.parseFloat(args[0]));
        } catch (NumberFormatException ignored) {
            return false;
        }
        return true;
    }
}
