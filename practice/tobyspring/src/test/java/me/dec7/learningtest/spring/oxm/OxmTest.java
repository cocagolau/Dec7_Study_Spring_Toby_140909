package me.dec7.learningtest.spring.oxm;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.List;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import me.dec7.user.sqlservice.jaxb.SqlType;
import me.dec7.user.sqlservice.jaxb.Sqlmap;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.oxm.Unmarshaller;
import org.springframework.oxm.XmlMappingException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;



/*
 * OxmTest
 *  - 더이상 Jaxb 라는 구체적 기술이 사용되지 않음
 *  - 단지, xml bean 설정에서만 바꾸기만 하면 됨
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:OxmTest-context.xml")
public class OxmTest {

	@Autowired
	Unmarshaller unmarshaller;
	
	@Test
	public void unmarshallSqlMap() throws XmlMappingException, IOException {
		Source xmlSource = new StreamSource(getClass().getResourceAsStream("sqlmap.xml"));
		
		Sqlmap sqlmap = (Sqlmap) this.unmarshaller.unmarshal(xmlSource);
		
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
