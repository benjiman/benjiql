package uk.co.benjiweber.benjiql.example;

import org.junit.Test;
import uk.co.benjiweber.benjiql.results.Mapper;
import uk.co.benjiweber.benjiql.results.RecordMapper;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static uk.co.benjiweber.benjiql.ddl.Create.create;
import static uk.co.benjiweber.benjiql.ddl.Create.relationship;
import static uk.co.benjiweber.benjiql.query.Select.from;
import static uk.co.benjiweber.benjiql.results.RecordMapper.mapper;
import static uk.co.benjiweber.benjiql.update.Delete.delete;
import static uk.co.benjiweber.benjiql.update.Upsert.insert;
import static uk.co.benjiweber.benjiql.update.Upsert.update;

public class RealExampleWithRecords {
    public record Person (String firstName, String lastName, Integer favouriteNumber) {}
    public record Conspiracy (Set<Person> members, String name) {
        public Conspiracy(String name) {
            this(new HashSet<>(), name);
        }
    }

    @Test public void example_of_create_table_persist_retrieve_and_update_with_real_database() throws SQLException {
        create(Person.class)
            .field(Person::firstName)
            .field(Person::lastName)
            .field(Person::favouriteNumber)
            .execute(this::openConnection);

        delete(Person.class)
            .where(Person::lastName)
            .equalTo("weber")
            .execute(this::openConnection);

        Person benji = new Person("benji","weber",9);

        insert(benji)
            .value(Person::firstName)
            .value(Person::lastName)
            .value(Person::favouriteNumber)
            .execute(this::openConnection);

        benji = new Person("benji-updated", "weber", 9);

        update(benji)
            .value(Person::firstName)
            .where(Person::lastName)
            .equalTo("weber")
            .execute(this::openConnection);

        Optional<Person> result = from(Person.class)
            .where(Person::firstName)
            .like("%updated")
            .and(Person::lastName)
            .equalTo("weber")
            .select(mapper(Person.class), this::openConnection);

        assertEquals("benji-updated", result.get().firstName());
        assertEquals("weber", result.get().lastName());
        assertEquals((Integer)9, result.get().favouriteNumber());
    }

    @Test public void example_of_select_with_join() throws SQLException {
        create(Person.class)
            .field(Person::firstName)
            .field(Person::lastName)
            .field(Person::favouriteNumber)
            .execute(this::openConnection);

        create(Conspiracy.class)
            .field(Conspiracy::name)
            .execute(this::openConnection);

        create(relationship(Conspiracy.class, Person.class))
            .fieldLeft(Conspiracy::name)
            .fieldRight(Person::firstName)
            .fieldRight(Person::lastName)
            .execute(this::openConnection);

        delete(Person.class)
            .execute(this::openConnection);

        delete(Conspiracy.class)
            .execute(this::openConnection);

        delete(relationship(Conspiracy.class, Person.class))
            .execute(this::openConnection);

        Person smith = new Person("agent","smith", 6);

        insert(smith)
            .value(Person::firstName)
            .value(Person::lastName)
            .value(Person::favouriteNumber)
            .execute(this::openConnection);

        Conspiracy nsa = new Conspiracy("nsa");
        nsa.members().add(smith);

        insert(nsa)
            .value(Conspiracy::name)
            .execute(this::openConnection);

        nsa.members().forEach(agent -> {
            insert(nsa, agent)
                    .valueLeft(Conspiracy::name)
                    .valueRight(Person::lastName)
                    .valueRight(Person::firstName)
                    .execute(this::openConnection);
        });

        Optional<Person> person = from(Person.class)
            .where(Person::lastName)
            .equalTo("smith")
            .join(relationship(Conspiracy.class, Person.class).invert())
            .using(Person::firstName, Person::lastName)
            .join(Conspiracy.class)
            .using(Conspiracy::name)
            .where(Conspiracy::name)
            .equalTo("nsa")
            .select(mapper(Person.class), this::openConnection);

        assertEquals(smith, person.get());

        delete(relationship(Conspiracy.class, Person.class))
            .whereLeft(Conspiracy::name)
            .equalTo("nsa")
            .andRight(Person::lastName)
            .equalTo("smith")
            .execute(this::openConnection);

    }


    private Connection openConnection() {
        try {
            Properties connectionProps = new Properties();
            connectionProps.put("user", "benjiql");
            connectionProps.put("password", "benjiql");
            return DriverManager.getConnection("jdbc:postgresql:benjiql", connectionProps);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
