package bio.ferlab.fhir.app.service;

import bio.ferlab.fhir.FhavroConverter;
import bio.ferlab.fhir.app.Response;
import bio.ferlab.fhir.converter.ConverterUtils;
import bio.ferlab.fhir.converter.exception.BadRequestException;
import bio.ferlab.fhir.schema.repository.SchemaMode;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.BaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class QueryService {

    @Autowired
    private IGenericClient genericClient;

    @Autowired
    private FhirContext fhirContext;

    public void serialize(OutputStream outputStream, String resource, SchemaMode schemaMode, int count) {
        Schema schema = FhavroConverter.loadSchema(resource, schemaMode);
        List<GenericRecord> genericRecords = genericClient.search()
                .forResource(resource)
                .count(count)
                .returnBundle(Bundle.class)
                .execute()
                .getEntry()
                .stream()
                .map(x -> FhavroConverter.convertResourceToGenericRecord(x.getResource(), schema))
                .collect(Collectors.toList());
        FhavroConverter.serializeGenericRecords(schema, genericRecords, outputStream);
    }

    public List<String> deserialize(MultipartFile file, String resource, SchemaMode schemaMode) {
        Schema schema = FhavroConverter.loadSchema(resource, schemaMode);
        List<GenericRecord> genericRecords = FhavroConverter.deserializeGenericRecords(schema, multipartFileToFile(file));
        return genericRecords.stream()
                .map(x -> FhavroConverter.convertGenericRecordToResource(x, schema, resource))
                .map(x -> ConverterUtils.standardizeDate(fhirContext.newJsonParser().encodeResourceToString((IBaseResource) x)))
                .collect(Collectors.toList());
    }

    public List<Response> query(String resource, SchemaMode schemaMode, int count) {
        Schema schema = FhavroConverter.loadSchema(resource, schemaMode);
        return genericClient.search()
                .forResource(resource)
                .count(count)
                .returnBundle(Bundle.class)
                .execute()
                .getEntry()
                .stream()
                .map(x -> convert(schema, x.getResource(), resource))
                .collect(Collectors.toList());
    }

    public Response queryById(String resource, SchemaMode schemaMode, String id) {
        Schema schema = FhavroConverter.loadSchema(resource, schemaMode);
        try {
            BaseResource baseResource = (BaseResource) genericClient.read()
                    .resource(resource)
                    .withId(id)
                    .execute();
            return convert(schema, baseResource, resource);
        } catch (ResourceNotFoundException ex) {
            throw new BadRequestException(String.format("Unknown Resource %s/%s is not found.", resource, id));
        }
    }

    private <T extends BaseResource> Response convert(Schema schema, BaseResource baseResource, String name) {
        String inputString = ConverterUtils.standardizeDate(fhirContext.newJsonParser().encodeResourceToString(baseResource));
        Response queryResult = new Response(inputString);
        try {
            GenericRecord input = FhavroConverter.convertResourceToGenericRecord(baseResource, schema);
            T resource = FhavroConverter.convertGenericRecordToResource(input, schema, name);
            String outputString = ConverterUtils.standardizeDate(fhirContext.newJsonParser().encodeResourceToString(resource));
            boolean valid = queryResult.getInput().equals(outputString);
            if (!valid) {
                queryResult.setOutput(outputString);
            }
            return queryResult.returnValid(valid);
        } catch (Exception ex) {
            return queryResult.returnError(ex.getMessage());
        }
    }

    public File multipartFileToFile(MultipartFile multipartFile) {
        File file = new File(multipartFile.getOriginalFilename());
        try (OutputStream os = new FileOutputStream(file)) {
            os.write(multipartFile.getBytes());
        } catch (IOException ex) {
            throw new BadRequestException("Please verify the provided file.");
        }
        return file;
    }
}
