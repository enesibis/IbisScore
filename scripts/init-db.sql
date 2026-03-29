-- IbisScore — PostgreSQL Initialization Script
-- Bu script ilk container başlatıldığında otomatik çalışır

-- ============================================================
-- EXTENSIONS
-- ============================================================
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";  -- Fuzzy text search

-- ============================================================
-- LEAGUES
-- ============================================================
CREATE TABLE IF NOT EXISTS leagues (
    id          SERIAL PRIMARY KEY,
    api_id      INTEGER UNIQUE NOT NULL,
    name        VARCHAR(100) NOT NULL,
    country     VARCHAR(50),
    season      INTEGER,
    logo_url    VARCHAR(500),
    is_active   BOOLEAN DEFAULT true,
    created_at  TIMESTAMP DEFAULT NOW(),
    updated_at  TIMESTAMP DEFAULT NOW()
);

-- ============================================================
-- TEAMS
-- ============================================================
CREATE TABLE IF NOT EXISTS teams (
    id               SERIAL PRIMARY KEY,
    api_id           INTEGER UNIQUE NOT NULL,
    name             VARCHAR(100) NOT NULL,
    short_name       VARCHAR(10),
    country          VARCHAR(50),
    logo_url         VARCHAR(500),
    venue_name       VARCHAR(150),
    venue_capacity   INTEGER,
    founded          INTEGER,
    created_at       TIMESTAMP DEFAULT NOW(),
    updated_at       TIMESTAMP DEFAULT NOW()
);

-- ============================================================
-- FIXTURES
-- ============================================================
CREATE TABLE IF NOT EXISTS fixtures (
    id               SERIAL PRIMARY KEY,
    api_id           INTEGER UNIQUE NOT NULL,
    league_id        INTEGER REFERENCES leagues(id) ON DELETE SET NULL,
    home_team_id     INTEGER REFERENCES teams(id) ON DELETE SET NULL,
    away_team_id     INTEGER REFERENCES teams(id) ON DELETE SET NULL,
    match_date       TIMESTAMP NOT NULL,
    status           VARCHAR(20) DEFAULT 'NS',
    -- Final score
    home_goals       INTEGER,
    away_goals       INTEGER,
    -- Half time
    home_goals_ht    INTEGER,
    away_goals_ht    INTEGER,
    -- Stats
    home_shots       INTEGER,
    away_shots       INTEGER,
    home_shots_on    INTEGER,
    away_shots_on    INTEGER,
    home_possession  DECIMAL(5,2),
    away_possession  DECIMAL(5,2),
    home_corners     INTEGER,
    away_corners     INTEGER,
    home_fouls       INTEGER,
    away_fouls       INTEGER,
    home_yellow      INTEGER,
    away_yellow      INTEGER,
    home_red         INTEGER,
    away_red         INTEGER,
    referee          VARCHAR(100),
    created_at       TIMESTAMP DEFAULT NOW(),
    updated_at       TIMESTAMP DEFAULT NOW()
);

-- ============================================================
-- ODDS
-- ============================================================
CREATE TABLE IF NOT EXISTS odds (
    id              SERIAL PRIMARY KEY,
    fixture_id      INTEGER REFERENCES fixtures(id) ON DELETE CASCADE,
    bookmaker       VARCHAR(50) NOT NULL,
    home_win_odd    DECIMAL(6,2),
    draw_odd        DECIMAL(6,2),
    away_win_odd    DECIMAL(6,2),
    over_2_5_odd    DECIMAL(6,2),
    under_2_5_odd   DECIMAL(6,2),
    btts_yes_odd    DECIMAL(6,2),
    btts_no_odd     DECIMAL(6,2),
    fetched_at      TIMESTAMP DEFAULT NOW(),
    UNIQUE(fixture_id, bookmaker)
);

-- ============================================================
-- TEAM SEASON STATISTICS
-- ============================================================
CREATE TABLE IF NOT EXISTS team_season_stats (
    id                      SERIAL PRIMARY KEY,
    team_id                 INTEGER REFERENCES teams(id) ON DELETE CASCADE,
    league_id               INTEGER REFERENCES leagues(id) ON DELETE CASCADE,
    season                  INTEGER NOT NULL,
    -- General
    played_home             INTEGER DEFAULT 0,
    played_away             INTEGER DEFAULT 0,
    wins_home               INTEGER DEFAULT 0,
    wins_away               INTEGER DEFAULT 0,
    draws_home              INTEGER DEFAULT 0,
    draws_away              INTEGER DEFAULT 0,
    losses_home             INTEGER DEFAULT 0,
    losses_away             INTEGER DEFAULT 0,
    -- Goals
    goals_for_home          INTEGER DEFAULT 0,
    goals_for_away          INTEGER DEFAULT 0,
    goals_against_home      INTEGER DEFAULT 0,
    goals_against_away      INTEGER DEFAULT 0,
    -- Averages (Poisson lambda)
    avg_goals_for_home      DECIMAL(6,3),
    avg_goals_for_away      DECIMAL(6,3),
    avg_goals_against_home  DECIMAL(6,3),
    avg_goals_against_away  DECIMAL(6,3),
    -- Clean sheets
    clean_sheets_home       INTEGER DEFAULT 0,
    clean_sheets_away       INTEGER DEFAULT 0,
    -- Form (son 5 maç: "WWDLW")
    form                    VARCHAR(10),
    form_points             INTEGER DEFAULT 0,
    -- League position
    league_position         INTEGER,
    points                  INTEGER DEFAULT 0,
    updated_at              TIMESTAMP DEFAULT NOW(),
    UNIQUE(team_id, league_id, season)
);

-- ============================================================
-- HEAD TO HEAD
-- ============================================================
CREATE TABLE IF NOT EXISTS head_to_head (
    id              SERIAL PRIMARY KEY,
    home_team_id    INTEGER REFERENCES teams(id),
    away_team_id    INTEGER REFERENCES teams(id),
    fixture_id      INTEGER REFERENCES fixtures(id),
    result          VARCHAR(10),   -- HOME_WIN, DRAW, AWAY_WIN
    home_goals      INTEGER,
    away_goals      INTEGER,
    match_date      TIMESTAMP,
    UNIQUE(fixture_id)
);

-- ============================================================
-- PREDICTIONS
-- ============================================================
CREATE TABLE IF NOT EXISTS predictions (
    id                      SERIAL PRIMARY KEY,
    fixture_id              INTEGER REFERENCES fixtures(id) ON DELETE CASCADE,
    model_version           VARCHAR(20) NOT NULL,
    -- Probabilities
    home_win_prob           DECIMAL(6,4),
    draw_prob               DECIMAL(6,4),
    away_win_prob           DECIMAL(6,4),
    -- Expected goals
    predicted_home_goals    DECIMAL(5,2),
    predicted_away_goals    DECIMAL(5,2),
    -- Additional markets
    over_2_5_prob           DECIMAL(6,4),
    btts_prob               DECIMAL(6,4),
    -- Model confidence
    confidence_score        DECIMAL(5,4),
    -- Recommendation
    recommendation          VARCHAR(20),
    created_at              TIMESTAMP DEFAULT NOW(),
    UNIQUE(fixture_id, model_version)
);

-- ============================================================
-- USERS
-- ============================================================
CREATE TABLE IF NOT EXISTS users (
    id              SERIAL PRIMARY KEY,
    username        VARCHAR(50) UNIQUE NOT NULL,
    email           VARCHAR(100) UNIQUE NOT NULL,
    password_hash   VARCHAR(255) NOT NULL,
    role            VARCHAR(20) DEFAULT 'USER',
    is_active       BOOLEAN DEFAULT true,
    created_at      TIMESTAMP DEFAULT NOW(),
    updated_at      TIMESTAMP DEFAULT NOW()
);

-- ============================================================
-- USER PREDICTIONS (Leaderboard)
-- ============================================================
CREATE TABLE IF NOT EXISTS user_predictions (
    id                  SERIAL PRIMARY KEY,
    user_id             INTEGER REFERENCES users(id) ON DELETE CASCADE,
    fixture_id          INTEGER REFERENCES fixtures(id) ON DELETE CASCADE,
    predicted_result    VARCHAR(10),    -- "1", "X", "2"
    predicted_home      INTEGER,
    predicted_away      INTEGER,
    points_earned       INTEGER DEFAULT 0,
    is_correct          BOOLEAN,
    created_at          TIMESTAMP DEFAULT NOW(),
    UNIQUE(user_id, fixture_id)
);

-- ============================================================
-- FAVORITE MATCHES
-- ============================================================
CREATE TABLE IF NOT EXISTS user_favorites (
    user_id     INTEGER REFERENCES users(id) ON DELETE CASCADE,
    fixture_id  INTEGER REFERENCES fixtures(id) ON DELETE CASCADE,
    created_at  TIMESTAMP DEFAULT NOW(),
    PRIMARY KEY (user_id, fixture_id)
);

-- ============================================================
-- INDEXES
-- ============================================================
CREATE INDEX IF NOT EXISTS idx_fixtures_date ON fixtures(match_date);
CREATE INDEX IF NOT EXISTS idx_fixtures_status ON fixtures(status);
CREATE INDEX IF NOT EXISTS idx_fixtures_league ON fixtures(league_id);
CREATE INDEX IF NOT EXISTS idx_fixtures_home_team ON fixtures(home_team_id);
CREATE INDEX IF NOT EXISTS idx_fixtures_away_team ON fixtures(away_team_id);
CREATE INDEX IF NOT EXISTS idx_predictions_fixture ON predictions(fixture_id);
CREATE INDEX IF NOT EXISTS idx_odds_fixture ON odds(fixture_id);
CREATE INDEX IF NOT EXISTS idx_team_stats_lookup ON team_season_stats(team_id, season);
CREATE INDEX IF NOT EXISTS idx_user_predictions_user ON user_predictions(user_id);
CREATE INDEX IF NOT EXISTS idx_h2h_teams ON head_to_head(home_team_id, away_team_id);

-- ============================================================
-- INITIAL DATA — Aktif Ligler
-- ============================================================
INSERT INTO leagues (api_id, name, country, season, is_active) VALUES
    (203, 'Süper Lig',              'Turkey',      2024, true),
    (39,  'Premier League',         'England',     2024, true),
    (140, 'La Liga',                'Spain',       2024, true),
    (135, 'Serie A',                'Italy',       2024, true),
    (78,  'Bundesliga',             'Germany',     2024, true),
    (61,  'Ligue 1',                'France',      2024, true),
    (2,   'UEFA Champions League',  'World',       2024, true),
    (3,   'UEFA Europa League',     'World',       2024, true)
ON CONFLICT (api_id) DO NOTHING;
