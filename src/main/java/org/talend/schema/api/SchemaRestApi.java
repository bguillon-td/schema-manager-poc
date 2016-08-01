package org.talend.schema.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.talend.schema.service.CacheServiceImpl;

@RestController
@RequestMapping("/api/1.0/schemas/{namespace}")
public class SchemaRestApi {

    @Autowired
    private CacheServiceImpl cacheService;

    @RequestMapping(value = "", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
    public List<String> listSchemas(@PathVariable("namespace") String namespace) throws Exception {
        // return cacheService.getSchemaSummaries(namespace).stream().collect(schema->schema.name);
        return null;
    }

}
