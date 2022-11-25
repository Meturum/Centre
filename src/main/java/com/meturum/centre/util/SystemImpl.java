package com.meturum.centre.util;

import com.meturum.centra.system.System;
import com.meturum.centra.system.SystemManager;
import com.meturum.centre.Centre;
import org.jetbrains.annotations.NotNull;

public abstract class SystemImpl implements System {

    protected final Centre centre;

    private @NotNull SystemState state = SystemState.DISABLED;

    public SystemImpl(@NotNull Centre centre) {
        this.centre = centre;
    }

    public @NotNull SystemState getState() {
        return state;
    }

    public @NotNull SystemManager getSystemManager() {
        return centre.getSystemManager();
    }

    public final void init() {
        if(state == SystemState.ACTIVE) return;

        _init();
        state = SystemState.ACTIVE;
    }

    protected void _init() {}

    public final void stop() {
        if(state == SystemState.DISABLED) return;

        _stop();
        state = SystemState.DISABLED;
    }

    protected void _stop() {}

}