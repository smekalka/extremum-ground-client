package io.extremum.groundClient.model;

import io.extremum.model.tools.mapper.GraphQlListUtils;
import io.extremum.sharedmodels.basic.GraphQlList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * todo Такую модуль можно взять за шаблонную:
 * - стандартные геттеры, сеттеры, equals
 * - List заменен на GraphQlList
 */
public class Account extends TestBasicModel {
    private String value;
    private AccountDatatype datatype;
    private GraphQlList<Change> changes = new GraphQlList<>();

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public AccountDatatype getDatatype() {
        return datatype;
    }

    public void setDatatype(AccountDatatype datatype) {
        this.datatype = datatype;
    }

    public GraphQlList<Change> getChanges() {
        return changes;
    }

    public void setChanges(GraphQlList<Change> changes) {
        this.changes = changes;
    }

    @Override
    public String toString() {
        return "Account{" +
                "value='" + value + '\'' +
                ", datatype=" + datatype +
                ", changes=" + changes +
                ", uuid=" + uuid +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Account account = (Account) o;

        if (!Objects.equals(value, account.value)) return false;
        if (datatype != account.datatype) return false;
        List<Change> changes1 = toList(changes);
        List<Change> changes2 = toList(account.changes);
        return Objects.equals(changes1, changes2);
    }

    private <T> List<T> toList(GraphQlList<T> graphQlList) {
        if (graphQlList == null) {
            return new ArrayList<>();
        } else {
            return GraphQlListUtils.INSTANCE.toList(graphQlList);
        }
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (value != null ? value.hashCode() : 0);
        result = 31 * result + (datatype != null ? datatype.hashCode() : 0);
        result = 31 * result + (changes != null ? changes.hashCode() : 0);
        return result;
    }

    public enum AccountDatatype {

        NUMBER("number"),
        STRING("string"),
        CUSTOM("custom"),
        NUMBER_ARRAY("number_array"),
        STRING_ARRAY("string_array"),
        CUSTOM_ARRAY("custom_array");
        private final String value;
        private final static Map<String, AccountDatatype> CONSTANTS = new HashMap<String, AccountDatatype>();

        static {
            for (AccountDatatype c : values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        AccountDatatype(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }

        public String value() {
            return this.value;
        }

        public static AccountDatatype fromValue(String value) {
            AccountDatatype constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }
    }
}
