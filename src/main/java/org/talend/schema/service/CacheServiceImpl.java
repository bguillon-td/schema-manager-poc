package org.talend.schema.service;

import java.util.Optional;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.talend.schema.model.SchemaSummary;

import com.google.common.collect.Table;
import com.google.common.collect.TreeBasedTable;

@Service
public class CacheServiceImpl implements CacheService {

    private static final Logger LOG = LoggerFactory.getLogger(CacheServiceImpl.class);

    private Table<String, String, SchemaSummary> schemaTable;

    @PostConstruct
    public void setup() {
        schemaTable = TreeBasedTable.create();
    }

    @PreDestroy
    public void destroy() {
        schemaTable.clear();
    }

    @Override
    public Stream<SchemaSummary> getSchemaSummaries(String namespace) {
        return this.schemaTable.row(namespace).values().stream();
    }

    @Override
    public Optional<SchemaSummary> getSchemaSummary(String namespace, String name) {
        return Optional.ofNullable(this.schemaTable.get(namespace, name));
    }

    @Override
    public synchronized void putSchemaSummary(SchemaSummary schemaSummary) {
        SchemaSummary previousSchema = schemaTable.get(schemaSummary.getNamespace(), schemaSummary.getName());
        if (previousSchema == null || previousSchema.getVersion().compareTo(schemaSummary.getVersion()) < 0) {
            this.schemaTable.put(schemaSummary.getNamespace(), schemaSummary.getName(), schemaSummary);
            LOG.info("cache updated with schema summary = " + schemaSummary);
        }
    }

    protected Table<String, String, SchemaSummary> getSchemaTable() {
        return this.schemaTable;
    }
}
