package bio.ferlab.fhir.app.controller;

import bio.ferlab.fhir.app.service.SchemaService;
import bio.ferlab.fhir.schema.repository.SchemaMode;
import io.swagger.annotations.ApiParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
public class SchemaController {

    @Autowired
    private SchemaService schemaService;

    @Operation(summary = "Generate a schema for a resource", description = "Generate a schema for a specific FHIR v4.0.1 resource")
    @GetMapping(value = "/api/v1/schema/{resource}")
    public String generateSchema(@ApiParam(value = "The resource to generate the schema for") @PathVariable String resource,
                                 @ApiParam(value = "The mode of schema generation. See README.md") @RequestParam(name = "mode", defaultValue = "DEFAULT") SchemaMode mode,
                                 @ApiParam(value = "Whether to pretty print the result or not.") @RequestParam(name = "pretty", defaultValue = "true") boolean pretty) {
        return schemaService.generateSchema(resource, mode, pretty);
    }

    @Operation(summary = "Generate a schema for a resource with a profile", description = "Generate a schema for a FHIR v4.0.1 resource based on a profile.")
    @PostMapping(value = "/api/v1/schema/{resource}/profile", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public String generateSchemaWithProfile(
            @ApiParam(value = "The resource to generate the schema for") @PathVariable String resource,
            @ApiParam(value = "The mode of schema generation. See README.md") @RequestParam(name = "mode", defaultValue = "ADVANCED") SchemaMode mode,
            @ApiParam(value = "The profile to modify the schema with", required = true) @RequestParam("profile") MultipartFile profile,
            @Parameter(description = "The dependencies of the profile", content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE))
            @RequestParam("extensions") List<MultipartFile> extensions) {
        return schemaService.generateSchemaWithProfile(resource, mode, profile, extensions);
    }
}
