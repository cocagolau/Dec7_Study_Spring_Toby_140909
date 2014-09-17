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

	@Autowired
	UserService userService;
	
//	@Autowired
//	DataSource dataSource;
	
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
				new User("dec1", "동규1", "pw1", "dec1@gmail.com", Level.BASIC, UserService.MIN_LOGCOUNT_FOR_SILVER-1, 0),
				new User("dec2", "동규2", "pw2", "dec2@gmail.com", Level.BASIC, UserService.MIN_LOGCOUNT_FOR_SILVER, 0),
				new User("dec3", "동규3", "pw3", "dec3@gmail.com",Level.SILVER, 60, UserService.MIN_RECOMMEND_FOR_GOLD-1),
				new User("dec4", "동규4", "pw4", "dec4@gmail.com",Level.SILVER, 60, UserService.MIN_RECOMMEND_FOR_GOLD),
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
		userService.setMailSender(mockMailSender);
		
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
	
	// @Test(expected=TestUserServiceException.class)
	@Test(expected=AssertionError.class)
	public void upgradeAllOrNothing() throws Exception {
		UserService testUserService = new TestUserService(users.get(3).getId());
		// 수동 DI
		testUserService.setUserDao(this.userDao);
//		testUserService.setDataSource(this.dataSource);
		testUserService.setTransactionManager(transactionManager);
		testUserService.setMailSender(mailSender);
		
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
	
	/*
	 * sendUpgradeEmail 테스트
	 * 
	 * javax.mail.MessagingException: Could not connect to SMTP host
	 *  - 메일서버가 준비되지 않았으므로
	 * 
	 * 
	 * 방법?
	 *  1. 메일서버 준비?
	 *  	- 운영중인 메일서버 부담 / 실제 메일이 발송됨
	 *  
	 *  2. 테스트용 메일서버 준비?
	 *  	- 메일발송기능은 사용자레벨 업그레이드의 보조기능 불과함
	 *  	- DB에 잘 반영되었는가하는 문제보다 덜 중요
	 *  	- 메일이 잘 도착했는지만 테스트하기는 엄밀하게 불가능
	 * 
	 *  3. 테스트용 JavaMail 사용
	 *  	- 구조
	 *  		- UserService --> JavaMail --> 테스트용 메일서버
	 *  		- UserService --> 테스트용 JavaMail
	 *  	- JavaMail은 자바의 표준기술로 안정적 
	 *  		- JavaMail API를 통해 요청이 가능할 경우 테스트할 때마다 JavaMail을 수행하여 외부 서버를 사용할 필요 없음 
	 * 
	 * 
	 * 5.4.3, 테스트위한 서비스 추상
	 */


}
