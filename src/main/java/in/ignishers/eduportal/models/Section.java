package in.ignishers.eduportal.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "sections")
public class Section extends SoftDeletableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "department_program_id", nullable = false)
    private DepartmentProgram departmentProgram;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "academic_session_id", nullable = false)
    private AcademicSession academicSession;

    @Column(nullable = false)
    private Integer semester;

    @Column(nullable = false, length = 20)
    private String name;

    @OneToMany(mappedBy = "section")
    private List<Enrollment> enrollments = new ArrayList<>();
}