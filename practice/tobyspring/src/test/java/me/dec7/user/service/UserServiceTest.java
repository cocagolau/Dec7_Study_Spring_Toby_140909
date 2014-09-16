package me.dec7.user.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

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
import org.springframework.transaction.PlatformTransactionManager;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/test-applicationContext.xml")
public class UserServiceTest {

	@Autowired
	UserService userService;
	
//	@Autowired
//	DataSource dataSource;
	
	@Autowired
	PlatformTransactionManager transactionManager;
	
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
				new User("dec1", "동규1", "pw1", Level.BASIC, UserService.MIN_LOGCOUNT_FOR_SILVER-1, 0),
				new User("dec2", "동규2", "pw2", Level.BASIC, UserService.MIN_LOGCOUNT_FOR_SILVER, 0),
				new User("dec3", "동규3", "pw3", Level.SILVER, 60, UserService.MIN_RECOMMEND_FOR_GOLD-1),
				new User("dec4", "동규4", "pw4", Level.SILVER, 60, UserService.MIN_RECOMMEND_FOR_GOLD),
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
	public void upgradeLevels() throws Exception {
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
		
		checkLevelUpgraded(users.get(0), false);
		checkLevelUpgraded(users.get(1), true);
		checkLevelUpgraded(users.get(2), false);
		checkLevelUpgraded(users.get(3), true);
		checkLevelUpgraded(users.get(4), false);
		
		
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
	private void checkLevelUpgraded(User user, boolean upgraded) {
		User userUpdate = userDao.get(user.getId());
		
		if (upgraded) {
			assertThat(userUpdate.getLevel(), is(user.getLevel().nextLevel()));
			
		} else {
			assertThat(userUpdate.getLevel(), is(user.getLevel()));
			
		}
	}
	
	// @Test(expected=TestUserServiceException.class)
	@Test(expected=AssertionError.class)
	public void upgradeAllOrNothing() throws Exception {
		UserService testUserService = new TestUserService(users.get(3).getId());
		// 수동 DI
		testUserService.setUserDao(this.userDao);
//		testUserService.setDataSource(this.dataSource);
		testUserService.setTransactionManager(transactionManager);
		
		userDao.deleteAll();
		
		for (User user : users) {
			userDao.add(user);
		}
		
		try {
			testUserService.upgradeLevels();
			// TestUserService는 업그레이드시 예외 발생시 정상
			fail("TestUserServiceException expected");
		} catch (TestUserServiceException e) {
			
		}
		
		// 예외 발생 전 레벨 변경이 있었던 사용자의 레벨이 처음 상태로 바뀌었는지 확인
		checkLevelUpgraded(users.get(1), false);
		
		/*
		 * 결과는 java.lang.AssertionError Expected: is<BASIC> got:<SILVER>
		 *  - 두번째 사용자 레벨이 BASIC에서 SILVER로 바뀌 것이,
		 *    네번째 사용자 처리중 예외 발생했지만 그대로 유지됨
		 *    
		 * 원인
		 *  - 트랜젝션 문제
		 *  - 모든 사용자의 레벨을 업그레이드 하는 작업인 upgradeLevels() 메소드가
		 *    하나의 트랜잭션 안에서 동작하지 않았기 때문
		 *  - 트랜잭션은 더 이상 나눌 수 없는 단위 작업 / 원자성
		 *  - 전체 성공 혹은 전체 실패
		 */
	}


}
