package es.uvigo.ei.sing.wines.entities;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "food", schema = "wine_db",
        indexes = {@Index(name = "food_name", columnList = "name", unique = true)})
public class FoodEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;
    @Basic
    @Column(name = "name", length = 300, nullable = false)
    private String name;
    @Basic
    @Column(name = "type")
    private String type;
    @Basic
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @ManyToMany(mappedBy = "foods")
    private Set<WineEntity> wines;
}
