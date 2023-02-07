package io.extremum.ground.client.model;

import io.extremum.sharedmodels.basic.StringOrMultilingual;

public class Product extends TestBasicModel {
    private StringOrMultilingual name;
    private Double rating;

    public StringOrMultilingual getName() {
        return name;
    }

    public void setName(StringOrMultilingual name) {
        this.name = name;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    @Override
    public String toString() {
        return "Product{" +
                "name=" + name +
                ", rating=" + rating +
                ", uuid=" + uuid +
                '}';
    }
}
