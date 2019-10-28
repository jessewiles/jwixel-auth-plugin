package io.jwixel.esplugins.auth;

import org.elasticsearch.cluster.metadata.IndexNameExpressionResolver;
import org.elasticsearch.cluster.node.DiscoveryNodes;
import org.elasticsearch.common.settings.ClusterSettings;
import org.elasticsearch.common.settings.IndexScopedSettings;
import org.elasticsearch.common.settings.Setting;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.settings.SettingsFilter;
import org.elasticsearch.common.util.concurrent.ThreadContext;
import org.elasticsearch.env.Environment;
import org.elasticsearch.plugins.ActionPlugin;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestHandler;
import org.elasticsearch.rest.RestRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;


public class JwixelAuthPlugin extends Plugin implements ActionPlugin {
    protected final Logger log = LogManager.getLogger(this.getClass());
    private BasicAuthHandler basicAuthHandler;

    public JwixelAuthPlugin(final Settings settings, final Path configPath) {
        this.log.info("JwixelAuthPlugin initialized...");
        final JwixelAuthConfig config;

        config = new JwixelAuthConfig(new Environment(settings, configPath));
        this.basicAuthHandler = new BasicAuthHandler(config);
    }

    @Override
    public List<Setting<?>> getSettings() {
        return Arrays.asList(
            JwixelAuthConfig.ADMIN_USER,
            JwixelAuthConfig.ADMIN_PASS,
            JwixelAuthConfig.AUTH_TOKEN);
    }

    @Override
    public List<RestHandler> getRestHandlers(final Settings settings,
                                             final RestController restController,
                                             final ClusterSettings clusterSettings,
                                             final IndexScopedSettings indexScopedSettings,
                                             final SettingsFilter settingsFilter,
                                             final IndexNameExpressionResolver indexNameExpressionResolver,
                                             final Supplier<DiscoveryNodes> nodesInCluster) {
        return Arrays.asList(new RestActions(settings, restController));
    }

    @Override
    public UnaryOperator<RestHandler> getRestHandlerWrapper(ThreadContext threadContext) {
        return originalHandler -> (RestHandler) (request, channel, client) -> {
            this.basicAuthHandler.authorize(originalHandler, request, channel, client);
        };
     }
}
