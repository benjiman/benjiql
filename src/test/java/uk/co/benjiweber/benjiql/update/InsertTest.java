package uk.co.benjiweber.benjiql.update;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.co.benjiweber.benjiql.example.Person;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.benjiweber.benjiql.update.Upsert.insert;
import static uk.co.benjiweber.benjiql.update.Upsert.update;

@RunWith(MockitoJUnitRunner.class)
public class InsertTest {

    Person person = new Person();
    @Mock Connection mockConnection;
    @Mock PreparedStatement mockStatement;

    @Test public void should_match_example() {
        String sql = insert(person)
            .value(Person::getFirstName)
            .value(Person::getFavouriteNumber)
            .toSql();

        assertEquals("INSERT INTO person (first_name, favourite_number) VALUES ( ?, ? )", sql.trim());
    }

    @Test public void should_set_values() throws SQLException {
        when(mockConnection.prepareStatement(any(String.class))).thenReturn(mockStatement);

        person.setFirstName("asdf");
        person.setFavouriteNumber(55);

        insert(person)
            .value(Person::getFirstName)
            .value(Person::getFavouriteNumber)
            .execute(() -> mockConnection);

        verify(mockStatement).setString(1, "asdf");
        verify(mockStatement).setInt(2, 55);
    }
}
