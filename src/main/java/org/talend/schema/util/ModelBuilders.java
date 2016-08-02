package org.talend.schema.util;

import org.talend.schema.model.SchemaSummary;

/**
 * Fluent API to build model objects
 */
public class ModelBuilders {

    private ModelBuilders() {
        // empty private constructor for utility class
    }

    public static class SchemaSummaryBuilder {

        private SchemaSummary schemaSummary;

        public SchemaSummaryBuilder() {
            this.schemaSummary = new SchemaSummary();
        }

        public SchemaSummaryBuilder namespace(String namespace) {
            this.schemaSummary.setNamespace(namespace);
            return this;
        }

        public SchemaSummaryBuilder name(String name) {
            this.schemaSummary.setName(name);
            return this;
        }

        public SchemaSummaryBuilder description(String description) {
            this.schemaSummary.setDescription(description);
            return this;
        }

        public SchemaSummaryBuilder version(Integer version) {
            this.schemaSummary.setVersion(version);
            return this;
        }

        public SchemaSummary build() {
            return this.schemaSummary;
        }
    }

}
