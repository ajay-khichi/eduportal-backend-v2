package in.ignishers.eduportal.models.institutional;

import in.ignishers.eduportal.models.SoftDeletableEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@Table(name = "programs")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Program extends SoftDeletableEntity {

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 30)
    private String code;
}