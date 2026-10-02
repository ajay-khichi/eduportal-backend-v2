package in.ignishers.eduportal.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(
        name = "faculty",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_faculty_college_employee",
                        columnNames = {"college_id", "employee_number"}
                )
        }
)
public class Faculty extends SoftDeletableEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "college_id", nullable = false)
    private College college;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @Column(name = "employee_number", nullable = false, length = 50)
    private String employeeNumber;

    @Column(nullable = false, length = 100)
    private String firstName;

    @Column(length = 100)
    private String lastName;

    @Column(length = 20)
    private String phone;

    @Column(length = 100)
    private String designation;

    @OneToMany(mappedBy = "faculty")
    private List<TeachingAssignment> teachingAssignments = new ArrayList<>();
}