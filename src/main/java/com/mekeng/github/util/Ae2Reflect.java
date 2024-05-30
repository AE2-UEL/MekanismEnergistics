package com.mekeng.github.util;

import appeng.api.parts.IPartModel;
import appeng.container.ContainerOpenContext;
import appeng.parts.p2p.PartP2PTunnel;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

public class Ae2Reflect {

    private static final Field fContainerOpenContext_x;
    private static final Field fContainerOpenContext_y;
    private static final Field fContainerOpenContext_z;
    private static final Method mPartP2PTunnel_setOutput;
    private static final Method mP2PModels_getModel;
    private static final Method mP2PModels_getModels;
    private static final Constructor<?> cP2PModels;

    static {
        try {
            fContainerOpenContext_x = reflectField(ContainerOpenContext.class, "x");
            fContainerOpenContext_y = reflectField(ContainerOpenContext.class, "y");
            fContainerOpenContext_z = reflectField(ContainerOpenContext.class, "z");
            mPartP2PTunnel_setOutput = reflectMethod(PartP2PTunnel.class, "setOutput", boolean.class);
            mP2PModels_getModel = reflectMethod(Class.forName("appeng.parts.p2p.P2PModels"), "getModel", boolean.class, boolean.class);
            mP2PModels_getModels = reflectMethod(Class.forName("appeng.parts.p2p.P2PModels"), "getModels");
            cP2PModels = Class.forName("appeng.parts.p2p.P2PModels").getDeclaredConstructor(String.class);
            cP2PModels.setAccessible(true);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize AE2 reflection hacks!", e);
        }
    }

    public static Method reflectMethod(Class<?> owner, String name, Class<?>... paramTypes) throws NoSuchMethodException {
        return reflectMethod(owner, new String[]{name}, paramTypes);
    }

    @SuppressWarnings("all")
    public static Method reflectMethod(Class<?> owner, String[] names, Class<?>... paramTypes) throws NoSuchMethodException {
        Method m = null;
        for (String name : names) {
            try {
                m = owner.getDeclaredMethod(name, paramTypes);
                if (m != null) break;
            }
            catch (NoSuchMethodException ignore) {
            }
        }
        if (m == null) throw new NoSuchMethodException("Can't find field from " + Arrays.toString(names));
        m.setAccessible(true);
        return m;
    }

    @SuppressWarnings("all")
    public static Field reflectField(Class<?> owner, String ...names) throws NoSuchFieldException {
        Field f = null;
        for (String name : names) {
            try {
                f = owner.getDeclaredField(name);
                if (f != null) break;
            }
            catch (NoSuchFieldException ignore) {
            }
        }
        if (f == null) throw new NoSuchFieldException("Can't find field from " + Arrays.toString(names));
        f.setAccessible(true);
        return f;
    }

    @SuppressWarnings("unchecked")
    public static <T> T readField(Object owner, Field field) {
        try {
            return (T)field.get(owner);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to read field: " + field);
        }
    }

    public static void writeField(Object owner, Field field, Object value) {
        try {
            field.set(owner, value);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to write field: " + field);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T create(Constructor<?> c, Object... para) {
        try {
            return (T) c.newInstance(para);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create instance: " + c);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T executeMethod(Object owner, Method method, Object... para) {
        try {
            return (T) method.invoke(owner, para);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to execute method: " + method);
        }
    }

    public static int getContextX(ContainerOpenContext owner) {
        return readField(owner, fContainerOpenContext_x);
    }

    public static int getContextY(ContainerOpenContext owner) {
        return readField(owner, fContainerOpenContext_y);
    }

    public static int getContextZ(ContainerOpenContext owner) {
        return readField(owner, fContainerOpenContext_z);
    }

    public static Object createP2PModel(String name) {
        return create(cP2PModels, name);
    }

    public static IPartModel getP2PModel(Object owner, boolean hasPower, boolean hasChannel) {
        return executeMethod(owner, mP2PModels_getModel, hasPower, hasChannel);
    }

    public static List<IPartModel> getP2PModel(Object owner) {
        return executeMethod(owner, mP2PModels_getModels);
    }

    public static void setP2POutput(Object owner, boolean value) {
        executeMethod(owner, mPartP2PTunnel_setOutput, value);
    }

}
