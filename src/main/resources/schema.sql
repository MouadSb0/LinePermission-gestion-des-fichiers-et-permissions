PRAGMA foreign_keys = ON;

CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    login VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_users_login_not_blank CHECK (length(trim(login)) > 0),
    CONSTRAINT ck_users_password_hash_not_blank CHECK (length(trim(password_hash)) > 0)
);

CREATE TABLE IF NOT EXISTS fichiers (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nom VARCHAR(255) NOT NULL,
    chemin VARCHAR(1000) NOT NULL UNIQUE,
    proprietaire_id INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_fichiers_nom_not_blank CHECK (length(trim(nom)) > 0),
    CONSTRAINT ck_fichiers_chemin_not_blank CHECK (length(trim(chemin)) > 0),
    CONSTRAINT fk_fichiers_proprietaire
        FOREIGN KEY (proprietaire_id) REFERENCES users(id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS logs (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    fichier_id INTEGER,
    action VARCHAR(50) NOT NULL,
    resultat VARCHAR(20) NOT NULL,
    occurred_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    details VARCHAR(1000),
    CONSTRAINT ck_logs_action_not_blank CHECK (length(trim(action)) > 0),
    CONSTRAINT ck_logs_resultat CHECK (resultat IN ('ACCEPTE', 'REFUSE')),
    CONSTRAINT fk_logs_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT fk_logs_fichier
        FOREIGN KEY (fichier_id) REFERENCES fichiers(id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
);

CREATE INDEX IF NOT EXISTS idx_logs_user_id ON logs(user_id);
CREATE INDEX IF NOT EXISTS idx_logs_fichier_id ON logs(fichier_id);
CREATE INDEX IF NOT EXISTS idx_logs_occurred_at ON logs(occurred_at);

CREATE TRIGGER IF NOT EXISTS prevent_logs_update
BEFORE UPDATE ON logs
BEGIN
    SELECT RAISE(ABORT, 'logs are immutable');
END;

CREATE TRIGGER IF NOT EXISTS prevent_logs_delete
BEFORE DELETE ON logs
BEGIN
    SELECT RAISE(ABORT, 'logs are immutable');
END;
