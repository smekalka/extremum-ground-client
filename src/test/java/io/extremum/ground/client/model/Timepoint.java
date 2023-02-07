package io.extremum.ground.client.model;

import java.time.ZonedDateTime;
import java.util.Objects;

public class Timepoint extends TestBasicModel {
    private ZonedDateTime timestamp;

    public ZonedDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(ZonedDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "Timepoint{" +
                "timestamp=" + timestamp +
                ", uuid=" + uuid +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Timepoint timepoint = (Timepoint) o;

        return Objects.equals(timestamp, timepoint.timestamp);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (timestamp != null ? timestamp.hashCode() : 0);
        return result;
    }
}
