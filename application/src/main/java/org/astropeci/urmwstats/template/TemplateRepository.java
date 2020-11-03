package org.astropeci.urmwstats.template;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Slf4j
@Component
public class TemplateRepository {

    private final MongoDatabase db;
    private final Map<String, Template> templates = new HashMap<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public TemplateRepository(MongoDatabase db) {
        this.db = db;

        MongoCollection<SavedTemplate> collection = db.getCollection("templates", SavedTemplate.class);
        List<SavedTemplate> savedTemplates = collection.find().into(new ArrayList<>());

        log.info("Compiling {} saved templates", savedTemplates.size());
        for (SavedTemplate savedTemplate : savedTemplates) {
            try {
                Template template = Template.compile(savedTemplate.getSource());
                templates.put(savedTemplate.getName(), template);
            } catch (TemplateCompileException e) {
                log.error("Could not compile saved template {}", savedTemplate.getName(), e);
            }
        }
    }

    public Template save(String name, String source) {
        Template template = Template.compile(source);
        SavedTemplate savedTemplate = new SavedTemplate(name, source);

        @Cleanup("unlock") Lock lock = this.lock.writeLock();
        lock.lock();

        log.info("Saving template {}", name);
        MongoCollection<SavedTemplate> collection = db.getCollection("templates", SavedTemplate.class);
        collection.replaceOne(Filters.eq("name", name), savedTemplate, new ReplaceOptions().upsert(true));

        templates.put(name, template);

        return template;
    }

    public boolean delete(String name) {
        @Cleanup("unlock") Lock lock = this.lock.writeLock();
        lock.lock();

        log.info("Deleting template {}", name);
        MongoCollection<SavedTemplate> collection = db.getCollection("templates", SavedTemplate.class);
        collection.deleteOne(Filters.eq("name", name));

        if (templates.remove(name) == null) {
            return false;
        }

        return true;
    }

    public String getSource(String name) {
        @Cleanup("unlock") Lock lock = this.lock.writeLock();
        lock.lock();

        MongoCollection<SavedTemplate> collection = db.getCollection("templates", SavedTemplate.class);
        SavedTemplate savedTemplate = collection.find(Filters.eq("name", name)).first();
        return savedTemplate == null ? null : savedTemplate.getSource();
    }

    public Template get(String name) {
        @Cleanup("unlock") Lock lock = this.lock.readLock();
        lock.lock();
        return templates.get(name);
    }

    public Map<String, Template> all() {
        @Cleanup("unlock") Lock lock = this.lock.readLock();
        lock.lock();
        return new HashMap<>(templates);
    }
}
