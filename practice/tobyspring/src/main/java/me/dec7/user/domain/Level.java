package me.dec7.user.domain;

public enum Level {
	
	// 3개의 enum object 정의
	// BASIC(1), SILVER(2), GOLD(3);
	GOLD(3, null), SILVER(2, GOLD), BASIC(1, SILVER);
	
	private final int value;
	// 다음 단게의 레벨 정보를 스스로 가지고 있도록 Level타입의 next변수를 추가
	private final Level next;
	
	// DB에 저장할 값을 넣어줄 생성자 
	Level(int value, Level next) {
		this.value = value;
		this.next = next;
	}
	
	// 값을 가져오는 메소드
	public int intValue() {
		return value;
	}
	
	public Level nextLevel() {
		return this.next;
	}
	
	
	/*
	 * Level enum은 내부에 DB를 저장할 int 타입의 값을 가지고 있지만
	 * 겉으로는 Level 타입의 오브젝트이므로 안전하게 사용 가능
	 * 
	 * 만약 setLevel(1000) 같은 코드는 컴파일러가 타입이 일치하지 않는다는 에러는 내면서 걸러줄 것임
	 */
	public static Level valueOf(int value) {
		switch (value) {
			case 1: return BASIC;
			case 2: return SILVER;
			case 3: return GOLD;
			default: throw new AssertionError("Unknown value: " + value);
		}
	}

}
