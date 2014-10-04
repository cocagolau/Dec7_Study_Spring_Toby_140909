package me.dec7.learningtest.jdk.jaxb;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import me.dec7.user.sqlservice.jaxb.SqlType;
import me.dec7.user.sqlservice.jaxb.Sqlmap;

import org.junit.Test;

/*
 * unmarshalling
 *  - XML문서를 읽어서 자바 오브젝트로 변환하는 것을 JAXB에서 unmarshalling 이라함
 * 
 * marshalling
 *  - 바인딩 오브젝트를 XML문서로 변환하는 것
 *  
 * serialization (직렬화)
 *  - 자바 오브젝트를 바이트 스트림으로 바꾸는 것
 */
public class JaxbTest {

	@Test
	public void readSqlmap() throws JAXBException, IOException {
		String contextPath = Sqlmap.class.getPackage().getName();
		
		// 바인딩용 클래스들의 위치를 가지고 JAXBContext를 만듦
		JAXBContext context = JAXBContext.newInstance(contextPath);
		
		/*
		 *  unmarshaller 생성
		 *  unmarshal하면 매핑된 오브젝트 트리 루트인 Sqlmap을 반환
		 */
		Unmarshaller unmarshaller = context.createUnmarshaller();
		Sqlmap sqlmap = (Sqlmap) unmarshaller.unmarshal(getClass().getResourceAsStream("sqlmap.xml"));
		List<SqlType> sqlList = sqlmap.getSql();
		
		assertThat(sqlList.size(), is(3));
		assertThat(sqlList.get(0).getKey() ,is("add"));
		assertThat(sqlList.get(0).getValue() ,is("insert"));
		
		assertThat(sqlList.get(1).getKey() ,is("get"));
		assertThat(sqlList.get(1).getValue() ,is("select"));
		
		assertThat(sqlList.get(2).getKey() ,is("delete"));
		assertThat(sqlList.get(2).getValue() ,is("delete"));
		
		
	}

}
