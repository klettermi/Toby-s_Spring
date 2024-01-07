package toby.user.dao;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;

public class CountingDaoFactory {

    public UserDao userDao() {
        UserDao userDao = new UserDao();
        userDao.setDataSource(dataSource());
        return userDao;
    }


    public ConnectionMaker connectionMaker(){
        return new CountingConnectionMaker(dataSource());
    }


    public DataSource dataSource(){
        return new SimpleDriverDataSource();
    }


}
