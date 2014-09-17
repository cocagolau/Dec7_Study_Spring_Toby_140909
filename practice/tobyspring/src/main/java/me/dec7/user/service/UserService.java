package me.dec7.user.service;

import java.util.List;

import javax.sql.DataSource;

import me.dec7.user.dao.UserDao;
import me.dec7.user.domain.Level;
import me.dec7.user.domain.User;

import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.transaction.PlatformTransactionManager;


/*
 * DAO에 transaction을 적용하며 spring이 비슷한 여러 종류의 기술을 추상화 / 사용하는 방법
 * 
 * 사용자 레벨 관리 기능
 *  - UserDao는 User 오브젝트의 CRUD작업만 가능	
 *  - 어떤 비지니스 로직은 담겨 있지 않음
 *  - 사용자 관리 비지니스로직은 UserDaoJdbc에 담기 적합하지 않음 
 *  	- dao는 데이터의 조작에 대한 관심을 가진 객체
 *  - UserService는 UserDao 구현에 영향받지 않도록 해야함
 *  	- 데이터 액세스 로직이 바뀌어도 로직은 바뀌지 않아야함
 */

/*
 * 트랜잭션
 *  - SQL이 여러개 실행될 때 
 *    첫번째는 성공적 실행 두번째 성공전 실패시 작업이 중단되는 경우
 *    
 *    앞에서 처리한 작업을 취소시켜야함
 *     --> 트랜잭션 롤백
 *     
 *    여러개의 sql을 하나의 트랜잭션으로 처리하는 경우, 모두 성공했다고 DB에 알려주는 것
 *     --> 트랜잭션 커밋
 * 
 * 반드시
 *  - 시작하는 지점과 끝나는 지점을 가져야 함
 *  - 시작하는 방법은 한가지, 끝나는 방법은 두가지
 *  	- 롤백 / 무효화
 *  	- 커밋 / 작업 확정
 *  - 어플리케이션 내에서 트랜잭션이 시작되고 끝나는 위치를 트랜잭션 경계라 함 
 *  
 */
public class UserService {
	
	public static final int MIN_LOGCOUNT_FOR_SILVER = 50;
	public static final int MIN_RECOMMEND_FOR_GOLD = 30;
	
	protected UserDao userDao;
	protected DataSource dataSource;
	private MailSender mailSender;
		
	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}
		
	public void setMailSender(MailSender mailSender) {
		this.mailSender = mailSender;
	}

	// 트랜잭션에 사용한 dataSoruce; setter method injection
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}
	
	protected PlatformTransactionManager transactionManager;
	public void setTransactionManager(PlatformTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

	/*
	 * TestUserService에서 접근할 수있도록 접근권한을 protected로 변경
	 *
	private void upgradeLevel(User user) {
	*/
	protected void upgradeLevel(User user) {
		user.upgradeLevel();
		userDao.update(user);
		sendUpgradeEmail(user);
	}
	
	private void sendUpgradeEmail(User user) {
		/*
		 * JavaMail API 사용
		 * 
		Properties props = new Properties();
		props.put("mail.smtp.host", "mail.ksug.org");
		
		
		Session s = Session.getInstance(props, null);
		
		MimeMessage message = new MimeMessage(s);
		try {
			message.setFrom(new InternetAddress("useradmin@ksug.org"));
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(user.getEmail()));
			message.setSubject("Upgrade 안내");
			message.setText("사용자님의 등급이 " + user.getLevel().name() + "로 업그레이드되었습니다.");
			
			Transport.send(message);
		} catch (AddressException e) {
			throw new RuntimeException(e);
		} catch (MessagingException e) {
			throw new RuntimeException(e);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	     */
		
		/*
		 *  javax.mail.Session 사용법
		 *  
		 *  JavaMail에서는 Session 오브젝트를 만들어야 메일 메시지를 생성할 수 있음
		 *   - Session은 interface가 아닌 class
		 *   - 생성자 모두 private
		 *   - 상속 불가 final 클래스
		 *   - 결국,  JavaMail의 구현을 테스트용으로 바꾸기는 불가능
		 *   	- 확장, 지원 불가능하도록 만들어진 자바 표준 API중 하나
		 *   
		 *  해결책
		 *   - JavaMail 대신 테스트용 JavaMail로 대체
		 *   - 서비스 추상화 사용
		 */
		
		// MailSender 구현 클래스의 오브젝트 생성
		/*
		 *  DI적용
		 *  JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
		 *  mailSender.setHost("mail.server.com");
		 */
		
		/*
		 *  MailMessage interface의 구현 클래스 오브젝트를 만들어 메일 내용 작성
		 *  
		 *  내부적으로 JavaMail API을 이용해 메일 전송
		 */
		SimpleMailMessage mailMessage = new SimpleMailMessage();
		
		mailMessage.setTo(user.getEmail());
		mailMessage.setFrom("useradmin@ksug.org");
		mailMessage.setSubject("Upgrade 안내");
		mailMessage.setText("사용자님의 등급이 " + user.getLevel().name() + "로 업그레이드되었습니다.");
		
		mailSender.send(mailMessage);
	}

	public void upgradeLevels() throws Exception {
		
		List<User> users = userDao.getAll();
		
		for (User user : users) {
			// upgrade 가능시
			if (canUpgradeLevel(user)) {
				// upgrade 해라
				upgradeLevel(user);
			}
		}
	}

	/*
	 * TestUserService에서 접근할 수있도록 접근권한을 protected로 변경
	 *
	private boolean canUpgradeLevel(User user) {
	*/
	protected boolean canUpgradeLevel(User user) {
		Level currentLevel = user.getLevel();
		
		// level별로 구분해 조건을 판단
		switch (currentLevel) {
			case BASIC	: return (user.getLogin() >= MIN_LOGCOUNT_FOR_SILVER);
			case SILVER	: return (user.getRecommend() >= MIN_RECOMMEND_FOR_GOLD);
			case GOLD	: return false;
			default		: throw new IllegalArgumentException("Unknown Level: " + currentLevel);
		}
	}

	public void add(User user) {
		if (user.getLevel() == null) {
			user.setLevel(Level.BASIC);
		}
		
		userDao.add(user);
	}

}
