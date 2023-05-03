package io.extremum.ground.client.client;

import io.extremum.ground.client.builder.Builders;
import io.extremum.ground.client.builder.core.GraphQlBuilderKt;
import io.extremum.ground.client.builder.core.PagingAndSortingRequest;
import io.extremum.ground.client.builder.core.outputfield.OutputField;
import io.extremum.ground.client.builder.core.outputfield.OutputFields;
import io.extremum.ground.client.builder.impl.GraphQlAddToSublistBuilder;
import io.extremum.ground.client.builder.impl.GraphQlGetByIdBuilder;
import io.extremum.ground.client.builder.impl.GraphQlQueryBuilder;
import io.extremum.ground.client.builder.impl.GraphQlRemoveFromSublistBuilder;
import io.extremum.ground.client.builder.impl.GraphQlUpdateBuilder;
import io.extremum.ground.client.model.Account;
import io.extremum.ground.client.model.Change;
import io.extremum.ground.client.model.Compensation;
import io.extremum.ground.client.model.Event;
import io.extremum.ground.client.model.Experience;
import io.extremum.ground.client.model.Product;
import io.extremum.ground.client.model.Zone;
import io.extremum.model.tools.mapper.MapperUtils;
import io.extremum.sharedmodels.basic.GraphQlList;
import io.extremum.sharedmodels.basic.StringOrMultilingual;
import io.extremum.sharedmodels.basic.StringOrObject;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.test.tools.AssertionUtils;
import org.apache.http.HttpHeaders;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static io.extremum.ground.client.client.Response.Status.MODEL_NOT_FOUND;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

public class GroundApiClientJavaIT {

    GroundApiClient groundApiClient;

    public GroundApiClientJavaIT() {
        HashMap<String, String> headers = new HashMap<>();
        headers.put(HttpHeaders.AUTHORIZATION, GroundProperties.TOKEN);
        groundApiClient = new GroundApiClient(GroundProperties.URL, headers);
    }

    @Disabled("launched ground application is needed")
    @Test
    void getZones() throws ExecutionException, InterruptedException {
        groundApiClient.createEmpty(Zone.class, null).get();

        List<OutputField> fields = new ArrayList<>();
        fields.add(OutputFields.INSTANCE.field("description"));
        fields.add(OutputFields.INSTANCE.field("uuid"));
        GraphQlQueryBuilder builder = Builders.INSTANCE.query(new PagingAndSortingRequest(), null);
        GraphQlBuilderKt.setOutputFields(builder, fields);

        List<Zone> result = groundApiClient.query(Zone.class, builder).get().validateStatusAndValueNotNull("Zone");

        System.out.println(result);
        assertThat(result).isNotEmpty();
    }

    @Disabled("launched ground application is needed")
    @Test
    void findAccountByFilter() throws ExecutionException, InterruptedException {
        String searchingValue = "searching";
        Compensation compensation = new Compensation();
        compensation.setFunction(searchingValue);
        groundApiClient.create(Compensation.class, compensation, null);

        GraphQlQueryBuilder builder = Builders.INSTANCE.query(new PagingAndSortingRequest(), "object.function.eq(\"" + searchingValue + "\")");
        List<Compensation> result = groundApiClient.query(Compensation.class, builder).get().validateStatusAndValueNotNull("Compensation");

        System.out.println(result);
        assertThat(result).isNotEmpty();
    }

    @Disabled("launched ground application is needed")
    @Test
    void getZoneById() throws ExecutionException, InterruptedException {
        Zone zone = groundApiClient.createEmpty(Zone.class, null).get();
        Descriptor id = zone.getUuid();
        GraphQlGetByIdBuilder builder = Builders.INSTANCE.getById(id);
        List<OutputField> fields = new ArrayList<>();
        fields.add(OutputFields.INSTANCE.field("description"));
        fields.add(OutputFields.INSTANCE.field("uuid"));
        GraphQlBuilderKt.setOutputFields(builder, fields);

        Zone result = groundApiClient.getById(Zone.class, builder).get().validateStatusAndValueNotNull("Zone");

        System.out.println(result);
        AssertionUtils.INSTANCE.assertEqualsDescriptors(id, result.getUuid());
    }

    @Disabled("launched ground application is needed")
    @Test
    void getZoneByNotExistingId() throws ExecutionException, InterruptedException {
        GraphQlGetByIdBuilder builder = Builders.INSTANCE.getById(randomUUID().toString());

        Response<Zone> result = groundApiClient.getById(Zone.class, builder).get();

        System.out.println(result);
        assertThat(result.getStatus()).isEqualTo(MODEL_NOT_FOUND);
        assertThat(result.getValue()).isNull();
    }

    @Disabled("launched ground application is needed")
    @Test
    void getZoneByIdWithoutBuilder() throws ExecutionException, InterruptedException {
        Zone zone = groundApiClient.createEmpty(Zone.class, null).get();
        Descriptor id = zone.getUuid();

        Zone result = groundApiClient.getById(Zone.class, id, null).get();

        assertThat(result).isNotNull();
        AssertionUtils.INSTANCE.assertEqualsDescriptors(id, result.getUuid());
    }

    @Disabled("launched ground application is needed")
    @Test
    void createEventWithNestedFields() throws ExecutionException, InterruptedException {
        Event event = new Event();
        event.setUrl("event url");
        event.setSize(23);

        Product product = new Product();
        product.setName(new StringOrMultilingual("bottle"));
        product.setRating(8.3);
        event.setProduct(product);

        Experience experience1 = new Experience();
        experience1.setMime("mime1");
        Experience experience2 = new Experience();
        experience2.setMime("mime2");
        ArrayList<Experience> experiences = new ArrayList<>();
        experiences.add(experience1);
        experiences.add(experience2);
        GraphQlList<Experience> experienceGraphQlList = new GraphQlList<>(experiences);
        event.setExperiences(experienceGraphQlList);

        GraphQlUpdateBuilder builder = Builders.INSTANCE.update(null);
        builder.setInput(event, new ArrayList<>());
        GraphQlBuilderKt.addOutputFields(
                builder,
                OutputFields.INSTANCE.field("url"),
                OutputFields.INSTANCE.field("size")
        );

        Event result = groundApiClient.update(Event.class, builder).get().validateStatusAndValueNotNull("Event");

        System.out.println(result);
        assertThat(result.getUrl()).isEqualTo(event.getUrl());
        assertThat(result.getSize()).isEqualTo(event.getSize());
    }

    @Disabled("launched ground application is needed")
    @Test
    void updateAccount() throws ExecutionException, InterruptedException {
        Account account = groundApiClient.createEmpty(Account.class, null).get();
        Descriptor id = account.getUuid();
        String updatedValue = "updated value";
        Account accountWithUpdating = new Account();
        accountWithUpdating.setValue(updatedValue);
        GraphQlUpdateBuilder builder = Builders.INSTANCE.update(id);
        builder.setInput(accountWithUpdating, new ArrayList<>());
        GraphQlBuilderKt.addOutputFields(
                builder,
                OutputFields.INSTANCE.field("value")
        );

        Account result = groundApiClient.update(Account.class, builder).get().validateStatusAndValueNotNull("Account");

        System.out.println(result);
        assertThat(result.getValue()).isEqualTo(updatedValue);
    }

    @Disabled("launched ground application is needed")
    @Test
    void addChanges() throws ExecutionException, InterruptedException {
        Account createdAccount = createAccount();
        Descriptor id = createdAccount.getUuid();
        String function = "function name";
        CustomProperties parameters = new CustomProperties(
                "param11 value",
                "param22 value"
        );

        List<Change> result = addChange(id, function, parameters);

        System.out.println(result);
        assertThat(result).isNotEmpty();
        Change createdChange = result.get(0);
        assertThat(createdChange).hasFieldOrProperty("compensation");
        Compensation compensation = createdChange.getCompensation();
        assertThat(compensation)
                .isNotNull()
                .hasFieldOrPropertyWithValue("function", function)
                .hasFieldOrProperty("parameters");
        Object savedParameters = compensation.getParameters().getObject();
        Object savedCustomProperties = MapperUtils.INSTANCE.convertValue(savedParameters, CustomProperties.class);
        assertThat(savedCustomProperties).isEqualTo(parameters);
    }

    private Account createAccount() throws ExecutionException, InterruptedException {
        Account account = new Account();
        account.setValue("base value");
        GraphQlUpdateBuilder builder = Builders.INSTANCE.update(null);
        builder.setInput(account, new ArrayList<>());
        return groundApiClient.update(Account.class, builder).get().validateStatusAndValueNotNull("account");
    }

    private List<Change> addChange(Descriptor accountId, String function, CustomProperties parameters) throws ExecutionException, InterruptedException {
        Change change = new Change();
        change.setOrdinal(23.0);

        Compensation compensation = new Compensation();
        compensation.setFunction(function);
        compensation.setParameters(new StringOrObject<>(parameters));
        change.setCompensation(compensation);

        GraphQlAddToSublistBuilder<Change> builder = Builders.INSTANCE.addToSublist(accountId, "changes", change, new ArrayList<>());
        GraphQlBuilderKt.setAllOutputFields(builder, Change.class);

        return groundApiClient.updateSublist(Account.class, Change.class, builder).get().validateStatusAndValueNotNull("change");
    }

    @Disabled("launched ground application is needed")
    @Test
    void removeChanges() throws ExecutionException, InterruptedException {
        Account createdAccount = createAccount();
        Descriptor id = createdAccount.getUuid();
        String function = "function name";
        CustomProperties parameters = new CustomProperties(
                "param11 value",
                "param22 value"
        );

        List<Change> addChangesResult = addChange(id, function, parameters);
        List<Change> addedChanges = addChangesResult.stream().filter(change -> function.equals(change.getCompensation().getFunction())).toList();
        assertThat(addedChanges).isNotEmpty();
        Change addedChange = addedChanges.get(0);

        GraphQlRemoveFromSublistBuilder builder = Builders.INSTANCE.removeFromSublist(id, "changes", addedChange.getUuid());
        GraphQlBuilderKt.addOutputFields(builder, OutputFields.INSTANCE.field("uuid"));

        List<Change> result = groundApiClient.updateSublist(Account.class, Change.class, builder).get().validateStatusAndValueNotNull("account");

        System.out.println(result);
        List<Change> removed = result.stream().filter(change -> function.equals(change.getCompensation().getFunction())).toList();
        assertThat(removed).isEmpty();
    }
}
