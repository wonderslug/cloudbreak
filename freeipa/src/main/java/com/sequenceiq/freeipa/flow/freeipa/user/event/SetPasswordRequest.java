package com.sequenceiq.freeipa.flow.freeipa.user.event;

import java.time.Instant;
import java.util.Optional;

public class SetPasswordRequest extends FreeIpaClientRequest<SetPasswordResult> {

    private final String environment;

    private final String username;

    private final String password;

    private final Optional<Instant> expirationInstant;

    public SetPasswordRequest(Long stackId, String environment, String username, String password, Optional<Instant> expirationInstant) {
        super(stackId);
        this.environment = environment;
        this.username = username;
        this.password = password;
        this.expirationInstant = expirationInstant;
    }

    public String getEnvironment() {
        return environment;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public Optional<Instant> getExpirationInstant() {
        return expirationInstant;
    }

    @Override
    public String toString() {
        return "SetPasswordRequest{"
                + "stackId='" + getResourceId() + '\''
                + "environment='" + environment + '\''
                + "username='" + username + '\''
                + "expirationInstant=" + expirationInstant
                + '}';
    }
}
