package io.extremum.ground.client.builder.core.outputfield.model;

import io.extremum.ground.client.model.TestBasicModel;

public class Level0 extends TestBasicModel {
    private Level1 level0;

    public Level1 getLevel0() {
        return level0;
    }

    public void setLevel0(Level1 level0) {
        this.level0 = level0;
    }
}