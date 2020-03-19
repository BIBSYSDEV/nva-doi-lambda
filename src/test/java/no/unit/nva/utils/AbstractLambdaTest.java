package no.unit.nva.utils;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import java.util.Arrays;

public abstract class AbstractLambdaTest {

    protected static final Context mockLambdaContext = createMockContext();

    protected static Context createMockContext() {
        Context context = mock(Context.class);
        when(context.getLogger()).thenReturn(mockLambdaLogger());
        return context;
    }

    protected static LambdaLogger mockLambdaLogger() {
        return new LambdaLogger() {
            @Override
            public void log(String message) {
                System.out.print(message);
            }

            @Override
            public void log(byte[] message) {
                log(Arrays.toString(message));
            }
        };
    }

}
