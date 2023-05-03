package io.extremum.ground.client.client;

import java.util.Objects;

public class CustomProperties {

    private String param11;

    private String param22;

    public CustomProperties() {
    }

    public CustomProperties(String param11, String param22) {
        this.param11 = param11;
        this.param22 = param22;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CustomProperties that = (CustomProperties) o;

        if (!Objects.equals(param11, that.param11)) return false;
        return Objects.equals(param22, that.param22);
    }

    @Override
    public int hashCode() {
        int result = param11 != null ? param11.hashCode() : 0;
        result = 31 * result + (param22 != null ? param22.hashCode() : 0);
        return result;
    }

    public String getParam11() {
        return param11;
    }

    public void setParam11(String param11) {
        this.param11 = param11;
    }

    public String getParam22() {
        return param22;
    }

    public void setParam22(String param22) {
        this.param22 = param22;
    }
}
