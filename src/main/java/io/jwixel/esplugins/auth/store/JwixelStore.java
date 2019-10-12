package io.jwixel.esplugins.auth.store;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class JwixelStore implements IAuthStore {
    protected final Logger log = LogManager.getLogger(this.getClass());

    @Override
    public Boolean authentic(String auth) {
        int firstColonIndex = auth.indexOf(':');
        String username = null;

        if (firstColonIndex > 0) {
            username = auth.substring(0, firstColonIndex);
            if (username.equals("jwixel")) {
                log.warn("You're a jwixel; do what you want...");
                return true;
            }
        }
        return false;
    }
}
