package io.jwixel.esplugins.auth;

import io.jwixel.esplugins.auth.store.IAuthStore;
//import io.jwixel.esplugins.auth.store.JwixelStore;
//import io.jwixel.esplugins.auth.store.SqliteStore;
import io.jwixel.esplugins.auth.store.ESIndexStore;

import java.lang.Exception;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestHandler;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.RestStatus;

final class BasicAuthHandler {
    protected final Logger log = LogManager.getLogger(this.getClass());
    private JwixelAuthConfig config;

    public BasicAuthHandler(JwixelAuthConfig config) {
        this.config = config;
    }

    public void authorize(RestHandler originalHandler, RestRequest request, RestChannel channel, NodeClient client) throws Exception {
        final String authHeader = request.header("Authorization");
        final String authTokenHeader = request.header("X-Auth-Token");


        if (authTokenHeader != null && authTokenHeader.contains(this.config.getAuthToken())) {
            this.log.warn("Authentication successful with auth token.");
            originalHandler.handleRequest(request, channel, client);
        } else {
            if (authHeader != null) {
                if (!authHeader.trim().toLowerCase().startsWith("basic ")) {
                    this.doNotAuthorized(channel, "Could not find a 'Basic Authorization' header");
                } else {
                    final String header = new String(Base64.getDecoder().decode(authHeader.split(" ")[1]), StandardCharsets.UTF_8);
                    final IAuthStore store = this.selectStore();
                    boolean authentic = false;

                    try {
                        authentic = store.authentic(header);
                    } catch (Exception e) {
                        this.log.error("Error encountered doing authentication: {}", e);
                        authentic = false;
                    }

                    if (authentic) {
                        log.warn("Authentication successful.");
                        originalHandler.handleRequest(request, channel, client);
                    } else {
                        this.doNotAuthorized(channel, String.format("Authentication unsucessful with auth header. %s...", header));
                    }
                }
            } else {
                this.doNotAuthorized(channel, "No auth header...");
            }
        }
    }

    private void doNotAuthorized(final RestChannel channel, String message) {
        log.warn(message);

        final BytesRestResponse response = new BytesRestResponse(RestStatus.UNAUTHORIZED, "Not authorized");
        response.addHeader("WWW-Authenticate", "Basic realm=\"JWIXEL.IO\"");
        channel.sendResponse(response);
    }

    private IAuthStore selectStore() throws Exception {
        // TODO: Ultimately this will need to be a swtich based on a config setting.
        // Hard-coding ESIndexStore for now.
        return new ESIndexStore(this.config);
    }

}
