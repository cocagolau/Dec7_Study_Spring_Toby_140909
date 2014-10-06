package me.dec7.user.sqlservice;


/*
 * 리소스 추상화
 *  - OxmSqlReader, XmlSqlReader의 문제점
 *  	- SQL 매핑 정보가 담긴 xml 파일은 UserDao.class와 동일한 classpath에 존재하는 파일로 제한됨
 *  - relative path of servlet context, http, ftp protocal ..
 *  	- 자바에 다양한 위치에 존재하는 리소트에 대한 단일화된 접근 interface가 없음
 *  	- 클래스패스에서 리소스를 가져오기 위해
 *  		- ClassLoader 클래스의 getResourceAsStream()
 *  	- 웹, HTTP 접근통해 파일로 바꾸기 위해
 *  		- URL 클래스
 *  	- Servlet context
 *  		- ServletContext의 getResourceAsStream()
 *  	- 리소스 위치/종류에 따른 다른 방법
 *  		- 최종적으로 InputStream 사용 (목적 동일)
 *  
 * Resource
 *  - spring은 자바의 일관성없는 리소스 접근 API 추상화 위해 Resource 라는 추상화 interface를 정의
 *  - Resouce는 spring에서 bean이 아닌 값으로 취급됨
 *  	- bean 등록하지 않아도 됨
 *  	- 단지, 추상화하는 방법이 문제
 *  
 * ResourceLoader
 *  - 접두어를 사용해 Resource 오브젝트를 선언하는 방법
 *  	- 문자열 안에 리소스 종류 / 위치를 표현
 *  	- 접두어	
 * 			- 없는경우
 * 				- 리소스로더의 구현방식에 따라 리소스를 가져오는 방식이 달라짐
 * 			- 있는 경우
 * 				- file:
 * 					- 파일시스템
 * 				- classpath:
 * 					- 클래스패스의 루트
 * 				- http:, ftp:
 * 					- http 프로토콜을 사용해 접근할 수 있는 웹 상의 리소스 지정
 * 
 * ApplicationContext
 * 	- ResouceLoader interface를 상속함
 * 	- 사용목적
 * 		- 스프링 설정정보를 Resource 형태로 읽어옴
 * 		- 외부에서 읽어오는 모든 정보
 * 		- 빈의 프로퍼티 값을 수정시 
 * 	- myFile의 프로퍼티가 Resource 타입
 * 		<property name="myFile" value="classpath:me/dec7/..../myFile.txt" />
 * 		<property name="myFile" value="file:/data/myFile.txt" />
 * 		<property name="myFile" value="http://dec7.me/myFile" />
 * 
 * 
 */
public interface SqlReader {
	
	void read(SqlRegistry sqlRegistry);
}
