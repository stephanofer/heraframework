package com.stephanofer.hera.core.runtime;

import com.stephanofer.hera.core.api.HeraModule;
import com.stephanofer.hera.core.api.HeraRuntime;
import com.stephanofer.hera.core.api.ModuleDependency;
import com.stephanofer.hera.core.api.ModuleDescriptor;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HeraRuntimeBuilderTest {

    @Test
    void shouldRejectMissingRequiredDependency() {
        HeraRuntimeBuilder builder = new HeraRuntimeBuilder()
            .register(new TestModule(new ModuleDescriptor("feature", Set.of(ModuleDependency.required("core")), false)));

        assertThrows(ModuleGraphException.class, builder::build);
    }

    @Test
    void shouldRejectDependencyCycles() {
        HeraRuntimeBuilder builder = new HeraRuntimeBuilder()
            .register(new TestModule(new ModuleDescriptor("alpha", Set.of(ModuleDependency.required("beta")), false)))
            .register(new TestModule(new ModuleDescriptor("beta", Set.of(ModuleDependency.required("alpha")), false)));

        assertThrows(ModuleGraphException.class, builder::build);
    }

    @Test
    void shouldStopModulesInReverseOrder() {
        List<String> events = new ArrayList<>();

        TestModule core = new TestModule(ModuleDescriptor.of("core"), events);
        TestModule feature = new TestModule(new ModuleDescriptor("feature", Set.of(ModuleDependency.required("core")), false), events);

        HeraRuntime runtime = new HeraRuntimeBuilder()
            .register(core)
            .register(feature)
            .build();

        runtime.close();

        assertEquals(
            List.of("configure:core", "configure:feature", "start:core", "start:feature", "stop:feature", "stop:core"),
            events
        );
    }

    private record TestModule(ModuleDescriptor descriptor, List<String> events) implements HeraModule {

        private TestModule(ModuleDescriptor descriptor) {
            this(descriptor, new ArrayList<>());
        }

        @Override
        public void configure(HeraRuntime runtime) {
            this.events.add("configure:" + this.descriptor.id());
        }

        @Override
        public void start() {
            this.events.add("start:" + this.descriptor.id());
        }

        @Override
        public void stop() {
            this.events.add("stop:" + this.descriptor.id());
        }
    }
}
