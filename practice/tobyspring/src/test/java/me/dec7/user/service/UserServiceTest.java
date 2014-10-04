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
		
		DefaultTransactionDefinition txDefinition = new DefaultTransactionDefinition();
		TransactionStatus txStatus = transactionManager.getTransaction(txDefinition);
		
		userService.add(users.get(0));
		userService.add(users.get(1));
		assertThat(userDao.getCount(), is(2));
		
		transactionManager.rollback(txStatus);
		assertThat(userDao.getCount(), is(0));
		
	}
	
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
	@Rollback(false)
	public void transactionSync5() {
		userService.deleteAll();
		userService.add(users.get(0));
		userService.add(users.get(1));
	}
	
	@Test
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
