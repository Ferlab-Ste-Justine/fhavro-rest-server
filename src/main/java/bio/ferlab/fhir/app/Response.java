package bio.ferlab.fhir.app;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Response {

    private String input;
    private String output;
    private String error;
    private boolean valid;

    public Response(String input) {
        setInput(input);
    }

    public Response returnValid(boolean valid) {
        setValid(valid);
        return this;
    }

    public Response returnError(String error) {
        setError(error);
        return this;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
        this.valid = false;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }
}
