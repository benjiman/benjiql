package uk.co.benjiweber.benjiql.example;

import org.junit.Test;
import uk.co.benjiweber.benjiql.results.Mapper;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Optional;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static uk.co.benjiweber.benjiql.ddl.Create.create;
import static uk.co.benjiweber.benjiql.query.Select.from;
import static uk.co.benjiweber.benjiql.update.Delete.delete;
import static uk.co.benjiweber.benjiql.update.Upsert.insert;
import static uk.co.benjiweber.benjiql.update.Upsert.update;

public class RealExample {

    @Test public void example_of_create_table_persist_retrieve_and_update_with_real_database() throws SQLException {
        create(Person.class)
            .field(Person::getFirstName)
            .field(Person::getLastName)
            .field(Person::getFavouriteNumber)
            .execute(this::openConnection);

        delete(Person.class)
            .where(Person::getLastName)
            .equalTo("weber")
            .execute(this::openConnection);

        Person benji = new Person("benji","weber");
        benji.setFavouriteNumber(9);

        insert(benji)
            .value(Person::getFirstName)
            .value(Person::getLastName)
            .value(Person::getFavouriteNumber)
            .execute(this::openConnection);

        benji.setFirstName("benji-updated");

        update(benji)
            .value(Person::getFirstName)
            .execute(this::openConnection);

        Mapper<Person> personMapper = Mapper.mapper(Person::new)
            .set(Person::setFirstName)
            .set(Person::setLastName)
            .set(Person::setFavouriteNumber);

        Optional<Person> result = from(Person.class)
            .where(Person::getFirstName)
            .like("%updated")
            .and(Person::getLastName)
            .equalTo("weber")
            .select(personMapper, this::openConnection);

        assertEquals("benji-updated", result.get().getFirstName());
        assertEquals("weber", result.get().getLastName());
        assertEquals((Integer)9, result.get().getFavouriteNumber());
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
