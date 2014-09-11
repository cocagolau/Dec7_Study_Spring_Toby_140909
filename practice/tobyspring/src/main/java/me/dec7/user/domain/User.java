package me.dec7.user.domain;


/*
 * 사용자 정보 저장시 Java Bean 규약을 따르는 Object를 만들면 편함
 * 
 * JavaBean
 *  - 원래 비주얼 툴에서 조작 가능한 컴포넌트를 말함
 *  - Java의 주력 개발플랫폼이 웹 기반 엔터프라이즈 방식으로 바뀜으로써 자바빈은 인기를 잃음
 *  - Java Bean의 몇 가지 코딩 관례는 JSP Bean, EJB와 같은 표준기술, 오픈소스를 통해 이어짐
 *  - 이젠, 비주얼 컴포넌트라기 보다 두 가지 관례를 따라 만들어진 Object에 가까움
 *  	1. default constructor
 *  		- 파라미터가 없는 디폴트 생성자를 가진다.
 *  		- 툴, 프레임워크에서 reflection을 이용해 Ojbect를 생성하기 때문
 *  	2. property
 *  		- Java Bean이 노출하는 이름을 가진 속성
 *  		- property는 set으로 시작하는 수정자 메소드, get으로 시작하는 접근자 메소드를 이용해 수정/조회 가능
 *  
 */

public class User {

	private String id;
	private String name;
	private String password;

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

}
