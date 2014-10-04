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
import org.springframework.context.ApplicationContext;
import org.springframework.dao.TransientDataAccessResourceException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/test-applicationContext.xml")
/*
 * @TransactionConfiguration(defaultRollback=false)
 *  - 롤백 여부에 대한 기본 설정,
 *  	- @Rollback 어노테이션은 메소드 단위 기록 가능하므로
 *  - 트랜잭션 매니져 빈을 지정하는데 사용 가능
 *  	- 관례는 transactionManager
 */

/*
 * 효과적인 DB테스트
 *  - 테스트 내에서 트랜잭션 제어 4가지 어노테이션 잘 활용시 DB가 사용되는 통합 테스트를 만들때 매우 편리
 *  - 일반적으로 단위테스트와 통합테스트 클래스를 구분해 만드는 게 좋음
 *  	- 단위테스트: 의존,협력 오브젝트 사용 않고 고립된 상태에서 테스트 진행
 *  	- 통합테스틔: DB 같은 외부 리소스, 여러 계층 클래스 참여
 *  - 통합세스트를 별도의 클래스로 만들면 기본적으로 클래스 레벨에 @Transactional을 부여 
 *  	- 그리고 DB 사용시 가능한 롤백 테스트로 만드는 것이 좋음
 * 
 */
public class UserServiceTest {
	
	// factory bean을 가져오기 위해 context 필요
	@Autowired
	ApplicationContext context;
	
	@Autowired
	UserService userService;
	
	@Autowired
	UserService testUserService;
	
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
	public void transactionSync1() {
		
		// 트랜잭션을 롤백시 돌아갈 초기 상태를 만들기 위해 시작 전 초기화
		userService.deleteAll();
		assertThat(userDao.getCount(), is(0));
		
		/*
		 * 3개의 트랜잭션을 하나로 통합하기
		 *  - 세 개 메소드의 트랜잭션 전파 속성이 REQUIRED이므로 호출 전 트랜잭션이 시작되게만 한다면 가능
		 *  - 하지만 메소드를 추가하지 않고 테스트 코드만으로 통합
		 *  	- UserService 메소드를 호출하기 전 트랜잭션을 미리 시작해주면 됨
		 */
		// 트랜잭션 정의 기본값 사용
		DefaultTransactionDefinition txDefinition = new DefaultTransactionDefinition();
		// 트랜잭션 참여를 검증하기 위한 설정
		//txDefinition.setReadOnly(true);
		
		/*
		 *  트랜잭션 매니저에게 트랜잭션 요청
		 *  기존에 시작된 트랜잭션 없으니 새로운 트랜잭션 시작 후 트랜잭션 정보를 돌려줌
		 *  동시에 만들어진 트랜잭션도 다른 곳에서 사용할 수 있도록 동기화 
		 */
		TransactionStatus txStatus = transactionManager.getTransaction(txDefinition);
		
		
		/*
		 * transactionSync 가 실행되면서 3개의 트랜잭션 생성
		 * 
		 * 각 메소드는 모두 독립적인 트랜잭션 안에서 실행
		 * 각 메소드 실행시 진행중인 트랜잭션 없고, 전파 속성은 REQUIRED이니, 새로운 트랜잭션 시작
		 * 그리고 메소드 종료시 커밋됨
		 * 
		 */
		
		// 앞서 만들어진 트랜잭션에 참여
		// 읽기 전용으로 에러 발생지점
		// userService.deleteAll();
		
		userService.add(users.get(0));
		userService.add(users.get(1));
		assertThat(userDao.getCount(), is(2));
		
		
		// 앞에서 시작한 트랜잭션을 커밋
		// transactionManager.commit(txStatus);
		
		// 강제로 롤백, 트랜잭선 시작전 상태임 확인
		transactionManager.rollback(txStatus);
		assertThat(userDao.getCount(), is(0));
		
	}
	
	/* rollback test
	 * 
	 *  - DB 작업이 포함된 테스트가 수행돼도 DB에 영향을 주지 않으므로 장점이 많음
	 *  - 여러 개발자가 하나의 공용 테스트용 DB를 사용할 수 있도록 해줌
	 *  - 절절한 격리수준만 보장해주면 동시에 여러 개의 테스트가 진행돼도 상관 없음
	 */
	@Test
	public void transactionSync2() {

		DefaultTransactionDefinition txDefinition = new DefaultTransactionDefinition();
		TransactionStatus txStatus = transactionManager.getTransaction(txDefinition);
		
		try {
			// 테스트 안의 모든 작업을 하나의 트랜잭션으로 통합
			userService.deleteAll();
			userService.add(users.get(0));
			userService.add(users.get(1));
			
		} finally {
			/*
			 *  테스트 결과가 어떻든 상관없이
			 *  테스트 종료시 무조건 롤백
			 */
			transactionManager.rollback(txStatus);
			
		}
		
	}
	
	@Test
	/*
	 * 테스트에도 @Transactional을 적용하면 트랜잭션 경계가 자동 설정됨
	 *  - 테스트 내에서 진행하는 모든 트랜잭션 관련 작업을 하나로 묶어줄 수 있음
	 *  - 테스트의 @Transactional은 AOP를 위한 것 아니고, 단지 context test framework에 의해 트랜잭션 부여 용도
	 *  	- 하지만 동작 방식은 동일 
	 *  
	 * 테스트 클래스 레벨도 부여 가능
	 *  - 테스트 메소드 모두 트랜잭션 적용
	 *  
	 * 테스트에 적용된 @Transactional은 테스트 종료시 자동 롤백
	 *  - 어플리케이션 클래스 적용시와 다른점
	 */
	@Transactional
	public void transactionSync3() {
		userService.deleteAll();
		userService.add(users.get(0));
		userService.add(users.get(1));
	}
	
	// test메소드에서 @Transactional이 잘 동작하는지 테스트
	@Test(expected=TransientDataAccessResourceException.class)
	@Transactional(readOnly=true)
	public void transactionSync4() {
		userService.deleteAll();
		userService.add(users.get(0));
		userService.add(users.get(1));
	}
	
	@Test
	@Transactional
	/*
	 *  자동 롤백을 원하지 않는 경우
	 *  메소드 레벨만 적용 가능
	 */
	@Rollback(false)
	public void transactionSync5() {
		userService.deleteAll();
		userService.add(users.get(0));
		userService.add(users.get(1));
	}
	
	@Test
	/*
	 *  @NotTransactional spring3.0에서 제거됨
	 *  트랜잭션 설정 무시하고자 할때
	 */
	@Transactional(propagation=Propagation.NEVER)
	public void transactionSync6() {
		userService.deleteAll();
		userService.add(users.get(0));
		userService.add(users.get(1));
	}
	
	@Test
	public void bean() {
		assertThat(this.userService, is(notNullValue()));
	}
	
	
	@Test
	@DirtiesContext
	public void upgradeLevels() throws Exception {

		UserServiceImpl userServiceImpl = new UserServiceImpl();

		UserDao mockUserDao = mock(UserDao.class);
		
		when(mockUserDao.getAll()).thenReturn(this.users);
		userServiceImpl.setUserDao(mockUserDao);
		
		MailSender mockMailSender = mock(MailSender.class);
		userServiceImpl.setMailSender(mockMailSender);
		
		// 테스트 대상 실행
		userServiceImpl.upgradeLevels();
		
		verify(mockUserDao, times(2)).update(any(User.class));
		verify(mockUserDao).update(users.get(1));
		
		assertThat(users.get(1).getLevel(), is(Level.SILVER));
		
		verify(mockUserDao).update(users.get(3));
		assertThat(users.get(3).getLevel(), is(Level.GOLD));
		
		ArgumentCaptor<SimpleMailMessage> mailMessageArg = ArgumentCaptor.forClass(SimpleMailMessage.class);
		// 파라미터를 정밀하게 검사하기 위해 캡쳐할 수도 있음
		verify(mockMailSender, times(2)).send(mailMessageArg.capture());
		
		List<SimpleMailMessage> mailMessages = mailMessageArg.getAllValues();
		assertThat(mailMessages.get(0).getTo()[0], is(users.get(1).getEmail()));
		assertThat(mailMessages.get(1).getTo()[0], is(users.get(3).getEmail()));
		
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

	private void checkLevelUpgraded(User user, boolean upgraded) {
		User userUpdate = userDao.get(user.getId());
		
		if (upgraded) {
			assertThat(userUpdate.getLevel(), is(user.getLevel().nextLevel()));
			
		} else {
			assertThat(userUpdate.getLevel(), is(user.getLevel()));
			
		}
	}
	

	@Test(expected=TestUserServiceException.class)
	public void upgradeAllOrNothing() throws Exception {

		userDao.deleteAll();
		
		for (User user : users) {
			userDao.add(user);
		}
		
		this.testUserService.upgradeLevels();
	
		checkLevelUpgraded(users.get(1), false);
	}
	
	@Test(expected=TransientDataAccessResourceException.class)
//	@Test
	public void readOnlyTransactionAttribute() {
		// 예외 발생해야함
		testUserService.getAll();
	}
	
	
	
	static class TestUserServiceImpl extends UserServiceImpl {
		private String id = "dec2";
		
		protected void upgradeLevel(User user) {
			if (user.getId().equals(this.id)) {
				throw new TestUserServiceException();
			}
			
			super.upgradeLevel(user);
		}
		
		
		public List<User> getAll() {
			for (User user : super.getAll()) {
				// 강제로 쓰기 시도, 읽기전용 속성으로 인한 예외 발생해야함
				super.update(user);
			}
			
			return null;
		}
	}
	
}
