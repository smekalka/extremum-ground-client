package io.extremum.groundClient.builder.core.outputField.model;

import io.extremum.groundClient.model.TestBasicModel;

public class Level1 extends TestBasicModel {
    private String level1_1;
    private Level2 level1_2;

    public String getLevel1_1() {
        return level1_1;
    }

    public void setLevel1_1(String level1_1) {
        this.level1_1 = level1_1;
    }

    public Level2 getLevel1_2() {
        return level1_2;
    }

    public void setLevel1_2(Level2 level1_2) {
        this.level1_2 = level1_2;
    }
}