package io.extremum.groundClient.builder.core.outputField.model;

import io.extremum.groundClient.model.TestBasicModel;
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