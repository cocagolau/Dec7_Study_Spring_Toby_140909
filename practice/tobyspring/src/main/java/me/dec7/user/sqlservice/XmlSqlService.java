package me.dec7.user.sqlservice;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import me.dec7.user.dao.UserDao;
import me.dec7.user.sqlservice.jaxb.SqlType;
import me.dec7.user.sqlservice.jaxb.Sqlmap;


/*
 * 7.2.5 interface 분리
 *  - 변화에 대한 유연성
 *  
 * 현재 XmlSqlService는 특정 포멧을 사용하고 HashMap타입의 맵에 저장
 *  - 다른 포멧을 사용 / 다른 저장방식으로 변화
 *  
 * 책임에 따른 interface 정의
 *  1. 분리 가능한 관심 구분 (변경가능한 책임)
 *  	1) SQL 정보를 외부의 리소스에서 읽어오기
 *  		- XML, test, scheme, xls, DB ...
 *  	2) SQL을 보관하고 필요할 때 제공
 *     +3) 한번 가져온 SQL을 필요에 따라 수정
 *     
 *  변경방법
 *   - Dao관점에서 SqlService라는 interface를 구현한 오브젝트에만 의존하므로 달라질 것 없음
 *   - SqlService가 SqlReader, SqlRegistry 타입의 오브젝트를 사용하도록 함
 *   
 *  문제점
 *   - SqlReader가 읽어오는 Sql정보
 *   	- 다시 SqlRegistry에 전달되어 등록되도록 해야함
 *   	- 정보를 어떤 형식으로 전달할지
 *   	- sqlReader.readSql(sqlRegistry) // 저장 전략을 가진 객체를 sqlReader에제 전달해 읽으라고 함  	
 */

/*
 * loadSql()
 *  - XmlSqlService
 *  - sqlReader에게 sqlRegistry를 전달하며 sql을 읽고 저장하도록 요청
 * 
 * getSql()
 *  - SqlService
 * 
 */

/*
 * 7.2.6, default 의존관계
 *  - 확장가능한 interface 정의, DI가능한 코드 --> 완전히 분리 DI조합
 *  - me.dec7.user.sqlservice.BaseSqlService 참고
 */

public class XmlSqlService implements SqlService, SqlRegistry, SqlReader {
	
	private Map<String, String> sqlMap = new HashMap<String, String>();
	private String sqlMapFile;
	private SqlReader sqlReader;
	private SqlRegistry sqlRegistry;
	
	
	
	
	// spring이 오브젝트를 만드는 시점에서 SQL을 읽도록 생성자 이용
	/*
	public XmlSqlService() {
		 * 빈 초기화시, 생성자에서 예외가 발생할 수 있는 복잡한 초기화는 좋지 않음
		 * 
		 * 문제1) 생성자에서 예외 처리
		 *  1. 예외 다루기 어렵고
		 *  2. 상속하기 불편
		 *  3. 보안에도 문제 있음
		 *  - 해결책
		 *  	- 초기상태를 가진 오브젝트 만들고,
		 *  	- 별도의 초기화 메소드를 사용하는 것이 바람직
		 * 
		 * 문제2) 읽어들일 파일의 위치/이름 코드에 고정됨
		 *  - 코드에서 바뀔 가능성이 있는 내용을 외부에서 DI 설정할 수 있도록 만들어야함
		 *  - 해결책
		 *  	- setter method 구성
		 *
		 *
		 * loadSql 메소드로 이동
		 * 
		String contextPath = Sqlmap.class.getPackage().getName();
		
		try {
			JAXBContext context = JAXBContext.newInstance(contextPath);
			Unmarshaller unmarshaller = context.createUnmarshaller();
			
			// UserDao와 같은 classpath의 sqlmap.xml 파일을 변환
			InputStream is = UserDao.class.getResourceAsStream("sqlmap.xml");
			Sqlmap sqlmap = (Sqlmap) unmarshaller.unmarshal(is);
			
			// 읽어온 SQL을 map으로 저장
			for (SqlType sql : sqlmap.getSql()) {
				sqlMap.put(sql.getKey(), sql.getValue());
			}
			
		} catch (JAXBException e) {
			// JAXBException은 복구 불가능한 예외 --> runtime 예외로 포장해 던짐
			throw new RuntimeException(e);
			
		}
	}
	 */
	
	public void setSqlReader(SqlReader sqlReader) {
		this.sqlReader = sqlReader;
	}


	public void setSqlRegistry(SqlRegistry sqlRegistry) {
		this.sqlRegistry = sqlRegistry;
	}

	// 외부에서 mapfile 이름을 DI하도록 setter method 생성
	public void setSqlMapFile(String sqlMapFile) {
		this.sqlMapFile = sqlMapFile;
	}

	/*
	 * 1. xml bean 설정 파일 읽음
	 * 2. bean 오브젝트 생성
	 * 3. property에 의존 오브젝트 혹은 값을 주입
	 * 4. bean이나 tag로 등록된 후처리기 동작
	 * 	- 코드에 달린 어노테이션에 대한 부가작업 실행
	 */
	@PostConstruct
	public void loadSql() {
		/*
		String contextPath = Sqlmap.class.getPackage().getName();
		
		try {
			JAXBContext context = JAXBContext.newInstance(contextPath);
			Unmarshaller unmarshaller = context.createUnmarshaller();
			
			// UserDao와 같은 classpath의 sqlmap.xml 파일을 변환
			InputStream is = UserDao.class.getResourceAsStream(this.sqlMapFile);
			Sqlmap sqlmap = (Sqlmap) unmarshaller.unmarshal(is);
			
			// 읽어온 SQL을 map으로 저장
			for (SqlType sql : sqlmap.getSql()) {
				sqlMap.put(sql.getKey(), sql.getValue());
			}
			
		} catch (JAXBException e) {
			// JAXBException은 복구 불가능한 예외 --> runtime 예외로 포장해 던짐
			throw new RuntimeException(e);
			
		}
		*/
		this.sqlReader.read(this.sqlRegistry);
	}
	
	



	@Override
	public String getSql(String key) throws SqlRetrievalFailureException {
		try  {
			
			return this.sqlRegistry.findSql(key);
			
		} catch (SqlNotFoundException e) {
			
			throw new SqlRetrievalFailureException(e);
			
		}
		/*
		String sql = sqlMap.get(key);
		
		if (sql == null) {
			
			throw new SqlRetrievalFailureException(key + "를 이용해서 sql을 찾을 수 없습니다.");
			
		} else {
			
			return sql;
			
		}
		*/
	}


	@Override
	public void read(SqlRegistry sqlRegistry) {
		String contextPath = Sqlmap.class.getPackage().getName();
		
		try {
			JAXBContext context = JAXBContext.newInstance(contextPath);
			Unmarshaller unmarshaller = context.createUnmarshaller();
			
			// UserDao와 같은 classpath의 sqlmap.xml 파일을 변환
			InputStream is = UserDao.class.getResourceAsStream(this.sqlMapFile);
			Sqlmap sqlmap = (Sqlmap) unmarshaller.unmarshal(is);
			
			// 읽어온 SQL을 map으로 저장
			for (SqlType sql : sqlmap.getSql()) {
				// sql 저장로직 구현에 독립적인 interface method를 통해 읽어들인 sql과 key를 전달
				sqlRegistry.registerSql(sql.getKey(), sql.getValue());
				// sqlMap.put(sql.getKey(), sql.getValue());
			}
			
		} catch (JAXBException e) {
			// JAXBException은 복구 불가능한 예외 --> runtime 예외로 포장해 던짐
			throw new RuntimeException(e);
			
		}
		
	}

	
	/*
	 * (non-Javadoc)
	 * @see me.dec7.user.sqlservice.SqlRegistry#registerSql(java.lang.String, java.lang.String)
	 * 
	 * HashMap이라는 저장소를 사용하는 구체적인 구현방법에서
	 * 독립될 수 있도록 interface의 method로 접근하게 해줌
	 */
	@Override
	public void registerSql(String key, String sql) {
		sqlMap.put(key, sql);
	}


	@Override
	public String findSql(String key) throws SqlNotFoundException {
		String sql = sqlMap.get(key);
		
		if (sql == null) 	throw new SqlNotFoundException(key + "를 이용해서 sql을 찾을 수 없습니다.");
		else				return sql;
	}

}
