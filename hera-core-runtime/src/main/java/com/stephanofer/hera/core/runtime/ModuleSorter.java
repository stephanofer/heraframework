package com.stephanofer.hera.core.runtime;

import com.stephanofer.hera.core.api.HeraModule;
import com.stephanofer.hera.core.api.ModuleDependency;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class ModuleSorter {

    private final Map<String, HeraModule> modulesById;

    ModuleSorter(Map<String, HeraModule> modulesById) {
        this.modulesById = new LinkedHashMap<>(modulesById);
    }

    List<HeraModule> sort() {
        validateRequiredDependencies();

        List<HeraModule> ordered = new ArrayList<>();
        Set<String> visited = new LinkedHashSet<>();
        Set<String> visiting = new LinkedHashSet<>();
        Deque<String> stack = new ArrayDeque<>();

        for (String moduleId : this.modulesById.keySet()) {
            depthFirst(moduleId, visited, visiting, stack, ordered);
        }

        return List.copyOf(ordered);
    }

    private void validateRequiredDependencies() {
        for (HeraModule module : this.modulesById.values()) {
            for (String dependencyId : module.descriptor().requiredDependencies()) {
                if (!this.modulesById.containsKey(dependencyId)) {
                    throw new ModuleGraphException("Missing required dependency '" + dependencyId + "' for module '" + module.descriptor().id() + "'");
                }
            }
        }
    }

    private void depthFirst(
        String moduleId,
        Set<String> visited,
        Set<String> visiting,
        Deque<String> stack,
        List<HeraModule> ordered
    ) {
        if (visited.contains(moduleId)) {
            return;
        }

        if (visiting.contains(moduleId)) {
            throw new ModuleGraphException("Cyclic module dependency detected: " + describeCycle(stack, moduleId));
        }

        visiting.add(moduleId);
        stack.push(moduleId);

        HeraModule module = this.modulesById.get(moduleId);
        for (ModuleDependency dependency : module.descriptor().dependencies()) {
            if (!dependency.required() && !this.modulesById.containsKey(dependency.id())) {
                continue;
            }

            depthFirst(dependency.id(), visited, visiting, stack, ordered);
        }

        stack.pop();
        visiting.remove(moduleId);
        visited.add(moduleId);
        ordered.add(module);
    }

    private String describeCycle(Deque<String> stack, String repeatedId) {
        List<String> chain = new ArrayList<>();
        chain.add(repeatedId);

        for (String current : stack) {
            chain.add(current);
            if (current.equals(repeatedId)) {
                break;
            }
        }

        return String.join(" -> ", chain.reversed());
    }
}
