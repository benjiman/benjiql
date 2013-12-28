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
import static uk.co.benjiweber.benjiql.update.Delete.delete;
import static uk.co.benjiweber.benjiql.update.Upsert.update;

@RunWith(MockitoJUnitRunner.class)
public class UpdateTest {

    Person person = new Person();
    @Mock Connection mockConnection;
    @Mock PreparedStatement mockStatement;

    @Test public void should_match_example_with_no_restrictions() {
        String sql = update(person)
            .value(Person::getFirstName)
            .value(Person::getFavouriteNumber)
            .toSql();

        assertEquals("UPDATE person SET first_name = ?, favourite_number = ?", sql.trim());
    }

    @Test public void should_match_example_with_restrictions() {
        String sql = update(person)
            .value(Person::getFirstName)
            .value(Person::getFavouriteNumber)
            .where(Person::getLastName)
            .equalTo("weber")
            .and(Person::getFirstName)
            .notEqualTo("bob")
            .and(Person::getFirstName)
            .like("b%")
            .toSql();

        assertEquals("UPDATE person SET first_name = ?, favourite_number = ? WHERE last_name = ? AND first_name != ? AND first_name LIKE ?", sql.trim());
    }

    @Test public void should_set_values() throws SQLException {
        when(mockConnection.prepareStatement(any(String.class))).thenReturn(mockStatement);

        person.setFirstName("asdf");
        person.setFavouriteNumber(55);

        update(person)
            .value(Person::getFirstName)
            .value(Person::getFavouriteNumber)
            .where(Person::getLastName)
            .equalTo("weber")
            .and(Person::getFavouriteNumber)
            .equalTo(6)
            .execute(() -> mockConnection);

        verify(mockStatement).setString(1, "asdf");
        verify(mockStatement).setInt(2, 55);
        verify(mockStatement).setString(3, "weber");
        verify(mockStatement).setInt(4, 6);
    }
}
