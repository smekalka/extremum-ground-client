package io.extremum.ground.client.model;

import io.extremum.sharedmodels.basic.GraphQlList;
import io.extremum.sharedmodels.structs.IntegerRangeOrValue;

public class Event extends TestBasicModel {

    private String url;
    private Integer size;
    private Product product;
    private GraphQlList<Experience> experiences = new GraphQlList<>();

    private IntegerRangeOrValue participants;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public GraphQlList<Experience> getExperiences() {
        return experiences;
    }

    public void setExperiences(GraphQlList<Experience> experiences) {
        this.experiences = experiences;
    }

    public IntegerRangeOrValue getParticipants() {
        return participants;
    }

    public void setParticipants(IntegerRangeOrValue participants) {
        this.participants = participants;
    }

    @Override
    public String toString() {
        return "Event{" +
                "url='" + url + '\'' +
                ", size=" + size +
                ", product=" + product +
                ", experiences=" + experiences +
                ", participants=" + participants +
                ", uuid=" + uuid +
                '}';
    }
}