package fcul.modc.model;

import jakarta.persistence.*;

@Entity
public class Photo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    private byte[] data;

    @OneToOne(mappedBy = "photo", cascade = CascadeType.ALL)
    private PhotoMetadata metadata;

    @ManyToOne
    @JoinColumn(name = "album_id")
    private Album album;

    public Photo() {}

    public Photo(Long id, byte[] data, PhotoMetadata metadata, Album album) {
        this.id = id;
        this.data = data;
        this.metadata = metadata;
        this.album = album;
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

    public Album getAlbum() {return album;}

    public void setAlbum(Album album) {this.album = album;}
}
