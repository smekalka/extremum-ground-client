package io.extremum.ground.client.builder.core.outputfield.model;

import io.extremum.ground.client.model.TestBasicModel;

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