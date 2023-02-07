package io.extremum.groundClient.builder.core.outputField.model;

import io.extremum.groundClient.model.TestBasicModel;

public class Name  extends TestBasicModel {
    private String first;
    private String second;

    public String getFirst() {
        return first;
    }

    public void setFirst(String first) {
        this.first = first;
    }

    public String getSecond() {
        return second;
    }

    public void setSecond(String second) {
        this.second = second;
    }
}