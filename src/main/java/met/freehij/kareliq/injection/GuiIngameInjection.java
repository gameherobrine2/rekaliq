package met.freehij.kareliq.injection;

import met.freehij.kareliq.ClientMain;
import met.freehij.kareliq.module.*;
import met.freehij.kareliq.module.Module;
import met.freehij.kareliq.module.client.ModuleList;
import met.freehij.kareliq.module.client.TabGui;
import met.freehij.kareliq.util.NotificationUtils;
import met.freehij.loader.annotation.Inject;
import met.freehij.loader.annotation.Injection;
import met.freehij.loader.constant.At;
import met.freehij.loader.mappings.Creator;
import met.freehij.loader.struct.FontRenderer;
import met.freehij.loader.struct.GuiIngame;
import met.freehij.loader.struct.Minecraft;
import met.freehij.loader.util.InjectionHelper;
import met.freehij.loader.util.Reflector;

import org.lwjgl.input.Keyboard;

import java.awt.*;

import static met.freehij.kareliq.util.Utils.createScaledResolution;

@Injection("GuiIngame")
public class GuiIngameInjection {
    private static final int TEXT_COLOR = 0xFFFFFF;
    private static final int BACKGROUND_COLOR = 0x80000000;
    private static final int MODULE_HEIGHT = 11;
    private static final float HUE_CYCLE_SPEED = 4.0F;
    private static final int HUE_OFFSET_STEP = 100;

    private static int selectedCategory = 0;
    private static int selectedModule = 0;
    private static int selectedSetting = 0;
    private static boolean showModules = false;
    private static boolean showSettings = false;
    private static Module listeningKeyBind = null;
    private static SettingSlider listeningKeySlider = null;

    @Inject(method = "renderGameOverlay", at = At.RETURN)
    public static void renderGameOverlay(InjectionHelper helper) {
        try {
            Reflector mc_ = InjectionHelper.getMinecraft();
            Minecraft mc = Minecraft.getMinecraft();

            if (mc.isDebugInfoEnabled()) return;

            FontRenderer fontRenderer = mc.fontRenderer();
            Reflector scaledResolution = createScaledResolution(mc_);
            int screenWidth = getScaledWidth(scaledResolution);

            renderClientInfo(fontRenderer, screenWidth);
            NotificationUtils.drawNotifications(helper);

            if (ModuleList.INSTANCE.isToggled()) {
                renderModuleList(helper, fontRenderer, screenWidth);
            }

            if (TabGui.INSTANCE.isToggled()) {
                renderTabGui(helper, fontRenderer);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static double round(double value) {
        return Math.round(value * 10000000000.0) / 10000000000.0;
    }

    public static void handleKeyPress(int keyCode) {
        if (!TabGui.INSTANCE.isToggled()) return;
        if (listeningKeyBind != null) {
            if (keyCode == Keyboard.KEY_ESCAPE) {
                listeningKeyBind.setKeyBind(Keyboard.KEY_NONE);
            } else {
                listeningKeyBind.setKeyBind(keyCode);
            }
            NotificationUtils.notifications.add(new NotificationUtils.Notification(listeningKeyBind.getName() + " is now bound to " + Keyboard.getKeyName(listeningKeyBind.getKeyBind()), 3000));
            listeningKeyBind = null;
            return;
        }
        if (listeningKeySlider != null) {
            if (keyCode == Keyboard.KEY_ESCAPE) {
                listeningKeySlider = null;
            } else if (keyCode == Keyboard.KEY_LEFT) {
                double value = (double)listeningKeySlider.getValue();
                value = value - (value - listeningKeySlider.getStep() >= listeningKeySlider.getMin() ? listeningKeySlider.getStep() : 0.D);
                if (value < listeningKeySlider.getMin()) value = listeningKeySlider.getMin();
                listeningKeySlider.setValue(round(value));
                Module.Category.values()[selectedCategory].getModules()[selectedModule].onSettingChange(listeningKeySlider);
                return;
            } else if (keyCode == Keyboard.KEY_RIGHT) {
                double value = (double)listeningKeySlider.getValue();
                value = value + (value + listeningKeySlider.getStep() <= listeningKeySlider.getMax() ? listeningKeySlider.getStep() : 0.D);
                if (value > listeningKeySlider.getMax()) value = listeningKeySlider.getMax();
                listeningKeySlider.setValue(round(value));
                Module.Category.values()[selectedCategory].getModules()[selectedModule].onSettingChange(listeningKeySlider);
                return;
            }
        }
        switch (keyCode) {
            case Keyboard.KEY_UP:
                if (showSettings) {
                    selectedSetting--;
                } else if (showModules) {
                    selectedModule--;
                } else {
                    selectedCategory--;
                }
                break;
            case Keyboard.KEY_DOWN:
                if (showSettings) {
                    selectedSetting++;
                } else if (showModules) {
                    selectedModule++;
                } else {
                    selectedCategory++;
                }
                break;
            case Keyboard.KEY_RETURN:
                if (!showModules) {
                    if (Module.Category.values()[selectedCategory].getModules().length == 0) return;
                    showModules = true;
                    selectedModule = 0;
                } else if (!showSettings) {
                    Module.Category.values()[selectedCategory].getModules()[selectedModule].toggle();
                } else {
                    Module module = Module.Category.values()[selectedCategory].getModules()[selectedModule];
                    if (module.getSettings().length < 1 || selectedSetting > module.getSettings().length - 1) {
                        listeningKeyBind = module;
                    } else if (module.getSettings()[selectedSetting] instanceof SettingSlider) {
                        if (listeningKeySlider == null) {
                            listeningKeySlider = (SettingSlider)module.getSettings()[selectedSetting];
                        } else {
                            NotificationUtils.notifications.add(new NotificationUtils.Notification("Set " +
                                    listeningKeySlider.getName() + " in " + module.getName() + " to " + listeningKeySlider.getDouble(), 3000));
                            listeningKeySlider = null;
                        }
                    } else {
                        module.getSettings()[selectedSetting].switchValue();
                        if (module.getSettings()[selectedSetting] instanceof SettingButton) {
                            NotificationUtils.notifications.add(new NotificationUtils.Notification("Set " +
                                    module.getSettings()[selectedSetting].getName() + " in " + module.getName() + " to " + (module.getSettings()[selectedSetting].getBoolean() ? "§aON" : "§cOFF"), 3000));
                        } else {
                            NotificationUtils.notifications.add(new NotificationUtils.Notification("Set " +
                                    module.getSettings()[selectedSetting].getName() + " in " + module.getName() + " to " + ((SettingModes) module.getSettings()[selectedSetting]).getCurrentMode(), 3000));
                        }
                    }
                }
                break;
            case Keyboard.KEY_RIGHT:
                if (!showModules){
                    if (Module.Category.values()[selectedCategory].getModules().length == 0) return;
                    showModules = true;
                    selectedModule = 0;
                } else if(!showSettings) {
                    showSettings = true;
                    selectedSetting = 0;
                }
                break;
            case Keyboard.KEY_LEFT:
                if (showSettings) {
                    showSettings = false;
                } else if (showModules){
                    showModules = false;
                }
        }
        if (selectedCategory < 0) {
            selectedCategory = Module.Category.values().length - 1;
        } else if (selectedCategory > Module.Category.values().length - 1) {
            selectedCategory = 0;
        }
        if (showModules) {
            if (selectedModule < 0) {
                selectedModule = Module.Category.values()[selectedCategory].getModules().length - 1;
            } else if (selectedModule > Module.Category.values()[selectedCategory].getModules().length - 1) {
                selectedModule = 0;
            }
        }
        if (showSettings) {
            if (selectedSetting < 0) {
                selectedSetting = Module.Category.values()[selectedCategory].getModules()[selectedModule].getSettings().length;
            } else if (selectedSetting > Module.Category.values()[selectedCategory].getModules()[selectedModule].getSettings().length) {
                selectedSetting = 0;
            }
        }
    }

    private static void renderTabGui(InjectionHelper helper, FontRenderer fontRenderer) {
    	GuiIngame dis = Creator.proxy(helper.getSelf().get(), GuiIngame.class);
        int finalWidth = 0;
        for (Module.Category category : Module.Category.values()) {
            int length = fontRenderer.getStringWidth(category.getName());
            if (length > finalWidth) {
                finalWidth = length;
            }
        }

        int y = 22;
        if (ClientMain.note.trim().isEmpty()) y -= 10;
        for (Module.Category category : Module.Category.values()) {
        	dis.drawRect(2, y, finalWidth + 6, y + 11, category.ordinal() == selectedCategory ? 0xA0000000 : 0xD0000000);
            fontRenderer.drawStringWithShadow(category.getName(), 4, y + 2,
                    category.ordinal() == selectedCategory ? Color.HSBtoRGB(calculateHue(0), 1f, 1f) : Integer.MAX_VALUE);
            if (category.ordinal() == selectedCategory && showModules) {
                int y2 = y;
                int finalWidth2 = finalWidth;
                for (Module module : category.getModules()) {
                    int length = fontRenderer.getStringWidth(module.getName()) + finalWidth;
                    if (length > finalWidth2) {
                        finalWidth2 = length;
                    }
                }
                int currModule = 0;
                for (Module module : category.getModules()) {
                	dis.drawRect(finalWidth + 6, y2, finalWidth2 + 10, y2 + 11, selectedModule == currModule ? 0xA0000000 : 0xD0000000);
                    fontRenderer.drawStringWithShadow(module.getName(), finalWidth + 8, y2 + 2,
                            selectedModule == currModule ? Color.HSBtoRGB(calculateHue(0), 1f, 1f) : Integer.MAX_VALUE);
                    if (currModule == selectedModule && showSettings) {
                        int y3 = y2;
                        int currSetting = 0;
                        int finalWidth3 = finalWidth2;
                        for (Setting setting : module.getSettings()) {
                            String stuff = "";
                            if (setting instanceof SettingButton) {
                                stuff = setting.getName() + (setting.getBoolean() ? ": ON" : ": OFF");
                            } else if (setting instanceof SettingModes) {
                                stuff = setting.getName() + ": " + ((SettingModes) setting).getCurrentMode();
                            }
                            int length = fontRenderer.getStringWidth(stuff) + finalWidth2;
                            if (length > finalWidth3) {
                                finalWidth3 = length;
                            }
                        }
                        String bindString = "Bind: " + (listeningKeyBind != null && listeningKeyBind.equals(module) ? "..." : Keyboard.getKeyName(module.getKeyBind()));
                        int bindStringLen = fontRenderer.getStringWidth(bindString) + finalWidth2;
                        if (bindStringLen > finalWidth3) finalWidth3 = bindStringLen;
                        finalWidth3 += 4;
                        for (Setting setting : module.getSettings()) {
                        	dis.drawRect(finalWidth2 + 10, y3, finalWidth3 + 11, y3 + 11, selectedSetting == currSetting ? 0xA0000000 : 0xD0000000);
                            if (setting instanceof SettingSlider) {
                                SettingSlider slider = (SettingSlider) setting;
                                int settingLen = finalWidth3 - finalWidth2;
                                double normalized = (slider.getDouble() - slider.getMin()) / (slider.getMax() - slider.getMin());
                                normalized = Math.max(0.0, Math.min(1.0, normalized));
                                int pixelOffset = (int) (normalized * settingLen);
                                int pos = finalWidth2 + pixelOffset + 10;
                                dis.drawRect(pos, y3, pos+1, y3 + 11, slider == listeningKeySlider ? 0xff00ff00 : 0xffffffff);
                                fontRenderer.drawStringWithShadow(
                                        String.valueOf(slider.getDouble()),
                                        finalWidth2 + 12, y3 + 2, selectedSetting == currSetting ? Color.HSBtoRGB(calculateHue(0), 1f, 1f) : Integer.MAX_VALUE);
                            } else {
                                String stuff = "";
                                if (setting instanceof SettingButton) {
                                    stuff = setting.getName() + (setting.getBoolean() ? ": ON" : ": OFF");
                                } else if (setting instanceof SettingModes) {
                                    stuff = setting.getName() + ": " + ((SettingModes) setting).getCurrentMode();
                                }
                                fontRenderer.drawStringWithShadow(stuff, finalWidth2 + 12, y3 + 2,
                                        selectedSetting == currSetting ? Color.HSBtoRGB(calculateHue(0), 1f, 1f) : Integer.MAX_VALUE);
                            }
                            currSetting++;
                            y3 += MODULE_HEIGHT;
                        }
                        dis.drawRect(finalWidth2 + 10, y3, finalWidth3 + 11, y3 + 11, selectedSetting == currSetting ? 0xA0000000 : 0xD0000000);
                        fontRenderer.drawStringWithShadow(bindString, finalWidth2 + 12, y3 + 2, selectedSetting == currSetting ? Color.HSBtoRGB(calculateHue(0), 1f, 1f) : Integer.MAX_VALUE);
                    }
                    y2 += MODULE_HEIGHT;
                    currModule++;
                }
            }
            y += MODULE_HEIGHT;
        }
    }

    private static int getScaledWidth(Reflector scaledResolution) {
        return (int) scaledResolution.invoke("getScaledWidth").get();
    }

    private static void renderClientInfo(FontRenderer fontRenderer, int screenWidth) {
    	fontRenderer.drawStringWithShadow(ClientMain.name + " §f" + ClientMain.version, 2, 2, TEXT_COLOR);
    	fontRenderer.drawStringWithShadow(ClientMain.note, 2, 12, TEXT_COLOR);
    }

    private static void renderModuleList(InjectionHelper helper, FontRenderer fontRenderer, int screenWidth) {
        int yPos = 0;
        int lastXEnd = 0;
        boolean isFirstModule = true;
        int hueOffset = 0;
        GuiIngame dis = Creator.proxy(helper.getSelf().get(), GuiIngame.class);
        for (Module module : ClientMain.modules) {
            if (!module.isToggled()) continue;

            int textWidth = fontRenderer.getStringWidth(module.getName());
            int xPos = screenWidth - textWidth - 2;

            if (ModuleList.INSTANCE.getSettings()[2].getBoolean()) dis.drawRect(xPos - 2, yPos, screenWidth, yPos + MODULE_HEIGHT, BACKGROUND_COLOR);

            float hue = calculateHue(hueOffset);
            int color = Color.HSBtoRGB(hue, 1f, 1f);

            switch (ModuleList.INSTANCE.getSettings()[1].getInt()) {
                case 0:
                	dis.drawRect(
                            xPos - 3, yPos,
                            xPos - 2, yPos + MODULE_HEIGHT,
                            ModuleList.INSTANCE.getSettings()[0].getInt() == 0 ? color : 0xffffffff
                    );

                    if (!isFirstModule) {
                    	dis.drawRect(
                                lastXEnd - 3, yPos,
                                xPos - 2, yPos + 1,
                                ModuleList.INSTANCE.getSettings()[0].getInt() == 0 ? color : 0xffffffff
                        );
                    }
                    break;
                case 1:
                	dis.drawRect(
                            screenWidth - 1, yPos,
                            screenWidth, yPos +MODULE_HEIGHT,
                            ModuleList.INSTANCE.getSettings()[0].getInt() == 0 ? color : 0xffffffff);
                    break;
                case 2:
                	dis.drawRect(
                            xPos - 1, yPos,
                            xPos - 2, yPos +MODULE_HEIGHT,
                            ModuleList.INSTANCE.getSettings()[0].getInt() == 0 ? color : 0xffffffff);
                    break;
            }

            fontRenderer.drawStringWithShadow(module.getName(), xPos, yPos + 2,
                    ModuleList.INSTANCE.getSettings()[0].getInt() == 0 ? color : module.getCategory().getColor());

            yPos += MODULE_HEIGHT;
            lastXEnd = xPos;
            isFirstModule = false;
            hueOffset += HUE_OFFSET_STEP;
        }

        if (ModuleList.INSTANCE.getSettings()[1].getInt() == 0 && !isFirstModule) {
            float hue = calculateHue(hueOffset);
            dis.drawRect(
                    lastXEnd - 3, yPos,
                    screenWidth, yPos + 1,
                    ModuleList.INSTANCE.getSettings()[0].getInt() == 0 ? Color.HSBtoRGB(hue, 1f, 1f) : 0xffffffff
            );
        }
    }

    private static float calculateHue(int offset) {
        return ((System.currentTimeMillis() + offset) % (long) (HUE_CYCLE_SPEED * 1000)) / (HUE_CYCLE_SPEED * 1000.0F);
    }
}
