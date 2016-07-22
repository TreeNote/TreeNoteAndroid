package de.treenote.util;

import java.util.Arrays;

import mockit.Deencapsulation;

/**
 * Ansammlung von nützlichen Methoden für Tests
 */
public class TestUtil {

    public static Object invoke(Object objectWithMethod, String methodName, Object... parameters) {
        return Deencapsulation.invoke(objectWithMethod, methodName, parameters);
    }

    /**
     * Erzeugt ein Array aus den gegebenen Objekten
     * erspart den aufruf: new int[]{1, 2, 3}
     */
    public static <E> E[] array(E... elements) {
        return elements;
    }

    /**
     * Legt das übergebene Object in ein Objekt Array.
     * Diese Methode wird benötigt, falls bei varArgs ganze Arrays übergeben werden müssen
     * Andernfalls würden die Einträge im Array als einzelne varArg Elemente interpretiert werden
     */
    public static Object[] wrapInArray(Object object) {
        return new Object[]{object};
    }

    public static int[] toPrimitiveArray(Integer[] integerObjectArray) {
        int[] primitiveInts = new int[integerObjectArray.length];

        for (int i = 0, integerObjectArrayLength = integerObjectArray.length; i < integerObjectArrayLength; i++) {
            Integer integerObject = integerObjectArray[i];
            primitiveInts[i] = integerObject;
        }
        return primitiveInts;
    }

    public static Integer[] toObjectArray(int[] integerObjectArray) {
        Integer[] objectInts = new Integer[integerObjectArray.length];

        for (int i = 0, integerObjectArrayLength = integerObjectArray.length; i < integerObjectArrayLength; i++) {
            Integer primitiveInt = integerObjectArray[i];
            objectInts[i] = primitiveInt;
        }
        return objectInts;
    }
}
