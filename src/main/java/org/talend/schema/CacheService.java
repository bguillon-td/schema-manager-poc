package org.talend.schema;

import java.util.Collection;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.lang3.NotImplementedException;
import org.springframework.stereotype.Component;

import com.google.common.collect.Table;
import com.google.common.collect.TreeBasedTable;

@Component
public class CacheService {

    private Table<String, String, SchemaSummary> schemaTable;

    @PostConstruct
    public void setup() {
        schemaTable = TreeBasedTable.create();
    }

    @PreDestroy
    public void destroy() {
        schemaTable.clear();
    }

    public Collection<SchemaSummary> getSchemas(String namespace) {
        throw new NotImplementedException("Not yet implemented");
    }

    public SchemaSummary getSchema(String namespace, String name) {
        throw new NotImplementedException("Not yet implemented");
    }

    public synchronized void put(String namespace, String name, SchemaSummary schemaSummary) {
        SchemaSummary previousSchema = schemaTable.get(namespace, name);
        if (previousSchema == null || previousSchema.getVersion().compareTo(schemaSummary.getVersion()) < 0) {
            this.schemaTable.put(namespace, name, schemaSummary);
        }
    }

    protected Table<String, String, SchemaSummary> getSchemaTable() {
        return this.schemaTable;
    }
}
