drop database if exists lapka_db;
create database lapka_db character set utf8mb4 collate utf8mb4_unicode_ci;

use lapka_db;

-- притулки
create table shelters (
    shelter_id int auto_increment primary key,
    name varchar(150) not null,
    city varchar(100) not null,
    phone varchar(20) not null,
    email varchar(150) not null unique,
    address varchar(255),
    password_hash varchar(255),
    registered_at datetime not null default current_timestamp
);

-- тварини
create table pets (
    pet_id int auto_increment primary key,
    shelter_id int not null,
    name varchar(50) not null,
    species enum('dog', 'cat') not null,
    sex enum('male', 'female') not null,
    breed varchar(100) not null default 'метис',
    age_months int not null check (age_months >= 0),
    description text,
    photo varchar(255),
    status enum('available', 'reserved', 'adopted', 'archived') not null default 'available',
    added_at datetime not null default current_timestamp,
    adopted_at date,
    constraint fk_pets_shelters foreign key (shelter_id) references shelters (shelter_id)
        on update cascade
        on delete cascade
);

create index idx_pets_species_status on pets (species, status);

-- медична книжка
create table medical_records (
    record_id int auto_increment primary key,
    pet_id int not null,
    record_type enum('vaccination', 'sterilization', 'parasite_treatment', 'examination', 'other') not null,
    procedure_name varchar(150) not null,
    record_date date not null,
    notes varchar(255),
    constraint fk_medical_pets foreign key (pet_id) references pets (pet_id)
        on update cascade
        on delete cascade
);

-- анкети на усиновлення
create table adoption_applications (
    application_id int auto_increment primary key,
    pet_id int not null,
    full_name varchar(150) not null,
    phone varchar(20) not null,
    reason text,
    status enum('new', 'approved', 'rejected') not null default 'new',
    submitted_at datetime not null default current_timestamp,
    constraint fk_applications_pets foreign key (pet_id) references pets (pet_id)
        on update cascade
        on delete cascade
);

-- звернення зі сторінки зворотного зв'язку
create table contact_messages (
    message_id int auto_increment primary key,
    name varchar(100) not null,
    email varchar(150) not null,
    subject enum('adoption', 'shelter', 'technical', 'other') not null default 'other',
    message text not null,
    sent_at datetime not null default current_timestamp
);