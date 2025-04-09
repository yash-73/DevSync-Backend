package com.github.oauth.config;

import com.github.oauth.model.Tech;
import com.github.oauth.repository.TechRepository;
import org.modelmapper.ModelMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
public class AppConfig {

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

    @Bean
    public CommandLineRunner initTechStack(TechRepository techRepository) {
        return args -> {
            List<String> techList = Arrays.asList(
                    "C", "CPP", "JAVA", "JAVASCRIPT", "PYTHON", "GO", "RUST",
                    "RUBY", "PHP", "TYPESCRIPT", "SWIFT", "KOTLIN", "SCALA",
                    "DART", "PERL", "LUA", "HASKELL", "HTML", "CSS", "REACT_JS", "NEXT_JS", "ANGULAR",
                    "VUE_JS", "SVELTE", "SOLIDJS", "ALPINE_JS", "STENCIL", "NODE_JS", "EXPRESS_JS", "SPRING",
                    "SPRING_BOOT",
                    "DJANGO", "FLASK", "RUBY_ON_RAILS", "FASTAPI",
                    "HAPI_JS", "MICRONAUT", "QUARKUS", "ACTIX_WEB", "MYSQL", "POSTGRESQL", "MONGODB", "SQLITE", "REDIS",
                    "FIREBASE", "COUCHDB", "NEO4J", "ARANGO_DB",
                    "COCKROACHDB", "DYNAMODB", "AWS", "AZURE", "GOOGLE_CLOUD", "DOCKER",
                    "KUBERNETES", "KAFKA", "TERRAFORM", "ANSIBLE",
                    "HEROKU", "CLOUDFLARE", "NOMAD", "GIT", "GITHUB_ACTIONS", "JENKINS", "GITLAB_CI",
                    "CIRCLECI", "RABBITMQ", "APACHE_KAFKA", "NATS", "PULSAR",
                    "WEBSOCKETS", "GRAPHQL", "OAUTH", "JWT", "AWS_COGNITO", "FIREBASE_AUTH",
                    "LDAP", "SAML", "ELASTICSEARCH", "PROMETHEUS", "GRAFANA",
                    "LOKI", "NEW_RELIC", "DATADOG", "OPENAI_API");

            // Check if tech stack is already initialized
            if (techRepository.count() == 0) {
                for (String techName : techList) {
                    Tech tech = new Tech(techName);
                    techRepository.save(tech);
                }
            }
        };
    }
}
