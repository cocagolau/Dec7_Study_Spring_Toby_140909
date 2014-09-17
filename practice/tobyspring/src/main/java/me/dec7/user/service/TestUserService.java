package me.dec7.user.service;

import java.util.List;

import me.dec7.user.domain.User;

import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/*
 * 5.3, 서비스 추상화와 단일 책임 원칙
 * 
 * UserDao, UserService는 관심을 불리하고 독자적으로 확장 가능하도록 만듦
 *  - 같은 어플리케이션 로직 코드지만 내용에 따라 분리
 *  - 같은 계층에서 수평적인 분리라 할 수 있음
 *  
 * 트랜젝션 추상화
 *  - 위와 좀 다르게, 
 *    어플리케이션의 비지니스 로직과 그 하위에서 동작하는 로우레벨의 트랜잭션 기술이
 *    아예 다른 계층의 특성을 갖는 코드를 분리한 것
 *    
 * 수평, 수직 계층구조
 *  - DI로 계층구조로 구성, 결합도 낮춤
 *  
 * 단일 책임 원칙
 *  - 하나의 모듈은 한 가지 책임 (적절한 분리)
 *  - UserService에 JDBC connection을 직접 처리할 때
 *  	1) 사용자 레벨 업그레이드 처럼, 사용자 관리 로직에 대한 관심
 *  	2) sql connection에 대한 관심 
 *  
 *  - 장점
 *  	- 어떤 변경이 필요할 때 수정 대상이 명확해짐
 *  	- 이를 가능토록 하는 기술이 DI / interface 사용
 *  
 */

/*
 * p351, 테스트용 UserService 대역
 * 
 * 작업 중간에 예외를 생성하는 방법?
 *  - 가장 쉬운 방법은 예외를 강제로 발생시키도록 코드 수정
 *  	--> 하지만 테스트 위해 코드를 함부로 건드리는 건 좋지 않음
 *  
 *  - UserService를 상속받는 새로운 TestUserService생성
 * 
 */
public class TestUserService extends UserService {
	
	private String id;
	
	// 예외를 발생시킬 User 오브젝트의 id를 지정할 수 있도록 함
	public TestUserService(String id) {
		this.id = id;
	}	

	/*
	 * p357, UserService와 UserDao의 트랜잭션 문제
	 * 
	 * UserDao의 JdbcTemplate 안에서 DataSource의 getConnection()을 호출해
	 * Connection을 가져와 사용 후 Connection을 닫아줌
	 *  - tempate method는 호출 한번에 DB connection을 만들고 닫음
	 *  - 일반적으로 트랜잭션은 커넥션보다 존재 범위가 짦으므로 UserDao는 각 메소드 하나씩 독립적은 트랜잭션이 실행됨
	 *  
	 *  - 따라서, Dao 사용시
	 *    비지니스 로직을 담은 UserService 내에거는 트랜잭션으로 묶는 일이 불가능함
	 *    
	 *    
	 * 해결책
	 *  1. 트랜잭션이 필요한 upgradeLevels()를 Dao method 안으로 이동?
	 *  	- 비지니스 로직와 데이터 로직을 묶는 한심한 결과 
	 *  2. 트랜잭션 경계를 upgradeLevels() 안으로 두기 위해 DB connection을 내부에 두기?
	 *  	- 아래와 같은 구조
	 *  
			public void upgradeLevels() throws Exception {
				DB connection 생성
				트랜잭션 시작
				try {
					dao 메소드 호출
					트랜잭션 커밋
				} catch (Exception e) {
					트랜잭션 롤백
					throw e;
				} finally {
					DB connection 종료
				}
			}
			
			- 생성된 connection 오브잭트를 가지고 작업은 UserDao의 update()에서 진행
				- 트랜잭션으로 어쩔 수 없이 db connection을 가져왔지만
				  순수 데이터 액세스 로직은 UserDao에 둬야 하기 때문
			- upgradeLevels()은 User.upgradeLevel()를 경유하기 때문 connection을 User에게도 전달
			- 문제점
				1. JdbcTemplate 사용불가 --> UserService에 try,catch 사용
				2. dao 메소드, UserService 메소드에 connection 파라미터 추가
					- 멀티스레드 환경이기 때문에 인스턴스 변수를 사용 불가
				3. connection 파라미터가 UserDao interface method에 추가시 UserDao는 더 이상 액세스 기술에 독립적일 수 없음
				
				
	 * Spring의 해결책
	 *  - upgradeLevels() 메소드가 트랜잭션 경계 설정을 위해
	 *    그 안에서 Connection을 생성 하는 것은 피할 수 없음
	 *  - 다만, 파라미터로 전달하기는 피하고 싶음
	 *  
	 *  트랜잭션 동기화 방식 (Transaction Synchronization)
	 *   - UserService에서 트랜잭션을 시작하기 위해 만든 connection 오브잭트를 특별한 저장소에 보관
	 *   - 호출되는 dao의 method의 jdbcTemplate에서는 저장된 connection을 가져다 사용
	 *   - 트랜잭션 종료시 동기화 종료
	 *   
	 *  작업흐름
	 *   1) UserService는 Connection 생성
	 *   2) 오브잭트를 트랜잭션 동기화 저장소에 저장 후 setAutoCommit(false)을 호출
	 *   3) 첫번째 update() 호출
	 *   4) 내부의 jdbcTemplate 메소드에서 트랜잭션 동기화 저장소에 현재 시작된 트랜잭션 connection 오브랮트 있는지 확인
	 *   5) upgradeLevels() 메소드시작시 connection 가져옴
	 *   6) 해당 connection으로 트랜잭션 작업, 트랜잭션 동기화 저장소에서 가져온 connection은 종료하지 않음
	 *   ...
	 *   7) 모든 작업 정상적으로 마치면 commit()을 호출하고 마침.
	 *   	 - 트랜잭션 동기화 저장소에서 해당 connection을 제거
	 *   9) 어느 상황에서도 예외 발생시 UserService는 즉시 rollback()을 호출
	 *   	 - 트랜잭션 동기화 저장소에서 해당 connection을 제거
	 *   
	 *   각 스레드마다 독립적으로 connection 오브젝트를 보관하므로 멀티스레드 환경도 가능
	 *   
	
	@Override
	protected void upgradeLevel(User user) {
		
		// 지정한 id의 User오브젝트가 발견시 예외 던져 작업을 강제 중단
		if (user.getId().equals(this.id)) {
			throw new TestUserServiceException();
		}
		
		super.upgradeLevel(user);
	}
	 */

	// Spring의 트랜잭션 추상화 API 적용
	public void upgradeLevels() throws Exception {
		
		/*
		 * Spring이 제공하는 트랜잭션 경계설정을 위한 추상 interface
		 * JDBC의 로컬 트랜잭션을 이용시 PlatformTransactionManager를 구현한 DataSourceTransactionManager를 사용
		 */
		// PlatformTransactionManager transactionManager = 
				// JDBC 트랜잭션 추상 오브잭트 생성
				// new DataSourceTransactionManager(dataSource);
				
				// 하이버네트으로 구현시
				// new HibernateTransactionManager();
				
				// JTA 글로벌 트랜잭션 오브잭트 생성
				// new JtaTransactionManager();
				
		
		// getTransaction(): 트랜잭션 시작
		TransactionStatus status = this.transactionManager.getTransaction(
				// 트랜잭션에 대한 속성을 담음
				new DefaultTransactionDefinition());
		
		try {
			// 트랜잭션 안에서 진행되는 작업
			List<User> users = userDao.getAll();
			
			for (User user : users) {
				// upgrade 가능시
				if (canUpgradeLevel(user)) {
					// upgrade 해라
					upgradeLevel(user);
					userDao.update(user);
				}
			}
			
			// 트랜잭션 커밋
			this.transactionManager.commit(status);
			
		} catch (Exception e) {
			// 트랜잭션 롤백
			this.transactionManager.rollback(status);
			throw e;
			
		}
	}
	
	/*
	 * JdbcTemplate과 트랜잭션 동기화
	 * 
	 * JdbcTemplate 기능
	 *  1. try/catch 작업흐름지원
	 *  2. SQLException 예외변환
	 *  3. 트랜잭션 동기화 관리
	 *  
	 * 트랜잭션 서비스 추상화
	 *  - 트랜잭션 처리 코드를 담은 UserService에서 문제발생
	 *  	- 여러개의 DB사용, 하나의 트랜잭션 안에서 여러 개의 DB에 데이터를 넣는 작업시..
	 *  	- 한개 이상의 DB로 작업을 하나의 트랜잭션으로 만드는 일
	 *  		- JDBC connection을 이용한 트랜잭션 방식인 로컨 트랜지션은 불가능
	 *  		- 로컬 트랜지션은 하나의 DB connection에 종속
	 * 	- 해결책
	 * 		- 별도의 트랜잭션 관리자를 통해 트랜잭션을 관리하는 글로번 트랜잭션 방식 필요
	 * 	
	 * JTA (java transaction api)
	 *  - 자바는 글로벌 트랜지션을 지원하는 트랜잭션 매니져 지원
	 *  - 여러개의 DB, 메시징 서버에 대한 트랜잭션 관리
	 *  - 어플리케이션은 기존의 방법대로 api를 사용해서 작업 수행
	 *  	- DB는 JDBC
	 *  	- 메시징 서버는 JMS 
	 *  - 트랜잭션은 JTA를 통해 트랜잭션 매니져가 관리하도록 위임
	 *  - 트랜잭션 매니저는 리소스 매니저(DB와 메시징 서버를 제어, 관리)와 XA 프로토콜을 통해 연결
	 *  - 분산 트랜잭션, 글로번 트랜잭션 가능 
	 * 
	 * 
	 * 
	 */
}
