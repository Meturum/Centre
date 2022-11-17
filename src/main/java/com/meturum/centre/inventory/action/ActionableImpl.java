package com.meturum.centre.inventory.action;

import com.meturum.centra.inventory.actions.Actionable;
import com.meturum.centra.inventory.actions.GeneralAction;
import org.bukkit.event.inventory.InventoryAction;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Arrays;

public abstract class ActionableImpl implements Actionable {

    private @NotNull InventoryAction[] allowedActions = new InventoryAction[0];
    private @Nullable ActionLambda lambda;
    private @NotNull InventoryAction[] applicableActions = new InventoryAction[0];

    private boolean allowDragging = false;

    @Override
    public @NotNull ActionableImpl setAllowedActions(@NotNull final InventoryAction... actions) {
        allowedActions = actions;

        return this;
    }

    @Override
    public @NotNull ActionableImpl setAllowedActions(@NotNull final GeneralAction... actions) {
        return setAllowedActions(Arrays.stream(actions)
                .flatMap(action -> Arrays.stream(action.getChildren()))
                .toArray(InventoryAction[]::new));
    }

    @Override
    public final @NotNull InventoryAction[] getAllowedActions() {
        return allowedActions;
    }

    public final boolean isAllowedAction(@NotNull final InventoryAction action) {
        for (InventoryAction allowedAction : getAllowedActions()) {
            if (allowedAction.equals(action)) return true;
        }

        return false;
    }

    public final boolean isAllowedAction(@NotNull final GeneralAction action) {
        for (InventoryAction allowedAction : getAllowedActions()) {
            if (action.isApplicable(allowedAction)) return true;
        }

        return false;
    }

    @Override
    public Actionable interacts(@NotNull ActionLambda lambda) {
        this.lambda = lambda;

        return this;
    }

    @Override
    public Actionable interacts(@NotNull ActionLambda lambda, InventoryAction... applicableActions) {
        this.lambda = lambda;
        this.applicableActions = applicableActions;

        return this;
    }

    @Override
    public Actionable interacts(@NotNull ActionLambda lambda, GeneralAction... applicableActions) {
        this.lambda = lambda;
        this.applicableActions = Arrays.stream(applicableActions)
                .flatMap(generalAction -> Arrays.stream(generalAction.getChildren()))
                .toArray(InventoryAction[]::new);

        return this;
    }

    @Override
    public boolean isAllowDragging() {
        return allowDragging;
    }

    @Override
    public ActionableImpl setAllowDragging(boolean allowDragging) {
        this.allowDragging = allowDragging;

        return this;
    }

    public void reportInteraction(@NotNull ActionEventContextImpl context) {
        if(lambda == null) return;
        if(Arrays.stream(applicableActions).noneMatch(a -> a.equals(context.getBukkitContext().getAction())) && applicableActions.length > 0) return;

        lambda.run(context);
    }

}
