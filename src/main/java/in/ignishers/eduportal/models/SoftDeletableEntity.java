package in.ignishers.eduportal.models;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@MappedSuperclass
public abstract class SoftDeletableEntity extends BaseEntity {

    @Column(nullable = false)
    private boolean deleted = false;
}