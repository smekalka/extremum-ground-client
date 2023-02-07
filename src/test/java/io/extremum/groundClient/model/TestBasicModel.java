package io.extremum.groundClient.model;

import io.extremum.sharedmodels.basic.BasicModel;
import io.extremum.sharedmodels.descriptor.Descriptor;

import java.util.Objects;
import java.util.UUID;

public abstract class TestBasicModel implements BasicModel<UUID> {

    protected Descriptor uuid;

    public UUID getId() {
        return UUID.randomUUID();
    }

    public String getIri() {
        return this.uuid.toString();
    }

    public void setUuid(Descriptor uuid) {
        this.uuid = uuid;
    }

    public void setIri(String iri) {
    }

    public void setId(UUID id) {
    }

    public Descriptor getUuid() {
        return this.uuid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TestBasicModel that = (TestBasicModel) o;

        if((uuid == null) && (that.uuid == null)) return true;
        if((uuid == null) != (that.uuid == null)) return false;
        return Objects.equals(uuid.getExternalId(), that.uuid.getExternalId());
    }

    @Override
    public int hashCode() {
        return uuid != null ? uuid.hashCode() : 0;
    }
}
