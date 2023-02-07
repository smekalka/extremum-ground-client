package io.extremum.ground.client.model;

import io.extremum.sharedmodels.basic.StringOrObject;

public class Compensation extends TestBasicModel {
    private String function;
    private StringOrObject<Object> parameters;

    public String getFunction() {
        return function;
    }

    public void setFunction(String function) {
        this.function = function;
    }

    public StringOrObject<Object> getParameters() {
        return parameters;
    }

    public void setParameters(StringOrObject<Object> parameters) {
        this.parameters = parameters;
    }

    @Override
    public String toString() {
        return "Compensation{" +
                "function='" + function + '\'' +
                ", parameters=" + parameters +
                ", uuid=" + uuid +
                '}';
    }
}
