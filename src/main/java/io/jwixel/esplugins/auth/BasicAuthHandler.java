package io.jwixel.esplugins.auth;

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

    public BasicAuthHandler() { }

    public void authorize(RestHandler originalHandler, RestRequest request, RestChannel channel, NodeClient client) throws Exception {
        final String authHeader = request.header("Authorization");
        if (authHeader != null) {
            if (!authHeader.trim().toLowerCase().startsWith("basic ")) {
                this.doNotAuthorized(channel, "Could not find a 'Basic Authorization' header");
            } else {
                final String header = new String(Base64.getDecoder().decode(authHeader.split(" ")[1]), StandardCharsets.UTF_8);
                final int firstColonIndex = header.indexOf(':');

                String username = null;

                /*
                 * TODO: Search for these values in an ES index.
                 * Java search API
                    * https://www.elastic.co/guide/en/elasticsearch/client/java-api/current/java-search.html
                 * CreateIndex helper
                    * https://github.com/elastic/elasticsearch/blob/master/server/src/main/java/org/elasticsearch/action/admin/indices/create/CreateIndexRequest.java
                 * Foundation class for making calls into elasitc:
                    * https://github.com/elastic/elasticsearch/blob/master/server/src/main/java/org/elasticsearch/client/Client.java
                 * */
                if (firstColonIndex > 0) {
                    username = header.substring(0, firstColonIndex);
                    if (username.equals("jwixel")) {
                        log.warn("You're a jwixel; do what you want...");

                        originalHandler.handleRequest(request, channel, client);
                    } else {
                        this.doNotAuthorized(channel, String.format("Unknown user: %s. Failing auth...", username));
                    }
                } else {
                    this.doNotAuthorized(channel, String.format("Could not find user/pw pair in the auth header. %s...", header));
                }
            }
        } else {
            this.doNotAuthorized(channel, "No auth header...");
        }
    }

    private void doNotAuthorized(final RestChannel channel, String message) {
        log.warn(message);

        final BytesRestResponse response = new BytesRestResponse(RestStatus.UNAUTHORIZED, "Not authorized");
        response.addHeader("WWW-Authenticate", "Basic realm=\"JWIXEL.IO\"");
        channel.sendResponse(response);
    }
}
