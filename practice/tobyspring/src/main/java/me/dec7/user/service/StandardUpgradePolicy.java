package me.dec7.user.service;

import me.dec7.user.domain.Level;
import me.dec7.user.domain.User;

public class StandardUpgradePolicy  implements UserLevelUpgradePolicy {
	
	public static final int MIN_LOGCOUNT_FOR_SILVER = 50;
	public static final int MIN_RECOMMEND_FOR_GOLD = 30;
	
	// 조건만족시 upgrade 실현
	
		public void upgradeLevel(User user) {
			/*
			 * 문제점
			 *  1. GOLD 레벨 사용자는 이 메소드를 호출해도 아무일도 처리하지 않음
			 *  2. 레벨 증가시 if문이 점점 증가할 것임
			 *  
			 * 해결책
			 *  1. 레벨의 순서와 다음 단계의 레벨이 무엇인지 결정하는 일은 Level에 맡김	
			 *  	- 레벨의 순서를 UserService에 담을 필요 없음
			 *  	- me.dec7.user.domain.Level 참고
			 *  
			 *  2. User의 내부정보가 바뀌는 부분은 User가 스스로 다루는 게 적절
			 *  	- User는 사용자 정보를 담는 단순한 bean이지만
			 *  	- User도 자바 오브젝트이므로 내부 정보를 다루는 기능이 있을 수 있음
			 *  	- User에게 어떤 정보를 변경하라고 요청하는 게 더 좋은 방법
			 *  	- me.dec7.user.domain.User 참고
			 *
			if (user.getLevel() == Level.BASIC) {
				user.setLevel(Level.SILVER);
				
			} else if (user.getLevel() == Level.SILVER) {
				user.setLevel(Level.GOLD);
				
			}
			 */
			
			/*
			 * 객체지향적인 코드는
			 * 다른 오브젝트의 데이터를 가져와서 작업하는 대신
			 * 데이터를 가지고 있는 다른 오브젝트에게 작업을 해달라고 요청함
			 * 
			 * 오브젝트에게 데이터를 요구하지 말고
			 * 작업을 요청하는 것이 객체지향 프로그래밍의 가장 기본이 되는 원리
			 * 
			 */
			user.upgradeLevel();
		}

		// upgrade 가능 확인 메소드
		
		/*
		 * 사용자 업그레이드 정책이 매번 달라질 수 있는 경우
		 *  - 업그레이드 정책을 UserService에서 분리할 수 있음
		 *  - 분리된 업그레이드 정책을 담은 오브젝트는 DI를 통해 UserService에 주입
		 */
		public boolean canUpgradeLevel(User user) {

			Level currentLevel = user.getLevel();
			
			// level별로 구분해 조건을 판단
			switch (currentLevel) {
				case BASIC	: return (user.getLogin() >= MIN_LOGCOUNT_FOR_SILVER);
				case SILVER	: return (user.getRecommend() >= MIN_RECOMMEND_FOR_GOLD);
				case GOLD	: return false;
				default		: throw new IllegalArgumentException("Unknown Level: " + currentLevel);
			}
		}


}
