package io.jwixel.esplugins.auth;

import static org.elasticsearch.rest.RestRequest.Method;

import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.elasticsearch.rest.BaseRestHandler;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.RestStatus;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Date;


public class RestActions extends BaseRestHandler {
    protected final Logger log = LogManager.getLogger(this.getClass());

    public RestActions(final Settings settings, final RestController controller) {
        super(settings);
        controller.registerHandler(Method.GET, "/_jwixel", this);
    }

    @Override
    public String getName() {
        return "jwixel_actions";
    }

    @Override
    protected RestChannelConsumer prepareRequest(final RestRequest request, final NodeClient client) throws IOException {
        final boolean isPretty = request.hasParam("pretty");
        // final String index = request.param("index");
        this.log.info(request.method());
        this.log.info(request.rawPath());
        return channel -> {
            final XContentBuilder builder = JsonXContent.contentBuilder();
            if (isPretty) {
                builder.prettyPrint().lfAtEnd();
            }
            builder.startObject();
            builder.field("description", "This is a sample response: " + new Date().toString());
            builder.endObject();
            channel.sendResponse(new BytesRestResponse(RestStatus.OK, builder));
        };
    }
}
