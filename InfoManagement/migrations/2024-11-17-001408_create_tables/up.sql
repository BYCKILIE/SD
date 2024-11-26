CREATE TABLE devices
(
    id UUID PRIMARY KEY,
    value DOUBLE PRECISION NOT NULL
);

CREATE TABLE energy
(
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    device_id UUID,
    current DOUBLE PRECISION NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (device_id) REFERENCES devices(id)
);