package me.dec7.learningtest.spring.embeddeddb;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType.H2;

import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;

public class EmbeddedDbTest {

	EmbeddedDatabase db;
	SimpleJdbcTemplate template;
	
	@Before
	public void setUp() {
		db = new EmbeddedDatabaseBuilder()
			.setType(H2) // HSQL, DERBY, H2 세 가지 중 하나 선택 가능, 초기화 SQL 호환된다면 DB 종류는 언제든 변경 가능
			// 테이블 생성, 초기 데이터를 넣기 위한 스크립트 지정
			.addScript("classpath:/learningtest/embeddeddb/schema.sql")
			.addScript("classpath:/learningtest/embeddeddb/data.sql")
			.build();
		
		/*
		 * EmbeddedDatabase는 DataSource의 sub interface
		 */
		template = new SimpleJdbcTemplate(db);
	
	}
	
	/*
	 * 매 테스트 진행 뒤 DB 종료 
	 */
	@After
	public void tearDown() {
		db.shutdown();
	}
	
	// 초기화 스크립트 검증 테스트
	@Test
	public void initData() {
		assertThat(template.queryForInt("select count(*) from sqlmap"), is(2));
		
		List<Map<String, Object>> list = template.queryForList("select * from sqlmap order by key_");
		assertThat((String)list.get(0).get("key_"), is("KEY1"));
		assertThat((String)list.get(0).get("sql_"), is("SQL1"));
		assertThat((String)list.get(1).get("key_"), is("KEY2"));
		assertThat((String)list.get(1).get("sql_"), is("SQL2"));
	}
	
	@Test
	public void insert()  {
		template.update("insert into sqlmap(key_, sql_) values(?, ?)", "KEY3", "SQL3");
		assertThat(template.queryForInt("select count(*) from sqlmap"), is(3));
		
	}

}
