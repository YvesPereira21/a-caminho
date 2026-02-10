CREATE TABLE tb_user(
    user_id UUID PRIMARY KEY,
    email VARCHAR(120) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(40) NOT NULL
)

CREATE TABLE tb_state(
    state_id UUID PRIMARY KEY,
    state_name VARCHAR(80) NOT NULL UNIQUE
)

CREATE TABLE tb_city(
    city_id UUID PRIMARY KEY,
    city_name VARCHAR(80) NOT NULL,
    state_id UUID REFERENCES tb_state(state_id)
)

CREATE TABLE tb_municipality(
    municipality_id UUID PRIMARY KEY,
    municipality_name VARCHAR(255) NOT NULL,
    user_id UUID REFERENCES tb_user(user_id),
    city_id UUID REFERENCES tb_city(city_id)
)

CREATE TABLE tb_bus_driver(
    bus_driver_id UUID PRIMARY KEY,
    bus_driver_name VARCHAR(120) NOT NULL,
    user_id UUID REFERENCES tb_user(user_id),
    municipality_id UUID REFERENCES tb_municipality(municipality_id)
)

CREATE TABLE tb_bus(
    bus_id UUID PRIMARY KEY,
    bus_name VARCHAR(80) NOT NULL,
    municipality_id UUID REFERENCES tb_municipality(municipality_id),
    bus_driver_id UUID REFERENCES tb_bus_driver(bus_driver_id)
)

CREATE TABLE tb_university(
    university_id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    city_id UUID REFERENCES tb_city(city_id)
)

CREATE TABLE route_bus(
    bus_id UUID NOT NULL REFERENCES tb_bus(bus_id),
    university_id UUID NOT NULL REFERENCES tb_university(university_id),

    PRIMARY KEY (bus_id, university_id)
)

CREATE TABLE tb_university_student(
    university_student_id UUID PRIMARY KEY,
    student_name VARCHAR(80) NOT NULL,
    user_id UUID REFERENCES tb_user(user_id),
    municipality_id UUID REFERENCES tb_municipality(municipality_id),
    university_id UUID REFERENCES tb_university(university_id)
)
