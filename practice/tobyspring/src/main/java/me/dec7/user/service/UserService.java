package me.dec7.user.service;

import java.util.List;

import me.dec7.user.domain.User;


/*
 * 트랜잭션 경계설정 일원화
 *  - 특정 계층의 경계를 트랜잭션 경계와 일치하는 것이 좋음 
 *  - 서비스 계층(비지니스 로직 담은) 오브젝트의 메소드가 트랜잭션 경계를 부여하기 좋음
 *  	- 따라서, 테스트가 아닌 이상
 *  	- 다른 계층, 모듈에서 DAO에 직접 접근을 차단
 *  		- 트랜잭션은 보통 서비스 계층의 메소드 조합을 통해 만들어지므로
 *  		- DAO가 제공하는 주요 기능은 서비스계층에 위임 메소드로 만들 필요 있음
 * 		- 단순 비지니스 로직 + 단순 DB 입출력이라면 통합해도 되지만,
 * 		  비지니스 로직을 독자적으로 두고 테스트하려면 서비스 계층을 만듦
 * 
 */
public interface UserService {
	
	void add(User user);
	
	/*
	 *  추가된 메소드
	 *  DAO 메소드와 1:1 대응되는 CRUD 메소드지만 add() 처럼 단순 위임 이상의 로직을 가질 수 있음
	 */
	User get(String id);
	List<User> getAll();
	void deleteAll();
	void update(User user);
	
	void upgradeLevels();
}
