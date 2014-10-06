package me.dec7.user.sqlservice;

import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import me.dec7.user.dao.UserDao;
import me.dec7.user.sqlservice.jaxb.SqlType;
import me.dec7.user.sqlservice.jaxb.Sqlmap;


/*
 * 7.3. 서비스 추상화
 * 
 * 개선점
 *  1. 자바는 다양한 매핑 기술 존재
 *  	- jaxb외에 다양함, 필요에 따라 다른 기술로 바꿀 수 있어야 함
 *  2. xml파일을 다양한 소스에서 가져올 수 있어야 함
 *  	- 현재는 UserDao 와 동등한 classpath에서만 읽어올 수 있음
 *  	- 임의의 클래스패스, 시스템의 절대 위치, http 프로토콜을 통한 원격지
 *  
 * OXM (Object XML Mapping)
 *  - XML과 JAVA 오브젝트 맵핑 기술
 *  1. Castor XML
 *  	- 설정파일이 필요없는 인트로스펙트 모드 지원
 *  2. JiBX
 *  	- 높은 성능
 *  3. XmlBeans
 *  	- 아파치 프로젝트 중 하나, xml 정보셋을 효과적 제공
 *  4. Xstream
 *  	- 관례를 이용해서 설정없는 바인딩 기술 지원
 *  
 * 서비스 추상화
 *  - OXM 기술은 상호 호환성 존재
 *  - 비슷한 api 제공
 *  - 사용목적 동일
 *  
 *  - 로우레벨의 구체적 기술 / api에 독립적
 * 
 * 
 */
public class JaxbXmlSqlReader implements SqlReader {
	
	/*
	 * 굳이 상수로 만들 필요는 없음
	 * 하지만, 의도가 코드에 분명히 드러남
	 */
	private static final String DEFAULT_SQLMAP_FILE = "sqlmap.xml";
	private String sqlMapFile = DEFAULT_SQLMAP_FILE;
	
	public void setSqlMapFile(String sqlMapFile) {
		this.sqlMapFile = sqlMapFile;
	}
	
	@Override
	public void read(SqlRegistry sqlRegistry) {
		String contextPath = Sqlmap.class.getPackage().getName();
		
		try {
			JAXBContext context = JAXBContext.newInstance(contextPath);
			
			/*
			 * Unmarshaller interface
			 *  - xml 파일에 대한 정보를 담은 source type의 오브젝트 전달시
			 *  - 설정에서 지정한 OXM 기술 사용 --> 자바 오브젝트 트리로 변환
			 *  - 루트 오브젝트 반환
			 * 
			 */
			Unmarshaller unmarshaller = context.createUnmarshaller();
			
			InputStream is = UserDao.class.getResourceAsStream(this.sqlMapFile);
			Sqlmap sqlmap = (Sqlmap) unmarshaller.unmarshal(is);
			
			for (SqlType sql : sqlmap.getSql()) {
				sqlRegistry.registerSql(sql.getKey(), sql.getValue());
			}
			
		} catch (JAXBException e) {
			
			throw new RuntimeException(e);
			
		}
		
	}

}
