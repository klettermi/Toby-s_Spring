package toby.user.dao;

import toby.user.domain.User;

import java.util.List;

public interface UserDao {
    void add(User user);
    User get(String id);
    List<User> getAll();
    void deleteAll();
    Integer getCount();
}
