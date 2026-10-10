create Table users
(
    user_id uuid primary key default gen_random_uuid(),
    email varchar(255) not null unique,
    password varchar(255) not null,
    name varchar(50),
    phone varchar(20),
    role varchar(20) default 'USER' check ( role in ('USER', 'ADMIN') ),
    created_at timestamptz not null default now()
);

create table venues
(
    venue_id   uuid primary key default gen_random_uuid(),
    name       varchar(100) not null,
    address    varchar(255),
    created_at timestamptz  not null default now()
);

create table concerts
(
    concert_id uuid primary key default gen_random_uuid(),
    venue_id   uuid         not null references venues (venue_id) on delete restrict,
    title      varchar(255) not null,
    artist     varchar(255),
    genre      varchar(50),
    created_at timestamptz  not null default now()
);

create table concert_schedules
(
    schedule_id     uuid primary key default gen_random_uuid(),
    concert_id      uuid        not null references concerts (concert_id) on delete restrict,
    performance_at  timestamptz not null,
    booking_open_at timestamptz not null,
    created_at      timestamptz not null default now(),
    constraint uk_schedules_concert_performance unique (concert_id, performance_at),
    constraint ck_schedules_open_before_performance check (booking_open_at < performance_at)
);

create table seats
(
    seat_id     uuid primary key default gen_random_uuid(),
    schedule_id uuid        not null references concert_schedules (schedule_id) on delete restrict,
    section     varchar(10) not null,
    seat_row    integer     not null,
    seat_number integer     not null,
    grade       varchar(20) not null check (grade in ('VIP', 'R', 'S', 'A')),
    price       integer     not null check (price >= 0),
    status      varchar(20) not null default 'AVAILABLE' check (status in ('AVAILABLE', 'HELD', 'SOLD')),
    constraint uk_seats_position unique (schedule_id, section, seat_row, seat_number),
    constraint ck_seats_row_positive check (seat_row > 0),
    constraint ck_seats_number_positive check (seat_number > 0)
);