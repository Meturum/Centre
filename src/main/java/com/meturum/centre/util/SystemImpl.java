package com.meturum.centre.util;

import com.meturum.centra.system.System;
import com.meturum.centra.system.SystemManager;
import com.meturum.centre.Centre;
import org.jetbrains.annotations.NotNull;

public abstract class SystemImpl implements System {

    protected final Centre centre;

    public SystemImpl(@NotNull Centre centre) {
        this.centre = centre;
    }

    public @NotNull SystemManager getSystemManager() {
        return centre.getSystemManager();
    }

}