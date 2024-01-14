package toby.test;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.PlatformTransactionManager;
import toby.user.dao.UserDao;
import toby.user.domain.Level;
import toby.user.domain.User;
import toby.user.service.UserService;

import javax.sql.DataSource;
import javax.xml.crypto.Data;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.fail;
import static toby.user.service.UserService.MIN_LOGCOUNT_FOR_SILVER;
import static toby.user.service.UserService.MIN_RECCOMEND_FOR_GOLD;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(locations = "/applicationContext.xml")
public class UserServiceTest {
    @Autowired
    PlatformTransactionManager transactionManager;

    @Autowired
    UserService userService;

    @Autowired
    private UserDao userDao;

    @Autowired
    DataSource dataSource;

    @Autowired
    MailSender mailSender;

    List<User> users;

    // UserService의 테스트용 대역 클래스
    static class TestUserService extends UserService{
        private String id;
        private TestUserService(String id){
            this.id = id;
        }

        @Override
        protected void upgradeLevel(User user) {
            if(user.getId().equals(this.id)) throw new TestUserServiceException();
            super.upgradeLevel(user);
        }
    }

    // 테스트용 예외
    static class TestUserServiceException extends RuntimeException{

    }
    @BeforeEach
    public void setUp(){
        users = Arrays.asList(
                new User("bumjin", "박범진", "p1", Level.BASIC, MIN_LOGCOUNT_FOR_SILVER - 1, 0, "bumjin@email.com"),
                new User("joytouch", "강명성", "p2", Level.BASIC, MIN_LOGCOUNT_FOR_SILVER, 0, "joytouch@email.com"),
                new User("erwins", "신승한", "p3", Level.SILVER, 60, MIN_RECCOMEND_FOR_GOLD -1, "erwins@email.com"),
                new User("madnite1", "이상호", "p4", Level.SILVER, 60, MIN_RECCOMEND_FOR_GOLD, "madnite1@email.com"),
                new User("green", "오민규","p5", Level.GOLD, 100, Integer.MAX_VALUE, "green@email.com")
        );
    }

    @Test
    public void add(){
        userDao.deleteAll();

        User userWithLevel = users.get(4);
        User userWithoutLevel = users.get(0);
        userWithoutLevel.setLevel(null);

        userService.add(userWithLevel);
        userService.add(userWithoutLevel);

        User userWithLevelRead = userDao.get(userWithLevel.getId());
        User userWithoutLevelRead = userDao.get(userWithoutLevel.getId());

        assertThat(userWithLevelRead.getLevel()).isEqualTo(userWithLevel.getLevel());
        assertThat(userWithoutLevelRead.getLevel()).isEqualTo(userWithoutLevelRead.getLevel());
    }
    @Test
    @DirtiesContext
    public void upgradeLevels() throws Exception {
        userDao.deleteAll();
        for(User user : users) userDao.add(user);

        MockMailSender mockMailSender = new MockMailSender();
        userService.setMailSender(mockMailSender);

        userService.upgradeLevels();

        checkLevelUpgraded(users.get(0), false);
        checkLevelUpgraded(users.get(1), true);
        checkLevelUpgraded(users.get(2), false);
        checkLevelUpgraded(users.get(3), true);
        checkLevelUpgraded(users.get(4), false);

        List<String> request = mockMailSender.getRequests();
        assertThat(request.size()).isEqualTo(2);
        assertThat(request.get(0)).isEqualTo(users.get(1).getEmail());
        assertThat(request.get(1)).isEqualTo(users.get(3).getEmail());
    }

    @Test
    public void upgradeAllorNothing() throws Exception {
        UserService testUserService = new TestUserService(users.get(3).getId());
        testUserService.setUserDao(this.userDao);
        testUserService.setDataSource(this.dataSource);
        testUserService.setTransactionManager(transactionManager);
        testUserService.setMailSender(mailSender);

        userDao.deleteAll();
        for(User user : users) userDao.add(user);

        try{
            testUserService.upgradeLevels();
            fail("TestUserServiceException expected");
        }catch (Exception e){

        }

        checkLevelUpgraded(users.get(1), false);
    }
    private void checkLevelUpgraded(User user, boolean upgraded){
        User userUpdate = userDao.get(user.getId());
        if(upgraded){
            assertThat(userUpdate.getLevel()).isEqualTo(user.getLevel().nextLevel());
        }else{
            assertThat(userUpdate.getLevel()).isEqualTo(user.getLevel());
        }
    }
    private void checkLevel(User user, Level expectedLevel){
        User userUpdate = userDao.get(user.getId());
        assertThat(userUpdate.getLevel()).isEqualTo(expectedLevel);
    }

    static class MockMailSender implements MailSender{
        private List<String> requests = new ArrayList<String>();

        public List<String> getRequests() {
            return requests;
        }

        @Override
        public void send(SimpleMailMessage mailMessage) throws MailException {
            requests.add(mailMessage.getTo()[0]);
        }

        @Override
        public void send(SimpleMailMessage... mailMessage) throws MailException {

        }

    }
}
