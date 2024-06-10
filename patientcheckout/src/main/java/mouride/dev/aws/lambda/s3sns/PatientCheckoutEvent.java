package mouride.dev.aws.lambda.s3sns;

public record PatientCheckoutEvent(
        String firstName,
        String middleName,
        String lastName,
        String ssn) {

    public String toString() {
        return String.format("PatientCheckoutEvent{firstName=%s, middleName=%s, lastName=%s, sns=%s}",
                firstName, middleName, lastName, ssn);
    }
}
