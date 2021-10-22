package bio.ferlab.fhir.app.controller;

import bio.ferlab.fhir.app.Response;
import bio.ferlab.fhir.app.service.QueryService;
import bio.ferlab.fhir.schema.repository.SchemaMode;
import io.swagger.annotations.ApiParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@RestController
public class QueryController {

    @Autowired
    private QueryService queryService;

    @Operation(
            summary = "Query and verify the validity of the Fhavro library",
            description = "Query FHIR resource and then verify if the serialization/deserialization works as intended"
    )
    @GetMapping(value = "/api/v1/query/{resource}")
    public List<Response> query(@ApiParam(value = "The name of the FHIR resource.", required = true) @PathVariable String resource,
                                @ApiParam(value = "The mode of schema.", required = true) @RequestParam(name = "mode", defaultValue = "DEFAULT") SchemaMode mode,
                                @RequestParam(name = "id", required = false) String id,
                                @RequestParam(name = "count", defaultValue = "10") int count) {
        if (StringUtils.isNotBlank(id)) {
            return List.of(queryService.queryById(resource, mode, id));
        } else {
            return queryService.query(resource, mode, count);
        }
    }

    @Schema(hidden = true)
    @Operation(summary = "Generate an Avro file", description = "Generate an Avro file with the content obtained from the query results")
    @PostMapping(value = "/api/v1/query/{resource}/serialize")
    public ResponseEntity<StreamingResponseBody> serialize(@ApiParam(value = "The name of the FHIR resource.", required = true) @PathVariable String resource,
                                                           @ApiParam(value = "The mode of schema.", required = true) @RequestParam(name = "mode", defaultValue = "DEFAULT") SchemaMode mode,
                                                           @RequestParam(name = "count", defaultValue = "10") int count,
                                                           final HttpServletResponse response) {
        response.setHeader("Content-Disposition", "attachment;filename=" + resource + ".avro");
        return new ResponseEntity<>(outputStream -> queryService.serialize(outputStream, resource, mode, count), HttpStatus.OK);
    }

    @Operation(summary = "Read an Avro file and return the content in JSON", description = "Read an Avro file and return the content in JSON.")
    @PostMapping(value = "/api/v1/query/{resource}/deserialize", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public List<String> deserialize(@ApiParam(value = "The name of the FHIR resource.") @PathVariable String resource,
                                    @ApiParam(value = "The mode of schema.") @RequestParam(name = "mode", defaultValue = "DEFAULT") SchemaMode mode,
                                    @ApiParam(value = "The Avro file to be read", required = true) @RequestParam("avroFile") MultipartFile avroFile) throws IOException {
        return queryService.deserialize(avroFile, resource, mode);
    }
}
