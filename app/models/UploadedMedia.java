package models;

import com.avaje.ebean.Model;
import com.avaje.ebean.annotation.CreatedTimestamp;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import util.JsonKeys;

import javax.persistence.*;
import java.net.URI;
import java.util.Date;

/**
 * @author Jonas Kraus
 * @author Fabian Widmann
 */
@Entity
public class UploadedMedia extends Model {
    @Id
    @GeneratedValue
    @Column(name = JsonKeys.MEDIA_ID)
    @JsonProperty(JsonKeys.MEDIA_ID)
    private long id;

    @JsonProperty(JsonKeys.URI)
    @Column(name = JsonKeys.URI)
    private URI uri;

    @ManyToOne
    @JoinColumn(name=JsonKeys.USER_ID, referencedColumnName=JsonKeys.USER_ID)
    @JsonProperty(JsonKeys.AUTHOR)
    private User author;

    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss z") @CreatedTimestamp
    @Column(name = JsonKeys.DATE_CREATED)
    @JsonProperty(JsonKeys.DATE_CREATED)
    private Date created;

    @JsonProperty(JsonKeys.MEDIA_TYPE)
    private String mediaType;

    public static Model.Finder<Long,UploadedMedia> find = new Model.Finder<Long,UploadedMedia>(UploadedMedia.class);
    public UploadedMedia(URI uri, User author) {
        this.uri = uri;
        this.author = author;
    }

    public UploadedMedia(URI uri, User author, String mediaType) {
        this.uri = uri;
        this.author = author;
        this.mediaType = mediaType;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public URI getUri() {
        return uri;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    @Override
    public String toString() {
        return "UploadedMedia{" +
                "id=" + id +
                ", uri=" + uri +
                ", author=" + author +
                '}';
    }
}
