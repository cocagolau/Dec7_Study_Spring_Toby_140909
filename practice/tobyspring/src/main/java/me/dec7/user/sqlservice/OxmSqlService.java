package me.dec7.user.sqlservice;

import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import me.dec7.user.dao.UserDao;
import me.dec7.user.sqlservice.jaxb.SqlType;
import me.dec7.user.sqlservice.jaxb.Sqlmap;

import org.springframework.oxm.Unmarshaller;
import org.springframework.oxm.XmlMappingException;


/*
 * 7.3.2, OXM 서비스 추상화 적용
 * 
 * SqlRegistry
 *  - DI
 * 
 * SqlReader
 *  - spring의 OXM unmarshaller를 이용하도록 고정
 *  	- 꼭 OxmSqlService 클래스가 OXM기술에 의존되어도 꼭 OXM 코드를 가질 필요 없음
 *  
 * OxmSqlService
 *  - BaseSqlService와 유사
 *  - SqlReader 타입을 스태틱 멤버 클래스로 내장
 *  	- 의존 오브젝트를 자신만 사용하도록 독점하는 구조 
 *  - OxmSqlService와 OxmSqlReader 관계
 *  	- 구조적으로 강하게 결합
 *  	- 논리적으로 명확한 구조
 *  	- 자바의 스태틱 멤버 클래스는 위의 용도로 적합
 *  
 * 이러한 구조
 *  - 강한 결합, 확장/변경 제한
 * 	- OXM을 이용하는 서비스 구조로 최적화
 *  - 하나의 클래스 -> bean 등록/설정 단순, 쉽게 사용
 *  	- 유연한 구조를 위해 따로 bean 등록할 수 있지만 서비스 개발자 입자에서 bean 설정이 늘어나는 것은 번거로움
 */
public class OxmSqlService implements SqlService {
	
	/*
	 * final이므로 변경 불가
	 *  - OxmSqlService는 oxmSqlReader를 DI할 수 없음 
	 *  
	 * OxmSqlService와 OxmSqlReader는 강하게 결합
	 * 	- 하나의 bean으로 등록
	 *  - 한 번에 설정할 수 있음 
	 *  
	 * OxmSqlReader
	 *  - 스스로 bean으로 등록될 수 없음
	 *  	- 외부 노출되지 않고 OxmSqlService에 의해 만들어짐
	 *  	- 따라서, 자신이 DI를 통해 제공받아야 하는 프로퍼티를
	 *        OxmSqlService의 공개된 프로퍼티를 통해 간접적으로 DI 받아야 함
	 *  	
	 */
	private final OxmSqlReader oxmSqlReader = new OxmSqlReader() {
		
	};
	
	private SqlRegistry sqlRegistry = new HashMapSqlRegistry();
	public void setSqlRegistry(SqlRegistry sqlRegistry) {
		this.sqlRegistry = sqlRegistry;
	}
	
	@PostConstruct
	public void loadSql() {
		this.oxmSqlReader.read(this.sqlRegistry);
	}

	@Override
	public String getSql(String key) throws SqlRetrievalFailureException {
		try { return this.sqlRegistry.findSql(key); }
		catch (SqlNotFoundException e) { throw new SqlRetrievalFailureException(e); }
	}
	
	/*
	 * setUnmarshaller / setSqlMapFile
	 *  - OxmSqlService의 프로퍼티를 통해 내장된 멤버 클래스의 프로퍼티로 설정해주는 코드
	 *  - 단일 bean 설정 구조를 위한 창구 역할
	 */
	public void setUnmarshaller(Unmarshaller unmarshaller) {
		this.oxmSqlReader.setUnmarshaller(unmarshaller);
	}
	
	public void setSqlMapFile(String sqlMapFile) {
		this.oxmSqlReader.setSqlMapFile(sqlMapFile);
	}
	
	
	
	/*
	 *  private member class로 정의
	 *  외부에서 접근 불가
	 *   - top level인 OxmSqlService만 사용할 수 있음
	 *  
	 */
	private class OxmSqlReader implements SqlReader {
		
		private final static String DEFAULT_SQLMAP_FILE = "sqlmap.xml";
		private Unmarshaller unmarshaller;
		private String sqlMapFile = OxmSqlReader.DEFAULT_SQLMAP_FILE;
		
		public void setUnmarshaller(Unmarshaller unmarshaller) {
			this.unmarshaller = unmarshaller;
		}

		public void setSqlMapFile(String sqlMapFile) {
			this.sqlMapFile = sqlMapFile;
		}

		@Override
		public void read(SqlRegistry sqlRegistry) {			
			try {
				Source source = new StreamSource(UserDao.class.getResourceAsStream(this.sqlMapFile));
				// OxmSqlService를 통해 전달받은 OXM interface 구현오브젝트로 unmarshalling 작업 수행
				Sqlmap sqlmap = (Sqlmap) this.unmarshaller.unmarshal(source);
				
				for (SqlType sql : sqlmap.getSql()) {
					sqlRegistry.registerSql(sql.getKey(), sql.getValue());
				}
				
			} catch (XmlMappingException | IOException e) {
				throw new IllegalArgumentException(this.sqlMapFile + "을 가져올 수 없습니다.", e);
			}
			
		}
		
	}

}
