package ppkwu;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
public class Employee {

    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;

    @Override
    public String toString() {
        return "Employee{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                '}';
    }
}
