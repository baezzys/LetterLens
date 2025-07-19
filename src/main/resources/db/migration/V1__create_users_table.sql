CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    profile_image_url VARCHAR(500),
    oauth_provider VARCHAR(20) NOT NULL,
    oauth_original_id VARCHAR(100) NOT NULL,
    oauth_email VARCHAR(100),
    oauth_connected_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT uk_oauth_provider_id UNIQUE (oauth_provider, oauth_original_id)
);

CREATE INDEX idx_username ON users(username);
