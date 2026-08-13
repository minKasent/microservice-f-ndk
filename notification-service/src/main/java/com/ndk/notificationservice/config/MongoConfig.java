package com.ndk.notificationservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

@Configuration
public class MongoConfig {

  @Bean
  public MongoTemplate mongoTemplate(MongoDatabaseFactory factory) {
    MappingMongoConverter converter = new MappingMongoConverter(
        new DefaultDbRefResolver(factory),
        new MongoMappingContext()
    );

    // Remove _class field from documents
    converter.setTypeMapper(new DefaultMongoTypeMapper(null));

    return new MongoTemplate(factory, converter);
  }
}
