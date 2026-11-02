package in.ignishers.eduportal.models.attendance;

import in.ignishers.eduportal.models.BaseEntity;
import in.ignishers.eduportal.models.people.Student;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(
        name = "face_profiles",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_face_profile_student",
                        columnNames = "student_id"
                )
        }
)
public class FaceProfile extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false, unique = true)
    private Student student;

    @Lob
    @Column(nullable = false)
    private byte[] embedding;

    @Column(nullable = false, length = 50)
    private String modelVersion;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private LocalDateTime enrolledAt;
}