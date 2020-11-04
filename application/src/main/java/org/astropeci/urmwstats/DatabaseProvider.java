package org.astropeci.urmwstats;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import org.astropeci.urmwstats.poll.Poll;
import org.astropeci.urmwstats.template.LiveTemplateTracker;
import org.astropeci.urmwstats.template.SavedTemplate;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DatabaseProvider {

    @Bean
    public MongoClient mongoClient(SecretProvider secretProvider) {
        CodecRegistry codecRegistry = CodecRegistries.fromRegistries(
                MongoClientSettings.getDefaultCodecRegistry(),
                CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build())
        );

        MongoClientSettings settings = MongoClientSettings.builder()
                .codecRegistry(codecRegistry)
                .applyConnectionString(new ConnectionString(secretProvider.getMongoUri()))
                .build();

        return MongoClients.create(settings);
    }

    @Bean
    public MongoDatabase mongoDatabase(MongoClient client) {
        MongoDatabase db = client.getDatabase("urmw-stats");

        MongoCollection<Poll> polls = db.getCollection("polls", Poll.class);
        polls.createIndex(Indexes.ascending("name"), new IndexOptions().name("name").unique(true));

        MongoCollection<SavedTemplate> templates = db.getCollection("templates", SavedTemplate.class);
        templates.createIndex(Indexes.ascending("name"), new IndexOptions().name("name").unique(true));

        MongoCollection<LiveTemplateTracker> liveTemplates = db.getCollection("liveTemplates", LiveTemplateTracker.class);
        liveTemplates.createIndex(Indexes.ascending("messageId"), new IndexOptions().name("messageId").unique(true));

        return db;
    }
}
