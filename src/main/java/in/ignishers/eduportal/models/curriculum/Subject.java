package in.ignishers.eduportal.models.curriculum;

import in.ignishers.eduportal.models.SoftDeletableEntity;
import in.ignishers.eduportal.models.institutional.DepartmentProgram;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@Table(
        name = "subjects",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_subject_program_code",
                        columnNames = {"department_program_id", "code"}
                )
        }
)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Subject extends SoftDeletableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "department_program_id", nullable = false)
    private DepartmentProgram departmentProgram;

    @Column(nullable = false, length = 30)
    private String code;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false)
    private Integer semester;

    private Integer credits;
}