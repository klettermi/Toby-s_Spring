package toby.user.dao;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class CountingConnectionMaker implements ConnectionMaker{
    int counter = 0;
    private DataSource dataSource;

    public CountingConnectionMaker(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Connection makeConnection() throws ClassNotFoundException, SQLException{
        this.counter++;
        return dataSource.getConnection();
    }

    public int getCounter() {
        return counter;
    }
}
