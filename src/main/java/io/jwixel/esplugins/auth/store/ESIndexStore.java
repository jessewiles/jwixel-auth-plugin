package io.jwixel.esplugins.auth.store;

import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.node.NodeClient;
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
import java.net.SocketPermission;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.AccessControlException;

public final class ESIndexStore implements IAuthStore {
    protected final Logger log = LogManager.getLogger(this.getClass());
    private RestHighLevelClient restClient;

    @Override
    public Boolean authentic(String auth) {
        Boolean result;
        SearchRequest searchRequest;

        searchRequest = new SearchRequest("jwixel_auth");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder(); 
        sourceBuilder.query(QueryBuilders.termQuery("username", "jwixel")); 
        sourceBuilder.from(0); 
        sourceBuilder.size(1); 

        try {
            result = AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
                public Boolean run() {
                    AccessController.checkPermission(new SocketPermission("*", "connect,resolve"));
                    try {
                        HttpHost host = new HttpHost("localhost", 9200, "http");
                        RestClientBuilder clientBuilder = RestClient.builder(host);
                        Header[] defaultHeaders = new Header[]{new BasicHeader("X-Auth-Token", "let-me-pass")};
                        clientBuilder.setDefaultHeaders(defaultHeaders);

                        RestHighLevelClient client = new RestHighLevelClient(clientBuilder);

                        GetIndexRequest request = new GetIndexRequest();
                        request.indices("jwixel_auth");
                        request.local(false); 
                        request.humanReadable(true); 
                        request.includeDefaults(false); 
                        boolean exists = client.indices().exists(request, RequestOptions.DEFAULT);

                        if (!exists) {
                            CreateIndexRequest create = new CreateIndexRequest("jwixel_auth");
                            client.indices().create(create, RequestOptions.DEFAULT);
                            return false;
                        }

                        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
                        log.info(searchResponse);
                        return auth.contains("jwixel");
                    }
                    catch(IOException e) {
                        log.warn(e.toString());
                    }
                    catch(AccessControlException e) {
                        log.warn(e.toString());
                    }
                    return false;
                }
            });
        }
        catch (Exception e) {
            this.log.error("There was an error making the search: {}", e);
            return false;
        }

        return result;
    }

    public RestHighLevelClient getRESTClient() {
        this.log.info("getting rest client");
        if (this.restClient != null) {
            return this.restClient;
        }

        HttpHost host = new HttpHost("localhost", 9200, "http");
        RestClientBuilder clientBuilder = RestClient.builder(host);

        this.restClient = new RestHighLevelClient(clientBuilder);

        this.createIndex("jwixel_auth");

        return restClient;
    }

    private boolean createIndex(String name) {
        // TODO: check if exists if it's blank or contains whitespace

        try {
            Settings.Builder settings = Settings.builder();

            // create index with default system mappings; ES allows only one type per index
            CreateIndexRequest create = new CreateIndexRequest(name);
            //TODO: see if we can get mapping figured out
            // mapping(getDefaultMapping(), XContentType.JSON);
            this.log.warn("About to go create the index...");
            AccessController.doPrivileged(new PrivilegedAction<Void>() {
                public Void run() {
                    try {
                        AccessController.checkPermission(new SocketPermission("*", "connect,resolve"));
                        log.warn("About to create index...");
                        getRESTClient().indices().create(create, RequestOptions.DEFAULT);
                        log.warn("Done creating index...");

                        GetIndexRequest request = new GetIndexRequest();
                        request.indices(name);
                        request.local(false); 
                        request.humanReadable(true); 
                        request.includeDefaults(false); 
                        boolean exists = getRESTClient().indices().exists(request, RequestOptions.DEFAULT);
                        log.warn(exists);
                    }
                    catch(IOException e) {
                        log.warn(e.toString());
                    }
                    catch(AccessControlException e) {
                        log.warn(e.toString());
                    }
                    return null;
                }
            });
            this.log.info("Created a new index '{}'.", name);
        } catch (Exception e) {
            this.log.warn("There was a problem creating the index: {}", e);
            return false;
        }
        return true;
    }

    private static String getDefaultMapping() {
        return "{\n" +
            "  \"properties\": {\n" +
            "    \"username\": {\"type\": \"keyword\"},\n" +
            "    \"password\": {\"type\": \"keyword\"}\n" +
            "  }\n" +
            "}";
    }
}
