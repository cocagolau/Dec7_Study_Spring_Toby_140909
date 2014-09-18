package me.dec7.user.service;

import java.util.List;

import me.dec7.user.dao.UserDao;
import me.dec7.user.domain.Level;
import me.dec7.user.domain.User;

import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;

/*
 *  6.2.., 테스트를 위한 UserServiceImpl 고립
 *   - PlatformTransactionManager는 UserServiceTx로 분리
 *     UserServiceImpl은 의존하지 않음
 *     
 *   고립된 테스트가 가능하도록 UserService를 재구성하면
 *    - MockUserDao, MockMailSender의 두개의 Mock 오브젝트에만 의존시 완벽하게 고립됨
 * 
 * UserServiceImpl의 upgradeLevels() 메소드는 return값이 void형.
 *  - 메소드를 받고 그 결과를 검증은 불가능
 *  - dao를 통해 필요한 정보를 가져오고 작업 후 DB에 재반영. 그 결과를 확인위해서 db를 직접 확인해야함.
 *  - 그래서 기존엔 DB결과를 재조회하는 방법으로 테스트함
 *  --> 이 경우 테스트대상인 UserServiceImpl과 그 협렵 오브젝트인 UserDao에게 어떤 요청을 했는지 확인하는 작업 필요
 *  	테스트 중 DB에 결과가 반영되지 않더라도
 *  	UserDao의 update() 메소드를 호출하는 것을 확인할 수만 있다면 결과가 반영될 것이라고 결론 내릴 수 있기 때문
 *  --> 따라서 UserDao 같은 역할
 *  	UserServiceImpl과 커뮤니케이션하면서 주고 받은 정보를 저장 후
 *  	검증할 수 있는 목 오브젝트 필요
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

	protected void upgradeLevel(User user) {
		user.upgradeLevel();
		
		// 수정된 사용자 정보를 DB에 반영
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
