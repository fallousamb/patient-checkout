package mouride.dev.aws.lambda.s3sns;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BuildManagementLambda implements RequestHandler<SNSEvent, String> {

    private final Logger logger = LoggerFactory.getLogger(BuildManagementLambda.class);

    private final ObjectMapper objectMapper;

    public BuildManagementLambda() {
        this.objectMapper = new ObjectMapper();
    }

    public BuildManagementLambda(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }


    @Override
    public String handleRequest(SNSEvent input, Context context) {
        input.getRecords().forEach(snsEvent -> {
            PatientCheckoutEvent patientCheckoutEvent = null;
            try {
                logger.info("Processing data from SNS event");
                patientCheckoutEvent = objectMapper.readValue(
                        snsEvent.getSNS().getMessage(),
                        PatientCheckoutEvent.class
                );
            } catch (JsonProcessingException e) {
                logger.error(e.getMessage());
            }
            logger.info(String.valueOf(patientCheckoutEvent));
        });
        return "END processing event from SNS";
    }
}
