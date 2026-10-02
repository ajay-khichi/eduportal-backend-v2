package in.ignishers.eduportal.models;

import in.ignishers.eduportal.enums.VerificationStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "attendance_verifications")
public class AttendanceVerification extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "attendance_id",
            nullable = false,
            unique = true
    )
    private Attendance attendance;

    @Column(nullable = false)
    private boolean qrVerified = false;

    @Column(precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(nullable = false)
    private boolean faceVerified = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VerificationStatus status = VerificationStatus.PENDING;

    private LocalDateTime verifiedAt;
}