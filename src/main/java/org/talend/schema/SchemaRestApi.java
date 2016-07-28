package org.talend.schema;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/1.0/schemas/{namespace}")
public class SchemaRestApi {

    @RequestMapping(value="", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<String> listSchemas(
            @PathVariable("namespace") String namespace) throws Exception {
        return Collections.singletonList("Hello: " + namespace);

    }

}
