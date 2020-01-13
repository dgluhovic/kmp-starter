create TABLE users (
    id serial primary key,
    name varchar(255) not null,
    email varchar(255) unique,
    password varchar(255) not null,
    created_at timestamptz not null default current_timestamp,
    updated_at timestamptz not null default current_timestamp
);

create TABLE addresses (
    id serial primary key,
    user_id integer references users(id),
    street varchar(255),
    city varchar(255),
    county varchar(255),
    state varchar(255),
    country varchar(255),
    postal_code varchar(255),
    location geometry not null,
    is_primary bool,
    created_at timestamptz not null default current_timestamp,
    updated_at timestamptz not null default current_timestamp
);

CREATE INDEX addresses_idx ON addresses USING GIST (geography(location));
