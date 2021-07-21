/*
 * Copyright (c) 2003, 2014, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package weaver.micro.devkit.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;

class AnnotationInvocationHandler<T extends Annotation> implements InvocationHandler {

    final Class<T> annotationType;

    /**
     * @see <a href="https://docs.oracle.com/javase/specs/jls/se8/html/jls-9.html#jls-9.6.1">
     *         Annotation Type Elements
     *         </a>
     */
    final Map<String, Object> memberValues;

    /**
     * Constructor
     */
    AnnotationInvocationHandler(Class<T> type, Map<String, Object> mapping) {
        this.annotationType = type;
        this.memberValues = AnnotationUtils.checkReturnMemberValues(type, mapping);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String member = method.getName();
        Class<?>[] paramTypes = method.getParameterTypes();

        // Handle Object and Annotation methods
        if (member.equals("equals")
                && paramTypes.length == 1
                && paramTypes[0] == Object.class)
            return this.equalsImpl(args[0]);
        if (paramTypes.length != 0)
            throw new AssertionError("Too many parameters for an annotation method");

        // parameterless
        if ("toString".equals(member)) {
            return this.toStringImpl();
        } else if ("hashCode".equals(member)) {
            return this.hashCodeImpl();
        } else if ("annotationType".equals(member)) {
            return this.annotationType;
        }

        // Handle annotation member accessors
        Object result = this.memberValues.get(member);

        if (result.getClass().isArray() && Array.getLength(result) != 0)
            result = cloneArray(result);

        return result;
    }

    private int hashCodeImpl() {
        int result = 0;
        for (Map.Entry<String, Object> e : this.memberValues.entrySet()) {
            result += (127 * e.getKey().hashCode()) ^
                    memberValueHashCode(e.getValue());
        }
        return result;
    }

    private String toStringImpl() {
        StringBuilder result = new StringBuilder(128);
        result.append('@');
        result.append(this.annotationType.getName());
        result.append('(');
        boolean firstMember = true;
        for (Map.Entry<String, Object> e : this.memberValues.entrySet()) {
            if (firstMember)
                firstMember = false;
            else
                result.append(", ");

            result.append(e.getKey());
            result.append('=');
            result.append(memberValueToString(e.getValue()));
        }
        result.append(')');
        return result.toString();
    }

    private boolean equalsImpl(Object o) {
        if (!this.annotationType.isInstance(o))
            return false;

        Method[] methods = this.annotationType.getDeclaredMethods();
        AccessibleObject.setAccessible(methods, true);
        for (Method memberMethod : methods) {
            String memberName = memberMethod.getName();
            Object ourValue = this.memberValues.get(memberName);
            Object itsValue;
            try {
                itsValue = memberMethod.invoke(o);
            } catch (InvocationTargetException e) {
                return false;
            } catch (IllegalAccessException e) {
                throw new AssertionError(e);
            }

            if (!memberValueEquals(ourValue, itsValue))
                return false;
        }
        return true;
    }

    private static String memberValueToString(Object value) {
        Class<?> type = value.getClass();
        if (!type.isArray())
            return value.toString();

        if (type == byte[].class)
            return Arrays.toString((byte[]) value);
        if (type == char[].class)
            return Arrays.toString((char[]) value);
        if (type == double[].class)
            return Arrays.toString((double[]) value);
        if (type == float[].class)
            return Arrays.toString((float[]) value);
        if (type == int[].class)
            return Arrays.toString((int[]) value);
        if (type == long[].class)
            return Arrays.toString((long[]) value);
        if (type == short[].class)
            return Arrays.toString((short[]) value);
        if (type == boolean[].class)
            return Arrays.toString((boolean[]) value);
        return Arrays.toString((Object[]) value);
    }

    private static int memberValueHashCode(Object value) {
        Class<?> type = value.getClass();
        if (!type.isArray())
            return value.hashCode();

        if (type == byte[].class)
            return Arrays.hashCode((byte[]) value);
        if (type == char[].class)
            return Arrays.hashCode((char[]) value);
        if (type == double[].class)
            return Arrays.hashCode((double[]) value);
        if (type == float[].class)
            return Arrays.hashCode((float[]) value);
        if (type == int[].class)
            return Arrays.hashCode((int[]) value);
        if (type == long[].class)
            return Arrays.hashCode((long[]) value);
        if (type == short[].class)
            return Arrays.hashCode((short[]) value);
        if (type == boolean[].class)
            return Arrays.hashCode((boolean[]) value);
        return Arrays.hashCode((Object[]) value);
    }

    private static boolean memberValueEquals(Object v1, Object v2) {
        Class<?> type = v1.getClass();

        // Check for ill formed annotation(s)
        if (v2.getClass() != type)
            return false;

        // Check for primitive, string, class, enum const, annotation,
        // or ExceptionProxy
        if (!type.isArray())
            return v1.equals(v2);

        // Check for array of string, class, enum const, annotation,
        // or ExceptionProxy
        if (v1 instanceof Object[] && v2 instanceof Object[])
            return Arrays.equals((Object[]) v1, (Object[]) v2);

        // Deal with array of primitives
        if (type == byte[].class)
            return Arrays.equals((byte[]) v1, (byte[]) v2);
        if (type == char[].class)
            return Arrays.equals((char[]) v1, (char[]) v2);
        if (type == double[].class)
            return Arrays.equals((double[]) v1, (double[]) v2);
        if (type == float[].class)
            return Arrays.equals((float[]) v1, (float[]) v2);
        if (type == int[].class)
            return Arrays.equals((int[]) v1, (int[]) v2);
        if (type == long[].class)
            return Arrays.equals((long[]) v1, (long[]) v2);
        if (type == short[].class)
            return Arrays.equals((short[]) v1, (short[]) v2);
        if (type == boolean[].class)
            return Arrays.equals((boolean[]) v1, (boolean[]) v2);

        return false;
    }

    private static Object cloneArray(Object array) {
        Class<?> type = array.getClass();

        if (type == byte[].class) {
            byte[] byteArray = (byte[]) array;
            return byteArray.clone();
        }
        if (type == char[].class) {
            char[] charArray = (char[]) array;
            return charArray.clone();
        }
        if (type == double[].class) {
            double[] doubleArray = (double[]) array;
            return doubleArray.clone();
        }
        if (type == float[].class) {
            float[] floatArray = (float[]) array;
            return floatArray.clone();
        }
        if (type == int[].class) {
            int[] intArray = (int[]) array;
            return intArray.clone();
        }
        if (type == long[].class) {
            long[] longArray = (long[]) array;
            return longArray.clone();
        }
        if (type == short[].class) {
            short[] shortArray = (short[]) array;
            return shortArray.clone();
        }
        if (type == boolean[].class) {
            boolean[] booleanArray = (boolean[]) array;
            return booleanArray.clone();
        }

        Object[] objectArray = (Object[]) array;
        return objectArray.clone();
    }

}
