CREATE TABLE IF NOT EXISTS plants (
    id INT PRIMARY KEY NOT NUll,
    name TEXT NOT NULL,
    description TEXT,
    url TEXT,
    watering_cycle BIGINT,
    last_watered BIGINT,
    watering_deadline BIGINT
)