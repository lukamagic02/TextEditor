package hr.fer.ooup.plugins;

import java.lang.reflect.Constructor;

public class PluginFactory {

    private static ClassLoader loader;

    public static Plugin newInstance(String name) {
        try {
            if (loader == null) {
                loader = PluginFactory.class.getClassLoader();
            }

            Class<Plugin> clazz = (Class<Plugin>)loader.loadClass("hr.fer.ooup.plugins.implementations." + name);
            Constructor<?> constructor = clazz.getConstructor();

            return (Plugin)constructor.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}