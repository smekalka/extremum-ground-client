package io.extremum.ground.client.model;

import io.extremum.sharedmodels.basic.StringOrMultilingual;

import java.time.ZonedDateTime;

public class Zone extends TestBasicModel {
    private StringOrMultilingual description;
    private Integer size;
    private ZonedDateTime created;

    public StringOrMultilingual getDescription() {
        return description;
    }

    public void setDescription(StringOrMultilingual description) {
        this.description = description;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public ZonedDateTime getCreated() {
        return created;
    }

    public void setCreated(ZonedDateTime created) {
        this.created = created;
    }

    @Override
    public String toString() {
        return "Zone{" +
                "description=" + description +
                ", size=" + size +
                ", created=" + created +
                ", uuid=" + uuid +
                '}';
    }
}
