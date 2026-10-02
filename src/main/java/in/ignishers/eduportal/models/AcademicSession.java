package in.ignishers.eduportal.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(
        name = "academic_sessions",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_academic_session_college_name",
                        columnNames = {"college_id", "name"}
                )
        }
)
public class AcademicSession extends SoftDeletableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "college_id", nullable = false)
    private College college;

    @Column(nullable = false, length = 20)
    private String name;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private boolean current = false;
}