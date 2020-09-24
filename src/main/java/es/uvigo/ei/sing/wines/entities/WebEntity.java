package es.uvigo.ei.sing.wines.entities;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "web", schema = "wine_db",
        indexes = {@Index(name = "web_hash", columnList = "hash", unique = true),
                @Index(name = "web_modified", columnList = "modified"),
                @Index(name = "web_type", columnList = "type")})
public class WebEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;
    @Basic
    @Column(name = "hash", length = 256, nullable = false)
    private String hash;
    @Basic
    @Column(name = "url", columnDefinition = "TINYTEXT", nullable = false)
    private String url;
    @Enumerated(EnumType.STRING)
    @Column(name = "type", columnDefinition = "enum", nullable = false)
    private WebEntityType type;
    @Basic
    @Column(name = "modified")
    private LocalDateTime modified;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "web")
    private HtmlEntity htmlEntity;
}
