package no.unit.nva.doi;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import java.util.Map;

public class SimpleEchoRequestHandler  implements RequestHandler<Map<String, Object>, SimpleResponse> {

    @Override
    public SimpleResponse handleRequest(Map<String, Object> input, Context context) {
        System.out.println("input: "+input);
        return new SimpleResponse(input.toString(), "200");
    }
}
