package com.scorpio.validator.util;

public class ReflectionUtils {

    public static boolean isClassImplementingInterface(Class<?> clazz, Class<?> interfaceClass) {
        if (!interfaceClass.isInterface()) {
            return false;
        }

        Class<?>[] interfaces = clazz.getInterfaces();

        for (Class<?> interfaceClazz : interfaces) {
            if (interfaceClazz.equals(interfaceClass)) {
                return true;
            }
        }

        return false;
    }

    public static boolean classExists(String className) {
        try {
            return Class.forName(className) != null;
        } catch (ClassNotFoundException ex) {
            return false;
        }
    }

}
