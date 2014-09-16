package me.dec7.user.domain;


public class User {
	
//	private static final int BASIC = 1;
//	private static final int SILVER = 2;
//	private static final int GOLD = 3;
	
	
	/* 
	 * Level 타입의 변수담을 field 추가
	int level;
	
	 * 사용자 레벨 관리로직에 필요
	 * 	- 로그인 횟수, 추천수
	 */
	Level level;
	int login;
	int recommend;
	
	private String id;
	private String name;
	private String password;
	

	public User(String id, String name, String password, Level level, int login, int recommend) {
		this.id = id;
		this.name = name;
		this.password = password;
		this.level = level;
		this.login = login;
		this.recommend = recommend;
	}
	
	public User(String id, String name, String password) {
		this.id = id;
		this.name = name;
		this.password = password;
	}
	
	public User() {
	}
	

	/*
	 * p321, 사용자 레벨 상수 값을 이용한 코드
	
	if (user1.getLevel() == User.BASIC) {
		user1.setLevel(User.SILVER);
	}
	 
	 * 문제
	 * 	- level 타입이 int이므로 다른 종류의 정보를 넣는 실수를 해도 컴파일러가 체크 못함
	user1.setLevel(1000);
	
	 * 해결책
	 * 	- enum을 이용하는 것이 편리 & 안전
	 *  - me.dec7.user.domain.Level 참고
	 */
	/*
	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}
	*/
	

	public Level getLevel() {
		return level;
	}
	
	public void setLevel(Level level) {
		this.level = level;
	}	

	public int getLogin() {
		return login;
	}

	public void setLogin(int login) {
		this.login = login;
	}

	public int getRecommend() {
		return recommend;
	}

	public void setRecommend(int recommend) {
		this.recommend = recommend;
	}

	public String getId() {
		return id;
	}


	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	public void upgradeLevel() {
		// 다음 레벨이 무엇인지 확인
		Level nextLevel = this.level.nextLevel();
		
		// 물론 canUpgradeUser가 처리할 수 있지만 조금 더 명확히 예외상황을 알려줘야함
		if (nextLevel == null) {
			throw new IllegalStateException(this.level + "은 업그레이드가 불가능합니다.");
			
		} else {
			this.level = nextLevel;
			
		}
	}

}
