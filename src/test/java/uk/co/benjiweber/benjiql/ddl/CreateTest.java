package uk.co.benjiweber.benjiql.ddl;

import org.junit.Test;
import uk.co.benjiweber.benjiql.example.Conspiracy;
import uk.co.benjiweber.benjiql.example.Person;

import static org.junit.Assert.assertEquals;
import static uk.co.benjiweber.benjiql.ddl.Create.create;
import static uk.co.benjiweber.benjiql.ddl.Create.relationship;

public class CreateTest {
    static class Address {
        public String getFirstLine() { return null; }
        public Integer getHouseNumber() { return null; }
    }

    @Test
    public void check_create_matches_example() throws Exception {
        String sql = create(Address.class)
            .field(Address::getFirstLine)
            .field(Address::getHouseNumber)
            .toSql();

        assertEquals("CREATE TABLE IF NOT EXISTS address ( first_line text, house_number integer );", sql.trim());

    }

    @Test
    public void example_join_table() {
        String sql = create(relationship(Conspiracy.class, Person.class))
            .fieldLeft(Conspiracy::getName)
            .fieldRight(Person::getFirstName)
            .fieldRight(Person::getLastName)
            .toSql();

        assertEquals("CREATE TABLE IF NOT EXISTS conspiracy_person ( conspiracy_name text, person_first_name text, person_last_name text ); ", sql);
    }
}
