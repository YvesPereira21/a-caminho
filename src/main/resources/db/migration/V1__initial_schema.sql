-- 1. States
CREATE TABLE states (
    state_id UUID PRIMARY KEY,
    state_name VARCHAR(255) NOT NULL
);

-- 2. Cities
CREATE TABLE cities (
    city_id UUID PRIMARY KEY,
    city_name VARCHAR(255) NOT NULL,
    state_id UUID,
    CONSTRAINT fk_cities_state FOREIGN KEY (state_id) REFERENCES states(state_id)
);

-- 3. Users
CREATE TABLE users (
    user_id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL
);

-- 4. Municipalities
CREATE TABLE municipalities (
    municipality_id UUID PRIMARY KEY,
    municipality_name VARCHAR(255) NOT NULL,
    user_id UUID,
    city_id UUID,
    CONSTRAINT fk_municipalities_user FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT fk_municipalities_city FOREIGN KEY (city_id) REFERENCES cities(city_id)
);

-- 5. Universities
CREATE TABLE universities (
    university_id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    campus VARCHAR(255),
    city_id UUID,
    CONSTRAINT fk_universities_city FOREIGN KEY (city_id) REFERENCES cities(city_id)
);

-- 6. Buses
CREATE TABLE buses (
    bus_id UUID PRIMARY KEY,
    bus_name VARCHAR(255) NOT NULL,
    seats_quantity INTEGER,
    municipality_id UUID,
    CONSTRAINT fk_buses_municipality FOREIGN KEY (municipality_id) REFERENCES municipalities(municipality_id)
);

-- 7. Bus Drivers
CREATE TABLE bus_drivers (
    bus_driver_id UUID PRIMARY KEY,
    bus_driver_name VARCHAR(255) NOT NULL,
    cpf VARCHAR(14) NOT NULL UNIQUE,
    user_id UUID,
    municipality_id UUID,
    CONSTRAINT fk_bus_drivers_user FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT fk_bus_drivers_municipality FOREIGN KEY (municipality_id) REFERENCES municipalities(municipality_id)
);

-- 9. University Students
CREATE TABLE university_students (
    university_student_id UUID PRIMARY KEY,
    student_name VARCHAR(255) NOT NULL,
    cpf VARCHAR(14) NOT NULL UNIQUE,
    registration_number VARCHAR(100) UNIQUE,
    course_name VARCHAR(255),
    current_period INTEGER,
    admission_date DATE,
    user_id UUID,
    municipality_id UUID,
    university_id UUID,
    CONSTRAINT fk_university_students_user FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT fk_university_students_municipality FOREIGN KEY (municipality_id) REFERENCES municipalities(municipality_id),
    CONSTRAINT fk_university_students_university FOREIGN KEY (university_id) REFERENCES universities(university_id)
);

-- 10. Poll Options (Pontos de parada/embarque municipais)
CREATE TABLE poll_options (
    option_id UUID PRIMARY KEY,
    stop_name VARCHAR(255) NOT NULL,
    municipality_id UUID NOT NULL,
    CONSTRAINT fk_poll_options_municipality FOREIGN KEY (municipality_id) REFERENCES municipalities(municipality_id)
);

-- 11. Poll Templates
CREATE TABLE poll_templates (
    template_id UUID PRIMARY KEY,
    route_name VARCHAR(255) NOT NULL,
    shift VARCHAR(50) NOT NULL,
    default_start_time TIME WITHOUT TIME ZONE,
    default_end_time TIME WITHOUT TIME ZONE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    municipality_id UUID NOT NULL,
    CONSTRAINT fk_poll_templates_municipality FOREIGN KEY (municipality_id) REFERENCES municipalities(municipality_id)
);

-- 12. Associativas do PollTemplate
CREATE TABLE template_universities (
    template_id UUID NOT NULL,
    university_id UUID NOT NULL,
    PRIMARY KEY (template_id, university_id),
    CONSTRAINT fk_template_universities_template FOREIGN KEY (template_id) REFERENCES poll_templates(template_id),
    CONSTRAINT fk_template_universities_university FOREIGN KEY (university_id) REFERENCES universities(university_id)
);

CREATE TABLE template_options (
    template_id UUID NOT NULL,
    option_id UUID NOT NULL,
    PRIMARY KEY (template_id, option_id),
    CONSTRAINT fk_template_options_template FOREIGN KEY (template_id) REFERENCES poll_templates(template_id),
    CONSTRAINT fk_template_options_option FOREIGN KEY (option_id) REFERENCES poll_options(option_id)
);

-- 13. Polls
CREATE TABLE polls (
    poll_id UUID PRIMARY KEY,
    route_name VARCHAR(255) NOT NULL,
    poll_date DATE,
    shift VARCHAR(50) NOT NULL,
    start_time TIME WITHOUT TIME ZONE,
    end_time TIME WITHOUT TIME ZONE,
    created_at TIMESTAMP WITHOUT TIME ZONE,
    municipality_id UUID NOT NULL,
    CONSTRAINT fk_polls_municipality FOREIGN KEY (municipality_id) REFERENCES municipalities(municipality_id)
);

-- 14. Associativas de Poll
CREATE TABLE poll_universities (
    poll_id UUID NOT NULL,
    university_id UUID NOT NULL,
    PRIMARY KEY (poll_id, university_id),
    CONSTRAINT fk_poll_universities_poll FOREIGN KEY (poll_id) REFERENCES polls(poll_id),
    CONSTRAINT fk_poll_universities_university FOREIGN KEY (university_id) REFERENCES universities(university_id)
);

CREATE TABLE poll_options_rel (
    poll_id UUID NOT NULL,
    option_id UUID NOT NULL,
    PRIMARY KEY (poll_id, option_id),
    CONSTRAINT fk_poll_options_rel_poll FOREIGN KEY (poll_id) REFERENCES polls(poll_id),
    CONSTRAINT fk_poll_options_rel_option FOREIGN KEY (option_id) REFERENCES poll_options(option_id)
);

-- 15. Poll Votes
CREATE TABLE poll_votes (
    vote_id UUID PRIMARY KEY,
    vote_time TIMESTAMP WITHOUT TIME ZONE,
    return_confirmed BOOLEAN NOT NULL DEFAULT TRUE,
    poll_id UUID NOT NULL,
    option_id UUID NOT NULL,
    student_id UUID NOT NULL,
    CONSTRAINT uk_poll_student UNIQUE (poll_id, student_id),
    CONSTRAINT fk_poll_votes_poll FOREIGN KEY (poll_id) REFERENCES polls(poll_id),
    CONSTRAINT fk_poll_votes_option FOREIGN KEY (option_id) REFERENCES poll_options(option_id),
    CONSTRAINT fk_poll_votes_student FOREIGN KEY (student_id) REFERENCES university_students(university_student_id)
);

-- 16. Travels
CREATE TABLE travels (
    travel_id UUID PRIMARY KEY,
    travel_date DATE NOT NULL,
    status VARCHAR(50) NOT NULL,
    direction VARCHAR(50) NOT NULL DEFAULT 'OUTBOUND',
    cancellation_reason VARCHAR(255),
    shift VARCHAR(50),
    departure_time TIME WITHOUT TIME ZONE,
    pickup_time TIME WITHOUT TIME ZONE,
    return_time TIME WITHOUT TIME ZONE,
    estimated_duration_minutes INTEGER,
    bus_id UUID NOT NULL,
    bus_driver_id UUID NOT NULL,
    poll_id UUID,
    municipality_id UUID NOT NULL,
    CONSTRAINT fk_travels_bus FOREIGN KEY (bus_id) REFERENCES buses(bus_id),
    CONSTRAINT fk_travels_bus_driver FOREIGN KEY (bus_driver_id) REFERENCES bus_drivers(bus_driver_id),
    CONSTRAINT fk_travels_poll FOREIGN KEY (poll_id) REFERENCES polls(poll_id),
    CONSTRAINT fk_travels_municipality FOREIGN KEY (municipality_id) REFERENCES municipalities(municipality_id)
);

-- 17. Associativa de Travel com University
CREATE TABLE travel_universities (
    travel_id UUID NOT NULL,
    university_id UUID NOT NULL,
    PRIMARY KEY (travel_id, university_id),
    CONSTRAINT fk_travel_universities_travel FOREIGN KEY (travel_id) REFERENCES travels(travel_id),
    CONSTRAINT fk_travel_universities_university FOREIGN KEY (university_id) REFERENCES universities(university_id)
);
