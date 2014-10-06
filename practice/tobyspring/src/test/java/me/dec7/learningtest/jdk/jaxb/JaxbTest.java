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

public class JaxbTest {

	@Test
	public void readSqlmap() throws JAXBException, IOException {
		String contextPath = Sqlmap.class.getPackage().getName();
		
		JAXBContext context = JAXBContext.newInstance(contextPath);
		
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
