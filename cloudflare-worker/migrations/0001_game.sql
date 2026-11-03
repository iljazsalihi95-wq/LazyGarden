-- LazyGarden D1 schema v1: apply with wrangler d1 migrations apply lazygarden-db --remote
CREATE TABLE IF NOT EXISTS players (id TEXT PRIMARY KEY, created_at INTEGER NOT NULL, last_seen INTEGER NOT NULL);
CREATE TABLE IF NOT EXISTS sessions (token_hash TEXT PRIMARY KEY, player_id TEXT NOT NULL REFERENCES players(id), expires_at INTEGER NOT NULL);
CREATE TABLE IF NOT EXISTS progress (player_id TEXT PRIMARY KEY REFERENCES players(id), level INTEGER NOT NULL DEFAULT 1, stars INTEGER NOT NULL DEFAULT 0, coins INTEGER NOT NULL DEFAULT 200, lives INTEGER NOT NULL DEFAULT 5, lives_updated INTEGER NOT NULL, chapter INTEGER NOT NULL DEFAULT 1, revision INTEGER NOT NULL DEFAULT 0);
CREATE TABLE IF NOT EXISTS inventory (player_id TEXT NOT NULL REFERENCES players(id), item TEXT NOT NULL, quantity INTEGER NOT NULL DEFAULT 0 CHECK(quantity >= 0), PRIMARY KEY(player_id,item));
CREATE TABLE IF NOT EXISTS game_events (id TEXT PRIMARY KEY, player_id TEXT NOT NULL, kind TEXT NOT NULL, level INTEGER, payload TEXT NOT NULL, created_at INTEGER NOT NULL);
CREATE TABLE IF NOT EXISTS level_attempts (id TEXT PRIMARY KEY, player_id TEXT NOT NULL, level INTEGER NOT NULL, seed INTEGER NOT NULL, moves INTEGER NOT NULL, target INTEGER NOT NULL, expires_at INTEGER NOT NULL, consumed INTEGER NOT NULL DEFAULT 0);
CREATE INDEX IF NOT EXISTS idx_events_player ON game_events(player_id,created_at);
CREATE INDEX IF NOT EXISTS idx_attempts_player ON level_attempts(player_id,expires_at);