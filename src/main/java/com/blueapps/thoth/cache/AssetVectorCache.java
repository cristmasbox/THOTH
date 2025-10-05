package com.blueapps.thoth.cache;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.Drawable;

import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * AssetVectorCache
 * <p>
 * Utility class to load VectorDrawableCompat instances from the assets/ folder efficiently.
 * <p>
 * Motivation:
 * - Normally, calling VectorDrawableCompat.createFromXml() on an asset every time is slow.
 * - It requires opening the file, parsing the XML, and building a new drawable object.
 * - Doing this repeatedly for many icons (e.g., in a library) causes lag.
 * <p>
 * Optimization:
 * - We load and parse each XML only once.
 * - We cache its Drawable.ConstantState (the "blueprint" of the drawable).
 * - On future requests, we clone from the cached ConstantState instead of reparsing XML.
 * - Cloning is very cheap and fast compared to reloading XML.
 */
public class AssetVectorCache {

    // Cache to store parsed drawable "blueprints".
    // Key: the asset file path (e.g., "vectors/icon.xml")
    // Value: ConstantState of the parsed drawable (can generate clones)
    private static final Map<String, Drawable.ConstantState> cache = new HashMap<>();

    /**
     * Get a VectorDrawableCompat from assets, optimized with caching.
     *
     * @param context   Application or View context (used to access assets and resources).
     * @param fileName  Path to the asset XML file (e.g., "vectors/my_icon.xml").
     * @return          A fresh VectorDrawableCompat instance ready to use, or null if failed.
     */
    public static Drawable get(Context context, String fileName) {
        // 1. First check if we already parsed this file before.
        Drawable.ConstantState state = cache.get(fileName);
        if (state != null) {
            // If cached: clone a new drawable from its ConstantState.
            // This avoids reparsing XML and is very fast.
            return state.newDrawable(context.getResources()).mutate();
        }

        // 2. If not cached yet, we must parse the XML from assets.
        try {
            // Open the XML file as an XmlResourceParser from assets.
            XmlResourceParser parser = context.getAssets().openXmlResourceParser(fileName);

            // Parse it into a VectorDrawableCompat instance.
            Drawable d = VectorDrawableCompat.createFromXml(context.getResources(), parser);

            // If parsing succeeded:
            if (d != null) {
                // Cache its ConstantState for future cheap cloning.
                cache.put(fileName, d.getConstantState());
                return d;
            }
        } catch (Exception e) {
            // If something goes wrong (file missing, bad XML, etc.), print error.
            e.printStackTrace();
        }

        // 3. Return null if loading failed.
        return null;
    }

    /**
     * Preload all vector drawables from a folder in assets into cache.
     *
     * This prevents delays later when accessing them,
     * because parsing is done upfront (e.g., at app startup).
     *
     * @param context   Application context
     * @param folder    Path to the folder in assets (e.g., "vectors")
     */
    public static void preloadFolder(Context context, String folder) {
        try {
            // List all files inside the specified assets folder.
            String[] files = context.getAssets().list(folder);
            if (files != null) {
                for (String f : files) {
                    // For each file, call get() to load and cache it.
                    get(context, folder + "/" + f);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
