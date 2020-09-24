package es.uvigo.ei.sing.wines.entities;


import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum WebEntityType {
    // Allowed values to the type in the database
    list,
    wine,
    user,
    review,
    brand,
    grape,
    appellation,
    location,
    pairing
}
