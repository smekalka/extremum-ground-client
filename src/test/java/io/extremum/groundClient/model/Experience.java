package io.extremum.groundClient.model;

import java.util.Objects;

public class Experience extends TestBasicModel {
    private String mime;

    public String getMime() {
        return mime;
    }

    public void setMime(String mime) {
        this.mime = mime;
    }

    @Override
    public String toString() {
        return "Experience{" +
                "mime='" + mime + '\'' +
                ", uuid=" + uuid +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Experience that = (Experience) o;

        return Objects.equals(mime, that.mime);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (mime != null ? mime.hashCode() : 0);
        return result;
    }
}
