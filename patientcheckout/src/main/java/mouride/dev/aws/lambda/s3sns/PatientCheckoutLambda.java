package mouride.dev.aws.lambda.s3sns;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class PatientCheckoutLambda implements RequestHandler<S3Event, String> {

    private final Logger logger = LoggerFactory.getLogger(PatientCheckoutLambda.class);

    private final AmazonS3 amazonS3;

    private final ObjectMapper objectMapper;

    private final AmazonSNS amazonSNS;

    private final String snsName;

    public PatientCheckoutLambda() {
        this.amazonS3 = AmazonS3ClientBuilder.defaultClient();
        this.amazonSNS = AmazonSNSClientBuilder.defaultClient();
        this.objectMapper = new ObjectMapper();
        this.snsName = System.getenv("PATIENT_CHECKOUT_TOPIC");
    }

    public PatientCheckoutLambda(AmazonS3 amazonS3,
                                 AmazonSNS amazonSNS,
                                 ObjectMapper objectMapper,
                                 String snsName) {
        this.amazonS3 = amazonS3;
        this.amazonSNS = amazonSNS;
        this.objectMapper = objectMapper;
        this.snsName = snsName;
    }
    @Override
    public String handleRequest(S3Event input, Context context) {
        final List<PatientCheckoutEvent> patientCheckoutEventList = new ArrayList<>();
        input.getRecords().forEach(s3EventNotificationRecord -> {
            S3ObjectInputStream s3ObjectInputStream = amazonS3
                    .getObject(s3EventNotificationRecord.getS3().getBucket().getName(),
                            s3EventNotificationRecord.getS3().getObject().getKey())
                    .getObjectContent();
            try {
                logger.info("Processing S3 event from S3");
                PatientCheckoutEvent[] patientCheckoutEvents = objectMapper.readValue(s3ObjectInputStream, PatientCheckoutEvent[].class);
                patientCheckoutEventList.addAll(Arrays.asList(patientCheckoutEvents));
                s3ObjectInputStream.close();
                logger.info("Publish data to SNS");
                publishToSNS(patientCheckoutEventList);
                logger.info(String.valueOf(patientCheckoutEventList));
            } catch (JsonParseException | JsonMappingException e) {
                logger.error(e.getMessage());
                throw new RuntimeException("Error wile processing S3 event from S3");
            } catch (IOException e) {
                logger.error(e.getMessage());
                throw new RuntimeException("Error wile processing S3 event from S3");
            }
        });
        final String message = "End processing event from S3";
        logger.info(message);
        return message;
    }

    private void publishToSNS(List<PatientCheckoutEvent> patientCheckoutEventList) {
        patientCheckoutEventList.forEach(patientCheckoutEvent -> {
            try {
                this.amazonSNS.publish(
                        this.snsName,
                        objectMapper.writeValueAsString(patientCheckoutEvent)
                );
            } catch (JsonProcessingException e) {
                logger.error(e.getMessage());
            }
        });
    }

}
