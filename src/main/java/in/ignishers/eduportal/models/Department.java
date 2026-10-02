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
        name = "departments",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_department_college_code",
                        columnNames = {"college_id", "code"}
                )
        }
)
public class Department extends SoftDeletableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "college_id", nullable = false)
    private College college;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 20)
    private String code;

    @OneToMany(mappedBy = "department")
    private List<Student> students = new ArrayList<>();

    @OneToMany(mappedBy = "department")
    private List<Faculty> faculty = new ArrayList<>();

    @OneToMany(mappedBy = "department")
    private List<DepartmentProgram> programs = new ArrayList<>();
}