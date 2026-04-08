package fcul.model;

import jakarta.persistence.*;

@Entity
public class Photo {

    @Id
    @GeneratedValue
    private Long id;

    @Lob
    private byte[] data;

    @OneToOne
    @JoinColumn(name = "metadata_id")
    private PhotoMetadata metadata;


    public Photo(Long id, byte[] data, PhotoMetadata metadata) {
        this.id = id;
        this.data = data;
        this.metadata = metadata;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public PhotoMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(PhotoMetadata metadata) {
        this.metadata = metadata;
    }
}
