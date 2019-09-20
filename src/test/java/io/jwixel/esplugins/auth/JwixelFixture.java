package io.jwixel.esplugins.auth;

import org.elasticsearch.test.fixture.AbstractHttpFixture;

import java.io.IOException;
import java.util.Objects;

import static java.nio.charset.StandardCharsets.UTF_8;

public class JwixelFixture extends AbstractHttpFixture {

    private final String message;

    private JwixelFixture(final String workingDir, final String message) {
        super(workingDir);
        this.message = Objects.requireNonNull(message);
    }

    @Override
    protected Response handle(final Request request) throws IOException {
        if ("GET".equals(request.getMethod()) && "/".equals(request.getPath())) {
            return new Response(200, TEXT_PLAIN_CONTENT_TYPE, message.getBytes(UTF_8));
        }
        return null;
    }

    public static void main(final String[] args) throws Exception {
        if (args == null || args.length != 2) {
            throw new IllegalArgumentException("JwixelFixture <working directory> <echo message>");
        }

        final JwixelFixture fixture = new JwixelFixture(args[0], args[1]);
        fixture.listen();
    }
}
