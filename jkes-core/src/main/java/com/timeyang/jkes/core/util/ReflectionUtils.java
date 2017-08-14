package com.timeyang.jkes.core.util;

import com.timeyang.jkes.core.exception.IllegalMemberAccessException;
import com.timeyang.jkes.core.exception.JkesException;
import com.timeyang.jkes.core.exception.ReflectiveInvocationTargetException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reflection Utils
 *
 * @author chaokunyang
 */
public class ReflectionUtils {

    /**
     * @param method {@link Method} object
     * @return the actual type parameters used in the source code. Return <strong>empty</strong> list if no parametrized type
     */
    public static List<String> getReturnTypeParameters(Method method) {
        List<String> typeParameters = new ArrayList<>();

        Type type = method.getGenericReturnType();
        String typeName = type.getTypeName(); // ex: java.util.List<com.timeyang.search.entity.Person>, java.lang.Long
        Pattern p = Pattern.compile("<((\\S+\\.?),?\\s*)>");
        Matcher m = p.matcher(typeName);
        while (m.find()) {
            typeParameters.add(m.group(2));
        }

        return typeParameters;
    }

    public static String getTypeName(Method method) {
        Type type = method.getGenericReturnType();

        String typeName = type.getTypeName(); // ex: java.util.List<com.timeyang.search.entity.Person>, java.lang.Long

        return typeName;
    }

    /**
     * If return type is genetic type, then return last parameterized type, else return the formal return type name of the method represented by this {@code Method}  object.
     * @param method {@link Method} method
     * @return If return type is genetic type, then return last parameterized type, else return the formal return type name of the method represented by this {@code Method}  object.
     */
    public static String getInnermostType(Method method) {
        Type type = method.getGenericReturnType();

        String typeName = type.getTypeName(); // ex: java.util.List<com.timeyang.search.entity.Person>, java.lang.Long

        String[] types = typeName.split(",\\s*|<|<|>+");

        return types[types.length - 1];
    }

    /**
     * If return type is genetic type, then return class of last parameterized type, else return the formal class of  return type of the method represented by this {@code Method}  object.
     * @param method {@link Method} method
     * @return If return type is genetic type, then return class of last parameterized type, else return the formal class of  return type of the method represented by this {@code Method}  object.
     */
    public static Class<?> getInnermostTypeClass(Method method) {
        Class<?> clazz;
        try {
            clazz = Class.forName(getInnermostType(method));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return clazz;
    }

    /**
     * If the field object is a generic type, return innermost type; else return field type name directly
     * @param field Field object
     * @return If the field object is a generic type, return innermost type; else return field type name directly
     */
    public static String getInnermostType(Field field) {
        Type type = field.getGenericType();

        String typeName = type.getTypeName(); // ex: java.util.List<com.timeyang.search.entity.Person>, java.lang.Long

        String[] types = typeName.split(",\\s*|<|<|>+");

        return types[types.length - 1];
    }

    /**
     * If the field object is a generic type, return innermost type class; else return field class directly
     * @param field Field object
     * @return If the field object is a generic type, return innermost type class; else return field class directly
     */
    public static Class<?> getInnermostTypeClass(Field field) {
        Class<?> clazz;
        try {
            clazz = Class.forName(getInnermostType(field));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return clazz;
    }

    public static String getFieldNameForGetter(String methodName) {
        Asserts.notBlank(methodName, "methodName must have content");
        Asserts.check(methodName.startsWith("get") || methodName.startsWith("is"),
                "the method is not a getter method");
        if(methodName.startsWith("get")) {
            char c[] = methodName.toCharArray();
            c[3] = Character.toLowerCase(c[3]);
            return String.valueOf(Arrays.copyOfRange(c, 3, c.length));
        }else {
            char c[] = methodName.toCharArray();
            c[2] = Character.toLowerCase(c[2]);
            return String.valueOf(Arrays.copyOfRange(c, 2, c.length));
        }
    }

    public static String getFieldNameForGetter(Method method) {
        String methodName = method.getName();
        return getFieldNameForGetter(methodName);
    }


    /**
     * Invoke specified method in target class or super class, regardless of access level of method
     *
     * @param target target object
     * @param methodName the name of the method
     * @param parameterTypes parameter type array
     * @param params method params
     * @return method invoke result
     */
    public static Object invokeMethod(Object target, String methodName, Class<?>[] parameterTypes, Object... params) {

        Class<?> clazz = target.getClass();
        do {
            try {
                // getMethods() return only public methods, though includes super methods
                // getDeclaredMethods() return current class all methods, include non public methods, but doesn't include super methods, so clazz = clazz.getSuperclass() is needed.
                Method method = clazz.getDeclaredMethod(methodName, parameterTypes);
                method.setAccessible(true);

                return method.invoke(target, params);
            } catch (NoSuchMethodException e) {
                clazz = clazz.getSuperclass();
                if(clazz == null)
                    throw new RuntimeException(new NoSuchMethodException(target.getClass()
                            + " and all its super class doesn't have " + methodName
                            + " with parameterTypes: " + Arrays.toString(parameterTypes)));
            } catch (IllegalAccessException e) {
                throw new IllegalMemberAccessException(e);
            } catch (InvocationTargetException e) {
                throw new ReflectiveInvocationTargetException(e);
            }
        } while (true);

    }

    /**
     * Get value of annotated field. event if field is private
     * @param target current object
     * @param annotation annotation
     * @return value of annotated field
     */
    public static Object getAnnotatedFieldValue(Object target, Class<? extends Annotation> annotation) {
        Class<?> clazz = target.getClass();

        do {
            Field[] fields = clazz.getDeclaredFields();
            for(Field field : fields) {
                if(field.isAnnotationPresent(annotation)) {
                    field.setAccessible(true);
                    try {
                        return field.get(target);
                    } catch (IllegalAccessException e) {
                        throw new JkesException(e);
                    }
                }
            }

            clazz = clazz.getSuperclass();
        }while (clazz != null);

        return null;
    }

    /**
     * Get annotated field. The field can be in parent field
     *
     * @param clazz clazz
     * @param fieldName fieldName
     * @param annotation annotation
     * @return annotated field.
     */
    public static Field getAnnotatedField(Class<?> clazz, String fieldName, Class<? extends Annotation> annotation) {
        do {
            Field[] fields = clazz.getDeclaredFields();
            for(Field field : fields) {
                if(Objects.equals(fieldName, field.getName()) && field.isAnnotationPresent(annotation)) {
                    return field;
                }
            }

            clazz = clazz.getSuperclass();
        }while (clazz != null);

        return null;
    }

    /**
     * Get annotated field. The field can be in parent field
     *
     * @param clazz clazz
     * @param fieldName fieldName
     * @param annotationClass annotationClass
     * @return annotated field.
     */
    public static <T extends Annotation> T getFieldAnnotation(Class<?> clazz, String fieldName, Class<T> annotationClass) {
        Field annotatedField = getAnnotatedField(clazz, fieldName, annotationClass);
        if (annotatedField != null) {
            return annotatedField.getAnnotation(annotationClass);
        }
        return null;
    }


    /**
     * Get return value of annotated method
     * @param target current object
     * @param annotation annotation
     * @return return value of annotated method
     */
    public static Object getAnnotatedMethodReturnValue(Object target, Class<? extends Annotation> annotation) {

        Class<?> clazz = target.getClass();

        do {
            Method[] methods = target.getClass().getMethods();
            for (Method method : methods) {
                if (method.isAnnotationPresent(annotation)) {
                    try {
                        return method.invoke(target);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new JkesException(e);
                    }
                }
            }

            clazz = clazz.getSuperclass();

        }while (clazz != null);

        return null;
    }
}
