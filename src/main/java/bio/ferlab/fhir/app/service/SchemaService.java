package bio.ferlab.fhir.app.service;

import bio.ferlab.fhir.FhavroConverter;
import bio.ferlab.fhir.converter.exception.BadRequestException;
import bio.ferlab.fhir.schema.repository.SchemaMode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class SchemaService {

    public String generateSchema(String schemaName, SchemaMode schemaMode, boolean pretty) {
        if (StringUtils.isBlank(schemaName)) {
            throw new BadRequestException("Please provide the following parameters: [schemaName, schemaMode]");
        }

        String schemaJson;
        try {
            schemaJson = FhavroConverter.loadSchema(schemaName, schemaMode).toString();
        } catch (BadRequestException ex) {
            schemaJson = FhavroConverter.generateSchema(schemaName, schemaMode);
        }

        if (!pretty) {
            return schemaJson;
        } else {
            try {
                ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
                Object jsonObject = mapper.readValue(schemaJson, Object.class);
                return mapper.writeValueAsString(jsonObject);
            } catch (Exception ex) {
                throw new BadRequestException(ex.getMessage());
            }
        }
    }

    public String generateSchemaWithProfile(String schemaName, SchemaMode schemaMode, MultipartFile profile, List<MultipartFile> extensions) {
        if (StringUtils.isBlank(schemaName)) {
            throw new BadRequestException("Please provide the following parameters: [schemaName]");
        }

        try {
            StructureDefinition profileDefinition = FhavroConverter.loadProfile(profile.getInputStream());
            List<StructureDefinition> extensionDefinitions = new ArrayList<>();
            for (MultipartFile extensionFile : extensions) {
                extensionDefinitions.add(FhavroConverter.loadProfile(extensionFile.getInputStream()));
            }
            return FhavroConverter.generateSchema(schemaName, schemaMode, profileDefinition, extensionDefinitions);
        } catch (IOException ex) {
            throw new BadRequestException("Please verify the content of the profile and/or the extensions provided.");
        }
    }
}