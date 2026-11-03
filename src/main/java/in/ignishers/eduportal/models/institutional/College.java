package in.ignishers.eduportal.models.institutional;

import in.ignishers.eduportal.models.SoftDeletableEntity;
import in.ignishers.eduportal.models.auth.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "colleges")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class College extends SoftDeletableEntity {

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @Column(length = 255)
    private String address;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_user_id", unique = true)
    private User admin;

    @Builder.Default
    @OneToMany(mappedBy = "college")
    private List<Department> departments = new ArrayList<>();
}