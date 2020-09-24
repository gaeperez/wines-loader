package es.uvigo.ei.sing.wines.entities;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "html", schema = "wine_db")
public class HtmlEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;
    @Basic
    @Column(name = "html", columnDefinition = "LONGTEXT")
    private String html;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "web_id", nullable = false)
    private WebEntity web;
}
