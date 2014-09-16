package me.dec7.user.service;

import java.util.List;

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

public class UserService {

	private UserDao userDao;
	private UserLevelUpgradePolicy userLevelUpgradePolicy;
	
	
	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}
	
	public void setUserLevelUpgradePolicy (UserLevelUpgradePolicy userLevelUpgradePolicy) {
		this.userLevelUpgradePolicy = userLevelUpgradePolicy;
	}



	public void upgradeLevels() {
		List<User> users = userDao.getAll();
		
		/*
		 * 문제점
		 *  1. 성격이 다른 조건이 섞임
		 *  2. 레벨의 개수만큼 if 조건블록이 반복됨
		 *  3. 레벨과 업그레이드를 동시에 비교하는 부분
		 *
		for (User user : users) {
			Boolean changed = null;
			
			  // 현재 레벨 파악                      // 업그레이드 조건
			if (user.getLevel() == Level.BASIC && user.getLogin() >= 50) {
				// 다음 단계의 레벨
				user.setLevel(Level.SILVER);
				// flag 설정
				changed = true;
			} else if (user.getLevel() == Level.SILVER && user.getRecommend() >= 30) {
				user.setLevel(Level.GOLD);
				changed = true;
			} else if (user.getLevel() == Level.GOLD) {
				changed = false;
			} else {
				changed = false;
			}
			
			// flag에 따라 update 실행
			if (changed) {
				userDao.update(user);
			}
		}
		 */
		
		/*
		 * refactoring
		 * 1단계 - 레벨을 업그레이드하는 작업의 흐름
		 */
		
		for (User user : users) {
			// upgrade 가능시
			if (userLevelUpgradePolicy.canUpgradeLevel(user)) {
				// upgrade 해라
				userLevelUpgradePolicy.upgradeLevel(user);
				userDao.update(user);
			}
		}
		
	}
	
	/*
	 *  canUpgradeLevel()
	// upgradeLevel()
	 * 두 메소드는 StandardUpgradePolicy로 이동
	 */
	public void add(User user) {
		if (user.getLevel() == null) {
			user.setLevel(Level.BASIC);
		}
		
		userDao.add(user);
	}
}
