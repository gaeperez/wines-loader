package es.uvigo.ei.sing.wines.entities;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "appellation", schema = "wine_db",
        indexes = {@Index(name = "appellation_name", columnList = "name", unique = true)})
public class AppellationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;
    @Basic
    @Column(name = "name", nullable = false)
    private String name;
    @Basic
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @OneToMany(mappedBy = "appellation")
    private Set<WineEntity> wines;
}
