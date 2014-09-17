package me.dec7.user.service;

import java.util.List;

import me.dec7.user.dao.UserDao;
import me.dec7.user.domain.Level;
import me.dec7.user.domain.User;

import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;


/*
 * UserService interface의 구현클래스인 UserServiceImple은
 * 기존 UserService 클래스의 내용을 대부분 유지
 * 단, 트랜잭션과 관련된 코드는 독립시키기로 했으니 모두 제거 가능
 */

public class UserServiceImpl implements UserService {
	
	public static final int MIN_LOGCOUNT_FOR_SILVER = 50;
	public static final int MIN_RECOMMEND_FOR_GOLD = 30;
	
	protected UserDao userDao;
//	protected DataSource dataSource;
	private MailSender mailSender;
		
	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}
		
	public void setMailSender(MailSender mailSender) {
		this.mailSender = mailSender;
	}

	// 트랜잭션에 사용한 dataSoruce; setter method injection
	/*
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}
	*/
	
	/*
	protected PlatformTransactionManager transactionManager;
	public void setTransactionManager(PlatformTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}
	*/

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
		SimpleMailMessage mailMessage = new SimpleMailMessage();
		
		mailMessage.setTo(user.getEmail());
		mailMessage.setFrom("useradmin@ksug.org");
		mailMessage.setSubject("Upgrade 안내");
		mailMessage.setText("사용자님의 등급이 " + user.getLevel().name() + "로 업그레이드되었습니다.");
		
		mailSender.send(mailMessage);
	}

	public void upgradeLevels() {
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
