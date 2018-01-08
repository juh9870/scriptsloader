package com.juh9870.scriptsloader;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import ftb.lib.CommandFTBLMode;
import ftb.lib.api.EventFTBModeSet;
import ftb.lib.mod.FTBLib;
import ftb.lib.mod.FTBLibMod;
import minetweaker.MineTweakerAPI;
import minetweaker.MineTweakerImplementationAPI;
import minetweaker.mc1710.util.MineTweakerHacks;
import minetweaker.runtime.IScriptProvider;
import minetweaker.runtime.providers.ScriptProviderCascade;
import minetweaker.runtime.providers.ScriptProviderDirectory;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import scala.actors.threadpool.Arrays;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

@Mod(modid = "scriptsloader", name = "Scripts loader", version = "1.0.0", dependencies = "required-after:MineTweaker3;required-after:FTBL", useMetadata = false)
public class SLMain {

    private static HashSet<String> folders = new HashSet<String>();
    private static String filedata;
    private static File savefile;
    private static final String SPLITTER = "\\+";
    private static File scriptsDir;

    @Mod.Instance("scriptsloader")
    public static SLMain INSTANCE;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent e) {
        scriptsDir=new File(FTBLib.folderModpack,"scriptsloader");
        if (!scriptsDir.exists()) {
            scriptsDir.mkdirs();
        }
    }

    private void pause(){
        int a = 1+3;
    }

    @Mod.EventHandler
    public void onStarted(FMLServerStartedEvent e){
        if (!FMLCommonHandler.instance().getEffectiveSide().isClient() && MinecraftServer.getServer() != null) {
            World w = MinecraftServer.getServer().getEntityWorld();
            pause();
            if (w != null) {
                try {
                    savefile = new File(w.getSaveHandler().getWorldDirectory(), "scriptloader.txt");
                    if (!savefile.exists()) {
                        savefile.createNewFile();
                    }
                    BufferedReader reader = new BufferedReader(new FileReader(savefile));
                    if (reader.ready()) {
                        try {
                            folders=toHashSet(reader.readLine().trim());
                        } catch (Exception ex2) {
                            ex2.printStackTrace();
                        }
                    }

                    reader.close();
                } catch (Exception ex){
                    ex.printStackTrace();
                }
            }
            onReloaded(new EventFTBModeSet(Side.SERVER, FTBLib.getAllModes(), FTBLib.getMode()));
        }
    }

    @Mod.EventHandler
    public void onServerStarting(FMLServerStartingEvent e) {
        e.registerServerCommand(new CommandSL());
    }

    public static void save(){
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(savefile));
            writer.write(hashSetToString(folders));
            writer.close();
        } catch (Exception var1) {
            var1.printStackTrace();
        }
    }

    private static HashSet<String> toHashSet(String s){
        HashSet<String> hs = new HashSet<String>();
        String[] items = s.split(SPLITTER);
        hs.addAll(Arrays.asList(items));
        return hs;
    }

    private static String hashSetToString(HashSet<String> hs){
        String s = "";
        for(String st : hs) {
            s+=st+SPLITTER;
        }
        if (s.length()>0)
            s = s.substring(0, s.length() - 1);
        return s;
    }

    public static List<String> folders(){
        return Arrays.asList(folders.toArray(new String[0]));
    }

    public static void addFolder(String s){
        s=s.replaceAll("^([^a-zA-Z0-9/][*><?\\\"|:]*)$","");
        folders.add(s);
    }

    public static void removeFolder(String s){
        s=s.replaceAll("^([^a-zA-Z0-9/][*><?\\\"|:]*)$","");
        folders.remove(s);
    }

    public static ArrayList<IScriptProvider> reload(ArrayList<IScriptProvider> isp){
        List<String> folds = folders();
        Collections.sort(folds);
        for (String folder : folds){
            File curFold;
            if (folder.contains("/")){
                String[] path = folder.split("/");

                curFold=new File(scriptsDir,path[0]);
                if (!curFold.exists()) curFold.mkdirs();
                if (path.length>1)
                for (int i = 1; i<path.length;i++){
                    curFold=new File(curFold,path[i]);
                    if (!curFold.exists()) curFold.mkdirs();
                }
            } else {
                curFold = new File(scriptsDir, folder);
                if (!curFold.exists()) curFold.mkdirs();
            }
            isp.add(new ScriptProviderDirectory(curFold));
        }

        return isp;
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onReloaded(EventFTBModeSet e)
    {
        if (!e.getFile("scripts").exists())
        {
            e.getFile("scripts").mkdirs();
        }
        if (!e.getCommonFile("scripts").exists()) {
            e.getCommonFile("scripts").mkdirs();
        }
        MineTweakerAPI.tweaker.rollback();

        ArrayList<IScriptProvider> providers = new ArrayList<IScriptProvider>();

        providers.add(new ScriptProviderDirectory(e.getCommonFile("scripts")));
        providers.add(new ScriptProviderDirectory(e.getFile("scripts")));

        // Don't have to mkdir these because MT does that for us already.
        providers.add(new ScriptProviderDirectory(new File("scripts")));

        providers=reload(providers);

        if (e.side.isServer()) {
            providers.add(new ScriptProviderDirectory(new File(MineTweakerHacks.getWorldDirectory(MinecraftServer.getServer()), "scripts")));
        }

        MineTweakerImplementationAPI.setScriptProvider(new ScriptProviderCascade(providers.toArray(new IScriptProvider[providers.size()])));
        MineTweakerImplementationAPI.reload();
        save();
    }
}
