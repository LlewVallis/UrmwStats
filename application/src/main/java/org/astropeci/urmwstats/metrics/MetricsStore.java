package org.astropeci.urmwstats.metrics;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class MetricsStore implements DisposableBean {

    private final MongoDatabase db;
    private final Metrics metrics;

    public MetricsStore(MongoDatabase db) {
        MongoCollection<Metrics> collection = db.getCollection("metrics", Metrics.class);
        List<Metrics> documents = collection.find().into(new ArrayList<>());

        if (documents.size() == 0) {
            metrics = new Metrics();
        } else {
            if (documents.size() > 1) {
                log.error("Found multiple metrics documents");
            }

            metrics = documents.get(0);
        }

        this.db = db;
    }

    @Synchronized
    @Scheduled(fixedRate = 60000)
    private void save() {
        MongoCollection<Metrics> collection = db.getCollection("metrics", Metrics.class);
        collection.replaceOne(Filters.exists("_id"), metrics, new ReplaceOptions().upsert(true));
    }

    @Synchronized
    public void doggoProvided() {
        metrics.setDoggosProvided(metrics.getDoggosProvided() + 1);
    }

    @Synchronized
    public void commandRun() {
        metrics.setCommandsRun(metrics.getCommandsRun() + 1);
    }

    @Synchronized
    public Metrics getMetrics() {
        return new Metrics(metrics);
    }

    @Override
    public void destroy() {
        save();
    }
}
