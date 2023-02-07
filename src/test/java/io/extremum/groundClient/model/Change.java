package io.extremum.groundClient.model;

import io.extremum.sharedmodels.basic.StringOrObject;

import java.util.Objects;

public class Change extends TestBasicModel {
    private Double ordinal;
    private StringOrObject<Object> data;
    private Compensation compensation;

    public Double getOrdinal() {
        return ordinal;
    }

    public void setOrdinal(Double ordinal) {
        this.ordinal = ordinal;
    }

    public StringOrObject<Object> getData() {
        return data;
    }

    public void setData(StringOrObject<Object> data) {
        this.data = data;
    }

    public Compensation getCompensation() {
        return compensation;
    }

    public void setCompensation(Compensation compensation) {
        this.compensation = compensation;
    }

    @Override
    public String toString() {
        return "Change{" +
                "ordinal=" + ordinal +
                ", data=" + data +
                ", compensation=" + compensation +
                ", uuid=" + uuid +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Change change = (Change) o;

        if (!Objects.equals(ordinal, change.ordinal)) return false;
        if (!Objects.equals(data, change.data)) return false;
        return Objects.equals(compensation, change.compensation);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (ordinal != null ? ordinal.hashCode() : 0);
        result = 31 * result + (data != null ? data.hashCode() : 0);
        result = 31 * result + (compensation != null ? compensation.hashCode() : 0);
        return result;
    }
}
