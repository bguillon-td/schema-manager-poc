package org.talend.schema.service;

import java.util.Optional;
import java.util.stream.Stream;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.talend.schema.model.SchemaSummary;

@Validated
public interface CacheService {

    /**
     * Get the list of schema summaries, given a namespace
     *
     * @param namespace the namespace to filter on
     * @return the list of schema summaries matching the namespace
     */
    Stream<SchemaSummary> getSchemaSummaries(@NotBlank(message = "error.schema.namespace.null") String namespace);

    /**
     * Get the schema summary matching the given namespace and name
     * 
     * @param namespace the namespace to filter on
     * @param name the name to filter on
     * @return the matching schema, or null if no schema matches
     */
    Optional<SchemaSummary> getSchemaSummary(@NotBlank(message = "error.schema.namespace.null") String namespace,
            @NotBlank(message = "error.schema.name.null") String name);

    /**
     * Put a schema summary into the cache.
     * The schema is stored only if there is no existing schema matching
     * the given namespace and name, or if the existing schema has a previous version.
     * Thus, if a schema summary exists with a newer version, it is not updated.
     *
     * The writes are atomic, to allow multiple thread to write into the cache.
     *
     * @param schemaSummary the schema summary to be stored
     */
    void putSchemaSummary(@NotNull(message = "error.schema.summary.null") SchemaSummary schemaSummary);
}
