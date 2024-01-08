package toby.user.dao;

import org.springframework.dao.EmptyResultDataAccessException;
import toby.user.domain.User;

import javax.sql.DataSource;
import java.sql.*;

public class UserDao {
    private DataSource dataSource;

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void add(User user) throws SQLException {
        Connection c = dataSource.getConnection();

        // 2. SQL을 담은 Statement(or PreparedStatement)를 만든다.
        PreparedStatement ps = c.prepareStatement(
                "insert into users(id, name, password) values (?, ?, ?)"
        );

        // 3. ps를 실행한다.
        ps.setString(1, user.getId());
        ps.setString(2, user.getName());
        ps.setString(3, user.getPassword());

        ps.executeUpdate();

        // 4. 작업 중에 생성된 Connection, Statement, ResultSet과 같은 리소스는 작업이 마친 후 반드시 닫아준다.
        ps.close();
        c.close();;
    }

    public User get(String id) throws SQLException {
        Connection c = dataSource.getConnection();

        PreparedStatement ps = c.prepareStatement(
                "select * from users where id = ?"
        );
        ps.setString(1, id);

        ResultSet rs = ps.executeQuery();

        User user = null;
        if(rs.next()){
            user = new User();
            user.setId(rs.getString("id"));
            user.setName(rs.getString("name"));
            user.setPassword(rs.getString("password"));
        }

        rs.close();
        ps.close();
        c.close();

        if(user == null) throw new EmptyResultDataAccessException(1);

        return user;
    }

    public void deleteAll() throws SQLException {
        Connection c = null;
        PreparedStatement ps = null;

        try{
            c = dataSource.getConnection();
            ps = makeStatement(c);
            ps.executeUpdate();
        }catch(SQLException e){
            throw e;
        }finally {
            if(ps != null){
                try{
                    ps.close();
                }catch (SQLException e){
                }
            }
            if(c != null){
                try{
                    c.close();
                }catch (SQLException e){

                }
            }
        }
    }

    private PreparedStatement makeStatement(Connection c) throws SQLException{
        PreparedStatement ps;
        ps = c.prepareStatement("delete from users");
        return ps;
    }
    public int getCount() throws SQLException {
        Connection c = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try{
            c = dataSource.getConnection();
            ps = c.prepareStatement("select count(*) from users");
            rs = ps.executeQuery();
            rs.next();
            return rs.getInt(1);
        }catch (SQLException e){
            throw e;
        }finally {
            if(rs != null){
                try{
                    rs.close();
                }catch (SQLException e){

                }
            }
            if(ps != null){
                try{
                    ps.close();
                }catch (SQLException e){

                }
            }
            if(c != null){
                try{
                    c.close();
                }catch (SQLException e){

                }
            }
        }
    }
}

