package io.jwixel.esplugins.auth.store;

import io.jwixel.esplugins.auth.JwixelAuthConfig;

import org.elasticsearch.SpecialPermission;
import org.elasticsearch.common.CheckedRunnable;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.common.CheckedRunnable;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import org.elasticsearch.client.Requests;
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
import java.lang.Thread;
import java.math.BigInteger; 
import java.net.SocketPermission;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.AccessControlException;
import java.security.MessageDigest; 
import java.security.NoSuchAlgorithmException; 
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

public final class ESIndexStore implements IAuthStore {
    protected final Logger log = LogManager.getLogger(this.getClass());
    private RestHighLevelClient restClient;
    private JwixelAuthConfig config;

    public ESIndexStore(JwixelAuthConfig config) {
        this.config = config;
    }

    @Override
    public Boolean authentic(String auth) {
        Boolean result = true;
        try {
            result = this.ensureIndex();
        }
        catch (IOException e) {
            this.log.error("Problem ensuring index.  Permitting all traffic.");
            return result;
        }
        return this.checkCreds(auth);
    }

    private RestHighLevelClient getRESTClient() {
        if (this.restClient == null) {
            HttpHost host;
            RestClientBuilder clientBuilder;
            Header[] defaultHeaders;

            host = new HttpHost("localhost", 9200, "http");
            clientBuilder = RestClient.builder(host);
            defaultHeaders = new Header[]{new BasicHeader("X-Auth-Token", this.config.getAuthToken())};
            clientBuilder.setDefaultHeaders(defaultHeaders);

            this.restClient = new RestHighLevelClient(clientBuilder);
        }
        return this.restClient;
    }

    private Boolean ensureIndex() throws IOException {
        GetIndexRequest request = new GetIndexRequest();
        request.indices("jwixel_auth");
        request.local(false); 
        request.humanReadable(true); 
        request.includeDefaults(false);

        Boolean indexExists = doElevated(() -> {
            return this.getRESTClient().indices().exists(request, RequestOptions.DEFAULT);
        });
        this.log.debug("Index exists first: {}", indexExists);
        if (!indexExists) {
            CreateIndexRequest create = new CreateIndexRequest("jwixel_auth");
            indexExists = doElevated(() -> {
                this.getRESTClient().indices().create(create, RequestOptions.DEFAULT);
                return this.getRESTClient().indices().exists(request, RequestOptions.DEFAULT);
            });
            // TODO: figure out debug logging
            this.log.info("Created index? {}", indexExists);

            // Insert initial admin user:pass pair
            XContentBuilder builder = XContentFactory.jsonBuilder();
            builder.startObject();
            {
                builder.field("username", this.config.getAdminUser());
                // TODO: store md5 for now. figure out storing salt details for SHA-*.
                // UPDATE: unclear that salted hash is better than md5 here according
                // to research md5 is vulnerable to collision, not preimage.
                builder.field("password", getMd5(this.config.getAdminPass()));
            }
            builder.endObject();
            IndexRequest addUserRequest = new IndexRequest("jwixel_auth", "doc").source(builder);

            // make the call to add the user:pass pair
            IndexResponse addUserResponse = this.getRESTClient().index(addUserRequest, RequestOptions.DEFAULT);

            // give things a beat to let new user:pass pair become available.
            try {
                Thread.sleep(500);
            }
            catch (Exception e) { } // ignore
        }
        return indexExists;
    }

    private Boolean checkCreds(String auth) {
        SearchRequest searchRequest;
        SearchSourceBuilder sourceBuilder;
        String[] parts;

        parts = auth.split(":");

        try {
            searchRequest = new SearchRequest("jwixel_auth");

            sourceBuilder = new SearchSourceBuilder(); 
            sourceBuilder.query(QueryBuilders.matchQuery("username", parts[0])); 

            // TODO: Need to test how search results behave once multiple similar users are added
            // since search doesn't return exact match only
            sourceBuilder.from(0); 
            sourceBuilder.size(1); 

            SearchResponse searchResponse = this.getRESTClient().search(searchRequest, RequestOptions.DEFAULT);
            for (SearchHit hit: searchResponse.getHits().getHits()) {
                String username = (String)hit.getSourceAsMap().get("username");
                String password = (String)hit.getSourceAsMap().get("password");
                if (parts[0].equals(username) && getMd5(parts[1]).equals(password)) {
                    return true;
                }
            }
        }
        catch(IOException e) {
            this.log.warn(e.toString());
        }
        catch(AccessControlException e) {
            this.log.warn(e.toString());
        }
        return false;
    }

    private static String getDefaultMapping() {
        // TODO: Use a builder for this?
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

    private static String getMd5(String input) 
    {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5"); 
            byte[] messageDigest = md.digest(input.getBytes()); 
            BigInteger no = new BigInteger(1, messageDigest); 

            // Convert message digest into hex value 
            String hashtext = no.toString(16); 
            while (hashtext.length() < 32) { 
                hashtext = "0" + hashtext; 
            }
            return hashtext; 
        }
        catch (NoSuchAlgorithmException e) { 
            throw new RuntimeException(e); 
        }
    } 
}
