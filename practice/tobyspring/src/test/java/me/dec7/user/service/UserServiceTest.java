package me.dec7.user.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import me.dec7.user.dao.UserDao;
import me.dec7.user.domain.Level;
import me.dec7.user.domain.User;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.PlatformTransactionManager;


/*
 * 단위 테스트
 *  - 항성 먼저 고려
 *  - 하나의 목적, 성격이 같은 긴밀한 클래스, 외부의 의존관계 모두 차단
 *  - 빠른 속도, 외부 영향 없음
 * 
 * 
 * 통합 테스트
 *  - 외부 리소스를 사용해야만 하는 경우
 *  - 단위 테스트로 만들기 어려운 코드
 *  	- dao / 보통 db까지 연동하는 테스트가 효과적
 *  	- dao는 db라는 외부 리소스 사용하므로 통합 테스트로 분류
 *  	- dao는 mock을 사용하여 대체하기도 함
 *  - 여러개의 단위가 의존관계를 가지고 동작시
 *  	- 그래도 단위 테스트를 충분히 거치면 통합 테스트의 부담이 줄어듬
 *  - spring text context framework를 이용하는 테스트는 통합 테스트
 */

/*
 * 6.2, 고립된 단위 테스트
 *  - 가능한 작은 단위로 쪼개는 것이 좋은 테스트 방법
 *  	- 원인을 찾기 쉬움
 *  	- 의도, 내용이 분명해지고 만들기 쉬워짐
 * 
 * UserService는
 *   - UserDao, TransactionManager, MailSender와 의존관계 있음
 *   - 더 큰 문제는 위 세가지 오브젝트도 다른 오브젝트의 의존관계를 가지고 있음
 *   --> 따라서 이 경우 테스트를 준비하기 힘들고
 *       테스트 환경이 조금이라도 달라지면 동일한 테스트 결과를 내지 못할 수도 있음
 *       수행 속도는 느리고 그에 따라 테스트를 작성하고 실행 빈도가 점차 떨어질 것.. 분명함
 *       반대로 UserService는 문제가 없음에도 다른 문제로 시간을 보낼 수동
 *       DB와 관련된 테스트는 작성하기도 힘듦
 * 
 * 테스트 대상 오브젝트 고립시키기
 *  - 테스트 환경이 다른 환경에 종속받지 않도록 고립시켜야함..
 *  - MailSender처럼 테스트 대역을 사용하는 것
 *  	DummyMailSender, MockMailSender
 *  - me.dec7.user.service.UserServiceImpl 참고
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/test-applicationContext.xml")
public class UserServiceTest {
	
	@Autowired
	UserService userService;
	
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
		users = Arrays.asList(
				new User("dec1", "동규1", "pw1", "dec1@gmail.com", Level.BASIC, UserServiceImpl.MIN_LOGCOUNT_FOR_SILVER-1, 0),
				new User("dec2", "동규2", "pw2", "dec2@gmail.com", Level.BASIC, UserServiceImpl.MIN_LOGCOUNT_FOR_SILVER, 0),
				new User("dec3", "동규3", "pw3", "dec3@gmail.com",Level.SILVER, 60, UserServiceImpl.MIN_RECOMMEND_FOR_GOLD-1),
				new User("dec4", "동규4", "pw4", "dec4@gmail.com",Level.SILVER, 60, UserServiceImpl.MIN_RECOMMEND_FOR_GOLD),
				new User("dec5", "동규5", "pw5", "dec5@gmail.com",Level.GOLD, 100, 100)
			);
	}
	
	@Test
	public void bean() {
		assertThat(this.userService, is(notNullValue()));
	}
	
	/*
	 * 테스트 구성
	 *  1. 테스트 실행 중
	 *     UserDao를 통해 가져올 테스트 정보를 DB에 저장
	 *     최종 의존대상이 DB, 직접 정보를 저장해야함
	 *  2. 메일 발송여부 확인 위해 MailSender Mock 오브젝트를 DI
	 *  3. 실제 테스트 대상 실행
	 *  4. 결과를 DB에서 조회
	 *  5. Mock 오브젝트를 통해 메일 발송이 있었는지 확인
	 */
	/*
	 *  - DB를 접근하지 않음으로 테스트 수행성능 향상됨
	 */
	@Test
	@DirtiesContext
	public void upgradeLevels() throws Exception {
		
		/*
		 *  DB 테스트 데이터 준비
		 *  레벨 업그레이드 후보가 될 사용자 목록을 받아옴
		 *  
		 *  따라서 테스트용 UserDao에서 DB에서 읽어온 것처럼 미리 준비된 사용자 목록을 제공해야 함
		 *  userDao.update(user)는 리턴값이 없어 테스트용 UserDao가 할 일이 없음 (빈 메소드)
		 *  하지만 update() 메소드 사용은 upgradeLevels()의 핵심 로직인
		 *   - 전체 사용자 중에서 업그레이드 대상자는 레벨을 변경해준다'에서 '변경'을 담당
		 *   - 그러므로 getAll()에 대해서 stub으로 update()에 목 오브젝트로 동작하는 UserDao타입의 테스트 대역 필요
		 */
		/*
		userDao.deleteAll();
		for (User user : users) {
			userDao.add(user);
		}
		*/
		
		/*
		 *  고립된 테스트에서는 테스트 대상 오브젝트를 직접 생성하면 됨
		 *  
		 *  테스트 대역 오브젝트를 이용해 완전히 고립된 테스트를 사용
		 *   - @Autowired를 사용시 UserService 타입의 bean이었고 DI를 통해 많은 의존 오브젝트가 존재
		 *   - 하지만 고립할 것이므로 spring에서 bean을 가져올 필요가 없음
		 */
		UserServiceImpl userServiceImpl = new UserServiceImpl();
		
		// Mock 오브젝트로 만든 UserDao를 직접 DI
		// MockUserDao mockUserDao = new MockUserDao(this.users);
		
		/*
		 * Mockito를 통해 만들어진 Mock오브젝트는 메소드 호출과 관련된 모든 내용을 자동 저장
		 * 이를 간단한 메소드로 검증할 수 있도록 함
		 * 
		 * UserDao interface를 구현한 클래스를 만들 필요 없음
		 * 반환 값을 생성자를 통해 넣어줬다가 메소드 호출 시 리턴하도록 코드를 만들 필요 없음
		 * 
		 * 사용 방법
		 *  1. interface를 사용해 Mock 오브젝트 생성
		 *  2. Mock 오브젝트가 반환할 값이 있으면 이를 지정
		 *     호출시 예외를 강제로 던지게 할 수도 있음
		 *  3. 테스트 대상 오브젝트에 DI해 Mock 오브젝트가 테스트 중 사용되도록 함
		 *  4. 테스트 대상 오브젝트를 사용 후 Mock 오브젝트의 특정 메소드가 호출되었는지
		 *     어떤 값을 가지고 몇 번 호출됐는지 검증
		 * 
		 */
		
		UserDao mockUserDao = mock(UserDao.class);
		// getAll()을 호출할 때 this.users를 반환
		when(mockUserDao.getAll()).thenReturn(this.users);
		userServiceImpl.setUserDao(mockUserDao);
		
		// 메일 발송확인 위해 Mock오브젝트 DI
		// 메일 발송 결과를 테스트할 수 있도록 목 오브젝트를 만들어 userService의 의존 오브젝트로 주입
		// MockMailSender mockMailSender = new MockMailSender();
		MailSender mockMailSender = mock(MailSender.class);
		// userService.setMailSender(mockMailSender);
		userServiceImpl.setMailSender(mockMailSender);
		
		// 테스트 대상 실행
		userServiceImpl.upgradeLevels();
		
		
		/*
		 * 테스트에서 확인하고자 하는 사항
		 * 
		 *  1. UserDao의 update()가 두번 호출
		 *  2. 그때 파라미터는 getAll() 에서 넘겨준 User 목록의 두번째/네번째
		 * 
		 */
		// 테스트를 진행하는 동안 mockUserDao의 update() 메소드가 두번 호출되었는 확인하고 싶다면..
		verify(mockUserDao, times(2)).update(any(User.class));
		// verify(mockUserDao, times(2)).update(any(User.class));
		
		// update() 호출시 users.get(1)이 파라미터로 호출된 적이 있는지 검증
		verify(mockUserDao).update(users.get(1));
		
		// 반환값을 직접 비교
		assertThat(users.get(1).getLevel(), is(Level.SILVER));
		
		verify(mockUserDao).update(users.get(3));
		assertThat(users.get(3).getLevel(), is(Level.GOLD));
		
		/*
		List<User> updated = mockUserDao.getUpdated();
		assertThat(updated.size(), is(2));
		checkUserAndLevel(updated.get(0), "dec2", Level.SILVER);
		checkUserAndLevel(updated.get(1), "dec4", Level.GOLD);
		*/
		
		/*
		 * MailSender는 ArgumentCaptor 사용
		 *  - 실제 MailSender Mock 오브젝트에서 전달된 파라미터를 가져와 내용을 검증하는 방법
		 *  - 파라미터를 직접 비교보다, 내부 정보를 확인해야하는 경우에 유용
		 */
		ArgumentCaptor<SimpleMailMessage> mailMessageArg = ArgumentCaptor.forClass(SimpleMailMessage.class);
		// 파라미터를 정밀하게 검사하기 위해 캡쳐할 수도 있음
		verify(mockMailSender, times(2)).send(mailMessageArg.capture());
		
		List<SimpleMailMessage> mailMessages = mailMessageArg.getAllValues();
		assertThat(mailMessages.get(0).getTo()[0], is(users.get(1).getEmail()));
		assertThat(mailMessages.get(1).getTo()[0], is(users.get(3).getEmail()));
		
		// DB에 저장된 결과 확인
		/*
		checkLevelUpgraded(users.get(0), false);
		checkLevelUpgraded(users.get(1), true);
		checkLevelUpgraded(users.get(2), false);
		checkLevelUpgraded(users.get(3), true);
		checkLevelUpgraded(users.get(4), false);
		*/
		
		// mock 오브젝트에 저장된 메일 수신자 목록을 가져와 업그레이드 대상과 일치하는지 확인
		/*
		List<String> request = mockMailSender.getRequests();
		assertThat(request.size(), is(2));
		assertThat(request.get(0), is(users.get(1).getEmail()));
		assertThat(request.get(1), is(users.get(3).getEmail()));
		*/
	}

	private void checkUserAndLevel(User updated, String expectedId, Level expectedLevel) {
		assertThat(updated.getId(), is(expectedId));
		assertThat(updated.getLevel(), is(expectedLevel));		
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
