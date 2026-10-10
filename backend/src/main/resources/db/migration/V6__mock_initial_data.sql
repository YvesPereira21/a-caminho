-- V6: Insercao de dados mockados para desenvolvimento e testes
-- Senha de todos os usuarios cadastrados: 12345678 (hash BCrypt: $2a$10$OnB4DuNsMpbqwol8Gcflh.ILWyvqPYfHiasNgurjbmJlNAqheIYv.)

-- 1. Estados (States)
INSERT INTO states (state_id, state_name) VALUES
    ('a0000000-0000-0000-0000-000000000001', 'Paraíba'),
    ('a0000000-0000-0000-0000-000000000002', 'Pernambuco');

-- 2. Cidades (Cities)
INSERT INTO cities (city_id, city_name, state_id) VALUES
    ('b0000000-0000-0000-0000-000000000001', 'Guarabira', 'a0000000-0000-0000-0000-000000000001'),
    ('b0000000-0000-0000-0000-000000000002', 'Solânea', 'a0000000-0000-0000-0000-000000000001'),
    ('b0000000-0000-0000-0000-000000000003', 'João Pessoa', 'a0000000-0000-0000-0000-000000000001'),
    ('b0000000-0000-0000-0000-000000000004', 'Campina Grande', 'a0000000-0000-0000-0000-000000000001'),
    ('b0000000-0000-0000-0000-000000000005', 'Recife', 'a0000000-0000-0000-0000-000000000002');

-- 3. Universidades (Universities)
INSERT INTO universities (university_id, name, campus, city_id) VALUES
    ('c0000000-0000-0000-0000-000000000001', 'Universidade Federal da Paraíba', 'Campus I - João Pessoa', 'b0000000-0000-0000-0000-000000000003'),
    ('c0000000-0000-0000-0000-000000000002', 'Universidade Federal de Campina Grande', 'Campus Central - Campina Grande', 'b0000000-0000-0000-0000-000000000004'),
    ('c0000000-0000-0000-0000-000000000003', 'Universidade Estadual da Paraíba', 'Campus III - Guarabira', 'b0000000-0000-0000-0000-000000000001'),
    ('c0000000-0000-0000-0000-000000000004', 'Instituto Federal da Paraíba', 'Campus Guarabira', 'b0000000-0000-0000-0000-000000000001');

-- 4. Usuários (Users) - Todos com senha '12345678'
INSERT INTO users (user_id, email, password, role) VALUES
    ('d0000000-0000-0000-0000-000000000001', 'admin@acaminho.com', '$2a$10$OnB4DuNsMpbqwol8Gcflh.ILWyvqPYfHiasNgurjbmJlNAqheIYv.', 'ADMIN'),
    ('d0000000-0000-0000-0000-000000000002', 'prefeitura.guarabira@acaminho.com', '$2a$10$OnB4DuNsMpbqwol8Gcflh.ILWyvqPYfHiasNgurjbmJlNAqheIYv.', 'MUNICIPALITY'),
    ('d0000000-0000-0000-0000-000000000003', 'prefeitura.solanea@acaminho.com', '$2a$10$OnB4DuNsMpbqwol8Gcflh.ILWyvqPYfHiasNgurjbmJlNAqheIYv.', 'MUNICIPALITY'),
    ('d0000000-0000-0000-0000-000000000004', 'motorista.carlos@acaminho.com', '$2a$10$OnB4DuNsMpbqwol8Gcflh.ILWyvqPYfHiasNgurjbmJlNAqheIYv.', 'BUS_DRIVER'),
    ('d0000000-0000-0000-0000-000000000005', 'motorista.jose@acaminho.com', '$2a$10$OnB4DuNsMpbqwol8Gcflh.ILWyvqPYfHiasNgurjbmJlNAqheIYv.', 'BUS_DRIVER'),
    ('d0000000-0000-0000-0000-000000000006', 'estudante.lucas@acaminho.com', '$2a$10$OnB4DuNsMpbqwol8Gcflh.ILWyvqPYfHiasNgurjbmJlNAqheIYv.', 'STUDENT'),
    ('d0000000-0000-0000-0000-000000000007', 'estudante.mariana@acaminho.com', '$2a$10$OnB4DuNsMpbqwol8Gcflh.ILWyvqPYfHiasNgurjbmJlNAqheIYv.', 'STUDENT');

-- 5. Municípios (Municipalities)
INSERT INTO municipalities (municipality_id, municipality_name, user_id, city_id) VALUES
    ('e0000000-0000-0000-0000-000000000001', 'Prefeitura Municipal de Guarabira', 'd0000000-0000-0000-0000-000000000002', 'b0000000-0000-0000-0000-000000000001'),
    ('e0000000-0000-0000-0000-000000000002', 'Prefeitura Municipal de Solânea', 'd0000000-0000-0000-0000-000000000003', 'b0000000-0000-0000-0000-000000000002');

-- 6. Ônibus (Buses)
INSERT INTO buses (bus_id, bus_name, seats_quantity, municipality_id) VALUES
    ('f0000000-0000-0000-0000-000000000001', 'Ônibus Universitário 01 - Mercedes Benz', 46, 'e0000000-0000-0000-0000-000000000001'),
    ('f0000000-0000-0000-0000-000000000002', 'Ônibus Universitário 02 - Marcopolo Volare', 32, 'e0000000-0000-0000-0000-000000000001'),
    ('f0000000-0000-0000-0000-000000000003', 'Ônibus Solânea Linha JP', 44, 'e0000000-0000-0000-0000-000000000002');

-- 7. Motoristas (Bus Drivers)
INSERT INTO bus_drivers (bus_driver_id, bus_driver_name, cpf, user_id, municipality_id) VALUES
    ('10000000-0000-0000-0000-000000000001', 'Carlos Silva dos Santos', '111.222.333-44', 'd0000000-0000-0000-0000-000000000004', 'e0000000-0000-0000-0000-000000000001'),
    ('10000000-0000-0000-0000-000000000002', 'José Alves de Oliveira', '555.666.777-88', 'd0000000-0000-0000-0000-000000000005', 'e0000000-0000-0000-0000-000000000001');

-- 8. Estudantes Universitários (University Students)
INSERT INTO university_students (university_student_id, student_name, cpf, registration_number, course_name, current_period, admission_date, user_id, municipality_id, university_id) VALUES
    ('20000000-0000-0000-0000-000000000001', 'Lucas Gabriel Ferreira', '123.456.789-00', '2023001452', 'Engenharia da Computação', 5, '2023-02-15', 'd0000000-0000-0000-0000-000000000006', 'e0000000-0000-0000-0000-000000000001', 'c0000000-0000-0000-0000-000000000001'),
    ('20000000-0000-0000-0000-000000000002', 'Mariana Rocha Lima', '987.654.321-11', '2022008731', 'Medicina', 7, '2022-08-10', 'd0000000-0000-0000-0000-000000000007', 'e0000000-0000-0000-0000-000000000001', 'c0000000-0000-0000-0000-000000000001');

-- 9. Pontos de Parada / Embarque (Poll Options)
INSERT INTO poll_options (option_id, stop_name, municipality_id) VALUES
    ('30000000-0000-0000-0000-000000000001', 'Praça da Juventude (Centro)', 'e0000000-0000-0000-0000-000000000001'),
    ('30000000-0000-0000-0000-000000000002', 'Posto Frei Damião', 'e0000000-0000-0000-0000-000000000001'),
    ('30000000-0000-0000-0000-000000000003', 'Terminal Rodoviário Estadual', 'e0000000-0000-0000-0000-000000000001'),
    ('30000000-0000-0000-0000-000000000004', 'Praça Central de Solânea', 'e0000000-0000-0000-0000-000000000002');

-- 10. Modelos de Rota (Poll Templates)
INSERT INTO poll_templates (template_id, route_name, shift, default_start_time, default_end_time, active, municipality_id) VALUES
    ('40000000-0000-0000-0000-000000000001', 'Guarabira -> João Pessoa (UFPB)', 'MANHA', '05:30:00', '13:00:00', TRUE, 'e0000000-0000-0000-0000-000000000001'),
    ('40000000-0000-0000-0000-000000000002', 'Guarabira -> Campina Grande (UFCG)', 'NOITE', '17:00:00', '23:30:00', TRUE, 'e0000000-0000-0000-0000-000000000001');

-- Associativas do PollTemplate
INSERT INTO template_universities (template_id, university_id) VALUES
    ('40000000-0000-0000-0000-000000000001', 'c0000000-0000-0000-0000-000000000001'),
    ('40000000-0000-0000-0000-000000000002', 'c0000000-0000-0000-0000-000000000002');

INSERT INTO template_options (template_id, option_id) VALUES
    ('40000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001'),
    ('40000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000002'),
    ('40000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000003'),
    ('40000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000001');

-- 11. Enquetes (Polls)
INSERT INTO polls (poll_id, route_name, poll_date, shift, start_time, end_time, created_at, municipality_id) VALUES
    ('50000000-0000-0000-0000-000000000001', 'Guarabira -> João Pessoa (UFPB)', CURRENT_DATE, 'MANHA', CURRENT_DATE + TIME '05:00:00', CURRENT_DATE + INTERVAL '1 day' + TIME '23:59:59', NOW(), 'e0000000-0000-0000-0000-000000000001'),
    ('50000000-0000-0000-0000-000000000002', 'Guarabira -> Campina Grande (UFCG)', CURRENT_DATE, 'NOITE', CURRENT_DATE + TIME '16:00:00', CURRENT_DATE + INTERVAL '1 day' + TIME '23:59:59', NOW(), 'e0000000-0000-0000-0000-000000000001');

-- Associativas da Poll
INSERT INTO poll_universities (poll_id, university_id) VALUES
    ('50000000-0000-0000-0000-000000000001', 'c0000000-0000-0000-0000-000000000001'),
    ('50000000-0000-0000-0000-000000000002', 'c0000000-0000-0000-0000-000000000002');

INSERT INTO poll_options_rel (poll_id, option_id) VALUES
    ('50000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001'),
    ('50000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000002'),
    ('50000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000003'),
    ('50000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000001');

-- 12. Votos de Enquete (Poll Votes)
INSERT INTO poll_votes (vote_id, vote_time, return_confirmed, poll_id, option_id, student_id) VALUES
    ('60000000-0000-0000-0000-000000000001', NOW(), TRUE, '50000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000001'),
    ('60000000-0000-0000-0000-000000000002', NOW(), TRUE, '50000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000002', '20000000-0000-0000-0000-000000000002');

-- 13. Viagens (Travels)
INSERT INTO travels (travel_id, travel_date, status, direction, shift, departure_time, return_time, estimated_duration_minutes, bus_id, bus_driver_id, poll_id, municipality_id) VALUES
    ('70000000-0000-0000-0000-000000000001', CURRENT_DATE, 'IN_PROGRESS', 'OUTBOUND', 'MANHA', '05:45:00', '13:00:00', 90, 'f0000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001', '50000000-0000-0000-0000-000000000001', 'e0000000-0000-0000-0000-000000000001');

-- Associativa Travel com University
INSERT INTO travel_universities (travel_id, university_id) VALUES
    ('70000000-0000-0000-0000-000000000001', 'c0000000-0000-0000-0000-000000000001');

