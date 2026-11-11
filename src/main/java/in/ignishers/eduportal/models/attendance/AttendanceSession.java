package in.ignishers.eduportal.models.attendance;

import in.ignishers.eduportal.models.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
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

    @Column(precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(precision = 10, scale = 7)
    private BigDecimal longitude;

    private Integer radiusMeters;

    @Column(nullable = false)
    private boolean active = true;
}