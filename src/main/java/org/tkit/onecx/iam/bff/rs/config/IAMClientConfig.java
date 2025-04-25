package org.tkit.onecx.iam.bff.rs.config;

import java.util.Map;

import io.quarkus.runtime.annotations.ConfigDocFilename;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.quarkus.runtime.annotations.StaticInitSafe;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

@StaticInitSafe
@ConfigDocFilename("onecx-iam-bff.adoc")
@ConfigMapping(prefix = "onecx.iam")
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
public interface IAMClientConfig {

    /**
     * Client configurations.
     */
    @WithName("clients")
    Map<String, ConfigClient> clients();

    /**
     * Client configuration.
     */
    interface ConfigClient {

        /**
         * Url of the iam rest client.
         */
        @WithName("url")
        String url();

        /**
         * Set to true to share the HTTP client between REST clients.
         */
        @WithName("shared")
        @WithDefault("true")
        boolean shared();

        /**
         * The size of the rest client connection pool.
         */
        @WithName("connection-pool-size")
        @WithDefault("30")
        int connectionPoolSize();
    }

}
