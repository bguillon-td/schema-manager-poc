package org.talend.schema.api;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.collection.IsMapContaining.hasEntry;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.talend.schema.TestIntegrationAbstract;
import org.talend.schema.model.SchemaSummary;
import org.talend.schema.service.CacheServiceImpl;
import org.talend.schema.util.ApiVersioningUtils;
import org.talend.schema.util.ModelBuilders;

import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;

public class TestSchemaResource extends TestIntegrationAbstract {

    private static String GENERAL_OPERATIONS_API = "/api/" + ApiVersioningUtils.CURRENT_VERSION + "/schemas";

    @Autowired
    private CacheServiceImpl cacheService;

    @Test
    public void testListSchemas() throws Exception {
        // an empty list is expected when no schemas is stored
        Response response = given().contentType(ContentType.JSON).get(GENERAL_OPERATIONS_API + "/org.talend.schema");
        response.then().statusCode(HttpStatus.OK.value()).body("", Matchers.hasSize(0));

        // an empty list is expected when stored schemas do not match the filtering namespace, even if the filtering namespace is
        // an ancestry namespace
        SchemaSummary schemaSummary1 = new ModelBuilders.SchemaSummaryBuilder().name("b_record")
                .namespace("org.talend.schema.subpackage").description("doc1").version(0).build();
        SchemaSummary schemaSummary2 = new ModelBuilders.SchemaSummaryBuilder().name("a_record")
                .namespace("org.talend.schema.subpackage").description("doc2").version(1).build();
        SchemaSummary schemaSummary3 = new ModelBuilders.SchemaSummaryBuilder().name("z_record")
                .namespace("org.talend.schema.subpackage").description("doc3").version(3).build();
        cacheService.putSchemaSummary(schemaSummary1);
        cacheService.putSchemaSummary(schemaSummary2);
        cacheService.putSchemaSummary(schemaSummary3);
        response = given().contentType(ContentType.JSON).get(GENERAL_OPERATIONS_API + "/org.talend.schema");
        response.then().statusCode(HttpStatus.OK.value()).body("", Matchers.hasSize(0));

        // a name sorted list is expected when the filtering namespace matchs stored schemas
        response = given().contentType(ContentType.JSON).get(GENERAL_OPERATIONS_API + "/org.talend.schema.subpackage");
        response.then().statusCode(HttpStatus.OK.value()).body("", Matchers.hasSize(3)).body("",
                Matchers.contains(
                        Matchers.allOf(hasEntry("name", "a_record"), hasEntry("description", "doc2"),
                                hasEntry("namespace", "org.talend.schema.subpackage")),
                        hasEntry("name", "b_record"), hasEntry("name", "z_record")));

        //empty namespace
        response = given().contentType(ContentType.JSON).get(GENERAL_OPERATIONS_API + "/");
        response.then().statusCode(HttpStatus.NOT_FOUND.value());

        //null namespace
        response = given().contentType(ContentType.JSON).get(GENERAL_OPERATIONS_API + "/null");
        response.then().statusCode(HttpStatus.OK.value()).body("", Matchers.hasSize(0));
    }
}