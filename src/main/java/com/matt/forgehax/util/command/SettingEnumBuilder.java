package com.matt.forgehax.util.command;

import com.matt.forgehax.util.command.callbacks.OnChangeCallback;
import com.matt.forgehax.util.typeconverter.TypeConverter;
import com.matt.forgehax.util.typeconverter.TypeConverterRegistry;
import net.minecraft.util.math.MathHelper;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.function.Consumer;

/**
 * Created on 8/5/2017 by fr1kin
 */
public class SettingEnumBuilder<E extends Enum<E>> extends BaseCommandBuilder<SettingEnumBuilder<E>, Setting<E>> {
    public SettingEnumBuilder<E> changed(Consumer<OnChangeCallback<E>> consumer) {
        getCallbacks(CallbackType.CHANGE).add(consumer);
        return this;
    }

    public SettingEnumBuilder<E> defaultTo(E defaultValue) {
        return insert(Setting.DEFAULTVALUE, defaultValue)
                .convertFrom(defaultValue.getClass());
    }

    private SettingEnumBuilder<E> converter(TypeConverter<E> converter) {
        return insert(Setting.CONVERTER, converter)
                .comparator(converter.comparator());
    }

    private SettingEnumBuilder<E> comparator(Comparator<E> comparator) {
        return insert(Setting.COMPARATOR, comparator);
    }

    private SettingEnumBuilder<E> min(E minValue) {
        return insert(Setting.MINVALUE, minValue);
    }

    private SettingEnumBuilder<E> max(E maxValue) {
        return insert(Setting.MAXVALUE, maxValue);
    }

    private SettingEnumBuilder<E> convertFrom(Class<?> clazz) {
        TypeConverter<E> converter = TypeConverterRegistry.get(clazz);
        if(converter == null) converter = new TypeConverter<E>() {
            @Override
            public String label() {
                return clazz.getName();
            }

            @Override
            public Class<E> type() {
                return (Class<E>)clazz;
            }

            @Override
            public E parse(String value) {
                E[] values = type().getEnumConstants();
                try {
                    // first try by name
                    for(E v : values) if(v.name().equalsIgnoreCase(value))
                        return v;
                    // jump to catch
                    throw new NullPointerException();
                } catch (Throwable t) {
                    try {
                        // if name failed, try by index
                        // if it goes over or under the limit, clamp it
                        int index = Integer.valueOf(value);
                        return values[MathHelper.clamp(index, 0, values.length - 1)];
                    } catch (Throwable et) {
                        return null;
                    }
                }
            }

            @Override
            public String toString(E value) {
                return value.name();
            }

            @Nullable
            @Override
            public Comparator<E> comparator() {
                return Enum::compareTo;
            }
        };

        E min;
        E max;

        try {
            E[] constants = (E[])clazz.getEnumConstants();
            min = constants[0];
            max = constants[constants.length - 1];
        } catch (Throwable t) {
            min = null;
            max = null;
        }

        return converter(converter)
                .comparator(converter.comparator())
                .min(min)
                .max(max);
    }

    @Override
    public Setting<E> build() {
        return new Setting<>(data);
    }
}
