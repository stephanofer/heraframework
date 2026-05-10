package com.stephanofer.hera.command.paper;

import java.util.Arrays;
import java.util.Objects;

public final class CommandRequirements {

    private CommandRequirements() {
    }

    public static CommandRequirement permission(String permission) {
        Objects.requireNonNull(permission, "permission");
        if (permission.isBlank()) {
            throw new IllegalArgumentException("permission cannot be blank");
        }

        return context -> context.sender().hasPermission(permission);
    }

    public static CommandRequirement senderPolicy(SenderPolicy policy) {
        Objects.requireNonNull(policy, "policy");
        return context -> policy.allows(context.sender());
    }

    public static CommandRequirement executorPolicy(ExecutorPolicy policy) {
        Objects.requireNonNull(policy, "policy");
        return context -> policy.allows(context.executor());
    }

    public static CommandRequirement allOf(CommandRequirement... requirements) {
        Objects.requireNonNull(requirements, "requirements");
        CommandRequirement[] copy = Arrays.copyOf(requirements, requirements.length);
        return context -> {
            for (CommandRequirement requirement : copy) {
                if (requirement != null && !requirement.test(context)) {
                    return false;
                }
            }

            return true;
        };
    }
}
