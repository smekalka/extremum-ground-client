package io.extremum.ground.client.builder.core.outputfield.model;

import io.extremum.ground.client.model.TestBasicModel;
import io.extremum.sharedmodels.basic.GraphQlList;

public class ParentModel extends TestBasicModel {
    private GraphQlList<ChildModel> children = new GraphQlList<>();

    public GraphQlList<ChildModel> getChildren() {
        return children;
    }

    public void setChildren(GraphQlList<ChildModel> children) {
        this.children = children;
    }
}