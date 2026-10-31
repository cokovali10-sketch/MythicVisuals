package com.mythicvisuals.module.setting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Fixed option list with any subset selected. Unknown options are ignored. */
public class MultiSelectSetting extends Setting<Set<String>> {
    private final List<String> options;

    public MultiSelectSetting(String name, String description,
                              List<String> options, Set<String> defaultSelected) {
        super(name, description, new LinkedHashSet<>());
        if (options == null || options.isEmpty()) {
            throw new IllegalArgumentException("options must not be empty");
        }
        this.options = List.copyOf(options);
        LinkedHashSet<String> sel = new LinkedHashSet<>();
        if (defaultSelected != null) {
            for (String o : defaultSelected) {
                if (this.options.contains(o)) {
                    sel.add(o);
                }
            }
        }
        this.defaultValue = new LinkedHashSet<>(sel);
        this.value = new LinkedHashSet<>(sel);
    }

    public List<String> options() {
        return options;
    }

    @Override
    public Set<String> get() {
        return Collections.unmodifiableSet(value);
    }

    @Override
    public void set(Set<String> selected) {
        LinkedHashSet<String> sel = new LinkedHashSet<>();
        if (selected != null) {
            for (String o : selected) {
                if (options.contains(o)) {
                    sel.add(o);
                }
            }
        }
        this.value = sel;
    }

    public void toggle(String option) {
        if (!options.contains(option)) {
            return;
        }
        LinkedHashSet<String> sel = new LinkedHashSet<>(value);
        if (!sel.remove(option)) {
            sel.add(option);
        }
        this.value = sel;
    }

    @Override
    public Object toJsonValue() {
        return new ArrayList<>(value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void fromJsonValue(Object o) {
        if (o instanceof List<?> list) {
            LinkedHashSet<String> sel = new LinkedHashSet<>();
            for (Object e : list) {
                if (e instanceof String s && options.contains(s)) {
                    sel.add(s);
                }
            }
            this.value = sel;
        } else {
            reset();
        }
    }
}
