package me.dec7.user.sqlservice;


/*
 * 7.4, interface 상속을 통한 안전한 기능 확장
 * 
 * 서버가 운영중인 상태에서 서버를 재시작하지 않더라도 긴급하게 SQL을 변경할 필요 생김
 *  - SqlService 초기화 필요 
 *  	- 지금은 SQL 정보를 갱신하기 위해 서버를 재시작 / 어플리케이션 리로딩 필요
 * 
 * 어플리케이션을 재시작하지 않고 SQL 내용만 변경하고 싶다면?
 *  - DI와 기능의 확장
 *  	- DI는 일종의 디자인 패턴
 *  	- DI프레임워크를 적용했다고 해서 DI를 바르게 사용하는 것은 아님
 *  	- DI에 적합한 오브젝트 설계가 필요
 * 
 * DI를 의식하는 설계
 *  - 기능 확장
 *  	- SqlService 내부 기능
 *  		- 적절한 적절한 책임 / 역할에 따라 분리
 *  		- 인터페이스를 정의해 느슨하게 연결
 *  		- DI를 통해 유연하게 의존관계를 지정하도록 설계
 *  - DI를 의식하며 설계
 *  	- DI를 이용하는 것은 쉬움
 *  	- DI에 필요한 유연/확장성 뛰어난 오브젝트 설계는 어려움
 *  - 항상 오브젝트는 자유롭게 확장될 수 있음
 *  	- 커다란 오브젝트보다 최소한 두개 이상의, 의존고나계를 가지고 협력하는 오브젝트 필요
 *  	- 따라서 적절한 책임에 따라 오브젝트를 분리해야함 
 *  - 런타임시 오브젝트를 다이나믹하게 연결해 유연한 확장을 꾀하는 것이 목적
 *  
 * DI와 interface 프로그래밍
 *  - DI 답게 만들기 위해 두 개의 오브젝트가 인터페이스를 통해 느슨하게 연결되어야 함
 *  
 * 인터페이스 사용 이유
 * 	1. 다형성을 얻기 위함 
 * 		- 하나의 interface로 여러개의 구현을 바꿔가며 사용
 * 	2. 클라이언트와 의존 오브젝트 사이의 관계를 명확히 해줌
 * 		- 인터페이스는 하나의 오브젝트가 여러 개를 구현할 수 있음
 * 			- 하나의 오브젝트를 바라보는 창이 여러가지 일 수 있음
 * 			- 각기 다른 관심과 목적을 가지고 어떤 오브젝트에 의존할 수 있음을 의미
 * 
 * 인터페이스 분리 원칙
 *  - 충분히 응집도가 높더라도 목적/관심이 다른 client 존재시 interface를 통해 적절히 나눌 필요 있음
 *  - 모든 클라이언트가 자신의 관심에 따른 접근방식을 불필요한 간섭없이 유지할 수 있다는 점
 *  	- 기존 클라이언트에 영향ㄴ을 주지 않고 오브젝트의 기능을 확장 / 수정 가능
 *  
 * 인터페이스 상속
 *  - SqlService, BaseSqlService 관계
 *  	- BaseSqlService는 SqlReader, SqlRegistry 두개 interface로 의존 오브젝트를 DI
 *  	- BaseSqlService는 SqlRegistry를 통해 MySqlRegistry 오브젝트에 접근
 *  		- MySqlRegistry의 구현내용의 변경으로 SQL 등록기능 확장되더라도 BaseSqlService는 변경없이 유지 
 *  - SqlRegistry, MySqlRegistry 관계
 *  	- MySqlRegistry는 제 3의 클라이언트를 위한 interface를 가질 수 있음
 *  	- 새로운 클라이언트의 성격에 따라 기존 SqlRegistry와 별개인 혹은 확장한 interface를 이용할 수 있음
 *  
 * SQL 변경 기능 확장
 *  - BaseSqlService
 *  	- SqlRegistry interface가 제공하는 기능이 충분
 *  	- DAO를 위한 SQL 조회하는 입장에서 SQL 변경기능이 필요없음
 *  	- client의 목적과 용도에 적합한 interface만 제공하는 interface 분리원칙을 위해서도
 *  	  SqlRegistry를 변경하면 안됨
 *  - UpdatableSqlRegistry
 *  	- 대신 새롭게 추가할 기능을 사용하는 client를 위해
 *  		- 새로운 interface 정의
 *  		- 기존 interface 확자
 *  
 * UpdatableSqlRegistry
 *  - 새로운 기능을 사용할 클라이언트 존재해야함
 *  - 관리기능 = 검색 + 수정
 *  - 따라서 기존 SqlRegistry 확장
 */

public interface SqlRegistry {
	
	void registerSql(String key, String sql);
	
	String findSql(String key) throws SqlNotFoundException;
}
