package me.dec7.user.service;

import java.util.List;

import javax.sql.DataSource;

import org.springframework.transaction.PlatformTransactionManager;

import me.dec7.user.dao.UserDao;
import me.dec7.user.domain.Level;
import me.dec7.user.domain.User;


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
	
	
	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}
	
	// 트랜잭션에 사용한 dataSoruce; setter method injection
	protected DataSource dataSource;
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
	}
	
	public void upgradeLevels() throws Exception {
		
		List<User> users = userDao.getAll();
		
		for (User user : users) {
			// upgrade 가능시
			if (canUpgradeLevel(user)) {
				// upgrade 해라
				upgradeLevel(user);
				userDao.update(user);
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
