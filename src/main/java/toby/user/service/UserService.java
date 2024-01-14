package toby.user.service;

import toby.user.domain.User;

public interface UserService {
    void add(User user);
    void upgradeLevels();
}
