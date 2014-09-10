package me.dec7.user.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import me.dec7.user.domain.User;

public abstract class UserDao {

	public UserDao() {
		super();
	}

	public void add(User user) throws SQLException, ClassNotFoundException {
		Connection c = getConnection();
		
		PreparedStatement ps = c.prepareStatement("insert into users(id, name, password) values(?, ?, ?)");
		ps.setString(1, user.getId());
		ps.setString(2, user.getName());
		ps.setString(3, user.getPassword());
		
		ps.executeUpdate();
		
		ps.close();
		c.close();
	}

	public User get(String id) throws SQLException, ClassNotFoundException {
		Connection c = getConnection();
		
		PreparedStatement ps = c.prepareStatement("select * from users where id = ?");
		ps.setString(1, id);
		
		ResultSet rs = ps.executeQuery();
		rs.next();
		
		User user = new User();
		user.setId(rs.getString("id"));
		user.setName(rs.getString("name"));
		user.setPassword(rs.getString("password"));
		
		rs.close();
		ps.close();
		c.close();
		
		return user;
	}
	
	
	/*
	 * add()와 get()에 중복되어있던 Connection 연결 코드를 getConnection()으로 묶음
	 * 앞으로 connection을 한 곳에서 관리하게 됨
	 * 
	 * 앞서서 UserDao를 test했고 잘 동작했지만 수정 후에도 잘 동작하리라는 보장은 없으므로 재검증이 필요
	 * 
	 * main()을 다시 실행하면 test할 수 있지만
	 * 두 번째 실행부터는 id가 primary key이므로 무조건 예외가 발생함
	 */
	
	/*
	 * 리팩토링
	 *  - 기존의 코드를 외부 동작방식에 변화없이 내부 구조를 변경해서 재구성하는 작업 / 기술
	 *  - 리팩토링시 내부의 설계가 개선되어 코드 이해가 편해지고, 변화에 효율적으로 대응 가능함 
	 *  
	 *  메소드 추출 기법
	 *   - 아래 코드는 connection을 가져오는 기능을 getConnection()이라는 method로 추출함
	 */
	
	/*
	 * 1.2.3
	 * 코현코드는 제거되고 추상메소드로 바뀜
	 * 메소드 구현은 subClass에서 담당 
	 * 
	 * Dao는 아래와 같이 두 가지 class 레벨로 독립적으로 구분됨
	 *  - UserDao : Dao의 핵심기능인 데이터의 등록, 조회 등.. 을의 관심을 담당
	 *  - UserDaoImpl : DB연결방법을 어떻게 할 것인가?
	 *  
	 * 이로써 UserDao는 변경과 확장이 용이해짐
	 */
	protected abstract Connection getConnection() throws ClassNotFoundException, SQLException;
	
	/*
	 * Design Pattern
	 *  - SW 설계시 특정 상황에서 자주 만나는 문제를 해결하기 위해 사용할 수 있는 재사용 가능한 솔루션
	 *  - Pattern마다 간결한 이름이 있어서 이름만으로 설계 의도와 해결책을 설명할 수 있다는 장점
	 *  - 주로 객체지향 설계에 관한 것이고 객체지향적 설계 원칙을 이용해 문제 해결
	 *  
	 *  - 문제를 해결하기 위한 확장성 추구 방법이 대부분 두 가지 구조로 정리
	 *  	1. 클래스 상속
	 *  	2. 오브젝트 합성
	 *  
	 *  - 패턴에서 가장 중요한 것
	 *  	- 각 패턴의 핵심이 담긴 목적, 의도임. 
	 *  	- 패턴을 적용할 상황, 해결해야할 문제, 솔루션의 구조, 각 요소의 역할, 핵심 의도가 무엇인지 기억해야 함
	 */

	/*
	 * Template Method Pattern
	 *  - SuperClass에서 기본적인 로직의 흐름을 구성하고
	 *  - 그 기능의 일부를 abstract method나 overriding가능한 protected method 등으로 재구현하여
	 *  - SubClass에서 구체적인 Object 생성 방법을 결정
	 *  
	 *  - 상속을 통해 SuberClass의 기능을 확장할 수 있는 가장 대표적 방법
	 *  - SuperClass에서 default 기능을 만들고 필요할 때마다 Override 할 수 있는 method는 hook method라고 함
	 */
	
	/*
	 * Factory Method Pattern
	 *  - 상속을 통해 기능을 확장하는 패턴
	 *  - SuperClass 코드에서 SubClass에서 구현할 Method를 호출해서 필요한 Type의 Object를 가져와 사용
	 *  - 주로 interface type으로 Object를 리턴하므로 SubClass에서 어떤 class를 리턴할지 SuperClass는 알지 못함
	 *  - SubClass에서 다양한 방법으로 Object 생성방법 / 클래스 결정할 수 있도록 미리 정의한 Method
	 */
	
	/*
	 * 위 두 패턴은 관리사항이 다른 코드를 분리하여 서로 독립적으로 변경, 확장할 수 있는 가장 효과적방법
	 * 
	 * 하지만 '상속'을 이용했다는 단점
	 *  - 상속을 통한 상하위 클래스 관계는 생각보다 밀접함
	 *  - SubClass는 SuperClass의 기능을 직접 사용 가능 / 그만큼 SuperClass 변경시 SubClass에 직접적으로 변화가 전달
	 *  - 확장된 기능인 DB connection 생성 코드를 전혀 다른 Dao Class에 적용할 수도 없음
	 *  	- 만약 Dao Class가 다양해 질 경우 getConnection() 구현 코드도 Dao Class마다 중복될 수 있음
	 *  
	 */
}








