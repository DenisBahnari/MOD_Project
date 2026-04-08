package fcul.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public class Photo {

    @Id
    @GeneratedValue
    private Long id;

    private String filename;

}
