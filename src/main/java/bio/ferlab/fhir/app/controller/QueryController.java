package bio.ferlab.fhir.app.controller;

import bio.ferlab.fhir.app.Response;
import bio.ferlab.fhir.app.service.QueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class QueryController {

    @Autowired
    private QueryService queryService;

    @GetMapping(value = "/api/v1/query/{resource}")
    public List<Response> query(@PathVariable String resource, @RequestParam(name = "count", defaultValue = "10") int count) {
        return queryService.query(resource, count);
    }

    @GetMapping(value = "/api/v1/query/{resource}/{id}")
    public Response queryById(@PathVariable String resource, @PathVariable String id) {
        return queryService.queryById(resource, id);
    }
}
