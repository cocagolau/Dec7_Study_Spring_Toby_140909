package me.dec7.user.sqlservice;

/*
 * 7.2, 인터페이스 분리와 자기참조 빈
 * 
 * 어떤 인터페이스는 그 뒤에 숨어 있는 서브 시스템의 관문에 불과할 수 있음
 * 인터페이스로 대표되는 기능을 구현방법과 확장가능성에 따라 유연한 방법으로 재구성할 수 있도록 설계 필요
 * 
 * XML 파일 매핑
 *  - 스프링 설정파일에 sql정보를 넣고 활용하는 것은 좋은 방법 아님
 *  - JAXB (java architecture for xml binding) / java.xml.bind.JAXB
 *  	- XML 문서정보를 거의 동일한 구조의 오브젝트로 직접 맵핑해줌
 *  	- XML 문서의 구조를 정의한 스키마를 이용해 매핑할 오브젝트 클래스까지 자동으로 만들어주는 컴파일러 제공
 */

public interface SqlService {
	
	/*
	 * SqlRetrievalFailureException은 Runtime 예외
	 *  - 복구할 필요가 없는 경우 무시
	 */
	String getSql(String key) throws SqlRetrievalFailureException;
}
