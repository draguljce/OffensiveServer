package offensive.Server.Utilities;

import java.util.List;

import offensive.Server.Server;
import offensive.Server.Hybernate.POJO.User;

import org.hibernate.Session;
import org.hibernate.Transaction;

public class HibernateUtil {
	public static List<?> executeHql(String hql, Session session) {
		List<?> results = null;
		try {
			results = session.createQuery(hql).list();
		}
		catch (Exception e) {
			Server.getServer().logger.error(e.getMessage(), e);
		}
		
		return results;
	}
	
	public static Object executeScalarHql(String hql, Session session) {
		@SuppressWarnings("unchecked")
		List<Object> allResults = (List<Object>) HibernateUtil.executeHql(hql, session);
		
		return allResults.get(0);
	}
	
	public static User getPojoUser(long userId, Session session) {
		Transaction tran = session.beginTransaction();

		User pojoUser; 
		try {
			pojoUser = (offensive.Server.Hybernate.POJO.User)session.get(offensive.Server.Hybernate.POJO.User.class, userId);
		} finally {
			tran.rollback();
		}
		
		return pojoUser;
	}
}
