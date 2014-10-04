package me.dec7.user.sqlservice;

public class DefaultSqlService extends BeanSqlService {

	public DefaultSqlService() {
		setSqlReader(new JaxbXmlSqlReader());
		setSqlRegistry(new HashMapSqlRegistry());
	}
}
