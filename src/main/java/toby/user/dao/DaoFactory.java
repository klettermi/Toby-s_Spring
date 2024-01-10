package toby.user.dao;

import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;

 // 애플리케이션 컨텍스트 또는 빈 팩토리가 사용할 설정 정보라는 표시
public class DaoFactory {


    public UserDaoJdbc userDao() {
        UserDaoJdbc userDao = new UserDaoJdbc();
        userDao.setDataSource(dataSource());
        return userDao;
    }


    public DataSource dataSource(){
        return new SimpleDriverDataSource();
    }
}
