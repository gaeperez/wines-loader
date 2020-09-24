package es.uvigo.ei.sing.wines.entities;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "brand", schema = "wine_db",
        indexes = {@Index(name = "brand_name", columnList = "name", unique = true)})
public class BrandEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;
    @Basic
    @Column(name = "name", length = 500, nullable = false)
    private String name;
    @Basic
    @Column(name = "address", length = 500)
    private String address;
    @Basic
    @Column(name = "city", length = 100)
    private String city;
    @Basic
    @Column(name = "location", length = 500)
    private String location;
    @Basic
    @Column(name = "country", length = 100)
    private String country;
    @Basic
    @Column(name = "phone", length = 100)
    private String phone;
    @Basic
    @Column(name = "fax", length = 100)
    private String fax;
    @Basic
    @Column(name = "web", length = 500)
    private String web;
    @Basic
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @OneToMany(mappedBy = "brand")
    private Set<WineEntity> wines;
}
