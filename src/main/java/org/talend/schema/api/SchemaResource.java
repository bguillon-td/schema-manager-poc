package org.talend.schema.api;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.talend.schema.model.SchemaSummary;
import org.talend.schema.service.CacheServiceImpl;
import org.talend.schema.util.ApiVersioningUtils;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/api/" + ApiVersioningUtils.CURRENT_VERSION + "/schemas/{namespace:.+}")
@Api(value = "schemas", description = "Access and manage schemas")
public class SchemaResource {

    @Autowired
    private CacheServiceImpl cacheService;

    @RequestMapping(value = "", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
    @ApiOperation(value = "List all schemas available for a given namespace", notes = "Only provides the summary of each schema.")
    public Collection<SchemaSummary> listSchemas(@PathVariable("namespace") String namespace) throws Exception {
        return cacheService.getSchemaSummaries(namespace);
    }

}
