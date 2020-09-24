package es.uvigo.ei.sing.wines.entities;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "wine", schema = "wine_db",
        indexes = {@Index(name = "wine_hash", columnList = "hash", unique = true)})
public class WineEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;
    @Basic
    @Column(name = "hash", length = 256, nullable = false)
    private String hash;
    @Basic
    @Column(name = "name", length = 500, nullable = false)
    private String name;
    @Basic
    @Column(name = "source", nullable = false, columnDefinition = "TINYTEXT")
    private String source;
    @Basic
    @Column(name = "dc_rating")
    private float dcRating;
    @Basic
    @Column(name = "rp_rating")
    private float rpRating;
    @Basic
    @Column(name = "ws_rating")
    private float wsRating;
    @Basic
    @Column(name = "we_rating")
    private float weRating;
    @Basic
    @Column(name = "external_id")
    private String externalId;
    @Basic
    @Column(name = "price")
    private float price;
    @Basic
    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;
    @Basic
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    @Basic
    @Column(name = "tasting_notes", columnDefinition = "TEXT")
    private String tastingNotes;
    @Basic
    @Column(name = "type")
    private String type;
    @Basic
    @Column(name = "vintage")
    private int vintage;
    @Basic
    @Column(name = "volume", length = 50)
    private String volume;
    @Basic
    @Column(name = "min_consumption_temp")
    private int minConsumptionTemp;
    @Basic
    @Column(name = "max_consumption_temp")
    private int maxConsumptionTemp;
    @Basic
    @Column(name = "aging", length = 50)
    private String aging;
    @Basic
    @Column(name = "allergens")
    private String allergens;
    @Basic
    @Column(name = "alcohol_vol")
    private float alcoholVol;

    @ManyToOne
    @JoinColumn(name = "brand_id", referencedColumnName = "id")
    private BrandEntity brand;
    @ManyToOne
    @JoinColumn(name = "appellation_id", referencedColumnName = "id")
    private AppellationEntity appellation;
    @ManyToOne
    @JoinColumn(name = "location_id", referencedColumnName = "id")
    private LocationEntity location;

    @ManyToMany(cascade = {CascadeType.MERGE}, fetch = FetchType.EAGER)
    @JoinTable(name = "wine_competition",
            joinColumns = @JoinColumn(name = "wine_id", referencedColumnName = "id", nullable = false),
            inverseJoinColumns = @JoinColumn(name = "competition_id", referencedColumnName = "id", nullable = false))
    private Set<CompetitionEntity> competitions;
    @ManyToMany(cascade = {CascadeType.MERGE}, fetch = FetchType.EAGER)
    @JoinTable(name = "wine_food",
            joinColumns = @JoinColumn(name = "wine_id", referencedColumnName = "id", nullable = false),
            inverseJoinColumns = @JoinColumn(name = "food_id", referencedColumnName = "id", nullable = false))
    private Set<FoodEntity> foods;
    @ManyToMany(cascade = {CascadeType.MERGE}, fetch = FetchType.EAGER)
    @JoinTable(name = "wine_review",
            joinColumns = @JoinColumn(name = "wine_id", referencedColumnName = "id", nullable = false),
            inverseJoinColumns = @JoinColumn(name = "review_id", referencedColumnName = "id", nullable = false))
    private Set<ReviewEntity> reviews;
    @ManyToMany(cascade = {CascadeType.MERGE}, fetch = FetchType.EAGER)
    @JoinTable(name = "wine_grape",
            joinColumns = @JoinColumn(name = "wine_id", referencedColumnName = "id", nullable = false),
            inverseJoinColumns = @JoinColumn(name = "grape_id", referencedColumnName = "id", nullable = false))
    private Set<GrapeEntity> grapes;
}
