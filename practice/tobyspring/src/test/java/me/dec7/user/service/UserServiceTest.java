package me.dec7.user.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;

import me.dec7.user.dao.UserDao;
import me.dec7.user.domain.Level;
import me.dec7.user.domain.User;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/test-applicationContext.xml")
public class UserServiceTest {

	@Autowired
	UserService userService;
	
	@Autowired
	UserDao userDao;
	
	// test fixture
	List<User> users;
	
	@Before
	public void setUp() {
		// 배열을 리스트로 만들어주는 메소드, 배열을 가변인자로 넣어줄 수 있어 편리
		/*
		 * BASIC, SILVER를 2개씩 생성
		 * 각각 데이터의 경계값을 선택하여 테스트
		 */
		users = Arrays.asList(
				new User("dec1", "동규1", "pw1", Level.BASIC, StandardUpgradePolicy.MIN_LOGCOUNT_FOR_SILVER-1, 0),
				new User("dec2", "동규2", "pw2", Level.BASIC, StandardUpgradePolicy.MIN_LOGCOUNT_FOR_SILVER, 0),
				new User("dec3", "동규3", "pw3", Level.SILVER, 60, StandardUpgradePolicy.MIN_RECOMMEND_FOR_GOLD-1),
				new User("dec4", "동규4", "pw4", Level.SILVER, 60, StandardUpgradePolicy.MIN_RECOMMEND_FOR_GOLD),
				new User("dec5", "동규5", "pw5", Level.GOLD, 100, 100)
			);
	}
	
	/*
	 * @Test method가 없는경우 에러
	 * userService bean이 생성되서 userService변수에 주입되는지 확인하는 메소드
	 */
	@Test
	public void bean() {
		assertThat(this.userService, is(notNullValue()));
	}
	
	@Test
	public void upgradeLevels() {
		userDao.deleteAll();
		
		for (User user : users) {
			userDao.add(user);
		}
		
		userService.upgradeLevels();
		
		/*
		 * 리패토링
		 *  - 어떤 레벨로 바뀔 것인가라기보다
		 *    다음 레벨로 업그레이드 될 것인가 아닌가
		 *
		checkLevel(users.get(0), Level.BASIC);
		checkLevel(users.get(1), Level.SILVER);
		checkLevel(users.get(2), Level.SILVER);
		checkLevel(users.get(3), Level.GOLD);
		checkLevel(users.get(4), Level.GOLD);
		 */
		
		checkLevel(users.get(0), false);
		checkLevel(users.get(1), true);
		checkLevel(users.get(2), false);
		checkLevel(users.get(3), true);
		checkLevel(users.get(4), false);
		
		
	}

	@Test
	public void add() {
		userDao.deleteAll();
		
		// GOLD level이 지정된 유저는 레벨을 초기화하지 않아야 함
		User userWithLevel = users.get(4);
		
		// Level이 비어있는 사용자, 로직에 따라 BASIC으로 설정
		User userWithoutLevel = users.get(0);
		userWithoutLevel.setLevel(null);
		
		userService.add(userWithLevel);
		userService.add(userWithoutLevel);
		
		// DB에 저장된 결과를 가져와 확인
		User userWithLevelRead = userDao.get(userWithLevel.getId());
		User userWithoutLevelRead = userDao.get(userWithoutLevel.getId());
		
		assertThat(userWithLevelRead.getLevel(), is(userWithLevel.getLevel()));
		assertThat(userWithoutLevelRead.getLevel(), is(Level.BASIC));
		
	}
	/*
	 * 리팩토링
	private void checkLevel(User user, Level expectedLevel) {
		User userUpdate = userDao.get(user.getId());
		assertThat(userUpdate.getLevel(), is(expectedLevel));
	}
	*/
	private void checkLevel(User user, boolean upgraded) {
		User userUpdate = userDao.get(user.getId());
		
		if (upgraded) {
			assertThat(userUpdate.getLevel(), is(user.getLevel().nextLevel()));
			
		} else {
			assertThat(userUpdate.getLevel(), is(user.getLevel()));
			
		}
	}


}
