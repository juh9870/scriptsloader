package com.juh9870.scriptsloader;

import cpw.mods.fml.relauncher.Side;
import ftb.lib.api.EventFTBModeSet;
import ftb.lib.mod.FTBLib;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CommandSL extends CommandBase {
    @Override
    public void processCommand(ICommandSender ics, String[] args) {
        int a = 0;
        IChatComponent c = this.execCmd(ics, args);
        if (c != null) {
            ics.addChatMessage(c);
        } else {
            throw new IllegalArgumentException(args.length > 0 ? args[0] : "No arguments!");
        }
    }

    public int getRequiredPermissionLevel() {
        return 2;
    }

    public IChatComponent execCmd(ICommandSender ics, String[] args) {
        ChatComponentTranslation c;
        try {
            if (args[0].equals("add")) {
                if (args[1] != null) {
                    SLMain.addFolder(args[1]);
                    SLMain.INSTANCE.onReloaded(new EventFTBModeSet(Side.SERVER, FTBLib.getAllModes(), FTBLib.getMode()));
                    c=new ChatComponentTranslation("scriptsloader:added", args[1]);
                    return c;
                } else {
                    c=new ChatComponentTranslation("scriptsloader:nofolder", args[1]);
                    c.getChatStyle().setColor(EnumChatFormatting.RED);
                    return c;
                }
            }
            if (args[0].equals("remove")) {
                if (args[1] != null) {
                    SLMain.removeFolder(args[1]);
                    SLMain.INSTANCE.onReloaded(new EventFTBModeSet(Side.SERVER, FTBLib.getAllModes(), FTBLib.getMode()));
                    c=new ChatComponentTranslation("scriptsloader:removed", args[1]);
                    return c;
                } else {
                    c=new ChatComponentTranslation("scriptsloader:nofolder", args[1]);
                    c.getChatStyle().setColor(EnumChatFormatting.RED);
                    return c;
                }
            }
        } catch (ArrayIndexOutOfBoundsException ignored){
        }
        c=new ChatComponentTranslation("scriptsloader:noarguments");
        c.getChatStyle().setColor(EnumChatFormatting.RED);
        return c;

    }

    @Override
    public String getCommandUsage(ICommandSender ics) {
        return "/scriploader [add | remove] [folder]";
    }

    @Override
    public String getCommandName() {
        return "scriploader";
    }

    public List addTabCompletionOptions(ICommandSender ics, String[] args) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, new String[]{"add", "remove"});
        } else {
            return args.length == 2 && args[0].equals("remove") ? getListOfStringsFromIterableMatchingLastWord(args, SLMain.folders()) : null;
        }
    }

    @Override
    public List getCommandAliases() {
        return Collections.singletonList("sl");
    }
}
