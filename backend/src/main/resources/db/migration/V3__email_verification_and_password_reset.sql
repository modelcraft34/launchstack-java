CREATE TABLE email_verification_tokens (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(512) NOT NULL,
    user_id BIGINT NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    used_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_email_verification_tokens_token UNIQUE (token),
    CONSTRAINT fk_email_verification_tokens_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_email_verification_tokens_token ON email_verification_tokens (token);
CREATE INDEX idx_email_verification_tokens_user_id ON email_verification_tokens (user_id);

CREATE TABLE password_reset_tokens (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(512) NOT NULL,
    user_id BIGINT NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    used_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_password_reset_tokens_token UNIQUE (token),
    CONSTRAINT fk_password_reset_tokens_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_password_reset_tokens_token ON password_reset_tokens (token);
CREATE INDEX idx_password_reset_tokens_user_id ON password_reset_tokens (user_id);
