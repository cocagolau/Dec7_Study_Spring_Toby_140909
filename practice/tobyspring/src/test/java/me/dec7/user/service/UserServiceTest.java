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
import org.springframework.mail.MailSender;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.PlatformTransactionManager;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/test-applicationContext.xml")
public class UserServiceTest {
	
	/*
	 * Autowired는 interface 타입이라도 DI가 가능
	 * 하지만 applicationContext에는 2개의 UserService타입이 정의되어 있음
	 *  - 이 경우 필드 이름으로 찾음
	 */
	@Autowired
	UserService userService;
	
	/*
	 * UserServiceImpl
	 *  - MailSender의 Mock 오브젝트를 이용한 테스트에서 MailSender를 직접 DI해줘야했음.
	 *    따라서 MailSender를 DI해줄 대상을 명확히 해야함
	 */
	@Autowired
	UserServiceImpl userServiceImpl;
	
	@Autowired
	MailSender mailSender;
	
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
				new User("dec1", "동규1", "pw1", "dec1@gmail.com", Level.BASIC, UserServiceImpl.MIN_LOGCOUNT_FOR_SILVER-1, 0),
				new User("dec2", "동규2", "pw2", "dec2@gmail.com", Level.BASIC, UserServiceImpl.MIN_LOGCOUNT_FOR_SILVER, 0),
				new User("dec3", "동규3", "pw3", "dec3@gmail.com",Level.SILVER, 60, UserServiceImpl.MIN_RECOMMEND_FOR_GOLD-1),
				new User("dec4", "동규4", "pw4", "dec4@gmail.com",Level.SILVER, 60, UserServiceImpl.MIN_RECOMMEND_FOR_GOLD),
				new User("dec5", "동규5", "pw5", "dec5@gmail.com",Level.GOLD, 100, 100)
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
	// 컨텍스트의 DI설정을 변경하는 테스트라는 것을 알려줌
	@DirtiesContext
	public void upgradeLevels() throws Exception {
		userDao.deleteAll();
		
		for (User user : users) {
			userDao.add(user);
		}
		
		// 메일 발송 결과를 테스트할 수 있도록 목 오브젝트를 만들어 userService의 의존 오브젝트로 주입
		MockMailSender mockMailSender = new MockMailSender();
		// userService.setMailSender(mockMailSender);
		userServiceImpl.setMailSender(mockMailSender);
		
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
		
		// mock 오브젝트에 저장된 메일 수신자 목록을 가져와 업그레이드 대상과 일치하는지 확인
		List<String> request = mockMailSender.getRequests();
		assertThat(request.size(), is(2));
		assertThat(request.get(0), is(users.get(1).getEmail()));
		assertThat(request.get(1), is(users.get(3).getEmail()));
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
	
	
	/*
	 * 트랜잭션 경계설정 코드 분리와 DI를 통한 연결 장점
	 *  1. 비지니스 로직인 UserServiceImpl 코드는 기술적인 내용을 전혀 신경쓰지 않아도 됨
	 *  2. 비지니스 로직에 대한 테스트를 쉽게 만들 수 있음
	 */
	// @Test(expected=AssertionError.class)
	@Test(expected=TestUserServiceException.class)
	public void upgradeAllOrNothing() throws Exception {
		TestUserService testUserService = new TestUserService(users.get(3).getId());
		
		// 수동 DI	
		 testUserService.setUserDao(this.userDao);
		 testUserService.setMailSender(mailSender);
		 // testUserService.setTransactionManager(transactionManager);
		 
		 // 트랜잭션 기능을 분리한 UserServiceTx는 예외 발생용으로 수정할 필요가 없음
		 UserServiceTx txUserService = new UserServiceTx();
		 txUserService.setTransactionManager(transactionManager);
		 txUserService.setUserService(testUserService);
		
		userDao.deleteAll();
		
		for (User user : users) {
			userDao.add(user);
		}
		
		// 트랜잭션 기능을 분리한 오브젝트를 통해 예외발생용 TestUserService가 호출되게 해야함
		txUserService.upgradeLevels();
		/*
		try {
			// TestUserService는 업그레이드시 예외 발생시 정상
			fail("TestUserServiceException expected");
		} catch (TestUserServiceException e) {
			
		}*/
		
		// 예외 발생 전 레벨 변경이 있었던 사용자의 레벨이 처음 상태로 바뀌었는지 확인
		checkLevelUpgraded(users.get(1), false);
	}

}
