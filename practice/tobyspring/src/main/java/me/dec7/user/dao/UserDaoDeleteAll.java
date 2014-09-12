package me.dec7.user.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/*
 * UserDao를 상속하여 기능을 확장하면 개발 폐쇄 원칙을 잘 지킬 수 있지만
 * 
 * 문제점
 *  1. Dao로직마다 상속을 통해 새로운 class를 만들어야 한다는 것이 큰 부담
 *  2. 확장 구조가 class를 설계하는 시점에 고정되어져 
 *     변하지 않는 코드를 가지 UserDao의 JDBC try/catch 블록과
 *     변하는 코드를 가진 sub class들이 class level에서 컴파일 시점에 그 관계가 절정됨
 *     	- 결국 관계에 대한 유연성이 떨어짐
 *     
 * 해결책
 *  - 전략 패턴 사용
 *  	- 오브젝트를 둘로 분리
 *  	  class level에서는 interface를 통해서만 의존하도록 함
 * 	 	- 특정 기능을 interface를 통해 외부의 독립된 전략 class에 위임
 */

/*
 * deleteAll()의 context
 *  - db connection 받아오기
 *  - praparedStatement를 만들어줄 외부 기능 호출 --> 전략 패턴(interface)
 *  - 전달 받은 PreparedStatement 실행
 *  - 예외 발생시 외부로 던지기
 *  - 모든 경우 만들어진 PreparedStatement / Connection을 close
 * 
 */
public class UserDaoDeleteAll extends UserDao {

	@Override
	protected PreparedStatement makeStatement(Connection c) throws SQLException {
		PreparedStatement ps = c.prepareStatement("delete from users");
		
		return ps;
	}

}
