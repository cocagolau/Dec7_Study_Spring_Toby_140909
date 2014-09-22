package me.dec7.user.service;

import java.util.List;

import me.dec7.user.domain.User;

import org.springframework.transaction.annotation.Transactional;


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
 * 어노테이션은 타킷 클래스가 인터페이스보다 우선
 */
@Transactional
public interface UserService {
	
	/*
	
	<tx:method name="*" /> 과 같은 설정 효과
	메소드 레벨 @Transactional이 없으므로 대체 정책ㅇ네 따라 타입레벨에 부여된 default 속성 적용
	
	 */
	void add(User user);
	void deleteAll();
	void update(User user);
	void upgradeLevels();
	
	
	/*
	
	<tx:method name="get" read-only="true" />
	
	메소드 단위로 부여된 트랜잭션 속성
	같은 속성을 가지더라도 메소드 레벨에 부여될 때는 메소드마다 반복될 수밖에 없음
	
	 */
	@Transactional(readOnly=true)
	User get(String id);
	
	@Transactional(readOnly=true)
	List<User> getAll();
}
