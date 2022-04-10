package uk.co.benjiweber.benjiql.query;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.benjiweber.benjiql.results.Mapper;
import uk.co.benjiweber.benjiql.results.RecordMapper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.benjiweber.benjiql.ddl.Create.relationship;
import static uk.co.benjiweber.benjiql.query.Select.from;

@RunWith(MockitoJUnitRunner.class)
public class SelectRecordTest {

    public record Person (String firstName, String lastName, Integer favouriteNumber) {}
    public record Conspiracy (Set<Person> members, String name) {}


    @Mock Connection mockConnection;
    @Mock PreparedStatement mockStatement;
    @Mock ResultSet mockResults;
    Mapper<Person> personMapper = new RecordMapper<Person>(Person.class);

    @Test public void should_match_example() {
        String sql = from(Person.class)
                .where(Person::firstName)
                .equalTo("benji")
                .and(Person::lastName)
                .notEqualTo("foo")
                .and(Person::lastName)
                .like("web%")
                .and(Person::favouriteNumber)
                .equalTo(5)
                .toSql();

        assertEquals("SELECT * FROM person WHERE person.first_name = ? AND person.last_name != ? AND person.last_name LIKE ? AND person.favourite_number = ?", sql.trim());
    }

    @Test public void should_allow_joins() {
        String sql = from(Person.class)
                .where(Person::lastName)
                .equalTo("smith")
                .join(relationship(Conspiracy.class, Person.class).invert())
                .using(Person::firstName, Person::lastName)
                .join(Conspiracy.class)
                .using(Conspiracy::name)
                .where(Conspiracy::name)
                .equalTo("nsa")
                .toSql();

        assertEquals(
            "SELECT * FROM person " +
            "JOIN conspiracy_person " +
            "ON person.first_name = conspiracy_person.person_first_name " +
            "AND person.last_name = conspiracy_person.person_last_name " +
            "JOIN conspiracy " +
            "ON conspiracy_person.conspiracy_name = conspiracy.name " +
            "WHERE person.last_name = ? " +
            "AND conspiracy.name = ?",

            sql
        );
    }

    @Test public void should_set_values() throws SQLException {
        when(mockConnection.prepareStatement(any(String.class))).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResults);

        Optional<Person> result = from(Person.class)
                .where(Person::firstName)
                .equalTo("benji")
                .and(Person::lastName)
                .notEqualTo("foo")
                .and(Person::lastName)
                .like("web%")
                .and(Person::favouriteNumber)
                .equalTo(5)
                .select(personMapper, () -> mockConnection);

        verify(mockStatement).setString(1,"benji");
        verify(mockStatement).setString(2,"foo");
        verify(mockStatement).setString(3,"web%");
        verify(mockStatement).setInt(4, 5);
    }

    @Test public void should_map_results() throws SQLException {
        when(mockConnection.prepareStatement(any(String.class))).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResults);
        when(mockResults.next()).thenReturn(true);
        when(mockResults.getObject("first_name")).thenReturn("fname");
        when(mockResults.getObject("last_name")).thenReturn("lname");
        when(mockResults.getObject("favourite_number")).thenReturn(9001);

        Optional<Person> result = from(Person.class)
                .where(Person::firstName)
                .equalTo("benji")
                .and(Person::lastName)
                .notEqualTo("foo")
                .and(Person::lastName)
                .like("web%")
                .and(Person::favouriteNumber)
                .equalTo(5)
                .select(personMapper, () -> mockConnection);

        assertEquals("fname", result.get().firstName());
        assertEquals("lname", result.get().lastName());
        assertEquals((Integer)9001, result.get().favouriteNumber());
    }

    @Test public void should_map_results_list() throws SQLException {
        when(mockConnection.prepareStatement(any(String.class))).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResults);
        when(mockResults.next()).thenReturn(true).thenReturn(false);
        when(mockResults.getObject("first_name")).thenReturn("fname");
        when(mockResults.getObject("last_name")).thenReturn("lname");
        when(mockResults.getObject("favourite_number")).thenReturn(9001);

        List<Person> result = from(Person.class)
                .where(Person::firstName)
                .equalTo("benji")
                .and(Person::lastName)
                .notEqualTo("foo")
                .and(Person::lastName)
                .like("web%")
                .and(Person::favouriteNumber)
                .equalTo(5)
                .list(personMapper, () -> mockConnection);

        assertEquals(1, result.size());
        assertEquals("fname", result.get(0).firstName());
        assertEquals("lname", result.get(0).lastName());
        assertEquals((Integer)9001, result.get(0).favouriteNumber());
    }


}
