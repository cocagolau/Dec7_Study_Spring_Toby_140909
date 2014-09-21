package me.dec7.user.service;

import static org.hamcrest.CoreMatchers.*;
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
	
//	@Autowired
//	UserServiceImpl userServiceImpl;
	
	@Autowired
	/*
	 * 같은 타입의 bean이 두개 존재하기 때문에 field 이름을 기준으로 bean이 결정됨
	 * 자동 프록시 생성기에 의해 transaction 부가기능이 testUserSerivice bean에 적용되었는지 확인하는 것이 목적
	 */
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
	
	/*
	 * 자동 프록시 생성기를 통해 testUserService가 알맞는 bean에 적용되는지 확인
	 */
	@Test
	public void advisorAutoProxyCreator() {
		assertThat(testUserService, is(instanceOf(java.lang.reflect.Proxy.class)));
	}
	
	@Test(expected=TestUserServiceException.class)
	/*
	 *  @DirtiesContext
	 *  spring context의 bean 설정ㄴ을 변경하지 않으므로 제거 
	 *  모든 test를 위한 DI 작업은 설정파일을 통해 서버에서 진행되므로 테스트 코드는 간단해짐
	 */

	public void upgradeAllOrNothing() throws Exception {
		/*
		TestUserService testUserService = new TestUserService(users.get(3).getId());
		
		// 수동 DI	
		testUserService.setUserDao(this.userDao);
		testUserService.setMailSender(mailSender);

		ProxyFactoryBean txProxyFactoryBean = context.getBean("&userService", ProxyFactoryBean.class);
		txProxyFactoryBean.setTarget(testUserService);		 
		 
		UserService txUserService = (UserService) txProxyFactoryBean.getObject();
		*/

		userDao.deleteAll();
		
		for (User user : users) {
			userDao.add(user);
		}
		
		this.testUserService.upgradeLevels();
		// txUserService.upgradeLevels();
	
		checkLevelUpgraded(users.get(1), false);
	}
	
	/*
	 * 두가지 문제점 
	 *  1. TestUserService는 UserServiceTest 클래스 내부에 정의된 static 클래스?
	 *  2. pointcut이 taansactionAdvisor를 정요해주는 대상 클래스이름의 패턴과 다름
	 */
	static class TestUserServiceImpl extends UserServiceImpl {
		private String id = "dec2";
		
		protected void upgradeLevel(User user) {
			if (user.getId().equals(this.id)) {
				throw new TestUserServiceException();
			}
			
			super.upgradeLevel(user);
		}
	}
	
	/*
	 * 후처리 bean 메커니즘 이용
	 * 
	 * 확인사항
	 *  1. transaction이 필요한 bean에 부가기능이 적용되었는가?
	 *  	- upgradeAllOrNothing()
	 *  	- 정상적으로 commit되는 경우 transaciton 적용여부 확인 어려우므로
	 *  	  예외상황에서 transaction이 rollback되게 함으로써 적용여부 테스트
	 *  2. 아무 bean에 transaction 부가기능이 적용되는 것 아닌지 호가인
	 *  	- advisorAutoProxyCreator()
	 *  	- proxy 자동생성기가 advisor bean에 연결해둔 pointcut의 class filter를 이용해
	 *  	  원하는 bean에만 proxy를 생성했는지 확인
	 *  	- pointcut bean의 class이름 패턴을 변경해 testUserService bean에 transaction 적용 안되도록
	 * 
	 */

}
