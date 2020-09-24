package es.uvigo.ei.sing.wines.entities;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "review", schema = "wine_db",
        indexes = {@Index(name = "review_hash", columnList = "hash", unique = true)})
public class ReviewEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;
    @Basic
    @Column(name = "hash", length = 256, nullable = false)
    private String hash;
    @Basic
    @Column(name = "text", columnDefinition = "TEXT")
    private String text;
    @Basic
    @Column(name = "rating")
    private float rating;
    @Basic
    @Column(name = "date")
    private LocalDateTime date;
    @Basic
    @Column(name = "source")
    private String source;

    @ManyToMany(mappedBy = "reviews")
    private Set<WineEntity> wines = new HashSet<>();
    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private UserEntity user;
}
