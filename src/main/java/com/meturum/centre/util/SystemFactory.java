package com.meturum.centre.util;

import com.meturum.centra.system.System;
import com.meturum.centra.system.SystemManager;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SystemFactory implements SystemManager {

    private final List<SystemImpl> systems = new ArrayList<>();

    public void register(SystemImpl system) throws IllegalArgumentException {
        if(contains(system.getClass()))
            throw new IllegalArgumentException("Unable to register system, a system of type " + system.getClass().getSimpleName() + " is already registered.");

        systems.add(system);
    }

    public void registerAll(SystemImpl... systems) throws IllegalArgumentException{
        for (SystemImpl system : systems) {
            register(system);
        }
    }

    @Override
    public @Nullable <T extends System> T search(Class<T> clazz) {
        return (T) systems.stream().filter(clazz::isInstance).findFirst().orElse(null);
    }

    @Override
    public boolean contains(Class<? extends System> clazz) {
        return systems.stream().anyMatch(clazz::isInstance);
    }

}
