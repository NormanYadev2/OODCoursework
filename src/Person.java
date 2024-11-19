import java.sql.Connection;

public abstract class Person {
    protected String username;
    protected String password;

    public Person(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // Abstract method to enforce login implementation
    public abstract boolean login(Connection conn);
}
