package io.github.YvesPereira21.acaminho.config;

import org.springframework.boot.flyway.autoconfigure.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class FlywayConfig {

    @Bean
    @Profile("development") // Ativa esta estratégia apenas quando o perfil "development" estiver ativo
    public FlywayMigrationStrategy cleanMigrateStrategy() {
        return flyway -> {
            // Função para limpar o banco de dados, apagando todas as tabelas.
            flyway.clean();

            // Função para executar as migrações para recriar o banco do zero.
            flyway.migrate();
        };
    }
}
