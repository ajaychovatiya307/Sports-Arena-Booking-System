package com.infy.sportsarena.user;

import org.assertj.core.api.Assertions;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public final class UserDataAssertions {
    private UserDataAssertions() {}

    public static void verifyDataClass(Class<?> type) {
        try {
            Object sample = type.getDeclaredConstructor().newInstance();
            List<Field> fields = new ArrayList<>();
            for (Field f : type.getDeclaredFields()) {
                if (!Modifier.isStatic(f.getModifiers()) && !f.isSynthetic()) fields.add(f);
            }
            for (Field field : fields) {
                field.setAccessible(true);
                Object value = value(field.getType(), 1);
                field.set(sample, value);
                String getter = (field.getType() == boolean.class ? "is" : "get")
                        + Character.toUpperCase(field.getName().charAt(0)) + field.getName().substring(1);
                Assertions.assertThat(type.getMethod(getter).invoke(sample)).isEqualTo(value);
            }
            Object same = copy(type, fields, sample);
            Assertions.assertThat(sample).isEqualTo(same).hasSameHashCodeAs(same);
            Assertions.assertThat(sample.toString()).isNotBlank();
            Assertions.assertThat(sample).isNotEqualTo(null).isNotEqualTo("different type");
            for (Field field : fields) {
                Object changed = copy(type, fields, sample);
                field.setAccessible(true);
                field.set(changed, value(field.getType(), 2));
                Assertions.assertThat(sample).as(field.getName()).isNotEqualTo(changed);
                if (!field.getType().isPrimitive()) {
                    Object nullValue = copy(type, fields, sample);
                    field.set(nullValue, null);
                    Assertions.assertThat(sample).as(field.getName() + " null").isNotEqualTo(nullValue);
                    nullValue.hashCode();
                    Assertions.assertThat(nullValue.toString()).isNotBlank();
                }
            }
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("Unable to test data class " + type.getName(), e);
        }
    }

    private static Object copy(Class<?> type, List<Field> fields, Object source) throws ReflectiveOperationException {
        Object copy = type.getDeclaredConstructor().newInstance();
        for (Field field : fields) {
            field.setAccessible(true);
            field.set(copy, field.get(source));
        }
        return copy;
    }

    private static Object value(Class<?> type, int n) throws ReflectiveOperationException {
        if (type == String.class) return "value-" + n;
        if (type == Long.class || type == long.class) return (long) n;
        if (type == Integer.class || type == int.class) return n;
        if (type == boolean.class || type == Boolean.class) return n % 2 != 0;
        if (type.isEnum()) return type.getEnumConstants()[(n - 1) % type.getEnumConstants().length];
        if (type.getName().startsWith("com.infy.sportsarena.user.")) {
            return type.getDeclaredConstructor().newInstance();
        }
        throw new IllegalArgumentException("No test value configured for " + type.getName());
    }
}
