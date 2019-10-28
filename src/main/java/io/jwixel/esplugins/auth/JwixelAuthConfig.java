package io.jwixel.esplugins.auth;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.common.settings.SecureSetting;
import org.elasticsearch.common.settings.SecureString;
import org.elasticsearch.common.settings.Setting;
import org.elasticsearch.common.settings.Setting.Property;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.lang.Exception;
import java.nio.file.Path;


public final class JwixelAuthConfig {
    private Environment environment;
    protected final Logger log = LogManager.getLogger(this.getClass());
    static final Setting<SecureString> ADMIN_USER = SecureSetting.secureString("jwixel.admin_user", null);
    static final Setting<SecureString> ADMIN_PASS = SecureSetting.secureString("jwixel.admin_pass", null);
    static final Setting<SecureString> AUTH_TOKEN = SecureSetting.secureString("jwixel.auth_token", null);
    private String adminUser;
    private String adminPass;
    private String authToken;

    public JwixelAuthConfig(final Environment environment) {
        this.environment = environment;
        final SecureString username = ADMIN_USER.get(environment.settings());
        assert username != null;
        this.adminUser = username.toString();

        final SecureString password = ADMIN_PASS.get(environment.settings());
        assert password != null;
        this.adminPass = password.toString();

        final SecureString authToken = AUTH_TOKEN.get(environment.settings());
        assert authToken != null;
        this.authToken = authToken.toString();

        this.log.info("Configuring with admin info: {}:{}:{}", this.adminUser, this.adminPass, this.authToken);
    }

    public String getAdminUser() {
        return this.adminUser;
    }

    public String getAdminPass() {
        // TODO: calc the hash
        return this.adminPass;
    }

    public String getAuthToken() {
        return this.authToken;
    }
}
