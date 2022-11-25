package com.meturum.centre.util;

import com.meturum.centra.system.System;
import com.meturum.centra.system.SystemManager;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class SystemFactory implements SystemManager {

    private final List<SystemImpl> systems = new ArrayList<>();

    public void start() {
        systems.forEach(SystemImpl::init);
    }

    public void stop() {
        List<SystemImpl> systemList = new ArrayList<>(systems);
        Collections.reverse(systemList); // Stop in reverse order, so that dependencies are stopped first

        systemList.forEach(SystemImpl::stop);
    }

    public <T extends System> T register(SystemImpl system) throws IllegalArgumentException {
        if(contains(system.getClass()))
            throw new IllegalArgumentException("Unable to register system, a system of action " + system.getClass().getSimpleName() + " is already registered.");

        systems.add(system);
        system.centre.getServer().getPluginManager().registerEvents(system, system.centre);

        return (T) system;
    }

    public void registerAll(SystemImpl... systems) throws IllegalArgumentException {
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
