CREATE TABLE IF NOT EXISTS t_agent_session (
  session_id VARCHAR(64) NOT NULL PRIMARY KEY,
  admin_id BIGINT NULL,
  title VARCHAR(120) NULL,
  created_at DATETIME(3) NOT NULL,
  updated_at DATETIME(3) NOT NULL,
  KEY idx_updated (updated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS t_agent_message (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  session_id VARCHAR(64) NOT NULL,
  role VARCHAR(16) NOT NULL,
  content MEDIUMTEXT NOT NULL,
  created_at DATETIME(3) NOT NULL,
  KEY idx_session (session_id, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS t_agent_audit (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  actor VARCHAR(64) NULL,
  action VARCHAR(64) NOT NULL,
  target VARCHAR(120) NULL,
  detail VARCHAR(500) NULL,
  created_at DATETIME(3) NOT NULL,
  KEY idx_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
