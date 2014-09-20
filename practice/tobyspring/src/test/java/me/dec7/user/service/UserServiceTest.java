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
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
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

/*
 * add()
 *  - @Autowired로 가져온 userService bean 사용
 *  - TxProxyFactoryBean이 생성하는 다이나믹 프로시를 통해 UserService 기능을 사용할 것임
 * 
 * upgradeLevels(), mockUpgradeLevels()
 *  - mock 오브젝트를 이요해 비지니스 로직에 대한 단위 테스트
 *  - 트랜잭션과 무관
 *  
 * upgradeAllOrNothing()
 *  - 수동 DI를 통해 직접 다이나믹 프록시를 만들어사용해 Factory Bean이 적용 안됨
 *  - 기존 메소드 테스트는 예외 발생시 트랜잭션이 롤백됨을 확인위해 
 *    비지니스 로직 코드를 수정한 TestUserService 오브젝트를 Target 오브젝트로 대신 사용
 *  - 설정엔 정상적인 UserServiceImpl 오브젝트로 지정되었지만
 *    테스트엔 TestUserService 오브젝트가 동작하도록 해야함
 *  - 문제점
 *  	- TransactionHandler와 Dynamic Proxy 오브젝트를 직접만들 때는
 *  	  Target 오브젝트를 바꾸기 쉬웟지만, 
 *  	  지금은 spring bean에서 생성되는 Proxy 오브젝트에 대해 테스트 해야하므로 어려움
 *  	- target 오브젝트에 대한 레퍼런스는 TransactionHandler 오브젝트가 가지고 있지만
 *  	  TrasnactionHandler는 TxProxyFactoryBean 내부에서 만들어져 Dynamic Proxy 생성에 사용될 뿐
 *  	  별도 참조할 수 없다는 것
 *  - 해결책
 *  	- TxProxyFactoryBean의 트랜잭션을 지원하는 프록시를 바르게 만들어 주는지 확인하는게 목적
 *  	  빈으로 등록된 팩토리빈을 가져와 직적 프록시를 만들어 보기
 *  	- TxProxyFactoryBean을 가져와 target 프로퍼티를 재성성 후 
 *  	  다시 프록시 오브젝트를 생성하도록 요청
 *  	  이 경우, 컨텍스트의 설정을 변경하므로 @DirtiesContext를 등록 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/test-applicationContext.xml")
public class UserServiceTest {
	
	// factory bean을 가져오기 위해 context 필요
	@Autowired
	ApplicationContext context;
	
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
	// Context 무효화 어노테이션
	@DirtiesContext
	public void upgradeAllOrNothing() throws Exception {
		TestUserService testUserService = new TestUserService(users.get(3).getId());
		
		// 수동 DI	
		 testUserService.setUserDao(this.userDao);
		 testUserService.setMailSender(mailSender);
		 // testUserService.setTransactionManager(transactionManager);
		 
		 // 트랜잭션 기능을 분리한 UserServiceTx는 예외 발생용으로 수정할 필요가 없음
		 /*
		 UserServiceTx txUserService = new UserServiceTx();
		 txUserService.setTransactionManager(transactionManager);
		 txUserService.setUserService(testUserService);
		 */
		 // Dynamic Proxy 사용
		 /*
		 TransactionHandler txHandler = new TransactionHandler();
		 txHandler.setTarget(testUserService);
		 txHandler.setTransactionManager(transactionManager);
		 txHandler.setPattern("upgradeLevels");
		 
		 UserService txUserService = (UserService) Proxy.newProxyInstance(
				 	getClass().getClassLoader(),
				 	new Class[] { UserService.class },
				 	txHandler
				 );
		*/
		 /*
		  * Factory Bean 자체를 가져와야 하므로
		  * bean 이름에 &을 꼭 넣어야함
		  */
		 // TxProxyFactoryBean txProxyFactoryBean = context.getBean("&userService", TxProxyFactoryBean.class);
		 ProxyFactoryBean txProxyFactoryBean = context.getBean("&userService", ProxyFactoryBean.class);
		 txProxyFactoryBean.setTarget(testUserService);		 
		 
		 // 변경된 타깃 설정을 사용해 다이나믹 프록시 오브젝트를 다시 생성
		 UserService txUserService = (UserService) txProxyFactoryBean.getObject();
		 
		 
		 
		/*
		 * 문제점
		 *  - Proxy클래스의 newProxyInstance()라는 static method를 통해서만 만들 수 있음
		 *  - DI대상인 다이나믹 프록시 오브젝트는 bean으로 등록할 수 없음
		 *  	- 기본적으로 class이름, property로 정의
		 *  	- spring은 reflection을 사용해 class이름으로 오브젝트 생성 --> bean 등록
		 *  		- Date now = (Date) Class.forName("java.util.Data").newInstance();
		 *  	- Dynamic Proxy는 위 방식으로 오브젝트가 생성되지 않음
		 *  		- 사실 Class자체도 내부적으로 다이나믹하게 새로 정의하므로
		 *			  Dynamic Proxy 오브젝트의 클래스가 뭔지도 모름
		 *			- 따라서, proxy 오브젝트의 클래스 정보를 미리 알아내 스프링 빈에 정의할 수 없음
		 *
		 * 
		 * 팩토리 빈
		 *  - 다양한 빈 생성방법 중 스프링에서 제공하는 한가지
		 *  - Spring을 대신해 오브젝트의 생성 로직을 담당하도록 만들어진 특별한 빈
		 *  - Factory bean을 만드는 방법 중 가장 간단한 것은
		 *  	FactoryBean interface를 구현하는 것

			public interface FactoryBean<T> {
				// bean object를 생성ㅊ해 돌려줌
				T getObject() throws Exception;
				
				// 생성되는 오브젝트 타입을 알려줌
				Class<? extends T> getObjectType();
				
				// getObject()가 돌려주는 오브젝트가 항상 같은 싱글톤인지 알려줌
				boolean isSingleton();
			}

		 * 
		 */
				 
		
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
