package in.ignishers.eduportal.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "colleges")
public class College extends SoftDeletableEntity {

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @Column(length = 255)
    private String address;

    @OneToMany(mappedBy = "college")
    private List<Department> departments = new ArrayList<>();
}