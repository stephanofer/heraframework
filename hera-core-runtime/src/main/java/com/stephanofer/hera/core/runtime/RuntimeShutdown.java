package com.stephanofer.hera.core.runtime;

import com.stephanofer.hera.core.api.HeraModule;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

final class RuntimeShutdown {

    private RuntimeShutdown() {
    }

    static void stopInReverse(Collection<HeraModule> modules) {
        List<HeraModule> reversed = new ArrayList<>(modules);
        Collections.reverse(reversed);

        RuntimeException shutdownException = null;
        for (HeraModule module : reversed) {
            try {
                module.stop();
            } catch (Exception exception) {
                if (shutdownException == null) {
                    shutdownException = new RuntimeException("Failed during module shutdown");
                }

                shutdownException.addSuppressed(exception);
            }
        }

        if (shutdownException != null) {
            throw shutdownException;
        }
    }
}
