package in.ignishers.eduportal.models;

import in.ignishers.eduportal.enums.ClassStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@Entity
@Table(name = "class_sessions")
public class ClassSession extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "teaching_assignment_id", nullable = false)
    private TeachingAssignment teachingAssignment;

    @Column(nullable = false)
    private LocalDate classDate;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    @Column(length = 100)
    private String room;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ClassStatus status = ClassStatus.SCHEDULED;
}