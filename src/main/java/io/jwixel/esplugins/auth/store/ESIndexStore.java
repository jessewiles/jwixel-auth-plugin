package io.jwixel.esplugins.auth.store;

import org.elasticsearch.SpecialPermission;
import org.elasticsearch.common.CheckedRunnable;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.common.CheckedRunnable;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.http.HttpHost;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

import java.io.IOException;
import java.lang.Exception;
import java.net.SocketPermission;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.AccessControlException;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

public final class ESIndexStore implements IAuthStore {
    protected final Logger log = LogManager.getLogger(this.getClass());
    private RestHighLevelClient restClient;

    @Override
    public Boolean authentic(String auth) throws Exception {
        boolean result = false;
        //SearchRequest searchRequest;
        //SearchSourceBuilder sourceBuilder;


        /*
        searchRequest = new SearchRequest("jwixel_auth");

        sourceBuilder = new SearchSourceBuilder(); 
        sourceBuilder.query(QueryBuilders.termQuery("username", "jwixel")); 
        sourceBuilder.from(0); 
        sourceBuilder.size(1); 
        */

        try {
            boolean exists = this.ensureIndex();
            if (!exists) {
                throw new Exception("bad things!!");
            }
        } catch (IOException e) {
            this.log.error(e);
        }

        return result;
    }

    private RestHighLevelClient getRESTClient() {
        if (this.restClient == null) {
            HttpHost host;
            RestClientBuilder clientBuilder;
            Header[] defaultHeaders;

            host = new HttpHost("localhost", 9200, "http");
            clientBuilder = RestClient.builder(host);
            defaultHeaders = new Header[]{new BasicHeader("X-Auth-Token", "let-me-pass")};
            clientBuilder.setDefaultHeaders(defaultHeaders);

            this.restClient = new RestHighLevelClient(clientBuilder);
        }
        return this.restClient;
    }

    private boolean ensureIndex() throws IOException {
        GetIndexRequest request = new GetIndexRequest();
        request.indices("jwixel_auth");
        request.local(false); 
        request.humanReadable(true); 
        request.includeDefaults(false);

        boolean indexExists = doElevated(() -> {
            return this.getRESTClient().indices().exists(request, RequestOptions.DEFAULT);
        });
        this.log.debug("Index exists first: {}", indexExists);
        if (!indexExists) {
            CreateIndexRequest create = new CreateIndexRequest("jwixel_auth");
            indexExists = doElevated(() -> {
                this.getRESTClient().indices().create(create, RequestOptions.DEFAULT);
                return this.getRESTClient().indices().exists(request, RequestOptions.DEFAULT);
            });
            this.log.debug("Created index? {}", indexExists);
        }
        return indexExists;
    }

    private static String getDefaultMapping() {
        return "{\n" +
            "  \"properties\": {\n" +
            "    \"username\": {\"type\": \"keyword\"},\n" +
            "    \"password\": {\"type\": \"keyword\"}\n" +
            "  }\n" +
            "}";
    }

    private static <T> T doElevated(PrivilegedExceptionAction<T> work) throws IOException {
        SpecialPermission.check();
        try {
            return AccessController.doPrivileged(work);
        } catch (PrivilegedActionException e) {
            throw (IOException) e.getCause();
        }
    }
}
