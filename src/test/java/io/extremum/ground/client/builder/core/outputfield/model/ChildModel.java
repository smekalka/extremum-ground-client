package io.extremum.ground.client.builder.core.outputfield.model;

import io.extremum.ground.client.model.TestBasicModel;

public class ChildModel extends TestBasicModel {
    private Name name;
    private String surname;

    public Name getName() {
        return name;
    }

    public void setName(Name name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }
}