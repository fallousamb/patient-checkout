package mouride.dev.aws.lambda.s3sns;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ErrorHandlerLambda implements RequestHandler<SNSEvent, String> {

    private final Logger logger = LoggerFactory.getLogger(ErrorHandlerLambda.class);

    @Override
    public String handleRequest(SNSEvent input, Context context) {
        logger.info("ErrorHandlerLambda");
        input.getRecords().forEach(data -> logger.info("Dead Letter Queue Event: {}", data.toString()));
        return "End Read from Dead Letter Queue";
    }
}
