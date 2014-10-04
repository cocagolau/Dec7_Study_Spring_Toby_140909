package me.dec7.user.sqlservice;

import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import me.dec7.user.dao.UserDao;
import me.dec7.user.sqlservice.jaxb.SqlType;
import me.dec7.user.sqlservice.jaxb.Sqlmap;

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
