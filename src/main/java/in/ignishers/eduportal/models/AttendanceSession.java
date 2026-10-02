package in.ignishers.eduportal.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(
        name = "attendance_sessions",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_attendance_session_class",
                        columnNames = "class_id"
                )
        }
)
public class AttendanceSession extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "class_id", nullable = false, unique = true)
    private ClassSession classSession;

    @Column(nullable = false, unique = true, length = 64)
    private String secret;

    @Column(nullable = false)
    private LocalDateTime startedAt;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private boolean active = true;
}