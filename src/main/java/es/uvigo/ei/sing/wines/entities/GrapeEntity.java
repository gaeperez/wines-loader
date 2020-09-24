package es.uvigo.ei.sing.wines.entities;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "grape", schema = "wine_db",
        indexes = {@Index(name = "grape_name", columnList = "name", unique = true)})
public class GrapeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;
    @Basic
    @Column(name = "name", length = 500, nullable = false)
    private String name;
    @Basic
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @ManyToMany(mappedBy = "grapes")
    private Set<WineEntity> wines;
}
