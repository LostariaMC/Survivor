package fr.lumin0u.survivor.utils;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

public class WorldUtils {
    public WorldUtils() {
    }

    public static boolean unloadWorld(String worldName) {
        Bukkit.getServer().unloadWorld(worldName, false);
        return true;
    }

    public static boolean deleteWorld(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();

            for(int i = 0; i < files.length; ++i) {
                if (files[i].isDirectory()) {
                    deleteWorld(files[i]);
                } else {
                    files[i].delete();
                }
            }
        }

        return path.delete();
    }

    public static World loadWorld(String worldName) {
        WorldCreator worldCreater = new WorldCreator(worldName);
        Bukkit.getServer().createWorld(worldCreater);
        return Bukkit.getWorld(worldName);
    }

    public static void copyWorld(File source, File target) {
        try {
            ArrayList<String> ignore = new ArrayList(Arrays.asList("uid.dat", "session.dat"));
            if (!ignore.contains(source.getName())) {
                int length;
                if (source.isDirectory()) {
                    if (!target.exists()) {
                        target.mkdirs();
                    }

                    String[] files = source.list();
                    String[] var4 = files;
                    int var5 = files.length;

                    for(length = 0; length < var5; ++length) {
                        String file = var4[length];
                        File srcFile = new File(source, file);
                        File destFile = new File(target, file);
                        copyWorld(srcFile, destFile);
                    }
                } else {
                    InputStream in = new FileInputStream(source);
                    OutputStream out = new FileOutputStream(target);
                    byte[] buffer = new byte[1024];

                    while((length = in.read(buffer)) > 0) {
                        out.write(buffer, 0, length);
                    }

                    in.close();
                    out.close();
                }
            }
        } catch (IOException var10) {
            var10.printStackTrace();
        }

    }
}
