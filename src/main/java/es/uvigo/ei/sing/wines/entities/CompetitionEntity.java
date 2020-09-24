package es.uvigo.ei.sing.wines.entities;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "competition", schema = "wine_db",
        indexes = {@Index(name = "competition_award", columnList = "award", unique = true)})
public class CompetitionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;
    @Basic
    @Column(name = "award", length = 500, nullable = false)
    private String award;

    @ManyToMany(mappedBy = "competitions")
    private Set<WineEntity> wines;
}
