package com.example.sample;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.elasticsearch.config.EnableElasticsearchAuditing;

@EnableElasticsearchAuditing
@SpringBootApplication
public class Java25SpringbootSampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(Java25SpringbootSampleApplication.class, args);
    }

}
