package bio.ferlab.fhir.app.service;

import bio.ferlab.fhir.FhavroConverter;
import bio.ferlab.fhir.app.Response;
import bio.ferlab.fhir.converter.ConverterUtils;
import bio.ferlab.fhir.converter.exception.BadRequestException;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.hl7.fhir.r4.model.BaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class QueryService {

    @Autowired
    private IGenericClient genericClient;

    @Autowired
    private FhirContext fhirContext;

    public List<Response> query(String resource, int count) {
        Schema schema = FhavroConverter.loadSchema(resource);
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

    public Response queryById(String resource, String id) {
        Schema schema = FhavroConverter.loadSchema(resource);
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
}
